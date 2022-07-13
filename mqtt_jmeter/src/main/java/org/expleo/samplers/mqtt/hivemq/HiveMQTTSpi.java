package org.expleo.samplers.mqtt.hivemq;

import org.expleo.samplers.mqtt.MQTTFactory;
import org.expleo.samplers.mqtt.MQTTSpi;

public class HiveMQTTSpi implements MQTTSpi {

    @Override
    public MQTTFactory factory() {
        return new HiveMQTTFactory();
    }
}
