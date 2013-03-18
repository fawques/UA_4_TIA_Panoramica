package javavis.desktop.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Properties;
import javavis.Commons;
import javavis.base.Dialog;
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.functions.OpenWindow;
import javavis.jip2d.functions.Scale;
import javavis.jip2d.gui.FunctionDialog;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

/**
 * @author      Miguel Cazorla
 * @author      Boyan Bonev
 * @version      0.1
 * @date      5-2006
 */
public class DrawFunction extends JInternalFrame implements InternalFrameListener, MouseListener {
	private static final long serialVersionUID = -8828478286421823207L;
	/**
	 * height/width
	 */
	public static final int width=210;
	public static final int height=70;
	public static final int widthIcon=210;
	public static final int heightIcon=30;
	public static final int btWidth=22;
	public static final int btHeight=22;
	
	/**
	 * positions
	 * @uml.property  name="position"
	 */
	private Point position;
	/**
	 * @uml.property  name="start_pos"
	 */
	private Point start_pos;
	/**
	 * @uml.property  name="positionLeft"
	 */
	public Point positionLeft;
	/**
	 * @uml.property  name="positionRight"
	 */
	public Point positionRight;
	/**
	 * @uml.property  name="positionTop"
	 */
	public Point positionTop;
	/**
	 * @uml.property  name="positionDown"
	 */
	public Point positionDown;	
	/**
	 * @uml.property  name="positionLeftIcon"
	 */
	public Point positionLeftIcon;
	/**
	 * @uml.property  name="positionRightIcon"
	 */
	public Point positionRightIcon;
	/**
	 * @uml.property  name="positionTopIcon"
	 */
	public Point positionTopIcon;
	/**
	 * @uml.property  name="positionDownIcon"
	 */
	public Point positionDownIcon;
	
	/**
	 * design
	 * @uml.property  name="contentPane"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JPanel contentPane;
	/**
	 * @uml.property  name="contentPaneButtons"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JPanel contentPaneButtons;
	/**
	 * @uml.property  name="contentPanePreview"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JPanel contentPanePreview;
	/**
	 * @uml.property  name="btLight"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JLabel btLight;
	/**
	 * @uml.property  name="btPreview"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton btPreview;
	/**
	 * @uml.property  name="btProperties"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton btProperties;
	/**
	 * @uml.property  name="btRun"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton btRun;
	
	/**
	 * @uml.property  name="fdialog"
	 * @uml.associationEnd  
	 */
	private FunctionDialog fdialog;
	
	/**
	 * Root Frame
	 * @uml.property  name="window"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="functionsList:javavis.desktop.gui.GuiDesktop"
	 */
	public GuiDesktop window;
	
	/**
	 * Function Object
	 * @uml.property  name="func"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="mynode:javavis.desktop.gui.Function"
	 */
	public Function func=null;
	
	/**
	 * A snapshot of the function's result.
	 * @uml.property  name="snapshot"
	 */
	private Image snapshot=null;
	
	/**
	 * System properties for language
	 * @uml.property  name="prop"
	 */
	private Properties prop;
	
	//methods
	/**
	 * Object constructor
	 * 
	 * @param w - root numFrame
	 * @param n - name
	 * @param pos - initial position
	 */
	public DrawFunction(GuiDesktop w, Function f, Point pos) {
		super();
		window=w;
		prop=w.getProperties();
		
		contentPane=new JPanel(true);
		contentPaneButtons=new JPanel(true);
		contentPanePreview=new JPanel(true);
		
		
		position=pos;
		func=f;
		func.setParentNode(this);

		snapshot = null;
		
		//calculate positions to assign drawtransitions
		calculatePositions();
		
		//DESIGN
		setTitle(f.getName());
		setIconifiable(true);
		setBounds(new Rectangle((int)position.getX(),(int)position.getY(), DrawFunction.width, DrawFunction.height));
		setFrameIcon(Commons.getIcon(f.getName()+".jpg"));
		// This part activates the MouseListener (depending of which OS is running) to the JInternalFrame
		String lcOSName = System.getProperty("os.name").toLowerCase();
		if (lcOSName.startsWith("mac os x")) {
			addMouseListener(this);
		}
		else {
			Component[] comps = this.getComponents();
			BasicInternalFrameTitlePane bif=null;
			for (int i=0; i< comps.length; i++) 
				if (comps[i] instanceof BasicInternalFrameTitlePane) { 
					bif = (BasicInternalFrameTitlePane) comps[i];
					break;
				}
			if (bif!=null) 
				bif.addMouseListener(new MouseListener () {
					public void mouseClicked(MouseEvent e) {
						if (window.getAction()==DesktopAction.SELECTFUNCTION) {
							window.getDesktop().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
							window.setAction(DesktopAction.INSERTFUNCTION);
							functionSelected();
						}
						else if (window.getAction()==DesktopAction.REMOVEFUNCTION) {
							window.getDesktop().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							window.setAction(DesktopAction.NOACTION);
							delete();
						}
					}
					public void mouseEntered(MouseEvent e) {}
					public void mouseExited(MouseEvent e) {}
					public void mouseReleased(MouseEvent e) {
						double x, y;
						//compute movement vector
						x = getX() - start_pos.getX();
						y = getY() - start_pos.getY();
						if(x!=0 || y!=0) {
							updatePosition(x, y);
							window.repaint();
							window.isSaved = false;
						}
					}
					public void mousePressed(MouseEvent e) {
						start_pos = new Point(getX(), getY());
					}
				});
		}
			
		//event
		addInternalFrameListener(this);
		
		//main pane
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(Color.white);
		contentPane.setPreferredSize(new Dimension(DrawFunction.width, DrawFunction.height));
		//sub panes
		contentPaneButtons.setLayout(new GridLayout(3,1));
		contentPaneButtons.setBackground(Color.white);
		
		contentPanePreview.setLayout(new BorderLayout());
		contentPanePreview.setBackground(Color.white);
		
		contentPane.add(contentPaneButtons, BorderLayout.WEST);
		contentPane.add(contentPanePreview, BorderLayout.CENTER);
		
		// Buttons
		btLight=new JLabel();
		btLight.setToolTipText(prop.getProperty("FuncApplied"));
		btLight.setBackground(Color.white);
		btLight.setBounds(new Rectangle(2,2,DrawFunction.btWidth,DrawFunction.btHeight));
		btLight.setIcon(Commons.getIcon("off.png"));
		contentPaneButtons.add(btLight, null);

		btRun = new JButton("Run");
		btRun.putClientProperty("JButton.buttonType", "roundRect");
		btRun.setToolTipText(prop.getProperty("ExecFunc"));
		btRun.setBackground(Color.white);
		btRun.setBounds(new Rectangle(2,2,DrawFunction.btWidth,DrawFunction.btHeight));
		contentPaneButtons.add(btRun,null);
		btRun.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				runPressed_Action();
			}
		});	
		
		btProperties = new JButton("Params");
		btProperties.putClientProperty("JButton.buttonType", "roundRect");
		btProperties.setToolTipText(prop.getProperty("Parameters"));
		btProperties.setBackground(Color.white);
		btProperties.setBounds(new Rectangle(2,2,DrawFunction.btWidth,DrawFunction.btHeight));
		contentPaneButtons.add(btProperties,null);
		btProperties.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				fdialog = new FunctionDialog(window.getDesktop(), func.jipfunction, prop);
				fdialog.setVisible(true);
				setLightsOff();
			}
		});	

		btPreview=new JButton();
		btPreview.setToolTipText(prop.getProperty("ResSnap"));
		btPreview.setBackground(Color.white);
		btPreview.setBounds(new Rectangle(2,2,DrawFunction.btWidth,DrawFunction.btHeight));
		if(snapshot != null)
			btPreview.setIcon(new ImageIcon(snapshot));
		contentPanePreview.add(btPreview, BorderLayout.CENTER);
		btPreview.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					Function2D showFullSize = new OpenWindow();
					if(func.resultingImageToShow != null)
						showFullSize.processImg(func.resultingImageToShow);
					else if(func.resultingSequence !=null)
						showFullSize.processSeq(func.resultingSequence);
				}
				catch (JIPException exc) {
					// TODO: qué hacer con esto
				}
			}
		});	
		getContentPane().add(contentPane);
	}
	
	/**
	 * Performs the necessary actions to apply the function
	 * when the Run button of the function is pressed.
	 * @return errorcode = 0 if function successfully applied.
	 */
	public RunResult runPressed_Action(){
		if(fdialog!=null)
			func.applyToSequence = fdialog.applyToSeq();

		RunResult code = func.run();
		updateSnapshotAndLight();
		if (code != RunResult.OK) {
			String message = prop.getProperty("FuncNotApplied")+"\n";
			switch (code) {
				case IMAGERETNULL: message += prop.getProperty("Desktop6"); break;
				case WRONGPARAM: message += prop.getProperty("Desktop7"); break;
				case NOINPUTIMAGES: message += prop.getProperty("Desktop8");break;
				case OPENUNSUCCESS: message += prop.getProperty("Desktop9"); break;
				default: message += ""; break;
			}
			JOptionPane.showMessageDialog(new JFrame(), message, 
					"Error "+func.getName(), JOptionPane.ERROR_MESSAGE);
		}
		return code;
	}
	
	public void setFunction(Function f) {
		func=f;
		setTitle(func.getName());
	}
	
	/**
	 * set position
	 * @param newPoint   - new position
	 * @uml.property  name="position"
	 */
	public void setPosition(Point newPoint) {		
		position=newPoint;
		calculatePositions();
	}
	
	public void updatePosition(double x, double y) {
		position.setLocation((int)(position.getX()+x), (int)(position.getY()+y));
		calculatePositions();
	}
	
	/**
	 * @return   the position
	 * @uml.property  name="position"
	 */
	public Point getPosition() {
		return position;
	}
	
	/**
	 *  calculate positions to assign drawtransitions
	 *  when it's iconified and deiconified
	 */
	public void calculatePositions(){
		positionLeft=new Point((int)position.getX(), (int)(position.getY()+(DrawFunction.height-15)));
		positionRight=new Point((int)(10+position.getX()+DrawFunction.width), (int)(position.getY()+(DrawFunction.height-15)));
		positionTop=new Point((int)(position.getX()+(DrawFunction.width/2)), (int)(position.getY()));
		positionDown=new Point((int)(position.getX()+(DrawFunction.width/2)), (int)(position.getY()+DrawFunction.height+38));

		if(isIcon()) {
			JDesktopIcon icon = this.getDesktopIcon();
			positionLeftIcon=new Point((int)icon.getX(), (int)(icon.getY()+(DrawFunction.heightIcon/2)));
			positionRightIcon=new Point((int)(icon.getX()+DrawFunction.widthIcon-50), (int)(icon.getY()+(DrawFunction.heightIcon/2)));
			positionTopIcon=new Point((int)(icon.getX()+(DrawFunction.widthIcon/2-25)), (int)(icon.getY()));
			positionDownIcon=new Point((int)(icon.getX()+(DrawFunction.widthIcon/2-25)), (int)(icon.getY()+DrawFunction.heightIcon));
		}
	}
	
	/**
	 *  delete 
	 */	
	public void delete(){	
		if (!window.isFirstLoadImage(this)) {
			window.mainContentPane.removeFunction(this);
			this.dispose();
		}
		else {
			new Dialog(window).information(prop.getProperty("Desktop3"),
					prop.getProperty("Information"));
		}
	}
	
	private void functionSelected() {
		if (!window.isFirstLoadImage(this)) 
			window.mainContentPane.selectFunction(this);
		else {
			window.mainContentPane.selectFunction(null);
			new Dialog(window).information(prop.getProperty("Desktop4"),
					prop.getProperty("Information"));
		}
	}
	
	//JINTERNALFRAME LISTENERS
	/**
	 *  internal numFrame activated
	 * @param arg0 - Internal Frame Event
	 */	
	public void internalFrameActivated(InternalFrameEvent arg0) {
		
	}
	public void internalFrameClosed(InternalFrameEvent arg0) {
		
	}
	public void internalFrameClosing(InternalFrameEvent arg0) {
		
	}	
	public void internalFrameDeactivated(InternalFrameEvent arg0) {
		
	}
	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		this.setLocation(position);
		window.repaint();
	}
	public void internalFrameIconified(InternalFrameEvent arg0)  {
		JDesktopIcon icon = this.getDesktopIcon();
		icon.setLocation(position);
		icon.addMouseListener(new MouseListener () {
			public void mouseClicked(MouseEvent e) {
				if (window.getAction()==DesktopAction.SELECTFUNCTION) {
					window.getDesktop().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					window.setAction(DesktopAction.INSERTFUNCTION);
					functionSelected();
				}
				else if (window.getAction()==DesktopAction.REMOVEFUNCTION) {
					window.getDesktop().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					window.setAction(DesktopAction.NOACTION);
					delete();
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {
				double x, y;
				//compute movement vector
				x = getDesktopIcon().getX() - start_pos.getX();
				y = getDesktopIcon().getY() - start_pos.getY();
				if(x!=0 || y!=0) {
					updatePosition(x, y);
					window.repaint();
				}
			}
			public void mousePressed(MouseEvent e)  {
				start_pos = new Point(getDesktopIcon().getX(), getDesktopIcon().getY());
			}
		});
		window.repaint();

	}
	public void internalFrameOpened(InternalFrameEvent arg0) {}
	
	public void mouseDragged(MouseEvent e) {
		this.setPosition(e.getPoint());
	}
	
	private Image updateSnapshotAndLight(){
		boolean isSequence=false;
		if(func.resultingImage != null)
			isSequence = false;
		else if(func.resultingSequence != null)
			isSequence = true;
		
		if(func.resultingImageToShow != null){
			//First calculate ratio between snapshot and fullsize.
			int widthAux = func.resultingImageToShow.getWidth();
			int heightAux = func.resultingImageToShow.getHeight();
			double ratio = (double)widthAux/heightAux;
			if (widthAux>heightAux) {
				widthAux=2*width/3-3;
				heightAux=(int)(widthAux/ratio)-3;
			}
			else {
				heightAux=height - 5;
				widthAux=(int)(heightAux*ratio)-3;
			}
			//Generate the snapshot
			Function2D fscale = new Scale();
			try {
				fscale.setParamValue("Width",widthAux);
				fscale.setParamValue("Height",heightAux);
				snapshot = JIPToolkit.getAWTImage(fscale.processImg(func.resultingImageToShow));
			}
			catch (JIPException e) {
				// TODO: qué hacer con esto
				System.out.println(e);
			}
		}
		else
			snapshot = null;

		if(snapshot != null){
			btPreview.setIcon(new ImageIcon(snapshot));
			btLight.setIcon(Commons.getIcon("on.png"));
			if(isSequence)
				btLight.setText("SEQ");
			else
				btLight.setText("IMG");
		}
		else
			setLightOff();
		return snapshot;
	}
	
	public void setLightOff() {
		btPreview.setIcon(null);
		btLight.setIcon(Commons.getIcon("off.png"));
		btLight.setText("");
		func.resultingImage=null;
		func.resultingSequence=null;
	}
	
	public void setLightsOff () {
		window.setLightsOff(this);
	}

	public void mouseClicked(MouseEvent arg0) {
		if (window.getAction()==DesktopAction.SELECTFUNCTION) {
			window.getDesktop().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			window.setAction(DesktopAction.INSERTFUNCTION);
			functionSelected();
		}
		else if (window.getAction()==DesktopAction.REMOVEFUNCTION) {
			window.getDesktop().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			window.setAction(DesktopAction.NOACTION);
			delete();
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
		start_pos = new Point(getX(), getY());
	}

	public void mouseReleased(MouseEvent arg0) {
		double x, y;
		//compute movement vector
		x = getX() - start_pos.getX();
		y = getY() - start_pos.getY();
		if(x!=0 || y!=0) {
			updatePosition(x, y);
			window.repaint();
			window.isSaved = false;
		}
	}
}
