package net.xmeter.samplers.mqtt.fuse;

import net.xmeter.samplers.mqtt.MQTTFactory;
import net.xmeter.samplers.mqtt.MQTTSpi;

public class FuseMQTTSpi implements MQTTSpi {
    @Override
    public MQTTFactory factory() {
        return new FuseMQTTFactory();
    }
}
