package org.expleo.samplers.mqtt;

import java.util.function.Consumer;

public interface MQTTConnection {
    boolean isConnectionSucc();
    String getClientId();
    void disconnect() throws Exception;

    MQTTPubResult publish(String topicName, byte[] message, MQTTQoS qoS, boolean retained);

    void subscribe(String[] topicNames, MQTTQoS qos, Runnable onSuccess, Consumer<Throwable> onFailure);

    void setSubListener(MQTTSubListener listener);
}
