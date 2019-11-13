package net.xmeter.samplers.mqtt;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class MQTT {
    private MQTT() {
        //do nothing
    }

    private static volatile MQTTFactory MQTT_FACTORY;

    public static MQTTFactory getInstance() {
        if (MQTT_FACTORY == null) {
            synchronized (MQTT.class) {
                if (MQTT_FACTORY == null) {
                    MQTT_FACTORY = StreamSupport.stream(ServiceLoader.load(MQTTSpi.class).spliterator(), false)
                            .findFirst()
                            .map(MQTTSpi::factory)
                            .orElseThrow(() -> new IllegalStateException("No MQTTFactory implementations found"));
                }
            }
        }
        return MQTT_FACTORY;
    }
}
