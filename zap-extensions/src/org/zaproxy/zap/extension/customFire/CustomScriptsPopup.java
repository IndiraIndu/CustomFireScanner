package org.zaproxy.zap.extension.customFire;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */
public class CustomScriptsPopup extends JFrame {

	private static final long serialVersionUID = 1L;
	private JScrollPane scriptsScrollPane;
	private ScriptTreePanel scriptTree;

	JPanel existingscriptsPanel = new JPanel();
	final JPanel addNewScriptPanel = new JPanel(); 

	String vulName;

	protected Logger log = Logger.getLogger(CustomScriptsPopup.class.getName());
	private String addedScript;
	private List<CustomScriptComponent> components = null;

	/**
	 * Create the frame.
	 */
	public CustomScriptsPopup(final ScriptTreePanel stp,final String vulName) {

		this.vulName = vulName;

		setTitle(vulName);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(700, 400);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JLabel lblHeaderlabel = new JLabel("Select Scripts: ");
		Font font = new Font("Sans Serif", Font.BOLD,15);
		lblHeaderlabel.setFont(font);
		contentPane.add(lblHeaderlabel, BorderLayout.NORTH);

		scriptsScrollPane = new JScrollPane();
		contentPane.add(scriptsScrollPane, BorderLayout.CENTER);

		JPanel footerPanel = new JPanel();
		contentPane.add(footerPanel, BorderLayout.SOUTH);
		footerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JButton btnAddNewScript = new JButton("Add New Script");
		btnAddNewScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEditNewScriptUI dialog = new AddEditNewScriptUI(CustomScriptsPopup.this, "New Script",null);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);

			}
		});
		footerPanel.add(btnAddNewScript);

		JButton btnSaveChanges = new JButton("Save");
		btnSaveChanges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean success = saveChanges();
				if (success) {
					JOptionPane.showMessageDialog(CustomScriptsPopup.this, "Changed succesfully Saved!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(CustomScriptsPopup.this, "Changes not saved", "Failure",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		footerPanel.add(btnSaveChanges);

		JButton btnResetChanges = new JButton("Reset");
		btnResetChanges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(CustomScriptsPopup.this,
						"Confirm Reset", "Confirm",
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					resetChanges();
				}
			}
		});
		footerPanel.add(btnResetChanges);

		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(CustomScriptsPopup.this,
						"Do You want to save changes before exit?", 
						"Confirm"
						,JOptionPane.YES_NO_CANCEL_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					saveChanges();
					CustomScriptsPopup.this.setVisible(false);
				} else if(response==JOptionPane.NO_OPTION){
					resetChanges();
					CustomScriptsPopup.this.setVisible(false);
				}
			}
		});
		footerPanel.add(btnExit);
		initScriptsExisting();
		initScriptsNew();
	}

	/*public CustomScriptsPopup(ScriptTreePanel thisFrame) {
		this.scriptTree = thisFrame;

	}*/

	GridBagConstraints panelConstraints = new GridBagConstraints();
	GridBagConstraints gridBagConstraints = new GridBagConstraints();

	//scriptsPanel contains both new and existing
	JPanel scriptsPanel = new JPanel();			

	private boolean saveCustomSelectedScripts;

	/*** Initialize UI with the scripts***/
	/**
	 * @param vName
	 * 
	 * @return void
	 */
	void initScriptsExisting() {

		//Scroll panel for both new and existing scripts
		scriptsScrollPane.setViewportView(scriptsPanel);
		GridBagLayout gridBagLayout = new GridBagLayout();
		scriptsPanel.setLayout(gridBagLayout);

		panelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraints.fill = GridBagConstraints.HORIZONTAL;
		panelConstraints.weightx = 1.0;
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 0;

		existingscriptsPanel.setBorder(new TitledBorder(null,
				"Default Scripts"
				,TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scriptsPanel.add(existingscriptsPanel, panelConstraints);

		GridBagLayout settingPanelLayout = new GridBagLayout();
		existingscriptsPanel.setLayout(settingPanelLayout);

		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;

		String str;
		try{
			components = new ArrayList<>();

			File file = new File("C:\\"+vulName+".txt");
			FileReader fr = new FileReader(file);
			LineNumberReader lnr = new LineNumberReader(fr);

			if(file.exists()){
				while((str=lnr.readLine())!=null){
					if(lnr.getLineNumber()>0){
						gridBagConstraints.gridy++;
					}
					JCheckBox chkbx = new JCheckBox(str);
					chkbx.setSelected(true);
					CustomScriptComponent component = new CustomScriptComponent(chkbx, gridBagConstraints);
					components.add(component);
					existingscriptsPanel.add(chkbx, gridBagConstraints);
				}

			}else{
				log.error("Scripts file is missing");
			}

			lnr.close();
			fr.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		panelConstraints.gridy++;
		panelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

		addNewScriptPanel.setBorder(new TitledBorder(null,"Add New Script" , TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scriptsPanel.add(addNewScriptPanel, panelConstraints);
		addNewScriptPanel.setLayout(new GridBagLayout());

		gridBagConstraints.gridy = 0;
	}

	void initScriptsNew() {

		if(addedScript != null){

			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridx++;

			final JCheckBox addedchkbx = new JCheckBox(addedScript);
			addedchkbx.setSelected(true);	
			addNewScriptPanel.add(addedchkbx, gridBagConstraints);

			addedchkbx.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						AddEditNewScriptUI dialog = new AddEditNewScriptUI(CustomScriptsPopup.this, "Edit Script",addedchkbx);
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setVisible(true);
					}
				}
				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

			});
			gridBagConstraints.gridx++;
			final JButton btnRemoveAttribute = new JButton("Remove Script");
			btnRemoveAttribute.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int response = JOptionPane.showConfirmDialog(CustomScriptsPopup.this,
							"Remove Script", "Confirm"
							, JOptionPane.YES_NO_OPTION);
					if (response == JOptionPane.YES_OPTION) {
						addNewScriptPanel.remove(addedchkbx);
						addNewScriptPanel.remove(btnRemoveAttribute);

						addNewScriptPanel.updateUI();
						addNewScriptPanel.revalidate();
					}
				}
			});

			addNewScriptPanel.add(btnRemoveAttribute, gridBagConstraints);

			gridBagConstraints.gridy++;
		}
	}

	private boolean saveChanges(){

		boolean b = true;

		Component[] componentsExistScriptPanel = existingscriptsPanel.getComponents();
		Component[] componentsNewScriptPanel = addNewScriptPanel.getComponents();

		List<String> selectedChkbxScripts = new ArrayList<String>();
		for (Component component2 : componentsExistScriptPanel) {
			if (component2 instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) component2;
				if(checkBox.isSelected()){
					selectedChkbxScripts.add(checkBox.getText());
				}
			}
		}

		for (Component component2 : componentsNewScriptPanel) {
			if (component2 instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) component2;
				if(checkBox.isSelected()){
					selectedChkbxScripts.add(checkBox.getText());
				}
			}
		}

		try{

			File f = new File("..\\zap-extensions\\src\\org\\zaproxy\\zap\\extension\\customFire\\resources\\test.txt");
			FileWriter fw = new FileWriter(f, false);
			BufferedWriter bw = new BufferedWriter(fw);
			for(String s : selectedChkbxScripts){
				bw.write(s);
				bw.newLine();
			}
			b = true;
			bw.close();
			fw.close();
		}

		catch (IOException e1){
			b=false;
			e1.printStackTrace();
		}		
		return b;
	}

	private void resetChanges(){
		try {		            
			removeAddedScripts(addNewScriptPanel);
			removeAddedScripts(existingscriptsPanel);
			initScriptsExisting();
		} catch(Exception e1) {
			JOptionPane.showMessageDialog(CustomScriptsPopup.this, "Reset Failed","Failed",JOptionPane.ERROR_MESSAGE);
			log.error("Resetting settings failed");
		}
	}


	private void removeAddedScripts(JPanel jPanel) {
		jPanel.removeAll();

	}

	public void onEditDesiredScript(String s,JCheckBox cb) {
		if(!checkIfScriptExists(s)){
			cb.setText(s);
		}
		else{
			JOptionPane.showMessageDialog(CustomScriptsPopup.this, 
					"Script Exists already!", "Script Exists!"
					, JOptionPane.ERROR_MESSAGE);
		}
	}


	public void onAddDesiredScript(String s) {
		this.addedScript = s;
		if(!checkIfScriptExists(s)){
			initScriptsNew();
			addNewScriptPanel.updateUI();
		}
		else{
			JOptionPane.showMessageDialog(CustomScriptsPopup.this, 
					"Script Exists already!", "Script Exists!"
					, JOptionPane.ERROR_MESSAGE);
		}
	}


	public void onDeleteDesiredScript() {
		//this.scriptVal=scriptVal;
		//addNewScriptPanel.removeAll();
		//initScripts();
	}


	public void getDesiredScripts() {
	}


	/**
	 * 
	 * @param s
	 * @return boolean `
	 */
	public boolean checkIfScriptExists(String s) {
		boolean b = false;
		Component[] componentsExistScriptPanel = existingscriptsPanel.getComponents();
		for (Component component2 : componentsExistScriptPanel) {
			if (component2 instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) component2;
				if(s.equals(checkBox.getText())){
					b=true;
					return b;
				}
				else
					b=false;
			}
		}

		Component[] componentsNewScriptPanel = addNewScriptPanel.getComponents();
		for (Component component2 : componentsNewScriptPanel) {
			if (component2 instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) component2;
				if(s.equals(checkBox.getText())){
					b=true;
					return b;
				}
				else
					b=false;
			}
		}

		return b;
	}
}