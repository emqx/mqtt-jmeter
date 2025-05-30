package net.xmeter.samplers.mqtt.paho;

import net.xmeter.samplers.mqtt.*;


public class PahoMQTTSpi implements MQTTSpi {
    @Override
    public MQTTFactory factory() {
        return new PahoMQTTFactory();
    }
}
