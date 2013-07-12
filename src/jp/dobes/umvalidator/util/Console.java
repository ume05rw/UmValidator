package jp.dobes.umvalidator.util;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {

	private static MessageConsole console = null;
	private static MessageConsoleStream logger = null;


	public static void log(String msg){
		if ((Console.logger == null) || (Console.console == null)){
			if (!Console.initLogger()){
				return;
			}
		}

		Console.logger.println(msg);
	}
	
	public static void clear(){
		if (Console.console == null){
			Console.console = findConsole("UmValidator");
		}
		
		Console.console.clearConsole();
	}

	public static void close(){
		if (Console.logger != null){
			try{
				Console.logger.close();
			} catch(Exception ex){}

			Console.console = null;
			Console.logger = null;
		}
	}

	/**
	 * Initialize the console output Stream object.
	 */
	private static boolean initLogger(){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page == null){
			Console.close();
			return false;
		}

		Console.console = findConsole("UmValidator");
		Console.logger = Console.console.newMessageStream();

		try{
			//Get a console View the current page.
			IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);

			//Set the console view acquired or generated.
			view.display(Console.console);
		} catch(Exception ex){
			Console.close();
			return false;
		}
		return true;
	}

	/**
	 * Console object acquisition / generation
	 * Reference: http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F
	 * @param name
	 * @return
	 */
	private static MessageConsole findConsole(String name) {
		//Get the console of the current View
		ConsolePlugin plugin = ConsolePlugin.getDefault();

		//Get the console manager object
		IConsoleManager conMan = plugin.getConsoleManager();

		//Get an array of console object already generated.
		IConsole[] existing = conMan.getConsoles();

		for (int i = 0; i < existing.length; i++)
			//When passing-name console existing, Return the value of existing console object.
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];

		//When passing-name console is not found, Generate a new console.
		MessageConsole myConsole = new MessageConsole(name, null);

		//Add a new console to console manager
		conMan.addConsoles(new IConsole[]{myConsole});

		//Return value to console generated.
		return myConsole;
	}

	public static void dispose(){
		Console.close();
	}
}
