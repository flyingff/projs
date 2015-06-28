package net.flyingff.digitalspring.multicast;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;

import net.flyingff.digitalspring.lt.Encoder;
import net.flyingff.digitalspring.lt.LTCoderFactory;

public class DigitalSpringSender {
	public static final byte[] HELLODATA = "HELLO".getBytes();
	public static final int DROPLEN = 16, DROPEXP = 4;
	private static InetAddress ADDR_MULTICAST; static {
		try { ADDR_MULTICAST = InetAddress.getByName("255.255.255.255"); } catch(Exception e) { e.printStackTrace();}
	}
	private static final int PACKETLEN = 65536;
	private final ByteBuffer HELLOPACKET = ByteBuffer.allocate(HELLODATA.length + 8);
	private final ByteBuffer ACKRECV = ByteBuffer.allocate(256);
	private final ByteBuffer DATAPACKET = ByteBuffer.allocate(PACKETLEN);
	private final InetSocketAddress baddr;
	private HashSet<Long> ackedclient = new HashSet<>();
	
	private int dropLen = 4096; 
	private Encoder ec;
	private DatagramChannel ds;
	private Selector sel;
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
			ds = DatagramChannel.open(StandardProtocolFamily.INET)
//		         .setOption(StandardSocketOptions.SO_REUSEADDR, true)
		         .bind(null)
		         .setOption(StandardSocketOptions.SO_BROADCAST, true)
		         .setOption(StandardSocketOptions.IP_MULTICAST_IF, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
			ds.configureBlocking(false);
			
			baddr = new InetSocketAddress(ADDR_MULTICAST, port);
			sel = Selector.open();
			ds.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ec = new LTCoderFactory().setDropLen(1024).buildEnocder();
	}
	public int detectClient(){
		ackedclient.clear();
		HELLOPACKET.position(0);
		long stmp = System.currentTimeMillis();
		HELLOPACKET.putLong(stmp).put(HELLODATA);
		for(int i = 0; i < 3; i++) {
			try {
				HELLOPACKET.position(0);
				ds.send(HELLOPACKET, baddr);
				Thread.sleep(10);
				tryRecvAck(stmp);
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		clientCnt = ackedclient.size();
		return clientCnt;
	}
	private void tryRecvAck(long tmstmp){
		try {
			if(sel.selectNow() > 0) {
				for (SelectionKey kx : sel.selectedKeys()) {
					if (kx.isReadable()) {
						ACKRECV.position(0);
						((DatagramChannel) kx.channel()).receive(ACKRECV);
						if (ACKRECV.position() == 16) {
							ACKRECV.position(0);
							if (ACKRECV.getLong() != tmstmp) continue;
							long n = ACKRECV.getLong();
							if (!ackedclient.contains(n)) {
								ackedclient.add(n);
								//clientCnt ++;
							}
						}
					}
				}
				sel.selectedKeys().clear();
			}
		} catch(Exception e) {e.printStackTrace();}
	}
	
	public boolean send(byte[] data, final long timeout) {
		if (clientCnt < 1) return false;
		long begintm = System.currentTimeMillis();
		ackedclient.clear();
		ec.setData(data);
		DATAPACKET.position(0);
		DATAPACKET.putLong(begintm).putInt(data.length);
		while(System.currentTimeMillis() - begintm < timeout){
			DATAPACKET.limit(ec.write(DATAPACKET.array(), 12, PACKETLEN - 12) + 12).position(0);
			//System.out.println("SEND DATASEG, len: " + DATAPACKET.limit());
			//System.out.println(Arrays.toString(DATAPACKET.array()));
			try {
				while(sel.select() <= 0) {Thread.yield();}
				for(SelectionKey skx : sel.selectedKeys()) {
					if (skx.isWritable()){
						ds.send(DATAPACKET, baddr);
						//System.out.println("send: " + Arrays.toString(DATAPACKET.array()));
						break;
					}
				}
				sel.selectedKeys().clear();
				Thread.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tryRecvAck(begintm);
			if (ackedclient.size() >= clientCnt) {
				if (autoClientDetect) {
					clientCnt = ackedclient.size();
				}
				return true;
			}
			//else {
			//	System.out.println("not enough, curr: " + ackedclient.size() + ", expect: " + clientCnt);
			//}
		}
		// timeout...
		return false;
	}
}

