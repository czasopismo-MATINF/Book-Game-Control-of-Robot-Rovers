package game.control.robotic.rovers;

public class PromptPrinter {

	void print(String text) {
		System.out.print(text);
	}

	void println(String text) {
		System.out.println(text);
	}

	void println() {
		System.out.println();
	}

	void print(String[] stringArray) {
		for (String s : stringArray) {
			this.print(s);
			this.print("|");
		}
	}

	void println(String[] stringArray) {
		for (String s : stringArray) {
			this.print(s);
			this.print("|");
		}
		this.println();
	}

}
