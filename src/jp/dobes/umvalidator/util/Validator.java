package jp.dobes.umvalidator.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.dobes.umvalidator.Activator;
import jp.dobes.umvalidator.preference.Initializer;
import jp.dobes.umvalidator.util.Centrifuge.Area;
import jp.dobes.umvalidator.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class Validator {

	private static WorkbenchState state;
	private static boolean is_out_console;
	private static boolean is_working;
	private static String sourceId = "UM_VALIDATOR";

	/**
	 * コンストラクタ
	 */
	public Validator(){
		//System.out.println("Validator.constructor");

		Validator.is_working = false;
		Validator.state = new WorkbenchState();
	}

	/**
	 * バリデータが稼働中か否かを戻す。
	 * ※保存アクションフック時、無限ループ状態が発生してしまう現象への対策
	 *
	 * @return boolean
	 */
	public boolean isWorking(){
		//System.out.println("Validator.isWorking");
		return Validator.is_working;
	}


	/**
	 * 本プラグインが追加したマーカーを削除する。
	 *
	 * @param isRefreshState カレントワークベンチ情報を更新するか否か
	 */
	public void refreshMarkers(boolean isRefreshState){
		//System.out.println("Validator.refreshMarkers");

		//コンソール上のバリデート情報を削除する。
		Console.clear();

		//カレントエディタを取得する。
		if (isRefreshState) Validator.state.refresh();
		IFile file = Validator.state.getFile();
		if (file == null) return;

		//カレントエディタ上のマーカーを取得する。
		IMarker[] mkrs;
		String source_id;
		try{
			mkrs = file.findMarkers(null, true, IResource.DEPTH_INFINITE);
		} catch(Exception ex2){
			return;
		}

		//取得したマーカーのうち、本プラグインがセットしたマーカーのみを削除する。
		try {
			for(IMarker obj : mkrs){
				//マーカーのソースID属性を取得
				try{
					source_id = obj.getAttribute(IMarker.SOURCE_ID).toString();
				} catch(Exception ex){
					continue;
				}

				//ソースIDが"UM_VALIDATOR"のとき、本プラグインがセットしたマーカーと見做す。
				if (source_id.equals(Validator.sourceId)){
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
		//System.out.println("Validator.execMarking start");
		Validator.is_working = true; //稼働フラグをONにする。マーカーセット時にcommand.preExecuteが走ることがあるため。

		//カレントワークスペース情報を更新する。
		Validator.state.refresh();

		//カレントエディタ、ドキュメントが取得できないとき、何もしない。
		IFile file = Validator.state.getFile();
		IDocument doc = Validator.state.getDocument();
		if ((file == null) || (doc == null)) return;

		//既存マーカーを削除する。
		this.refreshMarkers(false);

		//コード解析解析を行う。
		Centrifuge ctr = new Centrifuge(doc.get());
		for(Area area : ctr.getAreas()){
			try {
				System.out.println(
					"Type: " + area.getTypeString()
					+ "  / Offset: " + area.getOffset()
					+ "  / Length: " + area.getLength()
					+ "  / Depath: " + area.getDepth()
					+ "  / Line: " + doc.getLineOfOffset(area.getOffset())
					+ "  / Value: 「" + doc.get().substring(area.getStart(), area.getEnd()) + "」"
				);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//バリデータ処理の変数宣言
		int tmpIdx;
		Matcher mtc;
		Matcher mtctmp;
		Pattern camel = Pattern.compile("[a-z0-9]*[A-Z]+[a-zA-Z0-9]+");
		Pattern underscore1 = Pattern.compile("[a-zA-Z0-9]+_[a-zA-Z0-9]+");
		Pattern underscore2 = Pattern.compile("[a-z]+");
		String wspace = new String(new char[]{'\u3000'}); //全角スペース
		String delimiter;
		int offset = 0;
		boolean isLoop = true;
		String tmp = "";
		ArrayList<Centrifuge.Area> areas;

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Validator.is_out_console           = store.getBoolean(Initializer.IS_OUT_CONSOLE);
		boolean is_detect_mbspace         = store.getBoolean(Initializer.IS_DETECT_MBSPACE);
		boolean is_detect_tab             = store.getBoolean(Initializer.IS_DETECT_TAB);;
		boolean is_detect_varname_camel   = store.getBoolean(Initializer.IS_DETECT_VARNAME_CAMEL);
		boolean is_detect_varname_uscore  = store.getBoolean(Initializer.IS_DETECT_VARNAME_USCORE);
		boolean is_detect_crlf            = store.getBoolean(Initializer.IS_DETECT_CRLF);
		boolean is_detect_start_brackets  = store.getBoolean(Initializer.IS_DETECT_START_BRACKETS);
		boolean is_detect_after_comma     = store.getBoolean(Initializer.IS_DETECT_AFTER_COMMA);
		boolean is_detect_function_return = store.getBoolean(Initializer.IS_DETECT_FUNCTION_RETURN);
		boolean is_detent_nest_depth      = store.getBoolean(Initializer.IS_DETECT_NEST_DEPTH);
		boolean is_detect_function_lines  = store.getBoolean(Initializer.IS_DETECT_FUNCTION_LINES);

		int limit_nest_depth     = store.getInt(Initializer.LIMIT_NEST_DEPTH);
		int limit_function_lines = store.getInt(Initializer.LIMIT_FUNCTION_LINES);


		//ドキュメントを一行ずつ取得して検証する。
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
					this.buildMarker(file, Messages.ERRMSG_DETECT_MB_SPACE, (i + 1),
						(info.getOffset() + tmpIdx), (info.getOffset() + tmpIdx + 1));
				}
			}

			//タブを検出する。
			if (is_detect_tab){
				tmpIdx = linedoc.indexOf("\t");
				if (tmpIdx != -1){
					this.buildMarker(file, Messages.ERRMSG_DETECT_TAB, (i + 1),
						(info.getOffset() + tmpIdx), (info.getOffset() + tmpIdx + 1));
				}
			}

			//キャメル記法を検出する。
			//大文字小文字複数一つ以上＋"_"＋大文字小文字複数一つ以上
			if (is_detect_varname_camel){
				mtc = camel.matcher(linedoc);
				if (mtc.find() && ctr.isActiveCode(info.getOffset() + mtc.start())){
					this.buildMarker(file, Messages.ERRMSG_DETECT_VARNAME_CAMEL, (i + 1),
						(info.getOffset() + mtc.start()), (info.getOffset() + mtc.end() + 1));
				}
			}

			//アンダースコア記法を検出する。
			//小文字複数無し可＋大文字複数一つ以上＋小文字複数一つ以上
			if (is_detect_varname_uscore){
				mtc = underscore1.matcher(linedoc);
				if (mtc.find() && ctr.isActiveCode(info.getOffset() + mtc.start())){
					mtctmp = underscore2.matcher(mtc.group()); //全て大文字の記法は定数表現と看做し、無視する。
					if (mtctmp.find()){
						this.buildMarker(file, Messages.ERRMSG_DETECT_VARNAME_USCORE, (i + 1),
							(info.getOffset() + mtc.start()), (info.getOffset() + mtc.end() + 1));
					}
				}
			}

			//改行コード検出する。
			if (is_detect_crlf){
				delimiter = null;
				try {
					delimiter = doc.getLineDelimiter(i);
				} catch (BadLocationException e) {
					e.printStackTrace();
					//カレントエディタが設定ファイルなどのとき常に例外が発生するため、出力はしない。
					//Console.log("Validator.execMarking Error: " + e.getMessage());
				}
				if (delimiter != null) {
					if (delimiter.equals("\r\n")){
						this.buildMarker(file, Messages.ERRMSG_DETECT_CRLF, (i + 1), -1, -1);
					}
				}
			}
		}

		//複数行対象につき、ドキュメント全体を対象として再度ループする。
		//開始括弧直後に改行していない箇所を検出
		if (is_detect_start_brackets){
			offset = 0;
			isLoop = true;
			mtc = Pattern.compile("\\{[^\\}\\r\\n].+").matcher(doc.get());
			tmp = "";

			do{
				isLoop = mtc.find(offset);
				if (isLoop){
					offset = mtc.end();
					if (!ctr.isActiveCode(mtc.start())) continue;

					//	function(){ //コメント		<-こういうのを検出する。
					// 	}
					tmp = Pattern.compile("[\\s\\t]").matcher(mtc.group()).replaceAll("");
					if ((tmp.length() >= 3) && (tmp.substring(0, 3).equals("{//"))) continue;

					try {
						this.buildMarker(file, Messages.ERRMSG_STRING_AFTER_START_BRACKETS,
							doc.getLineOfOffset(mtc.start()) + 1, mtc.start(), mtc.end());
					} catch (BadLocationException e) {}
				}
			} while(isLoop);
		}

		//複数行対象につき、ドキュメント全体を対象として再度ループする。
		//配列・オブジェクトの末尾要素カンマを検出
		//TODO: 精度を上げたい。コメントを挟んだ場合に検出できない。
		// {
		//		{},
		//		{}, //コメント		<-こういうのは検出できない。
		// }
		if (is_detect_after_comma){
			offset = 0;
			isLoop = true;
			mtc = Pattern.compile(",\\s*\\r*\\n*[\\t\\s]*[\\}\\]\\)]").matcher(doc.get());
			tmp = "";

			do{
				isLoop = mtc.find(offset);
				if (isLoop){
					offset = mtc.end();
					if (!ctr.isActiveCode(mtc.start())) continue;

					try {
						this.buildMarker(file, Messages.ERRMSG_LAST_ELEMENTS_AFTER_COMMA,
							doc.getLineOfOffset(mtc.start()) + 1, mtc.start(), mtc.end());
					} catch (BadLocationException e) {}
				}
			} while(isLoop);
		}

		//ネスト深度を検出
		if (is_detent_nest_depth){
			areas = ctr.getDepthOverStatements(limit_nest_depth);
			for(Centrifuge.Area area : areas){
				try {
					this.buildMarker(file, Messages.ERRMSG_LIMIT_NEST_DEPTH,
						doc.getLineOfOffset(area.getStart()) + 1, area.getStart(), area.getEnd());
				} catch (BadLocationException e) {}
			}
		}

		//関数の行数、関数末尾return を検出
		if (is_detect_function_return || is_detect_function_lines){
			areas = ctr.getAreas(Centrifuge.AreaType.FUNCTION);
			int tmpidx;
			for(Centrifuge.Area area : areas){

				//関数行数検出
				if (is_detect_function_lines){
					try {
						if (
							(doc.getLineOfOffset(area.getEnd())
							- doc.getLineOfOffset(area.getStart())
							+ 1
						) >= limit_function_lines){
							this.buildMarker(file, Messages.ERRMSG_LIMIT_FUNCTION_LINES,
								doc.getLineOfOffset(area.getStart()) + 1, area.getStart(), area.getEnd());
						}
					} catch (BadLocationException e) {}
				}

				//関数末尾retrun
				if (is_detect_function_return){
					try{
						tmpidx = doc.getLineOfOffset(area.getEnd());
						//関数末尾行のひとつ前の行が、関数範囲外のとき、検出対象にしない。
						if (!area.existOffset(doc.getLineOffset(tmpidx - 1))) continue;
						if (doc.get(doc.getLineOffset(tmpidx - 1), doc.getLineLength(tmpidx - 1)).indexOf("return") == -1){
							this.buildMarker(file, Messages.ERRMSG_FUNCTION_CLOSE_WITHOUT_RETURN,
								doc.getLineOfOffset(area.getStart()) + 1, area.getStart(), area.getEnd());
						}
					} catch (BadLocationException e) {}
				}
			}
		}

		camel = null;
		underscore1 = null;
		underscore2 = null;
		mtc = null;
		mtctmp = null;
		Validator.is_working = false;
		areas = null;

		//System.out.println("Validator.execMarking end");
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
			//マーカー種別
			mkr.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

			//ロールオーバー時の表示メッセージ
			mkr.setAttribute(IMarker.MESSAGE, message);

			//対象行番号
			mkr.setAttribute(IMarker.LINE_NUMBER, linenumber);

			if ((offset_start != -1) && (offset_end != -1)){
				//対象範囲にアンダーラインを引く。
				mkr.setAttribute(IMarker.CHAR_START, offset_start);
				mkr.setAttribute(IMarker.CHAR_END, offset_end);
			}

			//これは何なんだ？サンプルにあったため、一応セットしとく。
			mkr.setAttribute(IMarker.LOCATION, "Line: " + linenumber + " / " + message);

			//ソースID。マーカーのセット元を区別するため必須。
			mkr.setAttribute(IMarker.SOURCE_ID, Validator.sourceId);

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

	/**
	 * インスタンスを破棄する。
	 */
	public void dispose(){
		Validator.state.dispose();
		Validator.state = null;
	}
}
