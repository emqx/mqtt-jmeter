package net.xmeter.samplers;

public class SubSampler extends ConnectionSampler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2979978053740194951L;

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
	
	public boolean isDebugResponse() {
		return getPropertyAsBoolean(DEBUG_RESPONSE, false);
	}
	
	public void setDebugResponse(boolean debugResponse) {
		setProperty(DEBUG_RESPONSE, debugResponse);
	}
}
