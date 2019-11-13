package net.xmeter.samplers;

import net.xmeter.samplers.mqtt.MQTTConnection;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.text.MessageFormat;
import java.util.Vector;
import java.util.logging.Logger;

public class EfficientDisConnectSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 4360869021667126983L;
	private static final Logger logger = Logger.getLogger(EfficientDisConnectSampler.class.getCanonicalName());

	private transient Vector<MQTTConnection> connections = new Vector<>();

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getLabelPrefix() + getName());
		result.setSuccessful(true);
		
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		int conCapacity = (int) vars.getObject("conCapacity");
		connections = (Vector<MQTTConnection>) vars.getObject("conns");
		
		if (connections == null) {
			result.sampleStart();
			for (int i=0; i<conCapacity; i++) {
				SampleResult subResult = new SampleResult();
				subResult.sampleStart();
				subResult.setSampleLabel(getName() + "_" + i);
				subResult.setSuccessful(false);
				subResult.setResponseMessage("Connection not found.");
				subResult.setResponseData("Failed. Connection not found.".getBytes());
				subResult.setResponseCode("500");
				subResult.sampleEnd();
				result.addSubResult(subResult);
			}
			result.setSampleCount(conCapacity);
			return result;
		}
		
		result.sampleStart();
		int totalSampleCount = 0;
		for (MQTTConnection connection : connections) {
			String clientId = connection.getClientId();
			SampleResult subResult = new SampleResult();
			long cur = System.currentTimeMillis();
			subResult.sampleStart();
			subResult.setSampleLabel(getName());
			try {
				logger.info(MessageFormat.format("Disconnect connection {0}.", connection));
				connection.disconnect();
//				removeTopicSubscribed(clientId);
				subResult.setSuccessful(true);
				subResult.setResponseData("Successful.".getBytes());
				subResult.setResponseMessage(MessageFormat.format("Connection {0} disconnected.", connection));
				subResult.setResponseCodeOK();
			} catch (Exception e) {
				logger.severe(e.getMessage());
				subResult.setSuccessful(false);
				subResult.setResponseMessage(MessageFormat.format("Failed to disconnect Connection {0}.", connection));
				subResult.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't disconnect connection.", (clientId == null ? "null" : clientId)).getBytes());
				subResult.setResponseCode("501");
			} finally {
				totalSampleCount += subResult.getSampleCount();
				subResult.sampleEnd();
				System.out.println("dis-connect: " + (System.currentTimeMillis() - cur));
				result.addSubResult(subResult);
			}
		}
		
		int failedCon = conCapacity - connections.size();
		for (int i=0; i<failedCon; i++) {
			SampleResult subResult = new SampleResult();
			subResult.sampleStart();
			subResult.setSampleLabel(getName() + "_" + i);
			subResult.setResponseMessage("Connection not found.");
			subResult.setResponseData("Failed. Connection not found.".getBytes());
			subResult.setResponseCode("500");
			totalSampleCount += subResult.getSampleCount();
			subResult.sampleEnd();
			result.addSubResult(subResult);
		}
		
		vars.remove("conns"); // clean up thread local var as well
		result.setSampleCount(totalSampleCount);
		if (result.getEndTime() == 0) {
			result.sampleEnd();
		}
		
		return result;
	}

}
