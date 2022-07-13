package org.expleo.samplers.mqtt;

@FunctionalInterface
public interface MQTTSubListener {
    void accept(String topic, String message, Runnable ack);
}
