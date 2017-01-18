package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import net.xmeter.Constants;
import net.xmeter.Util;

public class ConnectionSampler extends AbstractSampler implements Constants, TestStateListener, ThreadListener {
	private static Logger logger = LoggingManager.getLoggerForClass();
	private MQTT mqtt = new MQTT();
	private FutureConnection connection = null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1859006013465470528L;

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		try {
			if(!DEFAULT_PROTOCOL.equals(getProtocol())) {
				mqtt.setSslContext(Util.getContext(this));
			}
			
			mqtt.setHost(getProtocol().toLowerCase() + "://" + getServer() + ":" + getPort());
			mqtt.setKeepAlive((short) getConnKeepAlive());
			String clientId = Util.generateClientId(getConnPrefix());
			mqtt.setClientId(clientId);
			mqtt.setConnectAttemptsMax(getConnAttamptMax());
			mqtt.setReconnectAttemptsMax(getConnReconnAttamptMax());
			
			if(!"".equals(getUserNameAuth().trim())) {
				mqtt.setUserName(getUserNameAuth());
			}
			if(!"".equals(getPasswordAuth().trim())) {
				mqtt.setPassword(getPasswordAuth());
			}
			
			result.sampleStart(); 
			connection = mqtt.futureConnection();
			Future<Void> f1 = connection.connect();
			f1.await(getConnTimeout(), TimeUnit.SECONDS);
			
			Topic[] topics = {new Topic("topic_"+ clientId, QoS.AT_LEAST_ONCE)};
			connection.subscribe(topics);
			
			result.sampleEnd(); 
            result.setSuccessful(true);
            result.setResponseData("Successful.".getBytes());
            result.setResponseMessage(MessageFormat.format("Connection {0} connected successfully.", connection));
            result.setResponseCodeOK(); 
		} catch(Exception e) {
			logger.log(Priority.ERROR, e.getMessage(), e);
			result.sampleEnd(); 
            result.setSuccessful(false);
            result.setResponseMessage(MessageFormat.format("Connection {0} connected failed.", connection));
            result.setResponseData("Failed.".getBytes());
            result.setResponseCode("500"); 
		}
		return result;
	}

	public String getServer() {
		return getPropertyAsString(SERVER, DEFAULT_SERVER);
	}

	public void setServer(String server) {
		setProperty(SERVER, server);
	}

	public int getPort() {
		return getPropertyAsInt(PORT, DEFAULT_PORT);
	}

	public void setPort(int port) {
		setProperty(PORT, port);
	}

	public int getConnTimeout() {
		return getPropertyAsInt(CONN_TIMEOUT, DEFAULT_CONN_TIME_OUT);
	}

	public void setConnTimeout(int connTimeout) {
		setProperty(CONN_TIMEOUT, connTimeout);
	}

	public String getProtocol() {
		return getPropertyAsString(PROTOCOL, DEFAULT_PROTOCOL);
	}

	public void setProtocol(String protocol) {
		setProperty(PROTOCOL, protocol);
	}

	public boolean isDualSSLAuth() {
		return getPropertyAsBoolean(DUAL_AUTH, false);
	}

	public void setDualSSLAuth(boolean dualSSLAuth) {
		setProperty(DUAL_AUTH, dualSSLAuth);
	}

	public String getCertFile1() {
		return getPropertyAsString(CERT_FILE_PATH1, "");
	}

	public void setCertFile1(String certFile1) {
		setProperty(CERT_FILE_PATH1, certFile1);
	}

	public String getCertFile2() {
		return getPropertyAsString(CERT_FILE_PATH2, "");
	}

	public void setCertFile2(String certFile2) {
		setProperty(CERT_FILE_PATH2, certFile2);
	}

	public String getKeyFileUsrName() {
		return getPropertyAsString(KEY_FILE_USR_NAME, "");
	}

	public void setKeyFileUsrName(String keyFileUsrName) {
		this.setProperty(KEY_FILE_USR_NAME, keyFileUsrName);
	}

	public String getKeyFilePassword() {
		return getPropertyAsString(KEY_FILE_PWD, "");
	}

	public void setKeyFilePassword(String keyFilePassword) {
		this.setProperty(KEY_FILE_PWD, keyFilePassword);
	}

	public String getConnPrefix() {
		return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX);
	}

	public void setConnPrefix(String connPrefix) {
		setProperty(CONN_CLIENT_ID_PREFIX, connPrefix);
	}

	public int getConnKeepAlive() {
		return getPropertyAsInt(CONN_KEEP_ALIVE, DEFAULT_CONN_KEEP_ALIVE);
	}

	public void setConnKeepAlive(int connKeepAlive) {
		setProperty(CONN_KEEP_ALIVE, connKeepAlive);
	}

	public int getConnKeepTime() {
		return getPropertyAsInt(CONN_KEEP_TIME, DEFAULT_CONN_KEEP_TIME);
	}

	public void setConnKeepTime(int connKeepTime) {
		setProperty(CONN_KEEP_TIME, connKeepTime);
	}

	public int getConnAttamptMax() {
		return getPropertyAsInt(CONN_ATTAMPT_MAX, DEFAULT_CONN_ATTAMPT_MAX);
	}

	public void setConnAttamptMax(int connAttamptMax) {
		setProperty(CONN_ATTAMPT_MAX, connAttamptMax);
	}

	public int getConnReconnAttamptMax() {
		return getPropertyAsInt(CONN_RECONN_ATTAMPT_MAX, DEFAULT_CONN_RECONN_ATTAMPT_MAX);
	}

	public void setConnReconnAttamptMax(int connReconnAttamptMax) {
		setProperty(CONN_RECONN_ATTAMPT_MAX, connReconnAttamptMax);
	}

	public String getUserNameAuth() {
		return getPropertyAsString(USER_NAME_AUTH, "");
	}

	public void setUserNameAuth(String userName) {
		setProperty(USER_NAME_AUTH, userName);
	}
	
	public String getPasswordAuth() {
		return getPropertyAsString(PASSWORD_AUTH, "");
	}

	public void setPasswordAuth(String password) {
		setProperty(PASSWORD_AUTH, password);
	}
	
	@Override
	public void testEnded() {
		this.testEnded("local");
	}

	@Override
	public void testEnded(String arg0) {
		
	}

	@Override
	public void testStarted() {
		
	}

	@Override
	public void testStarted(String arg0) {
		
	}

	@Override
	public void threadFinished() {
		try {
			TimeUnit.SECONDS.sleep(getConnKeepTime());	
			if(connection != null) {
				connection.disconnect();
				logger.log(Priority.INFO, MessageFormat.format("The connection {0} disconneted successfully.", connection));	
			}
		} catch (InterruptedException e) {
			logger.log(Priority.ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void threadStarted() {
		
	}
	
}
