package net.xmeter.samplers.mqtt.hivemq;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;

import net.xmeter.samplers.mqtt.MQTTClientException;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTPubResult;
import net.xmeter.samplers.mqtt.MQTTQoS;
import net.xmeter.samplers.mqtt.MQTTSubListener;

class HiveMQTT5Connection implements MQTTConnection {
    private static final Logger logger = Logger.getLogger(HiveMQTT5Connection.class.getCanonicalName());

    private static final Charset charset = Charset.forName("UTF-8");
    private static ThreadLocal<CharsetDecoder> decoder = ThreadLocal.withInitial(() -> charset.newDecoder());

    private final Mqtt5BlockingClient client;
    private final String clientId;
    private final Mqtt5ConnAck connAck;
    private MQTTSubListener listener;

    HiveMQTT5Connection(Mqtt5BlockingClient client, String clientId, Mqtt5ConnAck connAck) {
        this.client = client;
        this.clientId = clientId;
        this.connAck = connAck;
    }

    @Override
    public boolean isConnectionSucc() {
        return !connAck.getReasonCode().isError();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void disconnect() {
        client.disconnect();
    }

    @Override
    public MQTTPubResult publish(String topicName, byte[] message, MQTTQoS qoS, boolean retained) {
        try {
            client.publishWith()
                    .topic(topicName)
                    .payload(message)
                    .qos(HiveUtil.map(qoS))
                    .retain(retained)
                    .send();
            return new MQTTPubResult(true);
        } catch (Exception error) {
            return new MQTTPubResult(false, error.getMessage());
        }
    }

    @Override
    public void subscribe(String[] topicNames, MQTTQoS qos, Runnable onSuccess, Consumer<Throwable> onFailure) {
        MqttQos hiveQos = HiveUtil.map(qos);
        Mqtt5Subscribe subscribe = null;
        Mqtt5SubscribeBuilder builder = Mqtt5Subscribe.builder();
        for (int i = 0; i < topicNames.length; i++) {
            String topicName = topicNames[i];
            Mqtt5Subscription subscription = Mqtt5Subscription.builder().topicFilter(topicName).qos(hiveQos).build();
            if (i < topicNames.length - 1) {
                builder.addSubscription(subscription);
            } else {
                subscribe = builder.addSubscription(subscription).build();
            }
        }

        Mqtt5AsyncClient asyncClient = client.toAsync();
        
        asyncClient.subscribe(subscribe, this::handlePublishReceived).whenComplete((ack, error) -> {
            if (error != null) {
                onFailure.accept(error);
            } else {
                List<Mqtt5SubAckReasonCode> ackCodes = ack.getReasonCodes();
                for (int i = 0; i < ackCodes.size(); i++) {
                    Mqtt5SubAckReasonCode ackCode = ackCodes.get(i);
                    if (ackCode.isError()) {
                        int index = i;
                        logger.warning(() -> "Failed to subscribe " + topicNames[index] + " code: " + ackCode.name());
                    }
                }
                onSuccess.run();
            }
        });
    }

    private void handlePublishReceived(Mqtt5Publish received) {
        String topic = decode(received.getTopic().toByteBuffer());
        String payload = received.getPayload().map(this::decode).orElse("");
        this.listener.accept(topic, payload, () -> {});
    }

    private String decode(ByteBuffer value) {
        try {
            return decoder.get().decode(value).toString();
        } catch (CharacterCodingException e) {
            throw new RuntimeException(new MQTTClientException("Failed to decode", e));
        }
    }

    @Override
    public void setSubListener(MQTTSubListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        return "HiveMQTT5Connection{" +
                "clientId='" + clientId + '\'' +
                '}';
    }
}