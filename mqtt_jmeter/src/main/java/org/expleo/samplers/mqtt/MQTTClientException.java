package org.expleo.samplers.mqtt;

public class MQTTClientException extends Exception {
    public MQTTClientException(String message) {
        super(message);
    }

    public MQTTClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQTTClientException(Throwable cause) {
        super(cause);
    }
}
