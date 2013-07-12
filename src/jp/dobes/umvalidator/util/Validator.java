package jp.dobes.umvalidator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.dobes.umvalidator.Activator;
import jp.dobes.umvalidator.preference.Initializer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class Validator {

	//Error message constants
	private static final String ERRMSG_DETECT_MB_SPACE = "�S�p�X�y�[�X�����o���܂���";
	private static final String ERRMSG_DETECT_TAB = "�^�u�����o���܂���";
	private static final String ERRMSG_DETECT_VARNAME_CAMEL = "�L�������L�@�����o���܂���";
	private static final String ERRMSG_DETECT_VARNAME_USCORE = "�A���_�[�X�R�A�L�@�����o���܂���";
	private static final String ERRMSG_DETECT_CRLF = "���s�R�[�hCRLF�����o���܂���";
	private static final String ERRMSG_STRING_AFTER_START_BRACKETS = "�J�n����( { , [ )�̌�A���s�����ɋL�q����Ă���s�����o���܂���";
	private static final String ERRMSG_LAST_ELEMENTS_AFTER_COMMA = "�z��E�I�u�W�F�N�g�̖����v�f�̖��[�J���}( , )�����o���܂���";
	private static final String ERRMSG_FUNCTION_CLOSE_WITHOUT_RETURN = "return�����݂��Ȃ�function��`�����o���܂����B";

	private static WorkbenchState state;
	private static boolean is_out_console;
	private static boolean is_working;

	/**
	 * Constructor
	 */
	public Validator(){
		//System.out.println("Validator.constructor");

		Validator.is_working = false;
		Validator.state = new WorkbenchState();
	}
	
	public boolean isWorking(){
		return Validator.is_working;
	}


	/**
	 * �{�v���O�C�����ǉ������}�[�J�[���폜����B
	 */
	public void refreshMarkers(boolean isRefreshState){
		//System.out.println("Validator.refreshMarkers");
		Console.clear();

		//�J�����g�G�f�B�^���擾�o�����Ƃ��A�{�v���O�C���ŃZ�b�g�����}�[�J�[���폜����B
		if (isRefreshState) Validator.state.refresh();
		IFile file = Validator.state.getFile();
		if (file == null) return;

		IMarker[] mkrs;
		String source_id;
		try{
			mkrs = file.findMarkers(null, true, IResource.DEPTH_INFINITE);
		} catch(Exception ex2){
			return;
		}
		try {
			for(IMarker obj : mkrs){
				
				try{
					source_id = obj.getAttribute(IMarker.SOURCE_ID).toString();
				} catch(Exception ex){
					continue;
				}
				
				if (source_id.indexOf("UM_VALIDATOR") != -1){
					try {
						obj.delete();
					} catch (Exception ex) {
						ex.printStackTrace();
						Console.log("Validator.refreshMarkers Error1: " + ex.toString());
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Console.log("Validator.refreshMarkers Error2: " + ex.getMessage());
		}
	}

	/**
	 * �o���f�[�^�����s����B
	 */
	public void execMarking(){
		//System.out.println("Validator.execMarking");
		Validator.is_working = true; //�ғ��t���O��ON�ɂ���B�}�[�J�[�Z�b�g����command.preExecute�����邱�Ƃ����邽�߁B

		Validator.state.refresh();
		Console.clear();
		
		IFile file = Validator.state.getFile();
		IDocument doc = Validator.state.getDocument();
		if ((file == null) || (doc == null)) return;

		this.refreshMarkers(false);

		int tmpIdx;
		Matcher mtc;
		Matcher mtctmp;
		//Matcher mtcCamel = Pattern.compile("[a-zA-Z0-9]+_[a-zA-Z0-9]+").matcher(doc.get());
		//Matcher mtcUnderscore = Pattern.compile("[a-z0-9]*[A-Z]+[a-zA-Z0-9]+").matcher(doc.get());
		Pattern camel = Pattern.compile("[a-z0-9]*[A-Z]+[a-zA-Z0-9]+");
		Pattern underscore1 = Pattern.compile("[a-zA-Z0-9]+_[a-zA-Z0-9]+");
		Pattern underscore2 = Pattern.compile("[a-z]+");
		//Pattern underscore2 = Pattern.compile("[A-Z0-9]+_[A-Z0-9]+");
		char[] c = {'\u3000'};
		String wspace = new String(c);
		int offset = 0;
		boolean isLoop = true;
		String tmp = "";
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Validator.is_out_console          = store.getBoolean(Initializer.IS_OUT_CONSOLE);
		boolean is_detect_mbspace         = store.getBoolean(Initializer.IS_DETECT_MBSPACE);
		boolean is_detect_tab             = store.getBoolean(Initializer.IS_DETECT_TAB);;
		boolean is_detect_varname_camel   = store.getBoolean(Initializer.IS_DETECT_VARNAME_CAMEL);
		boolean is_detect_varname_uscore  = store.getBoolean(Initializer.IS_DETECT_VARNAME_USCORE);
		boolean is_detect_crlf            = store.getBoolean(Initializer.IS_DETECT_CRLF);
		boolean is_detect_start_brackets  = store.getBoolean(Initializer.IS_DETECT_START_BRACKETS);
		boolean is_detect_after_comma     = store.getBoolean(Initializer.IS_DETECT_AFTER_COMMA);
		boolean is_detect_function_return = store.getBoolean(Initializer.IS_DETECT_FUNCTION_RETURN);

		//��s�P�ʂŌ��؂���B
		for(int i = 0; i < doc.getNumberOfLines(); i++){
			
			//�J�����g�s�̈ʒu�����擾����B
			IRegion info;
			try {
				info = doc.getLineInformation(i);
			} catch (BadLocationException e) {
				e.printStackTrace();
				Console.log("Validator.execMarking Error: " + e.getMessage());
				continue;
			}

			//�J�����g�s�̕�������擾����B
			String linedoc;
			try {
				linedoc = doc.get(info.getOffset(), info.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
				Console.log("Validator.execMarking Error: " + e.getMessage());
				continue;
			}
			
			//�S�p�X�y�[�X�����o����B
			if (is_detect_mbspace){
				tmpIdx = linedoc.indexOf(wspace);
				if (tmpIdx != -1){
					this.buildMarker(file, Validator.ERRMSG_DETECT_MB_SPACE, (i + 1),
						(info.getOffset() + tmpIdx), (info.getOffset() + tmpIdx + 1));
				}
			}
			
			//�^�u�����o����B
			if (is_detect_tab){
				tmpIdx = linedoc.indexOf("\t");
				if (tmpIdx != -1){
					this.buildMarker(file, Validator.ERRMSG_DETECT_TAB, (i + 1),
						(info.getOffset() + tmpIdx), (info.getOffset() + tmpIdx + 1));
				}
			}
			
			//�L�������L�@�����o����B
			//�啶��������������ȏ�{"_"�{�啶��������������ȏ�
			if (is_detect_varname_camel){
				mtc = camel.matcher(linedoc);
				if (mtc.find()){
					this.buildMarker(file, Validator.ERRMSG_DETECT_VARNAME_CAMEL, (i + 1),
						(info.getOffset() + mtc.start()), (info.getOffset() + mtc.end() + 1));
				}
			}
			
			//�A���_�[�X�R�A�L�@�����o����B
			//���������������{�啶��������ȏ�{������������ȏ�
			if (is_detect_varname_uscore){
				mtc = underscore1.matcher(linedoc);
				if (mtc.find()){
					mtctmp = underscore2.matcher(mtc.group()); //�S�đ啶���̋L�@�͒萔�\���ƊŘ􂵁A��������B
					if (mtctmp.find()){
						this.buildMarker(file, Validator.ERRMSG_DETECT_VARNAME_USCORE, (i + 1),
							(info.getOffset() + mtc.start()), (info.getOffset() + mtc.end() + 1));
					}
				}
			}

			//���s�R�[�h���o����B
			if (is_detect_crlf){
				String delimiter;
				try {
					delimiter = doc.getLineDelimiter(i);
				} catch (BadLocationException e) {
					e.printStackTrace();
					//Console.log("Validator.execMarking Error: " + e.getMessage());
					continue;
				}
				if (delimiter == null) continue;
				
				if (delimiter.equals("\r\n")){
					this.buildMarker(file, Validator.ERRMSG_DETECT_CRLF, (i + 1), -1, -1);
				}
			}
		}
		
		//�J�n���ʒ���ɉ��s���Ă��Ȃ��ӏ������o
		if (is_detect_start_brackets){
			offset = 0;
			isLoop = true;
			//mtc = Pattern.compile("(\\[|\\{).+").matcher(doc.get());
			mtc = Pattern.compile("\\{.+").matcher(doc.get());
			tmp = "";
			
			do{
				isLoop = mtc.find(offset);
				
				if (isLoop){
					offset = mtc.end();
	
					try {
						tmp = doc.get(mtc.start(), 2);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					
					//���e�v�f���������̂͏��O����B
					//if ((tmp.equals("{}")) || (tmp.equals("[]"))){
					if (tmp.equals("{}")){
						continue;
					}
					
					try {
						this.buildMarker(file, Validator.ERRMSG_STRING_AFTER_START_BRACKETS,
							doc.getLineOfOffset(mtc.start()) + 1, mtc.start(), mtc.end());
					} catch (BadLocationException e) {}
				}
			} while(isLoop);
		}
		
		//�z��E�I�u�W�F�N�g�̖����v�f�J���}�����o 
		if (is_detect_after_comma){
			offset = 0;
			isLoop = true;
			mtc = Pattern.compile(",\\s*\\r*\\n*[\\t\\s]*[\\}\\]\\)]").matcher(doc.get());
			tmp = "";
			
			do{
				isLoop = mtc.find(offset);
				if (isLoop){
					offset = mtc.end();
					try {
						this.buildMarker(file, Validator.ERRMSG_LAST_ELEMENTS_AFTER_COMMA,
							doc.getLineOfOffset(mtc.start()) + 1, mtc.start(), mtc.end());
					} catch (BadLocationException e) {}
				}
			} while(isLoop);
		}
		
		camel = null;
		underscore1 = null;
		underscore2 = null;
		mtc = null;
		mtctmp = null;
		Validator.is_working = false;
	}

	/**
	 * �n���l�t�@�C���Ƀ}�[�J�[��ǉ�����B
	 *
	 * @param file
	 * @param message
	 * @param linenumber
	 * @param offset_start
	 * @param offset_end
	 * @return IMarker
	 */
	private IMarker buildMarker(IFile file,
								String message,
								int linenumber,
								int offset_start,
								int offset_end){

		//System.out.println("Validator.buildMarker");

		//�}�[�J�[�I�u�W�F�N�g�𐶐�����B
		IMarker mkr;
		try {
			mkr = file.createMarker(IMarker.PROBLEM);
		} catch (Exception e) {
			e.printStackTrace();
			Console.log("Validator.buildMarker Error: " + e.getMessage());
			return null;
		}

		//�}�[�J�[�ɏڍ׃v���p�e�B���Z�b�g����B
		try{
			mkr.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			mkr.setAttribute(IMarker.MESSAGE, message);
			mkr.setAttribute(IMarker.LINE_NUMBER, linenumber);
			if ((offset_start != -1) && (offset_end != -1)){
				mkr.setAttribute(IMarker.CHAR_START, offset_start);
				mkr.setAttribute(IMarker.CHAR_END, offset_end);
			}

			mkr.setAttribute(IMarker.LOCATION, "Line: " + linenumber + " / " + message);
			mkr.setAttribute(IMarker.SOURCE_ID, "UM_VALIDATOR");

			if (Validator.is_out_console){
				Console.log("Line: " + linenumber + " / " + message);
			}

		} catch (Exception e) {
			try {
				mkr.delete();
			} catch (CoreException e1) {}
			e.printStackTrace();
			Console.log("Validator.buildMarker Error: " + e.getMessage());
			return null;
		}

		//�g�p���Ȃ��Ǝv���邪�A�ꉞ�}�[�J�[�I�u�W�F�N�g��߂��B
		return mkr;
	}

	public void dispose(){
		Validator.state.dispose();
		Validator.state = null;
	}
}
