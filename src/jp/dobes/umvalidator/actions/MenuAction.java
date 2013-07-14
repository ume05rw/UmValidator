package jp.dobes.umvalidator.actions;

import jp.dobes.umvalidator.util.Validator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * メニュー操作時のアクション定義
 *
 * @author ikaruga
 *
 */
public class MenuAction extends AbstractHandler implements IWorkbenchWindowActionDelegate {

	private static Validator validator;

	/**
	 * コンストラクタ
	 */
	public MenuAction(){
		MenuAction.validator = new Validator();
	}

	/**
	 * メニュー選択時に実行される関数
	 *
	 * メニュー表示とクラスの関連付けは、plugin.xmlに記述。
	 */
	@Override
	public void run(IAction arg0) {
		//System.out.println("MenuAction.run");
		//System.out.println(arg0.getId());

		//メニュー要素のID属性を参照して処理を分岐。
		if (arg0.getId().equals("execute")){
			MenuAction.validator.execMarking();
		} else if (arg0.getId().equals("release")){
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

	/**
	 * インスタンスを破棄する。
	 */
	public void dispose(){
		try{
			MenuAction.validator.dispose();
			MenuAction.validator = null;
		} catch(Exception ex){}
	}

}
