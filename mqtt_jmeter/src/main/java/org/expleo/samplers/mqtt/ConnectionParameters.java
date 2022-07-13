package org.expleo.samplers.mqtt;

import org.expleo.Util;

public class ConnectionParameters {
    private MQTTSsl ssl;
    private String protocol;
    private String host;
    private int port;
    private String version;
    private short keepAlive;
    private String clientId;
    private int connectMaxAttempts;
    private int reconnectMaxAttempts;
    private int connectTimeout;
    private String username;
    private String password;
    private boolean cleanSession;
    private String path;

    public MQTTSsl getSsl() {
        return ssl;
    }

    public void setSsl(MQTTSsl ssl) {
        this.ssl = ssl;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public short getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(short keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getConnectMaxAttempts() {
        return connectMaxAttempts;
    }

    public void setConnectMaxAttempts(int connectMaxAttempts) {
        this.connectMaxAttempts = connectMaxAttempts;
    }

    public int getReconnectMaxAttempts() {
        return reconnectMaxAttempts;
    }

    public void setReconnectMaxAttempts(int reconnectMaxAttempts) {
        this.reconnectMaxAttempts = reconnectMaxAttempts;
    }


    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecureProtocol() {
        return Util.isSecureProtocol(getProtocol());
    }

    public boolean isWebSocketProtocol() {
        return Util.isWebSocketProtocol(getProtocol());
    }
}
