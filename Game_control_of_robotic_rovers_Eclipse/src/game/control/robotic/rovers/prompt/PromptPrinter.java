package game.control.robotic.rovers.prompt;

public class PromptPrinter implements PromptPrinterInterface {

	public void print(String text) {
		System.out.print(text);
	}

	public void println(String text) {
		System.out.println(text);
	}

	public void println() {
		System.out.println();
	}

	public void print(String[] stringArray) {
		for (String s : stringArray) {
			this.print(s);
			this.print("|");
		}
	}

	public void println(String[] stringArray) {
		for (String s : stringArray) {
			this.print(s);
			this.print("|");
		}
		this.println();
	}

}
