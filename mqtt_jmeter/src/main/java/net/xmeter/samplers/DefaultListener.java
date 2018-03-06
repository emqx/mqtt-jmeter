package net.xmeter.samplers;

import java.util.logging.Logger;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

@SuppressWarnings("deprecation")
public class DefaultListener implements Listener {

	private static final Logger logger = Logger.getLogger(DefaultListener.class.getCanonicalName());
	protected Object lock;
	private String received;
	private boolean succ = true;

	public DefaultListener(Object connLock) {
		this.lock = connLock;
	}

	@Override
	public void onConnected() {

	}

	@Override
	public void onDisconnected() {

	}

	@Override
	public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
		synchronized (lock) {
			this.received = topic + "," + body.hex();
			ack.run();
			this.succ = true;
			this.lock.notify();
		}
	}

	@Override
	public void onFailure(Throwable value) {
		synchronized (lock) {
			logger.severe(value.getMessage());
			this.succ = false;
			this.lock.notify();
		}
	}

	public String getReceived() {
		return received;
	}

	public boolean isSucc() {
		return succ;
	}
}
