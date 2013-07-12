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
 * Get and Keep the Window set the current state of, and documents state.
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

	public WorkbenchState(){
		//System.out.println("WorkbenchState.constructor");

		this.refresh();
	}

	public void refresh(){
		//System.out.println("WorkbenchState.refresh");

		//Remove any instance of reference during holding.
		this.workbench = null;
		this.window = null;
		this.page = null;
		this.editorpart = null;
		this.abeditor = null;
		this.document = null;
		this.file = null;

		//The sequentially obtained from the superior object, will continue to hold.
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

	public IDocument getDocument(){
		//System.out.println("WorkbenchState.getDocument");

		return this.document;
	}
	public IFile getFile(){
		//System.out.println("WorkbenchState.getFile");

		return this.file;
	}

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