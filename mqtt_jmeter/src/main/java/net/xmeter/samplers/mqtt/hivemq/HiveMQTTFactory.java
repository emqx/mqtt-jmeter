package net.xmeter.samplers.mqtt.hivemq;

import static net.xmeter.Constants.HIVEMQ_MQTT_CLIENT_NAME;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.TrustManagerFactory;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientSslConfigBuilder;
import com.hivemq.client.util.KeyStoreUtil;

import net.xmeter.AcceptAllTrustManagerFactory;
import net.xmeter.Constants;
import net.xmeter.Util;
import net.xmeter.samplers.AbstractMQTTSampler;
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTFactory;
import net.xmeter.samplers.mqtt.MQTTSsl;

class HiveMQTTFactory implements MQTTFactory {
    private static final Logger logger = Logger.getLogger(HiveMQTTFactory.class.getCanonicalName());

    @Override
    public String getName() {
        return HIVEMQ_MQTT_CLIENT_NAME;
    }

    @Override
    public List<String> getSupportedProtocols() {
        return HiveUtil.ALLOWED_PROTOCOLS;
    }

    @Override
    public MQTTClient createClient(ConnectionParameters parameters) throws Exception {
    	if(parameters.getVersion().equals(Constants.MQTT_VERSION_5)) {
    		return new HiveMQTT5Client(parameters);
    	}
    	
        return new HiveMQTT3Client(parameters);
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
