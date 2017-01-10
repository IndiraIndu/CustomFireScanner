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

import java.io.Serializable;
import java.util.List;
import java.util.TreeSet;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */
public class ScriptSet implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final ScriptSet AllScript = new ScriptSet();
	
	private TreeSet<Script> includeScript = new TreeSet<>();
	private TreeSet<Script> excludeScript = new TreeSet<>();

	public ScriptSet () {
	}
	
	public ScriptSet (List<Script> builtInTopLevelScript) {
		this (builtInTopLevelScript, (Script[])null);
	}
	
	public ScriptSet (List<Script> builtInTopLevelScript, Script[] exclude) {
		if (builtInTopLevelScript != null) {
			for (Script script : builtInTopLevelScript) {
				this.include(script);
			}
		}
		if (exclude != null) {
			for (Script script : exclude) {
				this.exclude(script);
			}
		}
	}
	
	public ScriptSet(ScriptSet scriptSet){
		this.includeScript.addAll(scriptSet.includeScript);
		this.excludeScript.addAll(scriptSet.excludeScript);
	}
	
	public void include(Script script) {
		excludeScript.remove(script);
		includeScript.add(script);
	}
	
	public void exclude(Script script) {
		includeScript.remove(script);
		excludeScript.add(script);
	}
	
	public boolean includes(Script script) {
		if (script == null) {
			return false;
		}
		if (excludeScript.contains(script)) {
			return false;
		} else if (includeScript.contains(script)) {
			return true;
		} else {
			return this.includes(script.getParent());
		}
	}
	
	public TreeSet<Script> getIncludeScript() {
		TreeSet<Script> copy = new TreeSet<>();
		copy.addAll(this.includeScript);
		return copy;
	}
	
	public TreeSet<Script> getExcludeScript() {
		TreeSet<Script> copy = new TreeSet<>();
		copy.addAll(this.excludeScript);
		return copy;
	}
	
	public void print() {
		System.out.println("ScriptSet: " + this.hashCode());
		for (Script script : includeScript) {
			System.out.println("\tInclude: " + script);
		}
		for (Script script : excludeScript) {
			System.out.println("\tExclude: " + script);
		}
		
	}
}