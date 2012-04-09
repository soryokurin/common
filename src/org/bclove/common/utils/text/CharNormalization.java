package org.bclove.common.utils.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bclove.common.utils.ResourceUtils;

/**
 * 类CompositeCharConverter.java
 */
public class CharNormalization {

	public static final int SYMBOL_MASK = 0x1 << 16;
	public static final int TRAD_MASK = 0x2 << 16;
	public static final int DBC_MASK = 0x4 << 16;
	public static final int UPPER_MASK = 0x8 << 16;
	public static final int HAN_MASK = 0x10 << 16;
	public static final int SYNONYMY_MASK = 0x20 << 16;
	public static final int MARS_MASK = 0x40 << 16;

	public static final String TRAD2SIMP_FILE = "Trad2Simp_CT.txt";
	public static final String UPPER_FILE = "Upper2Lower_CT.txt";
	public static final String SYMBOL_FILE = "Symbol_CT.txt";
	public static final String DBC_FILE = "DBC_CT.txt";
	public static final String SYNONYMY_FILE = "Synonymy_CT.txt";
	public static final String MARS2SIMP_FILE = "Mars2Simp_CT.txt";

	public static final char PARAGRAPH_BREAK = '\n';
	/**
	 * 默认控制字符。
	 */
	public static final char DEFAULT_BLANK_CHAR = (char) 0xffff;
	/**
	 * 中文代码页分隔符
	 */
	public static final char CJK_UNIFIED_IDEOGRAPHS_START = 0x4e00;
	public static final char CJK_UNIFIED_IDEOGRAPHS_END = 0xA000;
	// public static final char CJK_UNIFIED_IDEOGRAPHS_START = 19968;
	// public static final char CJK_UNIFIED_IDEOGRAPHS_END =40869;

	/**
	 * 半角空格的值，在ASCII中为32(Decimal)
	 */
	public static final char DBC_SPACE = ' '; // 半角空格

	// 字符表编码
	private static int[] codeTable;

	static {
		loadCodeTable();
	}

	/**
	 * <PRE>
	 * 转换一个字符串,为了性能所有代码写在一个方法内，减少方法调用时间。 主要完成以下工作： &lt;br&gt;
	 * 1、繁体转简体（可选） &lt;br&gt;
	 * 2、火星转简体（可选) &lt;br&gt;
	 * 3、全角转半角（可选）&lt;br&gt;
	 * 4、转小写（可选）&lt;br&gt;
	 * 5、过滤非日文字符（可选）&lt;br&gt;
	 * 6、过滤一些同义字符(可选) 比如 ㊆到7这种 &lt;br&gt;
	 * 7、过滤空白字符（包括&quot;\n&quot;&quot;\r&quot;&quot; &quot;&quot;\t&quot;等）（可选）&lt;br&gt;
	 * 8、连续空白字符是否保留一个（可选）&lt;br&gt;
	 * 
	 * </PRE>
	 * 
	 * @param src
	 *            源字符串
	 * @param needT2S
	 *            繁简体转换
	 * @param needM2S
	 *            火星文转换
	 * @param needDBC
	 *            全半角转换
	 * @param ignoreCase
	 *            大写转小写
	 * @param filterNoneHanLetter
	 *            过滤非日文字符
	 * @param convertSynonymy
	 *            过滤同义字符
	 * @param filterSymbol
	 *            是否过滤symbol字符（包括"\n""\r"" ""\t"等,见Symbol_CT.txt），
	 * @param keepLastSymbol
	 *            连续symbol字符是否保留一个
	 * @return 转换后的字符串，长度可能比转换前的短
	 */
	public static String compositeTextConvert(String src, boolean needT2S,
			boolean needM2S, boolean needDBC, boolean ignoreCase,
			boolean filterNoneJapaneseLetter, boolean convertSynonymy,
			boolean filterSymbol, boolean keepLastSymbol) {
		if (src == null || src.length() == 0) {
			return src;
		}
		char[] chs = src.toCharArray();
		StringBuilder buffer = new StringBuilder(chs.length);
		for (int i = 0; i < chs.length; i++) {
			char c = compositeCharConvert(chs[i], needT2S, needM2S, needDBC,
					ignoreCase, filterNoneJapaneseLetter, convertSynonymy,
					filterSymbol);
			if (keepLastSymbol) {
				if (c == DEFAULT_BLANK_CHAR && i < chs.length - 1) {
					char next = chs[i + 1];
					if (isSeperatorSymbol(chs[i]) && isSeperatorSymbol(next)) {
						continue;
					} else {
						c = (char) codeTable[(char) chs[i]];
					}
				}
			}
			if (c != DEFAULT_BLANK_CHAR) {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	public static boolean isSeperatorSymbol(char c) {
		int i = codeTable[c];
		return (i & SYMBOL_MASK) != 0;
	}

	/**
	 * <PRE>
	 * 转换一个字符串,为了性能所有代码写在一个方法内，减少方法调用时间。 主要完成以下工作： &lt;br&gt;
	 * 1、繁体转简体（可选） &lt;br&gt;
	 * 2、全角转半角（可选）&lt;br&gt;
	 * 3、转小写（可选）&lt;br&gt;
	 * 4、过滤非日文字符（可选）&lt;br&gt;
	 * 5、过滤symbol字符（包括&quot;\n&quot;&quot;\r&quot;&quot; &quot;&quot;\t&quot;等）（可选）&lt;br&gt;
	 * 6、连续symbol字符是否保留一个（可选）&lt;br&gt;
	 * 
	 * </PRE>
	 * 
	 * @param needT2S
	 *            繁简体转换
	 * @param needDBC
	 *            全半角转换
	 * @param ignoreCase
	 *            大写转小写
	 * @param filterNoneJapaneseLetter
	 *            过滤非日文字符
	 * @param filterSymbol
	 *            是否过滤symbol字符（包括"\n""\r"" ""\t"等,见Symbol_CT.txt），
	 * @param keepLastSpace
	 *            连续symbol字符是否保留一个
	 * @return 转换后的字符串，长度可能比转换前的短
	 */
	public static final char compositeCharConvert(char c, boolean needT2S,
			boolean needM2S, boolean needDBC, boolean ignoreCase,
			boolean filterNoneJapaneseLetter, boolean convertSynonymy,
			boolean filterSymbol) {
		if (needT2S) {
			c = convertCharT2S(c);
		}
		if (needM2S) {
			c = convertCharM2S(c);
		}
		if (needDBC) {
			c = convertCharDBC(c);
		}
		if (ignoreCase) {
			c = convertChar2Lower(c);
		}
		if (filterNoneJapaneseLetter) {
			c = filterNonJapanese(c);
		}
		if (convertSynonymy) {
			c = convertCharSynonymy(c);
		}
		if (filterSymbol) {
			c = filterSymbol(c);
		}
		return c;
	}

	public static char convertCharT2S(char ch) {
		int ret = codeTable[ch];
		if ((ret & TRAD_MASK) != 0) {
			return (char) ret;
		}
		return ch;
	}

	public static char convertCharM2S(char ch) {
		int ret = codeTable[ch];
		if ((ret & MARS_MASK) != 0) {
			return (char) ret;
		}
		return ch;
	}

	public static char convertChar2Lower(char ch) {
		int ret = codeTable[ch];
		if ((ret & UPPER_MASK) != 0) {
			return (char) ret;
		}
		return ch;
	}

	public static char convertCharDBC(char ch) {
		int ret = codeTable[ch];
		if ((ret & DBC_MASK) != 0) {
			return (char) ret;
		}
		return ch;
	}

	public static char convertCharSynonymy(char ch) {
		int ret = codeTable[ch];
		if ((ret & SYNONYMY_MASK) != 0) {
			return (char) ret;
		}
		return ch;
	}
	/*
	public static char filterNonHan(char ch) {
		int ret = codeTable[ch];
		if ((ret & HAN_MASK) != 0) {
			return ch;
		}
		return DEFAULT_BLANK_CHAR;
	}
	*/
	
	public static char filterNonJapanese(char ch){
		Character.UnicodeBlock cu = Character.UnicodeBlock.of(ch);
		if(cu == Character.UnicodeBlock.HIRAGANA ||
			cu ==Character.UnicodeBlock.KATAKANA ||
			cu ==Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ){
			return ch;
		}
		return DEFAULT_BLANK_CHAR;
	}

	public static char filterSymbol(char ch) {
		int ret = codeTable[ch];
		if ((ret & SYMBOL_MASK) != 0) {
			return DEFAULT_BLANK_CHAR;
		}
		return ch;
	}

	public static char getCharFromTable(char c) {
		return (char) codeTable[c];
	}

	/**
	 * 反转字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String stringReverse(String str) {
		StringBuffer rev = new StringBuffer(str);
		rev.reverse();
		return rev.toString();
	}

	/**
	 * 装载编码表,顺序非常重要，过滤类往往只是打标记，应先处理。
	 */
	private static final void loadCodeTable() {
		if (codeTable == null) {
			codeTable = new int[65536];
			// 初始化数组
			for (int i = 0; i < codeTable.length; i++) {
				codeTable[i] = i;
			}
			String ENCODING = "UTF-8";
			// 处理汉字过滤
			for (int i = CJK_UNIFIED_IDEOGRAPHS_START; i < CJK_UNIFIED_IDEOGRAPHS_END; i++) {
				codeTable[i] = HAN_MASK | codeTable[i];
			}
			// 处理Symbol过滤
			loadCodeTable(SYMBOL_FILE, ENCODING, codeTable, SYMBOL_MASK);
			// 处理繁体转简体
			loadCodeTable(TRAD2SIMP_FILE, ENCODING, codeTable, TRAD_MASK);
			// 处理火星文转简体
			loadCodeTable(MARS2SIMP_FILE, ENCODING, codeTable, MARS_MASK);
			// 处理大写转小写
			loadCodeTable(UPPER_FILE, ENCODING, codeTable, UPPER_MASK);
			// 处理全角转半角
			loadCodeTable(DBC_FILE, ENCODING, codeTable, DBC_MASK);
			// 处理汉字过滤
			for (int i = CJK_UNIFIED_IDEOGRAPHS_START; i < CJK_UNIFIED_IDEOGRAPHS_END; i++) {
				codeTable[i] = HAN_MASK | codeTable[i];
			}
			// 处理Symbol过滤
			loadCodeTable(SYMBOL_FILE, ENCODING, codeTable, SYMBOL_MASK);
			// 处理Synonymy
			loadCodeTable(SYNONYMY_FILE, ENCODING, codeTable, SYNONYMY_MASK);
		}
	}

	private static final void loadCodeTable(String file, String encoding,
			int[] codeTbl, int mask) {
		// String pckName = CharNormalization.class.getPackage().getName();
		// file = "/" + pckName.replace('.', '/') + "/resources/" + file;
//		file = "resources/" + file;
		InputStream istream = null;
		try {
			istream = ResourceUtils.getResourceAsStream(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (istream == null) {
			throw new RuntimeException("Could not find code table: " + file);
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(istream, encoding), 2048);
			String line = null;
			int i = 0;
			char c = 0;
			while ((line = reader.readLine()) != null) {
				if (line.length() > 5) {
					i = Integer.parseInt(line.substring(1, 5), 16);// 将U0020类似的转换为整数字符值
					if (i == ',' || i == '\n' || i == '\r' || i == '\t') {// ',','\n','\r'特殊处理
						c = (char) i;
					} else {
						String[] tokens = line.split(",");
						if (tokens.length == 3) {// 非','情况
							String last = tokens[2];
							if (last.length() == 1) {
								c = last.charAt(0);
							} else {
								last = last.trim();
								if (last.length() == 0) {// 如果是多个空格，trim可能使之为空，取空格
									c = DBC_SPACE;
								} else {
									c = last.charAt(0);
								}
							}
						}
					}
				}
				int ret = codeTbl[i];
				if (ret == i) {
					codeTbl[i] = (mask | c);
				} else {
					ret = mask | ret;
					codeTbl[i] = (ret & 0xffff0000) | c;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not read code table: " + file, e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static char[] charArray(char[] ch) {
		int len = ch.length;
		char[] rch = new char[len];
		for (int i = 0; i < len; i++) {
			rch[i] = ch[len - 1 - 1];
		}
		return rch;
	}
}
