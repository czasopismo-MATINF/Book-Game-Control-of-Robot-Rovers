package game.control.robotic.rovers.prompt;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PromptCommandHelper implements PromptCommandHelperInterface {

	public String makeCamelCased(String[] words) {

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
	
}
