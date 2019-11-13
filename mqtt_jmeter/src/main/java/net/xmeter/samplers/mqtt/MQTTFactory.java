package net.xmeter.samplers.mqtt;

import net.xmeter.samplers.AbstractMQTTSampler;

public interface MQTTFactory {
    MQTTClient createClient(ConnectionParameters parameters) throws Exception;
    MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception;
}
