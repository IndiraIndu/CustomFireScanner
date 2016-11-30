package org.zaproxy.zap.extension.customFire;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 * 
 */
public class AddEditNewScriptUI extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField txtScriptVals;

	/**
	 * Create the dialog box to add/edit additional scripts for a Test.
	 */
	public AddEditNewScriptUI(final CustomScriptsPopup customScriptsPopup, final String title, final JCheckBox cb) {
		setTitle(title);
		setBounds(100, 100, 450, 150);
		getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5)); 
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10,0,0,0);

		c.gridy++;
		c.gridx = 0;
		JLabel lblValue = new JLabel("Script: ");
		contentPanel.add(lblValue,c);

		c.gridx++;
		txtScriptVals = new JTextField();
		contentPanel.add(txtScriptVals, c);

		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new FlowLayout());
		getContentPane().add(footerPanel,BorderLayout.SOUTH);

		final JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (txtScriptVals.getText().equals("")) {
					JOptionPane.showMessageDialog(AddEditNewScriptUI.this, 
							"Please enter a Script", "No Script!"
							, JOptionPane.ERROR_MESSAGE);
					return;
				}

				if(title=="New Script"){
					customScriptsPopup.onAddDesiredScript(txtScriptVals.getText());
				}

				if(title=="Edit Script"){
					customScriptsPopup.onEditDesiredScript(txtScriptVals.getText(),cb);
					//cb.setText(txtScriptVals.getText());
				}

				AddEditNewScriptUI.this.setVisible(false);
				customScriptsPopup.addNewScriptPanel.updateUI();
				customScriptsPopup.addNewScriptPanel.revalidate();
			}
		});

		footerPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEditNewScriptUI.this.setVisible(false);
			}
		});
		footerPanel.add(cancelButton);
	}

	/**
	 * Get the value of text script to update its content
	 *
	 * @return String
	 */
	public String getTxtScriptValues() {
		return txtScriptVals.getText();
	}
}
