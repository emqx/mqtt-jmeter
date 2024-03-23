package net.xmeter.samplers.mqtt.paho;

import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTClientException;
import net.xmeter.samplers.mqtt.MQTTConnection;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PahoMQTT5Client implements MQTTClient {

    private static final Logger logger = Logger.getLogger(PahoMQTT5Client.class.getCanonicalName());
    private final ConnectionParameters parameters;
    private final MqttAsyncClient client;

    PahoMQTT5Client(ConnectionParameters parameters) throws MqttException {
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

        MqttConnectionOptions connectOptions = new MqttConnectionOptions();
        connectOptions.setCleanStart(parameters.isCleanStart());
        connectOptions.setKeepAliveInterval(parameters.getKeepAlive());
        connectOptions.setConnectionTimeout(parameters.getConnectTimeout());
        connectOptions.setSessionExpiryInterval(parameters.getSessionExpiryInterval());

        List<UserProperty> userProperty = PahoUtil.ConvertUserProperty(parameters.getConnUserProperty());
        if (!userProperty.isEmpty()) {
            connectOptions.setUserProperties(userProperty);
        }

        if (parameters.shouldAutomaticReconnectWithDefaultConfig()) {
            connectOptions.setAutomaticReconnect(true);
        }
        if (parameters.getUsername() != null) {
            connectOptions.setUserName(parameters.getUsername());
        }
        if (parameters.getPassword() != null) {
            connectOptions.setPassword(parameters.getPassword().getBytes());
        }

        if (parameters.isSecureProtocol()) {
            PahoMQTTSsl ssl = (PahoMQTTSsl) parameters.getSsl();
            connectOptions.setSocketFactory(PahoUtil.getSocketFactory(ssl.getCaFilePath(), ssl.getClientCertFilePath(), ssl.getClientKeyFilePath(), ""));
        }

        if (parameters.getConnWsHeader() != null && !parameters.getConnWsHeader().isEmpty()) {
            connectOptions.setCustomWebSocketHeaders(parameters.getConnWsHeader());
        }

//        if (parameters.getAuthMethod() != null && !parameters.getAuthMethod().isEmpty()) {
//            connectOptions.setAuthMethod(parameters.getAuthMethod());
//        }

//        if (parameters.getAuthData() != null && !parameters.getAuthData().isEmpty()) {
//            connectOptions.setAuthData(parameters.getAuthData().getBytes());
//        }

        try {
            client.connect(connectOptions)
                    .waitForCompletion(parameters.getConnectTimeout() * 1000L);

            logger.info(() -> "Connected client: " + parameters.getClientId());
            return new PahoMQTT5Connection(client);
        } catch (MqttException e) {
            throw new MQTTClientException("Connection error " + client, e);
        }
    }
}
