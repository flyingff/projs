package net.flyingff.douyu.barrage;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class DouyuTCPConnector {
	private static final boolean DEBUG = false;
	private final int port;
	private final String host;
	public DouyuTCPConnector(String host, int port) {
		this.port = port;
		this.host = host;
	}
	public DouyuTCPConnector(InetSocketAddress addr) {
		this.port = addr.getPort();
		this.host = addr.getHostString();
	}
	
	private boolean started = false, 
			stopFlag = false;
	public void start() {
		new Thread(this::run).start();
		stopFlag = false;
	}
	public void stop() {
		stopFlag = true;
	}
	public boolean isStarted() {
		return started;
	}
	
	protected Map<String, String> mapOf(Object... args) {
		Map<String, String> ret = new LinkedHashMap<>();
		for(int i = 0; i < args.length; i+= 2) {
			ret.put(args[i].toString(),
					args[i + 1] == null ? "" : args[i + 1].toString());
		}
		return ret;
	}
	protected abstract void handlePacket(Map<String, String> packet); 
	
	protected abstract void onConnected(Function<Map<String, String>, CompletableFuture<Void>> sender);
	
	private void run() {
		started = true;
		ByteBuffer sendBuf = ByteBuffer.allocate(65536), 
				recvBuf = ByteBuffer.allocate(65536);
		sendBuf.order(ByteOrder.LITTLE_ENDIAN);
		recvBuf.order(ByteOrder.LITTLE_ENDIAN);
		
		try(AsynchronousSocketChannel assc = connect().get()) {
			// read loop
			CompletableFuture.runAsync(()->{
				try {
					while(!stopFlag) {
						if(recvBuf.position() > 4) {
							recvBuf.flip();
							int packetLen = recvBuf.getInt(0);
							if(recvBuf.remaining() < packetLen + 4) {
								int toRead = packetLen + 4 - recvBuf.remaining();
								recvBuf.position(recvBuf.limit());
								recvBuf.limit(recvBuf.capacity());
								readFully(assc, recvBuf, toRead).get();
								recvBuf.flip();
							}
							handlePacket(deserializeMap(recvBuf));
							recvBuf.compact();
						} else {
							int cnt = read(assc, recvBuf).get();
							if(cnt <= 0) {
								// socket closed by remote
								assc.close();
								stopFlag = true;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			onConnected(map->{
				serialize(map, sendBuf);
				return writeFully(assc, sendBuf);
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			started = false;
		}
	}
	
	private String encode(String str) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);
			if(ch == '/') {
				sb.append("@S");
			} else if (ch == '@') {
				sb.append("@A");
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	private String decode(String str) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i), next;
			if(ch == '@' && i + 1 < len && (((next = str.charAt(i + 1)) == 'S') || next == 'A')) {
				if(next == 'S') {
					sb.append("/");
				} else {
					sb.append("@");
				}
				i++;
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	private void serialize(Map<String, String> map, ByteBuffer buffer) {
		buffer.clear();
		StringBuffer sb = new StringBuffer();
		map.forEach((k,v)->{
			sb.append(encode(k)).append("@=").append(encode(v)).append("/");
		});
		sb.append("\u0000");
		
		byte[] content = sb.toString().getBytes(Charset.forName("utf-8"));
		
		int dataLen = content.length + 8;
		
		buffer.putInt(dataLen);
		buffer.putInt(dataLen);
		buffer.putShort((short) 689);
		buffer.putShort((short) 0);
		buffer.put(content);
		buffer.flip();
		
		if(DEBUG) {
			System.out.println("Serialize: " + sb.toString());
			
			for(int i = 0; i < buffer.remaining(); i++) {
				System.out.print(String.format("%02X ", buffer.get(i) & 0xFF));
			}
			System.out.println();
		}
	}
	
	protected final Map<String, String> deserializeMap(ByteBuffer buffer) {
		int len = buffer.getInt();
		if(buffer.getInt() != len) {
			throw new AssertionError("Different Size");
		}
		if(buffer.getShort() != 690) {
			throw new AssertionError("Invalid response code");
		}
		buffer.getShort(); // not used
		
		byte[] sData = new byte[len - 8];
		buffer.get(sData);
		String str = new String(sData, 0, sData.length - 1);

		if(DEBUG) {
			System.out.println("Deserialize:" + str);
		}
		
		return deserializeMap(str);
	}
	
	protected final Map<String, String> deserializeMap(String str) {
		Map<String, String> ret = new HashMap<>();
		for(String place : str.split("/")) {
			String[] arr = place.split("@=");
			ret.put(decode(arr[0]), arr.length > 1 ? decode(arr[1]) : null);
		}
		return ret;
	}
	
	private CompletableFuture<AsynchronousSocketChannel> connect() throws Exception {
		CompletableFuture<AsynchronousSocketChannel> ret = new CompletableFuture<>();
		
		AsynchronousSocketChannel assc = AsynchronousSocketChannel.open();
		assc.connect(new InetSocketAddress(host, port),
				assc, new CompletionHandler<Void, AsynchronousSocketChannel>() {
					@Override
					public void completed(Void result, AsynchronousSocketChannel attachment) {
						ret.complete(attachment);
					}
					@Override
					public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
						ret.completeExceptionally(exc);
						try {
							attachment.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		return ret;
	}
	
	private CompletableFuture<Void> writeFully(AsynchronousSocketChannel assc, ByteBuffer buffer) {
		CompletableFuture<Void> ret = new CompletableFuture<>();
		try {
			while(buffer.hasRemaining()) {
				int bytes = write(assc, buffer).get();
				if(DEBUG) {
					System.out.println("Written " + bytes + " bytes.");
				}
			}
			ret.complete(null);
		} catch (Exception e) {
			ret.completeExceptionally(e);
		}
		return ret;
	}
	private CompletableFuture<Integer> write(AsynchronousSocketChannel assc, ByteBuffer buffer) {
		CompletableFuture<Integer> ret = new CompletableFuture<>();
		assc.write(buffer, null, new CompletionHandler<Integer, Void>() {
			@Override
			public void completed(Integer result, Void attachment) {
				ret.complete(result);
			}
			@Override
			public void failed(Throwable exc, Void attachment) {
				ret.completeExceptionally(exc);
			}
		});
		
		return ret;
	}
	
	private CompletableFuture<Void> readFully(AsynchronousSocketChannel assc, ByteBuffer buffer, int bytes) {
		CompletableFuture<Void> ret = new CompletableFuture<>();
		try {
			while(bytes > 0) {
				int r = read(assc, buffer).get();
				bytes -= r;
				if(r <= 0) {
					ret.completeExceptionally(new EOFException());
				}
			}
			ret.complete(null);
		} catch (Exception e) {
			ret.completeExceptionally(e);
		}
		return ret;
	}
	private CompletableFuture<Integer> read(AsynchronousSocketChannel assc, ByteBuffer buffer) {
		CompletableFuture<Integer> ret = new CompletableFuture<>();
		assc.read(buffer, null, new CompletionHandler<Integer, Void>() {
			@Override
			public void completed(Integer result, Void attachment) {
				ret.complete(result);
			}
			@Override
			public void failed(Throwable exc, Void attachment) {
				ret.completeExceptionally(exc);
			}
		});
		return ret;
	}
	/*
	private CompletableFuture<Map<String, String>> readPacket(AsynchronousSocketChannel assc, ByteBuffer buffer) {
		CompletableFuture<Map<String, String>> ret = new CompletableFuture<>();
		buffer.clear();
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		try {
			int readed = read(assc, buffer).get();
			if(readed < 0) throw new EOFException();
			
			int len = buffer.getInt(0);
			if(readed < len) {
				readFully(assc, buffer, len - readed).get();
			}
			buffer.flip();
			ret.complete(deserializeMap(buffer));
		} catch (Exception e) {
			ret.completeExceptionally(e);
		}
		return ret;
	}
	*/
	
}
