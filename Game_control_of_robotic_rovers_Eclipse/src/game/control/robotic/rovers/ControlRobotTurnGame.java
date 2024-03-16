package game.control.robotic.rovers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Annotation;

public class ControlRobotTurnGame {

	public class CommandMethodNotFoundException extends Exception {
	}

	@PromptCommandAnnotation
	public void testCommand(PromptCommand command) {
		Main.println("Test Command run.");
		Main.println(command.arguments);
	}

	public void runCommand(PromptCommand command) {
		try {

			Method m = this.getClass().getMethod(command.camelCasedKeyWords, PromptCommand.class);
			if (m != null && m.isAnnotationPresent(PromptCommandAnnotation.class)) {
				m.invoke(this, command);
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
