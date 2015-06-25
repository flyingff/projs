package net.flyingff.digitalspring.lt;

import java.nio.ByteBuffer;
import java.util.Random;

public class Encoder {
	public static final int HEADLEN = 4;
	private int dropLen;
	private byte[] data;
	private int maxDropIndex;
	
	Encoder(int dropLen) {
		this.dropLen = dropLen;
	}
	public void setData(byte[] data) {
		this.data = data;
		maxDropIndex = (int) Math.ceil((double)data.length / dropLen) - 1;
		if (maxDropIndex < 2)
			throw new RuntimeException("Too little data to send:" + maxDropIndex);
	}
	int[] repeat = new int[4];
	public int write(byte[] dest, int offset, int maxlen) {
		if(maxlen < dropLen + 4) {
			throw new RuntimeException("Too small buffer to write one drop");
		}
		// clear to zero
		/*for(int i = 0; i < dest.length; i++) {
			dest[i + offset] = 0;
		}*/
		// wrap into byte buffer
		ByteBuffer buf = ByteBuffer.wrap(dest, offset, maxlen);
		int writelen = 4, seed = (int) System.nanoTime();
		// write seed in
		buf.putInt(seed);
		Random r = new Random(seed);
		while(buf.remaining() >= dropLen) {
			int num = ((r.nextInt() & 0xff) % 3) + 1;	// 1-3 blocks selected
outer:		for(int i = 0; i < num; i++) { 
				int offx = ((r.nextInt() & 0x7fffffff) % (maxDropIndex + 1)) * dropLen;
				repeat[i] = offx;
				for(int j = 0; j < i; j++) {
					if (repeat[j] == offx)
						continue outer;
				}
				//System.out.println(offx + 256);
				if (i == 0) {
					copy(dest, writelen, data, offx);
					//System.out.print("data in " + writelen / dropLen + " comes from: " + offx / dropLen);
				} else {
					exclusive(dest, writelen, data, offx);
					//System.out.print(", " + offx / dropLen);
				}
			}
			//System.out.println();
			writelen += dropLen;
			buf.position(writelen);
		}
		return writelen;
	}
	private void exclusive(byte[] dest, int offdest, byte[] source, int offsrc){
		for(int i = 0; i < dropLen && i + offsrc < source.length && i + offdest < dest.length; i++) {
			dest[i + offdest] ^= source[i + offsrc];
		}
	}
	private void copy(byte[] dest, int offdest, byte[] source, int offsrc){
		for(int i = 0; i < dropLen && i + offsrc < source.length && i + offdest < dest.length; i++) {
			dest[i + offdest] = source[i + offsrc];
			//System.out.print(() + ", ");
		}
		//System.out.println();
	}
}
