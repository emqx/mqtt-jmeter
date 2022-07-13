package net.xmeter.samplers;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.QoS;

public class PubCallback implements Callback<Void>{
	private static final Logger logger = Logger.getLogger(PubCallback.class.getCanonicalName());
	private boolean successful = false;
	private Object pubLock;
	private String errorMessage = "";
	private QoS qos;
	
	public PubCallback(Object pubLock, QoS qos) {
		this.pubLock = pubLock;
		this.qos = qos;
		if(this.qos == QoS.AT_MOST_ONCE) {
			this.successful = true;
		}
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
			this.errorMessage = "err: " + value.getMessage();
			logger.log(Level.SEVERE, value.getMessage(), value);
			pubLock.notify();
		}	
	}

	public boolean isSuccessful() {
		return successful;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
