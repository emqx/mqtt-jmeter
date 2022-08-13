package net.xmeter.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import net.xmeter.Constants;
import net.xmeter.samplers.DisConnectSampler;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

public class DisConnectSamplerUI extends AbstractSamplerGui implements Constants {
	private static final long serialVersionUID = 1666890646673145131L;
	private final JLabeledTextField connName = new JLabeledTextField("MQTT Conn Name:");

	public DisConnectSamplerUI() {
		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(connName);
		mainPanel.add(optsPanel);
		add(mainPanel, BorderLayout.CENTER);
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		DisConnectSampler sampler = (DisConnectSampler) element;
		this.connName.setText(sampler.getConnName());
	}

	@Override
	public TestElement createTestElement() {
		DisConnectSampler sampler = new DisConnectSampler();
		this.configureTestElement(sampler);
		return sampler;
	}

	@Override
	public String getLabelResource() {
		throw new RuntimeException();
	}

	@Override
	public String getStaticLabel() {
		return "MQTT DisConnect";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		DisConnectSampler sampler = (DisConnectSampler)arg0;
		this.configureTestElement(sampler);
		sampler.setConnName(this.connName.getText());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		this.connName.setText(DEFAULT_MQTT_CONN_NAME);
	}

}
