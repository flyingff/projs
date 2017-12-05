package net.flyingff.snat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.flyingff.snat.NATEntry.NATStatus;

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
		while(true) {
			Set<NATEntry> implemented = proxies.stream().map(it->it.getEntry()).collect(Collectors.toSet()),
					required = new HashSet<>(entries), 
					toAdd = new HashSet<>(required),
					toRemove = new HashSet<>(implemented);
			
			// add new entries' implementation
			toAdd.removeAll(implemented);
			for(NATEntry entry : toAdd) {
				proxies.add(new Proxy(entry));
			}
			// start all started proxies
			for(Proxy p : proxies) {
				if(p.getEntry().getStatus() == NATStatus.STARTING) {
					p.start();
				}
			}
			
			toRemove.removeAll(required);
			// stop all proxies
			for(Proxy p : proxies) {
				NATEntry entry = p.getEntry();
				NATStatus st = entry.getStatus();
				if((toRemove.contains(entry) && 
						(st == NATStatus.STARTED || st == NATStatus.STOPPING)) ||
						p.getEntry().getStatus() == NATStatus.STOPPING) {
					p.stop();
				}
			}
			proxies.removeIf(it->toRemove.contains(it.getEntry()));
			
			try { Thread.sleep(200); } catch (Exception e) { }
		}
	}
}
