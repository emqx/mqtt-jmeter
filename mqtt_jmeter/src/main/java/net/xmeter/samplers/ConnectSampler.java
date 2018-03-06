package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;

import net.xmeter.Util;

public class ConnectSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = Logger.getLogger(ConnectSampler.class.getCanonicalName());
	
	private transient CallbackConnection connection = null;
	private transient MQTT mqtt = new MQTT();

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (CallbackConnection) vars.getObject("conn");
		if (connection != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Connection {0} is already established.", connection));
			result.setResponseData("Failed.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}
		
		try {
			if (!DEFAULT_PROTOCOL.equals(getProtocol())) {
				mqtt.setSslContext(Util.getContext(this));
			}
			
			mqtt.setHost(getProtocol().toLowerCase() + "://" + getServer() + ":" + getPort());
			mqtt.setVersion(getMqttVersion());
			mqtt.setKeepAlive((short) Integer.parseInt(getConnKeepAlive()));
			
			String clientId = null;
			if(isClientIdSuffix()) {
				clientId = Util.generateClientId(getConnPrefix());
			} else {
				clientId = getConnPrefix();
			}
			mqtt.setClientId(clientId);
			
			mqtt.setConnectAttemptsMax(Integer.parseInt(getConnAttamptMax()));
			mqtt.setReconnectAttemptsMax(Integer.parseInt(getConnReconnAttamptMax()));

			if (!"".equals(getUserNameAuth().trim())) {
				mqtt.setUserName(getUserNameAuth());
			}
			if (!"".equals(getPasswordAuth().trim())) {
				mqtt.setPassword(getPasswordAuth());
			}

			result.sampleStart();
			// TODO: Optionally connection can subscribe to topics ??
			connection = mqtt.callbackConnection();
			Object connLock = new Object();
			ConnectionCallback callback = new ConnectionCallback(connection, connLock);
			synchronized (connLock) {
				connection.connect(callback);
				connLock.wait(TimeUnit.SECONDS.toMillis(Integer.parseInt(getConnTimeout())));
			}
			
			Object lock1 = new Object();
			DefaultListener listener = new DefaultListener(lock1);
			synchronized (lock1) {
				connection.listener(listener);
				lock1.wait(TimeUnit.SECONDS.toMillis(10));
			}
			
			result.sampleEnd();
			if (callback.isConnectionSucc()) {
				vars.putObject("conn", connection); // save connection object as thread local variable !!
				if(listener.isSucc()) {
					result.setSuccessful(true);
					result.setResponseData(listener.getReceived().getBytes());
					result.setResponseMessage("OK");
					result.setResponseCodeOK();	
					return result;
				} else {
					result.setSuccessful(false);
					result.setResponseMessage(MessageFormat.format("Failed to receive message for connection {0}.", connection));
					result.setResponseData("Failed.".getBytes());
					result.setResponseCode("503");
				}
			} else {
				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}.", connection));
				result.setResponseData("Failed.".getBytes());
				result.setResponseCode("501");				
			}
		} catch (Exception e) {
			logger.severe(e.getMessage());
			if (result.getEndTime() == 0) result.sampleEnd(); //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}.", connection));
			result.setResponseData("Failed with exception.".getBytes());
			result.setResponseCode("502");
		}
		
		return result;
	}

}
