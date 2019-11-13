package net.xmeter.samplers.mqtt.fuse;

import net.xmeter.samplers.mqtt.MQTTSsl;

import javax.net.ssl.SSLContext;

class FuseMQTTSsl implements MQTTSsl {
    private final SSLContext sslContext;

    FuseMQTTSsl(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    SSLContext getSslContext() {
        return sslContext;
    }
}
