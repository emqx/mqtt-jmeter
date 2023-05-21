package net.xmeter.samplers.mqtt.hivemq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfigBuilder;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTClientException;
import net.xmeter.samplers.mqtt.MQTTConnection;

class HiveMQTT5Client implements MQTTClient {
    private static final Logger logger = Logger.getLogger(HiveMQTT5Client.class.getCanonicalName());
    private final ConnectionParameters parameters;
    private final Mqtt5BlockingClient client;

    HiveMQTT5Client(ConnectionParameters parameters) throws Exception {
        this.parameters = parameters;
        
        Mqtt5ClientBuilder mqtt5ClientBuilder = Mqtt5Client.builder()
			      .identifier(parameters.getClientId())
			      .serverHost(parameters.getHost())
			      .serverPort(parameters.getPort());
        
        mqtt5ClientBuilder = applyAdditionalConfig(mqtt5ClientBuilder, parameters);
        client = mqtt5ClientBuilder.buildBlocking();
       
    }
    
    private Mqtt5ClientBuilder applyAdditionalConfig(Mqtt5ClientBuilder builder, ConnectionParameters parameters)
    		throws Exception
    {
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
        Mqtt5ConnectBuilder.Send<CompletableFuture<Mqtt5ConnAck>> connectSend = client.toAsync().connectWith()
                .cleanStart(parameters.isCleanSession())
                .keepAlive(parameters.getKeepAlive());
        Mqtt5SimpleAuth auth = createAuth();
        if (auth != null) {
            connectSend = connectSend.simpleAuth(auth);
        }
        logger.info(() -> "Connect client: " + parameters.getClientId());
        CompletableFuture<Mqtt5ConnAck> connectFuture = connectSend.send();
        try {
            Mqtt5ConnAck connAck = connectFuture.get(parameters.getConnectTimeout(), TimeUnit.SECONDS);
            logger.info(() -> "Connected client: " + parameters.getClientId());
            return new HiveMQTT5Connection(client, parameters.getClientId(), connAck);
        } catch (TimeoutException timeoutException) {
            try {
                client.disconnect();
            } catch (Exception e) {
                logger.log(Level.FINE, "Disconnect on timeout failed " + client, e);
            }
            throw new MQTTClientException("Connection timeout " + client, timeoutException);
        }
    }

    private Mqtt5SimpleAuth createAuth() {
        if (parameters.getUsername() != null) {
            Mqtt5SimpleAuthBuilder.Complete simpleAuth = Mqtt5SimpleAuth.builder()
                    .username(parameters.getUsername());
            if (parameters.getPassword() != null) {
                simpleAuth.password(parameters.getPassword().getBytes());
            }
            return simpleAuth.build();
        }
        return null;
    }
}
