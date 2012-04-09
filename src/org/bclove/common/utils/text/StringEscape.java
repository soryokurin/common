package org.bclove.common.utils.text;

public class StringEscape {

	/**
	 * 针对html进行安全问题编码
	 * 
	 * @param str
	 * @return
	 */

	private static StringEscape instance=new StringEscape();
	public static StringEscape getInstance(){
		return instance;
	}
	
	public static String htmlSecurityEscape(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = null;
		int len = str.length();
		char ch;
		try {
			for (int i = 0; i < len; i++) {
				ch = str.charAt(i);
				switch (ch) {
				// 过滤一些恶意代码,这些字符会让utf-8的页面混乱，倒排
				case '\'':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&#39;");
					break;
				case '\"':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&quot;");
					break;
				case '>':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&gt;");
					break;
				case '<':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&lt;");
					break;
				case '&':
					int in = str.indexOf(';', i + 1);
					if (in != -1 && in - i < 9 && (str.substring(i + 1, in).indexOf('&') == -1)) {// 防止2次转义,同时防止&..&这种状况出现。
						if (sb != null) {
							sb.append(ch);
						}
					} else {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&amp;");
					}
					break;

				// 以下两个是为了转换/*xxxx*/这种注释的，这种注释会让代码乱掉
				case '/':
					if ((i + 1) < str.length() && str.charAt(i + 1) == '*') {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&#47;&#42;");
						++i;
						break;
					}

					if (sb != null) {
						sb.append(ch);
					}
					break;
				case '*':
					if ((i + 1) < str.length() && str.charAt(i + 1) == '/') {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&#42;&#47;");
						++i;
						break;
					}

					if (sb != null) {
						sb.append(ch);
					}
					break;
				default:
					// 防止几个从左到右的字符影响utf-8页面
					if (Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
							|| Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
							|| Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE) {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
					} else if (sb != null) {
						sb.append(ch);
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(str);
		}
		if (null != sb) {
			return sb.toString();
		} else {
			return str;
		}
	}

	/**
	 * 反编码
	 * 
	 * @param str
	 * @return
	 */
	public static String htmlSecurityUnescape(String str) {
		if (str == null) {
			return null;
		}
		int firstAmp = str.indexOf('&');
		if (firstAmp == -1) {
			return str;
		} else {
			StringBuilder sb = new StringBuilder(str.length() << 1);
			sb.append(str, 0, firstAmp);
			char c;
			for (int i = firstAmp; i < str.length(); i++) {
				c = str.charAt(i);
				if (c == '&') {
					int nextIdx = i + 1;
					int semiColonIdx = str.indexOf(';', nextIdx);
					if (semiColonIdx == -1) {
						sb.append(c);
						continue;
					}
					int amphersandIdx = str.indexOf('&', nextIdx);
					// 防止&...&..;这种被转换
					if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
						sb.append(c);
						continue;
					}
					String entityContent = str.substring(nextIdx, semiColonIdx);

					// 这里可以改成从map里面取或者从数组里面取，但是因为量不大，用if更加有效
					if (entityContent.equalsIgnoreCase("#39")) {
						sb.append('\'');
					} else if (entityContent.equalsIgnoreCase("#40")) {
						sb.append('(');
					} else if (entityContent.equalsIgnoreCase("#41")) {
						sb.append(')');
					} else if (entityContent.equalsIgnoreCase("lt")) {
						sb.append('<');
					} else if (entityContent.equalsIgnoreCase("gt")) {
						sb.append('>');
					} else if (entityContent.equalsIgnoreCase("amp")) {
						sb.append('&');
					} else if (entityContent.equalsIgnoreCase("quot")) {
						sb.append('"');
					} else {
						sb.append(c);
					}
					i = semiColonIdx;
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		}
	}

	public static void main(String[] args) {
		//String s = "http://<embedsrc=\"http://player.youku.com/player.php/sid/XMjE5NTc1NDQ=/v.swf\"quality=\"high\"width=\"480\"height=\"400\"align=\"middle\"allowScriptAccess=\"sameDomain\"type=\"application/x-shockwave-flash\"></embed>";
		String s = "<script>alert(0)</script>";
		System.out.println(htmlSecurityEscape(s));
		s = "<link>http://adf.com</link>";
		System.out.println(htmlSecurityEscape(s));
	}
}
