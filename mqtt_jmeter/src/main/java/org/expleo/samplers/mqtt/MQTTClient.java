package org.expleo.samplers.mqtt;

public interface MQTTClient {
    String getClientId();
    MQTTConnection connect() throws Exception;
}
