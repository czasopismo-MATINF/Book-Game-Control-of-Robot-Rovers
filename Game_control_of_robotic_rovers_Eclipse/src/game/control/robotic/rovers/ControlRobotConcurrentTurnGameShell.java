package game.control.robotic.rovers;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import game.control.robotic.rovers.command.CommandMethodArgumentException;
import game.control.robotic.rovers.command.GamePlayCommandAnnotation;
import game.control.robotic.rovers.command.GameStatusCommandAnnotation;
import game.control.robotic.rovers.command.PromptCommand;

public class ControlRobotConcurrentTurnGameShell implements Callable<Boolean> {

	protected ControlRobotTurnGameBoardAndCommands game;

	protected Map<Integer, RobotAI> robotAIs = new ConcurrentHashMap<>();

	protected int NUMBER_OF_THREADS_IN_AI_THREAD_POOL = 10;

	public ControlRobotConcurrentTurnGameShell(ControlRobotTurnGameBoardAndCommands game) {

		this.game = game;
		this.game.getPlanet().getRobots().stream().forEach(r -> {
			robotAIs.put(r.getId(), new RobotAI(this));
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

	protected Integer getRobotAIId(RobotAI robotAI) {

		return this.robotAIs.entrySet().stream().filter(e -> e.getValue() == robotAI).findAny().get().getKey();

	}

	public String runCommand(String commandLine, RobotAI robotAI) {

		Integer currentRobot = this.getRobotAIId(robotAI);
		return (currentRobot != null) ? this.runCommand(commandLine, currentRobot) : null;

	}

	@Override
	public Boolean call() throws Exception {
		return this.runTurns();
	}

	protected Boolean runTurns() {

		while (true) {

			if (this.runTurn()) {
				return true;
			}
			int totalRobotEnergy = this.game.getPlanet().getRobots().stream().collect(Collectors.summingInt(r -> {
				return Arrays.asList(r.getBatteries()).stream().collect(Collectors.summingInt(b -> b.getEnergy()));
			}));
			if (totalRobotEnergy == 0) {
				return false;
			}

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

		if (this.game.getPlanet().getMotherShip().isLaunched()) {
			return true;
		}
		return false;

	}

}
