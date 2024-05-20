package game.control.robot.rovers.prompt;

public interface PromptPrinterInterface {

	final static String COMMAND_LINE_PROMPT = ">";

	void print(String text);

	void println(String text);

	void println();

	void print(String[] stringArray);

	void println(String[] stringArray);

}
