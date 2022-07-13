package net.xmeter.samplers.mqtt.hivemq;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;

import net.xmeter.samplers.mqtt.MQTTClientException;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTPubResult;
import net.xmeter.samplers.mqtt.MQTTQoS;
import net.xmeter.samplers.mqtt.MQTTSubListener;

class HiveMQTTConnection implements MQTTConnection {
    private static final Logger logger = Logger.getLogger(HiveMQTTConnection.class.getCanonicalName());

    private static final Charset charset = StandardCharsets.UTF_8;
    private static final ThreadLocal<CharsetDecoder> decoder = ThreadLocal.withInitial(charset::newDecoder);

    private final Mqtt3BlockingClient client;
    private final String clientId;
    private final Mqtt3ConnAck connAck;
    private MQTTSubListener listener;

    HiveMQTTConnection(Mqtt3BlockingClient client, String clientId, Mqtt3ConnAck connAck) {
        this.client = client;
        this.clientId = clientId;
        this.connAck = connAck;
    }

    @Override
    public boolean isConnectionSucc() {
        return !connAck.getReturnCode().isError();
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
        Mqtt3Subscribe subscribe = null;
        Mqtt3SubscribeBuilder builder = Mqtt3Subscribe.builder();
        for (int i = 0; i < topicNames.length; i++) {
            String topicName = topicNames[i];
            Mqtt3Subscription subscription = Mqtt3Subscription.builder().topicFilter(topicName).qos(hiveQos).build();
            if (i < topicNames.length - 1) {
                builder.addSubscription(subscription);
            } else {
                subscribe = builder.addSubscription(subscription).build();
            }
        }

        Mqtt3AsyncClient asyncClient = client.toAsync();
        asyncClient.subscribe(subscribe, this::handlePublishReceived).whenComplete((ack, error) -> {
            if (error != null) {
                onFailure.accept(error);
            } else {
                List<Mqtt3SubAckReturnCode> ackCodes = ack.getReturnCodes();
                for (int i = 0; i < ackCodes.size(); i++) {
                    Mqtt3SubAckReturnCode ackCode = ackCodes.get(i);
                    if (ackCode.isError()) {
                        int index = i;
                        logger.warning(() -> "Failed to subscribe " + topicNames[index] + " code: " + ackCode.name());
                    }
                }
                onSuccess.run();
            }
        });
    }

    private void handlePublishReceived(Mqtt3Publish received) {
        System.out.println("Topic name (before decoding): " + received.getTopic());
        String topic = decode(received.getTopic().toByteBuffer());
        String payload = received.getPayload().map(this::decode).orElse("");
        this.listener.accept(topic, payload, () -> {});
    }

    private String decode(ByteBuffer value) {
//      try {
//          return decoder.get().decode(value).toString();
//      } catch (CharacterCodingException e) {
//          throw new RuntimeException(new MQTTClientException("Failed to decode", e));
//      }
        return StandardCharsets.UTF_8.decode(value).toString();
    }

    @Override
    public void setSubListener(MQTTSubListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        return "HiveMQTTConnection{clientId='" + clientId + "'}";
    }
}
