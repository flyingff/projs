package net.flyingff.snat;

import java.util.ArrayList;
import java.util.List;

public class ProxyManager {
	private List<NATEntry> entries;
	private List<Proxy> proxies;
	public ProxyManager(List<NATEntry> entries) {
		this.entries = entries;
		this.proxies = new ArrayList<>();
		
		Thread th = new Thread(this::work);
		th.setDaemon(true);
		th.start();
	}
	private void work() {
		
		
	}
}
