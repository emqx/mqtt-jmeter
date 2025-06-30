package net.xmeter.samplers;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.logging.Logger;

public class AsyncResponseSampler extends AbstractMQTTSampler {
	private static final long serialVersionUID = 4360869021667126983L;

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		SubSampler subSampler = (SubSampler) vars.getObject("sub");

		if (subSampler == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Sub sampler not found.");
			result.setResponseData("Failed. Sub sampler not found.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		// Could make clear responses configurable but cannot think of a good reason to not clear them which allows
		// the sub sampler to be reused
		return subSampler.produceAsyncResult(result, true);
	}
}
