package net.xmeter.samplers.mqtt;

public class MQTTException extends Exception {
    public MQTTException(String message) {
        super(message);
    }

    public MQTTException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQTTException(Throwable cause) {
        super(cause);
    }
}
