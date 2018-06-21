package net.flyingff.douyu.barrage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.flyingff.douyu.barrage.resolver.Gift;
import net.flyingff.douyu.barrage.resolver.GiftConfig;
import net.flyingff.douyu.barrage.util.MD5Util;

public class BarrageAddressGetter extends DouyuTCPConnector {
	private final String roomId;
	private BarrageLoginInfo info = new BarrageLoginInfo();
	private BarrageAddressGetter(String roomId, InetSocketAddress addr, GiftConfig config) {
		super(addr);
		this.roomId = roomId;
		this.info.giftConfig = config;
	}
	
	public static CompletableFuture<BarrageLoginInfo> resolve(String roomName) {
		InetSocketAddress sockAddr;
		String roomId;
		try {
			Document dom = Jsoup.connect("http://www.douyu.com/" + roomName + "?t=" + System.currentTimeMillis())
				.followRedirects(true)
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")  
		        .header("Accept-Encoding", "gzip, deflate")  
		        .header("Accept-Language","zh-CN,zh;q=0.9")
		        .header("Upgrade-Insecure-Requests", "1")
		        .header("Host", "www.douyu.com")
		        .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36") 
		        .timeout(120_0000).get();
			
			for(Element it : dom.select("script")) {
				String content = it.html();
				if(content.contains("var $ROOM")) {
					ScriptEngineManager manager = new ScriptEngineManager();
					ScriptEngine engine = manager.getEngineByName("javascript");
					
					engine.eval(content);
					engine.eval("$SERVER = eval(decodeURIComponent($ROOM.args.server_config))[0]");
					
					// barrage socket addr
					String ip = (String) engine.eval("$SERVER['ip']"),
							port = engine.eval("$SERVER['port']").toString();
					sockAddr = new InetSocketAddress(ip, Integer.parseInt(port));
					
					// room id
					roomId = engine.eval("$ROOM.room_id").toString();
					String giftConfigTemplateId = engine.eval("$ROOM.giftTempId").toString();
					
					engine.eval("var data; function DYConfigCallback(d) { data = JSON.stringify(d.data); }"); 
					
					Map<String, Map<String, String>> dataMap = new HashMap<>();
					
					resolveGift(engine, "https://webconf.douyucdn.cn/resource/common/prop_gift_list/prop_gift_config.json", dataMap, false);
					resolveGift(engine, "https://webconf.douyucdn.cn/resource/common/gift/gift_template/" +
							giftConfigTemplateId + ".json", dataMap, true);
					
					return new BarrageAddressGetter(roomId, sockAddr, Gift.initialize(dataMap)).get();
				}
			}
			throw new AssertionError("Room configuration not found!");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void resolveGift(ScriptEngine engine,
			String url,
			Map<String, Map<String, String>> dataMap,
			boolean array)
			throws IOException, ScriptException {
		Response res = Jsoup.connect(url)  
		        .header("Accept", "*/*")  
		        .header("Accept-Encoding", "gzip, deflate")  
		        .header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")  
		        .header("Host", "webconf.douyucdn.cn")
		        .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36") 
		        .timeout(60000).ignoreContentType(true).execute();
		engine.eval(res.body());
		String json = engine.eval("data").toString();
		if(array) {
			JsonArray arr = new Gson().fromJson(json, JsonArray.class);
			for(JsonElement e : arr) {
				Map<String, String> data = new HashMap<>();
				JsonObject o = e.getAsJsonObject();
				
				String id = o.get("id").getAsString();
				
				data.put("name", o.get("name").getAsString());
				data.put("himg", o.get("himg").getAsString());
				data.put("intro", o.get("intro").getAsString());
				data.put("devote", o.get("devote").getAsString());
				
				dataMap.put(id, data);
			}
		} else {
			JsonObject obj = new Gson().fromJson(json, JsonObject.class);
			for(Entry<String, JsonElement> e : obj.entrySet()) {
				
				Map<String, String> data = new HashMap<>();
				String id = e.getKey();
				JsonObject o = e.getValue().getAsJsonObject();
				
				data.put("name", o.get("name").getAsString());
				data.put("himg", o.get("himg").getAsString());
				data.put("intro", "");
				data.put("devote", o.get("devote").getAsString());
				
				dataMap.put(id, data);
			}
		}
	}

	private CompletableFuture<BarrageLoginInfo> future;
	@Override
	protected void handlePacket(Map<String, String> packet) {
		String type = packet.get("type");
		switch (type) {
		case "loginres":
			info.userName = packet.get("username");
			sender.apply(mapOf("qtlnq", ""));
			break;
		case "msgiplist": {
			Map<String, String> addr = deserializeMap(deserializeMap(packet.get("iplist")).keySet().iterator().next());
			info.hostIp = addr.get("ip");
			info.port = addr.get("port");
			info.roomId = roomId;
			stop();
			future.complete(info);
		}
			break;
		case "msgrepeaterlist": {
			Map<String, String> addr = deserializeMap(deserializeMap(packet.get("list")).keySet().iterator().next());
			info.hostIp = addr.get("ip");
			info.port = addr.get("port");
			info.roomId = roomId;
			stop();
			future.complete(info);
		}
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
