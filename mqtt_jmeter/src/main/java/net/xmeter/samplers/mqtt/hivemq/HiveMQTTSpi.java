package net.xmeter.samplers.mqtt.hivemq;

import net.xmeter.samplers.mqtt.MQTTFactory;
import net.xmeter.samplers.mqtt.MQTTSpi;

public class HiveMQTTSpi implements MQTTSpi {

    @Override
    public MQTTFactory factory() {
        return new HiveMQTTFactory();
    }
}
