package net.xmeter.samplers;

import java.util.logging.Logger;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;

public class ConnectionCallback implements Callback<Void>{
	private static final Logger logger = Logger.getLogger(ConnectionCallback.class.getCanonicalName());
	private Object connLock;
	private boolean connSucc = false;
	
	public ConnectionCallback(CallbackConnection connection, Object connLock) {
		this.connLock = connLock;
	}
	@Override
	public void onSuccess(Void value) {
		synchronized (connLock) {
			connSucc = true;
			this.connLock.notify();	
		}
	}

	@Override
	public void onFailure(Throwable value) {
		synchronized (connLock) {
			connSucc = false;
			logger.severe(value.getMessage());
			this.connLock.notify();			
		}
	}
	
	public boolean isConnectionSucc() {
		return connSucc;
	}
}
