package net.xmeter.samplers;

import org.apache.jmeter.samplers.AbstractSampler;

import net.xmeter.Constants;

public abstract class AbstractMQTTSampler extends AbstractSampler implements Constants {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7163793218595455807L;

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
		return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX_FOR_CONN);
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
}
