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

	public PromptCommand(String command, PromptCommandHelper helper, PromptCommandConfig config) {

		this.command = command;

		String[] commandSplit = this.command.split(config.SPLIT_KEYWORDS_ARGUMENTS, 2);
		this.keyWords = commandSplit[0];
		this.keyWordsArray = commandSplit[0].trim().split(config.SPLIT_REGEX);
		this.arguments = null;
		this.argumentsArray = new String[0];
		if (commandSplit.length >= 2) {
			this.arguments = commandSplit[1];
			this.argumentsArray = commandSplit[1].trim().split(config.SPLIT_REGEX);
		}

		this.camelCasedKeyWords = helper.makeCamelCased(this.keyWordsArray);

	}

}
