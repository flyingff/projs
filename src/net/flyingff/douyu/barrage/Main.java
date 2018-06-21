package net.flyingff.douyu.barrage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.flyingff.douyu.barrage.resolver.AllMessageListener;
import net.flyingff.douyu.barrage.resolver.GiftConfig;

public class Main {
	public static void main(String[] args) throws Exception {
		BarrageLoginInfo info = BarrageAddressGetter.resolve("160504").get();
		BarrageReceiver br = new BarrageReceiver(info);
		br.start();
		
		FullScreenBarrageWindow fbw = new FullScreenBarrageWindow();
		Map<String, int[]> map = new HashMap<>();
		br.setListener(new AllMessageListener(info.giftConfig) {
			@Override
			public void onChat(String userName, String text) {
				super.onChat(userName, text);
				fbw.pushBarrage(text, userName);
			}
			@Override
			public void onOnlineGiftItem(String userName,
					String item,
					String hits) {
				super.onOnlineGiftItem(userName, item, hits);
				
				int[] cnt = map.get(item);
				if(cnt == null) {
					map.put(item, cnt = new int[1]);
				}
				cnt[0] ++;
				print(info.giftConfig, map);
			}
		});
		
		/*
		try(Scanner sc = new Scanner(System.in)) {
			while(sc.hasNext()) {
				String id = sc.next();
				Gift g = info.giftConfig.get(id);
				if(g == null) System.out.println("Not found");
				else {
					for(BufferedImage img : g.getImgs()) {
						JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)));
					}
				}
			}
		}*/
		
	}
	private static final void print(GiftConfig giftConfig, Map<String, int[]> x) {
		StringBuffer sb = new StringBuffer();
		for(Entry<String, int[]> entry : x.entrySet()) {
			sb.append(giftConfig.getString(entry.getKey())).append(": ").append(entry.getValue()[0]).append(", ");
		}
		sb.setLength(sb.length() - 1);
		System.out.println(sb.toString());
	}
}
