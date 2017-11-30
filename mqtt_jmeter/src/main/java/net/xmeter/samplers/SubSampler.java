package net.xmeter.samplers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import net.xmeter.SubBean;
import net.xmeter.Util;

@SuppressWarnings("deprecation")
public class SubSampler extends AbstractMQTTSampler implements ThreadListener {
	private transient MQTT mqtt = new MQTT();
	private transient CallbackConnection connection = null;
	private transient static Logger logger = LoggingManager.getLoggerForClass();

	private boolean connectFailed = false;
	private boolean subFailed = false;
	private boolean receivedMsgFailed = false;

	private transient ConcurrentLinkedQueue<SubBean> batches = new ConcurrentLinkedQueue<>();
	private boolean printFlag = false;

	private transient Object dataLock = new Object();
	
	private int qos = QOS_0;
	private String connKey = "";
	/**
	 * 
	 */
	private static final long serialVersionUID = 2979978053740194951L;

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
		return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC);
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
			setProperty(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC);
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
		final boolean sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
		final int sampleCount = Integer.parseInt(getSampleCount());
		connKey = getKey();
		if(connection == null) {
			connection = ConnectionsManager.getInstance().getConnection(connKey);
			final String topicName= getTopic();
			if(connection != null) {
				logger.info("Use the shared connection: " + connection);
				setListener(sampleByTime, sampleCount);
				listenToTopics(topicName);
			} else {
				 // first loop, initializing ..
				try {
					if (!DEFAULT_PROTOCOL.equals(getProtocol())) {
						mqtt.setSslContext(Util.getContext(this));
					}
					
					mqtt.setHost(getProtocol().toLowerCase() + "://" + getServer() + ":" + getPort());
					mqtt.setVersion(getMqttVersion());
					mqtt.setKeepAlive((short) Integer.parseInt(getConnKeepAlive()));
		
					String clientId = null;
					if(isClientIdSuffix()) {
						clientId = Util.generateClientId(getConnClientId());
					} else {
						clientId = getConnClientId();
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
					
					connection = ConnectionsManager.getInstance().createConnection(connKey, mqtt);
					setListener(sampleByTime, sampleCount);
					connection.connect(new Callback<Void>() {
						@Override
						public void onSuccess(Void value) {
							listenToTopics(topicName);
							ConnectionsManager.getInstance().setConnectionStatus(connKey, true);
						}
		
						@Override
						public void onFailure(Throwable value) {
							connectFailed = true;
							ConnectionsManager.getInstance().setConnectionStatus(connKey, false);
						}
					});
				} catch (Exception e) {
					logger.log(Priority.ERROR, e.getMessage(), e);
				}
			
			}
		}
		
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		
		result.sampleStart();
		if (connectFailed) {
			return fillFailedResult(sampleByTime, result, MessageFormat.format("Connection {0} connected failed.", connection));
		} else if (subFailed) {
			return fillFailedResult(sampleByTime, result, "Failed to subscribe to topic.");
		} else if (receivedMsgFailed) {
			return fillFailedResult(sampleByTime, result, "Failed to receive message.");
		}
		
		if(sampleByTime) {
			try {
				TimeUnit.MILLISECONDS.sleep(Long.parseLong(getSampleElapsedTime()));
			} catch (InterruptedException e) {
				logger.info("Received exception when waiting for notification signal: " + e.getMessage());
			}
			synchronized (dataLock) {
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
				return produceResult(result);
			}
		}
	}
	
	private SampleResult produceResult(SampleResult result) {
		SubBean bean = batches.poll();
		if(bean == null) { //In case selected with time interval
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
			result.setEndTime(result.getStartTime());
		} else {
			if (isAddTimestamp()) {
				result.setEndTime(result.getStartTime() + (long) bean.getAvgElapsedTime());
				result.setLatency((long) bean.getAvgElapsedTime());
			} else {
				result.setEndTime(result.getStartTime());	
			}
		}
		result.setSampleCount(receivedCount);

		return result;
	}
	
	private void listenToTopics(final String topicName) {
		try {
			qos = Integer.parseInt(getQOS());
		} catch(Exception ex) {
			logger.error(MessageFormat.format("Specified invalid QoS value {0}, set to default QoS value {1}!", ex.getMessage(), qos));
			qos = QOS_0;
		}
		Topic[] topics = new Topic[1];
		if(qos < 0 || qos > 2) {
			logger.error("Specified invalid QoS value, set to default QoS value " + qos);
			qos = QOS_0;
		}
		if (qos == QOS_0) {
			topics[0] = new Topic(topicName, QoS.AT_MOST_ONCE);
		} else if (qos == QOS_1) {
			topics[0] = new Topic(topicName, QoS.AT_LEAST_ONCE);
		} else {
			topics[0] = new Topic(topicName, QoS.EXACTLY_ONCE);
		}

		connection.subscribe(topics, new Callback<byte[]>() {
			@Override
			public void onSuccess(byte[] value) {
				logger.info("sub successful, topic is " + topicName);
			}

			@Override
			public void onFailure(Throwable value) {
				subFailed = true;
				connection.kill(null);
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
							//logger.info(System.currentTimeMillis() + ": need notify? receivedCount=" + bean.getReceivedCount() + ", sampleCount=" + sampleCount);
							if(bean.getReceivedCount() == sampleCount) {
								dataLock.notify();
							}
						}
					}
				} catch (IOException e) {
					logger.log(Priority.ERROR, e.getMessage(), e);
				}
			}

			@Override
			public void onFailure(Throwable value) {
				connectFailed = true;
				connection.kill(null);
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

	@Override
	public void threadStarted() {
		//logger.info("*** in threadStarted");
		boolean sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
		if(!sampleByTime) {
			logger.info("Configured with sampled on message count, will not check message sent time.");
			return;
		}
	}
	
	@Override
	public void threadFinished() {
		//logger.info(System.currentTimeMillis() + ", threadFinished");
		//logger.info("*** in threadFinished");
		this.connection.disconnect(new Callback<Void>() {
			@Override
			public void onSuccess(Void value) {
				logger.info(MessageFormat.format("Connection {0} disconnect successfully.", connection));
			}

			@Override
			public void onFailure(Throwable value) {
				logger.info(MessageFormat.format("Connection {0} failed to disconnect.", connection));
			}
		});
		if(ConnectionsManager.getInstance().containsConnection(connKey)) {
			ConnectionsManager.getInstance().removeConnection(connKey);	
		}
	}
}
