package net.flyingff.digitalspring.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Random;

public class DigitalSpringSender {
	public static final byte[] HELLODATA = "HELLO".getBytes();
	public static final int DROPLEN = 16, DROPEXP = 4;
	private static InetAddress ADDR_MULTICAST; static {
		try { ADDR_MULTICAST = InetAddress.getByName("255.255.255.255"); } catch(Exception e) { e.printStackTrace();}
	}
	private static final int PACKETLEN = 65536;
	private final DatagramPacket HELLOPACKET = new DatagramPacket(HELLODATA, HELLODATA.length);
	private final DatagramPacket ACKRECV = new DatagramPacket(new byte[255], 255);
	private final DatagramPacket DATAPACKET = new DatagramPacket(new byte[PACKETLEN], PACKETLEN);
	private HashSet<Long> ackedclient = new HashSet<>();
	
	private int dropLen = 4096; 
	private SpringCup cup;
	private DatagramSocket ds;
	private int clientCnt = 1;
	private boolean autoClientDetect = false;
	
	public void setAutoClientDetect(boolean autoClientDetect) {this.autoClientDetect = autoClientDetect;}
	public boolean isAutoClientDetect() {return autoClientDetect;}
	public void setDropLen(int dropLen) { if (dropLen < PACKETLEN) throw new RuntimeException("Drop Len mast be smaller than " + PACKETLEN); this.dropLen = dropLen;}
	public int getDropLen() {return dropLen;}
	public void setClientCnt(int clientCnt) {this.clientCnt = clientCnt;}
	public int getClientCnt() {return clientCnt;}
	public HashSet<Long> getLastACKClient(){
		return ackedclient;
	}
	public DigitalSpringSender(int port) {
		try {
			ds = new DatagramSocket();
			ds.setReceiveBufferSize(8192);
			ds.setBroadcast(true);
			ds.setSoTimeout(100);
			HELLOPACKET.setAddress(ADDR_MULTICAST);
			HELLOPACKET.setPort(port);
			DATAPACKET.setAddress(ADDR_MULTICAST);
			DATAPACKET.setPort(port);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public int detectClient(){
		clientCnt = 0;
		ackedclient.clear();
		for(int i = 0; i < 3; i++) {
			try {
				ds.send(HELLOPACKET);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				while(true) {
					ds.receive(ACKRECV);
					if (ACKRECV.getLength() == 6) {
						long n = 0;
						byte[] data = ACKRECV.getData();
						for(int k = 0; k < 6; k++) {
							n |= (((long)data[k] & 0xff) << ((5 - k) * 8));
						}
						if (!ackedclient.contains(n)) {
							ackedclient.add(n);
							clientCnt ++;
						}
					}
				}
			} catch(SocketTimeoutException e) {}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return clientCnt;
	}
	private static void swap(int[] arr, int x, int y) {
		if (x == y) return;
		arr[x] ^= arr[y];arr[y] ^= arr[x];arr[x] ^= arr[y];
	}
	
	public boolean send(byte[] data, final long timeout) {
		if (clientCnt < 1) return false;
		cup = new SpringCup(data);
		int[] serials = new int[cup.getMaxSerialNumber() + 1];
		for(int i = 0 ; i < serials.length; i++) {
			serials[i] = i;
			swap(serials, i, (int) (Math.random() * (i + 1)));
		}
		int groupSCnt = (int) Math.floor((double)(PACKETLEN - 20) / (dropLen + 8));
		int groupCnt = (int) Math.ceil((double)data.length / (groupSCnt * dropLen));
		long begintm = System.currentTimeMillis();
		for(int i = 0 ; i < groupCnt; i++) {
			int offset = groupSCnt * i;
			int len = serials.length - offset;
			if (len > groupSCnt) { len = groupSCnt;}
			DATAPACKET.setLength(cup.makePacket(serials, offset, len, DATAPACKET.getData()));
			try {
				ds.send(DATAPACKET);
				//System.out.println(DATAPACKET);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int i = 0;
		ackedclient.clear();
		while(System.currentTimeMillis() - begintm < timeout){
			if (i == 0) {
				for(int j = 0 ; j < serials.length; j++) {
					swap(serials, j, (int) (Math.random() * serials.length));
				}
			}
			try {
				while(true) {
					ds.receive(ACKRECV);
					if (ACKRECV.getLength() == 6) {
						long n = 0;
						byte[] drcv = ACKRECV.getData();
						for(int k = 0; k < 6; k++) {
							n |= (((long)drcv[k] & 0xff) << ((5 - k) * 8));
						}
						if (!ackedclient.contains(n)) {
							ackedclient.add(n);
						}
					}
				}
			} catch(SocketTimeoutException e) {}
			catch (Exception e) {
				e.printStackTrace();
			}
			if (ackedclient.size() >= clientCnt) {
				if (autoClientDetect) {
					clientCnt = ackedclient.size();
				}
				return true;
			}
			// if not enough ACKS, it will send packets again...
			int offset = groupSCnt * i;
			int len = serials.length - offset;
			if (len > groupSCnt) { len = groupSCnt;}
			DATAPACKET.setLength(cup.makePacket(serials, offset, len, DATAPACKET.getData()));
			try {
				ds.send(DATAPACKET);
				//System.out.println(DATAPACKET);
			} catch (IOException e) {
				e.printStackTrace();
			}
			i = (i + 1) % groupCnt;
		}
		return false;
	}
	public static void main(String[] args) {
		/*DigitalSpringSender ss = new DigitalSpringSender(10086);
		System.out.println(ss.getClientCnt());
		System.out.println(ss.send("this is a test string...".getBytes(), 123));*/
		Random r = new Random(2374), r2 = new Random(2374);
		for(int i = 0; i < 10; i++) {
			System.out.println(r.nextInt() == r2.nextInt());
		}
	}
}

