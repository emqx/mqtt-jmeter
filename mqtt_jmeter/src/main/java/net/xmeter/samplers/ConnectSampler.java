package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import net.xmeter.Util;
import net.xmeter.samplers.mqtt.ConnectionParameters;
import net.xmeter.samplers.mqtt.MQTT;
import net.xmeter.samplers.mqtt.MQTTClient;
import net.xmeter.samplers.mqtt.MQTTConnection;
import net.xmeter.samplers.mqtt.MQTTSsl;

public class ConnectSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = Logger.getLogger(ConnectSampler.class.getCanonicalName());

	private transient MQTTClient client;
	private transient MQTTConnection connection;

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (MQTTConnection) vars.getObject(getConnName());
		String clientId = (String) vars.getObject(getConnName()+"_clientId");
		if (connection != null) {
			try {
				if (connection != null) {
					logger.info(MessageFormat.format("Disconnect connection {0}.", connection));
					connection.disconnect();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to disconnect Connection" + connection, e);
			} finally {
				vars.remove(getConnName()); // clean up thread local var as well
				topicSubscribed.remove(clientId);
			}
		}

		ConnectionParameters parameters = new ConnectionParameters();
		try {
			parameters.setProtocol(getProtocol());
			parameters.setHost(getServer());
			parameters.setPort(Integer.parseInt(getPort()));
			parameters.setVersion(getMqttVersion());
			parameters.setKeepAlive((short) Integer.parseInt(getConnKeepAlive()));
			if (!"".equals(getWsPath().trim())) {
				parameters.setPath(getWsPath());
			}

			if(isClientIdSuffix()) {
				clientId = Util.generateClientId(getConnPrefix());
			} else {
				clientId = getConnPrefix();
			}
			parameters.setClientId(clientId);

			parameters.setConnectMaxAttempts(Integer.parseInt(getConnAttemptMax()));
			parameters.setReconnectMaxAttempts(Integer.parseInt(getConnReconnAttemptMax()));

			if (!"".equals(getUserNameAuth().trim())) {
				parameters.setUsername(getUserNameAuth());
			}
			if (!"".equals(getPasswordAuth().trim())) {
				parameters.setPassword(getPasswordAuth());
			}
			parameters.setCleanSession(Boolean.parseBoolean(getConnCleanSession()));
			parameters.setConnectTimeout(Integer.parseInt(getConnTimeout()));
			if (parameters.isSecureProtocol()) {
				MQTTSsl ssl = MQTT.getInstance(getMqttClientName()).createSsl(this);
				parameters.setSsl(ssl);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to establish Connection " + connection , e);
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}. Please check SSL authentication info.", connection));
			result.setResponseData("Failed to establish Connection. Please check SSL authentication info.".getBytes());
			result.setResponseCode("502");
			return result;
		}
		
		try {
			client = MQTT.getInstance(getMqttClientName()).createClient(parameters);

			result.sampleStart();
			connection = client.connect();
			result.sampleEnd();

			if (connection.isConnectionSucc()) {
				vars.putObject(getConnName(), connection); // save connection object as thread local variable !!
				vars.putObject(getConnName()+"_clientId", client.getClientId());	//save client id as thread local variable
				topicSubscribed.put(client.getClientId(), new HashSet<>());
				result.setSuccessful(true);
				result.setResponseData("Successful.".getBytes());
				result.setResponseMessage(MessageFormat.format("Connection {0} established successfully.", connection));
				result.setResponseCodeOK();
			} else {
				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}.", connection));
				result.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't establish connection.",
				client.getClientId()).getBytes());
				result.setResponseCode("501");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to establish Connection " + connection , e);
			if (result.getEndTime() == 0) result.sampleEnd(); //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to establish Connection {0}.", connection));
			result.setResponseData(MessageFormat.format("Client [{0}] failed with exception.", client.getClientId()).getBytes());
			result.setResponseCode("502");
		}
		
		return result;
	}

}
