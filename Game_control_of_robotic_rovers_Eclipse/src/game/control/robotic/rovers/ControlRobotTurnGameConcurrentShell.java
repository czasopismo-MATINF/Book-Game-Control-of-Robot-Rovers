package game.control.robotic.rovers;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import game.control.robotic.rovers.board.MotherShip;
import game.control.robotic.rovers.command.CommandMethodArgumentException;
import game.control.robotic.rovers.command.GamePlayCommandAnnotation;
import game.control.robotic.rovers.command.GameStatusCommandAnnotation;
import game.control.robotic.rovers.command.PromptCommand;

public class ControlRobotTurnGameConcurrentShell implements Callable<Boolean> {

	protected ControlRobotTurnGameBoardAndCommands game;

	protected Map<Integer, ControlRobotsTurnGameRobotAI> robotAIs = new ConcurrentHashMap<>();

	protected int NUMBER_OF_THREADS_IN_AI_THREAD_POOL = 10;
	protected int ON_TURN_COMMIT_PAUSE_MILLISECONDS = 1000;

	public ControlRobotTurnGameConcurrentShell(ControlRobotTurnGameBoardAndCommands game) {

		this.game = game;
		this.game.getPlanet().getRobots().stream().forEach(r -> {
			robotAIs.put(r.getId(), new ControlRobotsTurnGameRobotAI(this));
		});

	}

	protected String runCommandThrowsExceptions(String commandLine, int currentRobot)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		PromptCommand command = new PromptCommand(commandLine);

		Method m = null;

		try {
			m = this.game.getClass().getMethod(command.camelCasedKeyWords, Integer.class, PromptCommand.class);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		if (m != null && m.isAnnotationPresent(GamePlayCommandAnnotation.class)) {

			m.invoke(this.game, currentRobot, command);
			return null;

		}
		if (m != null && m.isAnnotationPresent(GameStatusCommandAnnotation.class)) {

			return (String) m.invoke(this.game, currentRobot, command);

		}

		return null;

	}

	protected String runCommand(String commandLine, int currentRobot) {

		try {
			return this.runCommandThrowsExceptions(commandLine, currentRobot);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	protected Integer getRobotAIId(ControlRobotsTurnGameRobotAI robotAI) {

		return this.robotAIs.entrySet().stream().filter(e -> e.getValue() == robotAI).findAny().get().getKey();

	}

	public String runCommand(String commandLine, ControlRobotsTurnGameRobotAI robotAI) {

		Integer currentRobot = this.getRobotAIId(robotAI);
		return (currentRobot != null) ? this.runCommand(commandLine, currentRobot) : null;

	}

	@Override
	public Boolean call() throws Exception {
		return this.runTurns();
	}

	protected boolean runTurns() {
		
		while(true) {
			
			if (this.runTurn() == true) {
				return true;
			}
			
			try {
				Thread.sleep(this.ON_TURN_COMMIT_PAUSE_MILLISECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// if all robots run out of energy, return false
			
		}

	}

	protected boolean runTurn() {

		ExecutorService threadPool = Executors.newFixedThreadPool(this.NUMBER_OF_THREADS_IN_AI_THREAD_POOL);
		
		List<Future<Boolean>> responses = new ArrayList<>();
		
		this.robotAIs.entrySet().stream().forEach(e -> {
			
			Future<Boolean> response = threadPool.submit(e.getValue());
			responses.add(response);

		});

		for (var response : responses) {
			try {
				response.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			this.game.turnCommit(new PromptCommand("turn commit"));
		} catch (CommandMethodArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MotherShip motherShip = this.game.getPlanet().getMotherShip();

		if(motherShip != null && motherShip.isLaunched()) {

			return true;
		}

		return false;

	}

}
