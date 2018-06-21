package net.flyingff.douyu.barrage.resolver;

import java.util.Map;

public class GiftConfig {
	private final Map<String, Gift> giftMap;

	public GiftConfig(Map<String, Gift> giftMap) {
		this.giftMap = giftMap;
		System.out.println(giftMap);
	}
	public Gift get(String id) {
		return giftMap.getOrDefault(id, null);
	}
	public String getString(String id) {
		Gift g = giftMap.get(id);
		return g == null ? "[" + id + "]" : g.name;
	}
}
