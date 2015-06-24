package net.flyingff.digitalspring.lt;

import java.nio.ByteBuffer;
import java.util.Random;

public class Decoder {
	private int dropLen;
	private byte[] buf;
	private boolean[] flag;
	
	Decoder(int dropLen) {
		this.dropLen = dropLen;
	}
	
	public void clear(){
		
	}
	
	public void init(byte[] buf){
		this.buf = buf;
		flag = new boolean[(int) Math.ceil((double)buf.length / dropLen)];
	}
	public boolean update(byte[] data, int offset, int len){
		ByteBuffer buf = ByteBuffer.wrap(data, offset, len);
		int seed = buf.getInt();
		Random r = new Random(seed);
		
		return false;
	}
	public byte[] finish(){
		try {
			return buf;
		} finally{
			buf = null;
		}
	}
	private void put(){
		
	}
}

class Unit{
	int[] packets;
	int remainings;
	byte[] data;
	int offset;
	public Unit(byte[] data, int offset, int... packets){
		this.data = data;
		this.offset = offset;
		this.packets = packets;
		remainings = packets.length;
	}
}