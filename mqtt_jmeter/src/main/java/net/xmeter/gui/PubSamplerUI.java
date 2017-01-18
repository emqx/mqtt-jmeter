package net.xmeter.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import net.xmeter.Constants;
import net.xmeter.samplers.PubSampler;

public class PubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener {
	private static final Logger logger = LoggingManager.getLoggerForClass();
	private CommonConnUI connUI = new CommonConnUI();
	/**
	 * 
	 */
	private static final long serialVersionUID = 2479085966683186422L;

	private JLabeledChoice qosChoice;
	private final JLabeledTextField topicName = new JLabeledTextField("Topic name:");
	private JCheckBox timestamp = new JCheckBox("Add timestamp in payload");

	private JLabeledChoice messageTypes;
	private final JSyntaxTextArea sendMessage = JSyntaxTextArea.getInstance(10, 50);
	private final JTextScrollPane messagePanel = JTextScrollPane.getInstance(sendMessage);
	private JLabeledTextField stringLength = new JLabeledTextField("Length:");

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

		mainPanel.add(createPubOption());
		mainPanel.add(createPayload());
	}

	private JPanel createPubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Pub options"));

		qosChoice = new JLabeledChoice("QoS Level:", new String[] { String.valueOf(QOS_0), String.valueOf(QOS_1), String.valueOf(QOS_2) }, true, false);
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
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Payloads"));
		
		JPanel horizon1 = new HorizontalPanel();
		messageTypes = new JLabeledChoice("Message type:", new String[] { MESSAGE_TYPE_STRING, MESSAGE_TYPE_HEX_STRING, MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN }, false, false);
		messageTypes.setSelectedIndex(0);
		messageTypes.addChangeListener(this);
		horizon1.add(messageTypes, BorderLayout.WEST);
		stringLength.setVisible(false);
		horizon1.add(stringLength);
		
		JPanel horizon2 = new VerticalPanel();
		messagePanel.setVisible(false);
		horizon2.add(messagePanel);
		
		optsPanelCon.add(horizon1);
		optsPanelCon.add(horizon2);
		return optsPanelCon;
	}

	@Override
	public String getStaticLabel() {
		return "MQTT Pub Sampler";
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == this.messageTypes) {
			String messageType = messageTypes.getText();
			if(MESSAGE_TYPE_STRING.equalsIgnoreCase(messageType) || MESSAGE_TYPE_HEX_STRING.equalsIgnoreCase(messageType)) {
				stringLength.setVisible(false);
				messagePanel.setVisible(true);
			} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(messageType)) {
				messagePanel.setVisible(false);
				stringLength.setVisible(true);
			} else {
				logger.info("Unknown message type.");
			}
		}
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
		PubSampler sampler = (PubSampler) element;
		setupSamplerProperties(sampler);
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		PubSampler sampler = (PubSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(PubSampler sampler) {
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
		
		sampler.setMessageType(this.messageTypes.getText());
		sampler.setMessageLength(Integer.parseInt(this.stringLength.getText()));
		sampler.setMessage(this.sendMessage.getText());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		connUI.clearUI();
		this.topicName.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		this.timestamp.setSelected(false);
		
		this.messageTypes.setSelectedIndex(0);
		this.stringLength.setText(String.valueOf(DEFAULT_MESSAGE_FIX_LENGTH));
		this.sendMessage.setText("");
	}
}
