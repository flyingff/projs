package net.flyingff.web.dpic.core;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Random;


public class DataInPictureEncoder implements Encoder {
	private final RandomSequenceGenerator generator;

	public DataInPictureEncoder(RandomSequenceGenerator generator) {
		this.generator = generator;
	}
	@Override
	public BufferedImage encode(BufferedImage img, String password, byte[] data) {
		int w = img.getWidth(), h = img.getHeight(), size = w * h * 6 / 8;
		byte[] fullfill = new byte[size];
		// random fullfill
		new Random().nextBytes(fullfill);
		ByteBuffer buf = ByteBuffer.wrap(fullfill);
		int dataLen = Math.min(data.length, fullfill.length - 8);
		buf.putInt(dataLen);
		buf.putInt(dataLen);
		System.arraycopy(data, 0, fullfill, 8, dataLen);
		
		byte[] mask = generator.generate(password, fullfill.length);
		for(int i = 0; i < fullfill.length; i++) {
			//System.out.printf("%02x ", fullfill[i]);
			fullfill[i] = (byte) (fullfill[i] ^ mask[i]);
			//if(i % 16 == 15) {
			//	System.out.println();
			//}
		}
		
		int pos = 0, bitMask = 0xC0, bitPos = 6, rgbArray[] = new int[3];
		
		BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		for(int row = 0; row < h; row++) {
			for(int col = 0; col < w; col++) {
				int rgb = img.getRGB(col, row);
				rgbArray[0] = (rgb & 0xFF0000) >> 16;
				rgbArray[1] = (rgb & 0xFF00) >> 8;
				rgbArray[2] = (rgb & 0xFF);
				for(int i = 0; i < 3; i++) {
					if(pos >= fullfill.length) break;
					rgbArray[i] = (rgbArray[i] & 0xFC) | ((fullfill[pos] & bitMask) >> bitPos);
					
					bitMask >>>= 2;
					bitPos -= 2;
					if(bitPos < 0) {
						bitPos = 6;
						bitMask = 0xC0;
						pos++;
					}
				}
				
				ret.setRGB(col, row, ((rgbArray[0] & 0xFF) << 16) | ((rgbArray[1] & 0xFF) << 8) | (rgbArray[2] & 0xFF));
			}
		}
		return ret;
	}
	/*
	public static void main(String[] args) throws Exception {
		BufferedImage input = ImageIO.read(new File("d:\\1.png"));
		DataInPictureEncoder encoder = new DataInPictureEncoder(new JavaRandomSeqGenerator());
		BufferedImage out = encoder.encode(input, "123", "Hello world".getBytes());
		ImageIO.write(out, "png", new File("D:\\1o.png"));
	}
	*/
}
