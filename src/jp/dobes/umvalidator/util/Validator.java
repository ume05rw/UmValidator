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
	private static final String ERRMSG_DETECT_MB_SPACE = "全角スペースを検出しました";
	private static final String ERRMSG_DETECT_TAB = "タブを検出しました";
	private static final String ERRMSG_DETECT_VARNAME_CAMEL = "キャメル記法を検出しました";
	private static final String ERRMSG_DETECT_VARNAME_USCORE = "アンダースコア記法を検出しました";
	private static final String ERRMSG_DETECT_CRLF = "改行コードCRLFを検出しました";
	private static final String ERRMSG_STRING_AFTER_START_BRACKETS = "開始括弧( { , [ )の後、改行せずに記述されている行を検出しました";
	private static final String ERRMSG_LAST_ELEMENTS_AFTER_COMMA = "配列・オブジェクトの末尾要素の末端カンマ( , )を検出しました";
	private static final String ERRMSG_FUNCTION_CLOSE_WITHOUT_RETURN = "returnが存在しないfunction定義を検出しました。";

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
	 * 本プラグインが追加したマーカーを削除する。
	 */
	public void refreshMarkers(boolean isRefreshState){
		//System.out.println("Validator.refreshMarkers");
		Console.clear();

		//カレントエディタが取得出来たとき、本プラグインでセットしたマーカーを削除する。
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
	 * バリデータを実行する。
	 */
	public void execMarking(){
		//System.out.println("Validator.execMarking");
		Validator.is_working = true; //稼働フラグをONにする。マーカーセット時にcommand.preExecuteが走ることがあるため。

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

		//一行単位で検証する。
		for(int i = 0; i < doc.getNumberOfLines(); i++){
			
			//カレント行の位置情報を取得する。
			IRegion info;
			try {
				info = doc.getLineInformation(i);
			} catch (BadLocationException e) {
				e.printStackTrace();
				Console.log("Validator.execMarking Error: " + e.getMessage());
				continue;
			}

			//カレント行の文字列を取得する。
			String linedoc;
			try {
				linedoc = doc.get(info.getOffset(), info.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
				Console.log("Validator.execMarking Error: " + e.getMessage());
				continue;
			}
			
			//全角スペースを検出する。
			if (is_detect_mbspace){
				tmpIdx = linedoc.indexOf(wspace);
				if (tmpIdx != -1){
					this.buildMarker(file, Validator.ERRMSG_DETECT_MB_SPACE, (i + 1),
						(info.getOffset() + tmpIdx), (info.getOffset() + tmpIdx + 1));
				}
			}
			
			//タブを検出する。
			if (is_detect_tab){
				tmpIdx = linedoc.indexOf("\t");
				if (tmpIdx != -1){
					this.buildMarker(file, Validator.ERRMSG_DETECT_TAB, (i + 1),
						(info.getOffset() + tmpIdx), (info.getOffset() + tmpIdx + 1));
				}
			}
			
			//キャメル記法を検出する。
			//大文字小文字複数一つ以上＋"_"＋大文字小文字複数一つ以上
			if (is_detect_varname_camel){
				mtc = camel.matcher(linedoc);
				if (mtc.find()){
					this.buildMarker(file, Validator.ERRMSG_DETECT_VARNAME_CAMEL, (i + 1),
						(info.getOffset() + mtc.start()), (info.getOffset() + mtc.end() + 1));
				}
			}
			
			//アンダースコア記法を検出する。
			//小文字複数無し可＋大文字複数一つ以上＋小文字複数一つ以上
			if (is_detect_varname_uscore){
				mtc = underscore1.matcher(linedoc);
				if (mtc.find()){
					mtctmp = underscore2.matcher(mtc.group()); //全て大文字の記法は定数表現と看做し、無視する。
					if (mtctmp.find()){
						this.buildMarker(file, Validator.ERRMSG_DETECT_VARNAME_USCORE, (i + 1),
							(info.getOffset() + mtc.start()), (info.getOffset() + mtc.end() + 1));
					}
				}
			}

			//改行コード検出する。
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
		
		//開始括弧直後に改行していない箇所を検出
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
					
					//内容要素が無いものは除外する。
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
		
		//配列・オブジェクトの末尾要素カンマを検出 
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
	 * 渡し値ファイルにマーカーを追加する。
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

		//マーカーオブジェクトを生成する。
		IMarker mkr;
		try {
			mkr = file.createMarker(IMarker.PROBLEM);
		} catch (Exception e) {
			e.printStackTrace();
			Console.log("Validator.buildMarker Error: " + e.getMessage());
			return null;
		}

		//マーカーに詳細プロパティをセットする。
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

		//使用しないと思われるが、一応マーカーオブジェクトを戻す。
		return mkr;
	}

	public void dispose(){
		Validator.state.dispose();
		Validator.state = null;
	}
}
