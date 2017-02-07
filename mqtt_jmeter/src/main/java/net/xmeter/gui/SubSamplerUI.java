package net.xmeter.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import net.xmeter.Constants;
import net.xmeter.samplers.SubSampler;

public class SubSamplerUI extends AbstractSamplerGui implements Constants{
	private static final Logger logger = LoggingManager.getLoggerForClass();
	
	private CommonConnUI connUI = new CommonConnUI();
	
	private JLabeledChoice qosChoice;
	
	private final JLabeledTextField topicName = new JLabeledTextField("Topic name:");
	private JCheckBox debugResponse = new JCheckBox("Debug response");
	private JCheckBox timestamp = new JCheckBox("Payload includes timestamp");
	/**
	 * 
	 */
	private static final long serialVersionUID = -1715399546099472610L;

	public SubSamplerUI() {
		this.init();
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

		mainPanel.add(createSubOption());
	}
	
	private JPanel createSubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sub options"));

		qosChoice = new JLabeledChoice("QoS Level:", new String[] { String.valueOf(QOS_0), String.valueOf(QOS_1), String.valueOf(QOS_2) }, true, false);

		JPanel optsPanel1 = new HorizontalPanel();
		optsPanel1.add(qosChoice);
		optsPanel1.add(topicName);
		optsPanel1.add(timestamp);
		optsPanelCon.add(optsPanel1);
		
		JPanel optsPanel2 = new HorizontalPanel();
		optsPanel2.add(debugResponse);
		optsPanelCon.add(optsPanel2);

		return optsPanelCon;
	}
	
	@Override
	public String getStaticLabel() {
		return "MQTT Sub Sampler";
	}
	
	@Override
	public TestElement createTestElement() {
		SubSampler sampler = new SubSampler();
		this.setupSamplerProperties(sampler);
		return sampler;
	}
	
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		SubSampler sampler = (SubSampler) element;
		connUI.configure(sampler);
		this.qosChoice.setSelectedIndex(sampler.getQOS());
		this.topicName.setText(sampler.getTopic());
		this.timestamp.setSelected(sampler.isAddTimestamp());
		this.debugResponse.setSelected(sampler.isDebugResponse());
	}

	@Override
	public String getLabelResource() {
		return "";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		SubSampler sampler = (SubSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(SubSampler sampler) {
		this.configureTestElement(sampler);
		connUI.setupSamplerProperties(sampler);
		sampler.setTopic(this.topicName.getText());
		
		int qos = QOS_0;
		try {
			qos = Integer.parseInt(this.qosChoice.getText());
			if (qos < QOS_0 || qos > QOS_2) {
				qos = QOS_0;
				logger.info("Invalid QoS value, set to default QoS value 0.");
			}
		} catch (Exception ex) {
			logger.info("Invalid QoS value, set to default QoS value 0.");
			qos = QOS_0;
		}
		sampler.setQOS(qos);
		sampler.setAddTimestamp(this.timestamp.isSelected());
		sampler.setDebugResponse(this.debugResponse.isSelected());
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		connUI.clearUI();
		connUI.connNamePrefix.setText(DEFAULT_CONN_PREFIX_FOR_SUB);
		this.topicName.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		this.timestamp.setSelected(false);
		this.debugResponse.setSelected(false);
	}
	
}
