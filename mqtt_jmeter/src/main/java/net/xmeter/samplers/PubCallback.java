package net.xmeter.samplers;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.fusesource.mqtt.client.Callback;

public class PubCallback implements Callback<Void>{
	private static Logger logger = LoggingManager.getLoggerForClass();
	private boolean successful = false;
	private Object connLock;
	
	public PubCallback(Object connLock) {
		this.connLock = connLock;
	}
	
	@Override
	public void onSuccess(Void value) {
		synchronized (connLock) {
			System.out.println("PubCallback acquired lock..." );
			this.successful = true;
			System.out.println("PubCallback is notifying..." );
			connLock.notify();
		}
	}
	
	@Override
	public void onFailure(Throwable value) {
		synchronized (connLock) {
			this.successful = false;
			logger.log(Priority.ERROR, value.getMessage(), value);
			connLock.notify();
		}
	}

	public boolean isSuccessful() {
		return successful;
	}
}
