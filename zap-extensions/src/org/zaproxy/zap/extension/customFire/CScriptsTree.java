package org.zaproxy.zap.extension.customFire;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.parosproxy.paros.Constant;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Jan 10, 2017  org.zaproxy.zap.extension.customFire
 */
public class CScriptsTree {

	JTree tree = new JTree();
	
	TreeCellRenderer renderer = new TreeCellRenderer() {
		
		private JCheckBox leafRenderer = new JCheckBox();

		private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();

		Color selectionBorderColor, selectionForeground, selectionBackground,
		textForeground, textBackground;

		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {



			Component returnValue;
			if (leaf) {

				String stringValue = tree.convertValueToText(value, selected,
						expanded, leaf, row, false);
				leafRenderer.setText(stringValue);
				leafRenderer.setSelected(false);

				leafRenderer.setEnabled(tree.isEnabled());

				if (selected) {
					leafRenderer.setForeground(selectionForeground);
					leafRenderer.setBackground(selectionBackground);
				} else {
					leafRenderer.setForeground(textForeground);
					leafRenderer.setBackground(textBackground);
				}

				if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
					Object userObject = ((DefaultMutableTreeNode) value)
							.getUserObject();
					if (userObject instanceof CheckBoxNode) {
						CheckBoxNode node = (CheckBoxNode) userObject;
						leafRenderer.setText(node.getText());
						leafRenderer.setSelected(node.isSelected());
					}
				}
				returnValue = leafRenderer;
			} else {
				returnValue = nonLeafRenderer.getTreeCellRendererComponent(tree,
						value, selected, expanded, leaf, row, hasFocus);
			}
			return returnValue;
		
		}
	};
	
	//CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

	public JTree getTree(String folderPath){

		String str;
		File folder = new File(folderPath);

		/*****/
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles.length < 1){
			//log.error("There is no File inside Folder");
		}
		else{
			//log.debug("List of Files & Folder");
		}

		Vector rVector = new NamedVector("Root");

		for (File file : listOfFiles) {
			if( FilenameUtils.isExtension(file.getName(), "txt")){ //checking for text files in given folder
				String fname = file.getName();
				//log.debug(fname);

				try{
					FileReader fr = new FileReader(file);
					LineNumberReader lnr = new LineNumberReader(fr);
					List<CheckBoxNode> cbnList = new ArrayList<CheckBoxNode>();
					while((str=lnr.readLine())!=null){ //Reading from file
						if( !(str.startsWith("#")) ){
							/*if(lnr.getLineNumber()>0){
								gridBagConstraints.gridy++;
							}*/
							CheckBoxNode cbn = new CheckBoxNode(str, true);
							cbnList.add(cbn);
							//							CustomScriptComponent component = new CustomScriptComponent(cbn, gridBagConstraints);
							//							components.add(component);
						}
					}

					Vector textV = new NamedVector(fname,cbnList.toArray());
					rVector.add(textV);

					lnr.close();
					fr.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		tree = new JTree(rVector);
		tree.setCellRenderer(renderer);
		tree.setCellEditor(new CheckBoxNodeEditor(tree));
		tree.setEditable(true);
		
		tree.addMouseListener(new MouseListener(){


			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
						
				if(SwingUtilities.isRightMouseButton(e)){

					final TreePath tp = tree.getPathForLocation(e.getX(), e.getY()-1);
					Object c = tp.getLastPathComponent();

					if( c instanceof DefaultMutableTreeNode  && !( ((DefaultMutableTreeNode) c).isLeaf() ) ){

						DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
						final DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
						final int nodeIndex = root.getIndex(((DefaultMutableTreeNode) tp.getLastPathComponent()));
						final int totalLeafs = root.getChildAt(nodeIndex).getChildCount();

						//Popup Menu to select and deselect scripts
						final JPopupMenu popup = new JPopupMenu();
						JMenuItem menuItem0 = new JMenuItem(Constant.messages.getString("customFire.custom.csp.popup.menuItem.select"));
						JMenuItem menuItem1 = new JMenuItem(Constant.messages.getString("customFire.custom.csp.popup.menuItem.unselect"));
						popup.add(menuItem0, 0);
						popup.add(menuItem1, 1);
						popup.setLocation(e.getXOnScreen(), e.getYOnScreen());

						tree.add(popup);
						popup.setVisible(true);
						
						menuItem0.addChangeListener(new ChangeListener() {

							@Override
							public void stateChanged(ChangeEvent arg0) {
								popup.setVisible(false);
								for(int j=0 ; j<totalLeafs;j++){
									toggle((DefaultMutableTreeNode) root.getChildAt(nodeIndex).getChildAt(j), true);
								}
								tree.updateUI();

							}
						});

						menuItem1.addChangeListener(new ChangeListener() {

							@Override
							public void stateChanged(ChangeEvent arg0) {
								popup.setVisible(false);
								for(int j=0 ; j<totalLeafs;j++){
									toggle((DefaultMutableTreeNode) root.getChildAt(nodeIndex).getChildAt(j), false);
								}
								tree.updateUI();

							}
						});


					}
				}
			}

			private void toggle(DefaultMutableTreeNode childAt, boolean b) {
				if(childAt.getUserObject() instanceof CheckBoxNode){
					((CheckBoxNode) childAt.getUserObject()).setSelected(b);
				}
			}
		});

		return tree;

	}

}
