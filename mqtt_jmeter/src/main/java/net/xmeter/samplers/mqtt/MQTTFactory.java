package net.xmeter.samplers.mqtt;

import net.xmeter.samplers.AbstractMQTTSampler;

public interface MQTTFactory {
    String getName();
    MQTTClient createClient(ConnectionParameters parameters) throws Exception;
    MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception;
}
