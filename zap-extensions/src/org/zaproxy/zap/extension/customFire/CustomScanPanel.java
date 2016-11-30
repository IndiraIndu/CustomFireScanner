/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 */
package org.zaproxy.zap.extension.customFire;

import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */
public class CustomScanPanel extends ScanPanel<CustomScan, ScanController<CustomScan>> implements ScanListenner2,
ScannerListener {

	private static final Logger LOGGER = Logger.getLogger(CustomScanPanel.class);

	private static final long serialVersionUID = 1L;
	/**
	 * @deprecated (2.3.0) Replaced by {@link #MESSAGE_CONTAINER_NAME}.
	 */
	@Deprecated
	public static final String PANEL_NAME = "CustomFireScan";

	/**
	 * The name of the custom fire scan HTTP messages container.
	 * 
	 * @see org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer
	 */
	public static final String MESSAGE_CONTAINER_NAME = "CustomFireScanMessageContainer";
	
	private static final CustomFireScanTableModel EMPTY_RESULTS_MODEL = new CustomFireScanTableModel();

	private ExtensionCustomFire extension;
	private JScrollPane jScrollPane;
	private HistoryReferencesTable messagesTable;

	private JButton policyButton = null;
	private JButton scanButton = null;
	private JButton progressButton;
	private JLabel numRequests;

	/**
	 * @param extension
	 */
	public CustomScanPanel(ExtensionCustomFire extension) {
		super("CustomFireScan", new ImageIcon(CustomScanPanel.class.getResource("/org/zaproxy/zap/extension/customFire/resources/cake.png")), extension, null);
		this.extension = extension;
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("customFire.custom.panel.mnemonic"));
	}
	
	

	@Override
	protected int addToolBarElements(JToolBar panelToolbar, Location loc, int x) {
		// Override to add elements into the toolbar
		if (Location.start.equals(loc)) {
			panelToolbar.add(getPolicyManagerButton(), getGBC(x++,0));
		}
		if (Location.beforeProgressBar.equals(loc)) {
			panelToolbar.add(getProgressButton(), getGBC(x++,0));
		}
		if (Location.afterProgressBar.equals(loc)) {
			panelToolbar.add(new JLabel(Constant.messages.getString("customFire.custom.toolbar.requests.label")), getGBC(x++,0));
			panelToolbar.add(getNumRequests(), getGBC(x++,0));
		}
		return x;
	}

	private JButton getPolicyManagerButton() {
		if (policyButton == null) {
			policyButton = new JButton();
			policyButton.setToolTipText(Constant.messages.getString("customFire.custom.menu.analyse.scanPolicy"));
			policyButton.setIcon(DisplayUtils.getScaledIcon(new ImageIcon(CustomScanPanel.class.getResource("/org/zaproxy/zap/extension/customFire/resources/cake.png"))));
			policyButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					extension.showPolicyManagerDialog();
				}
			});
		}
		return policyButton;
	}

	@Override
	public JButton getNewScanButton() {
		if (scanButton == null) {
			scanButton = new JButton(Constant.messages.getString("customFire.custom.toolbar.button.new"));
			scanButton.setIcon(DisplayUtils.getScaledIcon(new ImageIcon(CustomScanPanel.class.getResource("/org/zaproxy/zap/extension/customFire/resources/cake.png"))));
			scanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					extension.showCustomFireDialog(null);
				}
			});
		}
		return scanButton;
	}

	private JButton getProgressButton() {
		if (progressButton == null) {
			progressButton = new JButton("Test");
			progressButton.setEnabled(false); //false->true
			
			progressButton.setIcon(DisplayUtils.getScaledIcon(new ImageIcon(CustomScanPanel.class.getResource("/org/zaproxy/zap/extension/customFire/resources/cake.png"))));
			progressButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					showScanProgressDialog();
					
				}
			});
		}
		return progressButton;
	}

	private JLabel getNumRequests() {
		if (numRequests == null) {
			numRequests = new JLabel();
		}
		return numRequests;
	}

	private void showScanProgressDialog() {
		
		CustomScan scan = this.getSelectedScanner();
		
		if (scan != null) {
			ScanProgressDialog spp = 
					new ScanProgressDialog(View.getSingleton().getMainFrame(), scan.getDisplayName(), this.extension);
			
			spp.setCustomScan(scan);
			spp.setVisible(true);
		}
	}

	@Override
	public void clearFinishedScans() {
		if (extension.getScannerParam().isPromptToClearFinishedScans()) {
			// Prompt to double check
			int res = View.getSingleton().showConfirmDontPromptDialog(
					View.getSingleton().getMainFrame(), Constant.messages.getString("customfire.custom.toolbar.confirm.clear"));
			if (View.getSingleton().isDontPromptLastDialogChosen()) {
				extension.getScannerParam().setPromptToClearFinishedScans(false);
			}
			if (res != JOptionPane.YES_OPTION) {
				return;
			}
		}
		super.clearFinishedScans();
	}


	@Override
	protected JScrollPane getWorkPanel() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getMessagesTable());
		}
		return jScrollPane;
	}

	private void resetMessagesTable() {
		getMessagesTable().setModel(EMPTY_RESULTS_MODEL);
	}

	private HistoryReferencesTable getMessagesTable() {
		if (messagesTable == null) {
			
			messagesTable = new HistoryReferencesTable(EMPTY_RESULTS_MODEL);
			
			messagesTable.setAutoCreateColumnsFromModel(false);
		}
		return messagesTable;
	}

	@Override
	public void switchView(final CustomScan scanner) {
		
		if (View.isInitialised() && !EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						switchView(scanner);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LOGGER.error("Failed to switch view: " + e.getMessage(), e);
			}
			return;
		}

		if (scanner != null) {
			
			getMessagesTable().setModel(scanner.getMessagesTableModel());
			
			this.getNumRequests().setText(Integer.toString(scanner.getTotalRequests()));
			
			this.getProgressButton().setEnabled(true);
			
			/**
			 * Not yet implemented
			 */
			/*if (scanner instanceof AttackScan) { 
				// Its the custom scanner - none of these controls make sense
				
				this.getProgressBar().setEnabled(false);
				this.getProgressButton().setEnabled(false);
				this.getPauseScanButton().setEnabled(false);
				this.getStopScanButton().setEnabled(false);
			}*/
		} else {
			
			resetMessagesTable();
			this.getNumRequests().setText("");
					this.getProgressButton().setEnabled(false);
					}
	}


	@Override
	public void alertFound(Alert alert) {
		ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.NAME);
		if (extAlert != null) {
			extAlert.alertFound(alert, alert.getHistoryRef());
		}
	}


	@Override
	public void hostComplete(int id, String hostAndPort) {
		this.scanFinshed(id, hostAndPort);

	}


	@Override
	public void hostNewScan(int id, String hostAndPort, HostProcess hostThread) {
	}


	@Override
	public void hostProgress(int id, String hostAndPort, String msg, int percentage) {
		this.scanProgress(id, hostAndPort, percentage, 100);
		updateRequestCount();
	}

	@Override
	public void scannerComplete(int id) {
		this.scanFinshed(id, this.getName());
	}

	private void updateRequestCount() {
		CustomScan cscan = this.getSelectedScanner();
		if (cscan != null) {
			this.getNumRequests().setText(Integer.toString(cscan.getTotalRequests()));
		}
	}

	@Override
	public void notifyNewMessage(HttpMessage msg) {
	}

	@Override
	public void reset() {
		super.reset();
		this.resetMessagesTable();
		this.getProgressButton().setEnabled(false);
	}

	@Override
	protected int getNumberOfScansToShow() {
		return extension.getScannerParam().getMaxScansInUI();
	}

	
}
