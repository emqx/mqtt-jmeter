package net.xmeter.gui;

import java.awt.BorderLayout;
import java.util.logging.Logger;

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

import net.xmeter.Constants;
import net.xmeter.samplers.SubSampler;

public class SubSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener{
	private static final long serialVersionUID = 1715399546099472610L;
	private static final Logger logger = Logger.getLogger(SubSamplerUI.class.getCanonicalName());
	
	private JLabeledChoice qosChoice;
	private JLabeledChoice sampleOnCondition;
	
	private final JLabeledTextField sampleConditionValue = new JLabeledTextField("");
	private final JLabeledTextField topicNames = new JLabeledTextField("Topic name(s):");
	
	private JCheckBox debugResponse = new JCheckBox("Debug response");
	private JCheckBox timestamp = new JCheckBox("Payload includes timestamp");
	private JCheckBox async = new JCheckBox("Async");
	
	public SubSamplerUI() {
		this.init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(createSubOption());
	}
	
	private JPanel createSubOption() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sub options"));

		qosChoice = new JLabeledChoice("QoS Level:", new String[] { String.valueOf(QOS_0), String.valueOf(QOS_1), String.valueOf(QOS_2) }, true, false);
		sampleOnCondition = new JLabeledChoice("Sample on:", new String[] {SAMPLE_ON_CONDITION_OPTION1, SAMPLE_ON_CONDITION_OPTION2});

		JPanel optsPanel1 = new HorizontalPanel();
		optsPanel1.add(qosChoice);
		optsPanel1.add(topicNames);
		topicNames.setToolTipText("A list of topics to be subscribed to, comma-separated.");
		optsPanel1.add(timestamp);
		optsPanelCon.add(optsPanel1);
		
		JPanel optsPanel3 = new HorizontalPanel();
		sampleOnCondition.addChangeListener(this);
		optsPanel3.add(sampleOnCondition);
		optsPanel3.add(sampleConditionValue);
		sampleOnCondition.setToolTipText("When sub sampler should report out.");
		sampleConditionValue.setToolTipText("Please specify an integer value great than 0, other values will be ignored.");
		optsPanelCon.add(optsPanel3);
		
		JPanel optsPanel2 = new HorizontalPanel();
		optsPanel2.add(debugResponse);
		optsPanel2.add(async);
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

		if(sampler.getQOS().trim().indexOf(JMETER_VARIABLE_PREFIX) == -1){
			this.qosChoice.setSelectedIndex(Integer.parseInt(sampler.getQOS()));	
		} else {
			this.qosChoice.setText(sampler.getQOS());
		}
		
		this.topicNames.setText(sampler.getTopics());
		this.timestamp.setSelected(sampler.isAddTimestamp());
		this.debugResponse.setSelected(sampler.isDebugResponse());
		this.async.setSelected(sampler.isAsync());
		this.sampleOnCondition.setText(sampler.getSampleCondition());

		if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
			this.sampleConditionValue.setText(sampler.getSampleElapsedTime());
		} else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
			this.sampleConditionValue.setText(sampler.getSampleCount());
		}
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
		sampler.setTopics(this.topicNames.getText());
		
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
		
		sampler.setAddTimestamp(this.timestamp.isSelected());
		sampler.setDebugResponse(this.debugResponse.isSelected());
		sampler.setAsync(this.async.isSelected());
		sampler.setSampleCondition(this.sampleOnCondition.getText());
		
		if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
			sampler.setSampleElapsedTime(this.sampleConditionValue.getText());
		} else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
			sampler.setSampleCount(this.sampleConditionValue.getText());
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		this.topicNames.setText(DEFAULT_TOPIC_NAME);
		this.qosChoice.setText(String.valueOf(QOS_0));
		this.timestamp.setSelected(false);
		this.debugResponse.setSelected(false);
		this.async.setSelected(false);
		this.sampleOnCondition.setText(SAMPLE_ON_CONDITION_OPTION1);
		this.sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(this.sampleOnCondition == e.getSource()) {
			if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
				sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC);
			} else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
				sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_COUNT);
			}
		}
	}
	
}
