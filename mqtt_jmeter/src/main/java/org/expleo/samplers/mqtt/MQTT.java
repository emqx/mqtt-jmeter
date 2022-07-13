package org.expleo.samplers.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MQTT {
    private MQTT() {
        //do nothing
    }

    private static volatile Map<String, MQTTFactory> MQTT_FACTORIES;

    private static Map<String, MQTTFactory> getAvailableFactories() {
        if (MQTT_FACTORIES == null) {
            synchronized (MQTT.class) {
                if (MQTT_FACTORIES == null) {
                    Map<String, MQTTFactory> loaded = StreamSupport.stream(ServiceLoader.load(MQTTSpi.class).spliterator(), false)
                            .map(MQTTSpi::factory)
                            .collect(Collectors.toMap(MQTTFactory::getName, Function.identity()));
                    MQTT_FACTORIES = new TreeMap<>(loaded);
                }
            }
        }
        return MQTT_FACTORIES;
    }

    public static List<String> getAvailableNames() {
        return new ArrayList<>(getAvailableFactories().keySet());
    }

    public static MQTTFactory getInstance(String name) {
        MQTTFactory factory = getAvailableFactories().get(name);
        if (factory == null) {
            throw new IllegalArgumentException("Failed to find MQTTFactory named" + name);
        }
        return factory;
    }

    public static List<String> getSupportedProtocols(String name) {
        MQTTFactory factory = getAvailableFactories().get(name);
        if (factory == null) {
            throw new IllegalArgumentException("Failed to find MQTTFactory named" + name);
        }
        return factory.getSupportedProtocols();
    }
}
