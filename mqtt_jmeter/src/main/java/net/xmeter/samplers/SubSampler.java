package net.xmeter.samplers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import net.xmeter.SubBean;

@SuppressWarnings("deprecation")
public class SubSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 2979978053740194951L;
	private static final Logger logger = Logger.getLogger(SubSampler.class.getCanonicalName());
	
	private transient CallbackConnection connection = null;
	private boolean subFailed = false;

	private transient ConcurrentLinkedQueue<SubBean> batches = new ConcurrentLinkedQueue<>();
	private boolean printFlag = false;

	private transient Object dataLock = new Object();
	
	private int qos = QOS_0;

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
		try {
			int temp = Integer.parseInt(count);
			if(temp < 1) {
				logger.info("Invalid sample message count value.");
				throw new IllegalArgumentException();
			}
			setProperty(SAMPLE_CONDITION_VALUE, count);
		} catch(Exception ex) {
			logger.info("Invalid count value, set to default value.");
			setProperty(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_COUNT);
		}
	}
	
	public String getSampleElapsedTime() {
		return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
	}
	
	public void setSampleElapsedTime(String elapsedTime) {
		try {
			int temp = Integer.parseInt(elapsedTime);
			if(temp <= 0) {
				throw new IllegalArgumentException();
			}
			setProperty(SAMPLE_CONDITION_VALUE, elapsedTime);
		}catch(Exception ex) {
			logger.info("Invalid elapsed time value, set to default value: " + elapsedTime);
			setProperty(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
		}
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

	public String getConnClientId() {
		return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX_FOR_SUB);
	}

	@Override
	public SampleResult sample(Entry arg0) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
	
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (CallbackConnection) vars.getObject("conn");
		if (connection == null) {
			result.setSuccessful(false);
			result.setResponseMessage("Subscribe: Connection not found.");
			result.setResponseData("Subscribe failed because connection is not established.".getBytes());
			result.setResponseCode("500");
			return result;
		}
		
		final boolean sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
		final int sampleCount = Integer.parseInt(getSampleCount());
		final String topicsName= getTopics();
		setListener(sampleByTime, sampleCount);
		listenToTopics(topicsName);  // TODO: run once or multiple times ?
		
		if (subFailed) {
			result.sampleStart();
			return fillFailedResult(sampleByTime, result, "Failed to subscribe to topic.");
		}
		
		if(sampleByTime) {
			try {
				TimeUnit.MILLISECONDS.sleep(Long.parseLong(getSampleElapsedTime()));
			} catch (InterruptedException e) {
				logger.info("Received exception when waiting for notification signal: " + e.getMessage());
			}
			synchronized (dataLock) {
				result.sampleStart();
				return produceResult(result);	
			}
		} else {
			synchronized (dataLock) {
				int receivedCount1 = (batches.isEmpty() ? 0 : batches.element().getReceivedCount());;
				boolean needWait = false;
				if(receivedCount1 < sampleCount) {
					needWait = true;
				}
				
				//logger.info(System.currentTimeMillis() + ": need wait? receivedCount=" + receivedCount + ", sampleCount=" + sampleCount);
				if(needWait) {
					try {
						dataLock.wait();
					} catch (InterruptedException e) {
						logger.info("Received exception when waiting for notification signal: " + e.getMessage());
					}
				}
				result.sampleStart();
				return produceResult(result);
			}
		}
	}
	
	private SampleResult produceResult(SampleResult result) {
		SubBean bean = batches.poll();
		if(bean == null) { //In "elapsed time" mode, return "dummy" when time is reached
			bean = new SubBean();
		}
		int receivedCount = bean.getReceivedCount();
		List<String> contents = bean.getContents();
		String message = MessageFormat.format("Received {0} of message\n.", receivedCount);
		StringBuffer content = new StringBuffer("");
		if (isDebugResponse()) {
			for (int i = 0; i < contents.size(); i++) {
				content.append(contents.get(i) + " \n");
			}
		}
		result = fillOKResult(result, bean.getReceivedMessageSize(), message, content.toString());
		
		if(receivedCount == 0) {
			result.setEndTime(result.getStartTime()); // dummy result, rectify sample time
		} else {
			if (isAddTimestamp()) {
				result.setEndTime(result.getStartTime() + (long) bean.getAvgElapsedTime()); // rectify sample time
				result.setLatency((long) bean.getAvgElapsedTime());
			} else {
				result.setEndTime(result.getStartTime()); // TODO: any better solution?
			}
		}
		result.setSampleCount(receivedCount);

		return result;
	}
	
	private void listenToTopics(final String topicsName) {
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
				logger.info("sub successful, topic length is " + paraTopics.length);
			}

			@Override
			public void onFailure(Throwable value) {
				subFailed = true;
			}
		});
	}
	
	private void setListener(final boolean sampleByTime, final int sampleCount) {
		connection.listener(new Listener() {
			@Override
			public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					body.writeTo(baos);
					String msg = baos.toString();
					ack.run();
					
					if(sampleByTime) {
						synchronized (dataLock) {
							handleSubBean(sampleByTime, msg, sampleCount);
						}
					} else {
						synchronized (dataLock) {
							SubBean bean = handleSubBean(sampleByTime, msg, sampleCount);
							if(bean.getReceivedCount() == sampleCount) {
								dataLock.notify();
							}
						}
					}
				} catch (IOException e) {
					logger.severe(e.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable value) {
			}

			@Override
			public void onDisconnected() {
			}

			@Override
			public void onConnected() {
			}
		});
	}
	
	private SubBean handleSubBean(boolean sampleByTime, String msg, int sampleCount) {
		SubBean bean = null;
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
				logger.info("Payload does not include timestamp: " + msg);
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

	private SampleResult fillFailedResult(boolean sampleByTime, SampleResult result, String message) {
		result.setResponseCode("500");
		result.setSuccessful(false);
		result.setResponseMessage(message);
		result.setResponseData(message.getBytes());
		result.setEndTime(result.getStartTime());
		
		if(sampleByTime) {
			try {
				TimeUnit.MILLISECONDS.sleep(Long.parseLong(getSampleElapsedTime()));
			} catch (InterruptedException e) {
				logger.info("Received exception when waiting for notification signal: " + e.getMessage());
			}
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
