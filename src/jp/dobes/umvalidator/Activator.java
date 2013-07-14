package jp.dobes.umvalidator;

import jp.dobes.umvalidator.util.Validator;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * プラグイン開始ポイント定義
 *
 * @author ikaruga
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	//プラグインID
	public static final String PLUGIN_ID = "jp.dobes.umvalidator"; //$NON-NLS-1$

	//自身のsharedインスタンス
	private static Activator plugin;

	//sharedのバリデータインスタンス
	private static Validator validator;

	/**
	 * コンストラクタ
	 */
	public Activator() {
		//System.out.println("Activator.Constructor");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		//System.out.println("Activator.start");
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * sharedインスタンスを返す。
	 *
	 * @return jp.dobes.umvalidator.Activator(shared)
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * スタートアップ時の初期化関数
	 *
	 * ※スタートアップ時のフック定義は、plugin.xmlを参照のこと。
	 */
	@Override
	public void earlyStartup() {
		//System.out.println("Activator.earlyStartup");
		Activator.validator = new Validator();
		Activator.validator.refreshMarkers(true);

		this.hookOnCommand("org.eclipse.ui.file.save");
		this.hookOnCommand("org.eclipse.ui.file.saveAll");
	}

	/**
	 * カレントウインドウ操作時のイベントをフックする。
	 *
	 * ※AnyEditToolsソースから拝領。
	 * @param commandId
	 */
	private void hookOnCommand(String commandId) {
		//System.out.println("Activator.hookOnCommand");
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand(commandId);
		command.addExecutionListener(new IExecutionListener(){

			@Override
			public void notHandled(String arg0, NotHandledException arg1) {
			}

			@Override
			public void postExecuteFailure(String arg0, ExecutionException arg1) {
			}

			@Override
			public void postExecuteSuccess(String arg0, Object arg1) {
			}

			@Override
			public void preExecute(String arg0, ExecutionEvent arg1) {
				if (!Activator.validator.isWorking()){
					Activator.validator.execMarking();
				}
			}
		});
	}

}
