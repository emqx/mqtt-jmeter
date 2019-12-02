package net.xmeter.samplers.mqtt.fuse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.QoS;

import net.xmeter.samplers.PubCallback;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTPubResult;
import net.xmeter.samplers.mqtt.MQTTQoS;
import net.xmeter.samplers.mqtt.MQTTSubListener;

class FuseMQTTConnection implements MQTTConnection {
    private static final Logger logger = Logger.getLogger(FuseMQTTConnection.class.getCanonicalName());

    private final Semaphore disconnectLock = new Semaphore(0);
    private final String clientId;
    private final ConnectionCallback connectionCallback;
    private final CallbackConnection callbackConnection;

    FuseMQTTConnection(String clientId, ConnectionCallback connectionCallback, CallbackConnection callbackConnection) {
        this.clientId = clientId;
        this.connectionCallback = connectionCallback;
        this.callbackConnection = callbackConnection;
    }

    @Override
    public boolean isConnectionSucc() {
        return connectionCallback.isConnSucc();
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void disconnect() throws Exception {
        callbackConnection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                disconnectLock.release();
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.log(Level.SEVERE, "Disconnect failed", throwable);
                disconnectLock.release();
            }
        });
        disconnectLock.tryAcquire(30, TimeUnit.SECONDS);
    }

    @Override
    public MQTTPubResult publish(String topicName, byte[] message, MQTTQoS qos, boolean retained) {
        final Object pubLock = new Object();
        QoS fuseQos = FuseUtil.map(qos);
        PubCallback pubCallback = new PubCallback(pubLock, fuseQos);

        try {
            if (qos == MQTTQoS.AT_MOST_ONCE) {
                //For QoS == 0, the callback is the same thread with sampler thread, so it cannot use the lock object wait() & notify() in else block;
                //Otherwise the sampler thread will be blocked.
                callbackConnection.publish(topicName, message, fuseQos, retained, pubCallback);
            } else {
                synchronized (pubLock) {
                    callbackConnection.publish(topicName, message, fuseQos, retained, pubCallback);
                    pubLock.wait();
                }
            }
            return new MQTTPubResult(pubCallback.isSuccessful(), pubCallback.getErrorMessage());
        } catch (Exception exception) {
            return new MQTTPubResult(false, exception.getMessage());
        }
    }

    @Override
    public void subscribe(String[] topicNames, MQTTQoS qos, Runnable onSuccess, Consumer<Throwable> onFailure) {
        FuseSubscription subscription = new FuseSubscription(topicNames, qos, callbackConnection);
        subscription.subscribe(onSuccess, onFailure);
    }

    @Override
    public void setSubListener(MQTTSubListener listener) {
        callbackConnection.listener(new Listener() {
            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    body.writeTo(baos);
                    String msg = baos.toString();

                    listener.accept(topic.toString(), msg, ack);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable value) {
                logger.log(Level.SEVERE, "Sub listener failure", value);
            }

            @Override
            public void onDisconnected() {
            }

            @Override
            public void onConnected() {
            }
        });
    }

    @Override
    public String toString() {
        return "FuseMQTTConnection{" +
                "clientId='" + clientId + '\'' +
                '}';
    }
}
