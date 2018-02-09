package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;

import net.xmeter.Util;

public class PubSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 4312341622759500786L;
	private static final Logger logger = Logger.getLogger(PubSampler.class.getCanonicalName());
	
	private transient CallbackConnection connection = null;
	private String payload = null;
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

	public static byte[] hexToBinary(String hex) {
	    return DatatypeConverter.parseHexBinary(hex);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
	
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (CallbackConnection) vars.getObject("conn");
		if (connection == null) {
			result.setSuccessful(false);
			result.setResponseMessage("Publish: Connection not found.");
			result.setResponseData("Publish failed because connection is not established.".getBytes());
			result.setResponseCode("500");
			return result;
		}
		
		try {
			byte[] toSend = new byte[]{};
			byte[] tmp = new byte[]{};

			if (MESSAGE_TYPE_HEX_STRING.equals(getMessageType())) {
				tmp = hexToBinary(getMessage());
			} else if (MESSAGE_TYPE_STRING.equals(getMessageType())) {
				tmp = getMessage().getBytes();
			} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getMessageType())) {
				if (payload == null) {
					payload = Util.generatePayload(Integer.parseInt(getMessageLength()));
				}
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
			final Object pubLock = new Object();
			PubCallback pubCallback = new PubCallback(pubLock);
			
			if(qos_enum == QoS.AT_MOST_ONCE) { 
				//For QoS == 0, the callback is the same thread with sampler thread, so it cannot use the lock object wait() & notify() in else block;
				//Otherwise the sampler thread will be blocked.
				connection.publish(topicName, toSend, qos_enum, false, pubCallback);
			} else {
				synchronized (pubLock) {
					connection.publish(topicName, toSend, qos_enum, false, pubCallback);
					pubLock.wait();
				}
			}
			
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
			logger.severe(ex.getMessage());
			if (result.getEndTime() == 0) result.sampleEnd();
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", connection));
			result.setResponseData(ex.getMessage().getBytes());
			result.setResponseCode("500");
		}
		return result;
	}
	
}
