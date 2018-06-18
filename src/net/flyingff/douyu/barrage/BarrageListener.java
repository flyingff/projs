package net.flyingff.douyu.barrage;

import java.util.HashMap;
import java.util.Map;

public abstract class BarrageListener {
	public static final Map<String, String> GIFT_MAP = new HashMap<>();
	static {
		GIFT_MAP.put("824", "粉丝荧光棒");
		GIFT_MAP.put("193", "弱鸡");
		GIFT_MAP.put("712", "棒棒哒");
		GIFT_MAP.put("713", "辣眼睛");
		GIFT_MAP.put("750", "办卡");
		GIFT_MAP.put("1730", "足球");
		GIFT_MAP.put("191", "100鱼丸");
		GIFT_MAP.put("520", "稳");
		GIFT_MAP.put("1027", "药丸");
		GIFT_MAP.put("192", "赞");
		GIFT_MAP.put("714", "怂");
		GIFT_MAP.put("519", "呵呵");
		GIFT_MAP.put("195", "飞机");
	}
	public static String getGift(String giftId) {
		return GIFT_MAP.getOrDefault(giftId, "尚未收录");
	}
	
	public void onLoiginResponse(Map<String, String> packet) {
		System.out.println("Successfully logined");
	}
	public void onKeepAlive(Map<String, String> packet) {
		System.out.println("HeartBeat Received");
	}
	public void onPingRequest(Map<String, String> packet) { }
	
	public void onChat(String uName, String content, Map<String, String> packet) {
		System.out.println(uName + ":" + content);
	}
	public void onPlentyGift(String uName, int count, Map<String, String> packet) {
		System.out.println(uName + " 鱼丸暴击:" + count);
	}
	public void onGift(String uName, String giftId, String multi, Map<String, String> packet) {
		System.out.println(uName + "送了礼物[" + giftId + "]" + getGift(giftId) + (multi == null ? "" : " 连击" + multi));		
	}
	public void onUserEnter(String uName, Map<String, String> packet) {
		System.out.println(uName + "进入了房间");
	}
	public void onDeserve(Map<String, String> packet) {
		System.out.println("赠送酬勤？？");		
	}
	public void onStateChange(boolean close, Map<String, String> packet) {
		System.out.println(close ? "关直播了" : "开直播了");		
	}
	public void onRankChange(Map<String, String> packet) {
		System.out.println("榜单更新");		
	}
	public void onSuperChat(String content, Map<String, String> packet) {
		System.out.println("超级弹幕：" + content);		
	}
	public void onGiftEffect(String giftName, Map<String, String> packet) {
		System.out.println("礼物特效 " + giftName + " - " + packet);
	}
	public void onRedPacket(Map<String, String> packet) {
		System.out.println(packet.get("dnk") + "抢到了" + packet.get("snk") + "派发的" + packet.get("silver") + "个鱼丸");		
	}
	public void onTopChange(Map<String, String> packet) {
		System.out.println("top10变化");		
	}
	public void onError(String errorCode, Map<String, String> packet) {
		System.out.println("出错：" + errorCode  + " - " + packet);		
	}
	public void onUnknown(String type, Map<String, String> packet) {
		System.out.println("未知消息类型" + type + ": " + packet);
	}
	
}
