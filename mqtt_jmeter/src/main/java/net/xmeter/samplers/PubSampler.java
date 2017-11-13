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
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
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
	private transient CallbackConnection connection = null;
	private String payload = null;
	private String clientId = "";
	private QoS qos_enum = QoS.AT_MOST_ONCE;
	private String topicName = "";
	private String connKey = "";

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
	public boolean isConnectionShareShow() {
		return true;
	}
	
	private String getKey() {
		String key = getThreadName();
		if(!isConnectionShare()) {
			key = new String(getThreadName() + this.hashCode());
		}
		return key;
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		this.connKey = getKey();
		if(connection == null) {
			connection = ConnectionsManager.getInstance().getConnection(connKey);
			if(connection != null) {
				logger.info("Use the shared connection: " + connection);
			} else {
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

					Object connLock = new Object();
					connection = ConnectionsManager.getInstance().createConnection(connKey, mqtt);
					synchronized (connLock) {
						connection.connect(new ConnectionCallback(connection, connLock));
						connLock.wait(TimeUnit.SECONDS.toMillis(Integer.parseInt(getConnTimeout())));
					}
				} catch (Exception e) {
					logger.log(Priority.ERROR, e.getMessage(), e);
				}
			}
		}
		
		if(payload == null) {
			if (MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getMessageType())) {
				payload = Util.generatePayload(Integer.parseInt(getMessageLength()));
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
			PubCallback pubCallback = new PubCallback();
			connection.publish(topicName, toSend, qos_enum, false, pubCallback);
			
			result.sampleEnd();
			result.setSamplerData(new String(toSend));
			result.setSentBytes(toSend.length);
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(pubCallback.isSuccessful());
			if(pubCallback.isSuccessful()) {
				result.setResponseData("Publish successfuly.".getBytes());
				result.setResponseMessage(MessageFormat.format("publish successfully for Connection {0}.", connection));
				result.setResponseCodeOK();	
			} else {
				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", connection));
				result.setResponseData("Publish failed.".getBytes());
				result.setResponseCode("500");
			}
		} catch (Exception ex) {
			logger.log(Priority.ERROR, ex.getMessage(), ex);
			result.sampleEnd();
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", connection));
			result.setResponseData(ex.getMessage().getBytes());
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
			this.connection.disconnect(new Callback<Void>() {
				
				@Override
				public void onSuccess(Void value) {
					logger.info(MessageFormat.format("The connection {0} disconneted successfully.", connection));		
				}
				
				@Override
				public void onFailure(Throwable value) {
					logger.log(Priority.ERROR, value.getMessage(), value);
				}
			});
		}
		if(ConnectionsManager.getInstance().containsConnection(connKey)) {
			ConnectionsManager.getInstance().removeConnection(connKey);	
		}
	}
}
