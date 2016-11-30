/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.core.scanner.Plugin;
import org.zaproxy.zap.control.CoreFunctionality;
import org.zaproxy.zap.control.ExtensionFactory;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */
public class Script implements Comparable<Script> {

	private static List<AbstractPlugin> loadedPlugins = null;
	private List<Plugin> listAllPlugin = new ArrayList<Plugin>();

	private List<String> pluginNameList = new ArrayList<>();

	public static List<Script> builtInTopLevelScript = new ArrayList<Script>();;
	public static List<Script> builtInScript = new ArrayList<Script>();

	private Script parent = null;
	private String name = null;
	private String keyUiName;
	private DefaultMutableTreeNode defaultMutableTreeNode;
	
	public static List<AbstractPlugin> loadPlugins(){
		if (loadedPlugins == null) {
			loadedPlugins = new ArrayList<>(CoreFunctionality.getBuiltInActiveScanRules());
			loadedPlugins.addAll(ExtensionFactory.getAddOnLoader().getActiveScanRules());
		}
		return loadedPlugins;
	}


	public static List<Script> getPluginNameList(){
		if(loadedPlugins==null){
			loadPlugins();
		}
		for(AbstractPlugin loadedPlugin : loadedPlugins){   
			Script s = new Script(loadedPlugin.getName());
			
			builtInTopLevelScript.add(s);
			builtInScript.add(s);
		}
		return builtInTopLevelScript;
	}
	/**
	 * 
	 * @param name
	 */
	public Script(String name) {
		this(name, null);
	}

	/**
	 * 
	 * @param name
	 * @param keyUiName
	 */
	public Script(String name, String keyUiName) {
		if (name.indexOf(".") > 0) {
			this.name = name.substring(name.lastIndexOf(".") + 1);
			this.parent = new Script(name.substring(0, name.lastIndexOf(".")));

		} else {
			this.name = name;
		}
		this.keyUiName = keyUiName;
	}
	/**
	 * 
	 * @param parent
	 * @param name
	 */
	public Script(Script parent, String name) {
		this(parent, name, null);
	}
	/**
	 * 
	 * @param parent
	 * @param name
	 * @param keyUiName
	 */
	public Script(Script parent, String name, String keyUiName) {
		this.parent = parent;
		this.name = name;
		this.keyUiName = keyUiName;
	}

	@Override
	public String toString() {
		if (parent == null) {
			return this.name;

		} else {
			return parent.toString() + "." + this.name;
		}
	}

	@Override
	public boolean equals(Object tech) {
		if (tech == null) {
			return false;
		}

		return this.toString().equals(tech.toString());
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	public boolean is(Script other) {
		if (other == null) {
			return false;
		}

		for (Script t = this; t != null; t = t.parent) {
			if (other == t) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @return Script `
	 */
	public Script getParent() {
		return parent;
	}

	/**
	 * 
	 * @param parent void `
	 */
	public void setParent(Script parent) {
		this.parent = parent;
	}
	/**
	 * 
	 * @param defaultMutableTreeNode void `
	 */
	public void setParent(DefaultMutableTreeNode defaultMutableTreeNode) {
		this.defaultMutableTreeNode = defaultMutableTreeNode;
	}

/**
 * 
 * @return String `
 */
	public String getName() {
		return name;
	}

	public String getUiName() {
		if (keyUiName == null) {
			return getName();
		}
		return Constant.messages.getString(keyUiName);
	}

	@Override
	public int compareTo(Script o) {
		if (o == null) {
			return -1;
		}

		return this.toString().compareTo(o.toString());
	}
}
