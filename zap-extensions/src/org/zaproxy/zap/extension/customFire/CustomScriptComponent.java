/**
 * 
 */
package org.zaproxy.zap.extension.customFire;


import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;

/**
 * 
 * @author <a href="mailto:indu79455@gmail.com">Indira</a>
 *
 * Nov 29, 2016  org.zaproxy.zap.extension.customFire
 */
public class CustomScriptComponent {

	private JCheckBox comp; 
	private GridBagConstraints constraints;

	public CustomScriptComponent(JCheckBox chkbx, GridBagConstraints gridBagConstraints) {
		this.comp = chkbx;
		this.constraints = gridBagConstraints;

	}


	public JCheckBox getComp() {
		return comp;
	}


	public void setComp(JCheckBox comp) {
		this.comp = comp;
	}


	public GridBagConstraints getConstraints() {
		return constraints;
	}


	public void setConstraints(GridBagConstraints constraints) {
		this.constraints = constraints;
	}

}