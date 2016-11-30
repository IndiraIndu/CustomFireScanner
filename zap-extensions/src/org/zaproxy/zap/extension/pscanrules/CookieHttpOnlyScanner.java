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
package org.zaproxy.zap.extension.pscanrules;

import java.util.Vector;

import net.htmlparser.jericho.Source;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.pscan.PassiveScanThread;
import org.zaproxy.zap.extension.pscan.PluginPassiveScanner;


public class CookieHttpOnlyScanner extends PluginPassiveScanner {

	/**
	 * Prefix for internationalised messages used by this rule
	 */
	private static final String MESSAGE_PREFIX = "pscanrules.cookiehttponlyscanner.";
	private static final int PLUGIN_ID = 10010;
	
	private PassiveScanThread parent = null;

	@Override
	public void setParent (PassiveScanThread parent) {
		this.parent = parent;
	}

	@Override
	public void scanHttpRequestSend(HttpMessage msg, int id) {
		// Ignore
	}

	@Override
	public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {
		Vector<String> cookies1 = msg.getResponseHeader().getHeaders(HttpHeader.SET_COOKIE);

		if (cookies1 != null) {
			for (String cookie : cookies1) {
				if (cookie.toLowerCase().indexOf("httponly") < 0) {
					this.raiseAlert(msg, id, cookie);
				}
			}
		}

		Vector<String> cookies2 = msg.getResponseHeader().getHeaders(HttpHeader.SET_COOKIE2);
		
		if (cookies2 != null) {
			for (String cookie : cookies2) {
				if (cookie.toLowerCase().indexOf("httponly") < 0) {
					this.raiseAlert(msg, id, cookie);
				}
			}
		}
	}
	
	private void raiseAlert(HttpMessage msg, int id, String cookie) {
	    Alert alert = new Alert(getPluginId(), Alert.RISK_LOW, Alert.CONFIDENCE_MEDIUM, 
		    	getName());
		    	alert.setDetail(
		    		getDescription(), 
		    		msg.getRequestHeader().getURI().toString(),
		    		cookie, "", "",
		    		getSolution(), 
		            getReference(), 
		            cookie, // evidence
		            16,	// CWE Id 16 - Configuration
		            13,	// WASC Id - Info leakage
		            msg);
	
    	parent.raiseAlert(id, alert);

	}

	@Override
	public int getPluginId() {
		return PLUGIN_ID;
	}

	@Override
	public String getName() {
		return Constant.messages.getString(MESSAGE_PREFIX + "name");
	}
	
	private String getDescription() {
		return Constant.messages.getString(MESSAGE_PREFIX + "desc");
	}
	
	private String getSolution() {
		return Constant.messages.getString(MESSAGE_PREFIX + "soln");
	}
	
	private String getReference() {
		return Constant.messages.getString(MESSAGE_PREFIX + "refs");
	}
}
