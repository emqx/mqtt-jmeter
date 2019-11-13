package net.xmeter.samplers.mqtt.fuse;

import net.xmeter.Util;
import net.xmeter.samplers.AbstractMQTTSampler;
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTFactory;
import net.xmeter.samplers.mqtt.MQTTSsl;

class FuseMQTTFactory implements MQTTFactory {
    @Override
    public MQTTClient createClient(ConnectionParameters parameters) throws Exception {
        return new FuseMQTTClient(parameters);
    }

    @Override
    public MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception {
        return new FuseMQTTSsl(Util.getContext(sampler));
    }
}
