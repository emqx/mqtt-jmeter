package org.expleo.samplers.mqtt.hivemq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfigBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;

import org.expleo.samplers.mqtt.ConnectionParameters;
import org.expleo.samplers.mqtt.MQTTClient;
import org.expleo.samplers.mqtt.MQTTClientException;
import org.expleo.samplers.mqtt.MQTTConnection;

class HiveMQTTClient implements MQTTClient {
    private static final Logger logger = Logger.getLogger(HiveMQTTClient.class.getCanonicalName());
    private final ConnectionParameters parameters;
    private final Mqtt3BlockingClient client;

    HiveMQTTClient(ConnectionParameters parameters) throws Exception {
        this.parameters = parameters;
        Mqtt3ClientBuilder mqtt3ClientBuilder = Mqtt3Client.builder()
                .identifier(parameters.getClientId())
                .serverHost(parameters.getHost())
                .serverPort(parameters.getPort());
        mqtt3ClientBuilder = applyAdditionalConfig(mqtt3ClientBuilder, parameters);
        client = mqtt3ClientBuilder
                .buildBlocking();
    }

    private Mqtt3ClientBuilder applyAdditionalConfig(Mqtt3ClientBuilder builder, ConnectionParameters parameters)
            throws Exception {
        if (parameters.getReconnectMaxAttempts() > 0) {
            builder = builder.automaticReconnect(MqttClientAutoReconnect.builder().build());
        }
        if (parameters.isSecureProtocol()) {
            MqttClientSslConfig sslConfig = ((HiveMQTTSsl) parameters.getSsl()).getSslConfig();
            builder = builder.sslConfig(sslConfig);
        }
        if (parameters.isWebSocketProtocol()) {
            MqttWebSocketConfigBuilder wsConfigBuilder = MqttWebSocketConfig.builder();
            if (parameters.getPath() != null) {
                wsConfigBuilder.serverPath(parameters.getPath());
            }
            builder = builder.webSocketConfig(wsConfigBuilder.build());
        }
        return builder;
    }

    @Override
    public String getClientId() {
        return parameters.getClientId();
    }

    @Override
    public MQTTConnection connect() throws Exception {
        Mqtt3ConnectBuilder.Send<CompletableFuture<Mqtt3ConnAck>> connectSend = client.toAsync().connectWith()
                .cleanSession(parameters.isCleanSession())
                .keepAlive(parameters.getKeepAlive());
        Mqtt3SimpleAuth auth = createAuth();
        if (auth != null) {
            connectSend = connectSend.simpleAuth(auth);
        }
        logger.info(() -> "Connect client: " + parameters.getClientId());
        CompletableFuture<Mqtt3ConnAck> connectFuture = connectSend.send();
        try {
            Mqtt3ConnAck connAck = connectFuture.get(parameters.getConnectTimeout(), TimeUnit.SECONDS);
            logger.info(() -> "Connected client: " + parameters.getClientId());
            return new HiveMQTTConnection(client, parameters.getClientId(), connAck);
        } catch (TimeoutException timeoutException) {
            try {
                client.disconnect();
            } catch (Exception e) {
                logger.log(Level.FINE, "Disconnect on timeout failed " + client, e);
            }
            throw new MQTTClientException("Connection timeout " + client, timeoutException);
        }
    }

    private Mqtt3SimpleAuth createAuth() {
        if (parameters.getUsername() != null) {
            Mqtt3SimpleAuthBuilder.Complete simpleAuth = Mqtt3SimpleAuth.builder()
                    .username(parameters.getUsername());
            if (parameters.getPassword() != null) {
                simpleAuth.password(parameters.getPassword().getBytes());
            }
            return simpleAuth.build();
        }
        return null;
    }
}
