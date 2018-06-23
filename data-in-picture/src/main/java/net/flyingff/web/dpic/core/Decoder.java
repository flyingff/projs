package net.flyingff.web.dpic.core;

import java.awt.image.BufferedImage;

public interface Decoder {
	byte[] decode(BufferedImage img, String password);
	default String decodeString(BufferedImage img, String password) {
		byte[] code = decode(img, password);
		return code == null ? null : new String(code);
	}
}
