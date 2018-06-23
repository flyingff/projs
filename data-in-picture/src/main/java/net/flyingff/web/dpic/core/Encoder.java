package net.flyingff.web.dpic.core;

import java.awt.image.BufferedImage;

public interface Encoder {
	BufferedImage encode(BufferedImage img, String password, byte[] data);
	default BufferedImage encodeString(BufferedImage img, String password, String data) {
		return encode(img, password, data.getBytes());
	}
}
