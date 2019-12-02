package net.xmeter.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

import net.xmeter.Constants;
import net.xmeter.Util;
import net.xmeter.samplers.AbstractMQTTSampler;
import net.xmeter.samplers.mqtt.MQTT;

public class CommonConnUI implements ChangeListener, ActionListener, Constants{
	private final JLabeledTextField serverAddr = new JLabeledTextField("Server name or IP:");
	private final JLabeledTextField serverPort = new JLabeledTextField("Port number:", 5);
	private JLabeledChoice mqttVersion = new JLabeledChoice("MQTT version:", new String[] { MQTT_VERSION_3_1, MQTT_VERSION_3_1_1 }, false, false);;
	private final JLabeledTextField timeout = new JLabeledTextField("Timeout(s):", 5);
	
	private final JLabeledTextField userNameAuth = new JLabeledTextField("User name:");
	private final JLabeledTextField passwordAuth = new JLabeledTextField("Password:");

	private JLabeledChoice protocols;
	private JLabeledChoice clientNames;

	private JCheckBox dualAuth = new JCheckBox("Dual SSL authentication");
	private JLabeledTextField wsPath = new JLabeledTextField("WS Path: ", 10);

	private final JLabeledTextField tksFilePath = new JLabeledTextField("Trust Key Store(*.jks):       ", 25);
	private final JLabeledTextField ccFilePath = new JLabeledTextField("Client Certification(*.p12):", 25);
	
	private final JLabeledTextField tksPassword = new JLabeledTextField("Secret:", 10);
	private final JLabeledTextField ccPassword = new JLabeledTextField("Secret:", 10);

	private JButton tksBrowseButton;
	private JButton ccBrowseButton;
	private static final String TKS_BROWSE = "tks_browse";
	private static final String CC_BROWSE = "cc_browse";
	
	public final JLabeledTextField connNamePrefix = new JLabeledTextField("ClientId:", 8);
	private JCheckBox connNameSuffix = new JCheckBox("Add random suffix for ClientId");
	
	private final JLabeledTextField connKeepAlive = new JLabeledTextField("Keep alive(s):", 3);
	
	private final JLabeledTextField connAttmptMax = new JLabeledTextField("Connect attampt max:", 3);
	private final JLabeledTextField reconnAttmptMax = new JLabeledTextField("Reconnect attampt max:", 3);

	private final JLabeledTextField connCleanSession = new JLabeledTextField("Clean session:", 3);

	private final List<String> clientNamesList = MQTT.getAvailableNames();

	public JPanel createConnPanel() {
		JPanel con = new HorizontalPanel();
		
		JPanel connPanel = new HorizontalPanel();
		connPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "MQTT connection"));
		connPanel.add(serverAddr);
		connPanel.add(serverPort);
		connPanel.add(mqttVersion);
		
		JPanel timeoutPannel = new HorizontalPanel();
		timeoutPannel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Timeout"));
		timeoutPannel.add(timeout);

		con.add(connPanel);
		con.add(timeoutPannel);
		return con;
	}
	
	public JPanel createConnOptions() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Connection options"));
		
		JPanel optsPanel0 = new HorizontalPanel();
		optsPanel0.add(connNamePrefix);
		optsPanel0.add(connNameSuffix);
		connNameSuffix.setSelected(true);
		optsPanelCon.add(optsPanel0);
		
		JPanel optsPanel1 = new HorizontalPanel();
		optsPanel1.add(connKeepAlive);
		optsPanelCon.add(optsPanel1);
		
		optsPanel1.add(connAttmptMax);
		optsPanel1.add(reconnAttmptMax);
		optsPanel1.add(connCleanSession);
		optsPanelCon.add(optsPanel1);
		
		return optsPanelCon;
	}
	
	public JPanel createAuthentication() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "User authentication"));
		
		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(userNameAuth);
		optsPanel.add(passwordAuth);
		optsPanelCon.add(optsPanel);
		
		return optsPanelCon;
	}

	public JPanel createProtocolPanel() {
		JPanel protocolPanel = new VerticalPanel();
		protocolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Protocols"));
		
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BorderLayout());
		//pPanel.setLayout(new GridLayout(1, 2));

		JPanel pCenter = new JPanel(new FlowLayout(FlowLayout.LEFT));
		clientNames = new JLabeledChoice("Clients:", clientNamesList.toArray(new String[] {}), true, false);
		clientNames.addChangeListener(this);
		pCenter.add(clientNames);

		protocols = new JLabeledChoice("Protocols:", false);
		//JComboBox<String> component = (JComboBox) protocols.getComponentList().get(1);
		//component.setSize(new Dimension(40, component.getHeight()));
		protocols.addChangeListener(this);
		pCenter.add(protocols);

		wsPath.setFont(null);
		wsPath.setVisible(false);
		pCenter.add(wsPath);

		pPanel.add(pCenter, BorderLayout.CENTER);

		dualAuth.setSelected(false);
		dualAuth.setFont(null);
		dualAuth.setVisible(false);
		dualAuth.addChangeListener(this);
		pPanel.add(dualAuth, BorderLayout.SOUTH);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.SOUTHWEST;
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
		tksFilePath.setVisible(false);
		panel.add(tksFilePath, c);

		c.gridx = 2; c.gridy = 0; c.gridwidth = 1;
		tksBrowseButton = new JButton(JMeterUtils.getResString("browse"));
		tksBrowseButton.setActionCommand(TKS_BROWSE);
		tksBrowseButton.addActionListener(this);
		tksBrowseButton.setVisible(false);
		panel.add(tksBrowseButton, c);
		
		c.gridx = 3; c.gridy = 0; c.gridwidth = 2;
		tksPassword.setVisible(false);
		panel.add(tksPassword, c);

		//c.weightx = 0.0;
		c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
		ccFilePath.setVisible(false);
		panel.add(ccFilePath, c);

		c.gridx = 2; c.gridy = 1; c.gridwidth = 1;
		ccBrowseButton = new JButton(JMeterUtils.getResString("browse"));
		ccBrowseButton.setActionCommand(CC_BROWSE);
		ccBrowseButton.addActionListener(this);
		ccBrowseButton.setVisible(false);
		panel.add(ccBrowseButton, c);
		
		c.gridx = 3; c.gridy = 1; c.gridwidth = 2;
		ccPassword.setVisible(false);
		panel.add(ccPassword, c);
		
		protocolPanel.add(pPanel);
		protocolPanel.add(panel);
		
		return protocolPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(TKS_BROWSE.equals(action)) {
			String path = browseAndGetFilePath();
			tksFilePath.setText(path);
		}else if(CC_BROWSE.equals(action)) {
			String path = browseAndGetFilePath();
			ccFilePath.setText(path);
		}
	}
	private String browseAndGetFilePath() {
		String path = "";
		JFileChooser chooser = FileDialoger.promptToOpenFile();
		if (chooser != null) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				path = file.getPath();
			}
		}
		return path;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == dualAuth) {
			if(dualAuth.isSelected()) {
				tksFilePath.setVisible(true);
				tksBrowseButton.setVisible(true);
				tksPassword.setVisible(true);
				ccFilePath.setVisible(true);
				ccBrowseButton.setVisible(true);
				ccPassword.setVisible(true);
			} else {
				tksFilePath.setVisible(false);
				tksBrowseButton.setVisible(false);
				tksPassword.setVisible(false);
				ccFilePath.setVisible(false);
				ccBrowseButton.setVisible(false);
				ccPassword.setVisible(false);
			}
		} else if(e.getSource() == protocols) {
			boolean isSecure = Util.isSecureProtocol(protocols.getText());
			dualAuth.setVisible(isSecure);
			dualAuth.setEnabled(isSecure);
			boolean wsProtocol = Util.isWebSocketProtocol(protocols.getText());
			wsPath.setVisible(wsProtocol);
			wsPath.setEnabled(wsProtocol);
		} else if (e.getSource() == clientNames) {
			int index = clientNames.getSelectedIndex();
			if (index > -1) {
				String clientName = clientNames.getItems()[index];
				List<String> supportedProtocols = MQTT.getSupportedProtocols(clientName);
				protocols.setValues(supportedProtocols.toArray(new String[supportedProtocols.size()]));
			} else {
				protocols.setValues(new String[0]);
			}
		}
	}

	public void configure(AbstractMQTTSampler sampler) {
		serverAddr.setText(sampler.getServer());
		serverPort.setText(sampler.getPort());
		if(sampler.getMqttVersion().equals(MQTT_VERSION_3_1)) {
			mqttVersion.setSelectedIndex(0);
		} else if(sampler.getMqttVersion().equals(MQTT_VERSION_3_1_1)) {
			mqttVersion.setSelectedIndex(1);
		}
		timeout.setText(sampler.getConnTimeout());

		if (sampler.getProtocol().trim().indexOf(JMETER_VARIABLE_PREFIX) == -1) {
			int index = clientNamesList.indexOf(sampler.getMqttClientName());
			clientNames.setSelectedIndex(index);
		} else{
			clientNames.setText(sampler.getMqttClientName());
		}

		if(sampler.getProtocol().trim().indexOf(JMETER_VARIABLE_PREFIX) == -1) {
			List<String> items = Arrays.asList(protocols.getItems());
			int index = items.indexOf(sampler.getProtocol());
			protocols.setSelectedIndex(index);
		} else {
			protocols.setText(sampler.getProtocol());
		}

		boolean wsProtocol = Util.isWebSocketProtocol(sampler.getProtocol());
		wsPath.setText(sampler.getWsPath());
		wsPath.setVisible(wsProtocol);
		wsPath.setEnabled(wsProtocol);

		if(sampler.isDualSSLAuth()) {
			dualAuth.setVisible(true);
			dualAuth.setSelected(sampler.isDualSSLAuth());	
		}
		tksFilePath.setText(sampler.getKeyStoreFilePath());
		tksPassword.setText(sampler.getKeyStorePassword());
		ccFilePath.setText(sampler.getClientCertFilePath());
		ccPassword.setText(sampler.getClientCertPassword());

		userNameAuth.setText(sampler.getUserNameAuth());
		passwordAuth.setText(sampler.getPasswordAuth());
		
		connNamePrefix.setText(sampler.getConnPrefix());
		if(sampler.isClientIdSuffix()) {
			connNameSuffix.setSelected(true);
		} else {
			connNameSuffix.setSelected(false);
		}
		
		connKeepAlive.setText(sampler.getConnKeepAlive());
		connAttmptMax.setText(sampler.getConnAttamptMax());
		reconnAttmptMax.setText(sampler.getConnReconnAttamptMax());
		
		connCleanSession.setText(sampler.getConnCleanSession().toString());
	}
	
	
	public void setupSamplerProperties(AbstractMQTTSampler sampler) {
		sampler.setServer(serverAddr.getText());
		sampler.setPort(serverPort.getText());
		sampler.setMqttVersion(mqttVersion.getText());
		sampler.setConnTimeout(timeout.getText());

		sampler.setMqttClientName(clientNames.getText());
		sampler.setProtocol(protocols.getText());
		sampler.setWsPath(wsPath.getText());
		sampler.setDualSSLAuth(dualAuth.isSelected());
		sampler.setKeyStoreFilePath(tksFilePath.getText());
		sampler.setKeyStorePassword(tksPassword.getText());
		sampler.setClientCertFilePath(ccFilePath.getText());
		sampler.setClientCertPassword(ccPassword.getText());

		sampler.setUserNameAuth(userNameAuth.getText());
		sampler.setPasswordAuth(passwordAuth.getText());

		sampler.setConnPrefix(connNamePrefix.getText());
		sampler.setClientIdSuffix(connNameSuffix.isSelected());
		
		sampler.setConnKeepAlive(connKeepAlive.getText());
		sampler.setConnAttamptMax(connAttmptMax.getText());
		sampler.setConnReconnAttamptMax(reconnAttmptMax.getText());
		
		sampler.setConnCleanSession(Boolean.parseBoolean(connCleanSession.getText()));
	}
	
	public static int parseInt(String value) {
		if(value == null || "".equals(value.trim())) {
			return 0;
		}
		return Integer.parseInt(value);
	}
	
	public void clearUI() {
		serverAddr.setText(DEFAULT_SERVER);
		serverPort.setText(DEFAULT_PORT);
		mqttVersion.setSelectedIndex(0);
		timeout.setText(DEFAULT_CONN_TIME_OUT);

		clientNames.setSelectedIndex(clientNamesList.indexOf(DEFAULT_MQTT_CLIENT_NAME));
		protocols.setValues(MQTT.getSupportedProtocols(DEFAULT_MQTT_CLIENT_NAME).toArray(new String[] {}));
		protocols.setSelectedIndex(0);

		dualAuth.setSelected(false);
		wsPath.setText("");
		tksFilePath.setText("");
		tksPassword.setText("");
		ccFilePath.setText("");
		ccPassword.setText("");
		
		userNameAuth.setText("");
		passwordAuth.setText("");

		connNamePrefix.setText(DEFAULT_CONN_PREFIX_FOR_CONN);
		connNameSuffix.setSelected(true);

		connAttmptMax.setText(DEFAULT_CONN_ATTAMPT_MAX);
		connKeepAlive.setText(DEFAULT_CONN_KEEP_ALIVE);
		reconnAttmptMax.setText(DEFAULT_CONN_RECONN_ATTAMPT_MAX);
	}
}
