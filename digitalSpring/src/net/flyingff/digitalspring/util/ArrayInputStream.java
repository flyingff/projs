package net.flyingff.digitalspring.util;

public class ArrayInputStream {
	private byte[] buf;
	private int pos = 0;
	private int len;
	public ArrayInputStream(byte[] arr, int len) {
		this.buf = arr;
		this.len = len;
	}
	public int peeki(int offset){
		pos += offset;
		int result = readi();
		pos -= 4 + offset;
		return result;
	}
	public int readi(){
		int ret = 0;
		for(int i = 0; i < 4; i++) {
			ret |= (((int)buf[pos++] & 0xff) << (i * 8));
		}
		return ret;
	}
	public long readl(){
		long ret = 0;
		for(int i = 0; i < 8; i++) {
			ret |= (((long)buf[pos++] & 0xff) << (i * 8));
		}
		return ret;
	}
	public void read(byte[] a, int offset, int len) {
		for(int i = 0; i < len; i++) {
			a[offset + i] = buf[pos++];
		}
	}
	public void skip(int n) {
		pos += n;
	}
	public boolean hasNext(){
		return pos < len;
	}
}
