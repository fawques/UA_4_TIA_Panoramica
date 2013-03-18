package javavis.desktop.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Properties;
import javavis.desktop.gui.DesktopAction;
import javavis.jip2d.base.FunctionList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * @author     Miguel Cazorla
 * @version     0.1
 * @date     5-2006
 */
public class DesktopPane extends JDesktopPane implements MouseListener {
	private static final long serialVersionUID = 2938728899004652082L;
	/**
	 * root pane
	 * @uml.property  name="windows"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="mainContentPane:javavis.desktop.gui.GuiDesktop"
	 */
	private GuiDesktop windows;
	/**
	 * root numFrame
	 * @uml.property  name="mf"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JFrame mf;
	/**
	 * Function list
	 * @uml.property  name="jfl"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private FunctionList jfl;
	/**
	 * JDialog
	 * @uml.property  name="jd"
	 * @uml.associationEnd  
	 */
	private JDialog jd;
	/**
	 * Combo
	 * @uml.property  name="jcb"
	 * @uml.associationEnd  
	 */
	private JComboBox jcb;
	/**
	 * Success
	 * @uml.property  name="success"
	 */
	private boolean success;
	/**
	 * Function name
	 * @uml.property  name="funcName"
	 */
	private String funcName;
	
	/**
	 * DrawFunction selected
	 * @uml.property  name="functionSelected"
	 */
	private int functionSelected;
	
	/**
	 * Properties for language
	 * @uml.property  name="prop"
	 */
	Properties prop;
	
	/**
	 * object constructor
	 * @param mfi - root numFrame
	 * @param mw - main window
	 */
	public DesktopPane(JFrame mfi, GuiDesktop mw, FunctionList jfli) {
		super();
		windows=mw;
		prop=windows.prop;
		mf=mfi;
		jfl=jfli;
		addMouseListener(this);
	}

	/**
	 * recalculate minimum distances
	 *
	 */
	public Point[] computeArrow(DrawFunction initialFunction, DrawFunction finalFunction) {
		Point[] returnValue=new Point[2];
		double limit=Double.MAX_VALUE;
		double tmpLimit;
		Point p1 = null, p2 = null;
		
		if(initialFunction.isIcon()) {
			initialFunction.calculatePositions();
			if(finalFunction.isIcon()) {
				finalFunction.calculatePositions();
				tmpLimit=distance(initialFunction.positionDownIcon, finalFunction.positionTopIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDownIcon);
					p2=new Point(finalFunction.positionTopIcon);
				}
				tmpLimit=distance(initialFunction.positionDownIcon, finalFunction.positionLeftIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDownIcon);
					p2=new Point(finalFunction.positionLeftIcon);
				}
				tmpLimit=distance(initialFunction.positionDownIcon, finalFunction.positionRightIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDownIcon);
					p2=new Point(finalFunction.positionRightIcon);
				}

				tmpLimit=distance(initialFunction.positionTopIcon, finalFunction.positionDownIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTopIcon);
					p2=new Point(finalFunction.positionDownIcon);
				}
				tmpLimit=distance(initialFunction.positionTopIcon, finalFunction.positionLeftIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTopIcon);
					p2=new Point(finalFunction.positionLeftIcon);
				}
				tmpLimit=distance(initialFunction.positionTopIcon, finalFunction.positionRightIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTopIcon);
					p2=new Point(finalFunction.positionRightIcon);
				}

				tmpLimit=distance(initialFunction.positionRightIcon, finalFunction.positionLeftIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRightIcon);
					p2=new Point(finalFunction.positionLeftIcon);
				}
				tmpLimit=distance(initialFunction.positionRightIcon, finalFunction.positionTopIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRightIcon);
					p2=new Point(finalFunction.positionTopIcon);
				}
				tmpLimit=distance(initialFunction.positionRightIcon, finalFunction.positionDownIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRightIcon);
					p2=new Point(finalFunction.positionDownIcon);
				}

				tmpLimit=distance(initialFunction.positionLeftIcon, finalFunction.positionRightIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeftIcon);
					p2=new Point(finalFunction.positionRightIcon);
				}
				tmpLimit=distance(initialFunction.positionLeftIcon, finalFunction.positionTopIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeftIcon);
					p2=new Point(finalFunction.positionTopIcon);
				}
				tmpLimit=distance(initialFunction.positionLeftIcon, finalFunction.positionDownIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeftIcon);
					p2=new Point(finalFunction.positionDownIcon);
				}
				returnValue[0]=p1;
				returnValue[1]=p2;
			}
			else {
				tmpLimit=distance(initialFunction.positionDownIcon, finalFunction.positionTop);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDownIcon);
					p2=new Point(finalFunction.positionTop);
				}
				tmpLimit=distance(initialFunction.positionDownIcon, finalFunction.positionLeft);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDownIcon);
					p2=new Point(finalFunction.positionLeft);
				}
				tmpLimit=distance(initialFunction.positionDownIcon, finalFunction.positionRight);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDownIcon);
					p2=new Point(finalFunction.positionRight);
				}

				tmpLimit=distance(initialFunction.positionTopIcon, finalFunction.positionDown);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTopIcon);
					p2=new Point(finalFunction.positionDown);
				}
				tmpLimit=distance(initialFunction.positionTopIcon, finalFunction.positionLeft);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTopIcon);
					p2=new Point(finalFunction.positionLeft);
				}
				tmpLimit=distance(initialFunction.positionTopIcon, finalFunction.positionRight);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTopIcon);
					p2=new Point(finalFunction.positionRight);
				}

				tmpLimit=distance(initialFunction.positionRightIcon, finalFunction.positionLeft);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRightIcon);
					p2=new Point(finalFunction.positionLeft);
				}
				tmpLimit=distance(initialFunction.positionRightIcon, finalFunction.positionTop);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRightIcon);
					p2=new Point(finalFunction.positionTop);
				}
				tmpLimit=distance(initialFunction.positionRightIcon, finalFunction.positionDown);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRightIcon);
					p2=new Point(finalFunction.positionDown);
				}

				tmpLimit=distance(initialFunction.positionLeftIcon, finalFunction.positionRight);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeftIcon);
					p2=new Point(finalFunction.positionRight);
				}
				tmpLimit=distance(initialFunction.positionLeftIcon, finalFunction.positionTop);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeftIcon);
					p2=new Point(finalFunction.positionTop);
				}
				tmpLimit=distance(initialFunction.positionLeftIcon, finalFunction.positionDown);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeftIcon);
					p2=new Point(finalFunction.positionDown);
				}
				returnValue[0]=p1;
				returnValue[1]=p2;
			}	
		}
		else {
			if(finalFunction.isIcon()) {
				finalFunction.calculatePositions();
				tmpLimit=distance(initialFunction.positionDown, finalFunction.positionTopIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDown);
					p2=new Point(finalFunction.positionTopIcon);
				}
				tmpLimit=distance(initialFunction.positionDown, finalFunction.positionLeftIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDown);
					p2=new Point(finalFunction.positionLeftIcon);
				}
				tmpLimit=distance(initialFunction.positionDown, finalFunction.positionRightIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDown);
					p2=new Point(finalFunction.positionRightIcon);
				}

				tmpLimit=distance(initialFunction.positionTop, finalFunction.positionDownIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTop);
					p2=new Point(finalFunction.positionDownIcon);
				}
				tmpLimit=distance(initialFunction.positionTop, finalFunction.positionLeftIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTop);
					p2=new Point(finalFunction.positionLeftIcon);
				}
				tmpLimit=distance(initialFunction.positionTop, finalFunction.positionRightIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTop);
					p2=new Point(finalFunction.positionRightIcon);
				}

				tmpLimit=distance(initialFunction.positionRight, finalFunction.positionLeftIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRight);
					p2=new Point(finalFunction.positionLeftIcon);
				}
				tmpLimit=distance(initialFunction.positionRight, finalFunction.positionTopIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRight);
					p2=new Point(finalFunction.positionTopIcon);
				}
				tmpLimit=distance(initialFunction.positionRight, finalFunction.positionDownIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRight);
					p2=new Point(finalFunction.positionDownIcon);
				}

				tmpLimit=distance(initialFunction.positionLeft, finalFunction.positionRightIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeft);
					p2=new Point(finalFunction.positionRightIcon);
				}
				tmpLimit=distance(initialFunction.positionLeft, finalFunction.positionTopIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeft);
					p2=new Point(finalFunction.positionTopIcon);
				}
				tmpLimit=distance(initialFunction.positionLeft, finalFunction.positionDownIcon);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeft);
					p2=new Point(finalFunction.positionDownIcon);
				}
				returnValue[0]=p1;
				returnValue[1]=p2;
			}
			else {
				tmpLimit=distance(initialFunction.positionDown, finalFunction.positionTop);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDown);
					p2=new Point(finalFunction.positionTop);
				}
				tmpLimit=distance(initialFunction.positionDown, finalFunction.positionLeft);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDown);
					p2=new Point(finalFunction.positionLeft);
				}
				tmpLimit=distance(initialFunction.positionDown, finalFunction.positionRight);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionDown);
					p2=new Point(finalFunction.positionRight);
				}

				tmpLimit=distance(initialFunction.positionTop, finalFunction.positionDown);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTop);
					p2=new Point(finalFunction.positionDown);
				}
				tmpLimit=distance(initialFunction.positionTop, finalFunction.positionLeft);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTop);
					p2=new Point(finalFunction.positionLeft);
				}
				tmpLimit=distance(initialFunction.positionTop, finalFunction.positionRight);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionTop);
					p2=new Point(finalFunction.positionRight);
				}

				tmpLimit=distance(initialFunction.positionRight, finalFunction.positionLeft);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRight);
					p2=new Point(finalFunction.positionLeft);
				}
				tmpLimit=distance(initialFunction.positionRight, finalFunction.positionTop);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRight);
					p2=new Point(finalFunction.positionTop);
				}
				tmpLimit=distance(initialFunction.positionRight, finalFunction.positionDown);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionRight);
					p2=new Point(finalFunction.positionDown);
				}

				tmpLimit=distance(initialFunction.positionLeft, finalFunction.positionRight);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeft);
					p2=new Point(finalFunction.positionRight);
				}
				tmpLimit=distance(initialFunction.positionLeft, finalFunction.positionTop);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeft);
					p2=new Point(finalFunction.positionTop);
				}
				tmpLimit=distance(initialFunction.positionLeft, finalFunction.positionDown);
				if(tmpLimit<=limit){
					limit=tmpLimit;
					p1=new Point(initialFunction.positionLeft);
					p2=new Point(finalFunction.positionDown);
				}

				returnValue[0]=p1;
				returnValue[1]=p2;
			}	
		}

		
		return returnValue;
	}

	public double distance(Point n1, Point n2) {
		double returnValue=0.0;
		returnValue=Math.sqrt((n1.getX()-n2.getX())*(n1.getX()-n2.getX())+(n1.getY()-n2.getY())*(n1.getY()-n2.getY()));
		return returnValue;
	}

	
	public void removeFunction(DrawFunction func) {
		int pos;
		pos = windows.functionsList.indexOf(func);
		if(pos!=-1) {
			windows.functionsList.remove(pos);
			repaint();
			windows.isSaved = false;
		}
	}
	
	
	public void selectFunction(DrawFunction func) {
		if (func==null) functionSelected=-1;
		else functionSelected = windows.functionsList.indexOf(func);
	}
	//paint methods
	/**
	 * to repaint
	 * @param g - Graphics
	 */
	public void update(Graphics g ) {}
	
	/**
	 * call to super repaint
	 */
	public void repaint(){
		super.repaint();
	}
	
	/**
	 * paint in desktopPane, all transitions
	 * @param g - Graphics
	 */
	public void paint(Graphics g ) {
		int i, size;
		DrawFunction first, second;
		Point []p;

		super.paint(g);

		size = windows.functionsList.size();
		if(size>0) {
			first = windows.functionsList.get(0);
			for(i=1;i<size;i++) {
				second = windows.functionsList.get(i);
				p = computeArrow(first, second);
				Arrow.drawArrow(g,(int)p[0].getX(),(int)p[0].getY(),(int)p[1].getX(),(int)p[1].getY());
				g.drawLine((int)p[0].getX(),(int)p[0].getY(),(int)p[1].getX(),(int)p[1].getY());
				first = second;
			}
		}

	}
	
	/**
	 * mouse clicked.
	 * @param arg0 - mouse event
	 */
	public void mouseClicked(MouseEvent arg0) {
		JDialog jd;
		switch (windows.getAction()) {
			case ADDLOADIMAGE:
				funcName = "LoadImage";
			case ADDFUNCTION:
				mf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    success = false;
				if(windows.getAction() == DesktopAction.ADDFUNCTION){
					jd=createFuncList();
					jd.setModal(true);
					jd.setVisible(true);
					jd.pack();
				}
		        if (success || windows.getAction()==DesktopAction.ADDLOADIMAGE) {
		        	//draw function panel
	   		   		Function func=new Function(funcName, windows.functionsList);
	   		   		DrawFunction newFunction=new DrawFunction(this.windows, func, arg0.getPoint());
	   		   		add(newFunction);
	   		   		newFunction.show();
	   		   		newFunction.pack();
		        	//append function to the functions list
		        	windows.functionsList.add(newFunction);
		        	windows.isSaved = false;
	   		   	}
		        windows.setAction(DesktopAction.NOACTION);
				break;
			case INSERTFUNCTION:
				mf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				if(functionSelected!=-1) {
					jd=createFuncList();
					jd.setModal(true);
			        jd.setVisible(true);
			        jd.pack();
			        if (success) {
			        	//draw function panel
		   		   		Function func=new Function(funcName, windows.functionsList);
		   		   		DrawFunction newFunction=new DrawFunction(this.windows, func, arg0.getPoint());
		   		   		add(newFunction);
		   		   		newFunction.show();
		   		   		newFunction.pack();
			        	//append function to the functions list
			        	windows.functionsList.add(functionSelected, newFunction);
		   		   		functionSelected = -1;
		   				repaint();
		   				windows.isSaved = false;
		   		   	}
				}
				windows.setAction(DesktopAction.NOACTION);
				break;
			case REMOVEFUNCTION:
				mf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				windows.setAction(DesktopAction.NOACTION);
				break;
				
			default: break;
		}
	}

	/**
	 * Runs all the functions on the Desktop.
	 * @return true if all functions were applied, false otherwise.
	 */
	public boolean runAllFunctionsAction(){
		for(Iterator<DrawFunction> it= windows.functionsList.iterator(); it.hasNext(); ){
			DrawFunction df = it.next();
			if( df.runPressed_Action() != RunResult.OK )
				return false;
		}
		return true;
	}
	
	private JDialog createFuncList () {
		jd = new JDialog();
		JPanel contentPane=new JPanel();
		int width=220, height=90;
		jd.setTitle(prop.getProperty("FunctionList"));
		jd.setResizable(false);
		JButton btYes=new JButton();
		JButton btNo=new JButton();
		btYes.setText(prop.getProperty("Accept"));
		btYes.setBounds(new Rectangle(10,40,100,20));
		btYes.putClientProperty("JButton.buttonType", "roundRect");
		btNo.setText(prop.getProperty("Cancel"));
		btNo.setBounds(new Rectangle(110,40,100,20));
		btNo.putClientProperty("JButton.buttonType", "roundRect");
		
		jcb = new JComboBox(jfl.getFuncArray());
		jcb.setBounds(new Rectangle(7,7,180,20));
		jcb.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);

		btYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				funcName=(String)jcb.getSelectedItem();
				success=true;
				jd.setVisible(false);
			}
		});
		btNo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				success=false;
				jd.setVisible(false);
			}
		});
		int newX=(int)((Toolkit.getDefaultToolkit().getScreenSize().getWidth()-width)/2);
		int newY=(int)((Toolkit.getDefaultToolkit().getScreenSize().getHeight()-height)/2)-100;
		jd.setBounds(new Rectangle(newX,newY,width,height));
		
		contentPane.setLayout(null);
		contentPane.setPreferredSize(new Dimension(width, height));
		
		contentPane.add(btYes);
		contentPane.add(btNo);
		contentPane.add(jcb);
		jd.getContentPane().add(contentPane);
		return jd;
	}
		
	public void mouseEntered(MouseEvent arg0) {
		
	}
	public void mouseExited(MouseEvent arg0) {
		
	}
	public void mousePressed(MouseEvent arg0) {
		
	}
	public void mouseReleased(MouseEvent arg0) {
		
	}
}