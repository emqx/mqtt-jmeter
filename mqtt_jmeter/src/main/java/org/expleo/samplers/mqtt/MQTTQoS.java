package org.expleo.samplers.mqtt;

public enum MQTTQoS {
    AT_MOST_ONCE,
    AT_LEAST_ONCE,
    EXACTLY_ONCE
    ;

    public static MQTTQoS fromValue(int value) {
        return values()[value];
    }
}
