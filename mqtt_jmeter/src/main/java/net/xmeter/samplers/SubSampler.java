package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.xmeter.SubBean;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTQoS;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

@SuppressWarnings("deprecation")
public class SubSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 2979978053740194951L;
	private static final Logger logger = Logger.getLogger(SubSampler.class.getCanonicalName());
	
	private transient MQTTConnection connection = null;
	private transient String clientId;
	private boolean subFailed = false;

	private int sampleElapsedTime = 1000;
	private int sampleCount = 1;
	private int sampleCountTimeout = 5000;

	private final transient ConcurrentLinkedQueue<SubBean> batches = new ConcurrentLinkedQueue<>();
	private boolean printFlag = false;

	private final transient Object dataLock = new Object();
	private boolean lockReleased = false;

	public String getQOS() {
		return getPropertyAsString(QOS_LEVEL, String.valueOf(QOS_0));
	}

	public void setQOS(String qos) {
		setProperty(QOS_LEVEL, qos);
	}

	public String getTopic() { return getPropertyAsString(TOPIC_NAME, DEFAULT_TOPIC_NAME); }

	public void setTopic(String topicsName) {
		setProperty(TOPIC_NAME, topicsName);
	}
	
	public String getSampleCondition() {
		return getPropertyAsString(SAMPLE_CONDITION, SAMPLE_ON_CONDITION_OPTION1);
	}
	
	public void setSampleCondition(String option) {
		setProperty(SAMPLE_CONDITION, option);
	}
	
	public String getSampleCount() {
		return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_COUNT);
	}
	
	public void setSampleCount(String count) {
		setProperty(SAMPLE_CONDITION_VALUE, count);
	}

	public String getSampleCountTimeout() {
		return getPropertyAsString(SAMPLE_CONDITION_VALUE_OPT, DEFAULT_SAMPLE_VALUE_COUNT_TIMEOUT);
	}

	public void setSampleCountTimeout(String timeout) {
		setProperty(SAMPLE_CONDITION_VALUE_OPT, timeout);
	}

	public String getSampleElapsedTime() {
		return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
	}
	
	public void setSampleElapsedTime(String elapsedTime) {
		setProperty(SAMPLE_CONDITION_VALUE, elapsedTime);
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

	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
	
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (MQTTConnection) vars.getObject(getConnName());
		clientId = (String) vars.getObject(getConnName()+"_clientId");
		if (connection == null) {
			return fillFailedResult(result, "500", "Subscribe failed because connection is not established.");
		}

		// initial values
		boolean sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
		try {
			if (sampleByTime) {
				sampleElapsedTime = Integer.parseUnsignedInt(getSampleElapsedTime());
			} else {
				sampleCount = Integer.parseUnsignedInt(getSampleCount());
				sampleCountTimeout = Integer.parseUnsignedInt(getSampleCountTimeout());
			}
		} catch (NumberFormatException e) {
			return fillFailedResult(result, "510", "Unrecognized value for sample elapsed time or message count.");
		}
		
		if (sampleByTime && sampleElapsedTime <=0 ) {
			return fillFailedResult(result, "511", "Sample on elapsed time: must be greater than 0 ms.");
		} else if (sampleCount < 1) {
			return fillFailedResult(result, "512", "Sample on message count: must be greater than 0.");
		}
		
		String topicsName = getTopic();
		setListener(sampleByTime, sampleCount);
		Set<String> topics = topicsSubscribed.get(clientId);
		if (topics == null) {
			logger.severe("subscribed topics haven't been initiated. [clientId: " + (clientId == null ? "null" : clientId) + "]");
			topics = new HashSet<>();
			topics.add(topicsName);
			topicsSubscribed.put(clientId, topics);
			listenToTopics(topicsName);
		} else {
			if (!topics.contains(topicsName)) {
				topics.add(topicsName);
				topicsSubscribed.put(clientId, topics);
				logger.fine("Listen to topic: " + topicsName);
				listenToTopics(topicsName);
			}
		}
		
		if (subFailed) {
			return fillFailedResult(result, "501", "Failed to subscribe to topic(s):" + topicsName);
		}
		
		if(sampleByTime) {
			try {
				TimeUnit.MILLISECONDS.sleep(sampleElapsedTime);
			} catch (InterruptedException e) {
				logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
			}
			synchronized (dataLock) {
				result.sampleStart();
				return produceResult(result, topicsName);
			}
		} else {
			synchronized (dataLock) {
				int receivedCount = (batches.isEmpty() ? 0 : batches.element().getReceivedCount());
				if (receivedCount < sampleCount) {
					try {
						if (sampleCountTimeout > 0) {
							// handle spurious wakeups (https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Object.html#wait(long,int))
							lockReleased = false;
							long endtime = System.currentTimeMillis() + sampleCountTimeout;
							long currenttime = 0;
							while (!lockReleased && currenttime < endtime) {
								dataLock.wait(sampleCountTimeout);
								currenttime = System.currentTimeMillis();
							}
						} else {
							// handle spurious wakeups (https://docs.oracle.com/en/java/javase/18/docs/api/java.base/java/lang/Object.html#wait(long,int))
							lockReleased = false;
							while (lockReleased == false) {
								dataLock.wait();
							}
						}
					} catch (InterruptedException e) {
						logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
					}
				}
				receivedCount = (batches.isEmpty() ? 0 : batches.element().getReceivedCount());
				if (receivedCount < sampleCount) {
					return fillFailedResult(result, "502", "Failed: No message received on topic: " + topicsName + " (Timeout after " + sampleCountTimeout + "ms)");
				}
				result.sampleStart();
				return produceResult(result, topicsName);
			}
		}
	}
	
	private SampleResult produceResult(SampleResult result, String topicName) {
		SubBean bean = batches.poll();
		if(bean == null) { // In "elapsed time" mode, return "dummy" when time is reached
			bean = new SubBean();
		}
		int receivedCount = bean.getReceivedCount();
		List<String> contents = bean.getContents();
		String message = MessageFormat.format("Received {0} of message.", receivedCount);
		StringBuilder content = new StringBuilder();
		if (isDebugResponse()) {
			for (String s : contents) {
				content.append(s).append("\n");
			}
		}
		result = fillOKResult(result, bean.getReceivedMessageSize(), message, content.toString());
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("sub [topic]: " + topicName + ", [payload]: " + content);
		}
		
		if(receivedCount == 0) {
			result.setEndTime(result.getStartTime()); // dummy result, rectify sample time
		} else {
			if (isAddTimestamp()) {
				result.setEndTime(result.getStartTime() + (long) bean.getAvgElapsedTime()); // rectify sample time
				result.setLatency((long) bean.getAvgElapsedTime());
			} else {
				result.setEndTime(result.getStartTime()); // received messages w/o timestamp, then we cannot reliably calculate elapsed time
			}
		}
		result.setSampleCount(receivedCount);

		return result;
	}
	
	private void listenToTopics(final String topicsName) {
		int qos;
		try {
			qos = Integer.parseInt(getQOS());
		} catch(Exception e) {
			logger.log(Level.SEVERE, e, () -> MessageFormat.format("Specified invalid QoS value {0}, set to default QoS value {1}!", e.getMessage(), QOS_0));
			qos = QOS_0;
		}
		
		final String[] topicNames = topicsName.split(",");
		if(qos < 0 || qos > 2) {
			logger.severe("Specified invalid QoS value, set to default QoS value " + qos);
			qos = QOS_0;
		}

		connection.subscribe(topicNames,
				MQTTQoS.fromValue(qos),
				() -> logger.fine(() -> "successful subscribed topics: " + String.join(", ", topicNames)),
				error -> {
					logger.log(Level.INFO, "subscribe failed", error);
					subFailed = true;
				});
	}
	
	private void setListener(final boolean sampleByTime, final int sampleCount) {
		connection.setSubListener(((topic, message, ack) -> {
			ack.run();

			if(sampleByTime) {
				synchronized (dataLock) {
					handleSubBean(sampleByTime, message, sampleCount);
				}
			} else {
				synchronized (dataLock) {
					SubBean bean = handleSubBean(sampleByTime, message, sampleCount);
					if(bean.getReceivedCount() == sampleCount) {
						lockReleased = true;
						dataLock.notify();
					}
				}
			}
		}));
	}
	
	private SubBean handleSubBean(boolean sampleByTime, String msg, int sampleCount) {
		SubBean bean;
		if(batches.isEmpty()) {
			bean = new SubBean();
			batches.add(bean);
		} else {
			SubBean[] beans = new SubBean[batches.size()];
			batches.toArray(beans);
			bean = beans[beans.length - 1];
		}
		
		if((!sampleByTime) && (bean.getReceivedCount() == sampleCount)) { //Create a new batch when latest bean is full.
			logger.info("The tail bean is full, will create a new bean for it.");
			bean = new SubBean();
			batches.add(bean);
		}
		if (isAddTimestamp()) {
			long now = System.currentTimeMillis();
			int index = msg.indexOf(TIME_STAMP_SEP_FLAG);
			if (index == -1 && (!printFlag)) {
				logger.info(() -> "Payload does not include timestamp: " + msg);
				printFlag = true;
			} else if (index != -1) {
				long start = Long.parseLong(msg.substring(0, index));
				long elapsed = now - start;
				
				double avgElapsedTime = bean.getAvgElapsedTime();
				int receivedCount = bean.getReceivedCount();
				avgElapsedTime = (avgElapsedTime * receivedCount + elapsed) / (receivedCount + 1);
				bean.setAvgElapsedTime(avgElapsedTime);
			}
		}
		if (isDebugResponse()) {
			bean.getContents().add(msg);
		}
		bean.setReceivedMessageSize(bean.getReceivedMessageSize() + msg.length());
		bean.setReceivedCount(bean.getReceivedCount() + 1);
		return bean;
	}

	private SampleResult fillFailedResult(SampleResult result, String code, String message) {
		result.sampleStart();
		result.setResponseCode(code); // 5xx means various failures
		result.setSuccessful(false);
		result.setResponseMessage(message);
		if (clientId != null) {
			result.setResponseData(MessageFormat.format("Client [{0}]: {1}", clientId, message).getBytes());
		} else {
			result.setResponseData(message.getBytes());
		}
		result.sampleEnd();
		
		// avoid massive repeated "early stage" failures in a short period of time
		// which probably overloads JMeter CPU and distorts test metrics such as TPS, avg response time
		try {
			TimeUnit.MILLISECONDS.sleep(SUB_FAIL_PENALTY);
		} catch (InterruptedException e) {
			logger.log(Level.INFO, "Received exception when waiting for notification signal", e);
		}
		return result;
	}

	private SampleResult fillOKResult(SampleResult result, int size, String message, String contents) {
		result.setResponseCode("200");
		result.setSuccessful(true);
		result.setResponseMessage(message);
		result.setBodySize(size);
		result.setBytes(size);
		result.setResponseData(contents.getBytes());
		result.sampleEnd();
		return result;
	}

}
