package net.xmeter.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
import net.xmeter.samplers.AbstractMQTTSampler;
import net.xmeter.samplers.ConnectionSampler;

public class CommonConnUI implements ChangeListener, ActionListener, Constants{
	private final JLabeledTextField serverAddr = new JLabeledTextField("Server name or IP:");
	private final JLabeledTextField serverPort = new JLabeledTextField("Port number:", 5);
	private final JLabeledTextField timeout = new JLabeledTextField("Timeout(s):", 5);
	
	private final JLabeledTextField userNameAuth = new JLabeledTextField("User name:");
	private final JLabeledTextField passwordAuth = new JLabeledTextField("Password:");

	private JLabeledChoice protocols;

	private JCheckBox dualAuth = new JCheckBox("Dual SSL authentication");

	private final JLabeledTextField certificationFilePath1 = new JLabeledTextField("Certification file path(*.jks):", 20);
	private final JLabeledTextField certificationFilePath2 = new JLabeledTextField("Certification file path(*.p12):", 20);
	
	private final JLabeledTextField userName = new JLabeledTextField("Key file username:", 6);
	private final JLabeledTextField password = new JLabeledTextField("Key file Password:", 6);

	private JButton browse1;
	private JButton browse2;
	private static final String BROWSE1 = "browse1";
	private static final String BROWSE2 = "browse2";
	
	private final JLabeledTextField connNamePrefix = new JLabeledTextField("ClientId prefix:", 8);
	private final JLabeledTextField connKeepAlive = new JLabeledTextField("Keep alive(s):", 4);
	
	private final JLabeledTextField connKeeptime = new JLabeledTextField("Connection keep time(s):", 4);
	
	private final JLabeledTextField connAttmptMax = new JLabeledTextField("Connect attampt max:", 0);
	private final JLabeledTextField reconnAttmptMax = new JLabeledTextField("Reconnect attampt max:", 0);
	
	public JPanel createConnPanel() {
		JPanel con = new HorizontalPanel();
		
		JPanel connPanel = new HorizontalPanel();
		connPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "MQTT connection"));
		connPanel.add(serverAddr);
		connPanel.add(serverPort);
		
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
		
		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(connNamePrefix);
		optsPanel.add(connKeepAlive);
		optsPanel.add(connKeeptime);
		optsPanelCon.add(optsPanel);
		
		JPanel optsPanel2 = new HorizontalPanel();
		optsPanel2.add(connAttmptMax);
		optsPanel2.add(reconnAttmptMax);
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
		
		return optsPanelCon;
	}

	public JPanel createProtocolPanel() {
		JPanel protocolPanel = new VerticalPanel();
		protocolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Protocols"));
		
		JPanel pPanel = new HorizontalPanel();
		//pPanel.setLayout(new GridLayout(1, 2));

		protocols = new JLabeledChoice("Protocols:", new String[] { "TCP", "SSL" }, true, false);
		protocols.addChangeListener(this);
		pPanel.add(protocols, BorderLayout.WEST);

		dualAuth.setSelected(false);
		dualAuth.setFont(null);
		dualAuth.setVisible(false);
		dualAuth.addChangeListener(this);
		pPanel.add(dualAuth, BorderLayout.CENTER);

		JPanel panel = new HorizontalPanel();
		panel.add(certificationFilePath1);
		certificationFilePath1.setVisible(false);

		browse1 = new JButton(JMeterUtils.getResString("browse"));
		browse1.setActionCommand(BROWSE1);
		browse1.addActionListener(this);
		browse1.setVisible(false);
		panel.add(browse1);
		
		JPanel panel2 = new HorizontalPanel();
		certificationFilePath2.setVisible(false);
		panel2.add(certificationFilePath2);

		browse2 = new JButton(JMeterUtils.getResString("browse"));
		browse2.setActionCommand(BROWSE2);
		browse2.addActionListener(this);
		browse2.setVisible(false);
		panel2.add(browse2);
		
		JPanel panel3 = new HorizontalPanel();
		panel3.add(userName);
		userName.setVisible(false);
		panel3.add(password);
		password.setVisible(false);
		
		protocolPanel.add(pPanel);
		protocolPanel.add(panel);
		protocolPanel.add(panel2);
		protocolPanel.add(panel3);
		
		return protocolPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if(BROWSE1.equals(action)) {
			String path = browseAndGetFilePath();
			certificationFilePath1.setText(path);
		}else if(BROWSE2.equals(action)) {
			String path = browseAndGetFilePath();
			certificationFilePath2.setText(path);
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
				certificationFilePath1.setVisible(true);
				certificationFilePath2.setVisible(true);
				userName.setVisible(true);
				password.setVisible(true);
				browse1.setVisible(true);
				browse2.setVisible(true);
			} else {
				certificationFilePath1.setVisible(false);
				certificationFilePath2.setVisible(false);
				userName.setVisible(false);
				password.setVisible(false);
				browse1.setVisible(false);
				browse2.setVisible(false);
			}
		} else if(e.getSource() == protocols) {
			if("TCP".equals(protocols.getText())) {
				dualAuth.setVisible(false);
				dualAuth.setSelected(false);
			} else if("SSL".equals(protocols.getText())) {
				dualAuth.setVisible(true);
				dualAuth.setEnabled(true);
			}
		}
	}
	
	public void configure(AbstractMQTTSampler sampler) {
		certificationFilePath1.setText(sampler.getCertFile1());
		certificationFilePath2.setText(sampler.getCertFile2());
		connAttmptMax.setText(String.valueOf(sampler.getConnAttamptMax()));
		connKeepAlive.setText(String.valueOf(sampler.getConnKeepAlive()));
		connKeeptime.setText(String.valueOf(sampler.getConnKeepTime()));
		connNamePrefix.setText(sampler.getConnPrefix());
		if(sampler.isDualSSLAuth()) {
			dualAuth.setVisible(true);
			dualAuth.setSelected(sampler.isDualSSLAuth());	
		}
		password.setText(sampler.getKeyFilePassword());
		if(DEFAULT_PROTOCOL.equals(sampler.getProtocol())) {
			protocols.setSelectedIndex(0);	
		} else {
			protocols.setSelectedIndex(1);
		}
		reconnAttmptMax.setText(String.valueOf(sampler.getConnReconnAttamptMax()));
		serverAddr.setText(sampler.getServer());
		serverPort.setText(String.valueOf(sampler.getPort()));
		timeout.setText(String.valueOf(sampler.getConnTimeout()));
		userName.setText(String.valueOf(sampler.getKeyFileUsrName()));
		userNameAuth.setText(sampler.getUserNameAuth());
		passwordAuth.setText(sampler.getPasswordAuth());
	}
	
	
	public void setupSamplerProperties(AbstractMQTTSampler sampler) {
		sampler.setCertFile1(certificationFilePath1.getText());
		sampler.setCertFile2(certificationFilePath2.getText());
		sampler.setConnKeepAlive(parseInt(connKeepAlive.getText()));
		sampler.setConnAttamptMax(parseInt(connAttmptMax.getText()));
		sampler.setConnKeepTime(parseInt(connKeeptime.getText()));
		sampler.setConnPrefix(connNamePrefix.getText());
		sampler.setConnReconnAttamptMax(parseInt(reconnAttmptMax.getText()));
		sampler.setConnTimeout(parseInt(timeout.getText()));
		sampler.setDualSSLAuth(dualAuth.isSelected());
		sampler.setKeyFilePassword(password.getText());
		sampler.setKeyFileUsrName(userName.getText());
		sampler.setPort(parseInt(serverPort.getText()));
		sampler.setProtocol(protocols.getText());
		sampler.setServer(serverAddr.getText());
		sampler.setUserNameAuth(userNameAuth.getText());
		sampler.setPasswordAuth(passwordAuth.getText());
	}
	
	public static int parseInt(String value) {
		if(value == null || "".equals(value.trim())) {
			return 0;
		}
		return Integer.parseInt(value);
	}
	
	public void clearUI() {
		certificationFilePath1.setText("");
		certificationFilePath2.setText("");
		dualAuth.setSelected(false);
		connAttmptMax.setText(String.valueOf(DEFAULT_CONN_ATTAMPT_MAX));
		connKeepAlive.setText(String.valueOf(DEFAULT_CONN_KEEP_ALIVE));
		connKeeptime.setText(String.valueOf(DEFAULT_CONN_KEEP_TIME));
		connNamePrefix.setText(DEFAULT_CONN_PREFIX_FOR_CONN);
		protocols.setSelectedIndex(0);
		password.setText("");
		reconnAttmptMax.setText(String.valueOf(DEFAULT_CONN_RECONN_ATTAMPT_MAX));
		serverAddr.setText(DEFAULT_SERVER);
		serverPort.setText(String.valueOf(DEFAULT_PORT));
		timeout.setText(String.valueOf(DEFAULT_CONN_TIME_OUT));
		userNameAuth.setText("");
		passwordAuth.setText("");
	}
}
