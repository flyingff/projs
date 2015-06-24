package net.flyingff.digitalspring.multicast;



public class SpringCup {
	private static long lastSTMP;
	private long stamp;
	private int maxSerial;
	private int dropLen, lastDropLen;
	private byte[] data;
	
	public SpringCup(byte[] data) {
		stamp = System.currentTimeMillis();
		synchronized (SpringCup.class) {
			if (stamp <= lastSTMP) {
				stamp = lastSTMP + 1;
			}
			lastSTMP = stamp;
		}
		this.dropLen = DigitalSpringSender.DROPLEN;
		lastDropLen = data.length % dropLen;
		maxSerial = data.length / dropLen;
		if (lastDropLen == 0) {
			maxSerial --;
			lastDropLen = dropLen;
		}
		this.data = data;
	}
	
	public int getMaxSerialNumber(){
		return maxSerial;
	}
	
	/**
	 * Packet structure: <br>
	 * <pre> [ stamp(8B) | totalLength(4B) | x * ( serial(4B) | length(4B) | data(length B) ) ]; </pre>
	 * @param serial - serial numbers that we want to send
	 * @param offset - the begin position in array serial
	 * @param len - length of serial number we peek
	 * @param buf - where to store the generated packet(start from position 0)
	 * @return length of data writing into buf
	 */
	public int makePacket(int[] serial,int offset, int len, byte[] buf){
		if (buf.length < len * (dropLen + 8) + 12) {
			throw new RuntimeException("Not enough buffer len. Require: " + (serial.length * (dropLen + 8) + 20) + ", but was: " + buf.length);
		}
		ArrayOutputStream aos = new ArrayOutputStream(buf);
		aos.write(stamp);
		aos.write(data.length);
		int lenx = 0;
		for(int i = 0; i < len; i++) {
			int sx = serial[offset + i];
			if (sx > maxSerial) {
				throw new RuntimeException("Serial out of range: " + sx);
			} else if (sx == maxSerial) {
				lenx = lastDropLen;
			} else {
				lenx = dropLen;
			}
			aos.write(sx);
			aos.write(lenx);
			aos.write(data, sx * dropLen, lenx);
			//System.out.println("make packet #" + sx + ": len " + lenx);
			//System.out.println(Arrays.toString(data));
			//System.out.println(Arrays.toString(buf));
		}
		//System.out.println("packet construct end");
		return aos.getPos();
	}
}

class ArrayOutputStream {
	private byte[] buf;
	private int pos = 0;
	public int getPos() {
		return pos;
	}
	public ArrayOutputStream(byte[] arr) {
		this.buf = arr;
	}
	public void write(int x){
		for(int i = 0; i < 4; i++) {
			buf[pos++] = (byte) (x & 0xff);
			x >>>= 8;
		}
	}
	public void write(long x){
		for(int i = 0; i < 8; i++) {
			buf[pos++] = (byte) (x & 0xff);
			x >>>= 8;
		}
	}
	public void write(byte[] a, int offset, int len) {
		for(int i = 0; i < len; i++) {
			buf[pos++] = a[offset + i];
		}
	}
	
}
