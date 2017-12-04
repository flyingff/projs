package net.flyingff.snat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import net.flyingff.snat.NATEntry.NATStatus;

public class Proxy {
	private static final InetAddress LOCALHOST; static {
		try {
			LOCALHOST = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException("Cannot resolve localhost!");
		}
	}
	private final NATEntry entry;
	private ServerSocketChannel ssc;
	private Selector selector;
	private Map<SocketChannel, SockPairInfo> channelPairs = new HashMap<>();
	
	public Proxy(NATEntry entry, Selector selector) {
		this.entry = entry;
		this.selector = null;
	}
	
	public void start() throws IOException {
		if(ssc != null) throw new IllegalStateException();
		
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(entry.getExternalPort()));
		
		ssc.register(selector, SelectionKey.OP_ACCEPT, this);
		entry.setStatus(NATStatus.STARTED);
	}
	
	public void onAccept(SocketChannel sockExt) throws IOException{
		// check is IP valid
		InetSocketAddress addr = (InetSocketAddress) sockExt.getRemoteAddress();
		String addrString = addr.getAddress().getHostAddress();
		if(!entry.getIpRegExp().matches(addrString)) {
			sockExt.close();
			return;
		}
		
		SocketChannel sockLocal = SocketChannel.open(new InetSocketAddress(LOCALHOST, entry.getLocalPort()));
		sockLocal.configureBlocking(false);
		
		sockLocal.register(selector, SelectionKey.OP_CONNECT, this);
		SockPairInfo info = new SockPairInfo();
		info.sockExternal = sockExt;
		info.sockLocal = sockLocal;
		channelPairs.put(sockExt, info);
		channelPairs.put(sockLocal, info);
	}
	public void onConnect(SocketChannel sockLocal) {
		// local connection finished connection
		
	}
	public void onReceieve(SocketChannel sock) {
		// local or external socket data sent
	}

	public void onClose(SocketChannel ch) {
		// local or external socket close
		// remains to handle data that stored in buffer
	}
	
	public void onTick() {
		// check for time-out connection?
	}
	
	public void stop() throws IOException{
		ssc.close();
		for(SocketChannel ch : channelPairs.keySet()) {
			ch.close();
		}
		channelPairs.clear();
		ssc = null;
		entry.setStatus(NATStatus.STOPPED);
	}
}

class SockPairInfo {
	SocketChannel sockExternal, sockLocal;
	final long localConnectTime = System.currentTimeMillis();
	ByteBuffer bufferExtToLocal = ByteBuffer.allocate(65536),
			bufferLocalToExt = ByteBuffer.allocate(65536);
	
}
