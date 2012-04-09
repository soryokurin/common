package org.bclove.common.utils.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;

/**
 * HTML工具类
 * 
 * @author Admin
 * 
 */
public class HtmlUtils {
	
	
	/**
	 * 判断一段字符串中是否含有样式相关代码<br>
	 * <link rel='stylesheet' 或 <style... 或 ...style=  等被视为含有样式相关代码
	 * @param string
	 * @return true 含有样式代码
	 */
	public static boolean isIncludeStyle(String string) {
		if(string==null) return false;
		String regEx = "[sS][tT][yY][lL][eE]";
		try {
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(string);
			boolean bool = m.find();
			if (m != null ) {
				if(bool){
					return true;	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String checkEditor(String text){
		if(isIncludeStyle(text)){
			//	\ &#
			//if(text.indexOf("*")>-1){
				//return "*";
			//}  * 号须在外替换成 ×
			
			text = text.toLowerCase().replaceAll("\\s", "");
			if(text.indexOf("url(")>-1){
				return "url(";
			}
			if(text.indexOf("!--")>-1){
				return "!--";
			}
			//需要正则
			Pattern p = Pattern.compile("[EeＥｅ][XxＸｘ][PpＰｐ][RrＲｒ][EeＥｅ][SsＳｓ][SsＳｓ][IiＩｉ][OoＯｏ][NnＮｎ]");
			Matcher m = p.matcher(text);
			boolean b = false;
			if (m != null )
				b = m.matches();			
			if(b){
				return "expression";
			}
			if(text.indexOf("@import")>-1){
				return "@import";
			}
			if(text.indexOf("@media")>-1){
				return "@media";
			}
//			if(text.indexOf("eval")>-1){
//				return "eval";
//			}
			
			//!--
			//expression
			//@import
			//@media
			//eval			
		}
		return null;
	}
	
	public static String filtrateEditorTag (String content) throws Exception{
		String contentLow = content.toLowerCase();	//全部转化为小写
		Parser parser = Parser.createParser(contentLow, "gb2312");	//初始化解析器
		NodeFilter filter = new NodeClassFilter(TagNode.class);		//过滤器，只要tag的node，别的不要
		NodeList nodes = parser.extractAllNodesThatMatch(filter);	//获取所有tag的node
		int hasCut = 0;	//已经切除了多少
		for(int i=0;i<nodes.size();i++){	//遍历所有TagNode
			TagNode tagNode = (TagNode)nodes.elementAt(i);	//获取一个tagNode
			boolean cutTag = false;
			if(isBadName(tagNode)){	//是否是恶意的tag
				//切掉恶意tag
				cutTag = true;
			}
			else{	//切除恶意属性
				Vector<String> vecBadAttribute = badAttribute(tagNode);
				if (vecBadAttribute != null) {
					Iterator<String> iteraror = vecBadAttribute.iterator();
					while (iteraror.hasNext()) {
						String badAttribute = iteraror.next();
						int pos = contentLow.indexOf(badAttribute, tagNode
								.getTagBegin());
						if (pos == -1 || pos > tagNode.getTagEnd()) {// 如果找不到了，切掉tag
							cutTag = true;
							break;
						} else { // 如果找到了，切掉属性
							content = content.substring(0, pos - hasCut)
									+ content.substring(pos
											+ badAttribute.length() - hasCut);
							hasCut += badAttribute.length();
						}
					}
				}
				else{
					cutTag = true;
				}
			}
			if(cutTag){
				content = content.substring(0,tagNode.getTagBegin()-hasCut)+content.substring(tagNode.getTagEnd()-hasCut);
				hasCut += tagNode.getTagEnd() - tagNode.getTagBegin();
			}
		}
		return content;//返回过滤后的字符串
	}
	private static boolean isBadName(TagNode tagNode){	//是否是恶意的标签名
		String name = tagNode.getTagName().toLowerCase();	//获取标签名
		if(name==null)
			return false;
		for(int i=0;i<arrBadName.length;i++){	//遍历所有可能的恶意标签名
			if(name.indexOf(arrBadName[i])!=-1)		//如果是则返回true
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param tagNode tag节点
	 * @return 返回恶意属性的set，如果返回null，说明里面至少有两个一样的恶意属性，明显搞我们，直接干掉tag
	 */
	private static Vector<String> badAttribute(TagNode tagNode){	//是否含有恶意属性
		Vector vecAttribute = tagNode.getAttributesEx();	//获取该标签的所有属性
		Vector<String> vecBadAttribute = new Vector<String>();
		Set<String> setAttributeHead = new HashSet<String>();
		for (int i = 0; i < vecAttribute.size(); i++) { // 遍历所有属性
			String strAttribute = vecAttribute.elementAt(i).toString();
			if (strAttribute == null) // 不知道会不会出现空值，但是加一下，安全些
				continue;
			String strAttributeHead = null;
			if(strAttribute.length()<=3)
				continue;
			else
				strAttributeHead = strAttribute.substring(0,3);
			if(setAttributeHead.contains(strAttributeHead)){
				return null;
			}
			setAttributeHead.add(strAttributeHead);
			if (strAttribute.startsWith("on")) { // 如果是以on开头的属性，加入，如onclick等
				vecBadAttribute.add(strAttribute);
				continue;
			}
			if (hasExpression(strAttribute)) { // 如果含有expression，加入
				vecBadAttribute.add(strAttribute);
				continue;
			}
			if(badLink(strAttribute,tagNode)){	
				vecBadAttribute.add(strAttribute);
				continue;
			}
			for (int j = 0; j < arrBadAttribute.length; j++) { // 遍历所有可能的恶意属性
				if (strAttribute.contains(arrBadAttribute[j])) { // 如果含有，则加入
					vecBadAttribute.add(strAttribute);
					break;
				}
			}
		}
		return vecBadAttribute;
	}
	private static boolean badLink(String strAttribute,TagNode tagNode){	//是否是恶意的链接
		for(int i=0;i<arrBadLink.length;i++){	//遍历所有可能的链接
			if(strAttribute.startsWith(arrBadLink[i])){
				String strContent = tagNode.getAttribute(arrBadLink[i]);	//将链接的属性提出来
				if(strAttribute==null)
					continue;
				//判断是否是http协议头或者ftp协议头，如果不是，说明是恶意的，加入
				if((!strContent.startsWith("http://"))&&(!strContent.startsWith("ftp://"))){
					return true;
				}
			}
		}
		return false;
	}
	//判断是否含有expression，包括全角、半角，只要属性中能凑出expression，就认为含有
	private static boolean hasExpression(String strAttribute){
		int pos = 0;
		for(int i=0;i<arrExpression1.length;i++){
			int pos1 = strAttribute.indexOf(arrExpression1[i], pos);
			int pos2 = strAttribute.indexOf(arrExpression2[i], pos);
			int pos3 = strAttribute.indexOf(arrExpression3[i], pos);
			if(pos1==-1){
				if(pos2==-1){
					if(pos3==-1)
						break;
					else
						pos = pos3;
				}
				else{
					if(pos3==-1)
						pos = pos2;
					else
						pos = pos2<pos3?pos2:pos3;
				}
			}
			else{
				if(pos2==-1){
					if(pos3==-1)
						pos = pos1;
					else
						pos = pos1<pos3?pos1:pos3;
				}
				else{
					if(pos3==-1)
						pos = pos1<pos2?pos1:pos2;
					else{
						pos = pos1<pos2?pos1:pos2;
						pos = pos<pos3?pos:pos3;
					}
				}
			}
			if(i>=arrExpression1.length-1)
				return true;
		}
		return false;
	}
	//恶意标签名
	private static String arrBadName[] = {"applet","base","basefont","bgsound","blink","body","embed",
		"frame","frameset","head","html","ilayer","iframe","layer","link","meta","object","style",
		"title","script","@media","@import","behavior","form"};
	//恶意属性
	private static String arrBadAttribute[] = {"\\","@media","@import","expression","&#","<",">",
		"!import","url(","!--","behavior"};
	//所有链接
	private static String arrBadLink[] = {"dynsrc","href","lowsrc","src","background","value","action",
		"bgsound","behavior"};
	//expression的全角半角所有可能（半角已转化为小写，大写就不必再写一遍了）
	private static String arrExpression1[] = {"e","x","p","r","e","s","s","i","o","n"};
	private static String arrExpression2[] = {"ｅ","ｘ","ｐ","ｒ","ｅ","ｓ","ｓ","ｉ","ｏ","ｎ"};
	private static String arrExpression3[] = {"Ｅ","Ｘ","Ｐ","Ｒ","Ｅ","Ｓ","Ｓ","Ｉ","Ｏ","Ｎ"};	
}