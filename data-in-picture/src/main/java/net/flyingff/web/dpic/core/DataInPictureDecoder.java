package net.flyingff.web.dpic.core;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class DataInPictureDecoder implements Decoder {
	private final RandomSequenceGenerator generator;

	public DataInPictureDecoder(RandomSequenceGenerator generator) {
		this.generator = generator;
	}
	@Override
	public byte[] decode(BufferedImage img, String password) {
		int w = img.getWidth(), h = img.getHeight(), size = w * h * 6 / 8;
		byte[] data = new byte[size];
		
		int pos = 0, bitPos = 6, rgbArray[] = new int[3];
		for(int row = 0; row < h; row++) {
			for(int col = 0; col < w; col++) {
				int rgb = img.getRGB(col, row);
				rgbArray[0] = (rgb & 0xFF0000) >> 16;
				rgbArray[1] = (rgb & 0xFF00) >> 8;
				rgbArray[2] = (rgb & 0xFF);
				for(int i = 0; i < 3; i++) {
					if(pos >= data.length) break;
					data[pos] |= (rgbArray[i] & 0x03) << bitPos;
					bitPos -= 2;
					if(bitPos < 0) {
						bitPos = 6;
						pos++;
					}
				}
			}
		}
	
		byte[] mask = generator.generate(password, data.length);
		// System.out.println(Arrays.toString(mask));
		for(int i = 0; i < data.length; i++) {
			data[i] = (byte) (data[i] ^ mask[i]);
		}		
		ByteBuffer buf = ByteBuffer.wrap(data);
		
		int len = buf.getInt(), len2 = buf.getInt();
		if(len2 != len) {
			return null;
		}
		byte[] ret = new byte[len];
		buf.get(ret);
		return ret;
	}
	/*
	public static void main(String[] args) throws Exception {
		long tm = System.currentTimeMillis();
		BufferedImage input = ImageIO.read(new File("d:\\1o.png"));
		DataInPictureDecoder decoder = new DataInPictureDecoder(new JavaRandomSeqGenerator());
		byte[] data = decoder.decode(input, "123");
		System.out.println();
		System.out.println(new String(data));
	}
	*/
}
