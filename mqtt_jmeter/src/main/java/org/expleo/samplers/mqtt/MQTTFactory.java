package org.expleo.samplers.mqtt;

import java.util.List;

import org.expleo.samplers.AbstractMQTTSampler;

public interface MQTTFactory {
    String getName();
    List<String> getSupportedProtocols();
    MQTTClient createClient(ConnectionParameters parameters) throws Exception;
    MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception;
}
