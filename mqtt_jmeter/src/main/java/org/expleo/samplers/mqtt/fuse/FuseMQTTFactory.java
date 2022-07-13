package org.expleo.samplers.mqtt.fuse;

import java.util.List;

import org.expleo.Constants;
import org.expleo.Util;
import org.expleo.samplers.AbstractMQTTSampler;
import org.expleo.samplers.mqtt.ConnectionParameters;
import org.expleo.samplers.mqtt.MQTTClient;
import org.expleo.samplers.mqtt.MQTTFactory;
import org.expleo.samplers.mqtt.MQTTSsl;

class FuseMQTTFactory implements MQTTFactory {
    @Override
    public String getName() {
        return Constants.FUSESOURCE_MQTT_CLIENT_NAME;
    }

    @Override
    public List<String> getSupportedProtocols() {
        return FuseUtil.ALLOWED_PROTOCOLS;
    }

    @Override
    public MQTTClient createClient(ConnectionParameters parameters) throws Exception {
        return new FuseMQTTClient(parameters);
    }

    @Override
    public MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception {
        return new FuseMQTTSsl(Util.getContext(sampler));
    }
}
