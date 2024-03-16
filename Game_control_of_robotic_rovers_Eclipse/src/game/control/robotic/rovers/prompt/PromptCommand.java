package game.control.robotic.rovers.prompt;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PromptCommand {

	public String command;
	public String keyWords;
	public String arguments;
	public String[] keyWordsArray;
	public String[] argumentsArray;
	public String camelCasedKeyWords;

	public PromptCommand(String command, PromptCommandHelperInterface helper) {

		this.command = command;

		String[] commandSplit = this.command.split(PromptCommandConfigInterface.SPLIT_KEYWORDS_ARGUMENTS, 2);
		this.keyWords = commandSplit[0];
		this.keyWordsArray = commandSplit[0].trim().split(PromptCommandConfigInterface.SPLIT_REGEX);
		this.arguments = null;
		this.argumentsArray = new String[0];
		if (commandSplit.length >= 2) {
			this.arguments = commandSplit[1];
			this.argumentsArray = commandSplit[1].trim().split(PromptCommandConfigInterface.SPLIT_REGEX);
		}

		this.camelCasedKeyWords = helper.makeCamelCased(this.keyWordsArray);

	}

}
