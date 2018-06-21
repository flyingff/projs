package net.flyingff.douyu.barrage;

import net.flyingff.douyu.barrage.resolver.GiftConfig;

public class BarrageLoginInfo {
	public String userName, hostIp, port, roomId;
	public GiftConfig giftConfig;

	@Override
	public String toString() {
		return "BarrageLoginInfo [userName=" + userName + ", hostIp=" + hostIp + ", port=" + port + "]";
	}
	
	
}
