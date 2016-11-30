/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team.
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

import java.awt.CardLayout;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.extension.authorization.ExtensionAuthorization;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 * 
 * Adds a set of customize injections for security testing web applications.
 */
public class ExtensionCustomFire extends ExtensionAdaptor implements SessionChangedListener,ScanController<CustomScan> {

	public static final String NAME = "ExtensionCustomFire";

	// The i18n prefix, by default the package name - defined in one place to make it easier
	// to copy and change this example
	protected static final String PREFIX = "customFire";

	private static final String RESOURCE = "/org/zaproxy/zap/extension/customFire/resources";

	private static final ImageIcon ICON = new ImageIcon(
			ExtensionCustomFire.class.getResource( RESOURCE + "/cake.png"));

	private ZapMenuItem menuExample = null;
	private RightClickMsgMenu popupMsgMenuExample = null;
	private AbstractPanel statusPanel = null;
	private CustomFireDialog customFireDialog = null;
	private List<CustomFirePanel> customFirePanels = new ArrayList<CustomFirePanel>();

	private ScannerParam scannerParam = null; 
	private PolicyManager policyManager = null; 
	private final List<AbstractParamPanel> policyPanels = new ArrayList<>(); 
	private OptionsVariantPanel optionsVariantPanel = null;

	private static final Logger logger = Logger.getLogger(ExtensionCustomFire.class);
	private Logger log = Logger.getLogger(this.getClass());

	private CustomScanController cscanController = null;
	private CustomScanPanel customScanPanel = null;
	private PolicyManagerDialog policyManagerDialog = null;

	/**
	 * 
	 */
	public ExtensionCustomFire() {
		super();
		initialize();
	}

	/**
	 * @param name
	 */
	public ExtensionCustomFire(String name) {
		super(name);
		this.setName(NAME); //?
	}

	/** 
	 * The list of extensions this depends on. 
	 */
	private static final List<Class<?>> EXTENSION_DEPENDENCIES;
	static {
		// Prepare a list of Extensions on which this extension depends
		List<Class<?>> dependencies = new ArrayList<>(1);
		dependencies.add(ExtensionUserManagement.class);
		dependencies.add(ExtensionAuthentication.class);
		dependencies.add(ExtensionAuthorization.class);
		EXTENSION_DEPENDENCIES = Collections.unmodifiableList(dependencies);
	}

	/**
	 * This method initializes this extension
	 * 
	 */
	private void initialize() {
		this.setName(NAME);
		policyManager = new PolicyManager(this);
		cscanController = new CustomScanController(this);
		//		attackModeScanner = new AttackModeScanner(this);
	}

	@Override
	public void postInit() {
		policyManager.init();

		if (Control.getSingleton().getMode().equals(Mode.attack)) {
			if (View.isInitialised() && ! this.getScannerParam().isAllowAttackOnStart()) {
				// Disable attack mode for safeties sake (when running with the UI)
				View.getSingleton().getMainFrame().getMainToolbarPanel().setMode(Mode.standard);
			} else {
				// TODO Need to make sure the attackModeScanner starts up

				//this.attackModeScanner.sessionModeChanged(Control.getSingleton().getMode());
			}
		}
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);

		if (getView() != null) {
			// Register as Tools menu item, as long as we're not running as a daemon
			extensionHook.getHookMenu().addToolsMenuItem(getMenuExample());
			// Register as Popup menu item
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMsgMenuExample());

			extensionHook.getHookView().addStatusPanel(getStatusPanel());

			extensionHook.getHookView().addStatusPanel(getCustomScanPanel());//ia
			//extensionHook.getHookView().addOptionPanel(getOptionsVariantPanel());//ia
		}

		extensionHook.addSessionListener(this);

		extensionHook.addOptionsParamSet(getScannerParam());

	}

	private AbstractPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new AbstractPanel();
			statusPanel.setLayout(new CardLayout());
			statusPanel.setName(Constant.messages.getString(PREFIX + ".panel.title"));
			statusPanel.setIcon(ICON);
			JTextPane pane = new JTextPane();
			pane.setEditable(false);
			pane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			pane.setContentType("text/html");
			pane.setText(Constant.messages.getString(PREFIX + ".panel.msg"));
			statusPanel.add(pane);
		}
		return statusPanel;
	}

	protected void showPolicyDialog(PolicyManagerDialog parent) throws ConfigurationException {
		this.showPolicyDialog(parent, null);
	}

	protected void showPolicyDialog(PolicyManagerDialog parent, String name) throws ConfigurationException {
		CustomScanPolicy policy;
		if (name != null) {
			policy = this.getPolicyManager().getPolicy(name);
		} else {
			policy = this.getPolicyManager().getTemplatePolicy();
		}
		PolicyDialog dialog = new PolicyDialog(this, parent, policy);
		dialog.initParam(getModel().getOptionsParam());
		for (AbstractParamPanel panel : policyPanels) {
			dialog.addPolicyPanel(panel);
		}

		int result = dialog.showDialog(true);
		if (result == JOptionPane.OK_OPTION) {
			try {
				getModel().getOptionsParam().getConfig().save();

			} catch (ConfigurationException ce) {
				logger.error(ce.getMessage(), ce);
				getView().showWarningDialog(Constant.messages.getString("scanner.save.warning"));
			}
		}
	}


	private ZapMenuItem getMenuExample() {
		if (menuExample == null) {
			menuExample = new ZapMenuItem(PREFIX + ".topmenu.tools.title");

			menuExample.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent ae) {
					//Display Custom Fire Dialogue box
					showCustomFireDialog(null);
				}
			});
		}
		return menuExample;
	}


	private RightClickMsgMenu getPopupMsgMenuExample() {
		if (popupMsgMenuExample  == null) {
			popupMsgMenuExample = new RightClickMsgMenu(this, 
					Constant.messages.getString(PREFIX + ".popup.title"));
		}
		return popupMsgMenuExample;
	}


	/**
	 * Methods yet to be implemented
	 */
	@Override
	public void sessionChanged(Session session) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// TODO Auto-generated method stub
	}



	/**
	 * This method initializes optionsVariantPanel
	 *
	 * @return org.zaproxy.zap.extension.customFire.OptionsVariantPanel
	 */
	private OptionsVariantPanel getOptionsVariantPanel() {
		if (optionsVariantPanel == null) {
			optionsVariantPanel = new OptionsVariantPanel();
		}
		return optionsVariantPanel;
	}




	/**
	 * This method initializes scannerParam
	 *
	 * @return org.parosproxy.paros.core.scanner.ScannerParam
	 */
	protected ScannerParam getScannerParam() { 
		if (scannerParam == null) {
			scannerParam = new ScannerParam();
		}
		return scannerParam;
	} 


	public void addPolicyPanel(AbstractParamPanel panel) {
		this.policyPanels.add(panel);
	}

	@Override
	public void sessionAboutToChange(Session session) {
		// TODO Auto-generated method stub
		this.cscanController.reset();
		//		this.attackModeScanner.stop();

		if (View.isInitialised()) {
			this.getCustomScanPanel().reset();
			if (customFireDialog != null) {
				customFireDialog.reset();
			}
		}
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString(PREFIX + ".desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_EXTENSIONS_PAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void showCustomFireDialog(SiteNode node) {

		if (customFireDialog == null) {
			// Work out the tabs 
			String[] tabs = CustomFireDialog.STD_TAB_LABELS;

			if (this.customFirePanels.size() > 0) {

				List<String> tabList = new ArrayList<String>();

				for (String str : CustomFireDialog.STD_TAB_LABELS) {
					tabList.add(str);

				}
				for (CustomFirePanel csp : customFirePanels) {
					tabList.add(csp.getLabel());

				}
				tabs = tabList.toArray(new String[tabList.size()]);

			}

			customFireDialog = new CustomFireDialog(this, tabs, this.customFirePanels, 
					View.getSingleton().getMainFrame(), new Dimension(700, 500)); 

		}
		if (customFireDialog.isVisible()) {
			customFireDialog.requestFocus();
			customFireDialog.toFront();
			return;
		}
		if (node != null) {
			customFireDialog.init(new Target(node));
		} else {
			// Keep the previously selected target
			customFireDialog.init(null);
		}
		customFireDialog.setVisible(true);
	}

	public void addCustomFirePanel (CustomFirePanel panel) {
		this.customFirePanels.add(panel);
		customFireDialog = null;	// Force it to be reinitialised
	}

	public void removeCustomFirePanel (CustomFirePanel panel) {
		this.customFirePanels.remove(panel);
		customFireDialog = null;	// Force it to be reinitialised
	}

	public void showPolicyManagerDialog() {
		if (policyManagerDialog == null) {
			policyManagerDialog = new PolicyManagerDialog(View.getSingleton().getMainFrame());
			policyManagerDialog.init(this);
		}
		// The policy names _may_ have changed, eg via the api
		policyManagerDialog.policyNamesChanged();
		policyManagerDialog.setVisible(true);
	}

	public PolicyManager getPolicyManager() {
		if (policyManagerDialog == null) {
			policyManagerDialog = new PolicyManagerDialog(View.getSingleton().getMainFrame());
			policyManagerDialog.init(this);
		}
		// The policy names _may_ have changed, eg via the api
		policyManagerDialog.policyNamesChanged();
		policyManagerDialog.setVisible(true);
		return policyManager;
	}

	private CustomScanPanel getCustomScanPanel() {
		if (customScanPanel == null) {
			customScanPanel = new CustomScanPanel(this);
		}   
		return customScanPanel;
	}

	public void startScanAllInScope() {
		SiteNode snroot = (SiteNode) Model.getSingleton().getSession().getSiteTree().getRoot();
		this.startScan(new Target(snroot, null, true, true));
	}

	/**
	 * Start the scanning process beginning to a specific node 
	 * @param startNode the start node where the scanning should begin to work
	 */
	public int startScan(SiteNode startNode) {
		return this.startScan(new Target(startNode, true));
	}

	public int startScanNode(SiteNode startNode) {
		return this.startScan(new Target(startNode, false));
	}

	public int startScan(Target target) {

		return this.startScan(target, null, null);
	}

	public int startScan(Target target, User user, Object[] contextSpecificObjects) {

		return this.startScan(target.getDisplayName(), target, user, contextSpecificObjects);
	}

	@Override
	public int startScan(String name, Target target, User user, Object[] contextSpecificObjects) {

		if (name == null) {
			name = target.getDisplayName();

		}

		switch (Control.getSingleton().getMode()) {
		case safe:

			throw new InvalidParameterException("Scans are not allowed in Safe mode");
		case protect:

			List<StructuralNode> nodes = target.getStartNodes();

			if (nodes != null) {
				for (StructuralNode node : nodes) {
					if (node instanceof StructuralSiteNode) {
						SiteNode siteNode = ((StructuralSiteNode) node).getSiteNode();
						if (!siteNode.isIncludedInScope()) {
							throw new InvalidParameterException("Scans are not allowed on nodes not in scope Protected mode "
									+ target.getStartNode().getHierarchicNodeName());
						}
					}
				}
			}
			// No problem
			break;
		case standard:
			// No problem

			break;
		case attack:
			// No problem

			break;
		}

		int id = this.cscanController.startScan(name, target, user, contextSpecificObjects);

		if (View.isInitialised()) {

			CustomScan scanner = this.cscanController.getScan(id);

			scanner.addScannerListener(getCustomScanPanel());	//* So the UI gets updated

			this.getCustomScanPanel().scannerStarted(scanner);

			this.getCustomScanPanel().switchView(scanner);

			this.getCustomScanPanel().setTabFocus();

		}

		return id;
	}

	/**
	 * Not used as of now. Will be implemented later.`
	 */
	/*public List<CustomScan> getCustomScans() {
		// TODO Auto-generated method stub
		return cscanController.getActiveScans();
	}*/

	public void scannerComplete() {
	}

	public void hostProgress(String hostAndPort, String msg, int percentage) {
	}

	public void hostComplete(String hostAndPort) {
	}

	public void hostNewScan(String hostAndPort, HostProcess hostThread) {
	}

	@Override
	public List<CustomScan> getAllScans() {
		return cscanController.getAllScans();
	}

	@Override
	public CustomScan getLastScan() {
		return cscanController.getLastScan();
	}

	@Override
	public CustomScan getScan(int id) {
		return cscanController.getScan(id);
	}

	@Override
	public void pauseAllScans() {
		cscanController.pauseAllScans();		
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getCustomScanPanel().updateScannerUI();
		}

	}

	@Override
	public void pauseScan(int id) {
		cscanController.pauseScan(id);
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getCustomScanPanel().updateScannerUI();
		}
	}

	@Override
	public int removeAllScans() {
		return cscanController.removeAllScans();
	}

	@Override
	public int removeFinishedScans() {
		return cscanController.removeFinishedScans();
	}

	@Override
	public CustomScan removeScan(int id) {
		return cscanController.removeScan(id);
	}

	@Override
	public void resumeAllScans() {
		cscanController.removeAllScans();		
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getCustomScanPanel().updateScannerUI();
		}

	}

	@Override
	public void resumeScan(int id) {
		cscanController.resumeScan(id);
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getCustomScanPanel().updateScannerUI();
		}

	}

	@Override
	public void stopAllScans() {
		cscanController.stopAllScans();		
		// No need to update the UI - this will happen automatically via the events
	}

	@Override
	public void stopScan(int id) {
		// Dont need to update the UI - this will happen automatically via the events
		cscanController.stopScan(id);
	}

	@Override
	public List<CustomScan> getActiveScans() {
		return cscanController.getActiveScans();
	}

	public int registerScan(CustomScan scanner) {
		int id = cscanController.registerScan(scanner);
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			scanner.addScannerListener(getCustomScanPanel());	// So the UI get updated
			this.getCustomScanPanel().scannerStarted(scanner);
			this.getCustomScanPanel().switchView(scanner);
			this.getCustomScanPanel().setTabFocus();
		}

		return id;
	}

	@Override
	public boolean supportsLowMemory() {
		return true;
	}

	/**
	 * Part of the core set of features that should be supported by all db types
	 */
	@Override
	public boolean supportsDb(String type) {
		return true;
	}
}