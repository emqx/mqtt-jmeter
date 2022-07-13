package org.expleo.samplers.mqtt.hivemq;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.TrustManagerFactory;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientSslConfigBuilder;
import com.hivemq.client.util.KeyStoreUtil;

import org.expleo.AcceptAllTrustManagerFactory;
import org.expleo.Util;
import org.expleo.samplers.AbstractMQTTSampler;
import org.expleo.samplers.mqtt.ConnectionParameters;
import org.expleo.samplers.mqtt.MQTTClient;
import org.expleo.samplers.mqtt.MQTTFactory;
import org.expleo.samplers.mqtt.MQTTSsl;
import org.expleo.Constants;

class HiveMQTTFactory implements MQTTFactory {
    private static final Logger logger = Logger.getLogger(HiveMQTTFactory.class.getCanonicalName());

    @Override
    public String getName() {
        return Constants.HIVEMQ_MQTT_CLIENT_NAME;
    }

    @Override
    public List<String> getSupportedProtocols() {
        return HiveUtil.ALLOWED_PROTOCOLS;
    }

    @Override
    public MQTTClient createClient(ConnectionParameters parameters) throws Exception {
        return new HiveMQTTClient(parameters);
    }

    @Override
    public MQTTSsl createSsl(AbstractMQTTSampler sampler) throws Exception {
        MqttClientSslConfigBuilder sslBuilder = MqttClientSslConfig.builder()
                .protocols(Collections.singletonList("TLSv1.2"));

        //As the purpose is server performance testing, we make the assumption that 
        //server side certificate is always valid.
        if (!sampler.isDualSSLAuth()) {
        		logger.info("Configured with non-dual SSL.");
        		TrustManagerFactory acceptAllTmFactory = AcceptAllTrustManagerFactory.getInstance();
        		sslBuilder = sslBuilder.trustManagerFactory(acceptAllTmFactory);
        } else {
        		logger.info("Configured with dual SSL, trying to load client certification.");
//        		File keyStoreFile = Util.getKeyStoreFile(sampler);
//        		String keyStorePass = sampler.getKeyStorePassword();
        		File clientCertFile = Util.getClientCertFile(sampler);
        		String clientPass = sampler.getClientCertPassword();
        		sslBuilder = sslBuilder.keyManagerFactory(KeyStoreUtil.keyManagerFromKeystore(clientCertFile, clientPass, clientPass))
        				.trustManagerFactory(AcceptAllTrustManagerFactory.getInstance());
        }
        return new HiveMQTTSsl(sslBuilder.build());
    }
}
