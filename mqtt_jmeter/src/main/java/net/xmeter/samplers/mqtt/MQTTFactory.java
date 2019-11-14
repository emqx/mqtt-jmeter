package net.xmeter.samplers.mqtt;

import java.util.List;

import net.xmeter.samplers.AbstractMQTTSampler;

public interface MQTTFactory {
    String getName();
    List<String> getSupportedProtocols();
    MQTTClient createClient(ConnectionParameters parameters) throws Exception;
    MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception;
}
