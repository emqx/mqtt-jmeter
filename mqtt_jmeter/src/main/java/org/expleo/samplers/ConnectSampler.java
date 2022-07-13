package org.expleo.samplers;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import org.expleo.Util;
import org.expleo.samplers.mqtt.ConnectionParameters;
import org.expleo.samplers.mqtt.MQTT;
import org.expleo.samplers.mqtt.MQTTClient;
import org.expleo.samplers.mqtt.MQTTConnection;
import org.expleo.samplers.mqtt.MQTTSsl;

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
		connection = (MQTTConnection) vars.getObject("conn");
		if (connection != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Connection {0} is already established.", connection));
			result.setResponseData("Failed. Connection is already established.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
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

			String clientId;
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
				vars.putObject("conn", connection); // save connection object as thread local variable !!
				vars.putObject("clientId", client.getClientId());	//save client id as thread local variable
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
