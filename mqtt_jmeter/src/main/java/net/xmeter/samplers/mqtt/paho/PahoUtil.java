package net.xmeter.samplers.mqtt.paho;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import net.xmeter.Constants;
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTTQoS;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class PahoUtil {
    static final List<String> ALLOWED_PROTOCOLS;

    static {
        ALLOWED_PROTOCOLS = new ArrayList<>();
        ALLOWED_PROTOCOLS.add(Constants.TCP_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.SSL_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.WS_PROTOCOL);
        ALLOWED_PROTOCOLS.add(Constants.WSS_PROTOCOL);
    }

    static int map(MQTTQoS qos) {
        switch (qos) {
            case AT_MOST_ONCE:
                return 0;
            case AT_LEAST_ONCE:
                return 1;
            case EXACTLY_ONCE:
                return 2;
            default:
                throw new IllegalArgumentException("Unknown QoS: " + qos);
        }
    }

    static String createHostAddress(ConnectionParameters parameters) {
        return String.format("%s://%s:%d",
                parameters.getProtocol().toLowerCase(),
                parameters.getHost(),
                parameters.getPort());
    }

    static List<UserProperty> ConvertUserProperty(Map<String, String> userProperty) {
        if (!userProperty.isEmpty()) {
            ArrayList<UserProperty> userDefinedProperties = new ArrayList<>();
            for (Map.Entry<String, String> entry : userProperty.entrySet()) {
                userDefinedProperties.add(new UserProperty(entry.getKey(), entry.getValue()));
            }
            return userDefinedProperties;
        } else {
            return new ArrayList<UserProperty>();
        }
    }

    static SSLSocketFactory getSingleSocketFactory(final String caCrtFile) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Load CA certificates
        KeyStore caKs = loadCAKeyStore(caCrtFile);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }

    static SSLSocketFactory getSocketFactory(final String caCrtFile,
                                                    final String crtFile, final String keyFile, final String password)
            throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // Load CA certificates
        KeyStore caKs = loadCAKeyStore(caCrtFile);

        // Load client certificate chain and key
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);

        // Load the entire client certificate chain
        Certificate[] chain;
        try (FileInputStream fis = new FileInputStream(crtFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certs = cf.generateCertificates(fis);
            chain = certs.toArray(new Certificate[0]);
        }

        // Load client private key
        try (PEMParser pemParser = new PEMParser(new FileReader(keyFile))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            KeyPair key = converter.getKeyPair((PEMKeyPair) object);
            ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), chain);
        }

        // Set up key managers and trust managers
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    private static KeyStore loadCAKeyStore(String caCrtFile) throws Exception {
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        try (FileInputStream fis = new FileInputStream(caCrtFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> caCerts = cf.generateCertificates(fis);
            int certIndex = 1;
            for (Certificate caCert : caCerts) {
                String alias = "ca-certificate-" + certIndex++;
                caKs.setCertificateEntry(alias, caCert);
            }
        }
        return caKs;
    }
}
