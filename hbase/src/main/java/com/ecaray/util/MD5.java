package com.ecaray.util;

import java.security.MessageDigest;

public class MD5 {

	private static final String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	private static String byteArrayToHexString(byte[] paramArrayOfByte) {
		StringBuffer localStringBuffer = new StringBuffer();
		for (int i = 0; i < paramArrayOfByte.length; ++i)
			localStringBuffer.append(byteToHexString(paramArrayOfByte[i]));
		return localStringBuffer.toString();
	}

	private static String byteToHexString(byte paramByte) {
		int i = paramByte;
		if (i < 0)
			i = 256 + i;
		int j = i / 16;
		int k = i % 16;
		return hexDigits[j] + hexDigits[k];
	}

	public static String MD5Encode(String paramString) {
		String str = null;
		try {
			str = new String(paramString);
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			str = byteArrayToHexString(localMessageDigest
					.digest(str.getBytes()));
		} catch (Exception localException) {
		}
		return str;
	}

	public static String MD5Encode(String paramString1, String paramString2) {
		String str = null;
		try {
			str = new String(paramString1);
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			str = byteArrayToHexString(localMessageDigest.digest(str
					.getBytes(paramString2)));
		} catch (Exception localException) {
		}
		return str;
	}

	public static String MD5Encode(byte[] paramArrayOfByte) {
		String str = null;
		try {
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			str = byteArrayToHexString(localMessageDigest
					.digest(paramArrayOfByte));
		} catch (Exception localException) {
		}
		return str;
	}

	public static void main(String[] paramArrayOfString) {
		System.err.println(MD5Encode("测试文件数据...."));
	}

}
