package net.xmeter.samplers.mqtt;

@FunctionalInterface
public interface MQTTSubListener {
    void accept(String topic, String message, Runnable ack);
}
