/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.customFire;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.customFire.JCheckBoxScriptsTree.CheckChangeEvent;
import org.zaproxy.zap.extension.customFire.JCheckBoxScriptsTree.CheckedNode;
import org.zaproxy.zap.view.JCheckBoxTree;



/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 * 
 * A {@code JPanel} that allows to display and select scripts through a check box tree.
 *
 * @see Script
 * @see ScriptSet
 * @see JCheckBoxTree
 * @since TODO add version
 *
 */
public class ScriptTreePanel extends JPanel {

	private static final long serialVersionUID = 5514692105773714202L;

	private final JCheckBoxScriptsTree scriptTree;

	private final HashMap<Script, DefaultMutableTreeNode> scriptToNodeMap;

	ExtensionCustomFire ext = new ExtensionCustomFire();

	List<String> pList = new ArrayList<String>();

	//HashMap<TreePath, CheckedNode> nodesCheckingState;

	public ScriptTreePanel(String nameRootNode) {
		setLayout(new BorderLayout());

		scriptToNodeMap = new HashMap<>();
		scriptTree = new JCheckBoxScriptsTree() {


			private static final long serialVersionUID = 1L;

			@Override
			protected void setExpandedState(TreePath path, boolean state) {
				// Ignore all collapse requests; collapse events will not be fired
				if (state) {
					super.setExpandedState(path, state);
				}
			}

		};
		// Initialize the structure based on all the Scripts we know about
		ScriptSet ts = new ScriptSet(Script.getPluginNameList());
		Iterator<Script> iter = ts.getIncludeScript().iterator();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(nameRootNode);
		root.setAllowsChildren(true);


		Script script;
		DefaultMutableTreeNode parent;
		DefaultMutableTreeNode node;
		while (iter.hasNext()) {
			script = iter.next();
			if (script.getParent() != null) {
				parent = scriptToNodeMap.get(script.getParent());

			} else {
				parent = null;

			}
			if (parent == null) {
				parent = root;
				script.setParent(root);
			}
			node = new DefaultMutableTreeNode(script.getUiName());

			pList.add(script.getUiName());

			node.setUserObject(script);

			parent.add(node);

			scriptToNodeMap.put(script, node);
		}



		scriptTree.setModel(new DefaultTreeModel(root));

		scriptTree.setRootVisible(true);
		scriptTree.setShowsRootHandles(false);

		addScriptTreeListener(this, false);

		/*scriptTree.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				if(e.getClickCount()==2){
					//Do deser
					String vulName = pList.get((scriptTree.getRowForLocation(e.getX(), e.getY())-1));
					CustomScriptsPopup scriptPopupUI = new CustomScriptsPopup(vulName);
					JFileChooser chooser = new JFileChooser(Constant.getZapHome());
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
					File file = null;
					int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
					if (rc == JFileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
						if (file == null || !(file.getName().equalsIgnoreCase(vulName+".ser")) ) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.mismatch.error"));
							return;
						}
						try {
							FileInputStream fis = new FileInputStream(file.getPath());
							ObjectInputStream ois = new ObjectInputStream(fis);
							//CustomScriptsPopup scriptPopupUI1 = new CustomScriptsPopup(vulName);
							final CustomScriptsPopup scriptPopupUI1 = (CustomScriptsPopup)ois.readObject();
							scriptPopupUI1.getBtnAddNewScript().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									AddEditNewScriptUI dialog = new AddEditNewScriptUI(scriptPopupUI1, "New Script",null);
									dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
									dialog.setVisible(true);

								}
							});

							scriptPopupUI1.getBtnSaveChanges().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									boolean success = scriptPopupUI1.saveChanges();
									if (success) {
										JOptionPane.showMessageDialog(scriptPopupUI1, 
												Constant.messages.getString("customFire.custom.csp.success.msg"),Constant.messages.getString("customFire.custom.csp.success.title")
												,JOptionPane.INFORMATION_MESSAGE);
										scriptPopupUI1.setVisible(false);
									} else {
										JOptionPane.showMessageDialog(scriptPopupUI1, 
												Constant.messages.getString("customFire.custom.csp.failure.msg"),Constant.messages.getString("customFire.custom.csp.failure.title")
												,JOptionPane.ERROR_MESSAGE);
										//CustomScriptsPopup.this.setVisible(false);
									}
								}
							});



							scriptPopupUI1.getBtnResetChanges().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									int response = JOptionPane.showConfirmDialog(scriptPopupUI1,
											Constant.messages.getString("customFire.custom.csp.alert.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
											,JOptionPane.YES_NO_OPTION);
									if (response == JOptionPane.YES_OPTION) {
										scriptPopupUI1.resetChanges();
									}
								}
							});

							scriptPopupUI1.getBtnExit().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									int response = JOptionPane.showConfirmDialog(scriptPopupUI1,
											Constant.messages.getString("customFire.custom.csp.exit.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
											,JOptionPane.YES_NO_CANCEL_OPTION);
									if (response == JOptionPane.YES_OPTION) {
										scriptPopupUI1.saveChanges();
										scriptPopupUI1.setVisible(false);
									} else if(response==JOptionPane.NO_OPTION){
										scriptPopupUI1.resetChanges();
										scriptPopupUI1.setVisible(false);
									}
								}
							});

							scriptPopupUI = scriptPopupUI1;

							ois.close();
							fis.close();
						} catch ( IOException | ClassNotFoundException e1) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.load.error"));
						}
					}

					if(!scriptPopupUI.isVisible()){
						scriptPopupUI.setVisible(true);
					}
					else{
						scriptPopupUI.toFront();
					}

				}//double click
			} });*/

		scriptTree.setCheckBoxEnabled(new TreePath(root), false);
		reset();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(scriptTree);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		add(scrollPane, BorderLayout.CENTER);

	}

	/**
	 * 
	 *  void `
	 */
	public void saveScriptsState(){
		//Do ser
		JFileChooser chooser = new JFileChooser(Constant.getPoliciesDir());
		File file = new File(Constant.getZapHome(), "Scripts.ser");
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
				return;
			}
			try {
				//ScriptTreePanel stp = new ScriptTreePanel(Constant.messages.getString("customFire.custom.tab.script.node"));
				FileOutputStream fos = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(ScriptTreePanel.this);
				oos.close();
				fos.close();
				View.getSingleton().showMessageDialog(Constant.messages.getString("customFire.custom.ser.saveScripts.success"));

			} catch (IOException e1) {
				View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.save.error"));
				return;
			}
		}
		if (rc == JFileChooser.CANCEL_OPTION) {
			chooser.setVisible(false);
			return;
		}


	}


	/**
	 * 
	 * @param stp
	 * @return JCheckBoxTree `
	 */
	public ScriptTreePanel addScriptTreeListener(final ScriptTreePanel stp, final boolean s){

		stp.scriptTree.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				TreePath tp = scriptTree.getPathForLocation(e.getX(), e.getY());
				scriptTree.check1(tp, s);
				scriptTree.repaint();

				if(e.getClickCount()==2){

					String vulName = pList.get((scriptTree.getRowForLocation(e.getX(), e.getY())-1));
					CustomScriptsPopup scriptPopupUI = new CustomScriptsPopup(vulName);
					JFileChooser chooser = new JFileChooser(Constant.getZapHome());
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
					File file = null;
					int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
					if (rc == JFileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
						if (file == null || !(file.getName().equalsIgnoreCase(vulName+".ser")) ) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.mismatch.error"));
							return;
						}
						try {
							FileInputStream fis = new FileInputStream(file.getPath());
							ObjectInputStream ois = new ObjectInputStream(fis);
							
							final CustomScriptsPopup scriptPopupUI1 = (CustomScriptsPopup)ois.readObject();
							
							scriptPopupUI1.getBtnAddNewScript().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									AddEditNewScriptUI dialog = new AddEditNewScriptUI(scriptPopupUI1, "New Script",null);
									dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
									dialog.setVisible(true);

								}
							});

							scriptPopupUI1.getBtnSaveChanges().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									boolean success = scriptPopupUI1.saveChanges();
									if (success) {
										JOptionPane.showMessageDialog(scriptPopupUI1, 
												Constant.messages.getString("customFire.custom.csp.success.msg"),Constant.messages.getString("customFire.custom.csp.success.title")
												,JOptionPane.INFORMATION_MESSAGE);
										scriptPopupUI1.setVisible(false);
									} else {
										JOptionPane.showMessageDialog(scriptPopupUI1, 
												Constant.messages.getString("customFire.custom.csp.failure.msg"),Constant.messages.getString("customFire.custom.csp.failure.title")
												,JOptionPane.ERROR_MESSAGE);
										//CustomScriptsPopup.this.setVisible(false);
									}
								}
							});



							scriptPopupUI1.getBtnResetChanges().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									int response = JOptionPane.showConfirmDialog(scriptPopupUI1,
											Constant.messages.getString("customFire.custom.csp.alert.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
											,JOptionPane.YES_NO_OPTION);
									if (response == JOptionPane.YES_OPTION) {
										scriptPopupUI1.resetChanges();
									}
								}
							});

							scriptPopupUI1.getBtnExit().addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									int response = JOptionPane.showConfirmDialog(scriptPopupUI1,
											Constant.messages.getString("customFire.custom.csp.exit.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
											,JOptionPane.YES_NO_CANCEL_OPTION);
									if (response == JOptionPane.YES_OPTION) {
										scriptPopupUI1.saveChanges();
										scriptPopupUI1.setVisible(false);
									} else if(response==JOptionPane.NO_OPTION){
										scriptPopupUI1.resetChanges();
										scriptPopupUI1.setVisible(false);
									}
								}
							});

							for(int i=0; i<scriptPopupUI1.btnRemoveAddedScriptlist.size(); i++){
								final JButton b = scriptPopupUI1.btnRemoveAddedScriptlist.get(i);
								final JCheckBox c = scriptPopupUI1.addedchkbxlist.get(i);
								b.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										int response = JOptionPane.showConfirmDialog(scriptPopupUI1,
												Constant.messages.getString("customFire.custom.csp.remove.msg"),Constant.messages.getString("customFire.custom.csp.alert.title")
												, JOptionPane.YES_NO_OPTION);
										if (response == JOptionPane.YES_OPTION) {
											scriptPopupUI1.addNewScriptPanel.remove(c);
											scriptPopupUI1.addNewScriptPanel.remove(b);

											scriptPopupUI1.addNewScriptPanel.updateUI();
											scriptPopupUI1.addNewScriptPanel.revalidate();
										}

									}
								});
							}

							for(int i=0; i<scriptPopupUI1.addedchkbxlist.size(); i++){
								final JCheckBox c = scriptPopupUI1.addedchkbxlist.get(i);
								c.addMouseListener(new MouseListener(){

									@Override
									public void mouseClicked(MouseEvent e) {
										if (e.getClickCount() == 2) {
											AddEditNewScriptUI dialog = new AddEditNewScriptUI(scriptPopupUI1, "Edit Script",c);
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

							}

							scriptPopupUI = scriptPopupUI1;
														
							ois.close();
							fis.close();
						} catch ( IOException | ClassNotFoundException e1) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.load.error"));
						}
					}

					if(!scriptPopupUI.isVisible()){
						scriptPopupUI.setVisible(true);
					}
					else{
						scriptPopupUI.toFront();
					}

				}
			} });

		return stp;

	}




	/**
	 * Sets the scripts that should be selected, if included, and not if excluded.
	 *
	 * @param scriptSet the scripts that will be selected, if included, and not if excluded.
	 * @see ScriptSet#includes(Script)
	 */
	public void setScriptSet(ScriptSet scriptSet) {
		Set<Script> includedScript = scriptSet.getIncludeScript();
		Iterator<Entry<Script, DefaultMutableTreeNode>> iter = scriptToNodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Script, DefaultMutableTreeNode> node = iter.next();
			TreePath tp = this.getPath(node.getValue());
			Script script = node.getKey();
			if (Script.getPluginNameList().contains(script)) { //im
				scriptTree.check(tp, containsAnyOfTopLevelScript(includedScript, script));
			} else {
				scriptTree.check(tp, scriptSet.includes(script));
			}
		}
	}

	/**
	 * Gets a {@code ScriptSet} with the scripts included, if selected, and excluded if not.
	 *
	 * @return a ScriptSet with the scripts included and excluded
	 * @see ScriptSet#include(Script)
	 * @see ScriptSet#exclude(Script)
	 */
	public ScriptSet getScriptSet() {
		ScriptSet scriptSet = new ScriptSet();
		Iterator<Entry<Script, DefaultMutableTreeNode>> iter = scriptToNodeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Script, DefaultMutableTreeNode> node = iter.next();
			TreePath tp = this.getPath(node.getValue());
			Script script = node.getKey();
			if (scriptTree.isSelectedFully(tp)) {
				scriptSet.include(script);
			} else {
				scriptSet.exclude(script);
			}
		}
		return scriptSet;
	}

	/**
	 * Resets the selection the panel by selecting all scripts.
	 */
	public void reset() {
		scriptTree.checkSubTree(scriptTree.getPathForRow(0), true);
	}

	private TreePath getPath(TreeNode node) {
		List<TreeNode> list = new ArrayList<>();

		// Add all nodes to list
		while (node != null) {
			list.add(node);
			node = node.getParent();
		}
		Collections.reverse(list);

		// Convert array of nodes to TreePath
		return new TreePath(list.toArray());
	}

	private static boolean containsAnyOfTopLevelScript(Set<Script> scriptSet, Script topLevelScript) {
		for (Script script : scriptSet) {
			if (topLevelScript.equals(script.getParent())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the scriptTree
	 */
	public JCheckBoxScriptsTree getScriptTree() {
		return scriptTree;
	}
}