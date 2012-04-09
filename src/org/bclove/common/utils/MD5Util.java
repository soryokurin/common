package org.bclove.common.utils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类
 * @author soryokurin
 *
 */
public class MD5Util {

	//MD5
	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	public static String md5(String text) {
    	MessageDigest msgDigest = null;

    	try {
    		msgDigest = MessageDigest.getInstance("MD5");
    	} catch (NoSuchAlgorithmException e) {
    		throw new IllegalStateException(
    				"System doesn't support MD5 algorithm.");
    	}

    	try {
    		msgDigest.update(text.getBytes("utf-8"));

    	} catch (UnsupportedEncodingException e) {

    		throw new IllegalStateException(
    				"System doesn't support your  EncodingException.");

    	}

    	byte[] bytes = msgDigest.digest();

    	String md5Str = new String(encodeHex(bytes));

    	return md5Str;
    }
	//16位的MD5就是32位的中间的是内容
	public static String md5_16(String text) {
    	return md5(text).substring(8, 24);
    }
	
	public static char[] encodeHex(byte[] data) {

    	int l = data.length;

    	char[] out = new char[l << 1];

    	// two characters form the hex value.
    	for (int i = 0, j = 0; i < l; i++) {
    		out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
    		out[j++] = DIGITS[0x0F & data[i]];
    	}

    	return out;
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(MD5Util.md5_16("111111"));
		System.out.println(MD5Util.md5_16("fsdfsdfsdf"));
		System.out.println(MD5Util.md5_16("111ffsdfsd的防守对方fdsfsdf111"));
		System.out.println(MD5Util.md5_16(""));

	}
	
}
