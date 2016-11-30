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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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

	private final JCheckBoxTree scriptTree;
	private final HashMap<Script, DefaultMutableTreeNode> scriptToNodeMap;

	ExtensionCustomFire ext = new ExtensionCustomFire();

	List<String> pList = new ArrayList<String>();



	public ScriptTreePanel(String nameRootNode) {
		setLayout(new BorderLayout());

		scriptToNodeMap = new HashMap<>();
		scriptTree = new JCheckBoxTree() {


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


		scriptTree.addMouseListener(new MouseListener() {

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

				CustomScriptsPopup scriptPopupUI = new CustomScriptsPopup(ScriptTreePanel.this,pList.get((scriptTree.getRowForLocation(e.getX(), e.getY())-1)));//Row index starts from 1. Subtract 1!
				if(!scriptPopupUI.isVisible()){
					scriptPopupUI.setVisible(true);
				}
				else{
					scriptPopupUI.toFront();
				}
				} 
			}
		});

		scriptTree.setCheckBoxEnabled(new TreePath(root), false);
		reset();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(scriptTree);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		add(scrollPane, BorderLayout.CENTER);

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
}