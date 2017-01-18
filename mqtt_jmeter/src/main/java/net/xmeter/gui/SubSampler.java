package net.xmeter.gui;

import java.awt.BorderLayout;

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

public class SubSampler extends AbstractSamplerGui implements Constants, ChangeListener{
	private static final Logger logger = LoggingManager.getLoggerForClass();
	
	private CommonConnUI connUI = new CommonConnUI();
	
	private JLabeledChoice qosChoice;
	
	private final JLabeledTextField topicName = new JLabeledTextField("Topic name:");
	private JCheckBox timestamp = new JCheckBox("Add timestamp in payload");
	/**
	 * 
	 */
	private static final long serialVersionUID = -1715399546099472610L;

	public SubSampler() {
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
	
	@Override
	public TestElement createTestElement() {
		return null;
	}

	@Override
	public String getLabelResource() {
		return null;
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		
	}
}
