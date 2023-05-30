package net.xmeter.gui;

import net.xmeter.Constants;
import net.xmeter.samplers.AsyncResponseSampler;
import net.xmeter.samplers.DisConnectSampler;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class AsyncResponseSamplerUI extends AbstractSamplerGui implements Constants {
	private static final long serialVersionUID = 1666890646673145131L;

	public AsyncResponseSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
	}

	@Override
	public TestElement createTestElement() {
		AsyncResponseSampler sampler = new AsyncResponseSampler();
		this.configureTestElement(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "MQTT Async Response";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		AsyncResponseSampler sampler = (AsyncResponseSampler) arg0;
		this.configureTestElement(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
	}

}
