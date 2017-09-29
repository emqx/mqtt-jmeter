package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import net.xmeter.Util;

public class PubSampler extends AbstractMQTTSampler implements ThreadListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4312341622759500786L;
	private transient static Logger logger = LoggingManager.getLoggerForClass();
	private transient MQTT mqtt = new MQTT();
	private transient FutureConnection connection = null;
	private String payload = null;
	private String clientId = "";
	private QoS qos_enum = QoS.AT_MOST_ONCE;
	private String topicName = "";

	public String getQOS() {
		return getPropertyAsString(QOS_LEVEL, String.valueOf(QOS_0));
	}

	public void setQOS(String qos) {
		setProperty(QOS_LEVEL, qos);
	}

	public String getTopic() {
		return getPropertyAsString(TOPIC_NAME, DEFAULT_TOPIC_NAME);
	}

	public void setTopic(String topicName) {
		setProperty(TOPIC_NAME, topicName);
	}

	public boolean isAddTimestamp() {
		return getPropertyAsBoolean(ADD_TIMESTAMP);
	}

	public void setAddTimestamp(boolean addTimestamp) {
		setProperty(ADD_TIMESTAMP, addTimestamp);
	}

	public String getMessageType() {
		return getPropertyAsString(MESSAGE_TYPE, MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN);
	}

	public void setMessageType(String messageType) {
		setProperty(MESSAGE_TYPE, messageType);
	}

	public String getMessageLength() {
		return getPropertyAsString(MESSAGE_FIX_LENGTH, DEFAULT_MESSAGE_FIX_LENGTH);
	}

	public void setMessageLength(String length) {
		setProperty(MESSAGE_FIX_LENGTH, length);
	}

	public String getMessage() {
		return getPropertyAsString(MESSAGE_TO_BE_SENT, "");
	}

	public void setMessage(String message) {
		setProperty(MESSAGE_TO_BE_SENT, message);
	}

	public String getConnPrefix() {
		return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX_FOR_PUB);
	}
	
	public static byte[] hexToBinary(String hex) {
	    return DatatypeConverter.parseHexBinary(hex);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		if (connection == null) { // first loop, do initialization
			try {
				if (!DEFAULT_PROTOCOL.equals(getProtocol())) {
					mqtt.setSslContext(Util.getContext(this));
				}
				
				mqtt.setVersion(getMqttVersion());
				mqtt.setHost(getProtocol().toLowerCase() + "://" + getServer() + ":" + getPort());
				mqtt.setKeepAlive((short) Integer.parseInt(getConnKeepAlive()));

				if(isClientIdSuffix()) {
					clientId = Util.generateClientId(getConnPrefix());
				} else {
					clientId = getConnPrefix();
				}
				mqtt.setClientId(clientId);

				mqtt.setConnectAttemptsMax(Integer.parseInt(getConnAttamptMax()));
				mqtt.setReconnectAttemptsMax(Integer.parseInt(getConnReconnAttamptMax()));

				if (!"".equals(getUserNameAuth().trim())) {
					mqtt.setUserName(getUserNameAuth());
				}
				if (!"".equals(getPasswordAuth().trim())) {
					mqtt.setPassword(getPasswordAuth());
				}
				if (MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getMessageType())) {
					payload = Util.generatePayload(Integer.parseInt(getMessageLength()));
				}

				int qos = Integer.parseInt(getQOS());
				switch (qos) {
				case 0:
					qos_enum = QoS.AT_MOST_ONCE;
					break;
				case 1:
					qos_enum = QoS.AT_LEAST_ONCE;
					break;
				case 2:
					qos_enum = QoS.EXACTLY_ONCE;
					break;
				default:
					break;
				}

				connection = mqtt.futureConnection();
				Future<Void> f1 = connection.connect();
				f1.await(Integer.parseInt(getConnTimeout()), TimeUnit.SECONDS);
			} catch (Exception e) {
				logger.log(Priority.ERROR, e.getMessage(), e);
			}
		}
		
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		try {
			byte[] toSend = new byte[]{};
			byte[] tmp = new byte[]{};

			if (MESSAGE_TYPE_HEX_STRING.equals(getMessageType())) {
				tmp = hexToBinary(getMessage());
			} else if (MESSAGE_TYPE_STRING.equals(getMessageType())) {
				tmp = getMessage().getBytes();
			} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getMessageType())) {
				tmp = payload.getBytes();
			}

			topicName = getTopic();
			if (isAddTimestamp()) {
				byte[] timePrefix = (System.currentTimeMillis() + TIME_STAMP_SEP_FLAG).getBytes();
				toSend = new byte[timePrefix.length + tmp.length];
				System.arraycopy(timePrefix, 0, toSend, 0, timePrefix.length);
				System.arraycopy(tmp, 0, toSend, timePrefix.length , tmp.length);
			} else {
				toSend = new byte[tmp.length];
				System.arraycopy(tmp, 0, toSend, 0 , tmp.length);
			}
			result.sampleStart();

			Future<Void> pub = connection.publish(topicName, toSend, qos_enum, false);
			pub.await();

			result.sampleEnd();
			result.setSuccessful(true);
			result.setResponseData((MessageFormat.format("Publish Successful by {0}.", clientId)).getBytes());
			result.setResponseMessage(MessageFormat.format("publish successfully via Connection {0}.", connection));
			result.setResponseCodeOK();
		} catch (Exception ex) {
			logger.log(Priority.ERROR, ex.getMessage(), ex);
			result.sampleEnd();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", connection));
			result.setResponseData("Failed.".getBytes());
			result.setResponseCode("500");
		}
		return result;
	}

	@Override
	public void threadStarted() {
		
	}

	@Override
	public void threadFinished() {
		if (this.connection != null) {
			this.connection.disconnect();
			logger.info(MessageFormat.format("The connection {0} disconneted successfully.", connection));
		}
	}
}
