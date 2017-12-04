package net.flyingff.snat;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NATEntry implements Serializable{
	private static final long serialVersionUID = 1L;
	private final int localPort, externalPort;
	private Pattern ipRegExp;
	
	private transient NATStatus status = NATStatus.STARTING;
	private transient int connections = 0;
	private transient long dataTransfered = 0;
	
	public NATEntry(int localPort, int externalPort) {
		this.localPort = localPort;
		this.externalPort = externalPort;
		ipRegExp = Pattern.compile(".*");
	}

	public String getIpRegExp() {
		return ipRegExp.toString();
	}

	public boolean setIpRegExp(String ipRegExp) {
		try {
			this.ipRegExp = Pattern.compile(ipRegExp);
		} catch (PatternSyntaxException e) {
			return false;
		}
		return true;
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}

	public long getDataTransfered() {
		return dataTransfered;
	}

	public void setDataTransfered(long dataTransfered) {
		this.dataTransfered = dataTransfered;
	}

	public int getLocalPort() {
		return localPort;
	}

	public int getExternalPort() {
		return externalPort;
	}
	public NATStatus getStatus() {
		if(status == null) {
			status = NATStatus.STARTING;
		}
		return status;
	}
	public void setStatus(NATStatus status) {
		this.status = status;
	}
	
	public static enum NATStatus {
		STARTING("Starting"), STARTED("OK"), ERROR("Error");
		private final String name;
		private NATStatus(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
}
