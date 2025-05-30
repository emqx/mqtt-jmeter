package net.xmeter.samplers.mqtt.paho;

import net.xmeter.Constants;
import net.xmeter.Util;
import net.xmeter.samplers.AbstractMQTTSampler;
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTFactory;
import net.xmeter.samplers.mqtt.MQTTSsl;

import java.util.List;

class PahoMQTTFactory implements MQTTFactory {
    @Override
    public String getName() {
        return Constants.PAHO_MQTT_CLIENT_NAME;
    }

    @Override
    public List<String> getSupportedProtocols() {
        return PahoUtil.ALLOWED_PROTOCOLS;
    }

    @Override
    public MQTTClient createClient(ConnectionParameters parameters) throws Exception {
        String mqttVersion = parameters.getVersion();
        if (Constants.MQTT_VERSION_3_1.equals(mqttVersion) || Constants.MQTT_VERSION_3_1_1.equals(mqttVersion)){
            return new PahoMQTT3Client(parameters);
        }
        else {
            return new PahoMQTT5Client(parameters);
        }
    }

    @Override
    public MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception {
        if (!sampler.isDualSSLAuth()){
            return  new PahoMQTTSsl("","","");
        } else {
            return  new PahoMQTTSsl(
                    sampler.getCAFilePath(),
                    sampler.getClientCert2FilePath(),
                    sampler.getClientPrivateKeyFilePath());
        }
    }
}
