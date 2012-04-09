package org.bclove.common.utils;

import java.security.MessageDigest;

import org.apache.log4j.Logger;

public class Encoder {

	private static Logger log = Logger.getLogger(Encoder.class);

	// Please note that 2 below methods are used in #getMD5_Base64 only
	// use them in other methods will make it not thread-safe
	private static MessageDigest digest = null;

	private static boolean isInited = false;

	private Encoder() {
	}

	public static String encode(String input) {
		return getMD5_Base64(input);
	}

	/**
	 * This method return a String that has been encrypted as MD5 and then
	 * escaped using Base64.
	 * <p>
	 * 
	 * @param input
	 *            String the string that need encrypted
	 * @return String the string after encrypted
	 */
	public static synchronized String getMD5_Base64(String input) {
		// please note that we dont use digest, because if we
		// cannot get digest, then the second time we have to call it
		// again, which will fail again
		if (isInited == false) {
			isInited = true;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (Exception ex) {
				log
						.fatal(
								"Cannot get MessageDigest. Application may fail to run correctly.",
								ex);
			}
		}
		if (digest == null)
			return input;

		// now everything is ok, go ahead
		byte[] rawData = digest.digest(input.getBytes());
		String retValue = null;
		try {
			retValue = byteArrayToHexString(rawData);
		} catch (Exception e) {
		}
		//System.out.println(retValue);
		return retValue;
	}



	private final static String[] hexDigits = {

	"0", "1", "2", "3", "4", "5", "6", "7",

	"8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 
	 * 转换字节数组为16进制字串
	 * 
	 * @param b
	 *            字节数组
	 * 
	 * @return 16进制字串
	 * 
	 */

	public static String byteArrayToHexString(byte[] b) {

		StringBuffer resultSb = new StringBuffer();

		for (int i = 0; i < b.length; i++) {

			resultSb.append(byteToHexString(b[i]));

		}

		return resultSb.toString();

	}

	private static String byteToHexString(byte b) {

		int n = b;

		if (n < 0)

			n = 256 + n;

		int d1 = n / 16;

		int d2 = n % 16;

		return hexDigits[d1] + hexDigits[d2];

	}
	public static void main (String args[]){
		System.out.println(Encoder.encode("eVa5011")) ;
	}

}