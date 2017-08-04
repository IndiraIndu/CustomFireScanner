package org.zaproxy.zap.extension.customFire;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.Highlight;

import org.zaproxy.zap.utils.ZapTextArea;

public class ListCellRendererEx implements ListCellRenderer<Highlight>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient ZapTextArea requestField = null;

	public ListCellRendererEx(ZapTextArea reqField) {
		super();
		this.requestField = reqField;
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Highlight> list, Highlight hlt, int index,
			boolean isSelected, boolean cellHasFocus) {

		String str = "";
		try {
			str = requestField.getText(hlt.getStartOffset(), hlt.getEndOffset() - hlt.getStartOffset());
			if (str.length() > 8) {
				// just show first 8 chrs (arbitrary limit;)
				str = str.substring(0, 8) + "..";
			}
		} catch (BadLocationException e) {
			// Ignore
		}

		return new JLabel("[" + hlt.getStartOffset() + "," + hlt.getEndOffset() + "]: " + str);
	}

}
