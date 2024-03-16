package game.control.robotic.rovers;

import java.util.Scanner;

public class Main {

	static final String COMMAND_LINE_PROMPT = ">";

	static void print(String text) {
		System.out.print(text);
	}

	static void println(String text) {
		System.out.println(text);
	}

	static void println() {
		System.out.println();
	}

	static void print(String[] stringArray) {
		for (String s : stringArray) {
			Main.print(s);
			Main.print("|");
		}
	}

	static void println(String[] stringArray) {
		for (String s : stringArray) {
			Main.print(s);
			Main.print("|");
		}
		Main.println();
	}

	ControlRobotTurnGame game = new ControlRobotTurnGame();

	public void run() {
		try (Scanner scanner = new Scanner(System.in)) {
			for (;;) {
				Main.print(Main.COMMAND_LINE_PROMPT);
				String commandLine = scanner.nextLine();
				PromptCommand promptCommand = new PromptCommand(commandLine);
				this.game.runCommand(promptCommand);
			}
		}
	}

	public static void main(String[] args) {

		Main main = new Main();
		main.run();

	}

}
