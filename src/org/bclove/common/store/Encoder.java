package org.bclove.common.store;

import java.security.MessageDigest;

public class Encoder {

	private static MessageDigest digest = null;

	private static boolean isInited = false;

	private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	public static String encode(String input) {
		return getMD5_Base64(input);
	}

	public static synchronized String getMD5_Base64(String input) {
		if (!isInited) {
			isInited = true;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		if (digest == null) {
			return input;
		}

		byte[] rawData = digest.digest(input.getBytes());
		String retValue = null;
		try {
			retValue = byteArrayToHexString(rawData);
		} catch (Exception e) {
		}
		return retValue;
	}

	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();

		for (int i = 0; i < b.length; ++i) {
			resultSb.append(byteToHexString(b[i]));
		}

		return resultSb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;

		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;

		int d2 = n % 16;

		return hexDigits[d1] + hexDigits[d2];
	}

}
