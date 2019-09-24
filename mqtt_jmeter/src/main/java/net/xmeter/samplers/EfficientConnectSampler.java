package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import net.xmeter.Util;

public class EfficientConnectSampler extends AbstractMQTTSampler {

	private static final long serialVersionUID = 780290182989404270L;
	private static final Logger logger = Logger.getLogger(EfficientConnectSampler.class.getCanonicalName());
	
	public static final String SUBSCRIBE_WHEN_CONNECTED = "mqtt.sub_when_connected";
	public static final String CONN_CAPACITY = "mqtt.conn_capacity";
	
	private transient Vector<ConnectionInfo> connections;
	
	private Boolean subSucc = null;
	private Object lock = new Object();

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getLabelPrefix() + getName());
		result.setSuccessful(true);
		
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connections = (Vector<ConnectionInfo>) vars.getObject("conns");
		if (connections != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Connections are already established.");
			result.setResponseData("Failed. Connections are already established.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}
		
		connections = new Vector<>();
		int conCapacity = Integer.parseInt(getConnCapacity());
		vars.putObject("conCapacity", conCapacity);
//		String conCapacityStr = System.getProperty("batchConPerSampler");
//		String conCapacityStr = "5";
//		if (conCapacityStr != null) {
//			try {
//				conCapacity = Integer.parseInt(conCapacityStr);
//				setConCapacity(conCapacity);
//			} catch (NumberFormatException e) {
//				logger.warning(MessageFormat.format("Invalid conCapacity value {0}", conCapacityStr));
//			}
//		}
		
		result.sampleStart();
		int totalSampleCount = 0;
		for (int i=0; i<conCapacity; i++) {
			MQTT mqtt = null;
			ConnectionInfo conInfo = null;
			SampleResult subResult = new SampleResult();
			long cur = 0;
			try {
				String clientId = null;
				if(isClientIdSuffix()) {
					clientId = Util.generateClientId(getConnPrefix());
				} else {
					clientId = getConnPrefix();
					if (clientId != null && !clientId.isEmpty()) {
						 clientId += "-xmeter-suffix-" + i;
					}
				}
				
				mqtt = createMqttInstance(clientId);
				cur = System.currentTimeMillis();
				subResult.sampleStart();
				subResult.setSampleLabel(getName());
				// TODO: Optionally connection can subscribe to topics ??
				CallbackConnection connection = mqtt.callbackConnection();
				Object connLock = new Object();
				ConnectionCallback callback = new ConnectionCallback(connection, connLock);
				synchronized (connLock) {
					connection.connect(callback);
					connLock.wait(TimeUnit.SECONDS.toMillis(Integer.parseInt(getConnTimeout())));
				}
				
				if (callback.isConnectionSucc()) {
					conInfo = new ConnectionInfo(connection, mqtt.getClientId());
					connections.add(conInfo);
//					setTopicSubscribed(mqtt.getClientId(), new HashSet<String>());
					//check if subscription needed
					boolean suc = true;
					if (isSubWhenConnected()) {
						suc = handleSubscription(connection);
					}
					if (suc) {
						subResult.setSuccessful(true);
						subResult.setResponseData("Successful.".getBytes());
						subResult.setResponseMessage(MessageFormat.format("Connection {0} established successfully.", connection));
						subResult.setResponseCodeOK();
					} else {
						subResult.setSuccessful(false);
						subResult.setResponseData(MessageFormat.format("Client [{0}] failed. Could not subscribe to topic(s) {1}.", mqtt.getClientId().toString(), getTopics()).getBytes());
						subResult.setResponseMessage(MessageFormat.format("Failed to subscripbe to topics(s) {0}.", getTopics()));
						subResult.setResponseCode("501");
					}
				} else {
//					failedConnCount += 1;
					subResult.setSuccessful(false);
					subResult.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}.", connection));
					subResult.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't establish connection.", mqtt.getClientId().toString()).getBytes());
					subResult.setResponseCode("501");
				}
			} catch (Exception e) {
				logger.severe(e.getMessage());
//				failedConnCount += 1;
				subResult.setSuccessful(false);
				subResult.setResponseMessage("Failed to establish Connections.");
				subResult.setResponseData(MessageFormat.format("Client [{0}] failed with exception.", mqtt.getClientId().toString()).getBytes());
				subResult.setResponseCode("502");
			} finally {
				totalSampleCount += subResult.getSampleCount();
				subResult.sampleEnd();
				result.addSubResult(subResult);
			}
			
		}
		if (!connections.isEmpty()) {
			vars.putObject("conns", connections);
		}
		result.setSampleCount(totalSampleCount);
		if (result.getEndTime() == 0) {
			result.sampleEnd();
		}
		
		return result;
	}
	
	private MQTT createMqttInstance(String clientId) throws Exception {
		MQTT mqtt = new MQTT();
		if (!DEFAULT_PROTOCOL.equals(getProtocol())) {
			mqtt.setSslContext(Util.getContext(this));
		}
		
		mqtt.setHost(getProtocol().toLowerCase() + "://" + getServer() + ":" + getPort());
		mqtt.setVersion(getMqttVersion());
		mqtt.setKeepAlive((short) Integer.parseInt(getConnKeepAlive()));
		
		mqtt.setClientId(clientId);
		
		mqtt.setConnectAttemptsMax(Integer.parseInt(getConnAttamptMax()));
		mqtt.setReconnectAttemptsMax(Integer.parseInt(getConnReconnAttamptMax()));
//		System.out.println("!!max reconnect:" + mqtt.getReconnectAttemptsMax());

		if (!"".equals(getUserNameAuth().trim())) {
			mqtt.setUserName(getUserNameAuth());
		}
		if (!"".equals(getPasswordAuth().trim())) {
			mqtt.setPassword(getPasswordAuth());
		}
		mqtt.setCleanSession(getConnCleanSession());
		
//		mqtt.setTracer(new Tracer() {
//			 public void debug(String message, Object...args) {
//				 logger.info("[" + mqtt.getClientId().toString() + "] MQTT Tracer: " + String.format(message, args));
//			 }
//		});
		
		return mqtt;
	}
	
	private boolean handleSubscription(CallbackConnection connection) throws InterruptedException {
		final String topicsName= getTopics();
		listenToTopics(connection, topicsName);
		synchronized (lock) {
			if (subSucc == null) {
				lock.wait();
			}
			if (subSucc) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	private void listenToTopics(CallbackConnection connection, final String topicsName) {
		connection.listener(new Listener() {

			@Override
			public void onConnected() {
			}

			@Override
			public void onDisconnected() {
			}

			@Override
			public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
			}

			@Override
			public void onFailure(Throwable value) {
			}
			
		});
		int qos = 0;
		try {
			qos = Integer.parseInt(getQOS());
		} catch(Exception ex) {
			logger.severe(MessageFormat.format("Specified invalid QoS value {0}, set to default QoS value {1}!", ex.getMessage(), qos));
			qos = QOS_0;
		}
		
		final String[] paraTopics = topicsName.split(",");
		
		Topic[] topics = new Topic[paraTopics.length];
		if(qos < 0 || qos > 2) {
			logger.severe("Specified invalid QoS value, set to default QoS value " + qos);
			qos = QOS_0;
		}
		for(int i = 0; i < topics.length; i++) {
			if (qos == QOS_0) {
				topics[i] = new Topic(paraTopics[i], QoS.AT_MOST_ONCE);
			} else if (qos == QOS_1) {
				topics[i] = new Topic(paraTopics[i], QoS.AT_LEAST_ONCE);
			} else {
				topics[i] = new Topic(paraTopics[i], QoS.EXACTLY_ONCE);
			}
		}

		connection.subscribe(topics, new Callback<byte[]>() {
			@Override
			public void onSuccess(byte[] value) {
				synchronized (lock) {
					logger.fine("sub successful, topic length is " + paraTopics.length);
					subSucc = true;
					lock.notify();
				}
			}

			@Override
			public void onFailure(Throwable value) {
				synchronized (lock) {
					logger.info("subscribe failed: " + value.getMessage());
					subSucc = false;
					lock.notify();
				}
			}
		});
	}
	
	public boolean isSubWhenConnected() {
		return getPropertyAsBoolean(SUBSCRIBE_WHEN_CONNECTED, DEFAULT_SUBSCRIBE_WHEN_CONNECTED);
	}
	
	public void setSubWhenConnected(boolean subWhenConnected) {
		setProperty(SUBSCRIBE_WHEN_CONNECTED, subWhenConnected);
	}
	
	public String getQOS() {
		return getPropertyAsString(QOS_LEVEL, String.valueOf(QOS_0));
	}

	public void setQOS(String qos) {
		setProperty(QOS_LEVEL, qos);
	}

	public String getTopics() {
		return getPropertyAsString(TOPIC_NAME, DEFAULT_TOPIC_NAME);
	}

	public void setTopics(String topicsName) {
		setProperty(TOPIC_NAME, topicsName);
	}
	
	public String getConnCapacity() {
		return getPropertyAsString(CONN_CAPACITY, "1");
	}
	
	public void setConnCapacity(String conCapacity) {
		setProperty(CONN_CAPACITY, conCapacity);
	}
}
