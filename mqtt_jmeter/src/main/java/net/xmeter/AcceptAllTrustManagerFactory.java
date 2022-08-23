package net.xmeter;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

public class AcceptAllTrustManagerFactory extends TrustManagerFactory {
	
	private static final Provider PROVIDER = new Provider("", "0.0", "") {
		private static final long serialVersionUID = -2226165055935321223L;
	};
	
	private AcceptAllTrustManagerFactory() {
		super(AcceptAllTrustManagerFactorySpi.getInstance(), PROVIDER, "");
	}
	
	public static TrustManagerFactory getInstance() {
		return new AcceptAllTrustManagerFactory();
	}
	
	static final class AcceptAllTrustManagerFactorySpi extends TrustManagerFactorySpi {
		
		public static AcceptAllTrustManagerFactorySpi getInstance() {
			return new AcceptAllTrustManagerFactorySpi();
		}
		
		@Override
		protected TrustManager[] engineGetTrustManagers() {
			System.out.println("!! get trust managers (X509)");
			return new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
		}

		@Override
		protected void engineInit(KeyStore ks) throws KeyStoreException {
		}

		@Override
		protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
		}
		
	}
	
}
