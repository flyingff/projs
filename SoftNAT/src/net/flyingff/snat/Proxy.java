package net.flyingff.snat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntSupplier;

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
	private final InetSocketAddress localAddress;
	private AsynchronousServerSocketChannel assc;
	private Map<SocketChannel, SockPairInfo> channelPairs = new HashMap<>();
	
	public Proxy(NATEntry entry) {
		this.entry = entry;
		localAddress = new InetSocketAddress(LOCALHOST, entry.getLocalPort());
	}
	
	public void start() throws IOException {
		if(assc != null) throw new IllegalStateException();
		
		assc = AsynchronousServerSocketChannel.open();
		assc.bind(new InetSocketAddress(entry.getExternalPort()));
		entry.setStatus(NATStatus.STARTED);
		
		acceptNew();
	}
	private void acceptNew() {
		CompletableFuture.supplyAsync(() -> {
			while(true) try {
				return assc.accept().get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).thenCompose((Function<AsynchronousSocketChannel, CompletableFuture<SockPairInfo>>)sockExt->{
			acceptNew();
			InetSocketAddress addr;
			AsynchronousSocketChannel sockLocal = null;
			try {
				addr = (InetSocketAddress) sockExt.getRemoteAddress();
				String addrString = addr.getAddress().getHostAddress();
				if(!entry.getIpRegExp().matches(addrString)) {
					sockExt.close();
					return CompletableFuture.completedFuture(SockPairInfo.failed(sockExt));
				}
				sockLocal = AsynchronousSocketChannel.open();
				CompletableFuture<SockPairInfo> future = new CompletableFuture<>();
				SockPairInfo pair = new SockPairInfo(sockExt, sockLocal, false);
				
				AsynchronousSocketChannel sockLocal_ = sockLocal;
				sockLocal.connect(localAddress, future, new CompletionHandler<Void, CompletableFuture<SockPairInfo>>() {
					@Override
					public void completed(Void result, CompletableFuture<SockPairInfo> attachment) {
						attachment.complete(pair);
					}
					@Override
					public void failed(Throwable exc, CompletableFuture<SockPairInfo> attachment) {
						attachment.complete(SockPairInfo.failed(sockExt, sockLocal_));
					}
				});
				// timeout
				CompletableFuture.runAsync(()->{
					try { Thread.sleep(1000); } catch (Exception e) { }
					future.complete(SockPairInfo.failed(sockExt, sockLocal_));
				});
				return future;
			} catch (IOException e) {
				e.printStackTrace();
				return CompletableFuture.completedFuture(SockPairInfo.failed(sockExt, sockLocal));
			}
		}).thenAccept(x->{
			if(x == null) throw new AssertionError();
			if(x.isLocalFailed()) {
				x.closeAll();
			} else {
				x.pipe();
			}
		});
		
		/*.thenAccept(sockLocal->{
			if(sockLocal != null) {
				
			}
			SockPairInfo info = new SockPairInfo();
			info.sockExternal = sockExt;
			info.sockLocal = sockLocal;
			channelPairs.put(sockExt, info);
			channelPairs.put(sockLocal, info);
		});*/
	}
	
	public void onAccept() throws IOException{
		// check is IP valid
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
		assc.close();
		for(SocketChannel ch : channelPairs.keySet()) {
			ch.close();
		}
		channelPairs.clear();
		assc = null;
		entry.setStatus(NATStatus.STOPPED);
	}
	
}

class SockPairInfo {
	private final AsynchronousSocketChannel sockExternal, sockLocal;
	private final long localConnectTime = System.currentTimeMillis();
	private ByteBuffer bufferExtToLocal,
			bufferLocalToExt;
	private final boolean isLocalFailed;
	
	public SockPairInfo(AsynchronousSocketChannel sockExternal, 
			AsynchronousSocketChannel sockLocal, boolean isLocalFailed) {
		this.sockExternal = sockExternal;
		this.sockLocal = sockLocal;
		this.isLocalFailed = isLocalFailed;
	}

	public void pipe() {
		sockExternal.read(bufferExtToLocal, bufferExtToLocal, new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				
			}
			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				
			}
		});
	}

	public AsynchronousSocketChannel getSockExternal() {
		return sockExternal;
	}

	public AsynchronousSocketChannel getSockLocal() {
		return sockLocal;
	}

	public long getLocalConnectTime() {
		return localConnectTime;
	}

	public ByteBuffer getBufferExtToLocal() {
		return bufferExtToLocal;
	}

	public ByteBuffer getBufferLocalToExt() {
		return bufferLocalToExt;
	}

	public boolean isLocalFailed() {
		return isLocalFailed;
	}
	public void closeAll() {
		Optional.ofNullable(sockExternal).ifPresent(it->{
			try { it.close(); } catch (IOException e) { }
		});
		Optional.ofNullable(sockLocal).ifPresent(it->{
			try { it.close(); } catch (IOException e) { }
		});
	}
	
	public static final SockPairInfo failed(AsynchronousSocketChannel sockExternal,
			AsynchronousSocketChannel sockLocal) {
		return new SockPairInfo(sockExternal, sockLocal, true);
	}
	public static final SockPairInfo failed(AsynchronousSocketChannel sockExternal) {
		return new SockPairInfo(sockExternal, null, true);
	}
	
}
