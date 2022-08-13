package net.xmeter.gui;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.xmeter.Constants;
import net.xmeter.samplers.PubSampler;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

public class PubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener {
	private static final long serialVersionUID = 2479085966683186422L;
	private static final Logger logger = Logger.getLogger(PubSamplerUI.class.getCanonicalName());

	private static final JLabel qosLabel = new JLabel("QOS Level:");
	private final JLabeledTextField connName = new JLabeledTextField("MQTT Conn Name:");
	private JLabeledChoice qosChoice;
	private final JLabeledTextField retainedMsg = new JLabeledTextField("Retained messages:", 1);
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
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(createPubOption());
		mainPanel.add(createPayload());
		mainPanel.add(createConnOptions());
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

	public JPanel createConnOptions() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connection options"));

		JPanel optsPanel0 = new HorizontalPanel();
		optsPanel0.add(connName);
		optsPanelCon.add(optsPanel0);

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

		this.connName.setText(sampler.getConnName());
		if (sampler.getQOS().trim().indexOf(JMETER_VARIABLE_PREFIX) == -1){
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
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		PubSampler sampler = (PubSampler) arg0;
		this.setupSamplerProperties(sampler);
	}

	private void setupSamplerProperties(PubSampler sampler) {
		this.configureTestElement(sampler);
		sampler.setConnName(this.connName.getText());
		sampler.setTopic(this.topicName.getText());
		
		if(this.qosChoice.getText().indexOf(JMETER_VARIABLE_PREFIX) == -1) {
			int qos = QOS_0;
			try {
				qos = Integer.parseInt(this.qosChoice.getText());
				if (qos < QOS_0 || qos > QOS_2) {
					qos = QOS_0;
					logger.info("Invalid QoS value, set to default QoS value 0.");
				}
			} catch (Exception ex) {
				logger.info("Invalid QoS value, set to default QoS value 0.");
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
	}

	@Override
	public void clearGui() {
		super.clearGui();
		this.connName.setText(DEFAULT_MQTT_CONN_NAME);
		this.topicName.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		this.timestamp.setSelected(false);
		
		this.messageTypes.setSelectedIndex(0);
		this.stringLength.setText(DEFAULT_MESSAGE_FIX_LENGTH);
		this.sendMessage.setText("");
	}
}
