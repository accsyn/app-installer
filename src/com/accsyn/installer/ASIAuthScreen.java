package com.accsyn.installer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.json.simple.JSONObject;

import com.accsyn.installer.common.ASICommon;
import com.install4j.api.context.InstallerContext;
import com.install4j.api.context.UserCanceledException;
import com.install4j.api.screens.AbstractInstallerScreen;
import com.install4j.api.screens.Console;

/**
 * Simple sample screen to show how to integrate custom screens into your
 * installer.
 *
 * The property buttonTitle can be changed in the install4j IDE.
 *
 * This sample screen has no BeanInfo. The SampleAction, ManyFeaturesAction and
 * the SampleFormComponents used in this project show how to use a BeanInfo.
 */
public class ASIAuthScreen extends AbstractInstallerScreen implements ActionListener, ItemListener {

	// GUI
	static final String LABEL_01 = "Please launch the accsyn server installation from https://accsyn.io/servers/new";
	static final String LABEL_02 = "Enter the code/ID: ";
	static final String LABEL_03 = "Please enter choice [yes or no]: ";
	static final String LABEL_04 = "Configuration data exist, ERASE configuration and do a clean installation?\n\n(Choosing No will keep and reuse current accsyn configuration)";

	static final String MESSAGE_01 = "Please enter the server ID as presented in the web application - 24 digits hexadecimal number!";
	static final String MESSAGE_02 = "A server error occured during authentication, please try again later or contact accsyn support: support@accsyn.com";
	static final String MESSAGE_03 = "A server error occurred when trying to connect to accsyn, please try again later or contact accsyn support: support@accsyn.com";
	static final String MESSAGE_04 = "Please enter 'yes' or 'no'!";

	static int MODE_ID = 0;
	static int MODE_SKIP = 1;

	private int mode = MODE_SKIP;

	private JTextField tf_id;

	// GUI setup ////////////////////////////////////////////////////////////////

	private JPanel createIDPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);

		c.weightx = 100;
		c.gridwidth = GridBagConstraints.REMAINDER;

		panel.add(new JLabel("<html>" + LABEL_01 + "</html>"), c);
		if (ASICommon.isDEV())
			panel.add(new JLabel("<html><strong>!!! DEVELOPMENT MODE !!!</strong></html>"), c);
		if (ASICommon.getRootPathEnv() != null)
			panel.add(new JLabel("<html>!!! Root: "+ASICommon.getRootPathEnv()+"!!!</html>"), c);
		panel.add(new JLabel(""), c);
		/*
		 * panel.add(new JLabel("<html>" + "Continue your domain setup or " +
		 * "logon as admin and go to your AccSyn and go ADMIN>SERVERS>Install Server." +
		 * "</html>"), c);
		 */
		panel.add(new JLabel(""), c);

		c.weighty = 50;
		panel.add(new JLabel(), c);

		c.gridwidth = 1;
		c.weighty = 1;
		c.weightx = 30;
		panel.add(new JLabel(), c);
		c.gridwidth = 1;
		c.weightx = 40;
		panel.add(new JLabel(LABEL_02), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 30;
		panel.add(new JLabel(), c);

		c.gridwidth = 1;
		c.weightx = 30;
		panel.add(new JLabel(), c);
		c.gridwidth = 1;
		c.weightx = 40;
		panel.add(tf_id = new JTextField(24), c);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				tf_id.requestFocus();
			}
		});
		tf_id.setBackground(new Color(240, 240, 255));
		tf_id.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
					}
				});
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
					}
				});
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
					}
				});
			}
		});
		tf_id.setFont(new Font("Courier", tf_id.getFont().getStyle(), tf_id.getFont().getSize() + 2));
		tf_id.setForeground(Color.BLACK);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 30;
		panel.add(new JLabel(), c);

		return panel;
	}

	@Override
	public JComponent createComponent() {
		
		if (ASICommon.isDEV())
			ASICommon.info("!!! Launched in DEVELOPMENT mode !!!");
		String root_path = ASICommon.getRootPathEnv();
		if (root_path != null) {
			ASICommon.info("!!! accsyn root path set to: " + root_path + " !!!");
		}
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);

		c.weightx = 100;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1;

		panel.add(createIDPanel(), c);

		c.weightx = 100;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weighty = 50;
		panel.add(new JLabel(), c);

		updateAuthScreen();

		// getInstallerContext()
		return panel;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		updateAuthScreen();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		updateAuthScreen();
	}

	private void updateAuthScreen() {
	}

	@Override
	public String getTitle() {
		return "Authentication";
	}

	@Override
	public String getSubTitle() {
		return "";
	}

	@Override
	public boolean isFillVertical() {
		return true;
	}

	@Override
	public boolean isFillHorizontal() {
		return true;
	}

	@Override
	public boolean isNextVisible() {
		return true;
	}

	@Override
	public void activated() {
		// Should erase old config?
		final boolean has_config = ASICommon.hasData() || ASICommon.hasLegacyData();
		final InstallerContext ic = getInstallerContext();
		if (has_config) {
			int choice = JOptionPane.showConfirmDialog(null, LABEL_04);
			if (choice == JOptionPane.YES_OPTION) {
				ic.setVariable("accsyn_factory_reset", true);
			} else if (choice == JOptionPane.NO_OPTION) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						mode = MODE_SKIP;
						ic.setVariable("accsyn_workspace", "(using existing configuration)");
						ic.setVariable("accsyn_api_user", "(using existing configuration)");
						ic.setVariable("accsyn_write_credentials", false);
						ic.goForward(1, true, true);
					}
				});
			} else
				ic.finish(-1);
		}
		super.activated();
	}

	@Override
	public boolean next() {
		ASICommon.info("next() mode:" + mode);
		InstallerContext ic = getInstallerContext();
		String client_id = tf_id.getText().trim();
		if (client_id.length() == 0 || !client_id.matches("^[0-9a-f]{24}$")) {
			JOptionPane.showMessageDialog(null, MESSAGE_01, "Authentication", JOptionPane.WARNING_MESSAGE);
			return false;
		} else {
			// Check with registry
			JSONObject data = new JSONObject();
			data.put("client", client_id);
			data.put("hostname", ASICommon.getHostname());
			try {
				JSONObject response = ASICommon.rest(ASICommon.REST_PUT, "client/check", data);
				if (response.containsKey("message")) {
					JOptionPane.showMessageDialog(null, response.get("message"), "Authentication", JOptionPane.WARNING_MESSAGE);
					return false;
				} else if (response.containsKey("exception")) {
					JOptionPane.showMessageDialog(null, MESSAGE_02, "Authentication", JOptionPane.ERROR_MESSAGE);
					return false;
				} else {
					JSONObject result = (JSONObject) response.get("result");
					// Store data so we can write it later
					ic.setVariable("accsyn_workspace", result.get("workspace_code"));
					ic.setVariable("accsyn_api_user", result.get("user_code"));
					ic.setVariable("accsyn_client_id", client_id + "");
					ASICommon.info("Successfully authenticated accsyn server. Installation can proceed.");
				}
			} catch (Exception e) {
				ASICommon.error(e);
				JOptionPane.showMessageDialog(null, MESSAGE_03, "Authentication", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return super.next();
	}

	// Console only

	@Override
	public boolean handleConsole(Console console) throws UserCanceledException {
		InstallerContext ic = getInstallerContext();

		boolean has_data = ASICommon.hasData() || ASICommon.hasLegacyData();
		if (has_data) {
			while (true) {
				console.println();
				console.println(LABEL_04);
				console.println();
				console.print(LABEL_03);
				String s = console.readLine();
				if (s == null || (!s.equalsIgnoreCase("yes") && !s.equalsIgnoreCase("no"))) {
					System.err.println(MESSAGE_04);
				} else {
					if (s.equalsIgnoreCase("yes")) {
						ic.setVariable("accsyn_factory_reset", true);
						break;
					} else {
						// Leave this step
						ic.setVariable("accsyn_workspace", "(using existing configuration)");
						ic.setVariable("accsyn_api_user", "(using existing configuration)");
						ic.setVariable("accsyn_write_credentials", false);
						return true;
					}
				}
			}
		}

		console.println();
		console.println(LABEL_01);
		console.println();
		while (true) {
			String client_id = null;
			while (client_id == null) {
				console.print(LABEL_02);
				String s = console.readLine();
				if (s == null || !s.matches("^[0-9a-f]{24}$")) {
					System.err.println(MESSAGE_01);
				} else
					client_id = s;
			}
			// Check with registry
			JSONObject data = new JSONObject();
			data.put("client", client_id);
			data.put("hostname", ASICommon.getHostname());
			try {
				JSONObject response = ASICommon.rest(ASICommon.REST_PUT, "client/check", data);
				if (response.containsKey("exception")) {
					ASICommon.error((String) response.get("exception"));
					console.println(MESSAGE_02);
				} else if (response.containsKey("message")) {
					console.println(response.get("message"));
					ASICommon.error((String) response.get("message"));
				} else {
					// Store data so we can write it later
					ic.setVariable("accsyn_client_id", client_id + "");
					ASICommon.info("Successfully authenticated accsyn server. Installation can proceed.");
					return true;
				}
			} catch (Exception e) {
				ASICommon.error(e);
				ASICommon.error(MESSAGE_03);
			}
		}
	}
}
