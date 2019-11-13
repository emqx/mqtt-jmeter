package net.xmeter.samplers.mqtt.hivemq;

import java.io.File;
import java.util.Collections;
import java.util.logging.Logger;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientSslConfigBuilder;
import com.hivemq.client.util.KeyStoreUtil;

import net.xmeter.Util;
import net.xmeter.samplers.AbstractMQTTSampler;
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTFactory;
import net.xmeter.samplers.mqtt.MQTTSsl;

class HiveMQTTFactory implements MQTTFactory {
    private static final Logger logger = Logger.getLogger(HiveMQTTFactory.class.getCanonicalName());

    @Override
    public MQTTClient createClient(ConnectionParameters parameters) throws Exception {
        return new HiveMQTTClient(parameters);
    }

    @Override
    public MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception {
        MqttClientSslConfigBuilder sslBuilder = MqttClientSslConfig.builder()
                .protocols(Collections.singletonList("TLSv1.2"));

        //TODO: cert file path is not handled
        if (sampler.isDualSSLAuth()) {
            logger.info("Configured with dual SSL, trying to load key store.");
            File keyStoreFile = Util.getKeyStoreFile(sampler);
            String keyStorePass = sampler.getKeyStorePassword();
            String certPass = sampler.getClientCertPassword();
            sslBuilder = sslBuilder.keyManagerFactory(KeyStoreUtil.keyManagerFromKeystore(keyStoreFile, keyStorePass, certPass));
        }
        return new HiveMQTTSsl(sslBuilder.build());
    }
}
