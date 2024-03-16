package game.control.robotic.rovers;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PromptCommand {

	String command;
	String keyWords;
	String arguments;
	String[] keyWordsArray;
	String[] argumentsArray;
	String camelCasedKeyWords;

	static final String SPLIT_KEYWORDS_ARGUMENTS = ">";
	static final String SPLIT_REGEX = "\\s+";

	static String makeCamelCased(String[] words) {

		String out = Arrays.asList(words).stream()
				.map(w -> (w.length() > 0) ? (w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase()) : w)
				.collect(Collectors.joining());

		/*
		String out = "";
		for (String w : words) {
			if (w.length() > 0) {
				out += w.substring(0, 1).toUpperCase() + w.toLowerCase().substring(1);
			}
		}
		*/

		if (out.length() > 0) {
			return out.substring(0, 1).toLowerCase() + out.substring(1);
		} else {
			return out;
		}

	}

	public PromptCommand(String command) {

		this.command = command;

		String[] commandSplit = this.command.split(PromptCommand.SPLIT_KEYWORDS_ARGUMENTS, 2);
		this.keyWords = commandSplit[0];
		this.keyWordsArray = commandSplit[0].trim().split(PromptCommand.SPLIT_REGEX);
		this.arguments = null;
		this.argumentsArray = new String[0];
		if (commandSplit.length >= 2) {
			this.arguments = commandSplit[1];
			this.argumentsArray = commandSplit[1].trim().split(PromptCommand.SPLIT_REGEX);
		}

		this.camelCasedKeyWords = PromptCommand.makeCamelCased(this.keyWordsArray);

	}

}
