
package javavis.desktop.gui;

import java.awt.Rectangle;
import java.beans.PropertyVetoException;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.apache.log4j.Logger;


/**
 * @author Daniel Garcia Nebot
 * @author Elad Rodriguez Alvaro
 * @author Miguel Cazorla
 * @version 0.2
 * @date 5-2005
 */
class DesktopManager extends DefaultDesktopManager {
	private static final long serialVersionUID = -7404052249229071496L;
	
	private static Logger logger = Logger.getLogger(DesktopManager.class);

	/**
	 * get bounds for internalframe when it's icon
	 */
	protected Rectangle getBoundsForIconOf( JInternalFrame f ){

		int width=DrawFunction.widthIcon;
		int height=DrawFunction.heightIcon;
		
		return new Rectangle(f.getX(),f.getY(),width,height);
	}
	
	/**
	 * was icon
	 */
	protected boolean wasIcon(JInternalFrame f ){
		return false;
	}

	/**
	 * iconify
	 */
	public void iconifyFrame(JInternalFrame f){
		super.iconifyFrame(f);
		
		try {
			f.setSelected(true);
		} catch (PropertyVetoException e) {logger.error(e);}
		
		String[] aux;
		int n;
		
		aux= f.getClass().getName().split("[.]");
		n = aux.length;
		
		//check class (DrawState deiconified, DrawTransition deiconified, DrawState iconified, DrawTransition deiconified) to apply:
		//setPosition, recalculate, repaint
		if(aux[n-1].equals("DrawFunction")) {
			DrawFunction auxds = ((DrawFunction)f);
			auxds.setPosition(f.getLocation());
//			auxds.window.mainPane.getDesktopPane().repaint();	
			auxds.window.mainContentPane.repaint();
		}
	}
	
	/**
	 * deiconify
	 */
	public void deiconifyFrame( JInternalFrame f ){
		Rectangle r = new Rectangle(f.getDesktopIcon().getX(),f.getDesktopIcon().getY(),f.getWidth(),f.getHeight());
		super.deiconifyFrame(f);	
		f.setBounds(r);
		
		String[] aux;
		int n;
		
		aux= f.getClass().getName().split("[.]");
		n = aux.length;
		
		//check class (DrawState deiconified, DrawTransition deiconified, DrawState iconified, DrawTransition deiconified) to apply:
		//setPosition, recalculate, repaint
		if(aux[n-1].equals("DrawFunction")) {
			DrawFunction auxds = ((DrawFunction)f);
			auxds.setPosition(f.getLocation());
			auxds.window.mainContentPane.repaint();
		}
	}
	
	/**
	 * dragging numFrame
	 */
	public void dragFrame(JComponent f, int x, int y){
		String[] aux;
		int n;
		
		aux= f.getClass().getName().split("[.]");
		n = aux.length;
		
		//check class (DrawState deiconified, DrawTransition deiconified, DrawState iconified, DrawTransition deiconified) to apply:
		//setPosition, recalculate, repaint
		if(aux[n-1].equals("DrawFunction")) {
			DrawFunction auxds = ((DrawFunction)f);
			auxds.setPosition(f.getLocation());
			auxds.window.mainContentPane.repaint();
			auxds.window.repaint();

		}
		super.dragFrame(f,x,y);
	}
	
	/**
	 * begin to drag numFrame
	 */
	public void beginDraggingFrame(JComponent f){}

	/**
	 * end to drag numFrame
	 */
	public void endDraggingFrame(JComponent f){
		String[] aux= f.getClass().getName().split("[.]");
		int n = aux.length;
		
		//check class (DrawState deiconified, DrawTransition deiconified, DrawState iconified, DrawTransition deiconified) to apply:
		//setPosition, recalculate, repaint
		if(aux[n-1].equals("DrawFunction")) {
			DrawFunction auxds = ((DrawFunction)f);
			auxds.setPosition(f.getLocation());
			auxds.window.mainContentPane.repaint();
			auxds.window.getDesktop().repaint();
		}
	}
}

