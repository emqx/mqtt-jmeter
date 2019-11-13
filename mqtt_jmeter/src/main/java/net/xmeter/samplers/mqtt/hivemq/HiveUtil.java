package net.xmeter.samplers.mqtt.hivemq;

import com.hivemq.client.mqtt.datatypes.MqttQos;

import net.xmeter.samplers.mqtt.MQTTQoS;

class HiveUtil {
    static MqttQos map(MQTTQoS qos) {
        switch (qos) {
            case AT_MOST_ONCE: return MqttQos.AT_MOST_ONCE;
            case AT_LEAST_ONCE: return MqttQos.AT_LEAST_ONCE;
            case EXACTLY_ONCE: return MqttQos.EXACTLY_ONCE;
            default: throw new IllegalArgumentException("Unknown QoS: " + qos);
        }
    }
}
