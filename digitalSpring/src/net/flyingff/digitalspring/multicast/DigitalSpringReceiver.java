package net.flyingff.digitalspring.multicast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import net.flyingff.digitalspring.lt.Decoder;
import net.flyingff.digitalspring.lt.LTCoderFactory;

public class DigitalSpringReceiver {
	private static final int RECVSIZE = 65536;
	private final ByteBuffer recv = ByteBuffer.allocate(RECVSIZE);
	private final ByteBuffer ACK = ByteBuffer.allocate(16); {
		try {
			byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			ByteBuffer tmp = ByteBuffer.allocate(8).put(mac);
			tmp.position(8).flip();
			ACK.position(8);
			ACK.putLong(tmp.getLong());
		} catch (Exception e) {e.printStackTrace();}
	}
	private DatagramChannel dc;
	private Selector sel;
	private ReceiveListener rl;
	private long currstamp = 0;
	private Decoder dec;
	private boolean committed = false;
	
	public void setReceiveListener(ReceiveListener rl) {this.rl = rl;}
	public ReceiveListener getReceiveListener() {return rl;}
	
	public DigitalSpringReceiver(int port) {
		try {
			dc = DatagramChannel.open()
					.bind(new InetSocketAddress(port))
					.setOption(StandardSocketOptions.SO_RCVBUF, recv.capacity() * 2);
			dc.configureBlocking(false);
			sel = Selector.open();
			dc.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			dec = new LTCoderFactory().setDropLen(1024).buildDecoder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) try {
					if(sel.select() <= 0) continue;
					for(SelectionKey skx : sel.selectedKeys()) {
						if (!skx.isReadable()) continue;
						recv.position(0);
						final SocketAddress srcaddr = dc.receive(recv);
						final int pktlen = recv.position();
						recv.position(0);
						final long stamp = recv.getLong();
						//System.out.println("recv pkt#" + stamp + " len: " + pktlen);
						if (pktlen == DigitalSpringSender.HELLODATA.length + 8) {
							boolean eq = true;
							byte[] data = recv.array();
							for(int i = 0; eq && i < DigitalSpringSender.HELLODATA.length; i++) {
								eq = data[i + 8] == DigitalSpringSender.HELLODATA[i];
							}
							if (eq) {
								sendack(srcaddr, stamp);
								continue;
							}
						}
						// read mode start
						//System.out.println("recved:" + Arrays.toString(recv.getData()));
						if (stamp != currstamp) {
							recv.position(8);
							int datalen = recv.getInt();
							System.out.println("New DATASEG, data within len:" + datalen);
							dec.init(datalen);
							currstamp = stamp;
							committed = false;
						}
						//System.out.println("recv: " + Arrays.toString(recv.array()));
						if (committed) {
							sendack(srcaddr, stamp);
						} else {
							//recv.position(12);
							//recvbuf.position(0);
							//recvbuf.put(recv);
							byte[] datarecv = new byte[pktlen - 12];
							System.arraycopy(recv.array(), 12, datarecv, 0, datarecv.length);
							if (dec.update(datarecv, 0, datarecv.length)){
								committed = true;
								sendack(srcaddr, stamp);
								if(rl != null) rl.onData(dec.finish());
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}).start();
	}
	public void sendack(SocketAddress addr, long tmstmp){
		while(true)	try {
			if (sel.select() <= 0) continue;
			for(SelectionKey kx : sel.selectedKeys()) {
				if (!kx.isWritable()) continue;
				ACK.position(0);
				ACK.putLong(tmstmp).position(0);
				dc.send(ACK, addr);
				//System.out.println("ack");
				return;
			}
			sel.selectedKeys().clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

