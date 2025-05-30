package net.xmeter.samplers.mqtt.paho;

import net.xmeter.samplers.mqtt.MQTTSsl;

import javax.net.ssl.SSLContext;

class PahoMQTTSsl implements MQTTSsl {
    private final String caFilePath;
    private final String clientCertFilePath;
    private final String clientKeyFilePath;

    PahoMQTTSsl(String ca, String cc, String ck) {
        this.caFilePath = ca;
        this.clientCertFilePath = cc;
        this.clientKeyFilePath = ck;
    }

    String getCaFilePath(){
        return caFilePath;
    }

    String getClientCertFilePath(){
        return clientCertFilePath;
    }

    String getClientKeyFilePath(){
        return  clientKeyFilePath;
    }

    @Override
    public String toString() {
        return "PahoMQTTSsl{" +
                "ca='" + caFilePath + '\'' +
                "cc='" + clientCertFilePath + '\'' +
                "ck='" + clientKeyFilePath + '\'' +
                '}';
    }
}
