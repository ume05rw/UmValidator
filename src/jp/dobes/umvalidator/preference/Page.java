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

    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(
            Initializer.IS_OUT_CONSOLE, Messages.PREF_IS_OUT_CONSOLE, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_MBSPACE, Messages.PREF_IS_DETECT_MBSPACE, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_TAB, Messages.PREF_IS_DETECT_TAB, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_VARNAME_CAMEL, Messages.PREF_IS_DETECT_VARNAME_CAMEL, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_VARNAME_USCORE, Messages.PREF_IS_DETECT_VARNAME_USCORE, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_CRLF, Messages.PREF_IS_DETECT_CRLF, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_START_BRACKETS, Messages.PREF_IS_DETECT_START_BRACKETS, getFieldEditorParent()
        ));
        addField(new BooleanFieldEditor(
            Initializer.IS_DETECT_AFTER_COMMA, Messages.PREF_IS_DETECT_AFTER_COMMA, getFieldEditorParent()

        ));
        //addField(new BooleanFieldEditor(
        //	Initializer.IS_DETECT_FUNCTION_RETURN, Messages.PREF_IS_DETECT_FUNCTION_RETURN, getFieldEditorParent()
        //));
    }

}
