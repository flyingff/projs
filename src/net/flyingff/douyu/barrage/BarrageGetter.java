package net.flyingff.douyu.barrage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BarrageGetter {
	private final int id;
	public BarrageGetter(int id) {
		this.id = id;
		
	}
	
	private boolean started = false, stopFlag = false;
	public void start(Consumer<String> accepter) {
		new Thread(this::run).start();
		stopFlag = false;
	}
	public void stop() {
		stopFlag = true;
	}
	public boolean isStarted() {
		return started;
	}
	
	private void run() {
		started = true;
		ByteBuffer sendBuf = ByteBuffer.allocate(65536), 
				recvBuf = ByteBuffer.allocate(65536);
		try(AsynchronousSocketChannel assc = connect().get()) {
			loginreq(assc, id, sendBuf).get();
			
			recvBuf.clear();
			read(assc, recvBuf).get();
			recvBuf.flip();
			Map<String, String> ret = deserializeMap(recvBuf);
			
			System.out.println(ret);
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
		
		byte[] content = sb.toString().getBytes(Charset.forName("utf-8"));
		
		int dataLen = content.length + 13;
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(dataLen);
		buffer.putInt(dataLen);
		buffer.putShort((short) 689);
		buffer.putShort((short) 0);
		buffer.put(content);
		buffer.put((byte) 0);
		buffer.flip();
		System.out.println(buffer.remaining() + " vs " + dataLen);
	}
	
	private void serialize(List<String> data, ByteBuffer buffer) {
		buffer.clear();
		StringBuffer sb = new StringBuffer();
		data.forEach((k)->{
			sb.append(encode(k)).append("/");
		});
		
		byte[] content = sb.toString().getBytes(Charset.forName("utf-8"));
		
		int dataLen = content.length + 13;
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(dataLen);
		buffer.putInt(dataLen);
		buffer.putShort((short) 689);
		buffer.putShort((short) 0);
		buffer.put(content);
		buffer.put((byte) 0);
	}
	
	private Map<String, String> deserializeMap(ByteBuffer buffer) {
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int len = buffer.getInt();
		if(buffer.getInt() != len) {
			throw new AssertionError("Different Size");
		}
		if(buffer.getShort() != 690) {
			throw new AssertionError("Invalid response code");
		}
		buffer.getShort();
		
		String str = new String(buffer.array(), buffer.position(), buffer.remaining() - 1);
		
		Map<String, String> ret = new HashMap<>();
		for(String place : str.split("/")) {
			String[] arr = place.split("@=");
			ret.put(decode(arr[0]), decode(arr[1]));
		}
		return ret;
	}
	
	private CompletableFuture<AsynchronousSocketChannel> connect() throws Exception {
		CompletableFuture<AsynchronousSocketChannel> ret = new CompletableFuture<>();
		
		AsynchronousSocketChannel assc = AsynchronousSocketChannel.open();
		assc.connect(new InetSocketAddress("openbarrage.douyutv.com", 8601),
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
				System.out.println("Write " + bytes + " bytes.");
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
				bytes -= read(assc, buffer).get();
			}
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
	
	protected CompletableFuture<Void> loginreq(AsynchronousSocketChannel assc, int roomid, ByteBuffer buffer) {
		Map<String, String> msg = new HashMap<>();
		msg.put("type", "loginreq");
		msg.put("roomid", Integer.toString(roomid));
		serialize(msg, buffer);
		
		return writeFully(assc, buffer);
	}
	
}
