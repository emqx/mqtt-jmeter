package org.expleo.samplers.mqtt.fuse;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.MQTT;

import org.expleo.samplers.mqtt.ConnectionParameters;

class ConnectionCallback implements Callback<Void> {
	private static final Logger logger = Logger.getLogger(ConnectionCallback.class.getCanonicalName());
	private final MQTT mqtt;
	private final ConnectionParameters parameters;
	private final Semaphore connLock;
	private volatile boolean connSucc = false;
	
	ConnectionCallback(MQTT mqtt, ConnectionParameters parameters, Semaphore connLock) {
		this.mqtt = mqtt;
		this.parameters = parameters;
		this.connLock = connLock;
	}

	@Override
	public void onSuccess(Void value) {
		connSucc = true;
		connLock.release();
	}

	@Override
	public void onFailure(Throwable value) {
		connSucc = false;
		connLock.release();
		logger.log(Level.SEVERE, "Failed to connect " + mqtt, value);
	}

	boolean isConnSucc() {
		return connSucc;
	}
}
