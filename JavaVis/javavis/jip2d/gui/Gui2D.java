package javavis.jip2d.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

import javavis.Commons;
import javavis.Gui;
import javavis.base.Dialog;
import javavis.base.FileType;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.ImageFilter;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.*;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPBmpShort;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.functions.ColorToGray;
import javavis.jip2d.gui.ALLImageFilter;
import javavis.jip2d.gui.Canvas2D;
import javavis.jip2d.gui.InfoPanelBottom;
import javavis.jip2d.gui.InfoPanelGeom;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import com.centerkey.utils.BareBonesBrowserLaunch;

/**
 * This class loads all menus and the ActionListener is
 * implemented.
 */
public class Gui2D extends JPanel implements ActionListener {
	private static final long serialVersionUID = 5359289125586625385L;

	private static Logger logger = Logger.getLogger(Gui2D.class);

	/**
	 * @uml.property  name="canvas"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="gui2d:javavis.jip2d.gui.Canvas2D"
	 */
	Canvas2D canvas;

	/**
	 * Scroll pane of canvas
	 * @uml.property  name="canvasScr"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JScrollPane canvasScr;

	/**
	 * Last directory accessed in load
	 * @uml.property  name="lastDir"
	 */
	String lastDir;

	/**
	 * Type of the open file
	 * @uml.property  name="openedFile"
	 * @uml.associationEnd  
	 */
	FileType openedFile = null;

	/**
	 * Information panel
	 * @uml.property  name="infoGeom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	InfoPanelGeom infoGeom;

	/**
	 * @uml.property  name="infoBottom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	InfoPanelBottom infoBottom;

	/**
	 * Function list
	 * @uml.property  name="funclist"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	FunctionList funclist;

	/**
	 * ArrayList for keeping previous sequences. Allows to implement Undo/Redo
	 * @uml.property  name="undoSeq"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.Sequence"
	 */
	ArrayList<Sequence> undoSeq;

	/**
	 * Index of the undo sequence
	 * @uml.property  name="undoIndex"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.Sequence"
	 */
	int undoIndex;

	/**
	 * Reference to the main mainGui
	 * @uml.property  name="mainGui"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="g2d:javavis.Gui"
	 */
	Gui mainGui;

	/**
	 * Last function which stores the last param values
	 * @uml.property  name="lastFuncApplied"
	 * @uml.associationEnd  
	 */
	Function2D lastFuncApplied = null;

	/**
	 * Flag indicating if file is saved
	 * @uml.property  name="isSaved"
	 */
	boolean isSaved;

	/** Maximum number of undo actions */
	static final int UNDO_LENGTH = 10;
	
	/**
	 * Variables checking if a panel is visible or not
	 * @uml.property  name="isGeometricPaneVis"
	 */
	boolean isGeometricPaneVis;


	/**
	 * Variables checking if a panel is visible or not
	 * @uml.property  name="isBottomPaneVis"
	 */
	boolean isBottomPaneVis;

	/**
	 * @uml.property  name="actions"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="prog:javavis.jip2d.gui.Gui2D$DoAction"
	 */
	DoAction actions;

    /**
	 * @uml.property  name="panelFuncList"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    JScrollPane panelFuncList;

    /**
	 * @uml.property  name="prop"
	 */
    Properties prop;
    
    /**
	 * @uml.property  name="paths"
	 */
    Properties paths;
    
    /**
	 * Additional windows
	 * @uml.property  name="infoBottomFrame"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    JDialog infoBottomFrame;
    /**
	 * @uml.property  name="infoGeomFrame"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    JDialog infoGeomFrame;
    /**
	 * @uml.property  name="canvasImage"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    JDialog canvasImage;

	/** Constructor */
	public Gui2D(Gui frameAux, Properties propi, Properties iPaths, FunctionList funclisti) {
		mainGui=frameAux;
		funclist = funclisti;
		actions = new DoAction(this);
		undoSeq = new ArrayList<Sequence>();
		undoIndex = 0;
		prop = propi;
		paths = iPaths;
		isSaved = true;

		setLayout(new BorderLayout());

	    addKeyListener(canvas);

        lastDir = paths.getProperty("Load2D");

		canvas = new Canvas2D(400, 300,this);
		canvasScr = new JScrollPane(canvas,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		canvasScr.setViewportBorder(new BevelBorder(BevelBorder.RAISED));

		infoGeom = new InfoPanelGeom(canvas, prop);
		infoBottom = new InfoPanelBottom(canvas, prop);

		canvas.assoccInfoGeom(infoGeom);
		canvas.assocInfoBottom(infoBottom);

		canvasImage = new JDialog(mainGui);
		canvasImage.add(canvasScr);
		canvasImage.setSize(400, 300);
		canvasImage.setJMenuBar(mainGui.getMenuBarGui2D());
		canvasImage.setDefaultCloseOperation(WindowConstants. DO_NOTHING_ON_CLOSE);
		
		infoGeomFrame = new JDialog(mainGui);
		infoGeomFrame.add(infoGeom);
		infoGeomFrame.setSize(150, 400);
		infoGeomFrame.setResizable(false);
		infoGeomFrame.setJMenuBar(mainGui.getMenuBarGui2D());

        infoBottomFrame = new JDialog(mainGui);
        infoBottomFrame.add(infoBottom);
        infoBottomFrame.setSize(530, 90);
        infoBottomFrame.setResizable(false);
        infoBottomFrame.setJMenuBar(mainGui.getMenuBarGui2D());
        
        isGeometricPaneVis=false;
        isBottomPaneVis=true;

		JToolBar tb = new JToolBar();
		addButtonsGeneral(tb);
		tb.setRollover(true);
		tb.setFloatable(false);
		setLayout(new BorderLayout());
        add(tb,BorderLayout.NORTH);
	}
	
	private void setVisibleGeom (boolean show) {
        infoGeomFrame.setLocation(600,0);
        infoGeomFrame.setVisible(show);
	}
	
	private void setVisibleInfoBottom (boolean show) {
		infoBottomFrame.setLocation(0,450);
		infoBottomFrame.setVisible(show);
	}
	
	public void setVisAdditionalWindows(boolean show) {
        canvasImage.setLocation(0,150);
        canvasImage.setVisible(show);
        if (isBottomPaneVis && show) 
        	setVisibleInfoBottom(true);
        else 
        	setVisibleInfoBottom(false);
        if (isGeometricPaneVis && show) 
        	setVisibleGeom(true);
        else 
        	setVisibleGeom(false);
	}
	
	public void changeCanvasImage() {
		canvasImage.setVisible(false);
		canvasImage.setSize(canvas.w+40,canvas.h+60);
		canvasImage.setVisible(true);
	}

	protected void addButtonsGeneral (JToolBar t) {
		JButton button = null;

		button = new JButton(Commons.getIcon("new.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("New"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.nnew();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("open.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("Open"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JIPToolkit.askForFile(lastDir, true, prop, mainGui);
				}catch (JIPException ex) {logger.error(ex);}
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("capture.gif"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("Capture"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JIPToolkit.askForCapture(prop, mainGui);
				}catch (JIPException ex) {logger.error(ex);}
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("guardar_ascii.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("Save"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.save();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("guardar_ascii.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("SaveJPG"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.save_as_jpg();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("background.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("BackColor"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.backgroundColor();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("panelcolor.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("PanelColor"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.panelColor();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("segmentcolor.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("SegmentColor"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.segmentColor();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("pointcolor.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("PointColor"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.pointColor();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("polygoncolor.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("PolygonColor"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.polygonColor();
			}
		});
		t.add(button);

		button = new JButton(Commons.getIcon("edgecolor.jpg"));
		button.setBorderPainted(false);
		button.setToolTipText(prop.getProperty("EdgeColor"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actions.edgeColor();
			}
		});
		t.add(button);

	}

	/**
	 * Event handler.
	 * @param e Action produced
	 */
	public void actionPerformed(ActionEvent e) {
		
		/***********************************************************************
		 * * New
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().nnew) {
			actions.nnew();
			return;
		}

		/***********************************************************************
		 * * Open Image
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().open) {
			try {
				JIPToolkit.askForFile(lastDir, true, prop, mainGui);
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}

		/***********************************************************************
		 * * Capture Image from Webcam
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().capture) {
			try {
				JIPToolkit.askForCapture(prop, mainGui);
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}

		/***********************************************************************
		 * * Save
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().save) {
			actions.save();
			return;
		}

		/***********************************************************************
		 * * Save as
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().saveas) {
			actions.save_as();
			return;
		}

		/***********************************************************************
		 * * Save jpg
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().savejpg) {
			actions.save_as_jpg();
			return;
		}

		/***********************************************************************
		 * * Exit
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().exit) {
			actions.exit();
		}

		/***********************************************************************
		 * * Color selection
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().bcolor) {
			actions.backgroundColor();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().pcolor) {
			actions.panelColor();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().scolor) {
			actions.segmentColor();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().cscolor) {
			canvas.currentLineColor(JColorChooser.showDialog(this,
					"Select color", new Color(255, 255, 255)));
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().pointcolor) {
			actions.pointColor();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().cpointcolor) {
			canvas.currentPointColor(JColorChooser.showDialog(this,
					"Select color", new Color(255, 255, 255)));
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().polycolor) {
			actions.polygonColor();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().cpolycolor) {
			canvas.currentPolygonColor(JColorChooser.showDialog(this,
					"Select color", new Color(255, 255, 255)));
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().edgecolor) {
			actions.edgeColor();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().junctioncolor) {
			actions.junctionColor();
			return;
		}

		/***********************************************************************
		 * * Highlight
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().hsegment) {
			canvas.enhanceSegments(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().hpoint) {
			canvas.enhancePoints(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().hpolygon) {
			canvas.enhancePolygon(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().hedge) {
			canvas.enhanceEdges(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().hjunction) {
			canvas.enhanceJunctions(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}

		/***********************************************************************
		 * * View
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().vbitmap) {
			canvas.bitmapVisible(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().vsegment) {
			canvas.segmentsVisible(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().vpoint) {
			canvas.pointsVisible(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().vpolygon) {
			canvas.polygonsVisible(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().vedge) {
			canvas.edgesVisible(((JCheckBoxMenuItem) e.getSource()).getState());
			canvasScr.repaint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().vgeom) {
			setVisibleGeom(((JCheckBoxMenuItem) e.getSource()).getState());
	        isGeometricPaneVis=((JCheckBoxMenuItem) e.getSource()).getState();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().vbottom) {
			setVisibleInfoBottom(((JCheckBoxMenuItem) e.getSource()).getState());
	        isBottomPaneVis=((JCheckBoxMenuItem) e.getSource()).getState();
			return;
		}

		/***********************************************************************
		 * * Segments data
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().datasegment) {
			String cols[] = { prop.getProperty("Index"), prop.getProperty("StartX"),
					prop.getProperty("StartY"), prop.getProperty("EndX"),
					prop.getProperty("EndY"), prop.getProperty("Length") };
			Object[][] data = canvas.getSegmentData();
			JTable table = new JTable(data, cols);
			JDialog dialog = new JDialog(mainGui, prop.getProperty("SegmentData"), true);
			JScrollPane tablaScroll = new JScrollPane(table);
			tablaScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			JPanel pan = new JPanel();
			pan.add(tablaScroll);
			dialog.setContentPane(pan);
			dialog.setSize(new Dimension(500, 400));
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(this);
			dialog.pack();
			dialog.setVisible(true);
			return;
		}

		/***********************************************************************
		 * * Scale
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().iscale) {
			String esc = JOptionPane.showInputDialog(this, prop.getProperty("IntroScaleFactor"),
					prop.getProperty("IntroduceScale"), JOptionPane.QUESTION_MESSAGE);
			float f = -1f;
			try {
				if (esc.length() > 0)
					f = Float.valueOf(esc);
			} catch (Exception err) {logger.error(err);}
			if (f != -1)
				infoGeom.changeScale(f);
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().slsegment) {
			float length = canvas.getLengthLastSegment();
			if (length == -1)
				return;
			String esc = JOptionPane.showInputDialog(this,
					prop.getProperty("IntroLengthLastSegment"),
					prop.getProperty("ScaleLastSegment"), JOptionPane.QUESTION_MESSAGE);
			float f = -1f;
			try {
				if (esc.length() > 0)
					f = Float.valueOf(esc);
			} catch (Exception err) {}
			if (f != -1)
				infoGeom.changeScale(f / length);
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().sselseg) {
			float length = canvas.getLengthSelectedSegment();
			if (length == -1)
				return;
			String esc = JOptionPane.showInputDialog(this,
					prop.getProperty("IntroLengthSelSegment"),
					prop.getProperty("ScaleSelectedSegment"), JOptionPane.QUESTION_MESSAGE);
			float f = -1f;
			try {
				if (esc.length() > 0)
					f = Float.valueOf(esc);
			} catch (Exception err) {logger.error(err);}
			if (f != -1)
				infoGeom.changeScale(f / length);
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().sl2points) {
			float length = canvas.getDistanceLastPoints();
			if (length == -1)
				return;
			String esc = JOptionPane.showInputDialog(this,
					prop.getProperty("IntroDist2Points"),
					prop.getProperty("ScaleLast2Points"), JOptionPane.QUESTION_MESSAGE);
			float f = -1f;
			try {
				if (esc.length() > 0)
					f = Float.valueOf(esc);
			} catch (Exception err) {}
			if (f != -1)
				infoGeom.changeScale(f / length);
			return;
		}

		/***********************************************************************
		 * * Units
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().units) {
			infoGeom.changeUnits(JOptionPane.showInputDialog(this,
					prop.getProperty("IntroUnitMeasure"), prop.getProperty("Units"),
					JOptionPane.QUESTION_MESSAGE));
			return;
		}

		/***********************************************************************
		 * * Zoom
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().zoom) {
			String factorS = JOptionPane.showInputDialog(this,
					prop.getProperty("IntroPerc"), prop.getProperty("Zoom"),
					JOptionPane.QUESTION_MESSAGE);
			int f = -1;
			if (factorS != null && factorS.length() > 0)
				f = Integer.valueOf(factorS);
			if (f > 1)
				try {
					canvas.zoomWindow(f);
				}catch (JIPException ex) {logger.error(ex);}
			changeCanvasImage();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().moreZoom) {
			try {
				canvas.zoomWindow(200);
			}catch (JIPException ex) {logger.error(ex);}
			changeCanvasImage();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().lessZoom) {
			try {
				canvas.zoomWindow(50);
			}catch (JIPException ex) {logger.error(ex);}
			changeCanvasImage();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().osize) {
			if (undoSeq != null) {
				try {
					canvas.setSequence(canvas.getSequence());
					canvas.outView();
					canvas.changeToFrame(canvas.getFrameNum());
				}catch (JIPException ex) {logger.error(ex);}
			} else
				new Dialog(this).information(prop.getProperty("ErrorOSize"), prop.getProperty("Error"));
			changeCanvasImage();
			return;
		}

		/***********************************************************************
		 * * Sequences
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().renameseq) {
			if (canvas.getSequence() == null)
				return;
			String nameS = JOptionPane.showInputDialog(this,
					prop.getProperty("IntroNewName"), prop.getProperty("RenameSequence"),
					JOptionPane.QUESTION_MESSAGE);
			canvas.setSequenceName(nameS);
			try {
				addUndo(canvas.getSequence());
			}catch (JIPException ex) {logger.error(ex);}
			mainGui.setTitle("JavaVis - " + nameS);
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().renameframe) {
			if (canvas.getSequence() == null)
				return;
			String nameS = JOptionPane.showInputDialog(this,
					prop.getProperty("IntroNewName"), prop.getProperty("RenameFrame"),
					JOptionPane.QUESTION_MESSAGE);
			try {
				addUndo(canvas.getSequence());
				canvas.setFrameName(nameS);
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().dupframe) {
			if (canvas.getSequence() == null)
				return;
			try {
				canvas.duplicateFrame();
				addUndo(canvas.getSequence());
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}

		/***********************************************************************
		 * * Add frames
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().addframe) {
			if (canvas.getSequence() == null)
				return;
			JFileChooser openFile = selectFile(lastDir, true);
			if (openFile != null) {
				try {
					File file = openFile.getSelectedFile();
					if (openFile.getFileFilter() instanceof ALLImageFilter) {
						Image img = JIPToolkit.getAWTImage(file.getAbsolutePath());
						if (img == null)
							return;
						JIPImage jipimg = JIPToolkit.getColorImage(img);
						jipimg.setName(file.getName().substring(0,
								file.getName().lastIndexOf(".")));
						canvas.addFrame(jipimg);
						addUndo(canvas.getSequence());
					} else if (openFile.getFileFilter() instanceof ImageFilter) {
						Sequence seq = JIPToolkit.getSeqFromFile(file.getAbsolutePath());
						if (seq == null)
							return;
						canvas.addFrames(seq);
						addUndo(canvas.getSequence());
					}
				}catch (JIPException ex) {logger.error(ex);}
			}
			return;
		}

		/***********************************************************************
		 * Add bands (of the first numFrame from a JIP file with the same rows
		 * and columns)
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().addbands) {
			if (canvas.getSequence() == null)
				return;
			try {
				JIPImage current = canvas.getSequence().getFrame(canvas.getFrameNum());
				if (current.getType() == ImageType.COLOR) {
					new Dialog(this).information(prop.getProperty("MessageNoColor"),
							prop.getProperty("Error"));
					return;
				}
				JFileChooser openFile = selectFile(lastDir, true);
				Sequence seq = null;
				if (openFile != null) {
					File file = openFile.getSelectedFile();
					if (openFile.getFileFilter() instanceof ALLImageFilter) {
						Image img = JIPToolkit.getAWTImage(file.getAbsolutePath());
						if (img == null) {
							logger.error("Error reading the image");
							return;
						}
						JIPImage jipimg = JIPToolkit.getColorImage(img);
						ColorToGray fctg = new ColorToGray();
						fctg.setParamValue("GRAY", "BYTE");
						JIPImage jipimg2;
						try {
							jipimg2 = fctg.processImg(jipimg);
							jipimg2.setName(file.getName().substring(0,
									file.getName().lastIndexOf(".")));
							seq = new Sequence();
							seq.addFrame(jipimg2);
						}
						catch (JIPException ejip) {
							logger.error(ejip);
						}
					} else if (openFile.getFileFilter() instanceof ImageFilter) {
						seq = JIPToolkit.getSeqFromFile(file.getAbsolutePath());
						if (seq == null) {
							logger.error("Error reading the image");
							return;
						}
					}
					if (checkSize(current, seq)) {
						JIPImage img = seq.getFrame(0);
						if (img instanceof JIPImgBitmap) {
							for (int j = 0; j < ((JIPImgBitmap)img).getNumBands(); j++)
								((JIPImgBitmap)current).appendBand(((JIPImgBitmap)img).getAllPixels(j));
							canvas.changeFrame(current);
							addUndo(canvas.getSequence());
						}
						else {
							logger.error("addbands does not valid for geometric data");
							throw new JIPException("Gui2D.addbands: do not valid for geometric data");
						}
					}
					else {
						logger.error("addnbands size or type are not the same");
						throw new JIPException("Gui2D.addbands: size or type are not the same");
					}
				}
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}

		/***********************************************************************
		 * * Delete bands
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().delband) {
			if (canvas.getSequence() == null) {
				new Dialog(this).information(prop.getProperty("EmptySequence"),
					prop.getProperty("Error"));
				return;
			}
			try {
				JIPImage current = canvas.getSequence().getFrame(canvas.getFrameNum());
				if (current instanceof JIPImgGeometric) {
					new Dialog(this).information(prop.getProperty("ErrorGeomData"),
						prop.getProperty("Error"));
					return;
				}
				if (((JIPImgBitmap)current).getNumBands() > 1) {
					if (current.getType() == ImageType.COLOR) {
						new Dialog(this).information(prop.getProperty("MessageNoColor2"),
								prop.getProperty("Error"));
						return;
					}
					if (new Dialog(this).confirm(prop.getProperty("MessageDelBand"),
							prop.getProperty("DeleteBand"))) {
						canvas.removeBand();
						addUndo(canvas.getSequence());
					}
				} else
					new Dialog(this).information(prop.getProperty("MessageNumBand"), prop.getProperty("Error"));
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}

		/***********************************************************************
		 * * Extract band
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().extband) {
			if (canvas.getSequence() == null) {
				new Dialog(this).information(prop.getProperty("NoSeqToOperate"),
						prop.getProperty("Attention"));
				return;
			}
			try {
				JIPImage current = canvas.getSequence().getFrame(canvas.getFrameNum());
				if (current.getType() == ImageType.COLOR) {
					new Dialog(this).information(prop.getProperty("MessageExtBand"),
							prop.getProperty("Attention"));
					return;
				}
				if (current instanceof JIPImgGeometric) {
					new Dialog(this).information(prop.getProperty("ErrorGeomData"),
						prop.getProperty("Error"));
					return;
				}
				JIPImgBitmap newBitmap;
				switch (current.getType()) {
					case BIT:   newBitmap = new JIPBmpBit(current.getWidth(), current.getHeight());
								break;
					case BYTE:  newBitmap = new JIPBmpByte(current.getWidth(), current.getHeight());
								break;
					case SHORT: newBitmap = new JIPBmpShort(current.getWidth(), current.getHeight());
								break;
					case FLOAT: newBitmap = new JIPBmpFloat(current.getWidth(), current.getHeight());
								break;
					default: newBitmap=null;
				}
				newBitmap.setAllPixels(((JIPImgBitmap)current).getAllPixels(canvas.getBandNum()));
				canvas.changeFrame(newBitmap);
				addUndo(canvas.getSequence());
			}catch (JIPException ex) {logger.error(ex);}
			return;
		}

		/***********************************************************************
		 * * Delete frame
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().delframe) {
			if (canvas.getSequence().getNumFrames() > 1) {
				if (new Dialog(this).confirm(prop.getProperty("MessageDelFrame"), prop.getProperty("DeleteFrame"))) {
					try {
						canvas.removeFrame();
						addUndo(canvas.getSequence());
					}catch (JIPException ex) {logger.error(ex);}
				}
			} else
				new Dialog(this).information(prop.getProperty("NoSeqToOperate"), prop.getProperty("Error"));
			return;
		}


		/***********************************************************************
		 * * Segments
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().dellsegment) {
			canvas.deleteLastSegment();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().dellpoint) {
			canvas.deleteLastPoint();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().delpoly) {
			canvas.deleteLastPolygon();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().delselec) {
			canvas.deleteSelection();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().addgeom) {
			canvas.saveGeometry();
			try {
				addUndo (canvas.getSequence());
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().expascii) {
			canvas.exportAscii();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().importascii) {
			try {
				canvas.importAscii();
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().pointframe) {
			try {
				canvas.addEmptyFrame(ImageType.POINT);
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().segmentframe) {
			try {
				canvas.addEmptyFrame(ImageType.SEGMENT);
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().polyframe) {
			try {
				canvas.addEmptyFrame(ImageType.POLY);
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().emptyall) {
			canvas.emptyAll();
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().emptypoints) {
			try {
				canvas.emptyPoints();
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().emptysegments) {
			try {
				canvas.emptySegments();
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}
		if (e.getSource() == mainGui.getMenuBarGui2D().emptypoly) {
			try {
				canvas.emptyPolygons();
			} catch (JIPException e1) {
				logger.error(e1);
			}
			return;
		}

		/***********************************************************************
		 * * Undo
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().undo) {
			actions.undo();
			return;
		}

		/***********************************************************************
		 * * Redo
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().redo) {
			actions.redo();
			return;
		}

		/***********************************************************************
		 * * About and help
		 **********************************************************************/
		if (e.getSource() == mainGui.getMenuBarGui2D().about) {
			Commons.showAbout();
			return;
		}
		if (e.getSource()==mainGui.getMenuBarGui2D().help) {
			BareBonesBrowserLaunch.openURL("http://sourceforge.net/apps/mediawiki/javavis/?source=navbar");
		}

		/***********************************************************************
		 * * Applying functions
		 **********************************************************************/
		if (e.getActionCommand().startsWith("F_") && canvas.getSequence() != null) {
			String auxF = e.getActionCommand().substring(2);
			Function2D f;
			try {
				f = (Function2D)Class.forName("javavis.jip2d.functions."+auxF).newInstance();
				actions.functions(f);
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	/**
	 * This method ask for a file with specify types.
	 * @param s Directory where it starts to find the file.
	 * @return  A JFileChoser  object or null if it is canceled.
	 */
	public JFileChooser selectFile(String s, boolean jpggif) {
		JFileChooser chooseFile = new JFileChooser();
		chooseFile.setDialogType(JFileChooser.SAVE_DIALOG);
		if (s != null) {
			String path = s.substring(0, s.lastIndexOf(mainGui.sep) + 1);
			String nfile = s.substring(s.lastIndexOf(mainGui.sep) + 1,
					s.length());
			chooseFile.setCurrentDirectory(new File(path));
			chooseFile.setSelectedFile(new File(nfile));
		} else {
			chooseFile.setCurrentDirectory(new File("."));
			chooseFile.setSelectedFile(new File(prop.getProperty("NewFile")));
		}
		chooseFile.setAcceptAllFileFilterUsed(false);
		chooseFile.addChoosableFileFilter(new ImageFilter());
		if (jpggif)
			chooseFile.addChoosableFileFilter(new ALLImageFilter());
		if (chooseFile.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			return chooseFile;
		else
			return null;
	}

	/**
	 * Checks if the size and type of the images are of the same type.
	 */
	public boolean checkSize(JIPImage img1, Sequence sec) throws JIPException {
		JIPImage img2 = sec.getFrame(0);
		return (img1.getType() == img2.getType()
				&& img1.getHeight() == img2.getHeight()
				&& img1.getWidth() == img2.getWidth());
	}

	/**
	 * Method to add a sequence to the undo manager
	 * @param seq Sequence to add
	 */
	public void addUndo (Sequence seq) throws JIPException {
		if (undoSeq!=null) {
			int undoSize =undoSeq.size();
			if (undoSize > undoIndex) {
				for (int i=undoSize-1; i>undoIndex-1; i--)
					undoSeq.remove(i);
			}
			if (undoSeq.size() >= UNDO_LENGTH)
				undoSeq.remove(0);
			undoSeq.add(new Sequence(seq));
			undoIndex=undoSeq.size();
		}
	}

	/**
	 * Method to get the last sequence from the undo manager
	 * @return Sequence
	 */
	public Sequence getLastSequence () throws JIPException {
		if (undoSeq==null) return null;
		int aux = undoSeq.size();
		if (aux == 0 || undoIndex <= 1) return null;
		undoIndex--;
		return new Sequence(undoSeq.get(undoIndex-1));
	}

	/**
	 * Method to get the next sequence from the undo manager
	 * @return Sequence
	 */
	public Sequence getNextSequence () {
		int aux = undoSeq.size();
		if (aux == 0 || undoIndex == aux) return null;
		return undoSeq.get(undoIndex++);
	}

	/**
	 * Opens a jpg or gif image
	 * @param fich File to save
	 */
	public void openJPGImage(File fich) throws JIPException {
		openedFile = null;
		canvas.emptyAll();
		lastDir = fich.getAbsolutePath();
		paths.setProperty("Load2D", lastDir.substring(0, lastDir.lastIndexOf(mainGui.sep) + 1));
		canvas.putBitmap(fich.getAbsolutePath());
		String nSeq = lastDir.substring(lastDir.lastIndexOf(mainGui.sep) + 1, lastDir
				.lastIndexOf("."));
		canvas.setSequenceName(nSeq);
		canvas.setFrameName(nSeq);
		openedFile = FileType.JPG;
		addUndo(canvas.getSequence());
		mainGui.setTitle("JavaVis - "+prop.getProperty("Sequence")+
			": [" + canvas.getSequenceName()
			+ "] "+prop.getProperty("File")+": [" + lastDir + "]");
		changeCanvasImage();
	}

	/**
	 * Opens a jip image
	 * @param file File to save
	 */
	public void openJIPImage(File file) throws JIPException {
		openedFile = null;
		canvas.emptyAll();
		lastDir = file.getAbsolutePath();
		paths.setProperty("Load2D", lastDir.substring(0, lastDir.lastIndexOf(mainGui.sep) + 1));
		canvas.loadSequence(file.getAbsolutePath());
		openedFile = FileType.JIP;
		addUndo(canvas.getSequence());
		mainGui.setTitle("JavaVis - "+prop.getProperty("Sequence")+
			": [" + canvas.getSequenceName()
			+ "] "+prop.getProperty("File")+": [" + lastDir + "]");
		changeCanvasImage();
	}

	/**
	 * Adds a JIPImage to the sequence of the canvas.
	 * @param img JIPImage will be added
	 * @throws JIPException 
	 */
	public void addJIPImageToSequence(JIPImage img) throws JIPException{
		canvas.addFrame(img);
		canvas.changeToFrame(canvas.getSequence().getNumFrames()-1);
		addUndo(canvas.getSequence());
		setSaved(false);
	}

	/**
	 * @return  Returns the mainGui.
	 * @uml.property  name="mainGui"
	 */
	public Gui getMainGui() {
		return mainGui;
	}

	/**
	 * @param mainGui  The mainGui to set.
	 * @uml.property  name="mainGui"
	 */
	public void setMainGui(Gui frame) {
		this.mainGui = frame;
	}

	/**
	 * @return  Returns the isSaved.
	 * @uml.property  name="isSaved"
	 */
	public boolean isSaved() {
		return isSaved;
	}

	/**
	 * @param isSaved The isSaved to set.
	 */
	public void setSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}

	/**
	 * This class is created to support actions (menu and toolbar actions)
	 * @author  Miguel Cazorla
	 */
    public class DoAction implements Runnable {
		/**
		 * @uml.property  name="prog"
		 * @uml.associationEnd  
		 */
		Gui2D prog;
		/**
		 * Objects used in thread task
		 * @uml.property  name="funcThread"
		 * @uml.associationEnd  
		 */
		private Function2D funcThread=null;
		private boolean isProcSeq=false;

		public DoAction(Gui2D g) {
			prog = g;
		}

		public void edgeColor() {
			canvas.edgeColor(JColorChooser.showDialog(prog, prop.getProperty("SelectColor"),
					new Color(255, 255, 255)));
			canvasScr.repaint();
		}
		
		public void junctionColor() {
			canvas.junctionColor(JColorChooser.showDialog(prog, prop.getProperty("SelectColor"),
					new Color(255, 255, 255)));
			canvasScr.repaint();
		}

		public void polygonColor() {
			canvas.polygonColor(JColorChooser.showDialog(prog, prop.getProperty("SelectColor"),
					new Color(255, 255, 255)));
			canvasScr.repaint();
		}

		public void pointColor() {
			canvas.pointColor(JColorChooser.showDialog(prog, prop.getProperty("SelectColor"),
					new Color(255, 255, 255)));
			canvasScr.repaint();
		}

		public void segmentColor() {
			canvas.lineColor(JColorChooser.showDialog(prog, prop.getProperty("SelectColor"),
					new Color(255, 255, 255)));
			canvasScr.repaint();
		}

		public void functions(Function2D f) {
			if (canvas.getSequence() != null) {
				Function2D funcAux;
				// Now, we check if the function to apply is the last one.
				// If so, we use the lastFuncApplied, where the last parameters values
				// were stored
				if (lastFuncApplied!= null && f!=null &&
						lastFuncApplied.getName().equals(f.getName())) {
					funcAux=lastFuncApplied;
				}
				else funcAux=f;
				FunctionDialog fdialog = new FunctionDialog(mainGui, funcAux, prop);
				fdialog.setVisible(true);
				if (fdialog.isConfirmed()) {
					if (fdialog.isAssignedOK()) {
						fdialog.setVisible(false);
						repaint();
						isProcSeq=fdialog.applyToSeq();
						if (isProcSeq) {
							mainGui.setTitle("JavaVis - "+prop.getProperty("Applying")
									+ " " + funcAux.getName()
									+ " "+prop.getProperty("to")+" " + canvas.getSequenceName());
						} else {
							mainGui.setTitle("JavaVis - "+prop.getProperty("Applying")+ " "
									+ funcAux.getName() + " "+prop.getProperty("to")+
									" numFrame " + canvas.getFrameNum()
									+ " "+prop.getProperty("of")+
									" " + canvas.getSequenceName());
						}
						infoBottom.setBar(0);
						Thread th = new Thread(this);
						funcThread=funcAux;
						MyProgressBar mpb = new MyProgressBar(funcThread, infoBottom, th);
						Thread th2 = new Thread(mpb);
						th2.start();
						th.start();
					} else {
						new Dialog(prog).information(prop.getProperty("ParamsReq"), prop.getProperty("Error"));
						return;
					}
					lastFuncApplied=funcAux;
				}
			}
		}

		public void run () {
			JIPImage auxImg=null;
			Sequence auxSeq=null;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			infoBottom.setBar(0);

			try {
				long t1=System.currentTimeMillis();
				if (isProcSeq)
					auxSeq = funcThread.processSeq(canvas.getSequence());
				else
					auxImg = funcThread.processImg(canvas.getSequence().getFrame(canvas.getFrameNum()));
				long t2=System.currentTimeMillis();
				infoBottom.setBar(100);
				if (isProcSeq)
					canvas.setSequence(auxSeq);
				else {
					auxImg.setName(canvas.getSequence().getFrame(canvas.getFrameNum()).getName());
					canvas.getSequence().setFrame(auxImg, canvas.getFrameNum());
					canvas.reassignedSeq();
				}
				if (funcThread.isInfo())
					new Dialog(prog).information(funcThread.getInfo(),
							prop.getProperty("Information"));
				canvas.outView();
				canvas.setBackGround(null);
				infoBottom.assocSequence(canvas.getSequence());
				canvas.changeToFrame(canvas.getFrameNum());
				infoBottom.setFrame(canvas.getFrameNum());
				addUndo(canvas.getSequence());
				isSaved=false;
				mainGui.setTitle("JavaVis - " + canvas.getSequenceName()+"; "+funcThread.getName()+" applied in "+(t2-t1)+" milliseconds");
			}
			catch (JIPException e) {
				new Dialog(prog).information(e.getMessage(), prop.getProperty("Error"));
				logger.error(e);
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			changeCanvasImage();
			repaint();
		}

		public void panelColor() {
			canvas.backgroundColorScroll(JColorChooser.showDialog(prog,
					prop.getProperty("SelectColor"), new Color(255, 255, 255)));
			canvasScr.repaint();
		}

		public void backgroundColor() {
			canvas.backgroundColor(JColorChooser.showDialog(prog, prop.getProperty("SelectColor"),
					new Color(255, 255, 255)));
			canvasScr.repaint();
		}

		public void save_as_jpg() {
			if (canvas.getSequence() == null) {
				new Dialog(prog).information(prop.getProperty("MessageSave"),
						prop.getProperty("Attention"));
				return;
			}
			String lastTitle = mainGui.getTitle();
			mainGui.setTitle("JavaVis - [" + lastDir + "] "+
					prop.getProperty("saving")+"...");
			JFileChooser chooseFle = new JFileChooser();
			chooseFle.setDialogType(JFileChooser.SAVE_DIALOG);
			if (lastDir != null) {
				String path = lastDir.substring(0, lastDir
						.lastIndexOf(mainGui.sep) + 1);
				String nfile = lastDir.substring(lastDir
						.lastIndexOf(mainGui.sep) + 1, lastDir.length());
				chooseFle.setCurrentDirectory(new File(path));
				chooseFle.setSelectedFile(new File(nfile));
			} else {
				chooseFle.setCurrentDirectory(new File("."));
				chooseFle.setSelectedFile(new File(prop.getProperty("New")));
			}
			chooseFle.addChoosableFileFilter(new ALLImageFilter());
			if (chooseFle.showSaveDialog(prog) == JFileChooser.APPROVE_OPTION) {
				File file = chooseFle.getSelectedFile();
				lastDir = file.getAbsolutePath();
				paths.setProperty("Load2D", lastDir.substring(0, lastDir.lastIndexOf(mainGui.sep) + 1));
				String path = lastDir.substring(0, lastDir
						.lastIndexOf(mainGui.sep) + 1);
				int aux = lastDir.lastIndexOf(".");
				if (aux < 0)
					aux = lastDir.length();
				String nfile = lastDir.substring(lastDir
						.lastIndexOf(mainGui.sep) + 1, aux);
				if (!nfile.toLowerCase().endsWith(".jpg"))
					nfile += ".jpg";
				JIPToolkit.saveImgIntoFileJpg(canvas.getSequence(), canvas
						.getFrameNum(), path, nfile);
				mainGui.setTitle("JavaVis - "+prop.getProperty("Sequence")+
						": [" + canvas.getSequenceName()
						+ "] File: [" + lastDir + "]");
				new Dialog(prog).information(prop.getProperty("JPGAtten"),
						prop.getProperty("Attention"));
			} else {
				mainGui.setTitle(lastTitle);
				return;
			}

		}

		public void save_as() {
			FileType type = null;
			if (canvas.getSequence() == null) {
				new Dialog(prog).information(prop.getProperty("noseq"),
						prop.getProperty("Attention"));
				return;
			}
			String lastTitle = mainGui.getTitle();
			mainGui.setTitle("JavaVis - [" + lastDir + "] "+
					prop.getProperty("saving")+"...");
			JFileChooser openFile = selectFile(lastDir, false);
			if (openFile == null) {
				mainGui.setTitle(lastTitle);
				return;
			} else {
				File fich = openFile.getSelectedFile();
				lastDir = fich.getAbsolutePath();
				paths.setProperty("Load2D", lastDir.substring(0, lastDir.lastIndexOf(mainGui.sep) + 1));
				if (openFile.getFileFilter() instanceof ImageFilter) {
					if (!lastDir.toLowerCase().endsWith(".jip"))
						lastDir += ".jip";
					type = FileType.JIP;
				} else
					type = null;
			}
			String path = lastDir.substring(0, lastDir
					.lastIndexOf(mainGui.sep) + 1);
			String nfile = lastDir.substring(lastDir
					.lastIndexOf(mainGui.sep) + 1, lastDir.length());
			openedFile = null;
			String ext = "";
			if (type == FileType.JIP || type == null) {
				JIPToolkit.saveSeqIntoFile(canvas.getSequence(), path, nfile);
				openedFile = FileType.JIP;
				ext = "JIP";
			} else
				return;
			mainGui.setTitle("JavaVis - "+prop.getProperty("Sequence")+": [" +
					canvas.getSequenceName()
					+ "] "+prop.getProperty("File")+": [" + lastDir + "]");
			new Dialog(prog).information(prop.getProperty("filesaved") +
					prop.getProperty("format")+" "+ext,
					prop.getProperty("Attention"));
			isSaved=true;
		}

		public void save() {
			FileType type = null;
			if (canvas.getSequence() == null) {
				new Dialog(prog).information(prop.getProperty("noseq"),
						prop.getProperty("Attention"));
				return;
			}
			String lastTitle = mainGui.getTitle();
			mainGui.setTitle("JavaVis - [" + lastDir + "] "+
					prop.getProperty("saving")+"...");
			if (openedFile == null || openedFile == FileType.JPG) {
				JFileChooser openFile = selectFile(lastDir, false);
				if (openFile == null) {
					mainGui.setTitle(lastTitle);
					return;
				} else {
					File fich = openFile.getSelectedFile();
					lastDir = fich.getAbsolutePath();
					paths.setProperty("Load2D", lastDir.substring(0, lastDir.lastIndexOf(mainGui.sep) + 1));
					if (openFile.getFileFilter() instanceof ImageFilter) {
						type = FileType.JIP;
						if (!lastDir.toLowerCase().endsWith(".jip"))
							lastDir += ".jip";
					} else
						type = null;
				}
			}
			//If the file was opened, saves its type.
			else
				type = openedFile;
			String path = lastDir.substring(0, lastDir
					.lastIndexOf(mainGui.sep) + 1);
			String nfile = lastDir.substring(lastDir
					.lastIndexOf(mainGui.sep) + 1, lastDir.length());
			openedFile = null;
			String ext = "";
			if (type == FileType.JIP || type == null) {
				JIPToolkit.saveSeqIntoFile(canvas.getSequence(), path, nfile);
				openedFile = FileType.JIP;
				ext = "JIP";
			} else
				return;
			mainGui.setTitle("JavaVis - "+prop.getProperty("Sequence")+": [" +
					canvas.getSequenceName()
					+ "] "+prop.getProperty("File")+": [" + lastDir + "]");
			new Dialog(prog).information(prop.getProperty("filesaved") +
					prop.getProperty("format")+" "+ext,
					prop.getProperty("Attention"));
			isSaved=true;
		}

		public void nnew() {
			String lenX, lenY;
			int w = -1, h = -1;
			lenX = JOptionPane.showInputDialog(prog, prop.getProperty("introwidth"));
			lenY = JOptionPane.showInputDialog(prog, prop.getProperty("introheight"));
			try {
				if (lenX.length() > 0)
					w = Integer.valueOf(lenX);
				if (lenY.length() > 0)
					h = Integer.valueOf(lenY);
			} catch (Exception err) {logger.error(err);}
			if (w > 0 && h > 0) {
				canvas.emptyAll();
				try {
					canvas.setSequence(null);
				} catch (JIPException e) {logger.error(e);}
				if (w > 0 && h > 0)
					canvas.newBitmap(w, h, null);
				undoSeq = null;
				mainGui.setTitle("JavaVis - "+prop.getProperty("nocurrentseq"));
				lastDir = null;
				openedFile = null;
				isSaved=true;
			}
			changeCanvasImage();
		}
		
		public void exit() {
			JIPToolkit.exit(prop, mainGui);
		}
		
		public void undo() {
			try {
				Sequence auxSeq = getLastSequence();
				if (auxSeq != null) {
					canvas.setSequence(auxSeq);
					canvas.setBackGround(null);
					canvas.outView();
					canvas.changeToFrame(canvas.getFrameNum());
					infoBottom.assocSequence(canvas.getSequence());
					changeCanvasImage();
					repaint();
				} else
					new Dialog(getParent()).information(prop.getProperty("MessageUndo"), prop.getProperty("Error"));
			}catch (JIPException ex) {logger.error(ex);}
		}
		
		public void redo() {
			Sequence auxSeq = getNextSequence();
			if (auxSeq != null) {
				try {
					canvas.setSequence(auxSeq);
					canvas.setBackGround(null);
					canvas.outView();
					canvas.changeToFrame(canvas.getFrameNum());
					infoBottom.assocSequence(canvas.getSequence());
					changeCanvasImage();
					repaint();
				}catch (JIPException ex) {logger.error(ex);}
			} else
				new Dialog(getParent()).information(prop.getProperty("MessageRedo"), prop.getProperty("Error"));
		}
	}


    /**
	 * Class to manage a thread to control the progress bar
	 * @author  Miguel Cazorla
	 */
    class MyProgressBar implements Runnable {
		/**
		 * @uml.property  name="func"
		 * @uml.associationEnd  
		 */
		private Function2D func;
		/**
		 * @uml.property  name="ipb"
		 * @uml.associationEnd  
		 */
		private InfoPanelBottom ipb;
		private Thread th;

    	public MyProgressBar (Function2D f, InfoPanelBottom ipbAux,
    			Thread thAux) {
    		ipb = ipbAux;
    		func = f;
    		th = thAux;
    	}

    	public void run () {
    		try {
    			while (th.isAlive()) {
    				ipb.setBar(func.getProgress());
    				Thread.sleep(1000);
    			}
    		} catch (InterruptedException e) {logger.error(e);}
    	}
    }
}
