package net.xmeter.gui;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

import net.xmeter.Constants;
import net.xmeter.samplers.EfficientConnectSampler;

public class EfficientConnectSamplerUI extends AbstractSamplerGui implements Constants {
	private static final long serialVersionUID = 1666890646673145131L;
	private static final Logger logger = Logger.getLogger(SubSamplerUI.class.getCanonicalName());

	private CommonConnUI connUI = new CommonConnUI();
	private JCheckBox shouldSub = new JCheckBox("Subscribe when connected");
	private JLabeledTextField connCapacity  = new JLabeledTextField("Connection capacity:");;
	private JLabeledChoice qosChoice;
	private final JLabeledTextField topicNames = new JLabeledTextField("Topic name(s):");
	
	public EfficientConnectSamplerUI() {
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
		mainPanel.add(createSubOption());
		mainPanel.add(createConCapacityOption());
	}
	
	private JPanel createSubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Subscription options"));
		
		JPanel optsPanel0 = new HorizontalPanel();
		optsPanel0.add(shouldSub);
		shouldSub.setSelected(false);
		optsPanelCon.add(optsPanel0);
		
		JPanel optsPanel1 = new HorizontalPanel();
		qosChoice = new JLabeledChoice("QoS Level:", new String[] { String.valueOf(QOS_0), String.valueOf(QOS_1), String.valueOf(QOS_2) }, true, false);
		optsPanel1.add(qosChoice);
		optsPanel1.add(topicNames);
		optsPanelCon.add(optsPanel1);
		
		return optsPanelCon;
	}
	
	private JPanel createConCapacityOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connection capacity options"));
		
		JPanel optsPanel0 = new HorizontalPanel();
		optsPanel0.add(connCapacity);
		optsPanelCon.add(optsPanel0);
		
		return optsPanelCon;
	}

	@Override
	public void configure(TestElement element) {
		super.configure(element);
		EfficientConnectSampler sampler = (EfficientConnectSampler)element;
		connUI.configure(sampler);
		//
		shouldSub.setSelected(sampler.isSubWhenConnected());
		if(sampler.getQOS().trim().indexOf(JMETER_VARIABLE_PREFIX) == -1){
			this.qosChoice.setSelectedIndex(Integer.parseInt(sampler.getQOS()));	
		} else {
			this.qosChoice.setText(sampler.getQOS());
		}
		this.topicNames.setText(sampler.getTopics());
		//
		connCapacity.setText(sampler.getConnCapacity());
	}

	@Override
	public TestElement createTestElement() {
		EfficientConnectSampler sampler = new EfficientConnectSampler();
		this.configureTestElement(sampler);
		connUI.setupSamplerProperties(sampler);
		this.setupSamplerProperties(sampler);
		return sampler;
	}
	
	private void setupSamplerProperties(EfficientConnectSampler sampler) {
		sampler.setSubWhenConnected(shouldSub.isSelected());
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
				qos = QOS_0;
			}
			sampler.setQOS(String.valueOf(qos));
		} else {
			sampler.setQOS(this.qosChoice.getText());
		}
		sampler.setTopics(this.topicNames.getText());
		sampler.setConnCapacity(this.connCapacity.getText());
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
	public void modifyTestElement(TestElement element) {
		EfficientConnectSampler sampler = (EfficientConnectSampler)element;
		this.configureTestElement(sampler);
		connUI.setupSamplerProperties(sampler);
		this.setupSamplerProperties(sampler);
	}

	@Override
	public void clearGui() {
		super.clearGui();
		connUI.clearUI();
		
		shouldSub.setSelected(DEFAULT_SUBSCRIBE_WHEN_CONNECTED);
		this.topicNames.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		
		this.connCapacity.setText("1");
	}
	
}
