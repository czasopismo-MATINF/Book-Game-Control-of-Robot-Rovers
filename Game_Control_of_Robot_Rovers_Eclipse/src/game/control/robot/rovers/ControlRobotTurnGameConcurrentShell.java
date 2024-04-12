package game.control.robot.rovers;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import game.control.robot.rovers.actions.END_OF_TURN_COMMAND;
import game.control.robot.rovers.actions.MESSAGE_COMMAND;
import game.control.robot.rovers.actions.TURN_COMMIT_COMMAND;
import game.control.robot.rovers.board.MotherShip;
import game.control.robot.rovers.command.PromptCommand;

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

	protected Integer getRobotAIId(ControlRobotsTurnGameRobotAI robotAI) {

		return this.robotAIs.entrySet().stream().filter(e -> e.getValue() == robotAI).findAny().get().getKey();

	}

	public String runCommand(String commandLine, ControlRobotsTurnGameRobotAI robotAI) {

		Integer currentRobot = this.getRobotAIId(robotAI);

		if (currentRobot == null)
			return null;

		PromptCommand promptCommand = new PromptCommand(commandLine);

		try {

			return this.game.runMessageCommand(MESSAGE_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					MESSAGE_COMMAND.MODE.CONCURRENT, promptCommand, currentRobot);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		try {

			this.game.runEndOfTurnCommand(END_OF_TURN_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					promptCommand, currentRobot);
			return null;

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public Boolean call() throws Exception {

		return this.runTurns();

	}

	protected boolean runTurns() {

		while (true) {

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
			if (this.game.getPlanet().getRobots().stream()
					.collect(Collectors.summingInt(r -> r.getTotalEnergy())) == 0) {
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

			PromptCommand promptCommand = new PromptCommand("turn commit");
			this.game.runTurnCommitCommand(TURN_COMMIT_COMMAND.valueOf(promptCommand.underscoreCasedKeyWords),
					promptCommand);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MotherShip motherShip = this.game.getPlanet().getMotherShip();

		if (motherShip != null && motherShip.isLaunched()) {

			return true;
		}

		return false;

	}

}
