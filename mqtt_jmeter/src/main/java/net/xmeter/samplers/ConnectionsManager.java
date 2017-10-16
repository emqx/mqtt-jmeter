package net.xmeter.samplers;

import java.util.concurrent.ConcurrentHashMap;

import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;

public class ConnectionsManager {
	private ConcurrentHashMap<String, CallbackConnection> connections = new ConcurrentHashMap<>();
	private static ConnectionsManager connectionsManager = new ConnectionsManager();
	private ConnectionsManager() {
		
	}
	
	public static synchronized ConnectionsManager getInstance() {
		return connectionsManager;
	}
	
	public CallbackConnection createConnection(String key, MQTT mqtt) {
		CallbackConnection conn = mqtt.callbackConnection();
		connections.put(key, conn);
		return conn;
	}
	
	public CallbackConnection getConnection(String key) {
		return this.connections.get(key);
	}
	
	public boolean containsConnection(String key) {
		return connections.containsKey(key);
	}
	
	public void removeConnection(String key) {
		this.connections.remove(key);
	}
}
