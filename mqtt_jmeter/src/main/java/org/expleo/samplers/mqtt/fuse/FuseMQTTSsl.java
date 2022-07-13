package org.expleo.samplers.mqtt.fuse;

import javax.net.ssl.SSLContext;

import org.expleo.samplers.mqtt.MQTTSsl;

class FuseMQTTSsl implements MQTTSsl {
    private final SSLContext sslContext;

    FuseMQTTSsl(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    SSLContext getSslContext() {
        return sslContext;
    }
}
