package net.flyingff.digitalspring.lt;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Decoder {
	private int dropLen, maxDropIndex, mergedCnt;
	private byte[] buf;
	private boolean[] flag;
	private LinkedList<Unit> unitList = new LinkedList<>();
	
	Decoder(int dropLen) {
		this.dropLen = dropLen;
	}
	
	public void clear(){
		buf = null;
		flag = null;
		unitList.clear();
		mergedCnt = 0;
	}
	
	public void init(int len){
		this.buf = new byte[len];
		maxDropIndex = (int) Math.ceil((double)len / dropLen) - 1;
		flag = new boolean[maxDropIndex + 1];
		unitList.clear();
		mergedCnt = 0;
		//System.err.println("flag len = " + flag.length + ", data len = " + len);
	}
	private int[] alistx = new int[4];
	public boolean update(byte[] data, int offset, int len){
		if (this.buf == null) throw new IllegalStateException("Not inited.");
		ByteBuffer buf = ByteBuffer.wrap(data, offset, len);
		// set buffer to read mode
		// get seed and data drops
		int seed = buf.getInt();
		//System.out.print("seed = " + seed + ", ");
		Random r = new Random(seed);
		while(buf.remaining() >= dropLen) {
			int num = Encoder.getNumber(r);	// 1-3 blocks selected
			int fakenum = 0;
outer:		for(int i = 0; i < num; i++) {
				int indexx = ((r.nextInt() & 0x7fffffff) % (maxDropIndex + 1));
				alistx[i - fakenum] = indexx;
				for(int j = 0; j < i - fakenum; j++) {
					if (alistx[j] == indexx){
						fakenum++;
						//System.err.println(indexx + "..." + fakenum);
						continue outer;
					}
				}
			}
			//System.err.println("#" + buf.position() / dropLen + ":" + Arrays.toString(alistx) + ", " + (num - fakenum));
			// create new Unit object
			Unit ux = new Unit(data, buf.position(), Arrays.copyOf(alistx, num - fakenum));
			//System.err.println(ux);
			put(ux);
			//System.out.println(ux);
			buf.position(buf.position() + dropLen);
		}
		//System.err.println("decoder serial:" + test);
		// after adding all drops, just try to merge them.
		tryMerge();
		//buf.position(offset);
		//System.out.println(buf.getInt());
		//System.out.println("merge cnt = " + mergedCnt + ", all = "+ flag.length);
		return mergedCnt >= flag.length;
	}
	public byte[] finish(){
		try {
			return buf;
		} finally{
			clear();
		}
	}
	private void put(Unit ux){
		if (!unitList.contains(ux))
			unitList.offer(ux);
		//System.out.println(unitList);
	}
	public void tryMerge(){
		//System.out.println(unitList);
		for(Iterator<Unit> it = unitList.iterator();it.hasNext();) {
			Unit ux = it.next();
			for(int i = 0; i < ux.packets.length;i++) {
				if (flag[ux.packets[i]] == true && ux.got[i] == false) {
					ux.got[i] = true;
					ux.remainings --;
				}
			}
			if (ux.remainings == 1) {
				int dest = 0, destindex;
				for(; dest < ux.got.length; dest++) {
					if (!ux.got[dest]) break;
				}
				destindex = ux.packets[dest];
				if (!flag[destindex]){
					flag[destindex] = true;
					copy(buf, destindex * dropLen, ux.data, ux.offset);
					mergedCnt++;
					//System.out.println("merge cnt = " + mergedCnt + ": " + destindex);
					//System.out.println(Arrays.toString(ux.packets));
					//System.out.println("copy from " + (ux.offset - 4) / dropLen + " to " + destindex);
					for(int i = 0; i < ux.packets.length; i++) {
						if (i == dest) continue;
						exclusive(buf, destindex * dropLen, buf, ux.packets[i] * dropLen);
						//System.out.println("mrge from " + ux.packets[i]  + " to " + destindex);
					}
					//System.out.println();
					//System.out.println(Arrays.toString(flag));
				}
				it.remove();
			} else if (ux.remainings < 1) {
				it.remove();
			}
		}
	}
	private void exclusive(byte[] dest, int offdest, byte[] source, int offsrc){
		for(int i = 0; i < dropLen && i + offsrc < source.length && i + offdest < dest.length; i++) {
			dest[i + offdest] ^= source[i + offsrc];
		}
	}
	private void copy(byte[] dest, int offdest, byte[] source, int offsrc){
		//System.err.println(offsrc);
		for(int i = 0; i < dropLen && i + offsrc < source.length && i + offdest < dest.length; i++) {
			dest[i + offdest] = source[i + offsrc];
			//System.out.print(() + ", ");
		}
		//System.out.println();
	}
	
	class Unit{
		int[] packets;
		boolean[] got;
		int remainings;
		byte[] data;
		int offset;
		public Unit(byte[] data, int offset, int... packets){
			this.data = data;
			this.offset = offset;
			this.packets = packets;
			remainings = packets.length;
			got = new boolean[packets.length];
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Unit) {
				return Arrays.equals(((Unit) obj).packets, packets);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return Arrays.hashCode(packets);
		}
		@Override
		public String toString() {
			return "[Ux@" + offset + " " + Arrays.toString(packets) + "]";
		}
	}
}
