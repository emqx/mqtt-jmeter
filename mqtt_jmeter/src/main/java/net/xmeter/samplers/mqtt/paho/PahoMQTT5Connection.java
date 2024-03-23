package net.xmeter.samplers.mqtt.paho;


import net.xmeter.samplers.mqtt.*;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

class PahoMQTT5Connection implements MQTTConnection {

    private static final Logger logger = Logger.getLogger(PahoMQTT5Connection.class.getCanonicalName());

    private final MqttAsyncClient client;

    private MQTTSubListener listener;

    PahoMQTT5Connection(MqttAsyncClient client) {
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
            msg.setProperties(buildMqtt5MessageFeature(req));

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

                MqttActionListener subListener = new MqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        onSuccess.run();
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        onFailure.accept(throwable);
                    }
                };

                // the subscribe method has issue when pass MqttActionListener and IMqttMessageListener
                // https://github.com/eclipse/paho.mqtt.java/issues/1019
                // https://github.com/eclipse/paho.mqtt.java/issues/826
                // for `messageArrived` use `client.setCallback` to replace at this time.
                client.setCallback(new MqttCallback() {
                    @Override
                    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {

                    }

                    @Override
                    public void mqttErrorOccurred(MqttException e) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        try {
                            handleMessageArrived(topic, message);
                        } catch (Exception e) {
                            logger.severe(e.getMessage());
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttToken iMqttToken) {

                    }

                    @Override
                    public void connectComplete(boolean b, String s) {

                    }

                    @Override
                    public void authPacketArrived(int i, MqttProperties mqttProperties) {

                    }
                });

//                MqttSubscription mqttSubscription = new MqttSubscription(topicName);
//                mqttSubscription.setQos(pahoQos);
//                mqttSubscription.setNoLocal(false);
//                mqttSubscription.setRetainHandling(0);
//                mqttSubscription.setRetainAsPublished(false);

                client.subscribe(topicName, pahoQos, "", subListener);
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

    private MqttProperties buildMqtt5MessageFeature(MQTT5PublishReq req){
        MqttProperties properties = new MqttProperties();
        properties.setMessageExpiryInterval(req.getMessageExpiryInterval());

        if (req.getCorrelationData() != null && !req.getCorrelationData().trim().isEmpty()) {
            properties.setCorrelationData(req.getCorrelationData().getBytes());
        }

        List<UserProperty> userProperty = PahoUtil.ConvertUserProperty(req.getUserProperties());
        if (!userProperty.isEmpty()) {
            properties.setUserProperties(userProperty);
        }

        if (req.getResponseTopic() != null && !req.getResponseTopic().isEmpty()) {
            properties.setResponseTopic(req.getResponseTopic());
        }

        if (req.getContentType() != null && !req.getContentType().isEmpty()){
            properties.setContentType(req.getContentType());
        }

        if (!req.getPayloadFormatIndicator().isEmpty()) {
            properties.setPayloadFormat(req.getPayloadFormatIndicator().equals("UTF_8"));
        }

        if (req.getTopicAlias() != null && !req.getTopicAlias().isEmpty()) {
            properties.setTopicAlias(Integer.parseInt(req.getTopicAlias()));
        }

        if (req.getSubscriptionIdentifier() != null && !req.getSubscriptionIdentifier().isEmpty()) {
            properties.setSubscriptionIdentifier(Integer.parseInt(req.getSubscriptionIdentifier()));
        }

//        logger.severe(properties.toString());

        return properties;
    }

    @Override
    public String toString() {
        return "PahoMQTT5Connection{" +
                "clientId='" + client.getClientId() + '\'' +
                '}';
    }
}
