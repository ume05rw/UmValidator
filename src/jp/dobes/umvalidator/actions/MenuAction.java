package jp.dobes.umvalidator.actions;

import jp.dobes.umvalidator.util.Console;
import jp.dobes.umvalidator.util.Validator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class MenuAction extends AbstractHandler implements IWorkbenchWindowActionDelegate {

	private static Validator validator;


	public MenuAction(){
		MenuAction.validator = new Validator();
	}

	@Override
	public void run(IAction arg0) {
		//System.out.println("MenuAction.run");
		//System.out.println(arg0.getId());

		if (arg0.getId().indexOf("execute") != -1){
			MenuAction.validator.execMarking();
		} else if (arg0.getId().indexOf("release") != -1){
			MenuAction.validator.refreshMarkers(true);
		}
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		return null;
	}

	@Override
	public void init(IWorkbenchWindow arg0) {
	}

	public void dispose(){
		try{
			MenuAction.validator.dispose();
			MenuAction.validator = null;
		} catch(Exception ex){}
	}

}
