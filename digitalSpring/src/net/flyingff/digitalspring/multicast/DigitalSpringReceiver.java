package net.flyingff.digitalspring.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import net.flyingff.digitalspring.util.ArrayInputStream;

public class DigitalSpringReceiver {
	private final DatagramPacket recv = new DatagramPacket(new byte[65536], 65536);
	private final DatagramPacket ACK = new DatagramPacket(new byte[6], 6); {
		try {
			byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			byte[] dest = ACK.getData();
			for(int i = 0; i < mac.length && i < dest.length; i++) {
				dest[i] = mac[i];
			}
		} catch (Exception e) {e.printStackTrace();}
	}
	private DatagramSocket ds;
	private ReceiveListener rl;
	private long currstamp;
	private byte[] currdata = null;
	private int bottlecnt, droplen;
	private boolean bottle[];
	private boolean committed = false;
	
	public void setReceiveListener(ReceiveListener rl) {this.rl = rl;}
	public ReceiveListener getReceiveListener() {return rl;}
	
	public DigitalSpringReceiver(int port) {
		try {
			ds = new DatagramSocket(port);
			ds.setReceiveBufferSize(65536 * 2);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						ds.receive(recv);
						if (recv.getLength() == DigitalSpringSender.HELLODATA.length ) {
							for(int i = 0; i < DigitalSpringSender.HELLODATA.length; i++) {
								if (recv.getData()[i] != DigitalSpringSender.HELLODATA[i])
									continue;
							}
							sendack(recv);
						} else {
							ArrayInputStream ais = new ArrayInputStream(recv.getData(), recv.getLength());
							//System.out.println("recved:" + Arrays.toString(recv.getData()));
							long stamp = ais.readl();
							if (currdata == null || stamp != currstamp) {
								int maxserial = ais.readi(), datalen = ais.readi();
								droplen = ais.readi();
								currstamp = stamp;
								bottlecnt = 0;
								bottle = new boolean[maxserial + 1];
								currdata = new byte[datalen];
								committed = false;
								
								//System.out.println("new packet: stamp[" + stamp + "], maxserial[" + maxserial + "], datalen[" + datalen + "], droplen[" + droplen +"]");
							} else {
								ais.skip(12);
							}
							if (committed) {
								if (Math.random() > 0.5)
									sendack(recv);
							} else {
								while(ais.hasNext()) {
									int sx = ais.readi();
									int lenx = ais.readi();
									//System.out.println("packet #" + sx + ": len " + lenx);
									if (!bottle[sx]) {
										ais.read(currdata, sx * droplen, lenx);
										bottle[sx] = true;
										bottlecnt ++;
										//System.out.println(Arrays.toString(recv.getData()));
									}
								}
								if (bottlecnt >= bottle.length) {
									sendack(recv);
									if(!committed){
										rl.onData(currdata);
										committed = true;
									}
								}
							}
						}
					} catch (Exception e) {
						System.err.println(e.toString());
					}
				}
			}
		}).start();
	}
	public void sendack(DatagramPacket rcv){
		try {
			ACK.setAddress(rcv.getAddress());
			ACK.setPort(rcv.getPort());
			ds.send(ACK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

