package net.xmeter.samplers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.samplers.AbstractSampler;

import net.xmeter.Constants;

public abstract class AbstractMQTTSampler extends AbstractSampler implements Constants {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7163793218595455807L;
	
//	protected static final String LABEL_PREFIX = "xmeter-mqtt-batch-con-mode-";
	
//	protected boolean useEfficientCon = Boolean.parseBoolean(System.getProperty("batchCon"));
	protected boolean useEfficientCon = true;
	
//	protected static int conCapacity = 1;
	
	//<connection client id, topics>
	protected static Map<String, Set<String>> topicSubscribed = new ConcurrentHashMap<>();

	public String getServer() {
		return getPropertyAsString(SERVER, DEFAULT_SERVER);
	}

	public void setServer(String server) {
		setProperty(SERVER, server);
	}
	
	public String getMqttVersion() {
		return getPropertyAsString(MQTT_VERSION, DEFAULT_MQTT_VERSION);
	}
	
	public void setMqttVersion(String version) {
		setProperty(MQTT_VERSION, version);
	}

	public String getPort() {
		return getPropertyAsString(PORT, DEFAULT_PORT);
	}

	public void setPort(String port) {
		setProperty(PORT, port);
	}

	public String getConnTimeout() {
		return getPropertyAsString(CONN_TIMEOUT, DEFAULT_CONN_TIME_OUT);
	}

	public void setConnTimeout(String connTimeout) {
		setProperty(CONN_TIMEOUT, connTimeout);
	}

	public String getProtocol() {
		return getPropertyAsString(PROTOCOL, DEFAULT_PROTOCOL);
	}

	public void setProtocol(String protocol) {
		setProperty(PROTOCOL, protocol);
	}

	public String getWsPath() {
		return getPropertyAsString(WS_PATH, "");
	}

	public void setWsPath(String wsPath) {
		setProperty(WS_PATH, wsPath);
	}

	public String getWsHeader() {
		return getPropertyAsString(WS_HEADER, "{}");
	}

	public void setWsHeader(String wsHeader) {
		setProperty(WS_HEADER, wsHeader);
	}

	public boolean isDualSSLAuth() {
		return getPropertyAsBoolean(DUAL_AUTH, false);
	}

	public void setDualSSLAuth(boolean dualSSLAuth) {
		setProperty(DUAL_AUTH, dualSSLAuth);
	}

	public String getKeyStoreFilePath() {
		return getPropertyAsString(CERT_FILE_PATH1, "");
	}

	public void setKeyStoreFilePath(String certFile1) {
		setProperty(CERT_FILE_PATH1, certFile1);
	}

	public String getClientCertFilePath() {
		return getPropertyAsString(CERT_FILE_PATH2, "");
	}

	public void setClientCertFilePath(String certFile2) {
		setProperty(CERT_FILE_PATH2, certFile2);
	}

	public String getKeyStorePassword() {
		return getPropertyAsString(KEY_FILE_PWD1, "");
	}
	
	public void setKeyStorePassword(String keyStorePassword) {
		this.setProperty(KEY_FILE_PWD1, keyStorePassword);
	}

	public String getClientCertPassword() {
		return getPropertyAsString(KEY_FILE_PWD2, "");
	}

	public void setClientCertPassword(String clientCertPassword) {
		this.setProperty(KEY_FILE_PWD2, clientCertPassword);
	}


	public String getConnPrefix() {
		return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX_FOR_CONN);
	}

	public void setConnPrefix(String connPrefix) {
		setProperty(CONN_CLIENT_ID_PREFIX, connPrefix);
	}

	public String getConnKeepAlive() {
		return getPropertyAsString(CONN_KEEP_ALIVE, DEFAULT_CONN_KEEP_ALIVE);
	}

	public void setConnKeepAlive(String connKeepAlive) {
		setProperty(CONN_KEEP_ALIVE, connKeepAlive);
	}
	
	public boolean isClientIdSuffix() {
		return getPropertyAsBoolean(CONN_CLIENT_ID_SUFFIX, DEFAULT_ADD_CLIENT_ID_SUFFIX);
	}
	
	public void setClientIdSuffix(boolean clientIdSuffix) {
		setProperty(CONN_CLIENT_ID_SUFFIX, clientIdSuffix);
	}

	public String getConnAttemptMax() {
		return getPropertyAsString(CONN_ATTEMPT_MAX, DEFAULT_CONN_ATTEMPT_MAX);
	}

	public void setConnAttemptMax(String connAttemptMax) {
		setProperty(CONN_ATTEMPT_MAX, connAttemptMax);
	}

	public String getConnReconnAttemptMax() {
		return getPropertyAsString(CONN_RECONN_ATTEMPT_MAX, DEFAULT_CONN_RECONN_ATTEMPT_MAX);
	}

	public void setConnReconnAttemptMax(String connReconnAttemptMax) {
		setProperty(CONN_RECONN_ATTEMPT_MAX, connReconnAttemptMax);
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
	
	public void setConnCleanSession(String cleanSession) {
		setProperty(CONN_CLEAN_SESSION, cleanSession);
	}
	
	public String getConnCleanSession() {
		return getPropertyAsString(CONN_CLEAN_SESSION, "true");
	}
	
	public void setTopicSubscribed(String clientId, Set<String> topics) {
		topicSubscribed.put(clientId, topics);
	}
	
	public void removeTopicSubscribed(String clientId) {
		topicSubscribed.remove(clientId);
	}
	
	public String getLabelPrefix() {
		String labelPrefix = System.getProperty("batchConLabelPrefix");
		if (labelPrefix == null) {
			labelPrefix = "xmeter-mqtt-batch-con-mode-";
		}
		return labelPrefix;
	}

	public String getMqttClientName() {
		return getPropertyAsString(MQTT_CLIENT_NAME, DEFAULT_MQTT_CLIENT_NAME);
	}

	public void setMqttClientName(String mqttClientName) {
		setProperty(MQTT_CLIENT_NAME, mqttClientName);
	}

	public void setConnCleanStart(String cleanStart) {
		setProperty(CONN_CLEAN_START, cleanStart);
	}

	public String getConnCleanStart() {
		return getPropertyAsString(CONN_CLEAN_START, "true");
	}

	public String getConnSessionExpiryInterval() {
		return getPropertyAsString(CONN_SESSION_EXPIRY_INTERVAL, "0");
	}

	public void setConnSessionExpiryInterval(String sessionExpiryInterval) {
		setProperty(CONN_SESSION_EXPIRY_INTERVAL, sessionExpiryInterval);
	}

	public String getConnUserProperty() {
		return getPropertyAsString(CONN_USER_PROPERTY, "{}");
	}

	public void setConnUserProperty(String connUserProperty) {
		setProperty(CONN_USER_PROPERTY, connUserProperty);
	}

	public String getCAFilePath() {
		return getPropertyAsString("mqtt.ca_file_path", "");
	}

	public void setCAFilePath(String ca) {
		setProperty("mqtt.ca_file_path", ca);
	}

	public String getClientCert2FilePath() {
		return getPropertyAsString("mqtt.client_cert_file_path", "");
	}

	public void setClientCert2FilePath(String cert) {
		setProperty("mqtt.client_cert_file_path", cert);
	}

	public String getClientPrivateKeyFilePath() {
		return getPropertyAsString("mqtt.client_key_file_path", "");
	}

	public void setClientPrivateKeyFilePath(String key) {
		setProperty("mqtt.client_key_file_path", key);
	}

//	public int getConCapacity() {
//		return conCapacity;
//	}
//
//	public void setConCapacity(int conCapacity) {
//		this.conCapacity = conCapacity;
//	}
	
}
