package game.control.robot.rovers;

import java.util.Scanner;

import game.control.robot.rovers.prompt.*;

public class Main {

	PromptPrinterInterface promptPrinter = new PromptPrinter();

	ControlRobotTurnGameShell gameShell = new ControlRobotTurnGameShell();

	public void run() {

		try (Scanner scanner = new Scanner(System.in)) {

			for (;;) {

				this.promptPrinter.print(PromptPrinterInterface.COMMAND_LINE_PROMPT);
				String commandLine = scanner.nextLine();

				if ("exit".equals(commandLine)) {
					this.promptPrinter.println("EXIT");
					System.exit(0);
				}

				String response = this.gameShell.runCommand(commandLine);
				if (response != null) {
					promptPrinter.println(response);
				}

			}
		}

	}

	public static void main(String[] args) {

		Main main = new Main();
		main.run();

	}

}
