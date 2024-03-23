package net.xmeter.samplers.mqtt.paho;

import net.xmeter.samplers.mqtt.*;
import org.eclipse.paho.client.mqttv3.*;

import java.util.function.Consumer;
import java.util.logging.Logger;

class PahoMQTT3Connection implements MQTTConnection {

    private static final Logger logger = Logger.getLogger(PahoMQTT3Connection.class.getCanonicalName());

    private final MqttAsyncClient client;

    private MQTTSubListener listener;

    PahoMQTT3Connection(MqttAsyncClient client) {
        this.client = client;
    }

    @Override
    public boolean isConnectionSucc() {
        return client.isConnected();
    }

    @Override
    public String getClientId() {
        return client.getClientId();
    }

    @Override
    public void disconnect() throws Exception {
        client.disconnect();
    }

    @Override
    public MQTTPubResult publish(String topicName, byte[] message, MQTTQoS qoS, boolean retained, MQTT5PublishReq req) {
        try {
            MqttMessage msg = new MqttMessage();
            msg.setPayload(message);
            msg.setQos((PahoUtil.map(qoS)));
            msg.setRetained(retained);
            client.publish(topicName, msg);
            return new MQTTPubResult(true);
        } catch (Exception error) {
            return new MQTTPubResult(false, error.getMessage());
        }
    }

    @Override
    public void subscribe(String[] topicNames, MQTTQoS qos, Runnable onSuccess, Consumer<Throwable> onFailure) {
        int pahoQos = PahoUtil.map(qos);

        for (String topicName : topicNames) {
            try {
                IMqttActionListener subListener = new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        onSuccess.run();
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        onFailure.accept(throwable);
                    }
                };

                client.subscribe(topicName, pahoQos, "", subListener, (topic, message) -> {
                    try {
                        handleMessageArrived(topic, message);
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                });
            } catch (MqttException e) {
                logger.warning("Failed to subscribe " + topicName + ", error: " + e.getMessage());
            }
        }
    }

    @Override
    public void setSubListener(MQTTSubListener listener) {
        this.listener = listener;
    }

    private void handleMessageArrived(String topic, MqttMessage message) {
        this.listener.accept(topic, new String(message.getPayload()), () -> {
        });
    }

    @Override
    public String toString() {
        return "PahoMQTT3Connection{" +
                "clientId='" + client.getClientId() + '\'' +
                '}';
    }
}

