package net.xmeter.samplers;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.fusesource.mqtt.client.CallbackConnection;

public class MessageCallbackSampler extends AbstractJavaSamplerClient {
	private transient CallbackConnection connection = null;
	private static final Logger logger = Logger.getLogger(MessageCallbackSampler.class.getCanonicalName());
	private static final String keyname = "lock_wait_time";
	
	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(keyname, "120");
		return defaultParameters;
	}
	
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		int wait_time = context.getIntParameter(keyname, 120);
		logger.info("** wait_time = " + wait_time);
		
		SampleResult result = new SampleResult();
		
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		connection = (CallbackConnection) vars.getObject("conn");
		if (connection == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Message callback: Connection not found.");
			result.setResponseData("Message callback failed because connection is not established.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		try {
			result.sampleStart();
			Object lock1 = new Object();
			synchronized (lock1) {
				DefaultListener listener = new DefaultListener(lock1);
				connection.listener(listener);
				lock1.wait(TimeUnit.SECONDS.toMillis(wait_time));
				if(listener.isSucc()) {
					result.setSuccessful(true);
					result.setResponseData(listener.getReceived().getBytes());
					result.setResponseMessage("OK");
					result.setResponseCodeOK();	
					return result;
				} else {
					return fillFailedResult(result, "503", "Failed to receive message.");
				}
			}
				
		} catch(Exception ex) {
			logger.severe(ex.getMessage());
			if (result.getEndTime() == 0) result.sampleEnd();
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Message callback failed for connection {0}.", connection));
			result.setResponseData(ex.getMessage().getBytes());
			result.setResponseCode("502");
			return result;
		}
	}
	
	private SampleResult fillFailedResult(SampleResult result, String code, String message) {
		//result.sampleStart();
		result.setResponseCode(code); // 5xx means various failures
		result.setSuccessful(false);
		result.setResponseMessage(message);
		result.setResponseData(message.getBytes());
		result.sampleEnd();
		return result;
	}
}
