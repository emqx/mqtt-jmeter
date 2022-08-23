package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import net.xmeter.Util;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTPubResult;
import net.xmeter.samplers.mqtt.MQTTQoS;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class PubSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 4312341622759500786L;
	private static final Logger logger = Logger.getLogger(PubSampler.class.getCanonicalName());

	private String payload = null;
	private MQTTQoS qos_enum = MQTTQoS.AT_MOST_ONCE;
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
	
	public void setRetainedMessage(Boolean retained) {
		setProperty(RETAINED_MESSAGE, retained);
	}
	
	public Boolean getRetainedMessage() {
		return getPropertyAsBoolean(RETAINED_MESSAGE, false);
	}

	public static byte[] hexToBinary(String hex) {
	    return DatatypeConverter.parseHexBinary(hex);
	}
	
	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
	
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		MQTTConnection connection = (MQTTConnection) vars.getObject(getConnName());
		String clientId = (String) vars.getObject(getConnName()+"_clientId");
		if (connection == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Publish: Connection not found.");
			result.setResponseData("Publish failed because connection is not established.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}
		if (!connection.isConnectionSucc()) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Publish: Connection is broken.");
			result.setResponseData("Publish failed because connection is broken and cannot be used.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd();
			return result;
		}
		
		byte[] toSend = new byte[]{};
		try {
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
				qos_enum = MQTTQoS.AT_MOST_ONCE;
				break;
			case 1:
				qos_enum = MQTTQoS.AT_LEAST_ONCE;
				break;
			case 2:
				qos_enum = MQTTQoS.EXACTLY_ONCE;
				break;
			default:
				break;
			}
			
			topicName = getTopic();
			boolean retainedMsg = getRetainedMessage();
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
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("pub [topic]: " + topicName + ", [payload]: " + new String(toSend));
			}

			MQTTPubResult pubResult = connection.publish(topicName, toSend, qos_enum, retainedMsg);
			
			result.sampleEnd();
			result.setSamplerData(new String(toSend));
			result.setSentBytes(toSend.length);
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(pubResult.isSuccessful());
			
			if(pubResult.isSuccessful()) {
				result.setResponseData("Publish successfuly.".getBytes());
				result.setResponseMessage(MessageFormat.format("publish successfully for Connection {0}.", connection));
				result.setResponseCodeOK();	
			} else {
				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", connection));
				result.setResponseData(MessageFormat.format("Client [{0}] publish failed: {1}", (clientId == null ? "null" : clientId), pubResult.getError().orElse("")).getBytes());
				result.setResponseCode("501");
				logger.info(MessageFormat.format("** [clientId: {0}, topic: {1}, payload: {2}] Publish failed for connection {3}.", (clientId == null ? "null" : clientId),
						topicName, new String(toSend), connection));
				pubResult.getError().ifPresent(logger::info);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Publish failed for connection " + connection, ex);
			if (result.getEndTime() == 0) result.sampleEnd();
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", connection));
			result.setResponseData(MessageFormat.format("Client [{0}] publish failed: {1}", (clientId == null ? "null" : clientId), ex.getMessage()).getBytes());
			result.setResponseCode("502");
			if (logger.isLoggable(Level.INFO)) {
				logger.info(MessageFormat.format("** [clientId: {0}, topic: {1}, payload: {2}] Publish failed for connection {3}.", (clientId == null ? "null" : clientId),
						topicName, new String(toSend), connection));
			}
		}
		return result;
	}
	
}
