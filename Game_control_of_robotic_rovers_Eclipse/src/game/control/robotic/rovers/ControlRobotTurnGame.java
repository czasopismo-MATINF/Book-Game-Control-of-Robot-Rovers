package game.control.robotic.rovers;

import game.control.robotic.rovers.prompt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import game.control.robotic.rovers.prompt.PromptCommand;
import game.control.robotic.rovers.prompt.PromptCommandAnnotation;

public class ControlRobotTurnGame {

	public class CommandMethodNotFoundException extends Exception {
	}

	@PromptCommandAnnotation
	public void testCommand(PromptCommand command, PromptPrinterInterface printer) {
		printer.println("Test Command run.");
		printer.println(command.arguments);
	}

	public void runCommand(PromptCommand command, PromptPrinterInterface printer) {
		try {
			Method m = this.getClass().getMethod(command.camelCasedKeyWords, PromptCommand.class,
					PromptPrinterInterface.class);
			if (m != null && m.isAnnotationPresent(PromptCommandAnnotation.class)) {
				m.invoke(this, command, printer);
			} else {
				throw new CommandMethodNotFoundException();
			}

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommandMethodNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

}
