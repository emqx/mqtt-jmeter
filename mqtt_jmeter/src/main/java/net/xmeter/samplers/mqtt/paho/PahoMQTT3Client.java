package net.xmeter.samplers.mqtt.paho;

import net.xmeter.Constants;
import net.xmeter.samplers.mqtt.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import java.util.function.Consumer;
import java.util.logging.Logger;

public class PahoMQTT3Client implements MQTTClient {

    private static final Logger logger = Logger.getLogger(PahoMQTT3Client.class.getCanonicalName());
    private final ConnectionParameters parameters;
    private final MqttAsyncClient client;

    PahoMQTT3Client(ConnectionParameters parameters) throws MqttException {
        this.parameters = parameters;
        MemoryPersistence persistence = new MemoryPersistence();
        this.client = new MqttAsyncClient(PahoUtil.createHostAddress(parameters), this.getClientId(), persistence);
    }


    @Override
    public String getClientId() {
        return parameters.getClientId();
    }

    @Override
    public MQTTConnection connect() throws Exception {

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(parameters.isCleanSession());
        connectOptions.setKeepAliveInterval(parameters.getKeepAlive());
        connectOptions.setConnectionTimeout(parameters.getConnectTimeout());

        if (parameters.getVersion().equals(Constants.MQTT_VERSION_3_1_1)) {
            connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        } else if (parameters.getVersion().equals(Constants.MQTT_VERSION_3_1)) {
            connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        }

        if (parameters.shouldAutomaticReconnectWithDefaultConfig()) {
            connectOptions.setAutomaticReconnect(true);
        }
        if (parameters.getUsername() != null) {
            connectOptions.setUserName(parameters.getUsername());
        }
        if (parameters.getPassword() != null) {
            connectOptions.setPassword(parameters.getPassword().toCharArray());
        }

        if (parameters.isSecureProtocol()) {
            PahoMQTTSsl ssl = (PahoMQTTSsl) parameters.getSsl();
            connectOptions.setSocketFactory(PahoUtil.getSocketFactory(ssl.getCaFilePath(), ssl.getClientCertFilePath(), ssl.getClientKeyFilePath(), ""));
        }

        try {
            client.connect(connectOptions)
                    .waitForCompletion(parameters.getConnectTimeout() * 1000L);

            logger.info(() -> "Connected client: " + parameters.getClientId());
            return new PahoMQTT3Connection(client);
        } catch (MqttException e) {
            throw new MQTTClientException("Connection error " + client, e);
        }
    }
}


