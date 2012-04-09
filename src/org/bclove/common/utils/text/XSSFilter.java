
package org.bclove.common.utils.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;



public class XSSFilter {
	private XSSFilter() {
	}

	private static XSSFilter instance = new XSSFilter();

	public static XSSFilter getInstance() {
		return instance;
	}

	/**
	 * Encode Html代码，让其不具有危害性
	 * 
	 * @param InputString
	 * @return OutputString
	 */
	public static String encodeHTMl(String InputString) {
		return StringEscape.htmlSecurityEscape(InputString);
	}

	/**
	 * decodeHtml代码，在编辑的时候用
	 * 
	 * @param InputString
	 * @return OutputString
	 */
	public String decodeHTML(String InputString) {
		return StringEscape.htmlSecurityUnescape(InputString);
	}

	/**
	 * 过滤掉恶意标签和属性，用于所见即所得的文本
	 * 
	 * @param content
	 *            需要过滤的内容
	 * @return 返回过滤后的内容
	 * @throws Exception
	 *             如果html解析失败，说明被用户恶意修改过，不能让发
	 */
	public String filtrateEditorTag(String content) {

		content = decodeHTML(content);

		String contentLow = content.toLowerCase(); // 全部转化为小写
		Parser parser = Parser.createParser(contentLow, "gb2312"); // 初始化解析器
		NodeList nodes;
		try {
			nodes = parser.extractAllNodesThatMatch(filter); // 获取所有tag的node
		} catch (Exception e) {
			e.printStackTrace();
			// RecordLog.getInstance().writeInfo("html解析失败,content="+content);
			return null;
		}
		int hasCut = 0; // 已经切除了多少
		Node[] nodeArray = nodes.toNodeArray();
		StringBuffer sb = new StringBuffer(content);
		for (int i = 0; i < nodeArray.length; i++) { // 遍历所有TagNode
			TagNode tagNode = (TagNode) nodeArray[i]; // 获取一个tagNode
			boolean cutTag = false;
			if (isBadName(tagNode)) { // 是否是恶意的tag
				// 切掉恶意tag
				cutTag = true;
			} else { // 切除恶意属性
				Vector<String> vecBadAttribute = badAttribute(tagNode);
				if (vecBadAttribute != null) {
					Iterator<String> iteraror = vecBadAttribute.iterator();
					while (iteraror.hasNext()) {
						String badAttribute = iteraror.next();
						int pos = contentLow.indexOf(badAttribute, tagNode
								.getTagBegin());
						if (pos == -1 || pos > tagNode.getTagEnd()) {// 如果找不到了，
							// 切掉tag
							cutTag = true;
							break;
						} else { // 如果找到了，切掉属性
							sb.delete(pos - hasCut, pos + badAttribute.length()
									- hasCut);
							hasCut += badAttribute.length();
						}
					}
				} else {
					cutTag = true;
				}
			}
			if (cutTag) {
				sb.delete(tagNode.getTagBegin() - hasCut, tagNode.getTagEnd()
						- hasCut);
				hasCut += tagNode.getTagEnd() - tagNode.getTagBegin();
			}
		}
		return sb.toString();// 返回过滤后的字符串
	}

	/**
	 * 
	 * @param tagNode
	 *            标签节点
	 * @return 是否是恶意的标签
	 */
	private static boolean isBadName(TagNode tagNode) {
		String name = tagNode.getTagName().toLowerCase(); // 获取标签名
		if (name == null) {
			return false;
		}
		for (int i = 0; i < arrBadName.length; i++) { // 遍历所有可能的恶意标签名
			if (name.indexOf(arrBadName[i]) != -1) // 如果是则返回true
				return true;
		}
		return false;
	}

	private static boolean containBadAttribute(String att) {
		for (int j = 0; j < arrBadAttribute.length; j++) { // 遍历所有可能的恶意属性

			if (att.contains(arrBadAttribute[j])) { // 如果含有，则加入
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static String filterBadAttribute(TagNode tagNode) {
		StringBuffer sb = new StringBuffer();
		Vector vecAttribute = tagNode.getAttributesEx();
		for (int i = 0; i < vecAttribute.size(); i++) { // 遍历所有属性
			String strAttribute = vecAttribute.elementAt(i).toString();
			System.out.println(strAttribute);
			if (hasOnAttribute(strAttribute) || hasExpression(strAttribute)
					|| badLink(strAttribute, tagNode)
					|| containBadAttribute(strAttribute)) {
				continue;
			} else {
				sb.append(strAttribute);
			}
		}
		return sb.toString();
	}

	/**
	 * 提取标签中可能为恶意的属性
	 * 
	 * @param tagNode
	 *            需要提取的标签
	 * @return 返回可能是恶意的属性
	 */
	@SuppressWarnings("unchecked")
	private static Vector<String> badAttribute(TagNode tagNode) { // 是否含有恶意属性
		Vector vecAttribute = tagNode.getAttributesEx(); // 获取该标签的所有属性
		Vector<String> vecBadAttribute = new Vector<String>();
		Set<String> setAttributeHead = new HashSet<String>();
		for (int i = 0; i < vecAttribute.size(); i++) { // 遍历所有属性
			String strAttribute = vecAttribute.elementAt(i).toString();
			if (strAttribute == null) // 不知道会不会出现空值，但是加一下，安全些
				continue;
			String strAttributeHead = null;
			if (strAttribute.length() <= 3)
				continue;
			else
				strAttributeHead = strAttribute.substring(0, 3);
			if (setAttributeHead.contains(strAttributeHead)) {
				return null;
			}
			setAttributeHead.add(strAttributeHead);
			if (hasOnAttribute(strAttribute)) { // 如果是以on开头的属性，加入，如onclick等
				vecBadAttribute.add(strAttribute);
				continue;
			}
			if (hasExpression(strAttribute)) { // 如果含有expression，加入
				vecBadAttribute.add(strAttribute);
				continue;
			}
			if (badLink(strAttribute, tagNode)) {
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

	/**
	 * 是否是恶意的链接，即src后，非http或者ftp协议的
	 * 
	 * @param strAttribute
	 *            属性名和属性值的总体
	 * @param tagNode
	 *            该属性存在的节点
	 * @return 是否是恶意的属性
	 */
	private static boolean badLink(String strAttribute, TagNode tagNode) { // 是否是恶意的链接
		if (strAttribute == null)
			return false;
		String strContent = null;
		for (int i = 0; i < arrBadLink.length; i++) { // 遍历所有可能的链接
			if (strAttribute.startsWith(arrBadLink[i])) {
				strContent = tagNode.getAttribute(arrBadLink[i]); // 将链接的属性提出来
				if (strContent == null)
					return true;
				// 判断是否是http协议头或者ftp协议头，如果不是，说明是恶意的，加入
				if ((!strContent.startsWith("http://"))
						&& (!strContent.startsWith("ftp://"))
						&& (!strContent.startsWith("https://"))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否含有expression，包括全角、半角，只要属性中能凑出expression，就认为含有
	 * 
	 * @param strAttribute
	 *            属性
	 * @return 是否含有expression
	 */
	private static boolean hasExpression(String strAttribute) {
		int pos = 0;
		for (int i = 0; i < arrExpression1.length; i++) {
			int pos1 = strAttribute.indexOf(arrExpression1[i], pos);
			int pos2 = strAttribute.indexOf(arrExpression2[i], pos);
			int pos3 = strAttribute.indexOf(arrExpression3[i], pos);
			if (pos1 == -1) {
				if (pos2 == -1) {
					if (pos3 == -1)
						break;
					else
						pos = pos3;
				} else {
					if (pos3 == -1)
						pos = pos2;
					else
						pos = pos2 < pos3 ? pos2 : pos3;
				}
			} else {
				if (pos2 == -1) {
					if (pos3 == -1)
						pos = pos1;
					else
						pos = pos1 < pos3 ? pos1 : pos3;
				} else {
					if (pos3 == -1)
						pos = pos1 < pos2 ? pos1 : pos2;
					else {
						pos = pos1 < pos2 ? pos1 : pos2;
						pos = pos < pos3 ? pos : pos3;
					}
				}
			}
			if (i >= arrExpression1.length - 1)
				return true;
		}
		return false;
	}

	private static boolean hasOnAttribute(String strAttribute) {

		for (String s : onAttributes) {
			if (strAttribute.contains(s))
				return true;
		}
		return false;
	}

	private final NodeFilter filter = new NodeClassFilter(TagNode.class); // 过滤器，
	// 只要tag的node
	// ，
	// 别的不要

	// 恶意标签名
	private static final String arrBadName[] = { "applet", "base", "basefont",
			"bgsound", "blink", "body", "embed", "frame", "frameset", "head",
			"html", "ilayer", "iframe", "layer", "link", "meta", "object",
			"style", "title", "script", "@media", "@import", "behavior", "form" };
	// 恶意属性
	private static final String arrBadAttribute[] = { "\\", "@media",
			"@import", "expression", "&#", "<", ">", "!import", "url(", "!--",
			"behavior", "script" };

	// 所有链接
	private static final String arrBadLink[] = { "dynsrc", "href", "lowsrc",
			"src", "background", "value", "action", "bgsound", "behavior" };
	// expression的全角半角所有可能（半角已转化为小写，大写就不必再写一遍了）
	private static final String arrExpression1[] = { "e", "x", "p", "r", "e",
			"s", "s", "i", "o", "n" };
	private static final String arrExpression2[] = { "ｅ", "ｘ", "ｐ", "ｒ", "ｅ",
			"ｓ", "ｓ", "ｉ", "ｏ", "ｎ" };
	private static final String arrExpression3[] = { "Ｅ", "Ｘ", "Ｐ", "Ｒ", "Ｅ",
			"Ｓ", "Ｓ", "Ｉ", "Ｏ", "Ｎ" };

	private static final String arrBadWordForCSS[] = { "@media", "@import",
			"expression", "behavior", "behaviour", "moz-binding",
			"include-source", "content", "<", ">", "\\", "&#" }; // 注意顺序，一定要先去掉

	// "\\","&#"

	private static final String[] onAttributes = { "onabort", "onactivate",
			"onafterprint", "onafterupdate", "onbeforeactivate",
			"onbeforecopy", "onbeforecut", "onbeforedeactivate",
			"onbeforeeditfocus", "onbeforepaste", "onbeforeprint",
			"onbeforeunload", "onbeforeupdate", "onblur", "onbounce",
			"oncellchange", "onchange", "onclick", "oncontextmenu",
			"oncontrolselect", "oncopy", "oncut", "ondataavailable",
			"ondatasetchanged", "ondatasetcomplete", "ondblclick",
			"ondeactivate", "ondrag", "ondragend", "ondragenter",
			"ondragleave", "ondragover", "ondragstart", "ondrop", "onerror",
			"onerrorupdate", "onfilterchange", "onfinish", "onfocus",
			"onfocusin", "onfocusout", "onhelp", "onkeydown", "onkeypress",
			"onkeyup", "onlayoutcomplete", "onload", "onlosecapture",
			"onmousedown", "onmouseenter", "onmouseleave", "onmousemove",
			"onmouseout", "onmouseover", "onmouseup", "onmousewheel", "onmove",
			"onmoveend", "onmovestart", "onpaste", "onpropertychange",
			"onreadystatechange", "onreset", "onresize", "onresizeend",
			"onresizestart", "onrowenter", "onrowexit", "onrowsdelete",
			"onrowsinserted", "onscroll", "onselect", "onselectionchange",
			"onselectstart", "onstart", "onstop", "onsubmit", "onunload" };

	public String filterForCSS(String content) {
		if (content == null)
			return null;
		content = content.replaceAll("/\\*.*\\*/", "");// 删除css注释内容，保证页面不会乱
		StringBuffer sbSrc = new StringBuffer(content);

		content = CharNormalization.compositeTextConvert(content, true, true,
				true, true, false, false, true, true);

		StringBuffer sbChange = new StringBuffer(content);
		int pos = -1;
		for (int i = arrBadWordForCSS.length - 1; i >= 0; i--) {
			pos = sbChange.indexOf(arrBadWordForCSS[i]);
			while (pos != -1) {
				sbSrc.delete(pos, pos + arrBadWordForCSS[i].length());
				sbChange.delete(pos, pos + arrBadWordForCSS[i].length());
				pos = sbChange.indexOf(arrBadWordForCSS[i], pos + 1);
			}
		}
		return sbSrc.toString();
	}

	public String filterHtml(String content) {
		StringBuffer sb = new StringBuffer();
		Parser parser = Parser.createParser(content, "utf-8");
		NodeFilter nodefilter = new NodeClassFilter(TagNode.class);
		NodeFilter textfilter = new NodeClassFilter(TextNode.class);
		NodeList nodelist = null;
		OrFilter lastFilter = new OrFilter();
		lastFilter.setPredicates(new NodeFilter[] { nodefilter, textfilter });
		try {
			nodelist = parser.parse(lastFilter);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		Node[] nodes = nodelist.toNodeArray();
		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];

			if (node instanceof TagNode) {
				TagNode tagNode = (TagNode) node;
				if (!isBadName(tagNode)) { // 非恶意tag
					sb.append("<" + filterBadAttribute(tagNode) + ">");
				}
			} else if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				sb.append(encodeHTMl(textNode.getText()));
			}
		}
		return sb.toString();
	}

	public static void main(String[] argc) {
		 String content ="test<br>test<br>test<br>test<br><br>";
		 long now=System.currentTimeMillis();
		 for(int i=0;i<1;i++){
		 System.out.println(XSSFilter.getInstance().filtrateEditorTag(content));
		 }
		 System.out.println(System.currentTimeMillis()-now);
//		 FileOperate fo = new FileOperate();
//		 String s = null;
//		 try {
//		 s = fo.readTxt("c:\\sina.htm", "UTF-8");
//		 } catch (IOException e) {
//		 //
//		 e.printStackTrace();
//		 }
//		 int count = 100;
//		  now = System.currentTimeMillis();
//		 for (int i = 0; i < count; i++) {
//		 XSSFilter.encodeHTMl(s);
//		 }
//		 System.out.println(System.currentTimeMillis() - now);
//		 System.out.println(XSSFilter.encodeHTMl("()"));
//		 System.out.println(XSSFilter.getInstance().decodeHTML(XSSFilter.
//		 encodeHTMl("()")));
//		FileOperate fo = new FileOperate();
//		String s = null;
//		try {
//			s = fo.readTxt("/tmp/2.test", "utf-8");
//			System.out.println(s);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(XSSFilter.getInstance().filterHtml(s));
		// System.out.println(XSSFilter.getInstance().filtrateEditorTag(s));
	}
}
