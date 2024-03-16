package game.control.robotic.rovers;

import game.control.robotic.rovers.prompt.*;
import java.util.Scanner;

import game.control.robotic.rovers.prompt.PromptCommand;
import game.control.robotic.rovers.prompt.PromptCommandHelper;
import game.control.robotic.rovers.prompt.PromptCommandHelperInterface;
import game.control.robotic.rovers.prompt.PromptPrinter;
import game.control.robotic.rovers.prompt.PromptPrinterConfigInterface;

public class Main {

	PromptPrinterInterface promptPrinter = new PromptPrinter();
	PromptCommandHelperInterface promptCommandHelper = new PromptCommandHelper();

	ControlRobotTurnGame game = new ControlRobotTurnGame();

	public void run() {
		try (Scanner scanner = new Scanner(System.in)) {
			for (;;) {
				this.promptPrinter.print(PromptPrinterConfigInterface.COMMAND_LINE_PROMPT);
				String commandLine = scanner.nextLine();
				PromptCommand promptCommand = new PromptCommand(commandLine, this.promptCommandHelper);

				if ("exit".equals(promptCommand.camelCasedKeyWords)) {
					this.promptPrinter.println("EXIT");
					System.exit(0);
				}

				this.game.runCommand(promptCommand, this.promptPrinter);
			}
		}
	}

	public static void main(String[] args) {

		Main main = new Main();
		main.run();

	}

}
