package org.zaproxy.zap.extension.customFire;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;

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
	JPanel addNewScriptPanel = new JPanel(); 

	String vulName;
	ScriptTreePanel stp;//un

	//protected Logger log = Logger.getLogger(CustomScriptsPopup.class.getName());//ser
	private String addedScript;
	private List<CustomScriptComponent> components = null;

	CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
	JTree tree = new JTree();
	//static JPopupMenu popup = new JPopupMenu();
	private JButton btnAddNewScript;
	private JButton btnSaveChanges;
	private JButton btnResetChanges;
	private JButton btnExit;
	List<JCheckBox> addedchkbxlist = new ArrayList<JCheckBox>();
	List<JButton> btnRemoveAddedScriptlist = new ArrayList<JButton>();
	


	/**
	 * Create the frame.
	 */
	public CustomScriptsPopup(final String vulName) {
		this.vulName = vulName;

		setTitle(vulName);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(700, 400);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JLabel lblHeaderlabel = new JLabel(Constant.messages.getString("customFire.custom.csp.label"));
		Font font = new Font("Sans Serif", Font.BOLD,15);
		lblHeaderlabel.setFont(font);
		contentPane.add(lblHeaderlabel, BorderLayout.NORTH);

		scriptsScrollPane = new JScrollPane();
		contentPane.add(scriptsScrollPane, BorderLayout.CENTER);

		JPanel footerPanel = new JPanel();
		contentPane.add(footerPanel, BorderLayout.SOUTH);
		footerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		btnAddNewScript = new JButton(Constant.messages.getString("customFire.custom.csp.button.addNewScript"));
		btnAddNewScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEditNewScriptUI dialog = new AddEditNewScriptUI(CustomScriptsPopup.this, "New Script",null);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);

			}
		});
		footerPanel.add(btnAddNewScript);

		btnSaveChanges = new JButton(Constant.messages.getString("customFire.custom.csp.button.save"));
		btnSaveChanges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean success = saveChanges();
				if (success) {
					JOptionPane.showMessageDialog(CustomScriptsPopup.this, 
							Constant.messages.getString("customFire.custom.csp.success.msg"),Constant.messages.getString("customFire.custom.csp.success.title")
							,JOptionPane.INFORMATION_MESSAGE);
					//CustomScriptsPopup.this.setVisible(false);
				} else {
					JOptionPane.showMessageDialog(CustomScriptsPopup.this, 
							Constant.messages.getString("customFire.custom.csp.failure.msg"),Constant.messages.getString("customFire.custom.csp.failure.title")
							,JOptionPane.ERROR_MESSAGE);
					//CustomScriptsPopup.this.setVisible(false);
				}
			}
		});
		footerPanel.add(btnSaveChanges);

		btnResetChanges = new JButton(Constant.messages.getString("customFire.custom.csp.button.reset"));
		btnResetChanges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(CustomScriptsPopup.this,
						Constant.messages.getString("customFire.custom.csp.alert.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
						,JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					resetChanges();
				}
			}
		});
		footerPanel.add(btnResetChanges);

		btnExit = new JButton(Constant.messages.getString("customFire.custom.csp.button.exit"));
		btnExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(CustomScriptsPopup.this,
						Constant.messages.getString("customFire.custom.csp.exit.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
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



	GridBagConstraints panelConstraints = new GridBagConstraints();
	GridBagConstraints gridBagConstraints = new GridBagConstraints();

	//scriptsPanel contains all scripts : newly added and existing
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
				Constant.messages.getString("customFire.custom.csp.label.default")
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
		components = new ArrayList<>();

		String vulNameM = vulName.replace(" ", "");
		vulNameM = vulNameM.replace(":", "");
		String fPath = Constant.getDefaultHomeDirectory(false)+"\\fuzzers\\fuzzdb-1.09\\attack-payloads\\"+Constant.messages.getString("customFire.csp."+vulNameM);//TODO M
		String folderPath = fPath.replace(Constant.FILE_SEPARATOR, Constant.FILE_SEPARATOR+Constant.FILE_SEPARATOR);

		File folder = new File(folderPath);

		if (folder.isDirectory()) {

			CScriptsTree ex = new CScriptsTree();
			tree = ex.getTree(folderPath);

			gridBagConstraints.fill=GridBagConstraints.HORIZONTAL;
			existingscriptsPanel.add(tree, gridBagConstraints);

		}

		else{
			System.out.println("There is no Folder @ given path :" + folderPath);
		}

		panelConstraints.gridy++;
		panelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;

		addNewScriptPanel.setBorder(new TitledBorder(null,"Add New Script" , TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scriptsPanel.add(addNewScriptPanel, panelConstraints);
		addNewScriptPanel.setLayout(new GridBagLayout());

		gridBagConstraints.gridy = 0;
	}

	/**
	 * For adding new scripts
	 *  void `
	 */
	void initScriptsNew() {

		if(addedScript != null){

			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridx++;

			final JCheckBox addedchkbx = new JCheckBox(addedScript);
//			addedchkbx = new JCheckBox(addedScript);
			
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
			addedchkbxlist.add(addedchkbx);
			gridBagConstraints.gridx++;
			final JButton btnRemoveAddedScript = new JButton(Constant.messages.getString("customFire.custom.csp.button.remove"));
			btnRemoveAddedScript.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int response = JOptionPane.showConfirmDialog(CustomScriptsPopup.this,
							Constant.messages.getString("customFire.custom.csp.remove.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
							, JOptionPane.YES_NO_OPTION);
					if (response == JOptionPane.YES_OPTION) {
						addNewScriptPanel.remove(addedchkbx);
						addNewScriptPanel.remove(btnRemoveAddedScript);

						addNewScriptPanel.updateUI();
						addNewScriptPanel.revalidate();
					}
				}
			});
			
			
			btnRemoveAddedScriptlist.add(btnRemoveAddedScript);
			
			
			addNewScriptPanel.add(btnRemoveAddedScript, gridBagConstraints);

			gridBagConstraints.gridy++;
		}
	}

	/**
	 * 
	 * @return boolean `
	 */
	public boolean saveChanges(){

		boolean b = true;

		/*Component[] componentsExistScriptPanel = existingscriptsPanel.getComponents();
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
		}*/

		/*try{ 
			//Ser
			CustomScriptsPopup cspSave = CustomScriptsPopup.this;
			FileOutputStream fos = new FileOutputStream("..\\zap-extensions\\src\\org\\zaproxy\\zap\\extension\\customFire\\resources\\"+vulName+"_Save.ser");//here in P
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(cspSave);//
			oos.close();
			fos.close();

			//Deser
			FileInputStream fis = new FileInputStream("..\\zap-extensions\\src\\org\\zaproxy\\zap\\extension\\customFire\\resources\\"+vulName+"_Save.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			CustomScriptsPopup si = new CustomScriptsPopup(vulName);
			si = (CustomScriptsPopup)ois.readObject();
			si.setVisible(true);
			ois.close();
			fis.close();

		}

		catch (Exception e1){
			b=false;
			e1.printStackTrace();
		} */		
		
		JFileChooser chooser = new JFileChooser(Constant.getPoliciesDir());
        File file = new File(Constant.getZapHome(), vulName+".ser");
        chooser.setSelectedFile(file);

        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else if (file.isFile() && file.getName().endsWith(".ser")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return Constant.messages.getString("customFire.custom.file.format.csp.ser");
            }
        });
        int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
        if (rc == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file == null) {
                b = false;
            }
            try {
            	CustomScriptsPopup cspSave = CustomScriptsPopup.this;
            	FileOutputStream fos = new FileOutputStream(file);
    			ObjectOutputStream oos = new ObjectOutputStream(fos);
    			oos.writeObject(cspSave);
    			oos.close();
    			fos.close();
                
            } catch (IOException e1) {
                //View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.save.error"));
            	b = false;
            }
        }
        if (rc == JFileChooser.CANCEL_OPTION) {
        	chooser.setVisible(false);
        	b = false;
        }
			
		return b;
	}

	/**
	 * 
	 *  void `
	 */
	public void resetChanges(){
		try {		            
			removeAddedScripts(addNewScriptPanel);
			removeAddedScripts(existingscriptsPanel);
			initScriptsExisting();
		} catch(Exception e1) {
			JOptionPane.showMessageDialog(CustomScriptsPopup.this, 
					Constant.messages.getString("customFire.custom.csp.reset.fail.msg"),Constant.messages.getString("customFire.custom.csp.failure.title")
					,JOptionPane.ERROR_MESSAGE);
			//log.error("Resetting scripts failed");
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
					Constant.messages.getString("customFire.custom.csp.error.scriptExists"),Constant.messages.getString("customFire.custom.csp.error.scriptExists.title")
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
					Constant.messages.getString("customFire.custom.csp.error.scriptExists"),Constant.messages.getString("customFire.custom.csp.error.scriptExists.title")
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
	 * @return boolean 
	 */
	public boolean checkIfScriptExists(String s) {
		boolean b = false;

		ArrayList<DefaultMutableTreeNode> leafs = new ArrayList<DefaultMutableTreeNode>();
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
		DefaultMutableTreeNode firstLeaf = root.getFirstLeaf();
		leafs.add(firstLeaf);
		DefaultMutableTreeNode tmpLeaf = firstLeaf.getNextLeaf();
		while(tmpLeaf!=null){
			leafs.add(tmpLeaf);
			tmpLeaf = tmpLeaf.getNextLeaf();
		}
		for(DefaultMutableTreeNode leaf : leafs){
			if(leaf.getUserObject() instanceof CheckBoxNode){
				CheckBoxNode checkBox = (CheckBoxNode) leaf.getUserObject();
				if(s.trim().equals(checkBox.getText().trim())){
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
				if(s.trim().equals(checkBox.getText())){
					b=true;
					return b;
				}
				else
					b=false;
			}
		}

		return b;
	}

	/**
	 * @return the btnAddNewScript
	 */
	public JButton getBtnAddNewScript() {
		if (btnAddNewScript == null ) {
			btnAddNewScript = new JButton(Constant.messages.getString("customFire.custom.csp.button.addNewScript"));
			
		}
		return btnAddNewScript;
	}

	/**
	 * @param btnAddNewScript the btnAddNewScript to set
	 */
	public void setBtnAddNewScript(JButton btnAddNewScript) {
		this.btnAddNewScript = btnAddNewScript;
	}

	/**
	 * @return the btnSaveChanges
	 */
	public JButton getBtnSaveChanges() {
		if (btnSaveChanges == null ) {
			btnSaveChanges = new JButton(Constant.messages.getString("customFire.custom.csp.button.save"));
			
		}
		return btnSaveChanges;
	}

	/**
	 * @param btnSaveChanges the btnSaveChanges to set
	 */
	public void setBtnSaveChanges(JButton btnSaveChanges) {
		this.btnSaveChanges = btnSaveChanges;
	}

	/**
	 * @return the btnResetChanges
	 */
	public JButton getBtnResetChanges() {
		if (btnResetChanges == null ) {
			btnResetChanges = new JButton(Constant.messages.getString("customFire.custom.csp.button.reset"));
			
		}
		return btnResetChanges;
	}

	/**
	 * @param btnResetChanges the btnResetChanges to set
	 */
	public void setBtnResetChanges(JButton btnResetChanges) {
		this.btnResetChanges = btnResetChanges;
	}

	/**
	 * @return the btnExit
	 */
	public JButton getBtnExit() {
		if (btnExit == null ) {
			btnExit = new JButton(Constant.messages.getString("customFire.custom.csp.button.exit"));
			
		}
		return btnExit;
	}

	/**
	 * @param btnExit the btnExit to set
	 */
	public void setBtnExit(JButton btnExit) {
		this.btnExit = btnExit;
	}

}