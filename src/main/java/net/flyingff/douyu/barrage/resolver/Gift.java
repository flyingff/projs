package net.flyingff.douyu.barrage.resolver;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/*
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
	
	
}
*/

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public final class Gift {
	public static final GiftConfig initialize(Map<String, Map<String,String>> info) {
		Map<String, Gift> giftMap = new HashMap<>();
		for(java.util.Map.Entry<String, Map<String, String>> entry : info.entrySet()) {
			String name, intro, devote;
			String id = entry.getKey();
			Map<String, String> map = entry.getValue();
			
			name = map.get("name");
			intro = map.get("intro");
			devote = map.get("devote");
			Gift g = new Gift(id, name, intro, (int) Math.round(Double.parseDouble(devote)), map.get("himg"));
			giftMap.put(id, g);
		}
		return new GiftConfig(giftMap);
	}
	
	public final String id, name, intro, himg;
	public final int value;
	private BufferedImage[] imgs = null;
	private Gift(String id, String name, String intro, int devote, String himg) {
		this.id = id;
		this.name = name;
		this.intro = intro;
		this.value = devote;
		this.himg = himg;
	}
	public BufferedImage[] getImgs() {
		if(imgs == null) {
			try (ImageInputStream iis = ImageIO.createImageInputStream(new URL(himg).openStream())) {
				ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
				reader.setInput(iis, false);
				int cnt = reader.getNumImages(true);
				imgs = new BufferedImage[cnt];
				Arrays.setAll(imgs, it -> {
					try { return reader.read(it); } catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return imgs;
	}
	@Override
	public String toString() {
		return name;
	}
}
