package net.flyingff.snat;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NATEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	private final int localPort, externalPort;
	private Pattern ipRegExp;
	
	private NATStatus status = NATStatus.STOPPED;
	private transient int connections = 0;
	private transient long dataTransfered = 0;
	
	public NATEntry(int localPort, int externalPort) {
		this.localPort = localPort;
		this.externalPort = externalPort;
		ipRegExp = Pattern.compile(".*");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + externalPort;
		result = prime * result + localPort;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NATEntry other = (NATEntry) obj;
		if (externalPort != other.externalPort)
			return false;
		if (localPort != other.localPort)
			return false;
		return true;
	}

	public String getIpRegExp() {
		return ipRegExp.toString();
	}
	public boolean testIP(String ip) {
		return ipRegExp.matcher(ip).matches();
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

	public void incConnections() {
		connections += 1;
	}
	public void decConnections() {
		connections -= 1;
	}
	public void resetConnections() {
		connections = 0;
	}

	private static final String[] UNIT = {
			"Byte", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "?!"
	};
	public String getDataTransfered() {
		int unit = 0;
		double result = dataTransfered;
		while(result > 1000) {
			result /= 1024;
			unit++;
		}
		unit = Math.min(unit, UNIT.length - 1);
		return String.format("%.2f%s", result, UNIT[unit]);
	}

	public void acuumulateDataTransfered(long dataTransfered) {
		this.dataTransfered += dataTransfered;
	}

	public int getLocalPort() {
		return localPort;
	}

	public int getExternalPort() {
		return externalPort;
	}
	public NATStatus getStatus() {
		if(status == null) {
			status = NATStatus.STOPPED;
		}
		return status;
	}
	public void setStatus(NATStatus status) {
		this.status = status;
	}
	
	public static enum NATStatus {
		STOPPED("Stopped"), STARTING("Starting"), STARTED("OK"), ERROR("Error"), STOPPING("Stopping");
		private final String name;
		private NATStatus(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	public void restore() {
		if(status == NATStatus.STARTED) {
			status = NATStatus.STARTING;
		} else {
			status = NATStatus.STOPPED;
		}
	}
}
