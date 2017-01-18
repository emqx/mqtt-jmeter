package net.xmeter.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import net.xmeter.Constants;
import net.xmeter.samplers.PubSampler;

public class PubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener, ActionListener{
	private static final Logger logger = LoggingManager.getLoggerForClass();
	private CommonConnUI connUI = new CommonConnUI();
	/**
	 * 
	 */
	private static final long serialVersionUID = 2479085966683186422L;

	private JLabeledChoice qosChoice;
	private final JLabeledTextField topicName = new JLabeledTextField("Topic name:");
	private JCheckBox timestamp = new JCheckBox("Add timestamp in payload");
	
	public PubSamplerUI() {
		init();
	}
	
	private void init() {
		logger.info("Initializing the UI.");
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(connUI.createConnPanel());
		mainPanel.add(connUI.createProtocolPanel()); 
		mainPanel.add(connUI.createAuthentication());
		mainPanel.add(connUI.createConnOptions());
		
		mainPanel.add(createMoreElements(mainPanel));
	}
	
	private JPanel createMoreElements(JPanel mainPanel) {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Pub options"));
		
		qosChoice = new JLabeledChoice("QoS Level:", new String[] { String.valueOf(QOS_0), String.valueOf(QOS_1), String.valueOf(QOS_2)}, true, false);
		qosChoice.addChangeListener(this);
		
		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(qosChoice);
		optsPanel.add(topicName);
		optsPanel.add(timestamp);
		optsPanelCon.add(optsPanel);
		
		return optsPanelCon;
	}
	
	
	private JPanel createPayload() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options"));
		
		JPanel optsPanel = new HorizontalPanel();
		optsPanelCon.add(optsPanel);
		
		return optsPanelCon;
	}
	
	
	
	@Override
	public String getStaticLabel() {
		return "MQTT Pub Sampler";
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		
	}


	@Override
	public String getLabelResource() {
		return "";
	}

	@Override
	public TestElement createTestElement() {
		PubSampler sampler = new PubSampler();
		this.setupSamplerProperties(sampler);
		return sampler;
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		PubSampler sampler = (PubSampler)element;
		setupSamplerProperties(sampler);
	}
	
	@Override
	public void modifyTestElement(TestElement arg0) {
		PubSampler sampler = (PubSampler)arg0;
		this.setupSamplerProperties(sampler);
	}
	
	private void setupSamplerProperties(PubSampler sampler) {
		this.configureTestElement(sampler);
		
		connUI.setupSamplerProperties(sampler);
		
		sampler.setTopic(this.topicName.getText());
		int qos = QOS_0;
		try {
			qos = Integer.parseInt(this.qosChoice.getText());
			if(qos < QOS_0 || qos > QOS_2) {
				qos = QOS_0;
				logger.info("Invalid QoS value, set to default QoS value 0.");
			}
		} catch(Exception ex) {
			logger.info("Invalid QoS value, set to default QoS value 0.");
			qos = QOS_0;
		}
		sampler.setQOS(qos);
		sampler.setAddTimestamp(this.timestamp.isSelected());
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		connUI.clearUI();
		this.topicName.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		this.timestamp.setSelected(false);
	}
}
