package net.flyingff.douyu.barrage;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.flyingff.douyu.barrage.resolver.AbstractBarrageListener;

public class BarrageReceiver extends DouyuTCPConnector{
	private final BarrageLoginInfo info;
	public BarrageReceiver(BarrageLoginInfo info) {
		super(new InetSocketAddress(info.hostIp, Integer.parseInt(info.port)));
		this.info = info;
	}
	private AbstractBarrageListener listener = null;
	public void setListener(AbstractBarrageListener listener) {
		this.listener = listener;
	}
	@Override
	protected void handlePacket(Map<String, String> packet) {
		if(listener != null) {
			listener.handle(packet);
		} else {
			System.out.println("Packet: " + packet);
		}
	}

	@Override
	protected void onConnected(Function<Map<String, String>, CompletableFuture<Void>> sender) {
		sender.apply(mapOf(
				"type", "loginreq",
				"username", info.userName,
				"password", "1234567890123456",
				"roomid", info.roomId,
				"dfl", "",
				"ct", "0",
				"ver", "20180413",
				"aver", "2018061551"
				));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) { }
		
		sender.apply(mapOf(
				"type", "joingroup",
				"rid", info.roomId,
				"gid", "-9999"
				));
		while(isStarted()) {
			try {
				Thread.sleep(40000);
			} catch (InterruptedException e) { }
			// heartbeat
			sender.apply(mapOf("type", "mrkl"));
		}
	}
	
}
