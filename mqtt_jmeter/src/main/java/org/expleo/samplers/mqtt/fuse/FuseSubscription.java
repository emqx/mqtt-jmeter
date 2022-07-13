package org.expleo.samplers.mqtt.fuse;

import java.util.function.Consumer;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import org.expleo.samplers.mqtt.MQTTQoS;

class FuseSubscription {
    private final String[] topics;
    private final MQTTQoS qos;
    private final CallbackConnection callbackConnection;

    FuseSubscription(String[] topics, MQTTQoS qos, CallbackConnection callbackConnection) {
        this.topics = topics;
        this.qos = qos;
        this.callbackConnection = callbackConnection;
    }

    void subscribe(Runnable onSuccess, Consumer<Throwable> onFailure) {
        Topic[] fuseTopics = new Topic[this.topics.length];
        QoS fuseQos = FuseUtil.map(qos);
        for(int i = 0; i < topics.length; i++) {
            fuseTopics[i] = new Topic(topics[i], fuseQos);
        }

        callbackConnection.subscribe(fuseTopics, new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                onSuccess.run();
            }

            @Override
            public void onFailure(Throwable value) {
                onFailure.accept(value);
            }
        });
    }
}
