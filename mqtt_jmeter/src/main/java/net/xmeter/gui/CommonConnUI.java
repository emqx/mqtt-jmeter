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

import javax.swing.*;
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
	private final JLabel mqttVersionLabel = new JLabel("MQTT version:");
	private JLabeledChoice mqttVersion = new JLabeledChoice("MQTT version:", new String[] { MQTT_VERSION_3_1, MQTT_VERSION_3_1_1, MQTT_VERSION_5_0 }, false, false);
	private final JLabeledTextField timeout = new JLabeledTextField("Timeout(s):", 5);
	
	private final JLabeledTextField userNameAuth = new JLabeledTextField("User name:");
	private final JLabeledTextField passwordAuth = new JLabeledTextField("Password:");
	private final JLabeledTextField authMethod = new JLabeledTextField("Auth Method:");
	private final JLabeledTextField authData = new JLabeledTextField("Auth Data:");

	private JLabeledChoice protocols;
//	private JLabeledChoice clientNames;

	private JCheckBox dualAuth = new JCheckBox("Dual SSL authentication");
	private JLabeledTextField wsPath = new JLabeledTextField("WS Path: ", 10);
	private final JLabeledTextField wsHeader = new JLabeledTextField("WS Header: ", 20);

//	private final JLabeledTextField tksFilePath = new JLabeledTextField("Trust Key Store(*.jks):       ", 25);
	private final JLabeledTextField ccFilePath = new JLabeledTextField("Client Certification(*.p12):", 25);
	
//	private final JLabeledTextField tksPassword = new JLabeledTextField("Secret:", 10);
	private final JLabeledTextField ccPassword = new JLabeledTextField("Secret:", 10);

	private final JLabeledTextField caCertFilePath = new JLabeledTextField("CA   Cert   :", 25);
	private final JLabeledTextField clientCertFilePath = new JLabeledTextField("Client Cert:", 25);
	private final JLabeledTextField clientKeyFilePath = new JLabeledTextField("Client Key :", 25);

//	private JButton tksBrowseButton;
	private JButton ccBrowseButton;

	private JButton caBrowseButton;
	private JButton clientCertBrowseButton;
	private JButton clientKeyBrowseButton;

//	private static final String TKS_BROWSE = "tks_browse";
	private static final String CC_BROWSE = "cc_browse";
	
	public final JLabeledTextField connNamePrefix = new JLabeledTextField("ClientId:", 8);
	private JCheckBox connNameSuffix = new JCheckBox("Add random suffix for ClientId");
	
	private final JLabeledTextField connKeepAlive = new JLabeledTextField("Keep alive(s):", 3);
	
	private final JLabeledTextField connAttmptMax = new JLabeledTextField("Connect attempt max:", 3);
	private final JLabeledTextField reconnAttmptMax = new JLabeledTextField("Reconnect attempt max:", 3);

	private final JLabeledTextField connCleanSession = new JLabeledTextField("Clean session:", 3);

	public final JLabeledTextField connUserProperty = new JLabeledTextField("User Property:", 10);

	private final JLabeledTextField connCleanStart = new JLabeledTextField("Clean start:", 5);

	private final JLabeledTextField connSessionExpiryInterval = new JLabeledTextField("Session Expiry Interval(s):", 5);
//	private final List<String> clientNamesList = MQTT.getAvailableNames();

	public JPanel createConnPanel() {
		JPanel con = new HorizontalPanel();
		
		JPanel connPanel = new HorizontalPanel();
		connPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "MQTT connection"));
		connPanel.add(serverAddr);
		connPanel.add(serverPort);
		connPanel.add(mqttVersionLabel);
		connPanel.add(mqttVersion);
		
		JPanel timeoutPannel = new HorizontalPanel();
		timeoutPannel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Timeout"));
		timeoutPannel.add(timeout);

		mqttVersion.addChangeListener(this);

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
		optsPanel1.add(connAttmptMax);
		optsPanel1.add(reconnAttmptMax);
		optsPanel1.add(connCleanSession);
		optsPanelCon.add(optsPanel1);

		JPanel optsPanel2 = new HorizontalPanel();
		optsPanel2.add(connUserProperty);
		optsPanel2.add(connCleanStart);
		optsPanel2.add(connSessionExpiryInterval);

		connUserProperty.setVisible(false);
		connCleanStart.setVisible(false);
		connSessionExpiryInterval.setVisible(false);

		optsPanelCon.add(optsPanel2);

		return optsPanelCon;
	}
	
	public JPanel createAuthentication() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "User authentication"));
		
		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(userNameAuth);
		optsPanel.add(passwordAuth);
		optsPanelCon.add(optsPanel);

//		JPanel optsPanel1 = new HorizontalPanel();
//		optsPanel1.add(authMethod);
//		optsPanel1.add(authData);
//		authMethod.setVisible(false);
//		authData.setVisible(false);
//		optsPanelCon.add(optsPanel1);
		
		return optsPanelCon;
	}

	public JPanel createProtocolPanel() {
		JPanel protocolPanel = new VerticalPanel();
		protocolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Protocols"));
		
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BorderLayout());
		//pPanel.setLayout(new GridLayout(1, 2));

		JPanel pCenter = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		clientNames = new JLabeledChoice("Clients:", clientNamesList.toArray(new String[] {}), true, false);
//		clientNames.addChangeListener(this);
//		pCenter.add(clientNames);

		protocols = new JLabeledChoice("Protocols:", false);
		//JComboBox<String> component = (JComboBox) protocols.getComponentList().get(1);
		//component.setSize(new Dimension(40, component.getHeight()));
		protocols.addChangeListener(this);
		pCenter.add(protocols);

		wsPath.setFont(null);
		wsPath.setVisible(false);
		pCenter.add(wsPath);

		wsHeader.setVisible(false);
		pCenter.add(wsHeader);

		pPanel.add(pCenter, BorderLayout.CENTER);

		dualAuth.setSelected(false);
		dualAuth.setFont(null);
		dualAuth.setVisible(false);
		dualAuth.addChangeListener(this);
		pPanel.add(dualAuth, BorderLayout.SOUTH);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.SOUTHWEST;
		
//		c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
//		tksFilePath.setVisible(false);
//		panel.add(tksFilePath, c);
//
//		c.gridx = 2; c.gridy = 0; c.gridwidth = 1;
//		tksBrowseButton = new JButton(JMeterUtils.getResString("browse"));
//		tksBrowseButton.setActionCommand(TKS_BROWSE);
//		tksBrowseButton.addActionListener(this);
//		tksBrowseButton.setVisible(false);
//		panel.add(tksBrowseButton, c);
//		
//		c.gridx = 3; c.gridy = 0; c.gridwidth = 2;
//		tksPassword.setVisible(false);
//		panel.add(tksPassword, c);

		//c.weightx = 0.0;
//		c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
//		ccFilePath.setVisible(false);
//		panel.add(ccFilePath, c);
//
//		c.gridx = 2; c.gridy = 1; c.gridwidth = 1;
//		ccBrowseButton = new JButton(JMeterUtils.getResString("browse"));
//		ccBrowseButton.setActionCommand(CC_BROWSE);
//		ccBrowseButton.addActionListener(this);
//		ccBrowseButton.setVisible(false);
//		panel.add(ccBrowseButton, c);
		
//		c.gridx = 3; c.gridy = 1; c.gridwidth = 2;
//		ccPassword.setVisible(false);
//		panel.add(ccPassword, c);

		c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
		caCertFilePath.setVisible(false);
		panel.add(caCertFilePath, c);

		c.gridx = 2; c.gridy = 2; c.gridwidth = 1;
		caBrowseButton = new JButton(JMeterUtils.getResString("browse"));
		caBrowseButton.setActionCommand("ca_browse");
		caBrowseButton.addActionListener(this);
		caBrowseButton.setVisible(false);
		panel.add(caBrowseButton, c);

		c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
		clientCertFilePath.setVisible(false);
		panel.add(clientCertFilePath, c);

		c.gridx = 2; c.gridy = 3; c.gridwidth = 1;
		clientCertBrowseButton = new JButton(JMeterUtils.getResString("browse"));
		clientCertBrowseButton.setActionCommand("client_cert_browse");
		clientCertBrowseButton.addActionListener(this);
		clientCertBrowseButton.setVisible(false);
		panel.add(clientCertBrowseButton, c);

		c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
		clientKeyFilePath.setVisible(false);
		panel.add(clientKeyFilePath, c);

		c.gridx = 2; c.gridy = 4; c.gridwidth = 1;
		clientKeyBrowseButton = new JButton(JMeterUtils.getResString("browse"));
		clientKeyBrowseButton.setActionCommand("client_key_browse");
		clientKeyBrowseButton.addActionListener(this);
		clientKeyBrowseButton.setVisible(false);
		panel.add(clientKeyBrowseButton, c);
		
		protocolPanel.add(pPanel);
		protocolPanel.add(panel);
		
		return protocolPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
//		if(TKS_BROWSE.equals(action)) {
//			String path = browseAndGetFilePath();
//			tksFilePath.setText(path);
//		}else 
		if(CC_BROWSE.equals(action)) {
			String path = browseAndGetFilePath();
			ccFilePath.setText(path);
		} else if ("ca_browse".equals(action)) {
			String path = browseAndGetFilePath();
			caCertFilePath.setText(path);
		} else if ("client_cert_browse".equals(action)) {
			String path = browseAndGetFilePath();
			clientCertFilePath.setText(path);
		} else if ("client_key_browse".equals(action)) {
			String path = browseAndGetFilePath();
			clientKeyFilePath.setText(path);
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
//				tksFilePath.setVisible(true);
//				tksBrowseButton.setVisible(true);
//				tksPassword.setVisible(true);
//				ccFilePath.setVisible(true);
//				ccBrowseButton.setVisible(true);
//				ccPassword.setVisible(true);

				caCertFilePath.setVisible(true);
				caBrowseButton.setVisible(true);
				clientCertFilePath.setVisible(true);
				clientCertBrowseButton.setVisible(true);
				clientKeyFilePath.setVisible(true);
				clientKeyBrowseButton.setVisible(true);
			} else {
//				tksFilePath.setVisible(false);
//				tksBrowseButton.setVisible(false);
//				tksPassword.setVisible(false);
//				ccFilePath.setVisible(false);
//				ccBrowseButton.setVisible(false);
//				ccPassword.setVisible(false);

				caCertFilePath.setVisible(false);
				caBrowseButton.setVisible(false);
				clientCertFilePath.setVisible(false);
				clientCertBrowseButton.setVisible(false);
				clientKeyFilePath.setVisible(false);
				clientKeyBrowseButton.setVisible(false);
			}
		} else if(e.getSource() == protocols) {
			boolean isSecure = Util.isSecureProtocol(protocols.getText());
			dualAuth.setVisible(isSecure);
			dualAuth.setEnabled(isSecure);
			boolean wsProtocol = Util.isWebSocketProtocol(protocols.getText());
			wsPath.setVisible(wsProtocol);
			wsPath.setEnabled(wsProtocol);
			wsHeader.setVisible(wsProtocol);
			wsHeader.setEnabled(wsProtocol);
//		} else if (e.getSource() == clientNames) {
//			int index = clientNames.getSelectedIndex();
//			if (index > -1) {
//				String clientName = clientNames.getItems()[index];
//				List<String> supportedProtocols = MQTT.getSupportedProtocols(clientName);
//				protocols.setValues(supportedProtocols.toArray(new String[supportedProtocols.size()]));
//			} else {
//				protocols.setValues(new String[0]);
//			}
		} else if(e.getSource() == mqttVersion) {
			boolean isMqtt5 = mqttVersion.getText().equals(MQTT_VERSION_5_0);
			connUserProperty.setVisible(isMqtt5);
			connCleanStart.setVisible(isMqtt5);
			connSessionExpiryInterval.setVisible(isMqtt5);
			connCleanSession.setVisible(!isMqtt5);
//			authMethod.setVisible(isMqtt5);
//			authData.setVisible(isMqtt5);
		}
	}

	public void configure(AbstractMQTTSampler sampler) {
		serverAddr.setText(sampler.getServer());
		serverPort.setText(sampler.getPort());
		if(sampler.getMqttVersion().equals(MQTT_VERSION_3_1)) {
			mqttVersion.setSelectedIndex(0);
		} else if(sampler.getMqttVersion().equals(MQTT_VERSION_3_1_1)) {
			mqttVersion.setSelectedIndex(1);
		} else if(sampler.getMqttVersion().equals(MQTT_VERSION_5_0)) {
			mqttVersion.setSelectedIndex(2);
		}

		timeout.setText(sampler.getConnTimeout());

//		if (sampler.getProtocol().trim().indexOf(JMETER_VARIABLE_PREFIX) == -1) {
//			int index = clientNamesList.indexOf(sampler.getMqttClientName());
//			clientNames.setSelectedIndex(index);
//		} else{
//			clientNames.setText(sampler.getMqttClientName());
//		}

		if(sampler.getProtocol().trim().contains(JMETER_VARIABLE_PREFIX)) {
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

		wsHeader.setText(sampler.getWsHeader());
		wsHeader.setVisible(wsProtocol);
		wsHeader.setEnabled(wsProtocol);

		if(sampler.isDualSSLAuth()) {
			dualAuth.setVisible(true);
			dualAuth.setSelected(sampler.isDualSSLAuth());	
		}
//		tksFilePath.setText(sampler.getKeyStoreFilePath());
//		tksPassword.setText(sampler.getKeyStorePassword());
		ccFilePath.setText(sampler.getClientCertFilePath());
		ccPassword.setText(sampler.getClientCertPassword());

		userNameAuth.setText(sampler.getUserNameAuth());
		passwordAuth.setText(sampler.getPasswordAuth());
//		authMethod.setText(sampler.getAuthMethod());
//		authData.setText(sampler.getAuthData());
		
		connNamePrefix.setText(sampler.getConnPrefix());
		if (sampler.isClientIdSuffix()) {
			connNameSuffix.setSelected(true);
		} else {
			connNameSuffix.setSelected(false);
		}
		
		connKeepAlive.setText(sampler.getConnKeepAlive());
		connAttmptMax.setText(sampler.getConnAttemptMax());
		reconnAttmptMax.setText(sampler.getConnReconnAttemptMax());
		
		connCleanSession.setText(sampler.getConnCleanSession());

		connUserProperty.setText(sampler.getConnUserProperty());
		connCleanStart.setText(sampler.getConnCleanStart());
		connSessionExpiryInterval.setText(sampler.getConnSessionExpiryInterval());

		caCertFilePath.setText(sampler.getCAFilePath());
		clientCertFilePath.setText(sampler.getClientCert2FilePath());
		clientKeyFilePath.setText(sampler.getClientPrivateKeyFilePath());
	}
	
	
	public void setupSamplerProperties(AbstractMQTTSampler sampler) {
		sampler.setServer(serverAddr.getText());
		sampler.setPort(serverPort.getText());
		sampler.setMqttVersion(mqttVersion.getText());
		sampler.setConnTimeout(timeout.getText());

//		sampler.setMqttClientName(clientNames.getText());
		sampler.setProtocol(protocols.getText());
		sampler.setWsPath(wsPath.getText());
		sampler.setWsHeader(wsHeader.getText());
		sampler.setDualSSLAuth(dualAuth.isSelected());
//		sampler.setKeyStoreFilePath(tksFilePath.getText());
//		sampler.setKeyStorePassword(tksPassword.getText());
		sampler.setClientCertFilePath(ccFilePath.getText());
		sampler.setClientCertPassword(ccPassword.getText());

		sampler.setUserNameAuth(userNameAuth.getText());
		sampler.setPasswordAuth(passwordAuth.getText());
//		sampler.setAuthMethod(authMethod.getText());
//		sampler.setAuthData(authData.getText());

		sampler.setConnPrefix(connNamePrefix.getText());
		sampler.setClientIdSuffix(connNameSuffix.isSelected());
		
		sampler.setConnKeepAlive(connKeepAlive.getText());
		sampler.setConnAttemptMax(connAttmptMax.getText());
		sampler.setConnReconnAttemptMax(reconnAttmptMax.getText());
		
		sampler.setConnCleanSession(connCleanSession.getText());

		sampler.setConnUserProperty(connUserProperty.getText());
		sampler.setConnCleanStart(connCleanStart.getText());
		sampler.setConnSessionExpiryInterval(connSessionExpiryInterval.getText());

		sampler.setCAFilePath(caCertFilePath.getText());
		sampler.setClientCert2FilePath(clientCertFilePath.getText());
		sampler.setClientPrivateKeyFilePath(clientKeyFilePath.getText());
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

//		clientNames.setSelectedIndex(clientNamesList.indexOf(DEFAULT_MQTT_CLIENT_NAME));
		protocols.setValues(MQTT.getSupportedProtocols(DEFAULT_MQTT_CLIENT_NAME).toArray(new String[] {}));
		protocols.setSelectedIndex(0);

		dualAuth.setSelected(false);
		wsPath.setText("");
		wsHeader.setText("{}");
//		tksFilePath.setText("");
//		tksPassword.setText("");
		ccFilePath.setText("");
		ccPassword.setText("");
		
		userNameAuth.setText("");
		passwordAuth.setText("");
//		authMethod.setText("");
//		authData.setText("");

		connNamePrefix.setText(DEFAULT_CONN_PREFIX_FOR_CONN);
		connNameSuffix.setSelected(true);

		connAttmptMax.setText(DEFAULT_CONN_ATTEMPT_MAX);
		connKeepAlive.setText(DEFAULT_CONN_KEEP_ALIVE);
		reconnAttmptMax.setText(DEFAULT_CONN_RECONN_ATTEMPT_MAX);
		connCleanSession.setText("true");

		connUserProperty.setText("{}");
		connCleanStart.setText("true");
		connSessionExpiryInterval.setText("0");

		caCertFilePath.setText("");
		clientCertFilePath.setText("");
		clientKeyFilePath.setText("");
	}
}
