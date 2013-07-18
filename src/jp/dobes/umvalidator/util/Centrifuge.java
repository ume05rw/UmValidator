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
		private Centrifuge.AreaType type;

		public Area(int offset, int length, Centrifuge.AreaType type){
			this.offset = offset;
			this.length = length;
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
		public Centrifuge.AreaType getType(){
			return this.type;
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

	/**
	 * コンストラクタ
	 *
	 * @param code
	 */
	public Centrifuge(String code){
		this.baseCode = code;
		this.formattedCode = this.formatCode(code);
		this.areas = new ArrayList<Area>();

		this.areas.addAll(this.getLineCommentAreas());
		this.areas.addAll(this.getBlockCommentAreas());
		this.areas.addAll(this.getStringAreas());
		//this.areas.addAll(this.getBracketLargeAreas());
		//this.areas.addAll(this.getBracketCurlyAreas());
		//this.areas.addAll(this.getFunctionAreas());
	}

	/**
	 * 渡し値コードドキュメントから、解析に不要な文字列を除外する。
	 *
	 * @param code
	 * @return String
	 */
	private String formatCode(String code){
		//エスケープされた「\, ', ", {, }, [, ] 」マークを削除。
		return Pattern.compile("\\\\|\\'|\\\"|\\{|\\}|\\[|\\]").matcher(code).replaceAll("  ");
	}

	/* */
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
						areas.add(new Area(startIndex, (endIndex - startIndex), AreaType.COMMENT));
					}
					offset = endIndex;
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
		int startIndex = 0;
		int endIndex = 0;
		boolean isLoop = true;

		do{
			isLoop = false;

			startIndex = this.formattedCode.indexOf("\"", offset);
			if (startIndex != -1){

				endIndex = this.formattedCode.indexOf("\"", startIndex);
				if (endIndex != -1){

					if (this.isActiveCode(startIndex)){
						areas.add(new Area(startIndex, (endIndex - startIndex), AreaType.STRING));
					}
					offset = endIndex;
					isLoop = true;
				}
			}
		} while(isLoop);

		offset = 0;
		do{
			isLoop = false;

			startIndex = this.formattedCode.indexOf("'", offset);
			if (startIndex != -1){

				endIndex = this.formattedCode.indexOf("'", startIndex);
				if (endIndex != -1){

					if (this.isActiveCode(startIndex)){
						areas.add(new Area(startIndex, (endIndex - startIndex), AreaType.STRING));
					}
					offset = endIndex;
					isLoop = true;
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

		return areas;
	}

	/**
	 * コード文字列から中括弧({～})で囲まれた範囲を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getBracketCurlyAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		return areas;
	}

	/**
	 * コード文字列から関数定義部分を抽出する。
	 *
	 * @return ArrayList<Area>
	 */
	private ArrayList<Area> getFunctionAreas(){
		ArrayList<Area> areas = new ArrayList<Area>();

		return areas;
	}

	/**
	 * 渡し値コード位置が、コードとして有効な箇所か否かを判定する。
	 *
	 * @param offset
	 * @return boolean
	 */
	public boolean isActiveCode(int offset){
		for(int i = 0; i < this.areas.size(); i++){
			if (
				(this.areas.get(i).getType() == Centrifuge.AreaType.COMMENT)
				|| (this.areas.get(i).getType() == Centrifuge.AreaType.STRING)
				&& (
					(this.areas.get(i).getStart() <= offset)
					&& (offset < this.areas.get(i).getEnd())
				)
			){
				return false;
			}
		}
		return true;
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
