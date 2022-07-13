package org.expleo.samplers.mqtt.hivemq;

import java.util.ArrayList;
import java.util.List;

import com.hivemq.client.mqtt.datatypes.MqttQos;

import org.expleo.Constants;
import org.expleo.samplers.mqtt.MQTTQoS;

class HiveUtil {
    static final List<String> ALLOWED_PROTOCOLS;
    static {
        ALLOWED_PROTOCOLS = new ArrayList<>();
        ALLOWED_PROTOCOLS.add(Constants.TCP_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.SSL_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.WS_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.WSS_PROTOCOL);
    }

    static MqttQos map(MQTTQoS qos) {
        switch (qos) {
            case AT_MOST_ONCE: return MqttQos.AT_MOST_ONCE;
            case AT_LEAST_ONCE: return MqttQos.AT_LEAST_ONCE;
            case EXACTLY_ONCE: return MqttQos.EXACTLY_ONCE;
            default: throw new IllegalArgumentException("Unknown QoS: " + qos);
        }
    }
}
