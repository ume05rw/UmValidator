package jp.dobes.umvalidator.preference;

import jp.dobes.umvalidator.Activator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * �ݒ���
 * 
 * @author ikaruga
 *
 */
public class Page extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * �R���X�g���N�^
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
			Initializer.IS_OUT_CONSOLE, "�o���f�[�g���ʂ��R���\�[���ɏo�͂���B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_MBSPACE, "�S�p�X�y�[�X�����o����B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_TAB, "�^�u�����o����B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_VARNAME_CAMEL, "�L�������L�@�����o����B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_VARNAME_USCORE, "�A���_�[�X�R�A�L�@�����o����B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_CRLF, "���s�R�[�hCRLF�����o����B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_START_BRACKETS, "�J�n����( { , [ )�̌�A���s�����ɋL�q����Ă���s�����o����B", getFieldEditorParent()
		));
		addField(new BooleanFieldEditor(
			Initializer.IS_DETECT_AFTER_COMMA, "�z��E�I�u�W�F�N�g�̖����v�f�̖��[�J���}( , )�����o����B", getFieldEditorParent()
		));
		//addField(new BooleanFieldEditor(
		//	Initializer.IS_DETECT_FUNCTION_RETURN, "return�����݂��Ȃ�function��`�����o����B", getFieldEditorParent()
		//));
	}

}
