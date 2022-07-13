package org.expleo.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import org.expleo.Constants;
import org.expleo.samplers.ConnectSampler;

public class ConnectSamplerUI extends AbstractSamplerGui implements Constants {
	private CommonConnUI connUI = new CommonConnUI();
	private static final long serialVersionUID = 1666890646673145131L;

	public ConnectSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(connUI.createConnPanel());
		mainPanel.add(connUI.createProtocolPanel()); 
		mainPanel.add(connUI.createAuthentication());
		mainPanel.add(connUI.createConnOptions());
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		ConnectSampler sampler = (ConnectSampler)element;
		connUI.configure(sampler);
	}

	@Override
	public TestElement createTestElement() {
		ConnectSampler sampler = new ConnectSampler();
		this.configureTestElement(sampler);
		connUI.setupSamplerProperties(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "MQTT Connect";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		ConnectSampler sampler = (ConnectSampler)arg0;
		this.configureTestElement(sampler);
		connUI.setupSamplerProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		connUI.clearUI();
	}

}
