package net.flyingff.snat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
	private Set<SockPairInfo> channelPairs = new HashSet<>();
	
	public Proxy(NATEntry entry) {
		this.entry = entry;
		localAddress = new InetSocketAddress(LOCALHOST, entry.getLocalPort());
	}
	public NATEntry getEntry() {
		return entry;
	}
	
	public void start() {
		if(assc != null) throw new IllegalStateException();
		
		try {
			assc = AsynchronousServerSocketChannel.open();
			assc.bind(new InetSocketAddress(entry.getExternalPort()));
			entry.setStatus(NATStatus.STARTED);
			acceptNew();
			entry.resetConnections();
		} catch (IOException e) {
			entry.setStatus(NATStatus.ERROR);
			e.printStackTrace();
		}
		
	}
	private void acceptNew() {
		CompletableFuture.supplyAsync(() -> {
			while(assc != null) try {
				return assc.accept().get();
			} catch (ExecutionException e) {
				if(!(e.getCause() instanceof AsynchronousCloseException)) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) { }
			return null;
		}).thenCompose((Function<AsynchronousSocketChannel, CompletableFuture<SockPairInfo>>)sockExt->{
			if(sockExt == null) {
				return CompletableFuture.completedFuture(null);
			}
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
					System.err.println("Time out...");
				});
				return future;
			} catch (IOException e) {
				e.printStackTrace();
				return CompletableFuture.completedFuture(SockPairInfo.failed(sockExt, sockLocal));
			}
		}).thenAccept(x->{
			if(x == null) return;
			if(x.isLocalFailed()) {
				x.closeAll();
			} else {
				x.pipe();
				entry.incConnections();
			}
		});
	}
	
	
	public void stop() {
		try {
			assc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(SockPairInfo ch : channelPairs) {
			ch.closeAll();
		}
		channelPairs.clear();
		assc = null;
		entry.setStatus(NATStatus.STOPPED);
		entry.resetConnections();
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
