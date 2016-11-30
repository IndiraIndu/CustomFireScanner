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

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHttpMessageContainer;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 *
 * Adds a right click menu item to all of the main
 * tabs which list messages. 
 * 
 * This class defines the Custom Fire Popup from menu item.
 */
public class RightClickMsgMenu extends PopupMenuItemHttpMessageContainer {

	private static final long serialVersionUID = 1L;
	private ExtensionCustomFire extension = null;

    /**
     * @param ext 
     * @param label
     */
    public RightClickMsgMenu(ExtensionCustomFire ext, String label) {
        super(label);
        this.extension = ext;
    }
	
	@Override
	public void performAction(HttpMessage msg) {
		// Here we'll just show Custom Fire Dialog box.
		extension.showCustomFireDialog(null);
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
		// This is enabled for all tabs which list messages
		// You can examine the invoker is you wish to restrict this to specific tabs
		return true;
	}	
}