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
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import net.xmeter.Constants;
import net.xmeter.samplers.ConnectionSampler;

public class ConnectionSamplerUI extends AbstractSamplerGui implements Constants, ChangeListener, ActionListener {
	private static final Logger logger = LoggingManager.getLoggerForClass();

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1666890646673145131L;

	public ConnectionSamplerUI() {
		this.init();
	}

	private void init() {
		logger.info("Initializing the UI.");
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		JPanel mainPanel = new VerticalPanel();
		add(mainPanel, BorderLayout.CENTER);

		mainPanel.add(createConnPanel());
		mainPanel.add(createProtocolPanel());
		mainPanel.add(createAuthentication());
		mainPanel.add(createConnOptions());
		
		this.dualAuth.setVisible(false);
	}

	private JPanel createConnPanel() {
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
	
	private JPanel createConnOptions() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options"));
		
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
	
	private JPanel createAuthentication() {
		JPanel optsPanelCon = new VerticalPanel();
		optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "User authentication"));
		
		JPanel optsPanel = new HorizontalPanel();
		optsPanel.add(userNameAuth);
		optsPanel.add(passwordAuth);
		optsPanelCon.add(optsPanel);
		
		return optsPanelCon;
	}

	private JPanel createProtocolPanel() {
		JPanel protocolPanel = new VerticalPanel();
		protocolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Protocols"));
		
		JPanel pPanel = new HorizontalPanel();
		//pPanel.setLayout(new GridLayout(1, 2));

		protocols = new JLabeledChoice("Protocols:", new String[] { "TCP", "SSL" }, true, false);
		protocols.addChangeListener(this);
		pPanel.add(protocols, BorderLayout.WEST);

		dualAuth.setSelected(false);
		dualAuth.setFont(null);
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
	public void configure(TestElement element) {
		super.configure(element);
		ConnectionSampler sampler = (ConnectionSampler)element;
		this.certificationFilePath1.setText(sampler.getCertFile1());
		this.certificationFilePath2.setText(sampler.getCertFile2());
		this.connAttmptMax.setText(String.valueOf(sampler.getConnAttamptMax()));
		this.connKeepAlive.setText(String.valueOf(sampler.getConnKeepAlive()));
		this.connKeeptime.setText(String.valueOf(sampler.getConnKeepTime()));
		this.connNamePrefix.setText(sampler.getConnPrefix());
		if(sampler.isDualSSLAuth()) {
			this.dualAuth.setVisible(true);
			this.dualAuth.setSelected(sampler.isDualSSLAuth());	
		}
		this.password.setText(sampler.getKeyFilePassword());
		if(DEFAULT_PROTOCOL.equals(sampler.getProtocol())) {
			this.protocols.setSelectedIndex(0);	
		} else {
			this.protocols.setSelectedIndex(1);
		}
		this.reconnAttmptMax.setText(String.valueOf(sampler.getConnReconnAttamptMax()));
		this.serverAddr.setText(sampler.getServer());
		this.serverPort.setText(String.valueOf(sampler.getPort()));
		this.timeout.setText(String.valueOf(sampler.getConnTimeout()));
		this.userName.setText(String.valueOf(sampler.getKeyFileUsrName()));
		this.userNameAuth.setText(sampler.getUserNameAuth());
		this.passwordAuth.setText(sampler.getPasswordAuth());
	}

	@Override
	public TestElement createTestElement() {
		ConnectionSampler sampler = new ConnectionSampler();
		this.setupSamplerProperties(sampler);
		return sampler;
	}

	
	private void setupSamplerProperties(ConnectionSampler sampler) {
		this.configureTestElement(sampler);
		sampler.setCertFile1(this.certificationFilePath1.getText());
		sampler.setCertFile2(this.certificationFilePath2.getText());
		sampler.setConnKeepAlive(Integer.parseInt(this.connKeepAlive.getText()));
		sampler.setConnAttamptMax(Integer.parseInt(this.connAttmptMax.getText()));
		sampler.setConnKeepTime(Integer.parseInt(this.connKeeptime.getText()));
		sampler.setConnPrefix(this.connNamePrefix.getText());
		sampler.setConnReconnAttamptMax(Integer.parseInt(this.reconnAttmptMax.getText()));
		sampler.setConnTimeout(Integer.parseInt(this.timeout.getText()));
		sampler.setDualSSLAuth(this.dualAuth.isSelected());
		sampler.setKeyFilePassword(this.password.getText());
		sampler.setKeyFileUsrName(this.userName.getText());
		sampler.setPort(Integer.parseInt(this.serverPort.getText()));
		sampler.setProtocol(this.protocols.getText());
		sampler.setServer(this.serverAddr.getText());
		sampler.setUserNameAuth(this.userNameAuth.getText());
		sampler.setPasswordAuth(this.passwordAuth.getText());
	}
	
	@Override
	public String getLabelResource() {
		return "";
	}

	@Override
	public String getStaticLabel() {
		return "MQTT Connection Sampler";
	}

	@Override
	public void modifyTestElement(TestElement arg0) {
		ConnectionSampler sampler = (ConnectionSampler)arg0;
		this.setupSamplerProperties(sampler);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == this.dualAuth) {
			if(this.dualAuth.isSelected()) {
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
		} else if(e.getSource() == this.protocols) {
			if("TCP".equals(this.protocols.getText())) {
				this.dualAuth.setVisible(false);
				this.dualAuth.setSelected(false);
			} else if("SSL".equals(this.protocols.getText())) {
				this.dualAuth.setVisible(true);
				this.dualAuth.setEnabled(true);
			}
		}
	}
	
	@Override
	public void clearGui() {
		super.clearGui();
		this.certificationFilePath1.setText("");
		this.certificationFilePath2.setText("");
		this.dualAuth.setSelected(false);
		this.connAttmptMax.setText(String.valueOf(DEFAULT_CONN_ATTAMPT_MAX));
		this.connKeepAlive.setText(String.valueOf(DEFAULT_CONN_KEEP_ALIVE));
		this.connKeeptime.setText(String.valueOf(DEFAULT_CONN_KEEP_TIME));
		this.connNamePrefix.setText(DEFAULT_CONN_PREFIX);
		this.protocols.setSelectedIndex(0);
		this.password.setText("");
		this.reconnAttmptMax.setText(String.valueOf(DEFAULT_CONN_RECONN_ATTAMPT_MAX));
		this.serverAddr.setText(DEFAULT_SERVER);
		this.serverPort.setText(String.valueOf(DEFAULT_PORT));
		this.timeout.setText(String.valueOf(DEFAULT_CONN_TIME_OUT));
		this.userNameAuth.setText("");
		this.passwordAuth.setText("");
	}

}
