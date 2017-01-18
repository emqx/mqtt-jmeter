package net.xmeter.samplers;

public class PubSampler extends ConnectionSampler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4312341622759500786L;

	public int getQOS() {
		return getPropertyAsInt(QOS_LEVEL, QOS_0);
	}

	public void setQOS(int qos) {
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
	
	public int getMessageLength() {
		return getPropertyAsInt(MESSAGE_FIX_LENGTH, DEFAULT_MESSAGE_FIX_LENGTH);
	}
	
	public void setMessageLength(int length) {
		setProperty(MESSAGE_FIX_LENGTH, length);
	}

	public String getMessage() {
		return getPropertyAsString(MESSAGE_TO_BE_SENT, "");
	}
	
	public void setMessage(String message) {
		setProperty(MESSAGE_TO_BE_SENT, message);
	}
}
