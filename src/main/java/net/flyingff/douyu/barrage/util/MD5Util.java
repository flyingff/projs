package net.flyingff.douyu.barrage.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

	public static String MD5(String string) {
		try {
			String str = new BigInteger(1, MessageDigest.getInstance("MD5").digest(string.getBytes())).toString(16).toUpperCase();
			while(str.length() < 32) {
				str = "0" + str;
			}
			return str;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
