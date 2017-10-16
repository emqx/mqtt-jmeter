package net.xmeter.samplers;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.fusesource.mqtt.client.Callback;

public class PubCallback implements Callback<Void>{
	private static Logger logger = LoggingManager.getLoggerForClass();
	private boolean successful = false;
	
	public PubCallback() {
	}
	
	@Override
	public void onSuccess(Void value) {
		this.successful = true;
	}
	
	
	@Override
	public void onFailure(Throwable value) {
		this.successful = false;
		logger.log(Priority.ERROR, value.getMessage(), value);
	}

	public boolean isSuccessful() {
		return successful;
	}
}
