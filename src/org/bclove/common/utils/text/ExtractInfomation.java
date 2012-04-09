package org.bclove.common.utils.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;

public class ExtractInfomation {

	private static ExtractInfomation instance = new ExtractInfomation();

//	private static String URLchar[] = {"$","-" , "_" , "." , "+","!" , "*" , "'" , "(" , ")" , ",","{" , "}" , "," , "\\", "^","~" , "[" , "]","`", "<" , ">" , "#" , "%" ,";" , "/" , "?" , ":" , "@" , "&" , "="}; 
//	private static HashMap<String,Integer> URLCharMap = new HashMap<String,Integer>();
	public ExtractInfomation() {
//		for(int i =0 ;i<URLchar.length;i++){
//			URLCharMap.put(URLchar[i], i);
//		}
	}

	final public static ExtractInfomation getInstance() {
		return instance;
	}

	private static final NodeFilter filter = new NodeClassFilter(TextNode.class); // 过滤器，只要text的node，别的不要

	/**
	 * 提取文本
	 * 
	 * @param content
	 * @return
	 */
	public String extractText(String content) {
		/**
		 * 提取所有的text(非tag)
		 */
		if (content == null)
			return null;
		Parser parser = Parser.createParser(content, "gb2312"); // 初始化解析器

		NodeList nodes;
		try {
			nodes = parser.extractAllNodesThatMatch(filter); // 提取所有的TextNode
		} catch (Exception e) {
			return null;
		}
		StringBuffer text = new StringBuffer();
		TextNode textNode = null;
		Node[] nodeArray = nodes.toNodeArray();
		for (int i = 0; i < nodeArray.length; i++) { // 遍历所有TextNode
			textNode = (TextNode) nodeArray[i]; // 合并所有的TextNode
			text.append(textNode.getText());
		}
		return text.toString();
	}

	/**
	 * 提取文本中所有的汉字
	 */

	public String extractChineseWords(String text) {

		if (text == null)
			return null;
		StringBuffer Chinesebuff = new StringBuffer();
		char[] array = text.toCharArray();
		for (int i = 0; i < array.length; i++) { // 遍历字符串
			char ch = array[i];
			// 在这个范围内的是汉字（可能会有些漏掉，但是都是些正常人不认识的生僻字，不会出现在违禁词中）
			if (ch >= 19968 && ch <= 40869)
				Chinesebuff.append(ch); // 是汉字就加入
		}
		return Chinesebuff.toString();
	}

	/**
	 * 提取文本中所有的汉字、数字和英文
	 */

	public String extractEntity(String text) {
		//System.out.println("+++in : "+ text);
		if (text == null)
			return null;
		StringBuffer entitybuff = new StringBuffer();
		char[] array = text.toCharArray();
		for (int i = 0; i < array.length; i++) { // 遍历字符串
			// 在这个范围内的是汉字、数字和英文
//			if ((array[i] >= 19968 && array[i] <= 40869)
//					|| (array[i] >= '0' && array[i] <= '9')
//					|| (array[i] >= 'a' && array[i] <= 'z')
//					|| (array[i] >= 'A' && array[i] <= 'Z')
//					|| (array[i]>= 229632 && array[i] <= 230399))
				entitybuff.append(array[i]); // 是汉字就加入
		}
		//System.out.println("+++out : "+entitybuff.toString());
		return entitybuff.toString();
	}
	/**
	 * 提取除了空白符之外的
	 * @param text
	 * @return
	 */
	public String extractAllExceptBlank(String text) {

		if (text == null)
			return null;
		StringBuffer Chinesebuff = new StringBuffer();
		char[] array = text.toCharArray();
		for (int i = 0; i < array.length; i++) { // 遍历字符串
			char ch = array[i];
			// 在这个范围内的是汉字（可能会有些漏掉，但是都是些正常人不认识的生僻字，不会出现在违禁词中）
			if (ch!=' '&& ch!='\t'&& ch!='\r'&& ch!='\n')
				Chinesebuff.append(ch); // 是汉字就加入
		}
		return Chinesebuff.toString();
	}	
	/**
	 * 从一个页面中提取title
	 * @param page
	 * @return
	 */
	public String extractTitle(String page){
		if(page == null){
			return null;
		}
		String lowerCasePage = page.toLowerCase();
		String left = "<title>";
		String right = "</title>";
		int pleft = lowerCasePage.indexOf(left);
		int pright = lowerCasePage.indexOf(right,pleft);
		if( pleft == -1 || pright == -1){
			return null;
		}
		String title = page.substring(pleft + 7, pright);
		return title;	
	}
	/**
	 * 提取文本中所有的非汉字，并把汉字部分用空格代替
	 */

	public String extractAllExceptChinese(String text) {

		if (text == null)
			return null;
		StringBuffer entitybuff = new StringBuffer();
		char[] array = text.toCharArray();
		for (int i = 0; i < array.length; i++) { // 遍历字符串
			if (!(array[i] >= 19968 && array[i] <= 40869)){
				entitybuff.append(array[i]); 
			}else{
				entitybuff.append(" ");
			}
		}
		return entitybuff.toString();
	}
	/**
	 * 提取文本中以分隔符隔开的url,分隔符包括汉字和
	
	public ArrayList<String> extractURLContent(String text) {

		if (text == null)
			return null;
		ArrayList<String> urlContentList = new ArrayList<String>();
		
		StringBuffer buff = new StringBuffer();
		char[] array = text.toCharArray();
		for (int i = 0; i < array.length; i++) { // 遍历字符串
			if ((array[i] >= 19968 && array[i] <= 40869))
				buff.append(array[i]); // 是汉字就加入
		}
		return buff.toString();
	} 
	*/
	/**
	 * 从右到左从上到下的竖排文字解析
	 * 
	 * @param strContent
	 * @return
	 */

	public String uprightToLevel(String strContent) {
		char mostCountChar = getMostCountChar(strContent);
		if (mostCountChar == 0)
			return null;
		
		String[] strArray = strContent.split("br");
		int lineCount = strArray.length;
		int longestRowCount = 0;
		List<List<String>> list = new ArrayList<List<String>>();
		for (int i = 0; i < lineCount; i++) {
			List<String> strLineList = split("" + mostCountChar, strArray[i]);
			if (strLineList.size() > longestRowCount)
				longestRowCount = strLineList.size();
			list.add(strLineList);
		}
		StringBuffer sb = new StringBuffer();
		int listSize = list.size();
		for (int i = 0; i < longestRowCount; i++) {
			for (int j = 0; j < listSize; j++) {
				List<String> strLineList = list.get(j);
				if (strLineList.size() <= i)
					continue;
				int lenth = strLineList.size() - 1;
				String str = strLineList.get(lenth - i);
				if (str != null && !str.equals(mostCountChar)) {
					sb.append(str);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * 根据div分开文本
	 * 
	 * @param div
	 * @param content
	 * @return
	 */
	private List<String> split(String div, String content) {
		int pos1 = 0;
		int pos2 = 0;
		int divLength = div.length();
		List<String> list = new ArrayList<String>();
		while ((pos2 = content.indexOf(div, pos1)) != -1) {
			list.add(content.substring(pos1, pos2));
			pos1 = pos2 + divLength;
			if (++pos2 >= content.length())
				break;
		}
		list.add(content.substring(pos1));
		return list;
	}

	/**
	 * 获取文本中出现次数最多的字符 出现次数大于文本总数的1／6
	 * 
	 * @param strContent
	 * @return
	 */
	private char getMostCountChar(String strContent) {
		HashMap<Character, Integer> hashmap = new HashMap<Character, Integer>();

		Integer count = null;
		char[] array = strContent.toCharArray();

		for (int i = 0; i < array.length; i++) {
			count = 1;
			if (hashmap.containsKey(array[i])) {
				count = (Integer) hashmap.get(array[i]) + 1;
			}
			hashmap.put(array[i], count);
		}
		char mostCountChar = 0;
		Integer mostCount = 0;

		char c;
		for (int i = 0; i < array.length; i++) {
			c = array[i];
			count = (Integer) hashmap.get(c);
			if (count > mostCount) {
				mostCount = count;
				mostCountChar = c;
			}
		}
		if (mostCount * 6 > strContent.length())
			return mostCountChar;
		return 0;
	}
}
