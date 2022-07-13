package org.expleo.samplers.mqtt.fuse;

import org.expleo.samplers.mqtt.MQTTFactory;
import org.expleo.samplers.mqtt.MQTTSpi;

public class FuseMQTTSpi implements MQTTSpi {
    @Override
    public MQTTFactory factory() {
        return new FuseMQTTFactory();
    }
}
