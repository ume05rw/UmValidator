package jp.dobes.umvalidator.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * カレントワークベンチ情報保持
 *
 * @author ikaruga
 */
public class WorkbenchState {
	private IWorkbench workbench;
	private IWorkbenchWindow window;
	private IWorkbenchPage page;
	private IEditorPart editorpart;
	private AbstractTextEditor abeditor;
	private IDocument document;
	private IFile file;

	/**
	 * コンストラクタ
	 */
	public WorkbenchState(){
		//System.out.println("WorkbenchState.constructor");

		this.refresh();
	}

	/**
	 * カレント状態を取得する。
	 */
	public void refresh(){
		//System.out.println("WorkbenchState.refresh");

		//現在保持中の参照を初期化する。
		this.workbench = null;
		this.window = null;
		this.page = null;
		this.editorpart = null;
		this.abeditor = null;
		this.document = null;
		this.file = null;

		//取得可能なオブジェクトから順次取得する。
		try{
			this.workbench = PlatformUI.getWorkbench();
			if (this.workbench == null) return;

			this.window = this.workbench.getActiveWorkbenchWindow();
			if (this.window == null) return;

			this.page = this.window.getActivePage();
			if (this.page == null) return;

			this.editorpart = page.getActiveEditor();
			if (this.editorpart == null) return;

			this.abeditor = (AbstractTextEditor)this.editorpart;
			if (this.abeditor == null) return;

			this.document = this.abeditor.getDocumentProvider().getDocument(this.abeditor.getEditorInput());
			if (this.document == null) return;

			this.file = ((IFileEditorInput)this.abeditor.getEditorInput()).getFile();
		} catch(Exception ex){
			ex.printStackTrace();
			Console.log("Validator.WorkbenchState.constructor Error: " + ex.getMessage());
		}
	}

	/**
	 * 現在編集中のドキュメントを返す。
	 *
	 * @return IDocument
	 */
	public IDocument getDocument(){
		//System.out.println("WorkbenchState.getDocument");

		return this.document;
	}

	/**
	 * 現在編集中のファイルを返す。
	 *
	 * @return IFile
	 */
	public IFile getFile(){
		//System.out.println("WorkbenchState.getFile");

		return this.file;
	}

	/**
	 * インスタンスを破棄する。
	 */
	public void dispose(){
		this.workbench = null;
		this.window = null;
		this.page = null;
		this.editorpart = null;
		this.abeditor = null;
		this.document = null;
		this.file = null;
	}
}