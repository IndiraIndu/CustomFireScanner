/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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

import java.awt.Dialog;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.ScannerParamFilter;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 * 
 * Not used as of now. Functionality yet to be implemented.
 */
class ExcludedParameterModifyDialog extends ExcludedParameterAddDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8284630574313238618L;
	private static final String DIALOG_TITLE = Constant.messages.getString("variant.options.excludedparam.dialog.token.modify.title");    
    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("variant.options.excludedparam.dialog.token.modify.button.confirm");
    
    protected ExcludedParameterModifyDialog(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }
    /**
     * 
     * @param token void `
     */
    public void setToken(ScannerParamFilter token) {
        this.token = token;
    }
    
    @Override
    protected boolean validateName(String tokenName) {
        if (token.getParamName().equals(tokenName)) {
            return true;
        }
        
        return super.validateName(tokenName);
    }

    @Override
    protected void init() {
        getNameTextField().setText(token.getParamName());
        getNameTextField().discardAllEdits();
        getUrlTextField().setText(token.getWildcardedUrl());
        getUrlTextField().discardAllEdits();
        getWhereComboField().setSelectedItem(token.getTypeString());
    }
}
