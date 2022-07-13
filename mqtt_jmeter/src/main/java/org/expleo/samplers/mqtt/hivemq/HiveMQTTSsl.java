package org.expleo.samplers.mqtt.hivemq;

import com.hivemq.client.mqtt.MqttClientSslConfig;

import org.expleo.samplers.mqtt.MQTTSsl;

class HiveMQTTSsl implements MQTTSsl {
    private final MqttClientSslConfig sslConfig;

    HiveMQTTSsl(MqttClientSslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    MqttClientSslConfig getSslConfig() {
        return sslConfig;
    }
}
