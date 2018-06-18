package net.flyingff.douyu.barrage;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JLabel;

import net.flyingff.douyu.barrage.resolver.AllMessageListener;
import net.flyingff.douyu.barrage.resolver.Gift;
import net.flyingff.douyu.barrage.resolver.Param;
import net.flyingff.douyu.barrage.resolver.Type;

public class Main {
	public static void main(String[] args) throws Exception {
		BarrageLoginInfo info = BarrageAddressGetter.resolve("688").get();
		BarrageReceiver br = new BarrageReceiver(info);
		br.start();
		
		Map<String, int[]> map = new HashMap<>();
		br.setListener(new AllMessageListener() {
			
			@Override
			@Type("dgb")
			public void onOnlineGiftItem(@Param("nn")String userName,
					@Param("gfid")String item,
					@Param("hits") String hits) {
				int[] cnt = map.get(item);
				if(cnt == null) {
					map.put(item, cnt = new int[1]);
				}
				cnt[0] ++;
				System.out.println(userName + "送出了" + Gift.getString(item) + (hits == null ? "" : "[连击" + hits + "]"));
			}
		});
		
		JFrame fr = new JFrame("Statistic");
		JLabel lb = new JLabel();
		fr.add(lb);
		EventQueue.invokeLater(()->{
			print(map, lb);
		});
		fr.setSize(640, 480);
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}
	private static final void print(Map<String, int[]> x, JLabel lbl) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body>");
		for(Entry<String, int[]> entry : x.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()[0]).append("<br>");
		}
		sb.append("</body></html>");
		lbl.setText(sb.toString());
		lbl.repaint();
		EventQueue.invokeLater(()->{
			print(x, lbl);
		});
	}
}
