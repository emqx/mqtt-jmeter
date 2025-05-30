package net.xmeter.gui;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.*;
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

import net.xmeter.Constants;
import net.xmeter.samplers.PubSampler;

public class PubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener {
	private static final long serialVersionUID = 2479085966683186422L;
	private static final Logger logger = Logger.getLogger(PubSamplerUI.class.getCanonicalName());

	private JLabeledChoice qosChoice;
	private final JLabeledTextField retainedMsg = new JLabeledTextField("Retained messages:", 1);
	private final JLabeledTextField topicName = new JLabeledTextField("Topic name:");
	private JCheckBox timestamp = new JCheckBox("Add timestamp in payload");

	private final JLabeledTextField messageExpiryInterval = new JLabeledTextField("Message Expiry Interval(s):");
	private final JLabeledTextField contentType = new JLabeledTextField("Content Type:");
	private final JLabeledTextField responseTopic = new JLabeledTextField("Response Topic:");
	private final JLabeledTextField correlationData = new JLabeledTextField("Correlation Data:");
	private final JLabeledTextField userProperties = new JLabeledTextField("User Properties:");

	private final JLabel qosLabel = new JLabel("QoS Level:");
	private final JLabel payloadFormatLabel = new JLabel("Payload Format:");
	private final JLabeledChoice payloadFormat = new JLabeledChoice("Payload Format:", new String[] { "UNSPECIFIED", "UTF_8" }, false, false);
	private final JLabeledTextField topicAlias = new JLabeledTextField("Topic Alias:");
	private final JLabeledTextField subscriptionIdentifier = new JLabeledTextField("Subscription Identifier:");

	private JLabeledChoice messageTypes;
	private final JSyntaxTextArea sendMessage = JSyntaxTextArea.getInstance(10, 50);
	private final JTextScrollPane messagePanel = JTextScrollPane.getInstance(sendMessage);
	private JLabeledTextField stringLength = new JLabeledTextField("Length:");

	public PubSamplerUI() {
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(createPubOption());
		mainPanel.add(createPayload());
	}

	private JPanel createPubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Pub options"));

		qosChoice = new JLabeledChoice("QoS Level:", new String[] { String.valueOf(QOS_0), String.valueOf(QOS_1), String.valueOf(QOS_2) }, true, false);
		qosChoice.addChangeListener(this);

		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(qosLabel);
		optsPanel.add(qosChoice);
		optsPanel.add(retainedMsg);
		optsPanel.add(topicName);
		topicName.setToolTipText("Name of topic that the message will be sent to.");
		optsPanel.add(timestamp);
		optsPanelCon.add(optsPanel);

		JPanel optsPanel1 = new HorizontalPanel();
		optsPanel1.add(userProperties);
		optsPanel1.add(messageExpiryInterval);
		optsPanel1.add(topicAlias);
		optsPanel1.add(payloadFormatLabel);
		optsPanel1.add(payloadFormat);
		payloadFormat.setToolTipText("Payload format indicator to the message");
		optsPanelCon.add(optsPanel1);

		JPanel optsPanel2 = new HorizontalPanel();
		optsPanel2.add(contentType);
		optsPanel2.add(responseTopic);
		optsPanel2.add(correlationData);
		optsPanel2.add(subscriptionIdentifier);
		optsPanelCon.add(optsPanel2);

		return optsPanelCon;
	}

	private JPanel createPayload() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Payloads"));
		
		JPanel horizon1 = new HorizontalPanel();
		messageTypes = new JLabeledChoice("Message type:", new String[] { MESSAGE_TYPE_STRING, MESSAGE_TYPE_HEX_STRING, MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN }, false, false);
		messageTypes.addChangeListener(this);
		messageTypes.setSelectedIndex(0);
		
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
			int selectedIndex = this.messageTypes.getSelectedIndex();
			if(selectedIndex == 0 || selectedIndex == 1) {
				stringLength.setVisible(false);
				messagePanel.setVisible(true);
			} else if(selectedIndex == 2) {
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
		
		if(sampler.getQOS().trim().contains(JMETER_VARIABLE_PREFIX)){
			this.qosChoice.setSelectedIndex(Integer.parseInt(sampler.getQOS()));	
		} else {
			this.qosChoice.setText(sampler.getQOS());
		}
		
		this.topicName.setText(sampler.getTopic());
		this.retainedMsg.setText(sampler.getRetainedMessage().toString());
		this.timestamp.setSelected(sampler.isAddTimestamp());
		if(MESSAGE_TYPE_STRING.equalsIgnoreCase(sampler.getMessageType())) {
			this.messageTypes.setSelectedIndex(0);	
			this.messagePanel.setVisible(true);
		} else if(MESSAGE_TYPE_HEX_STRING.equalsIgnoreCase(sampler.getMessageType())) {
			this.messageTypes.setSelectedIndex(1);
		} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equalsIgnoreCase(sampler.getMessageType())) {
			this.messageTypes.setSelectedIndex(2);
		}
		
		stringLength.setText(String.valueOf(sampler.getMessageLength()));
		sendMessage.setText(sampler.getMessage());

		contentType.setText(sampler.getContentType());
		messageExpiryInterval.setText(String.valueOf(sampler.getMessageExpiryInterval()));
		userProperties.setText(sampler.getUserProperties());
		responseTopic.setText(sampler.getResponseTopic());
		correlationData.setText(sampler.getCorrelationData());
		payloadFormat.setText(sampler.getPayloadFormat());
		topicAlias.setText(sampler.getTopicAlias());
		subscriptionIdentifier.setText(sampler.getSubscriptionIdentifier());
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		PubSampler sampler = (PubSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(PubSampler sampler) {
		this.configureTestElement(sampler);
		sampler.setTopic(this.topicName.getText());
		
		if(this.qosChoice.getText().contains(JMETER_VARIABLE_PREFIX)) {
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
			sampler.setQOS(String.valueOf(qos));
		} else {
			sampler.setQOS(this.qosChoice.getText());
		}
		
		sampler.setAddTimestamp(this.timestamp.isSelected());
		sampler.setMessageType(this.messageTypes.getText());
		sampler.setMessageLength(this.stringLength.getText());
		sampler.setMessage(this.sendMessage.getText());
		sampler.setRetainedMessage(Boolean.parseBoolean(this.retainedMsg.getText()));

		sampler.setContentType(this.contentType.getText());
		sampler.setCorrelationData(this.correlationData.getText());
		sampler.setMessageExpiryInterval(Long.parseLong(this.messageExpiryInterval.getText()));
		sampler.setUserProperties(this.userProperties.getText());
		sampler.setResponseTopic(this.responseTopic.getText());
		sampler.setPayloadFormat(this.payloadFormat.getText());
		sampler.setTopicAlias(this.topicAlias.getText());
		sampler.setSubscriptionIdentifier(this.subscriptionIdentifier.getText());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		this.topicName.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		this.timestamp.setSelected(false);
		
		this.messageTypes.setSelectedIndex(0);
		this.stringLength.setText(String.valueOf(DEFAULT_MESSAGE_FIX_LENGTH));
		this.sendMessage.setText("");

		this.messageExpiryInterval.setText("0");
		this.responseTopic.setText("");
		this.contentType.setText("");
		this.userProperties.setText("{}");
		this.correlationData.setText("");
		this.payloadFormat.setSelectedIndex(0);
		this.topicAlias.setText("");
		this.subscriptionIdentifier.setText("");
	}
}
