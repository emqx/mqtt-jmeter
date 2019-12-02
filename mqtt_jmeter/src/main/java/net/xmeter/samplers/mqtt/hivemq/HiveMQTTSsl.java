package net.xmeter.samplers.mqtt.hivemq;

import com.hivemq.client.mqtt.MqttClientSslConfig;

import net.xmeter.samplers.mqtt.MQTTSsl;

class HiveMQTTSsl implements MQTTSsl {
    private final MqttClientSslConfig sslConfig;

    HiveMQTTSsl(MqttClientSslConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    MqttClientSslConfig getSslConfig() {
        return sslConfig;
    }
}
