package net.flyingff.snat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.IntConsumer;

import net.flyingff.snat.NATEntry.NATStatus;
import net.flyingff.snat.ui.TheLogger;

public class Proxy {
	private static final InetAddress LOCALHOST; static {
		try {
			/*
			InetAddress ipv4Addr = Arrays.stream(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()))
				.filter(x->x instanceof Inet4Address)
				.findFirst().get();
				*/
			LOCALHOST = InetAddress.getLocalHost();
		} catch (Exception e) {
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
			
			TheLogger.log("Ready for accept connection at TCP[" + LOCALHOST + ":" + entry.getExternalPort() + "]");
		} catch (IOException e) {
			entry.setStatus(NATStatus.ERROR);
			TheLogger.err("Cannot listen to ", LOCALHOST + ":" + entry.getExternalPort(), ", caused by", e.toString());
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
				if(!entry.testIP(addrString)) {
					sockExt.close();
					return CompletableFuture.completedFuture(SockPairInfo.failed(sockExt, "IP Invalid: '" + addrString + "' does not match '" + entry.getIpRegExp() + "'"));
				}
				sockLocal = AsynchronousSocketChannel.open();
				CompletableFuture<SockPairInfo> future = new CompletableFuture<>();
				SockPairInfo pair = new SockPairInfo(sockExt, sockLocal);
				
				AsynchronousSocketChannel sockLocal_ = sockLocal;
				sockLocal.connect(localAddress, future, new CompletionHandler<Void, CompletableFuture<SockPairInfo>>() {
					@Override
					public void completed(Void result, CompletableFuture<SockPairInfo> attachment) {
						attachment.complete(pair);
					}
					@Override
					public void failed(Throwable exc, CompletableFuture<SockPairInfo> attachment) {
						attachment.complete(SockPairInfo.failed(sockExt, sockLocal_, "Faild to connect local port:" + exc.toString()));
					}
				});
				// timeout
				CompletableFuture.runAsync(()->{
					try { Thread.sleep(1000); } catch (Exception e) { }
					if(!future.isDone() && 
							future.complete(SockPairInfo.failed(sockExt, sockLocal_,
									"Connection timeout"))) {
						TheLogger.err("Time out...");
					}
				});
				return future;
			} catch (IOException e) {
				e.printStackTrace();
				return CompletableFuture.completedFuture(SockPairInfo.failed(sockExt, sockLocal, "Exception occured:" + e.toString()));
			}
		}).thenAccept(x->{
			if(x == null) return;
			if(x.isLocalFailed()) {
				TheLogger.err("Connection failed:" + x.getLocalFailed());
				x.closeAll();
			} else {
				try {
					TheLogger.log("Bridge from", x.getSockExternal().getRemoteAddress(), "to", x.getSockLocal().getRemoteAddress(), "established.");
				} catch (IOException e) { e.printStackTrace(); }
				x.pipe(()->{
					TheLogger.log("One connection pair closed.");
					entry.decConnections();
				}, flow->{
					entry.acuumulateDataTransfered(flow);
				});
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
	private final String isLocalFailed;
	private boolean isClosed = false;
	private Runnable closeCallback;
	
	
	public SockPairInfo(AsynchronousSocketChannel sockExternal, 
			AsynchronousSocketChannel sockLocal) {
		this(sockExternal, sockLocal, null);
	}
	private SockPairInfo(AsynchronousSocketChannel sockExternal, 
			AsynchronousSocketChannel sockLocal, String isLocalFailed) {
		this.sockExternal = sockExternal;
		this.sockLocal = sockLocal;
		this.isLocalFailed = isLocalFailed;
	}
	public void pipe(Runnable closeCallback, IntConsumer flowListener) {
		if(isClosed || this.closeCallback != null) {
			throw new IllegalStateException();
		}
		this.closeCallback = Objects.requireNonNull(closeCallback);
		Objects.requireNonNull(flowListener);
		
		ByteBuffer bufferExtToLocal = ByteBuffer.allocate(65536),
				bufferLocalToExt = ByteBuffer.allocate(65536);
		new SockPipe(sockLocal, sockExternal, bufferLocalToExt, this::onClose, flowListener).read();
		new SockPipe(sockExternal, sockLocal, bufferExtToLocal, this::onClose, flowListener).read();
	}
	
	private void onClose() {
		if(!isClosed) {
			isClosed = true;
			closeCallback.run();
			closeCallback = null;
		}
	}

	public AsynchronousSocketChannel getSockExternal() {
		return sockExternal;
	}

	public AsynchronousSocketChannel getSockLocal() {
		return sockLocal;
	}

	public boolean isLocalFailed() {
		return isLocalFailed != null;
	}
	public String getLocalFailed() {
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
			AsynchronousSocketChannel sockLocal, String reason) {
		return new SockPairInfo(sockExternal, sockLocal, reason);
	}
	public static final SockPairInfo failed(AsynchronousSocketChannel sockExternal, String reason) {
		return new SockPairInfo(sockExternal, null, reason);
	}
	
}
class SockPipe {
	private final AsynchronousSocketChannel readEnd, writeEnd;
	private final ByteBuffer buffer;
	private final Runnable closeCallback;
	private final IntConsumer flowListener;
	
	public SockPipe(AsynchronousSocketChannel readEnd, 
			AsynchronousSocketChannel writeEnd, 
			ByteBuffer buffer, Runnable closeCallback,
			IntConsumer flowListener) {
		this.readEnd = readEnd;
		this.writeEnd = writeEnd;
		this.buffer = buffer;
		this.closeCallback = Objects.requireNonNull(closeCallback);
		this.flowListener = Objects.requireNonNull(flowListener);
	}

	private final CompletionHandler<Integer, Void> readHander = new CompletionHandler<Integer, Void>() {
		@Override
		public void completed(Integer result, Void attachment) {
			if(result == -1) {
				close();
			} else {
				flowListener.accept(result);
				buffer.flip();
				write();
			}
		}
		@Override
		public void failed(Throwable exc, Void attachment) {
			if(!(exc instanceof AsynchronousCloseException)) {
				exc.printStackTrace();
			}
			close();
		}
	}, writeHandler = new CompletionHandler<Integer, Void>() {
		@Override
		public void completed(Integer result, Void attachment) {
			flowListener.accept(result);
			buffer.compact();
			read();
		}
		@Override
		public void failed(Throwable exc, Void attachment) {
			if(!(exc instanceof AsynchronousCloseException)) {
				exc.printStackTrace();
			}
			close();
		}
	} ;
	public void read() {
		try {
			readEnd.read(buffer, null, readHander);
		} catch (Exception e) {
			if(!(e instanceof ClosedChannelException)) {
				TheLogger.err(e.toString());
			}
			close();
		}
	}
	public void write() {
		try {
			writeEnd.write(buffer, null, writeHandler);
		} catch (Exception e) {
			if(!(e instanceof ClosedChannelException)) {
				TheLogger.err(e.toString());
			}
			close();
		}
	}
	
	public void close() {
		try {
			readEnd.close();
			writeEnd.close();
		} catch (IOException e) { e.printStackTrace(); }
		if(null != closeCallback) {
			closeCallback.run();
		}
	}
}
