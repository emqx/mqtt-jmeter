package net.xmeter.samplers;

import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;

public class ConnectionInfo {
	private CallbackConnection connection;
	private UTF8Buffer clientId;
	
	public ConnectionInfo(CallbackConnection connection, UTF8Buffer clientId) {
		this.connection = connection;
		this.clientId = clientId;
	}

	public CallbackConnection getConnection() {
		return connection;
	}

	public UTF8Buffer getClientId() {
		return clientId;
	}
	
}