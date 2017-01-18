package net.xmeter;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

import net.xmeter.samplers.ConnectionSampler;

public class Util implements Constants {
	
	private static SecureRandom random = new SecureRandom();
    private static char[] seeds = "abcdefghijklmnopqrstuvwxmy0123456789".toCharArray();

	public static String generateClientId(String prefix) {
		int leng = prefix.length();
		int postLeng = MAX_CLIENT_ID_LENGTH - leng;
		UUID uuid = UUID.randomUUID();
		String string = uuid.toString().replace("-", "");
		String post = string.substring(0, postLeng);
		return prefix + post;
	}

	public static SSLContext getContext(ConnectionSampler sampler) throws Exception {
		if (!sampler.isDualSSLAuth()) {
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} }, new SecureRandom());

			return sslContext;
		} else {
			String CA_KEYSTORE_PASS = sampler.getKeyFilePassword();
			String CLIENT_KEYSTORE_PASS = sampler.getKeyFileUsrName();

			//TODO Locate to the correct path of files
			InputStream is_cacert = Util.class.getResourceAsStream(sampler.getCertFile1());
			InputStream is_client = Util.class.getResourceAsStream(sampler.getCertFile2());

			KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType()); // jks
			tks.load(is_cacert, CA_KEYSTORE_PASS.toCharArray());

			KeyStore cks = KeyStore.getInstance("PKCS12");
			cks.load(is_client, CLIENT_KEYSTORE_PASS.toCharArray());

			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(tks, new TrustSelfSignedStrategy()) // use it to customize
					.loadKeyMaterial(cks, CLIENT_KEYSTORE_PASS.toCharArray()) // load client certificate
					.build();
			return sslContext;
		}
	}
	
	public static String generatePayload(int size) {
		StringBuffer res = new StringBuffer();
		for(int i = 0; i < size; i++) {
			res.append(seeds[random.nextInt(seeds.length - 1)]);
		}
		return res.toString();
	}
    
}
