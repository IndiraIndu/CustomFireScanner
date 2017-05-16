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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ArrayUtils;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
//import org.zaproxy.zap.model.Tech;
//import org.zaproxy.zap.model.TechSet;
//import org.zaproxy.zap.view.JCheckBoxTree;
import org.zaproxy.zap.extension.customFire.Tech;
import org.zaproxy.zap.extension.customFire.TechSet;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 * 
 * A {@code JPanel} that allows to display and select technologies through a check box tree.
 *
 * @see Tech
 * @see TechSet
 * @see JCheckBoxTree
 * @since TODO add version
 */
public class TechnologyTreePanel extends JPanel {

    private static final long serialVersionUID = 5514692105773714202L;

    private final JCheckBoxScriptsTree techTree;
    private final HashMap<Tech, DefaultMutableTreeNode> techToNodeMap;

    public TechnologyTreePanel(String nameRootNode) {
        setLayout(new BorderLayout());

        techToNodeMap = new HashMap<>();
        techTree = new JCheckBoxScriptsTree() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void setExpandedState(TreePath path, boolean state) {
                // Ignore all collapse requests; collapse events will not be fired
                if (state) {
                    super.setExpandedState(path, state);
                }
            }
            
        };
        // Initialise the structure based on all the tech we know about
        TechSet ts = new TechSet(Tech.builtInTech);
        Iterator<Tech> iter = ts.getIncludeTech().iterator();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(nameRootNode);
        Tech tech;
        DefaultMutableTreeNode parent;
        DefaultMutableTreeNode node;
        while (iter.hasNext()) {
            tech = iter.next();
            if (tech.getParent() != null) {
                parent = techToNodeMap.get(tech.getParent());
            } else {
                parent = null;
            }
            if (parent == null) {
                parent = root;
            }
            node = new DefaultMutableTreeNode(tech.getUiName());
            parent.add(node);
            techToNodeMap.put(tech, node);
        }

        techTree.setModel(new DefaultTreeModel(root));
        techTree.expandAll();
        techTree.setCheckBoxEnabled(new TreePath(root), false);
        reset();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(techTree);
        scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Sets the technologies that should be selected, if included, and not if excluded.
     *
     * @param techSet the technologies that will be selected, if included, and not if excluded.
     * @see TechSet#includes(Tech)
     */
    public void setTechSet(TechSet techSet) {
        Set<Tech> includedTech = techSet.getIncludeTech();
        Iterator<Entry<Tech, DefaultMutableTreeNode>> iter = techToNodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Tech, DefaultMutableTreeNode> node = iter.next();
            TreePath tp = this.getPath(node.getValue());
            Tech tech = node.getKey();
            if (ArrayUtils.contains(Tech.builtInTopLevelTech, tech)) {
                techTree.check(tp, containsAnyOfTopLevelTech(includedTech, tech));
            } else {
                techTree.check(tp, techSet.includes(tech));
            }
        }
    }

    /**
     * Gets a {@code TechSet} with the technologies included, if selected, and excluded if not.
     *
     * @return a TechSet with the technologies included and excluded
     * @see TechSet#include(Tech)
     * @see TechSet#exclude(Tech)
     */
    public TechSet getTechSet() {
        TechSet techSet = new TechSet();
        Iterator<Entry<Tech, DefaultMutableTreeNode>> iter = techToNodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Tech, DefaultMutableTreeNode> node = iter.next();
            TreePath tp = this.getPath(node.getValue());
            Tech tech = node.getKey();
            if (techTree.isSelectedFully(tp)) {
                techSet.include(tech);
            } else {
                techSet.exclude(tech);
            }
        }
        return techSet;
    }

    /**
     * Resets the selection the panel by selecting all technologies.
     */
    public void reset() {
        techTree.checkSubTree(techTree.getPathForRow(0), true);
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

    private static boolean containsAnyOfTopLevelTech(Set<Tech> techSet, Tech topLevelTech) {
        for (Tech tech : techSet) {
            if (topLevelTech.equals(tech.getParent())) {
                return true;
            }
        }
        return false;
    }

    /**
     * To save tech settings
     *  void `
     */
	public void saveTechState() {

		//Do Tech ser
		JFileChooser chooser = new JFileChooser(Constant.getPoliciesDir());
		File file = new File(Constant.getZapHome(), "Tech.ser");
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
				
				FileOutputStream fos = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(TechnologyTreePanel.this);
				oos.close();
				fos.close();
				View.getSingleton().showMessageDialog(Constant.messages.getString("customFire.custom.ser.saveTech.success"));

			} catch (IOException e1) {
				View.getSingleton().showWarningDialog(Constant.messages.getString("customFire.custom.ser.saveTech.error"));
				return;
			}
		}
		if (rc == JFileChooser.CANCEL_OPTION) {
			chooser.setVisible(false);
			return;
		}
	
	}
	
	public TechnologyTreePanel addTechTreeListener(final TechnologyTreePanel ttp, final boolean s){
		
		ttp.techTree.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath tp = techTree.getPathForLocation(e.getX(), e.getY());
				techTree.check1(tp, s);
				techTree.repaint();
				
			}
		});
		
		return ttp;
	}

	
	
}