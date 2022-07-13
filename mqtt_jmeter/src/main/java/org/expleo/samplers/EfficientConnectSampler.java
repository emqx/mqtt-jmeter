package org.expleo.samplers;

import java.text.MessageFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import org.expleo.Util;
import org.expleo.samplers.mqtt.ConnectionParameters;
import org.expleo.samplers.mqtt.MQTT;
import org.expleo.samplers.mqtt.MQTTClient;
import org.expleo.samplers.mqtt.MQTTConnection;
import org.expleo.samplers.mqtt.MQTTQoS;
import org.expleo.samplers.mqtt.MQTTSsl;

public class EfficientConnectSampler extends AbstractMQTTSampler {

	private static final long serialVersionUID = 780290182989404270L;
	private static final Logger logger = Logger.getLogger(EfficientConnectSampler.class.getCanonicalName());
	
	public static final String SUBSCRIBE_WHEN_CONNECTED = "mqtt.sub_when_connected";
	public static final String CONN_CAPACITY = "mqtt.conn_capacity";
	
	private transient Vector<MQTTConnection> connections;
	
	private Boolean subSucc = null;
	private Object lock;

	@Override
	public SampleResult sample(Entry entry) {
		lock = new Object();
		SampleResult result = new SampleResult();
		result.setSampleLabel(getLabelPrefix() + getName());
		result.setSuccessful(true);
		
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connections = (Vector<MQTTConnection>) vars.getObject("conns");
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
			SampleResult subResult = new SampleResult();
			long cur = 0;
            String clientId;
            if(isClientIdSuffix()) {
                clientId = Util.generateClientId(getConnPrefix());
            } else {
				clientId = getConnPrefix();
				if (clientId != null && !clientId.isEmpty()) {
					clientId += "-xmeter-suffix-" + i;
				}
            }

			try {
				MQTTClient client = createMqttInstance(clientId);
				cur = System.currentTimeMillis();
				subResult.sampleStart();
				subResult.setSampleLabel(getName());
				// TODO: Optionally connection can subscribe to topics ??
				MQTTConnection connection = client.connect();

				if (connection.isConnectionSucc()) {
					connections.add(connection);
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
						subResult.setResponseData(MessageFormat.format("Client [{0}] failed. Could not subscribe to topic(s) {1}.", client.getClientId(), getTopics()).getBytes());
						subResult.setResponseMessage(MessageFormat.format("Failed to subscripbe to topics(s) {0}.", getTopics()));
						subResult.setResponseCode("501");
					}
				} else {
//					failedConnCount += 1;
					subResult.setSuccessful(false);
					subResult.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}.", connection));
					subResult.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't establish connection.", client.getClientId()).getBytes());
					subResult.setResponseCode("501");
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to establish Connection", e);
//				failedConnCount += 1;
				subResult.setSuccessful(false);
				subResult.setResponseMessage("Failed to establish Connections.");
				subResult.setResponseData(MessageFormat.format("Client [{0}] failed with exception.", clientId));
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
	
	private MQTTClient createMqttInstance(String clientId) throws Exception {
        ConnectionParameters parameters = new ConnectionParameters();
        parameters.setClientId(clientId);
		if (parameters.isSecureProtocol()) {
			parameters.setSsl(MQTT.getInstance(getMqttClientName()).createSsl(this));
		}

		parameters.setProtocol(getProtocol());
		parameters.setHost(getServer());
		parameters.setPort(Integer.parseInt(getPort()));
        parameters.setVersion(getMqttVersion());
        parameters.setKeepAlive((short) Integer.parseInt(getConnKeepAlive()));

        parameters.setConnectMaxAttempts(Integer.parseInt(getConnAttemptMax()));
        parameters.setReconnectMaxAttempts(Integer.parseInt(getConnReconnAttemptMax()));
//		System.out.println("!!max reconnect:" + mqtt.getReconnectAttemptsMax());

		if (!"".equals(getUserNameAuth().trim())) {
			parameters.setUsername(getUserNameAuth());
		}
		if (!"".equals(getPasswordAuth().trim())) {
			parameters.setPassword(getPasswordAuth());
		}
		parameters.setCleanSession(Boolean.parseBoolean(getConnCleanSession()));
		parameters.setConnectTimeout(Integer.parseInt(getConnTimeout()));
		if (parameters.isSecureProtocol()) {
			MQTTSsl ssl = MQTT.getInstance(getMqttClientName()).createSsl(this);
			parameters.setSsl(ssl);
		}
		
		return MQTT.getInstance(getMqttClientName()).createClient(parameters);
	}
	
	private boolean handleSubscription(MQTTConnection connection) throws InterruptedException {
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
	
	private void listenToTopics(MQTTConnection connection, final String topicsName) {
	    connection.setSubListener((topic, message, ack) -> {});
		int qos = 0;
		try {
			qos = Integer.parseInt(getQOS());
		} catch(Exception ex) {
			logger.severe(MessageFormat.format("Specified invalid QoS value {0}, set to default QoS value {1}!", ex.getMessage(), qos));
			qos = QOS_0;
		}
		
		final String[] paraTopics = topicsName.split(",");

		if(qos < 0 || qos > 2) {
			logger.severe("Specified invalid QoS value, set to default QoS value " + qos);
			qos = QOS_0;
		}
		connection.subscribe(paraTopics, MQTTQoS.fromValue(qos), () -> {
            synchronized (lock) {
                logger.fine(() -> "sub successful, topic length is " + paraTopics.length);
                subSucc = true;
                lock.notify();
            }
        }, error -> {
            synchronized (lock) {
                logger.info(() -> "subscribe failed: " + error.getMessage());
                subSucc = false;
                lock.notify();
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
