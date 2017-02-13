package net.xmeter;

import java.io.File;
import java.io.FileInputStream;
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
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import net.xmeter.samplers.AbstractMQTTSampler;

public class Util implements Constants {
	
	private static SecureRandom random = new SecureRandom();
    private static char[] seeds = "abcdefghijklmnopqrstuvwxmy0123456789".toCharArray();
    private transient static Logger logger = LoggingManager.getLoggerForClass();

	public static String generateClientId(String prefix) {
		int leng = prefix.length();
		int postLeng = MAX_CLIENT_ID_LENGTH - leng;
		UUID uuid = UUID.randomUUID();
		String string = uuid.toString().replace("-", "");
		String post = string.substring(0, postLeng);
		return prefix + post;
	}

	public static SSLContext getContext(AbstractMQTTSampler sampler) throws Exception {
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
			logger.info("Configured with dual SSL, trying to load key files.");
			String KEYSTORE_PASS = sampler.getKeyStorePassword();
			String CLIENTCERT_PASS = sampler.getClientCertPassword();

			String baseDir = FileServer.getFileServer().getBaseDir();
			String file1 = sampler.getKeyStoreFilePath();
			
			File theFile1 = new File(file1);
			if(!theFile1.exists()) {
				file1 = baseDir + file1;
				theFile1 = new File(file1);
				if(!theFile1.exists()) {
					throw new RuntimeException("Cannot find file : " + sampler.getKeyStoreFilePath());
				}
			}
			
			String file2 = sampler.getClientCertFilePath();
			File theFile2 = new File(file2);
			if(!theFile2.exists()) {
				file2 = baseDir + file2;
				theFile2 = new File(file2);
				if(!theFile2.exists()) {
					throw new RuntimeException("Cannot find file : " + sampler.getClientCertFilePath());
				}
			}
			
			try(InputStream is_cacert = new FileInputStream(theFile1); InputStream is_client = new FileInputStream(theFile2)) {
				KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType()); // jks
				tks.load(is_cacert, KEYSTORE_PASS.toCharArray());

				KeyStore cks = KeyStore.getInstance("PKCS12");
				cks.load(is_client, CLIENTCERT_PASS.toCharArray());

				SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(tks, new TrustSelfSignedStrategy()) // use it to customize
						.loadKeyMaterial(cks, CLIENTCERT_PASS.toCharArray()) // load client certificate
						.build();
				return sslContext;
			}
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
