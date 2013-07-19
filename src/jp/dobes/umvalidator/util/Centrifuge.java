package jp.dobes.umvalidator.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;

/**
 * コードドキュメントの解析
 *
 * ※注)まだアクティブなクラスではない。
 *
 * @author ikaruga
 *
 */
public class Centrifuge {

	/**
	 * サブクラス：範囲定義
	 *
	 * @author ikaruga
	 *
	 */
	public class Area {
		private int offset;
		private int length;
		private int depth;
		private Centrifuge.AreaType type;

		public Area(int offset, int length, Centrifuge.AreaType type){
			this.offset = offset;
			this.length = length;
			this.depth = 0;
			this.type = type;
		}

		public int getOffset(){
			return this.offset;
		}
		public int getStart(){
			return this.offset;
		}
		public int getLength(){
			return this.length;
		}
		public int getEnd(){
			return (this.offset + this.length);
		}
		public int getDepth(){
			return this.depth;
		}
		public Centrifuge.AreaType getType(){
			return this.type;
		}
		public String getTypeString(){
			return this.type.name();
		}
		public boolean existOffset(int offset){
			if ((this.getStart() <= offset) && (offset <= this.getEnd())){
				return true;
			}
			return false;
		}
		protected void addDepth(){
			this.depth++;
		}
		public void dispose(){
			this.offset = -1;
			this.length = -1;
			this.type = null;
		}
	}

	//コード範囲の区分
	public enum AreaType {
		COMMENT,
		STRING,
		BRACKET_LARGE,
		BRACKET_CURLY,
		FUNCTION
	}

	//コードドキュメント文字列
	private String baseCode;
	private String formattedCode;

	//コード範囲配列
	private ArrayList<Area> areas;
	private ArrayList<Area> areasComment;
	private ArrayList<Area> areasString;
	private ArrayList<Area> areasNotCode;
	private ArrayList<Area> areasBracketLarge;
	private ArrayList<Area> areasBracketCurly;
	private ArrayList<Area> areasFunction;

	/**
	 * コンストラクタ
	 *
	 * @param code
	 */
	public Centrifuge(String code){
		this.baseCode = code;

		//コード解析用前処理を行う。
		this.formattedCode = this.formatCode(code);

		//範囲要素を全て保持する配列
		this.areas = new ArrayList<Area>();

		//コメント範囲要素を取得する。
		this.areasComment = this.getLineCommentAreas();
		this.areasComment.addAll(this.getBlockCommentAreas());
		this.areas.addAll(this.areasComment);

		//文字列範囲要素を取得する。
		this.areasString = this.getStringAreas();
		this.areas.addAll(this.areasString);

		//コメント・文字列範囲要素をコードとして扱わない範囲として保持する。
		this.areasNotCode = new ArrayList<Area>();
		this.areasNotCode.addAll(this.areasComment);
		this.areasNotCode.addAll(this.areasString);

		//大括弧範囲要素を取得する。
		this.areasBracketLarge = this.getBracketLargeAreas();
		this.areas.addAll(this.areasBracketLarge);

		//中括弧範囲要素を取得する。
		this.areasBracketCurly = this.getBracketCurlyAreas();
		this.areas.addAll(this.areasBracketCurly);

		//function範囲要素を取得する。
		this.areasFunction = this.getFunctionAreas();
		this.areas.addAll(this.areasFunction);

		//if範囲要素
		//for範囲要素

		//System.out.println("Centrifuge OriginCode:");
		//System.out.println(code);
		//System.out.println("Centrifuge ProcessedCode:");
		//System.out.println(this.formattedCode);
	}

	/**
	 * 渡し値コードドキュメントから、解析に不要な文字列を除外する。
	 *
	 * @param code
	 * @return String
	 */
	private String formatCode(String code){
		//エスケープされた記号「\\, \', \", \{, \}, \[, \] 」マークを削除。
		String str = Pattern.compile("\\\\\\'|\\\\\\\"|\\\\\\{|\\\\\\}|\\\\\\[|\\\\\\]").matcher(code).replaceAll("  ");
		str = Pattern.compile("\\\\\\\\").matcher(str).replaceAll("  ");

		return str;
	}

	/**
	 * コード文字列から行コメント部分を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getLineCommentAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		int offset = 0;
		boolean isLoop = true;
		Matcher mtc = Pattern.compile("//.*").matcher(this.formattedCode);

		do{
			isLoop = mtc.find(offset);
			if (isLoop){
				if (this.isActiveCode(mtc.start())){
					areas.add(new Area(mtc.start(), (mtc.end() - mtc.start()), AreaType.COMMENT));
				}
				offset = mtc.end();
			}
		} while(isLoop);

		return areas;
	}

	/**
	 * コード文字列からブロックコメント部分を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getBlockCommentAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		int offset = 0;
		int startIndex = 0;
		int endIndex = 0;
		boolean isLoop = true;

		do{
			isLoop = false;

			startIndex = this.formattedCode.indexOf("/*", offset);
			if (startIndex != -1){

				endIndex = this.formattedCode.indexOf("*/", startIndex);
				if (endIndex != -1){

					if (this.isActiveCode(startIndex)){
						areas.add(new Area(startIndex, (endIndex - startIndex) + 2, AreaType.COMMENT));
					}
					offset = endIndex + 2;
					isLoop = true;
				}
			}
		} while(isLoop);

		return areas;
	}

	/**
	 * コード文字列から文字列部分を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getStringAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		int offset = 0;
		int endIndex = 0;
		boolean isLoop = true;
		Matcher mtc = Pattern.compile("\\\"|\\'").matcher(this.formattedCode);

		do{
			isLoop = mtc.find(offset);
			if (isLoop){
				endIndex = this.formattedCode.indexOf(mtc.group(), mtc.end());
				if (endIndex != -1){

					if (this.isActiveCode(mtc.start())){
						areas.add(new Area(mtc.start(), (endIndex - mtc.start() + 1), AreaType.STRING));
					}
					offset = endIndex + 1;
				} else {
					offset = mtc.end();
				}
			}
		} while(isLoop);

		return areas;
	}

	/**
	 * コード文字列から大括弧([～])で囲まれた範囲を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getBracketLargeAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		ArrayList<Integer> startPoints = this.getStringIndexArray("[");
		ArrayList<Integer> endPoints =  this.getStringIndexArray("]");
		int index = -1;

		//終点の数分ループする。
		for(int i = 0; i < endPoints.size(); i++){
			index = -1;
			for(int j = 0; j < startPoints.size(); j++){
				if (startPoints.get(j) < endPoints.get(i)){
					index = j;
				} else {
					break;
				}
			}
			if (index != -1){
				//範囲オブジェクトを追加し、合致開始点をArrayListから削除する。
				areas.add(new Area(startPoints.get(index), (endPoints.get(i) - startPoints.get(index)) + 1, AreaType.BRACKET_LARGE));
				startPoints.remove(index);
			}
		}

		return areas;
	}

	/**
	 * コード文字列から中括弧({～})で囲まれた範囲を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getBracketCurlyAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		ArrayList<Integer> startPoints = this.getStringIndexArray("{");
		ArrayList<Integer> endPoints =  this.getStringIndexArray("}");
		int index = -1;

		//終点の数分ループする。
		for(int i = 0; i < endPoints.size(); i++){
			index = -1;
			for(int j = 0; j < startPoints.size(); j++){
				if (startPoints.get(j) < endPoints.get(i)){
					index = j;
				} else {
					break;
				}
			}
			if (index != -1){
				//範囲オブジェクトを追加し、合致開始点をArrayListから削除する。
				areas.add(new Area(startPoints.get(index), (endPoints.get(i) - startPoints.get(index)) + 1, AreaType.BRACKET_CURLY));
				startPoints.remove(index);
			}
		}

		//中括弧のネスト深度を計算する。
		for(Area ar1 : areas){
			for(Area ar2 : areas){
				//同一オブジェクトのときは比較しない。
				if (ar1.equals(ar2)) continue;

				if (
					(ar2.getStart() < ar1.getStart())
					&& (ar1.getEnd() < ar2.getEnd())
				){
					ar1.addDepth();
				}
			}
		}

		return areas;
	}

	/**
	 * コード文字列から関数定義部分を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getFunctionAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		int offset = 0;
		int start = 0;

		ArrayList<Integer> functionPoints = this.getStringIndexArray("function");
		ArrayList<Integer> brucketPoints = this.getStringIndexArray("{");

		//"function"文字列の個数分ループする。
		for(int i : functionPoints){
			//コードとして有効でないものはスキップする。
			if (!this.isActiveCode(i)) continue;

			//"function"文字列から最も近い位置の"{"文字列の位置を取得する。
			offset = i + 8;
			start = Integer.MAX_VALUE;
			for(int j : brucketPoints){
				if (j < offset) continue;
				if (start < j) continue;
				if (!this.isActiveCode(j)) continue;
				start = j;
			}

			//取得済みの中括弧範囲配列から、範囲が合致するものをピックアップする。
			for(Area area : this.areasBracketCurly){
				if (start == area.getStart()){
					areas.add(new Area(i, (area.getEnd() - i), AreaType.FUNCTION));
					break;
				}
			}
		}

		return areas;
	}

	/**
	 * コードの中で、渡し値文字列が検出されたオフセットインデックス値を全取得し、配列で返す。
	 *
	 * @param target
	 * @return
	 */
	private ArrayList<Integer> getStringIndexArray(String target){
		ArrayList<Integer> result = new ArrayList<Integer>();

		int index = 0;
		for(int i = 0; i < this.formattedCode.length(); i++){
			index = this.formattedCode.indexOf(target, i);
			if (index != -1){
				if (this.isActiveCode(index)){
					//対象文字列が見つかった場合、その位置情報を保存する。
					result.add(index);
				}
				i = index + 1;
			} else {
				//対象文字列が見つからない場合、ループを終了する。
				break;
			}
		}

		return result;
	}

	/**
	 * 渡し値コード位置が、コードとして有効な箇所か否かを判定する。
	 *
	 * @param offset
	 * @return boolean
	 */
	public boolean isActiveCode(int offset){
		//有効コード範囲オブジェクトの有無によって分岐
		if (this.areasNotCode == null) {
			//有効コード範囲が未取得のとき
			for(int i = 0; i < this.areas.size(); i++){
				if (
					(
						(this.areas.get(i).getType() == Centrifuge.AreaType.COMMENT)
						|| (this.areas.get(i).getType() == Centrifuge.AreaType.STRING)
					)
					&& (
						(this.areas.get(i).getStart() <= offset)
						&& (offset <= this.areas.get(i).getEnd())
					)
				){
					return false;
				}
			}
		} else {
			//有効コード範囲を取得済みのとき
			for(int i = 0; i < this.areasNotCode.size(); i++){
				if (
					(this.areasNotCode.get(i).getStart() <= offset)
					&& (offset <= this.areasNotCode.get(i).getEnd())
				){
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 範囲オブジェクト配列を返す。
	 *
	 * @return ArrayList<Area>
	 */
	public ArrayList<Area> getAreas(){
		return this.areas;
	}
	public ArrayList<Area> getAreas(Centrifuge.AreaType type){
		switch(type.name()){
		case "BRACKET_CURLY":
			return this.areasBracketCurly;
		case "BRACKET_LARGE":
			return this.areasBracketLarge;
		case "COMMENT":
			return this.areasComment;
		case "FUNCTION":
			return this.areasFunction;
		case "STRING":
			return this.areasString;
		default:
			return this.areas;
		}
	}

	/**
	 * 渡し値よりもネスト深度が深い中括弧範囲を返す。
	 *
	 * @param depth
	 * @return Area
	 */
	public ArrayList<Area> getDepthOverBrackets(int depth){
		ArrayList<Area> result = new ArrayList<Area>();

		for(Area area : this.areasBracketCurly){
			if (area.getDepth() >= depth){
				result.add(area);
			}
		}

		return result;
	}

	/**
	 * インスタンスを破棄する。
	 */
	public void dispose(){
		this.baseCode = null;
		this.formattedCode = null;

		for(int i = 0; i < this.areas.size(); i++){
			this.areas.get(i).dispose();
		}
		this.areas.clear();
		this.areas = null;
	}
}
