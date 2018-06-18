package net.flyingff.douyu.barrage.resolver;

import java.util.HashMap;
import java.util.Map;

class Holder {
	static final Map<String, Gift> refMap = new HashMap<>();
}
public enum Gift {
	UNKNOWN("-1", "???", 0),
	
	LED("824", "粉丝荧光棒", 1),
	CHICKEN("193", "弱鸡", 1),
	GOOD("712", "棒棒哒", 1),
	LYJ("713", "辣眼睛", 1),
	STABLE("520", "稳", 1),
	GREAT("192", "赞", 1),
	YUWAN("191", "100鱼丸", 1),
	CAPSULE("1027", "药丸", 1),
	SONG("714", "怂", 1),
	HEHE("519", "呵呵", 1),
	FOOTBALL("1730", "足球", 10),
	CARD("750", "办卡", 60),
	PLANE("195", "飞机", 1000),
	ROCKET("196", "火箭", 5000),
	SROCKET("0", "超级火箭", 20000)
	;
	
	Gift(String id, String name, int value) {
		this.id = id;
		this.name = name;
		this.value = value;
		Holder.refMap.put(id, this);
	}
	
	private final String id, name;
	private final int value;
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getValue() {
		return value;
	}
	
	public static Gift get(String id) {
		return Holder.refMap.getOrDefault(id, UNKNOWN);
	}
	public static String getString(String id) {
		Gift g = Holder.refMap.get(id);
		return g == null ? "[" + id + "]" : g.name;
	}
}
