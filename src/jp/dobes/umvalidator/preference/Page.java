package jp.dobes.umvalidator.preference;

import jp.dobes.umvalidator.Activator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 設定画面
 * 
 * @author ikaruga
 *
 */
public class Page extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * コンストラクタ
	 */
	public Page(){
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
			Initializer.IS_OUT_CONSOLE, "バリデート結果をコンソールに出力する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_MBSPACE, "全角スペースを検出する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_TAB, "タブを検出する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_VARNAME_CAMEL, "キャメル記法を検出する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_VARNAME_USCORE, "アンダースコア記法を検出する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_CRLF, "改行コードCRLFを検出する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_START_BRACKETS, "開始括弧( { , [ )の後、改行せずに記述されている行を検出する。", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_AFTER_COMMA, "配列・オブジェクトの末尾要素の末端カンマ( , )を検出する。", getFieldEditorParent()
		));
		//addField(new BooleanFieldEditor(
		//	Initializer.IS_DETECT_FUNCTION_RETURN, "returnが存在しないfunction定義を検出する。", getFieldEditorParent()
		//));
	}

}
