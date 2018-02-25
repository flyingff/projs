package net.flyingff.java.parser;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class Main {
	public static void main(String[] args) throws Exception{
		// ClassParser.parseFileTree("D:\\git\\projs\\dither\\bin\\net\\flyingff\\img\\dither").forEach(System.out::println);
		
		/*ByteBuffer buf = ByteBuffer.allocate(4096);
		buf.putLong(System.currentTimeMillis());
		buf.putInt(0);
		buf.putInt(2);
		buf.put("神奇的小屋".getBytes());
		
		DatagramPacket dp = new DatagramPacket(buf.array(), buf.position());
		dp.setPort(50203);
		dp.setAddress(InetAddress.getByName("255.255.255.255"));
		
		try (DatagramSocket ds = new DatagramSocket(new InetSocketAddress("0.0.0.0", 0))) {
			ds.setBroadcast(true);
			while(true) {
				ds.send(dp);
				Thread.sleep(1000);
				System.out.println("...");
			}
		}
		*/
	}
}
