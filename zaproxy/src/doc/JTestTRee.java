package doc;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;

public class JTestTRee extends JFrame{

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		JTestTRee jTestTRee = new JTestTRee();
		JPanel jPanel = new JPanel();
		jPanel.setSize(new Dimension(400, 400));
		final JTree jTree = new JTree();
		jTree.setSize(200, 200);
		jTree.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
						}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
						}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
						}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
						}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getButton() == MouseEvent.BUTTON3) {
			       		        
			        System.out.println("Right click");
			}}
		});
		jPanel.add(jTree);
		jTree.setVisible(true);
		jPanel.setVisible(true);
		jTestTRee.setContentPane(jPanel);
		jTestTRee.setSize(new Dimension(400, 400));
		jTestTRee.setVisible(true);
		System.err.println("test");
	}
	

}
