package net.flyingff.douyu.barrage;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import net.flyingff.douyu.barrage.util.MD5Util;

public class BarrageAddressGetter extends DouyuTCPConnector {
	private final String roomId;
	private static final WeakHashMap<Thread, String> ROOM_ID_MAP = new WeakHashMap<>();
	private static final InetSocketAddress getAddress(String roomId) {
		try {
			org.jsoup.nodes.Document dom = Jsoup.parse(new URL("http://www.douyu.com/" + roomId), 10000);
			for(Element it : dom.select("script")) {
				String content = it.html();
				if(content.contains("var $ROOM")) {
					ScriptEngineManager manager = new ScriptEngineManager();
					ScriptEngine engine = manager.getEngineByName("javascript");
					
					engine.eval(content);
					engine.eval("$SERVER = eval(decodeURIComponent($ROOM.args.server_config))[0]");
					
					String ip = (String) engine.eval("$SERVER['ip']"),
							port = engine.eval("$SERVER['port']").toString();
					
					ROOM_ID_MAP.put(Thread.currentThread(), engine.eval("$ROOM.room_id").toString());
					return new InetSocketAddress(ip, Integer.parseInt(port));
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private BarrageAddressGetter(String roomId) {
		super(getAddress(roomId));
		this.roomId = ROOM_ID_MAP.get(Thread.currentThread());
	}
	public static CompletableFuture<BarrageLoginInfo> resolve(String roomId) {
		return new BarrageAddressGetter(roomId).get();
	}

	private BarrageLoginInfo info = new BarrageLoginInfo();
	private CompletableFuture<BarrageLoginInfo> future;
	@Override
	protected void handlePacket(Map<String, String> packet) {
		String type = packet.get("type");
		switch (type) {
		case "loginres":
			info.userName = packet.get("username");
			sender.apply(mapOf("qtlnq", ""));
			break;
		case "msgiplist":
			Map<String, String> addr = deserializeMap(deserializeMap(packet.get("iplist")).keySet().iterator().next());
			info.hostIp = addr.get("ip");
			info.port = addr.get("port");
			info.roomId = roomId;
			stop();
			future.complete(info);
			break;
		default:
			System.out.println("Unknown Packet:" + packet);
			break;
		}
	}
	
	public CompletableFuture<BarrageLoginInfo> get() {
		if(future == null) {
			future = new CompletableFuture<>();
			start();
		}
		return future;
	}
	
	private Function<Map<String, String>, CompletableFuture<Void>> sender;
	@Override
	protected void onConnected(Function<Map<String, String>, CompletableFuture<Void>> sender) {
		this.sender = sender;
		
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
	    String devId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
	    String vk = MD5Util.MD5(timestamp + "7oE9nPEG9xXV69phU31FYCLUagKeYtsF" + devId); //vk参数
	    
		sender.apply(mapOf(
				"type", "loginreq",
				"username", "",
				"ct", "0",
				"password", "",
				"roomid", roomId,
				"devid", devId,
				"rt", timestamp,
				"vk", vk,
				"ver", "20180413",
				"aver", "2018061551",
				"ltkid", "",
				"biz", "",
				"stk", "",
				"dfl",""
				));
		
		try {
			future.get();
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
