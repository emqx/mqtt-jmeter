package net.xmeter.samplers;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.fusesource.mqtt.client.FutureConnection;

public class ConnectionsManager {
	private ConcurrentHashMap<String, FutureConnection> connections = new ConcurrentHashMap<>();
	private static ConnectionsManager connectionsManager = new ConnectionsManager();
	
	private ConnectionsManager() {
		
	}
	
	public static ConnectionsManager getInstance() {
		return connectionsManager;
	}
	
	public void addConnection(String key, FutureConnection conn) {
		this.connections.put(key, conn);
	}
	
	public FutureConnection getConnection(String key) {
		return this.connections.get(key);
	}
	
	public void removeConnection(String key) {
		this.connections.remove(key);
	}
	
	public void disconnectAndRemoveConn(String key) {
		FutureConnection connection =  this.connections.get(key);
		if(connection != null && connection.isConnected()) {
			connection.disconnect();
		}
		connections.remove(key);
	}
	
	public void disconnectAndRemoveAll() {
		Iterator<FutureConnection> connIt = connections.values().iterator();
		while(connIt.hasNext()) {
			FutureConnection connection = connIt.next();
			if(connection.isConnected()) {
				connection.disconnect();	
			}
		}
		connections.clear();
	}
}
