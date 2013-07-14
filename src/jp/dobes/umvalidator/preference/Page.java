package jp.dobes.umvalidator.preference;

import jp.dobes.umvalidator.Activator;
import jp.dobes.umvalidator.Messages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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

	/**
	 * 設定画面を生成する。
	 */
	@Override
	protected void createFieldEditors() {
		//バリデート結果をコンソールに出力する。
		addField(new BooleanFieldEditor(
			Initializer.IS_OUT_CONSOLE, Messages.PREF_IS_OUT_CONSOLE, getFieldEditorParent()
		));

		//全角スペースを検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_MBSPACE, Messages.PREF_IS_DETECT_MBSPACE, getFieldEditorParent()
		));

		//タブを検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_TAB, Messages.PREF_IS_DETECT_TAB, getFieldEditorParent()
		));

		//キャメル記法を検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_VARNAME_CAMEL, Messages.PREF_IS_DETECT_VARNAME_CAMEL, getFieldEditorParent()
		));

		//アンダースコア記法を検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_VARNAME_USCORE, Messages.PREF_IS_DETECT_VARNAME_USCORE, getFieldEditorParent()
		));

		//改行コードCRLFを検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_CRLF, Messages.PREF_IS_DETECT_CRLF, getFieldEditorParent()
		));

		//開始括弧( { )の後、改行せずに記述されている行を検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_START_BRACKETS, Messages.PREF_IS_DETECT_START_BRACKETS, getFieldEditorParent()
		));

		//配列・オブジェクトの末尾要素の末端カンマ( , )を検出する。
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_AFTER_COMMA, Messages.PREF_IS_DETECT_AFTER_COMMA, getFieldEditorParent()

		));
		//addField(new BooleanFieldEditor(
		//	Initializer.IS_DETECT_FUNCTION_RETURN, Messages.PREF_IS_DETECT_FUNCTION_RETURN, getFieldEditorParent()
		//));
	}

}
