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

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
public class ASIDaemonUserScreen extends AbstractInstallerScreen implements ActionListener, ItemListener {

	static final String LABEL_01 = "Choose which system user "+(ASICommon.isWindows()?"service":"daemon")+" should run as:";
	static final String LABEL_02 = "Default("+(ASICommon.isWindows()?"Local system account":"root")+")";
	static final String LABEL_04 = "Username:";
	static final String LABEL_05 = "Run "+(ASICommon.isWindows()?"service":"daemon")+" as default user("+(ASICommon.isWindows()?"Local system account":"root")+")";
	static final String LABEL_06 = "Custom user";
	static final String LABEL_07 = "Please enter choice [1 or 2]: ";
	static final String LABEL_08 = "Please enter choice [yes or no]: ";
	static final String LABEL_09 = "Umask:";
	static final String LABEL_10 = "Enter umask (leave empty and defaults will apply):";
	static final String LABEL_11 = "Password:";
	
	static final String NOTE_01 = "User will have to have read and write access to the storage handled by accsyn (root share), and system users cannot be configured within accsyn unless this user has "+(ASICommon.isWindows()?"administrative":"root")+" permission to switch user (unix: sudo)";
	static final String NOTE_02 = "All file operations will run as this user, both file transfers and file listings with associated file operations (mkdir, rename, move, delete)";
	static final String NOTE_03 = "You can also configure a global, and per user, system username.";
	static final String NOTE_04 = "Make sure user have been granted the 'Log on as a service' right (Logon as administrator, goto Administrative Tools>Local Security Policy>Local Policy>User Rights Assignment and add the user to 'Log on as a service' group).";
	static final String NOTE_05 = "Leave empty and default umask will apply.";
			
	static final String MESSAGE_01 = "Please enter a username";
	static final String MESSAGE_02 = "Could not validate user, proceed anyway?";
	static final String MESSAGE_03 = "Please enter a valid password!";
	static final String MESSAGE_04 = "Please enter a valid umask (octal form)!";
	
	private JCheckBox cb_default;
	
	private JPanel p_override;
	private JLabel l_username_note;
	private JTextField tf_daemon_username;
	private JPasswordField pf_pass;
	private JTextField tf_daemon_umask;
	JPanel p_pass;
	
	//private String username;
	

	// GUI setup ////////////////////////////////////////////////////////////////

	private JLabel addNote(JPanel p, GridBagConstraints c, Console console, String text) {
		JLabel l = null;
		if (p != null) {
			l = new JLabel("<html>Note:&nbsp;"+text.replace("\n", "<br>")+"</html>");
			l.setForeground(new Color(66,66,66));
			l.setFont(new Font(l.getFont().getFamily(), Font.PLAIN, l.getFont().getSize()-2));
			c.insets = new Insets(1, 20, 1, 1);
			p.add(l, c);
			c.insets = new Insets(1, 1, 1, 1);
			p.add(new JLabel(), c);
		} else {
			console.println("Note: "+text);
		}
		return l;
	}
	
	private JPanel createSysUsernamePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);
		// panel.setOpaque(true);
		// panel.setBackground(Color.RED);

		c.weightx = 100;
		c.gridwidth = GridBagConstraints.REMAINDER;

		JLabel l;

		JPanel p_ident = new JPanel(new GridBagLayout());
		GridBagConstraints c_ident = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);
		c_ident.gridheight = GridBagConstraints.REMAINDER;
		p_ident.add(l = new JLabel(LABEL_04), c_ident);
		l.setMinimumSize(new Dimension(120, l.getMinimumSize().height));
		l.setPreferredSize(new Dimension(120, l.getPreferredSize().height));
		c_ident.weightx = 100;
		c_ident.gridwidth = GridBagConstraints.REMAINDER;
		p_ident.add(tf_daemon_username = new JTextField(), c_ident);
		panel.add(p_ident, c);

		p_pass = new JPanel(new GridBagLayout());
		GridBagConstraints c_pass = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);
		c_pass.gridheight = GridBagConstraints.REMAINDER;
		p_pass.add(l = new JLabel(LABEL_11), c_pass);
		l.setMinimumSize(new Dimension(120, l.getMinimumSize().height));
		l.setPreferredSize(new Dimension(120, l.getPreferredSize().height));
		c_pass.weightx = 100;
		c_pass.gridwidth = GridBagConstraints.REMAINDER;
		p_pass.add(pf_pass = new JPasswordField(), c_pass);
		panel.add(p_pass, c);
		p_pass.setVisible(ASICommon.isWindows());
		
		panel.add(new JLabel(), c);
		
		//addNote(panel, c, null, NOTE_03);
		
		if (ASICommon.isWindows())
			addNote(panel, c, null, NOTE_04);
		
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weighty = 100;
		panel.add(new JLabel(), c);

		return panel;
	}

	@Override
	public JComponent createComponent() {
		if (ASICommon.isDEV())
			ASICommon.info("!!!Launched in DEVELOPMENT mode!!!");
		JLabel l;
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);
		// panel.setOpaque(true);
		// panel.setBackground(Color.RED);

		c.weightx = 100;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weighty = 1;

		panel.add(new JLabel(), c);

		panel.add(new JLabel("<html>" + LABEL_01 + "</html>"), c);
		panel.add(new JLabel(""), c);

		panel.add(cb_default = new JCheckBox(LABEL_02), c);
		cb_default.setSelected(true);
		cb_default.addActionListener(this);
		
		panel.add(new JLabel(""), c);

		panel.add(p_override = createSysUsernamePanel(), c);

		l_username_note = addNote(panel, c, null, NOTE_02);

		JPanel p_umask = new JPanel(new GridBagLayout());
		GridBagConstraints c_umask = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1);
		c_umask.gridheight = GridBagConstraints.REMAINDER;
		p_umask.add(l = new JLabel(LABEL_09), c_umask);
		l.setMinimumSize(new Dimension(120, l.getMinimumSize().height));
		l.setPreferredSize(new Dimension(120, l.getPreferredSize().height));
		c_umask.weightx = 100;
		c_umask.gridwidth = GridBagConstraints.REMAINDER;
		p_umask.add(tf_daemon_umask = new JTextField(), c_umask);
		panel.add(p_umask, c);
		p_umask.setVisible(!ASICommon.isWindows());
		
		if (!ASICommon.isWindows())
			addNote(panel, c, null, NOTE_05);
		
		c.weightx = 100;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weighty = 50;
		panel.add(new JLabel(), c);

		updateScreen();

		// getInstallerContext()
		return panel;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		updateScreen();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cb_default) {
			if (cb_default.isSelected())
				tf_daemon_username.setText("");
		}
		updateScreen();
	}

	private void updateScreen() {
		p_override.setVisible(!cb_default.isSelected());
		l_username_note.setVisible(!cb_default.isSelected());
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
		// Detect current user
		/*this.username = detectDaemonUser();
		if (this.username != null) {
			rb_override.setSelected(true);
			tf_daemon_username.setText(this.username);
		} else
			rb_default.setSelected(true);*/
		super.activated();
	}

	@Override
	public boolean next() {
		InstallerContext ic = getInstallerContext();
		String username = null, password = null, umask=null;
		if (!cb_default.isSelected()) {
			// Check input
			
			if (tf_daemon_username.getText().length()==0) {
				JOptionPane.showMessageDialog(null, MESSAGE_01, "System username", JOptionPane.WARNING_MESSAGE);
				return false;
			} else if (ASICommon.isWindows() && pf_pass.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, MESSAGE_03, "Authentication", JOptionPane.WARNING_MESSAGE);
				return false;
			} else {
				// Validate
				
				if (!ASICommon.validateSystemUser(tf_daemon_username.getText()) && JOptionPane.showConfirmDialog(null, MESSAGE_02, "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
					return false;
				
				username = tf_daemon_username.getText();
				if (ASICommon.isWindows())
					password = pf_pass.getText();
			}
		}
		umask = tf_daemon_umask.getText();
		if (0<umask.length()) {
			try {
				Integer.parseInt(umask,8);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, MESSAGE_04, "Configure daemon", JOptionPane.WARNING_MESSAGE);
				return false;
			}
		} 
		ic.setVariable("accsyn_daemon_user", username != null ? username : "");
		ic.setVariable("accsyn_daemon_password", password != null ? password : "");
		ic.setVariable("accsyn_daemon_umask", umask != null ? umask : "");
		return super.next();
	}

	// Console only

	@Override
	public boolean handleConsole(Console console) throws UserCanceledException {
		InstallerContext ic = getInstallerContext();
		
		String username = null;
		String umask = null;
		
		boolean override_username = false;
		while (true) {
			console.println();
			console.println("1) " + LABEL_05);
			console.println("2) " + LABEL_06);
			console.print(LABEL_07);
			String s = console.readLine();
			if (s == null || !s.matches("^[1-2]{1}$")) {
				System.err.println(MESSAGE_01);
			} else {
				override_username = s.equals("2");
				break;
			}
		}

		if (override_username) {

			addNote(null, null, console, NOTE_02);
			console.println();

			console.println();
			console.println(LABEL_04);
			addNote(null, null, console, NOTE_01);
			if (ASICommon.isWindows())
				addNote(null, null, console, NOTE_04);
			console.println();
			while (true) {
				String username_unvalidated = null;
				while (username_unvalidated == null) {
					console.print(LABEL_04);
					String s = console.readLine();
					if (s == null || s.length()==0) {
						System.err.println(MESSAGE_01);
					} else
						username_unvalidated = s;
				}
				// Validate
				
				if (!ASICommon.validateSystemUser(username_unvalidated)) {
					while (true) {
						console.println();
						console.println(MESSAGE_02);
						console.println();
						console.print(LABEL_08);
						String s = console.readLine();
						if (s == null || (!s.equalsIgnoreCase("yes") && !s.equalsIgnoreCase("no"))) {
							
						} else {
							if (s.equalsIgnoreCase("yes")) {
								username = username_unvalidated;
							} else {
							}
							break;
						}
					}
					if (username != null)
						break;
				} else {
					username = username_unvalidated;
					break;
				}
			}
	
		}
		
		if (!ASICommon.isWindows()) {
			String umask_unvalidated;
			while (true) {
				console.println();
				console.print(LABEL_10);
				umask_unvalidated = console.readLine();
				if (umask_unvalidated != null && 0<umask_unvalidated.length()) {
					try {
						Integer.parseInt(umask_unvalidated,8);
						umask = umask_unvalidated;
						break;
					} catch (Exception e) {
						console.println();
						System.err.println(MESSAGE_04);
					}
				} else
					break;
			}
		}
		
		ic.setVariable("accsyn_daemon_user", username != null ? username : "");
		ic.setVariable("accsyn_daemon_umask", umask != null ? umask : "");
		
		return true;
	}

}
