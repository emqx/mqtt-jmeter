package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import net.xmeter.Util;

public class ConnectionSampler extends AbstractMQTTSampler
		implements TestStateListener, ThreadListener, Interruptible, SampleListener {
	private transient static Logger logger = LoggingManager.getLoggerForClass();
	private transient MQTT mqtt = new MQTT();
	private transient FutureConnection connection = null;
	private boolean interrupt = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1859006013465470528L;

	@Override
	public boolean isKeepTimeShow() {
		return true;
	}
	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		try {
			if (!DEFAULT_PROTOCOL.equals(getProtocol())) {
				mqtt.setSslContext(Util.getContext(this));
			}

			mqtt.setHost(getProtocol().toLowerCase() + "://" + getServer() + ":" + getPort());
			mqtt.setKeepAlive((short) getConnKeepAlive());
			String clientId = Util.generateClientId(getConnPrefix());
			mqtt.setClientId(clientId);
			mqtt.setConnectAttemptsMax(getConnAttamptMax());
			mqtt.setReconnectAttemptsMax(getConnReconnAttamptMax());

			if (!"".equals(getUserNameAuth().trim())) {
				mqtt.setUserName(getUserNameAuth());
			}
			if (!"".equals(getPasswordAuth().trim())) {
				mqtt.setPassword(getPasswordAuth());
			}

			result.sampleStart();
			connection = mqtt.futureConnection();
			Future<Void> f1 = connection.connect();
			f1.await(getConnTimeout(), TimeUnit.SECONDS);

			Topic[] topics = { new Topic("topic_" + clientId, QoS.AT_LEAST_ONCE) };
			connection.subscribe(topics);

			result.sampleEnd();
			result.setSuccessful(true);
			result.setResponseData("Successful.".getBytes());
			result.setResponseMessage(MessageFormat.format("Connection {0} connected successfully.", connection));
			result.setResponseCodeOK();
		} catch (Exception e) {
			logger.log(Priority.ERROR, e.getMessage(), e);
			result.sampleEnd();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Connection {0} connected failed.", connection));
			result.setResponseData("Failed.".getBytes());
			result.setResponseCode("500");
		}
		return result;
	}

	@Override
	public void testEnded() {
		this.testEnded("local");
	}

	@Override
	public void testEnded(String arg0) {
		this.interrupt = true;
	}

	@Override
	public void testStarted() {

	}

	@Override
	public void testStarted(String arg0) {
	}

	@Override
	public void threadFinished() {
		if (JMeter.isNonGUI()) {
			logger.info("The work has been done, will sleep current thread for " + getConnKeepTime() + " sceconds.");
			sleepCurrentThreadAndDisconnect();
		}
	}

	private void sleepCurrentThreadAndDisconnect() {
		try {
			long start = System.currentTimeMillis();
			while ((System.currentTimeMillis() - start) <= TimeUnit.SECONDS.toMillis(getConnKeepTime())) {
				if (this.interrupt) {
					logger.info("interrupted flag is true, and stop the sleep.");
					break;
				}
				TimeUnit.SECONDS.sleep(1);
			}

			if (connection != null) {
				connection.disconnect();
				logger.log(Priority.INFO,
						MessageFormat.format("The connection {0} disconneted successfully.", connection));
			}
		} catch (InterruptedException e) {
			logger.log(Priority.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void threadStarted() {

	}

	@Override
	public boolean interrupt() {
		this.interrupt = true;
		if (!JMeter.isNonGUI()) {
			logger.info("In GUI mode, received the interrupt request from user.");
		}
		return true;
	}

	/**
	 * In this listener, it can receive the interrupt event trigger by user.
	 */
	@Override
	public void sampleOccurred(SampleEvent event) {
		if (!JMeter.isNonGUI()) {
			logger.info(
					"Created the sampler results, will sleep current thread for " + getConnKeepTime() + " sceconds");
			sleepCurrentThreadAndDisconnect();
		}
	}

	@Override
	public void sampleStarted(SampleEvent arg0) {
	}

	@Override
	public void sampleStopped(SampleEvent arg0) {
	}
}
