package net.flyingff.web.dpic.core;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class JavaRandomSeqGenerator implements RandomSequenceGenerator {

	private byte[] keyTransformer(String str) {
		byte[] ret = new byte[16];
		char[] arr = str.toCharArray();
		int cnt = 0, i = 0;
		while(cnt < 16) {
			for(char ch : arr) {
				ret[i] = (byte) ((ret[i] << 4) ^ ch);
				i = (i + 1) % ret.length;
			}
			cnt += arr.length;
		}
		return ret;
	}
	@Override
	public byte[] generate(String password, int len) {
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			byte[] seq = new byte[len];
			c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(
					keyTransformer(password), "AES"));  
			return c.doFinal(seq);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
