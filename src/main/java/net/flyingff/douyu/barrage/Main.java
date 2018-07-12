package net.flyingff.douyu.barrage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.flyingff.douyu.barrage.resolver.AllMessageListener;

public class Main {
	public static void main(String[] args) throws Exception {
		BarrageLoginInfo info = BarrageAddressGetter.resolve("160504").get();
		BarrageReceiver br = new BarrageReceiver(info);
		br.start();
		
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://115.154.137.62:3306/stamper?user=stamper&password=stamper&useUnicode=true&characterEncoding=UTF-8");
		PreparedStatement psBarrage = con.prepareStatement("insert into `barrage`(`uname`, `content`, `time`) values(?,?,?)");
		PreparedStatement psGift = con.prepareStatement("insert into `gift`(`gid`, `gname`, `uname`, `time`) values(?,?,?,?)");
		
		// FullScreenBarrageWindow fbw = new FullScreenBarrageWindow();
		// Map<String, int[]> map = new HashMap<>();
		br.setListener(new AllMessageListener(info.giftConfig) {
			@Override
			public void onChat(String userName, String text) {
				super.onChat(userName, text);
				// fbw.pushBarrage(text, userName);
				try {
					psBarrage.setString(1, userName);
					psBarrage.setString(2, text);
					psBarrage.setLong(3, System.currentTimeMillis());
					psBarrage.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onOnlineGiftItem(String userName,
					String item,
					String hits) {
				super.onOnlineGiftItem(userName, item, hits);
				/*
				int[] cnt = map.get(item);
				if(cnt == null) {
					map.put(item, cnt = new int[1]);
				}
				cnt[0] ++;
				print(info.giftConfig, map);
				*/
				try {
					psGift.setInt(1, Integer.parseInt(item));
					psGift.setString(2, info.giftConfig.getString(item));
					psGift.setString(3, userName);
					psGift.setLong(4, System.currentTimeMillis());
					psGift.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
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
	/*
	private static final void print(GiftConfig giftConfig, Map<String, int[]> x) {
		StringBuffer sb = new StringBuffer();
		for(Entry<String, int[]> entry : x.entrySet()) {
			sb.append(giftConfig.getString(entry.getKey())).append(": ").append(entry.getValue()[0]).append(", ");
		}
		sb.setLength(sb.length() - 1);
		System.out.println(sb.toString());
	}
	*/
}
