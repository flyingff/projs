package net.flyingff.digitalspring.lt;

import java.nio.ByteBuffer;
import java.util.Random;

public class Encoder {
	private int dropLen;
	private byte[] data;
	private int maxDropIndex;
	
	Encoder(int dropLen, byte[] data) {
		this.dropLen = dropLen;
		this.data = data;
		maxDropIndex = (int) Math.ceil((double)data.length / dropLen);
	}
	
	public int write(byte[] dest, int offset, int maxlen) {
		if(maxlen < dropLen + 4) {
			throw new RuntimeException("Too small buffer to write one drop");
		}
		// clear to zero
		for(int i = 0; i < dest.length; i++) {
			dest[i + offset] = 0;
		}
		// wrap into byte buffer
		ByteBuffer buf = ByteBuffer.wrap(dest, offset, maxlen);
		int writelen = 4, seed = (int) System.nanoTime();
		// write seed in
		buf.putInt(seed);
		Random r = new Random(seed);
		while(buf.remaining() >= dropLen) {
			int num = (r.nextInt() % 3) + 1;	// 1-4 blocks selected
			for(int i = 0; i < num; i++) {
				int offx = (r.nextInt() % maxDropIndex) * dropLen;
				exclusive(dest, data, writelen, offx);
			}
			writelen += dropLen;
			buf.position(writelen);
		}
		return writelen;
	}
	private void exclusive(byte[] dest, byte[] source, int offdest, int offsrc){
		for(int i = 0; i < dropLen;i++) {
			dest[i + offdest] ^= source[i + offsrc];
		}
	}
}
