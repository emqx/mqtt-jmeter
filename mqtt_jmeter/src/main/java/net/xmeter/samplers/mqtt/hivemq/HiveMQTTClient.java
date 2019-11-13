package net.xmeter.samplers.mqtt.hivemq;

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
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTConnection;

import java.util.logging.Logger;

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
    public MQTTConnection connect() {
        Mqtt3ConnectBuilder.Send<Mqtt3ConnAck> connectBuilder = client.connectWith()
                .cleanSession(parameters.isCleanSession())
                .keepAlive(parameters.getKeepAlive());
        connectBuilder = applyAuth(connectBuilder);
        logger.info(() -> "Connect client: " + parameters.getClientId());
        Mqtt3ConnAck connAck = connectBuilder.send();
        logger.info(() -> "Connected client: " + parameters.getClientId());
        return new HiveMQTTConnection(client, parameters.getClientId(), connAck);
    }

    private Mqtt3ConnectBuilder.Send<Mqtt3ConnAck> applyAuth(Mqtt3ConnectBuilder.Send<Mqtt3ConnAck> builder) {
        if (parameters.getUsername() != null) {
            Mqtt3SimpleAuthBuilder.Complete simpleAuth = Mqtt3SimpleAuth.builder()
                    .username(parameters.getUsername());
            if (parameters.getPassword() != null) {
                simpleAuth.password(parameters.getPassword().getBytes());
            }
            builder = builder.simpleAuth(simpleAuth.build());
        }
        return builder;
    }
}
