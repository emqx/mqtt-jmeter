package org.expleo.samplers.mqtt.fuse;

import java.util.ArrayList;
import java.util.List;

import org.expleo.Constants;
import org.expleo.samplers.mqtt.MQTTQoS;
import org.fusesource.mqtt.client.QoS;

class FuseUtil {
    static final List<String> ALLOWED_PROTOCOLS;
    static {
        ALLOWED_PROTOCOLS = new ArrayList<>();
        ALLOWED_PROTOCOLS.add(Constants.TCP_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.SSL_PROTOCOL);
    }

    static QoS map(MQTTQoS qos) {
        switch (qos) {
            case AT_MOST_ONCE: return QoS.AT_MOST_ONCE;
            case AT_LEAST_ONCE: return QoS.AT_LEAST_ONCE;
            case EXACTLY_ONCE: return QoS.EXACTLY_ONCE;
            default: throw new IllegalArgumentException("Unknown QoS: " + qos);
        }
    }
}
