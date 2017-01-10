/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.script.ScriptCollection;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */

public class CustomScanController implements ScanController<CustomScan>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ExtensionCustomFire extension;
	//private static final Logger logger = Logger.getLogger(CustomScanController.class);

	private ExtensionAlert extAlert = null;

	/**
	 * The {@code Lock} for exclusive access of instance variables related to multiple custom fire scans.
	 * 
	 * @see #customScanMap
	 * @see #customScanIdCounter
	 * @see #lastCustomScanAvailable
	 */
	private final Lock customFireScansLock;

	/**
	 * The counter used to give an unique ID to custom fire scans.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code customFireScansLock}.
	 * </p>
	 * 
	 * @see #customFireScansLock
	 * @see #scanURL(String, boolean, boolean)
	 */
	private int customScanIdCounter;

	/**
	 * A map that contains all {@code CustomFireScan}s created (and not yet removed). Used to control (i.e. pause/resume and
	 * stop) the multiple custom fire scans and get its results. The instance variable is never {@code null}. The map key is the ID
	 * of the scan.
	 * <p>
	 * <strong>Note:</strong> All accesses (both write and read) should be done while holding the {@code Lock}
	 * {@code customFireScansLock}.
	 * </p>
	 * 
	 * @see #customFireScansLock
	 * @see #scanURL(String, boolean, boolean)
	 * @see #customScanIdCounter
	 */
	private Map<Integer, CustomScan> customScanMap;

	/**
	 * An ordered list of all of the {@code CustomFireScan}s created (and not yet removed). Used to get provide the 'last'
	 * scan for client using the 'old' API that didn't support concurrent scans. 
	 */
	private List<CustomScan> customScanList;

	public CustomScanController (ExtensionCustomFire extension) {
		this.customFireScansLock = new ReentrantLock();
		this.extension = extension;
		this.customScanMap = new HashMap<>();
		this.customScanList = new ArrayList<CustomScan>();
	}

	public void setExtAlert(ExtensionAlert extAlert) {
		this.extAlert = extAlert;
	}

	@Override
	public int startScan(String name, Target target, User user, Object[] contextSpecificObjects) {
		
		customFireScansLock.lock();
		try {
			int id = this.customScanIdCounter++;
			CustomScan customScan = new CustomScan(name, extension.getScannerParam(), 
					extension.getModel().getOptionsParam().getConnectionParam(), 
					null) {
				@Override
				public void alertFound(Alert alert) {
					if (extAlert!= null) {
						extAlert.alertFound(alert, null);
					}
					super.alertFound(alert);
				}
			};

			// Set session level configs
			Session session = Model.getSingleton().getSession();
			customScan.setExcludeList(session.getExcludeFromScanRegexs());
			CustomScanPolicy policy = null;

			customScan.setId(id);
			customScan.setUser(user);

			boolean techOverridden = false;

			if (contextSpecificObjects != null) {
				for (Object obj : contextSpecificObjects) {
					if (obj instanceof ScannerParam) {
						//logger.debug("Setting custom scanner params");
						customScan.setScannerParam((ScannerParam)obj);
					} else if (obj instanceof CustomScanPolicy) {
						policy = (CustomScanPolicy)obj;
						//logger.debug("Setting custom policy " + policy.getName());
						customScan.setScanPolicy(policy);
					} else if (obj instanceof TechSet) {
						customScan.setTechSet((TechSet) obj);
						techOverridden = true;
					} else if (obj instanceof ScriptCollection) {
						customScan.addScriptCollection((ScriptCollection)obj);
					} else {
						//logger.error("Unexpected contextSpecificObject: " + obj.getClass().getCanonicalName());
					}
				}
			}
			if (policy == null) {
				// use the default
				policy = extension.getPolicyManager().getDefaultScanPolicy();
				//logger.debug("Setting default policy " + policy.getName());
				customScan.setScanPolicy(policy);
			}

			if (!techOverridden && target.getContext() != null) {
				customScan.setTechSet(target.getContext().getTechSet());
			}

			this.customScanMap.put(id, customScan);
			this.customScanList.add(customScan);
			customScan.start(target);

			return id;
		} finally {
			customFireScansLock.unlock();
		}
	}

	public int registerScan(CustomScan cscan) {
		customFireScansLock.lock();
		try {
			int id = this.customScanIdCounter++;
			cscan.setScanId(id);
			this.customScanMap.put(id, cscan);
			this.customScanList.add(cscan);
			return id;
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public CustomScan getScan(int id) {
		return this.customScanMap.get(id);
	}

	@Override
	public CustomScan getLastScan() {
		customFireScansLock.lock();
		try {
			if (customScanList.size() == 0) {
				return null;
			}
			return customScanList.get(customScanList.size()-1);
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public List<CustomScan> getAllScans() {
		List<CustomScan> list = new ArrayList<CustomScan>();
		customFireScansLock.lock();
		try {
			for (CustomScan scan : customScanList) {
				list.add(scan);
			}
			return list;
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public List<CustomScan> getActiveScans() {
		List<CustomScan> list = new ArrayList<CustomScan>();
		customFireScansLock.lock();
		try {
			for (CustomScan scan : customScanList) {
				if (!scan.isStopped()) {
					list.add(scan);
				}
			}
			return list;
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public CustomScan removeScan(int id) {
		customFireScansLock.lock();

		try {
			CustomScan cscan = this.customScanMap.get(id);
			if (! customScanMap.containsKey(id)) {
				//throw new IllegalArgumentException("Unknown id " + id);
				return null;
			}
			cscan.stopScan();
			customScanMap.remove(id);
			customScanList.remove(cscan);
			return cscan;
		} finally {
			customFireScansLock.unlock();
		}
	}

	public int getTotalNumberScans() {
		return customScanMap.size();
	}

	@Override
	public void stopAllScans() {
		customFireScansLock.lock();
		try {
			for (CustomScan scan : customScanMap.values()) {
				scan.stopScan();
			}
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public void pauseAllScans() {
		customFireScansLock.lock();
		try {
			for (CustomScan scan : customScanMap.values()) {
				scan.pauseScan();
			}
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public void resumeAllScans() {
		customFireScansLock.lock();
		try {
			for (CustomScan scan : customScanMap.values()) {
				scan.resumeScan();
			}
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public int removeAllScans() {
		customFireScansLock.lock();
		try {
			int count = 0;
			for (Iterator<CustomScan> it = customScanMap.values().iterator(); it.hasNext();) {
				CustomScan cscan = it.next();
				cscan.stopScan();
				it.remove();
				customScanList.remove(cscan);
				count++;
			}
			return count;
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public int removeFinishedScans() {
		customFireScansLock.lock();
		try {
			int count = 0;
			for (Iterator<CustomScan> it = customScanMap.values().iterator(); it.hasNext();) {
				CustomScan cscan = it.next();
				if (cscan.isStopped()) {
					cscan.stopScan();
					it.remove();
					customScanList.remove(cscan);
					count ++;
				}
			}
			return count;
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public void stopScan(int id) {
		customFireScansLock.lock();
		try {
			if (this.customScanMap.containsKey(id)) {
				this.customScanMap.get(id).stopScan();
			}
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public void pauseScan(int id) {
		customFireScansLock.lock();
		try {
			if (this.customScanMap.containsKey(id)) {
				this.customScanMap.get(id).pauseScan();
			}
		} finally {
			customFireScansLock.unlock();
		}
	}

	@Override
	public void resumeScan(int id) {
		customFireScansLock.lock();
		try {
			if (this.customScanMap.containsKey(id)) {
				this.customScanMap.get(id).resumeScan();
			}
		} finally {
			customFireScansLock.unlock();
		}
	}

	public void reset() {
		this.removeAllScans();
		customFireScansLock.lock();
		try {
			this.customScanIdCounter = 0;
		} finally {
			customFireScansLock.unlock();
		}
	}

}
