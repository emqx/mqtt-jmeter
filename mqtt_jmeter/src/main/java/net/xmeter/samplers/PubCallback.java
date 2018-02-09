package net.xmeter.samplers;


import java.util.logging.Logger;

import org.fusesource.mqtt.client.Callback;

public class PubCallback implements Callback<Void>{
	private static final Logger logger = Logger.getLogger(ConnectionCallback.class.getCanonicalName());
	private boolean successful = false;
	private Object pubLock;
	
	public PubCallback(Object pubLock) {
		this.pubLock = pubLock;
	}
	
	@Override
	public void onSuccess(Void value) {
		//If QoS == 0, then the current thread is the same thread of caller thread.
		//Else if QoS == 1 | 2, then the current thread is hawtdispatch-DEFAULT-x
		synchronized (pubLock) {
			this.successful = true;
			pubLock.notify();
		}
	}
	
	@Override
	public void onFailure(Throwable value) {
		synchronized (pubLock) {
			this.successful = false;
			logger.severe(value.getMessage());
			pubLock.notify();
		}
	}

	public boolean isSuccessful() {
		return successful;
	}
}
