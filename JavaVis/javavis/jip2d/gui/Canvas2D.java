package javavis.jip2d.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javavis.base.Dialog;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.*;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPGeomEdges;
import javavis.jip2d.base.geometrics.JIPGeomJunctions;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPGeomPoly;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Junction;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.base.geometrics.Segment;
import javavis.jip2d.gui.InfoPanelGeom.GeomAction;

import org.apache.log4j.Logger;

import javax.swing.*;


/**
 * Class managing all the visual data
 */
public class Canvas2D extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener{
	private static final long serialVersionUID = 2538428251952768226L;
	
	private static Logger logger = Logger.getLogger(Canvas2D.class);
	
	private static int DIST_THRESH=10;

	/**
	 * Array containing the segments drawn in the canvas, but not converted in an JIP image yet
	 * @uml.property  name="segments"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.geometrics.Segment"
	 */
	ArrayList<Segment> segments;

	/**
	 * Array containing the points drawn in the canvas, but not converted in an JIP image yet
	 * @uml.property  name="points"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.geometrics.Point2D"
	 */
	ArrayList<Point2D> points;

	/**
	 * Array containing the polygons drawn in the canvas, but not converted in an JIP image yet
	 * @uml.property  name="polygons"
	 */
	ArrayList<Polygon2D> polygons;

	/**
	 * Vector which has the historic of the scale factors
	 * @uml.property  name="factoresEscalas"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	ArrayList<Integer> scaleFactors;

	/**
	 * Vector which has the elements which is been wacthed
	 * @uml.property  name="visualizing"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	ArrayList<Integer> visualizing;

	/**
	 * Active sequence in the program
	 * @uml.property  name="sequence"
	 * @uml.associationEnd  
	 */
	Sequence sequence;
	
	/**
	 * Width
	 * @uml.property  name="w"
	 */
	int w = 400;
	
	/**
	 * Height
	 * @uml.property  name="h"
	 */
	int h = 300;
	
	/**
	 * Scale factor
	 * @uml.property  name="scaleFactor"
	 */
	int scaleFactor = 100;
	
	/**
	 * Flag which shows if the geometry is activate or not
	 * @uml.property  name="estadoGeo"
	 */
	boolean geoState = true;
	
	/**
	 * Original width
	 * @uml.property  name="worg"
	 */
	int worg;
	
	/**
	 * Original height
	 * @uml.property  name="horg"
	 */
	int horg;
	
	/**
	 * Flag which shows if the mouse is been dragged
	 * @uml.property  name="dragged"
	 */
	boolean dragged;
	
	/**
	 * Flag which shows if we have to emphasize the segments
	 * @uml.property  name="emphasizeSegments"
	 */
	boolean emphasizeSegments;
	
	/**
	 * Flag which shows if we have to emphasize the points
	 * @uml.property  name="emphasizePoints"
	 */
	boolean emphasizePoints;
	
	/**
	 * Flag which shows if we have to emphasize the polygons
	 * @uml.property  name="emphasizePolygons"
	 */
	boolean emphasizePolygons;
	
	/**
	 * Flag which shows if we have to emphasize the edges
	 * @uml.property  name="emphasizeEdges"
	 */
	boolean emphasizeEdges;
	
	/**
	 * Flag which shows if we have to emphasize the junctions
	 * @uml.property  name="emphasizeJuntions"
	 */
	boolean emphasizeJuntions;
	
	/**
	 * Flag which shows if the background Bitmap has been shown
	 * @uml.property  name="showBitmap"
	 */	
	boolean showBitmap;
	
	/**
	 * Flag which shows if the segments have been shown
	 * @uml.property  name="showSegments"
	 */
	boolean showSegments;
	
	/**
	 * Flag which shows if the points have been shown
	 * @uml.property  name="showPoints"
	 */
	boolean showPoints;
	
	/**
	 * Flag which shows if the polygons have been shown
	 * @uml.property  name="showPolygons"
	 */
	boolean showPolygons;
	
	/**
	 * Flag which shows if the edges have been shown
	 * @uml.property  name="showEdges"
	 */
	boolean showEdges;
	
	/**
	 * Flag which shows if the junctions have been shown
	 * @uml.property  name="showJunctions"
	 */
	boolean showJuntions;
	
	/**
	 * initial position
	 * @uml.property  name="ini"
	 * @uml.associationEnd  
	 */
	Point2D ini=null;
	
	/**
	 * final position
	 * @uml.property  name="fin"
	 * @uml.associationEnd  
	 */
	Point2D fin=null;
	
	/**
	 * Information panel
	 * @uml.property  name="infoGeom"
	 * @uml.associationEnd  inverse="canvas:javavis.jip2d.gui.InfoPanelGeom"
	 */
	InfoPanelGeom infoGeom;
	
	/**
	 * @uml.property  name="infoBottom"
	 * @uml.associationEnd  inverse="canvas:javavis.jip2d.gui.InfoPanelBottom"
	 */
	InfoPanelBottom infoBottom;

	
	/**
	 * Maximum number of segments
	 * @uml.property  name="maxSeg"
	 */
	int maxSeg = 1000;
	
	/**
	 * Component
	 * @uml.property  name="area"
	 */
	Component area;

	/**
	 * Background image
	 * @uml.property  name="backGround"
	 * @uml.associationEnd  
	 */
	ImageIcon backGround;

	/**
	 * Background color
	 * @uml.property  name="backgroundColor"
	 */
	Color backgroundColor;
	
	/**
	 * Background color of the scroll panel
	 * @uml.property  name="backgroundColorScroll"
	 */
	Color backgroundColorScroll;
	
	/**
	 * Lines color
	 * @uml.property  name="colorLine"
	 */
	Color colorLine;
	
	/**
	 * Current color line
	 * @uml.property  name="currentColorLine"
	 */	
	Color currentColorLine;
	
	/**
	 * Color of the points
	 * @uml.property  name="colorPoint"
	 */
	Color colorPoint;
	
	/**
	 * Color of the actual point
	 * @uml.property  name="colorCurrentPoint"
	 */
	Color colorCurrentPoint;
	
	/**
	 * Color of the polygons
	 * @uml.property  name="colorPolygon"
	 */
	Color colorPolygon;
	
	/**
	 * Color of the actual polygon
	 * @uml.property  name="colorCurrentPolygon"
	 */
	Color colorCurrentPolygon;
	
	/**
	 * Color of the edge
	 * @uml.property  name="colorEdges"
	 */
	Color colorEdges;
	
	/**
	 * Color of the junction
	 * @uml.property  name="colorJunctions"
	 */
	Color colorJunctions;

	/**
	 * Teh current polygon
	 * @uml.property  name="currentPolygon"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Polygon2D currentPolygon;

	/**
	 * Constants indicating the mode
	 */
	public enum ModeGeom {/**
	 * @uml.property  name="sEGMENT"
	 * @uml.associationEnd  
	 */
	SEGMENT, /**
	 * @uml.property  name="pOINT"
	 * @uml.associationEnd  
	 */
	POINT, /**
	 * @uml.property  name="pOLYGON"
	 * @uml.associationEnd  
	 */
	POLYGON, /**
	 * @uml.property  name="jUNCTION"
	 * @uml.associationEnd  
	 */
	JUNCTION};

	/**
	 * Indicates if we are using points, segments or polygons
	 * @uml.property  name="mode"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	ModeGeom mode;

	/**
	 * Indicates if we are selected points, segments or polygons
	 * @uml.property  name="typeSel"
	 * @uml.associationEnd  
	 */
	ModeGeom typeSel;
	
	/**
	 * Last position
	 * @uml.property  name="last"
	 * @uml.associationEnd  
	 */
	Point2D last=null;
	
	/**
	 * Shows the current frame of the sequence
	 * @uml.property  name="numFrame"
	 */
	int numFrame;
	
	/**
	 * Shows the current band of the sequence
	 * @uml.property  name="numBand"
	 */
	int numBand;
	
	/**
	 * Shows if we are selecting o adding
	 * @uml.property  name="action"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	GeomAction action = GeomAction.SELECT;
	
	/**
	 * Auxiliar elements when selecting geometrical elements
	 * @uml.property  name="selectedPoint"
	 * @uml.associationEnd  
	 */
	Point2D selectedPoint;
	/**
	 * @uml.property  name="selectedSegment"
	 * @uml.associationEnd  
	 */
	Segment selectedSegment;
	/**
	 * @uml.property  name="selectedPolygon"
	 * @uml.associationEnd  
	 */
	Polygon2D selectedPolygon;
	/**
	 * @uml.property  name="selectedJunction"
	 * @uml.associationEnd  
	 */
	Junction selectedJunction;
	
	/**
	 * Auxiliar variable to select
	 * @uml.property  name="idxPolySel"
	 */
	int idxPolySel = -1;

	/**
	 * Auxiliar Image type Variable
	 * @uml.property  name="imgtmp"
	 * @uml.associationEnd  
	 */
	JIPImage imgtmp;

	/**
	 * These variables allow to control the cursor with the keyboard
	 * @uml.property  name="posMouse"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Point2D posMouse=new Point2D(0,0);
	
	/**
	 * Reference to the Gui2d owning the Canvas
	 * @uml.property  name="gui2d"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="canvas:javavis.jip2d.gui.Gui2D"
	 */
	Gui2D gui2d;
	
	
	/**
	* Class constructor. The vectors and variables of the class are started
	* @param ww Width of the canvas
	* @param hh Height of the canvas
	*/
	public Canvas2D(int ww, int hh, Gui2D g2d) {
		gui2d = g2d;
		w = ww;
		h = hh;
		segments = new ArrayList<Segment>();
		points = new ArrayList<Point2D>();
		polygons = new ArrayList<Polygon2D>();
		currentPolygon = new Polygon2D();
		imgtmp = null;
		   
		setFocusable(true);
	    addMouseListener(this);
	    addMouseMotionListener(this);
	    addKeyListener(this);
		//historical vector of scale factors applied
		scaleFactors = new ArrayList<Integer>();
		visualizing = new ArrayList<Integer>();
		geoState = true;

		area = Box.createRigidArea(new Dimension(w, h));
		add(area);
		MyMouseListener mml = new MyMouseListener();
		addMouseListener(mml);
		addMouseMotionListener(mml);
		colorLine = new Color(255, 255, 255);
		currentColorLine = new Color(255, 255, 0);
		backgroundColor = new Color(0, 0, 0);
		colorPoint = new Color(128, 128, 255);
		colorPolygon = new Color(255, 255, 255);
		colorCurrentPolygon = new Color(128, 255, 128);
		backgroundColorScroll = new Color(63, 63, 63);
		colorEdges = new Color(255, 63, 255);
		colorJunctions = new Color(255, 63, 63);
		emphasizeSegments = false;
		emphasizePoints = true;
		emphasizePolygons = true;
		emphasizeEdges = true;
		emphasizeJuntions = true;
		showBitmap = showSegments = showPoints = showPolygons = showEdges = showJuntions = true;
		setBackground(backgroundColorScroll);
		
		mode = ModeGeom.SEGMENT;
	}

	/**
	 * It associates the information panel with the class
	 * @param i Information panel Geometry
	 */
	public void assoccInfoGeom(InfoPanelGeom i) {
		infoGeom = i;
	}
	
	/**
	 * It associates the information panel with the class
	 * @param i Information panel Botom
	 */
	public void assocInfoBottom(InfoPanelBottom i) {
		infoBottom = i;
	}

	/**
	 * It creates a new bitmap to create a new sequence, either it paints or 
	 * adds new images
	 * @param ww Width
	 * @param hh Height
	 * @param bg Background image
	 */
	public void newBitmap(int ww, int hh, ImageIcon bg) {
		scaleFactor = 100;
		scaleFactors.clear();
		geoState = true;
		w = ww;
		h = hh;
		remove(area);
		area = Box.createRigidArea(new Dimension(w, h));
		add(area);
		backGround = bg;
		if (sequence == null)
			infoBottom.assocSequence(null);
		selectedPoint=null;
		selectedSegment=null;
		selectedPolygon=null;
		selectedJunction=null;

		getParent().repaint();
	}
	
	/**
	 * It adds a new segment into the segments vector.
	 * @param x0 initial X coordinate of the segment
	 * @param y0 initial Y coordinate of the segment
	 * @param x1 final X coordinate of the segment
	 * @param y1 final Y coordinate of the segment
	 */
	public void addSegment(Segment si) {
		if (!(si.getBegin().getX() >= 00 && si.getBegin().getX() < w && si.getBegin().getY() >= 0 && si.getBegin().getY() < h && 
				si.getEnd().getX() >= 00 && si.getEnd().getX() < w && si.getEnd().getY() >= 0 && si.getEnd().getY() < h))
			return;
		for (Segment s : segments) {
			if (si.getBegin().getX() == s.getBegin().getX() && si.getBegin().getX() == s.getBegin().getY() && 
					si.getBegin().getX() == s.getEnd().getX() && si.getEnd().getY() == s.getEnd().getY())
				return;
		}
		segments.add(si);
	}

	/**
	* It adds a new point into the points vector
	* @param x X coordinate of the point
	* @param y Y coordinate of the point   
	*/
	public void addPoint(Point2D p) {
		if (p.getX() < 0 || p.getX() >= w || p.getY() < 0 || p.getY() >= h) 
			return;
		for (Point2D p2 : points) {
			if (p.getY() == p2.getX() && p.getY() == p2.getY())
				return;
		}
		points.add(p);
	}

	/**
	* It adds a new point into the actual polygon
	* @param x X coordintae of the new point in the polygon
	* @param y Y coordintae of the new point in the polygon  
	*/
	public void addPolyPoint(Point2D p) {
		if (p.getX() < 0 || p.getX() >= w || p.getY() < 0 || p.getY() >= h)
			return;
		ArrayList<Point2D> listp = currentPolygon.getData();
		for (Point2D p2 : listp) {
			if (p.getX() == p2.getX() && p.getY() == p2.getY())
				return;
		}
		currentPolygon.addPoint(p);
		last = p;
	}

	/**
	* It adds the current polygon into the global vector of polygons.
	* This polygon is already closed.  
	*/
	public void addPoligon() {
		polygons.add(currentPolygon);
		currentPolygon= new Polygon2D();
		last = null;
		getParent().repaint();
	}

	/**
	 *   This method is called when we don not have to display a background bitmap
	 */
	public void noBitmap() {
		backGround = null;
	}

	/**
	 * It sets the background bitmap from the bitmap path
	 * @param bitmapPath bitmap path
	 */
	public void putBitmap(String bitmapPath) throws JIPException {
		ImageIcon aux = new ImageIcon(bitmapPath);
		if (aux.getImageLoadStatus() == MediaTracker.ERRORED)
			return;
		backGround = aux;
		Image image = backGround.getImage();
		sequence = new Sequence(JIPToolkit.getColorImage(image));
		newBitmap(image.getWidth(backGround.getImageObserver()),
			image.getHeight(backGround.getImageObserver()),
			backGround);
		infoBottom.assocSequence(sequence);
		numFrame = numBand = 0;
		if (sequence != null)
			imgtmp = sequence.getFrame(numFrame);
		else
			imgtmp = null;
	}

	/**
	 * It changes the background color in the scroll panel
	 * @param nuevo New background color
	 */
	public void backgroundColorScroll(Color nuevo) {
		backgroundColorScroll = nuevo;
		setBackground(backgroundColorScroll);
	}

	/**
	 * It changes the line color
	 * @param c New line color   
	 */
	public void lineColor(Color c) {
		colorLine = c;
	}

	/**
	 * It changes the color of the current line
	 * @param c New color  
	 */
	public void currentLineColor(Color c) {
		currentColorLine = c;
	}

	/**
	 * It changes the background of the referenced frame to insert geometric data
	 * when we do File->New
	 * @param c New background color of the referenced frame
	 */
	public void backgroundColor(Color c) {
		backgroundColor = c;
	}

	/**
	 * It changes the color of points
	 * @param c New color of points
	 */
	public void pointColor(Color c) {
		colorPoint = c;
	}
	
	/**
	 * It changes the color of current point
	 * @param c New color of current point
	 */
	public void currentPointColor(Color c) {
		colorCurrentPoint = c;
	}

	/**
	 * It changes the color of polygons
	 * @param c New color of polygons
	 */
	public void polygonColor(Color c) {
		colorPolygon = c;
	}

	/**
	 * It changes the color of current polygon
	 * @param nuevo New color of current polygon
	 */
	public void currentPolygonColor(Color nuevo) {
		colorCurrentPolygon = nuevo;
	}

	/**
	 * It changes the color of edges
	 * @param c New color of edges
	 */
	public void edgeColor(Color c) {
		colorEdges = c;
	}

	/**
	 * It changes the color of junctions
	 * @param c New color of junctions
	 */
	public void junctionColor(Color c) {
		colorJunctions = c;
	}

	/**
	 * It changes the class variable to emphasize the segments
	 * @param b Boolean which shows if the segments have been emphasized
	 */
	public void enhanceSegments(boolean b) {
		emphasizeSegments = b;
	}

	/**
	 * 	It changes the class variable to emphasize the points.
	 * @param b Boolean which shows if the points have been emphasized
	 */
	public void enhancePoints(boolean b) {
		emphasizePoints = b;
	}

	/**
	 * It changes the class variable to emphasize the polygons.
	 * @param b Boolean which shows if the polygons have been emphasized
	 */
	public void enhancePolygon(boolean b) {
		emphasizePolygons = b;
	}
	
	/**
	 * It changes the class variable to emphasize the edges.
	 * @param b Boolean which shows if the edges have been emphasized	
	 */
	public void enhanceEdges(boolean b) {
		emphasizeEdges = b;
	}
	
	/**
	 * It changes the class variable to emphasize the junctions.
	 * @param b Boolean which shows if the edges have been emphasized	
	 */
	public void enhanceJunctions(boolean b) {
		emphasizeJuntions = b;
	}
	
	/**
	 * It changes the class variable to display the bitmap.
	 * @param b Boolean which shows if the polygons have been displayed	
	 */
	public void bitmapVisible(boolean b) {
		showBitmap = b;
	}
	
	/**
	 * It changes the class variable to display the segments.
	 * @param b Boolean which shows if the segments have been displayed
	 */
	public void segmentsVisible(boolean b) {
		showSegments = b;
	}
	
	/**
	 * It changes the class variable to display the points.
	 * @param b Boolean which shows if the points have been displayed
	 */
	public void pointsVisible(boolean b) {
		showPoints = b;
	}
	
	/**
	 * It changes the class variable to display the polygons.
	 * @param b Boolean which shows if the polygons have been displayed
	 */
	public void polygonsVisible(boolean b) {
		showPolygons = b;
	}
	
	/**
	 * It changes the class variable to display the edges.
	 * @param b Boolean which shows if the edges have been displayed
	 */
	public void edgesVisible(boolean b) {
		showEdges = b;
	}

	/**
	 * It returns the current selected frame 
	 * @return Number of current numFrame
	 */
	public int getFrameNum() {
		return numFrame;
	}

	/**
	 * 	It returns the current band 
	 * @return Number of current band
	 */
	public int getBandNum() {
		return numBand;
	}

	/**
	 * It associates a new name with the current sequence
	 * @param s New name of the sequence
	 */
	public void setSequenceName(String s) {
		if (sequence != null) {
			sequence.setName(s);
			infoBottom.assocSequence(sequence);
		}
	}
	
	/**
	 * It returns the name of the current sequence
	 * @return Name of the current sequence
	 */
	public String getSequenceName() {
		if (sequence != null)
			return sequence.getName();
		else
			return null;
	}
	
	/**
	 * It associates a new name with the current frame
	 * @param s New name for the current frame
	 */
	public void setFrameName(String s) throws JIPException {
		if (sequence == null)
			return;
		sequence.getFrame(numFrame).setName(s);
		infoBottom.assocSequence(sequence);
	}

	/**
	 * It associates a new sequence with the class
	 * @param s new sequence to associate
	 */
	public void setSequence(Sequence s) throws JIPException {
		sequence = s;
		if (s == null) {
			imgtmp = null;
			return;
		}
		imgtmp = sequence.getFrame(numFrame);

		JIPImage img = sequence.getFrame(0);
		ImageType t = img.getType();

		if (t == ImageType.BYTE || t == ImageType.BIT || t == ImageType.COLOR 
				|| t == ImageType.FLOAT) {
			Image img2 = JIPToolkit.getAWTImage(img);
			newBitmap(img.getWidth(), img.getHeight(), new ImageIcon(img2));
		} else if (t == ImageType.POINT || t == ImageType.POLY || 
				t == ImageType.SEGMENT || t == ImageType.EDGES || t== ImageType.JUNCTION) 
			newBitmap(img.getWidth(), img.getHeight(), null);
		numFrame = numBand = 0;
		getParent().repaint();
	}

	/**
	 * Necessary to repaint the new image 
	 */
	public void reassignedSeq() throws JIPException {
		JIPImage img = sequence.getFrame(numFrame);
		ImageType t = img.getType();

		if (t == ImageType.BYTE || t == ImageType.BIT || t == ImageType.COLOR 
				|| t == ImageType.FLOAT) {
			Image img2 = JIPToolkit.getAWTImage(img);
			newBitmap(img.getWidth(), img.getHeight(), new ImageIcon(img2));
		} else if (t == ImageType.POINT || t == ImageType.POLY || 
				t == ImageType.SEGMENT || t == ImageType.EDGES || t == ImageType.JUNCTION) 
			newBitmap(img.getWidth(), img.getHeight(), null);
	}
	
	/**
	 * It returns the current sequence
	 * @return Current sequence
	 */
	public Sequence getSequence() {
		scaleFactor = 100;
		return sequence;
	}

	/**
	 * It appends a frame to the sequence
	 * @param img Image to add
	 */
	public void addFrame(JIPImage img) throws JIPException {
		if (sequence != null)
			sequence.addFrame(img);
		else{
			setSequence(new Sequence(img));
			setSequenceName(img.getName());
		}
		infoBottom.assocSequence(sequence);
		gui2d.changeCanvasImage();
	}

	/**
	 * 	It appends a sequence to the current one
	 * @param s Sequence to append
	 */
	public void addFrames(Sequence s) throws JIPException {
		if (sequence == null || s == null)
			return;
		sequence.appendSequence(s);
		infoBottom.assocSequence(sequence);
		gui2d.changeCanvasImage();
	}

	/**
	 * 	It duplicates the current frame. 
	 */
	public void duplicateFrame() throws JIPException {
		JIPImage aux=sequence.getFrame(numFrame).clone();
		sequence.addFrame(aux);	
		infoBottom.assocSequence(sequence);
		gui2d.changeCanvasImage();
	}
	
	/**
	 * It changes the current frame
	 * @param img New image
	 */
	public void changeFrame(JIPImage img) throws JIPException {
		sequence.setFrame(img, numFrame);
		infoBottom.assocSequence(sequence);
		gui2d.changeCanvasImage();
	}

	/**
	 * It removes the current frame (if it is not the last one)
	 */
	public void removeFrame() throws JIPException {
		sequence.removeFrame(numFrame);
		backGround = null;
		outView();
		infoBottom.assocSequence(sequence);
		if (sequence != null)
			changeToFrame(0);
		gui2d.changeCanvasImage();
	}

	/**
	 * It removes the current band (if it is not the last one)
	 */
	public void removeBand() throws JIPException {
		JIPImage aux = sequence.getFrame(numFrame);
		if (aux instanceof JIPImgGeometric)
			throw new JIPException("Canvas2D.removeBand: do not valid for geometric types");
		((JIPImgBitmap)aux).removeBand(numBand);
		sequence.setFrame(aux, numFrame);
		infoBottom.assocSequence(sequence);
	}

	/**
	 * Returns the length of the last segment
	 * @return Length of last segment
	 */
	public float getLengthLastSegment() {
		if (segments.size() == 0)
			return -1f;
		Segment s = segments.get(segments.size()-1);
		int x0 = s.getBegin().getX();
		int y0 = s.getBegin().getY();
		int x1 = s.getEnd().getX();
		int y1 = s.getEnd().getY();
		return (float) Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
	}

	/**
	 * Returns the length of the selected segment
	 * @return Length of selected segment
	 */
	public float getLengthSelectedSegment() {
		if (segments.size() == 0 || selectedSegment==null || typeSel != ModeGeom.SEGMENT)
			return -1f;
		return (float) Math.sqrt(
			Math.pow(selectedSegment.getBegin().getX() - selectedSegment.getEnd().getX(), 2) + 
			Math.pow(selectedSegment.getBegin().getY() - selectedSegment.getEnd().getY(), 2));
	}

	/**
	 * Returns the distance between the last two points
	 * @return Distance between the last two points
	 */
	public float getDistanceLastPoints() {
		if (points.size() < 2)
			return -1f;
		Point2D p1=points.get(points.size()-2);
		Point2D p2=points.get(points.size()-2);
		int x0 = p1.getX();
		int y0 = p1.getY();
		int x1 = p2.getX();
		int y1 = p2.getY();
		return (float) Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
	}
	
	/**
	 * Set the image icon
	 */
	public void setBackGround(ImageIcon ic) {
		backGround = ic;
	}

	/**
	 * It returns an array with the segment data
	 * @return Array with segment data
	 */
	public Object[][] getSegmentData() {
		int x0, y0, x1, y1, i=0;
		Object data[][] = new Object[segments.size()][6];
		for (Segment s : segments) {
			x0 = s.getBegin().getX();
			y0 = s.getBegin().getY();
			x1 = s.getEnd().getX();
			y1 = s.getEnd().getY();
			data[i][0] = i;
			data[i][1] = x0;
			data[i][2] = y0;
			data[i][3] = x1;
			data[i][4] = y1;
			data[i][5] = (float) Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
			i++;
		}
		return (data);
	}

	/**
	 * It loads in the class a sequence from a file 
	 * @param file Name of the file which has the sequence
	 */
	public void loadSequence(String file) throws JIPException {
		sequence = JIPToolkit.getSeqFromFile(file);
		if (sequence != null)
			imgtmp = sequence.getFrame(0);
		else
			imgtmp = null;
		if (sequence == null)
			return;
		segments.clear();
		points.clear();
		polygons.clear();
		backGround = null;
		infoBottom.assocSequence(sequence);
		numFrame = numBand = 0;
		getParent().repaint();
	}

	/**
	 * It returns the scale factor according to total of scale factor applied
	 * @return Scale factor
	*/
	public double sequenceZoom() {
		double sol = 1.0;

		for (int i = 0; i < scaleFactors.size(); i++)
			sol = sol * scaleFactors.get(i) / 100.0;
		return (sol);
	}

	/**
	 * It changes the current frame to the one indicated as parameter
	 * @param n Number of numFrame that we will load
	 */
	public void changeToFrame(int n) throws JIPException {
		if (sequence == null || n<0 || n > sequence.getNumFrames()) {
			imgtmp = null;
			return;
		}
		numFrame = n;
		imgtmp = sequence.getFrame(numFrame);
		ImageType t = imgtmp.getType();
		numBand = 0;

		if (imgtmp instanceof JIPImgBitmap) {
			Image img2 = JIPToolkit.getAWTImage(imgtmp);

			if (!geoState) { //Zoom mode
				Image zoom = img2.getScaledInstance((int)(imgtmp.getWidth() * sequenceZoom()),
							(int)(imgtmp.getHeight() * sequenceZoom()), 0);

				backGround = new ImageIcon(zoom);
				w = zoom.getWidth(backGround.getImageObserver());
				h = zoom.getHeight(backGround.getImageObserver());

				worg = sequence.getFrame(numFrame).getWidth();
				horg = sequence.getFrame(numFrame).getHeight();

				remove(area);
				area = Box.createRigidArea(new Dimension(w, h));
				add(area);
			} else
				newBitmap(imgtmp.getWidth(), imgtmp.getHeight(), new ImageIcon(img2));

			segments.clear();
			points.clear();
			polygons.clear();

			visualizing.clear();
			n = ((JIPImgBitmap)imgtmp).getNumBands();
			if (t == ImageType.COLOR)
				n = -1;
		} else {
			for (int i = 0; i < visualizing.size(); i++)
				if (n == visualizing.get(i))
					return;
			visualizing.add(n);

			if (backGround == null)
				newBitmap(imgtmp.getWidth(), imgtmp.getHeight(), null);
			n=-1;
		}

		infoBottom.updateBandsFrame(imgtmp, n);
		getParent().repaint();
	}
	
	/**
	 * 	It removes all geometric elements
	 */
	public void outView() {
		segments.clear();
		points.clear();
		polygons.clear();
		visualizing.clear();
	}

	/** 
	 * Changes the current band
	 * @param n Number of band to show
	 */
	public void changeBand(int n) throws JIPException {
		if (sequence == null || n == numBand)
			return;
		JIPImage img = sequence.getFrame(numFrame);
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Canvas2D.changeBand: do not valid for geometric data");
		if (n > ((JIPImgBitmap)img).getNumBands())
			return;

		Image img2 = JIPToolkit.getAWTImage(img, n);

		if (img2 != null) {
			if (!geoState) { //Zoom mode
				Image zoom  = img2.getScaledInstance((int)(img.getWidth()*sequenceZoom()),
						(int)(img.getHeight()*sequenceZoom()), 0);

				backGround = new ImageIcon(zoom);
				w = zoom.getWidth(backGround.getImageObserver());
				h = zoom.getHeight(backGround.getImageObserver());

				worg = sequence.getFrame(numFrame).getWidth();
				horg = sequence.getFrame(numFrame).getHeight();

				remove(area);
				area = Box.createRigidArea(new Dimension(w, h));
				add(area);
			} else
				newBitmap(img.getWidth(), img.getHeight(), new ImageIcon(img2));
		}
		numBand = n;
		getParent().repaint();
	}

	/**
	 * It removes the last segment
	 */
	public void deleteLastSegment() {
		if (!segments.isEmpty()) {
			segments.remove(segments.size() - 1);
		}
		getParent().repaint();
	}
	
	/**
	 * It removes the last point
	 */
	public void deleteLastPoint() {
		if (!points.isEmpty()) {
			points.remove(points.size() - 1);
		}
		getParent().repaint();
	}

	/**
	 * It removes the last polygon
	 */
	public void deleteLastPolygon() {
		if (!polygons.isEmpty())  {
			polygons.remove(polygons.size() - 1);
		}
		getParent().repaint();
	}

	/**
	 * It removes the actual selection
	 */
	public void deleteSelection() {
		if (typeSel == ModeGeom.POINT) {
			for (int i = 0; i < points.size(); i++) {
				if (selectedPoint.getX() == points.get(i).getX()
					&& selectedPoint.getY() == points.get(i).getY()) {
					points.remove(i);
					selectedPoint=null;
					typeSel=null;
					getParent().repaint();
					return;
				}
			}
		}
		if (typeSel == ModeGeom.SEGMENT) {
			Segment s;
			for (int i = 0; i < segments.size(); i += 4) {
				s = segments.get(i);
				if (selectedSegment.getBegin().getX() == s.getBegin().getX() && selectedSegment.getBegin().getY() == s.getBegin().getY()
					&& selectedSegment.getEnd().getX() == s.getEnd().getX() && selectedSegment.getEnd().getY() == s.getEnd().getY()) {
					/*if (i < indiceSeg) {
						new Dialog(this).information("Data from a sequence can not be deleted", "ERROR");
						return;
					}*/
					segments.remove(i);
					selectedSegment = null;
					typeSel = null;
					getParent().repaint();
					return;
				}
			}
		}
	}

	/**
	 * Returns the new zoom value according to the scale factor
	 * @param vorg
	 * @return New value of the point
	 */
	public int getZoomValue(int vorg) {
		double dmul;

		for (int fa = 0; fa < scaleFactors.size(); fa++) {
			scaleFactor = scaleFactors.get(fa);
			dmul = (scaleFactor / 100.0);
			vorg = (int)((vorg * dmul) + (dmul - 1));
		}
		return vorg;
	}

	/**
	 * @param percent
	 */
	public void zoomWindow(int percent) throws JIPException {
		if (sequence == null || percent == 100)
			return;

		scaleFactor = percent;
		scaleFactors.add(scaleFactor);
		geoState = false; 

		Image zoom;
		if (sequence.getFrame(numFrame).getType() == ImageType.COLOR)
			zoom = JIPToolkit.getAWTImage(sequence.getFrame(numFrame)).getScaledInstance(
					(w * percent) / 100, (h * percent) / 100, 0);
		else
			zoom = JIPToolkit.getAWTImage(sequence.getFrame(numFrame),
					numBand).getScaledInstance((w * percent) / 100, (h * percent) / 100, 0);

		backGround = new ImageIcon(zoom);
		w = zoom.getWidth(backGround.getImageObserver());
		h = zoom.getHeight(backGround.getImageObserver());

		worg = sequence.getFrame(numFrame).getWidth();
		horg = sequence.getFrame(numFrame).getHeight();

		remove(area);
		area = Box.createRigidArea(new Dimension(w, h));
		add(area);
		infoGeom.updateGeo(geoState);
		gui2d.changeCanvasImage();
		getParent().repaint();
	}

	/**
	 * 	It exports geometric data of the temporal vector to ASCII file
	 */
	public void exportAscii() {
		JFileChooser openFile = new JFileChooser();
		FileWriter fOut;
		try {
			openFile.setCurrentDirectory(new File("."));
			openFile.setSelectedFile(new File("Ascii.txt"));
			int returnVal = openFile.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File fich = openFile.getSelectedFile();
				fOut = new FileWriter(fich);
				if (!points.isEmpty()) {
					fOut.write("POINTS " + points.size() + "\n");
					for (Point2D p : points) {
						fOut.write(p.toString());
						fOut.write("\n");
					}
				}

				if (!segments.isEmpty()) {
					fOut.write("SEGMENTS " + segments.size() + "\n");
					for (Segment s : segments) {
						fOut.write(s.toString());
						fOut.write("\n");
					}
				}
				if (!polygons.isEmpty()) {
					fOut.write("POLYGONS " + polygons.size() + "\n");
					fOut.write(polygons.size() + " : ");
					for (Polygon2D aux : polygons) {
						fOut.write(aux.toString());
						fOut.write("\n");
					}
				}
				fOut.close();
			}
		} catch (Exception err) {logger.error(err);}
	}

	/**
	 * 	It import geometric data of the ASCII file to the temporal vectors	
	 */
	public void importAscii() throws JIPException {
		boolean message = false;
		int c1, c2, c3, c4, amount, numpoints;
		try {
			JFileChooser openFile = new JFileChooser();
			openFile.setCurrentDirectory(new File("."));
			int returnVal = openFile.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File fich = openFile.getSelectedFile();
				FileInputStream fileInput = new FileInputStream(fich);
				Reader r = new BufferedReader(new InputStreamReader(fileInput));
				StreamTokenizer st = new StreamTokenizer(r);
				ArrayList<Integer> P = new ArrayList<Integer>();

				String title = "";
				st.nextToken();
				if (st.ttype != StreamTokenizer.TT_EOF) {
					title = st.sval;
					if (title.compareTo("POINTS") == 0) {
						emptyPoints();
						st.nextToken();
						amount = (int) st.nval;
						for (int kp = 0; kp < amount * 2; kp += 2) {
							st.nextToken();
							c1 = (int) st.nval;
							st.nextToken();
							c2 = (int) st.nval;
							addPoint(new Point2D(c1, c2));
							if (c1 < 0 || c1 >= w || c2 < 0 || c2 >= h)
								message = true;
						}
						st.nextToken();
					}
					if (st.ttype != StreamTokenizer.TT_EOF) {
						title = st.sval;
						if (title.compareTo("SEGMENTS") == 0) {
							emptySegments();
							st.nextToken();
							amount = (int) st.nval;
							for (int kp = 0; kp < amount * 4; kp += 4) {
								st.nextToken();
								c1 = (int) st.nval;
								st.nextToken();
								c2 = (int) st.nval;
								st.nextToken();
								c3 = (int) st.nval;
								st.nextToken();
								c4 = (int) st.nval;
								addSegment(new Segment(new Point2D(c1, c2), new Point2D(c3, c4)));
								if (c1 < 0 || c1 >= w || c3 < 0 || c3 >= w
									|| c2 < 0 || c2 >= h || c2 < 0 || c2 >= h) {
									message = true;
								}
							}
							st.nextToken();
						}
						if (st.ttype != StreamTokenizer.TT_EOF) {
							title = st.sval;
							if (title.compareTo("POLYGONS") == 0) {
								emptyPolygons();
								st.nextToken();
								amount = (int) st.nval;
								for (int kp = 0; kp < amount; kp++) {
									st.nextToken();
									numpoints = (int) st.nval;
									st.nextToken(); // LOS :
									boolean rec = true;
									for (int kl = 0; kl < numpoints; kl += 2) {
										st.nextToken();
										c1 = (int) st.nval;
										st.nextToken();
										c2 = (int) st.nval;
										if (c1 < 0 || c1 >= w || c2 < 0 || c2 >= h) {
											rec = false;
											message = true;
										}
										P.add(c1);
										P.add(c2);
									}
									if (rec) 
										//polygons.add(new ArrayList<Integer>(P));
									P.clear();
								}
							}
						} 
					} 
				} 
				if (message == true)
					new Dialog(this).information("Some points outside of the image have been omitted","ATTENTION");
			}
		} 
		catch (FileNotFoundException e) {logger.error(e);} 
		catch (IOException e) {logger.error(e);}

		infoBottom.assocSequence(sequence);
		getParent().repaint();

	}

	/**
	 * Converts the geometric data into a sequence.
	 * Every geometric type are in a different frame.
	 */
	public void saveGeometry() {
		JIPImgGeometric img;

		try {
			if (!points.isEmpty()) {
				img = new JIPGeomPoint(w, h);
				img.setData(points);
				if (sequence != null)
					sequence.addFrame(img);
				else
					sequence = new Sequence(img);
				points=new ArrayList<Point2D>();
			}
			if (!segments.isEmpty()) {
				img = new JIPGeomSegment(w, h);
				img.setData(segments);
				if (sequence != null)
					sequence.addFrame(img);
				else
					sequence = new Sequence(img);
				segments=new ArrayList<Segment>();
			}
			if (!polygons.isEmpty()) {
				img = new JIPGeomPoly(w, h);
				img.setData(polygons);
				if (sequence != null)
					sequence.addFrame(img);
				else 
					sequence = new Sequence(img);
				polygons=new ArrayList<Polygon2D>(); 
			}
			numFrame = 0;
			numBand = 0;
	
			if (sequence != null)
				imgtmp = sequence.getFrame(numFrame);
			else
				imgtmp = null;
		}catch (JIPException e){logger.error(e);}
		infoBottom.assocSequence(sequence);
		getParent().repaint();
	}

	/**
	 * It adds an empty frame with the type indicated as parameter 
	 * @param type Geometric type 
	 */
	public void addEmptyFrame(ImageType type) throws JIPException {
		JIPImgGeometric img;
		switch (type) {
			case POINT: img = new JIPGeomPoint(w,h);
						break;
			case SEGMENT: img = new JIPGeomSegment(w,h);
						break;
			case POLY: img = new JIPGeomPoly(w,h);
						break;
			case EDGES: img = new JIPGeomEdges(w,h);
						break;
			case JUNCTION: img = new JIPGeomJunctions(w,h);
						break;
			default: img=null;
		}
		if (sequence != null)
			sequence.addFrame(img);
		else
			sequence = new Sequence(img);
		infoBottom.assocSequence(sequence);
	}

	/**
	 * 	Empties the point array
	 */
	public void emptyPoints() throws JIPException {
		points.clear();
		infoBottom.assocSequence(sequence);
		getParent().repaint();
		for (int i = 0; i < visualizing.size(); i++) {
			if (sequence.getFrame(visualizing.get(i))
				.getType() == ImageType.POINT) {
				visualizing.remove(i);
				i--;
			}
		}
	}

	/**
	 * Empties the segment array
	 */
	public void emptySegments() throws JIPException {
		segments.clear();
		infoBottom.assocSequence(sequence);
		getParent().repaint();
		for (int i = 0; i < visualizing.size(); i++) {
			if (sequence.getFrame(visualizing.get(i))
				.getType() == ImageType.SEGMENT) {
				visualizing.remove(i);
				i--;
			}
		}
	}

	/**
	 * Empties the polygons array
	 */
	public void emptyPolygons() throws JIPException {
		polygons.clear();
		infoBottom.assocSequence(sequence);
		getParent().repaint();
		for (int i = 0; i < visualizing.size(); i++) {
			if (sequence.getFrame(visualizing.get(i))
				.getType() == ImageType.POLY) {
				visualizing.remove(i);
				i--;
			}
		}
	}

	/**
	 * Empties all the geometric data
	 */
	public void emptyAll() {
		points.clear();
		segments.clear();
		polygons.clear();
		action = GeomAction.SELECT;
		infoBottom.assocSequence(sequence);
		visualizing.clear();
		getParent().repaint();
	}

	/**
	 * It captures the events which are produced in the information panel
	 * @param e Event
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==infoGeom.bsegment)
			mode = ModeGeom.SEGMENT;
		if (e.getSource()==infoGeom.bpoint) {
			mode = ModeGeom.POINT;
			dragged = false;
		}
		if (e.getSource()==infoGeom.bpoly) {
			mode = ModeGeom.POLYGON;
			dragged = false;
		}
		if (e.getSource()==infoGeom.bjunc) {
			mode = ModeGeom.JUNCTION;
			dragged = false;
		}

		if (e.getSource()==infoGeom.actionSel) {
			if (infoGeom.actionSel.getSelectedIndex()==0)
				action = GeomAction.SELECT;
			else  action = GeomAction.ADD;
			if (action == GeomAction.ADD) {
				infoGeom.bjunc.setEnabled(false);
				selectedPoint = null;
				selectedSegment = null;
				selectedPolygon = null;
				selectedJunction = null;
				idxPolySel = -1;
				infoGeom.updateSelVoid();
				infoGeom.updateGeo(geoState);
				return;
			}
			else
				infoGeom.bjunc.setEnabled(true);
		}
	}

	/**
	 * 	MouseListener class
	 */
	class MyMouseListener extends MouseAdapter implements MouseMotionListener {
		
		/**
		 * Method which is called when the mouse is pressed
		 * @param e Event
		 */
		public void mousePressed(MouseEvent e) {
			try {
				if (geoState) {
					if (action == GeomAction.SELECT) {
						if (mode == ModeGeom.POINT) {
							ini = new Point2D(e.getX(), e.getY());
							for (Point2D p : points) {
								if (Math.sqrt(Math.pow(p.getX() - ini.getX(), 2) + Math.pow(p.getY() - ini.getY(), 2)) < DIST_THRESH) {
									selectedPoint = p;
									typeSel = ModeGeom.POINT;
									infoGeom.updateSelPoint(p);
									infoGeom.updateGeo(geoState);
									getParent().repaint();
									return;
								}
							}
							for (Integer i : visualizing) {
								JIPImage img=sequence.getFrame(visualizing.get(i));
								if (img.getType()==ImageType.POINT) {
									ArrayList <Point2D> pointAux = ((JIPGeomPoint)img).getData();
									for (Point2D p : pointAux) {
										if (Math.sqrt(Math.pow(p.getX() - ini.getX(), 2) + Math.pow(p.getY() - ini.getY(), 2)) < DIST_THRESH) {
											selectedPoint = p;
											typeSel = ModeGeom.POINT;
											infoGeom.updateSelPoint(p);
											infoGeom.updateGeo(geoState);
											getParent().repaint();
											return;
										}
									}
								}
							}
							selectedPoint=null;
							getParent().repaint();
						}
						if (mode == ModeGeom.JUNCTION) {
							ini = new Point2D(e.getX(), e.getY());
							for (Integer i : visualizing) {
								JIPImage img=sequence.getFrame(visualizing.get(i));
								if (img.getType()==ImageType.JUNCTION) {
									ArrayList <Junction> junctionAux = ((JIPGeomJunctions)img).getData();
									for (Junction j : junctionAux) {
										if (Math.sqrt(Math.pow(j.getX() - ini.getX(), 2) + Math.pow(j.getY() - ini.getY(), 2)) < DIST_THRESH) {
											selectedJunction = j;
											typeSel = ModeGeom.JUNCTION;
											infoGeom.updateSelJunction(j);
											infoGeom.updateGeo(geoState);
											getParent().repaint();
											return;
										}
									}
								}
							}
							selectedJunction=null;
							getParent().repaint();
						}
						if (mode == ModeGeom.POLYGON) {
							ini = new Point2D(e.getX(), e.getY());
							ArrayList<Point2D> points; 
							for (Polygon2D pol : polygons) {
								points = pol.getData();
								for (Point2D p : points) {
									if (Math.sqrt(Math.pow(p.getX() - ini.getX(), 2) + Math.pow(p.getY() - ini.getY(), 2)) < DIST_THRESH) {
										selectedPolygon = pol;
										infoGeom.updateSelPolygon(pol);
										infoGeom.updateGeo(geoState);
										typeSel = ModeGeom.POLYGON;
										getParent().repaint();
										return;
									}
								}
							}
							for (Integer i : visualizing) {
								JIPImage img=sequence.getFrame(visualizing.get(i));
								if (img.getType()==ImageType.POLY) {
									ArrayList <Polygon2D> polyAux = ((JIPGeomPoly)img).getData();
									for (Polygon2D pol : polyAux) {
										points = pol.getData();
										for (Point2D p : points) {
											if (Math.sqrt(Math.pow(p.getX() - ini.getX(), 2) + Math.pow(p.getY() - ini.getY(), 2)) < DIST_THRESH) {
												selectedPolygon = pol;
												infoGeom.updateSelPolygon(pol);
												infoGeom.updateGeo(geoState);
												typeSel = ModeGeom.POLYGON;
												getParent().repaint();
												return;
											}
										}
									}
								}
							}
							selectedPolygon=null;
							getParent().repaint();
						}
						if (mode == ModeGeom.SEGMENT) {
							ini = new Point2D(e.getX(), e.getY());
							for (Segment s : segments) {
								if (Math.sqrt(Math.pow(s.getBegin().getX() - ini.getX(), 2) + Math.pow(s.getBegin().getY() - ini.getY(), 2)) < DIST_THRESH
										||Math.sqrt(Math.pow(s.getEnd().getX() - ini.getX(), 2) + Math.pow(s.getEnd().getY() - ini.getY(), 2)) < DIST_THRESH) {
									selectedSegment = s;
									infoGeom.updateSelSegment(s);
									infoGeom.updateGeo(geoState);
									typeSel = ModeGeom.SEGMENT;
									getParent().repaint();
									return;
								}
							}
							for (Integer i : visualizing) {
								JIPImage img=sequence.getFrame(visualizing.get(i));
								if (img.getType()==ImageType.SEGMENT) {
									ArrayList <Segment> segmentAux = ((JIPGeomSegment)img).getData();
									for (Segment s : segmentAux) {
										if (Math.sqrt(Math.pow(s.getBegin().getX() - ini.getX(), 2) + Math.pow(s.getBegin().getY() - ini.getY(), 2)) < DIST_THRESH
												||Math.sqrt(Math.pow(s.getEnd().getX() - ini.getX(), 2) + Math.pow(s.getEnd().getY() - ini.getY(), 2)) < DIST_THRESH) {
											selectedSegment = s;
											infoGeom.updateSelSegment(s);
											infoGeom.updateGeo(geoState);
											typeSel = ModeGeom.SEGMENT;
											getParent().repaint();
											return;
										}
									}
								}
							}
							selectedSegment=null;
							getParent().repaint();
						}
					}
					else {
						if (mode == ModeGeom.SEGMENT) {
							ini = new Point2D(e.getX(), e.getY());
							fin = ini;
						}
						if (mode == ModeGeom.POLYGON) {
							if (last != null) {
								ini = last;
								fin = new Point2D(e.getX(), e.getY());
								if (fin.getX() > w - 1)
									fin.setX(w - 1);
								if (fin.getY() > h - 1)
									fin.setY(h - 1);
								if (fin.getX() < 0)
									fin.setX(0);
								if (fin.getY() < 0)
									fin.setY(0);
							}
							else {
								if (ini==null) ini = new Point2D(e.getX(), e.getY());
							}
						}
						if (mode == ModeGeom.SEGMENT || mode == ModeGeom.POLYGON) {
							if (ini.getX() >= 0 && ini.getX() < w && ini.getY() >= 0 && ini.getY() < h)
								dragged = true;
						}
						if (mode == ModeGeom.POINT) {
							ini = new Point2D(e.getX(), e.getY());
							addPoint(ini);
							getParent().repaint();
						}
					}
				}
			} catch (JIPException exp) {logger.error(exp);};
		}

		/**
		 * @param e Event
		 */
		public void mouseReleased(MouseEvent e) {
			if (geoState) {
				if (action == GeomAction.SELECT)
					return; 
				if (mode == ModeGeom.SEGMENT) {
					dragged = false;
					addSegment(new Segment(ini, fin));
					infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
					infoBottom.updateInfo(e.getX(), e.getY(), imgtmp, numBand);
					infoGeom.updateGeo(geoState);
					getParent().repaint();
				}
				if (mode == ModeGeom.POLYGON) {
					if (ini!=null && fin!=null && ini.getX() == fin.getX() && ini.getY() == fin.getY()) {
						if (currentPolygon.length() > 2)
							addPoligon();
					} else {
						if (fin==null)
							addPolyPoint(ini);
						else
							addPolyPoint(fin);
					}
					getParent().repaint();
				}
			}
		}

		/**
		 * @param e Event
		 */
		public void mouseMoved(MouseEvent e) {
			if (mode != ModeGeom.POINT && action == GeomAction.ADD && geoState) {
				infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
				infoBottom.updateInfo(e.getX(), e.getY(), imgtmp, numBand);
				infoGeom.updateGeo(geoState);
			} else {
				int mx, my;
				mx = e.getX();
				my = e.getY();
				double mul = w / (double)worg;
				scaleFactor = (int)(100*mul);
				if (!geoState && mul > 1) {
					// geometry off -> doing zoom
					int xs, ys;
					ys = (int) (my / mul);
					xs = (int) (mx / mul);
					infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
					infoBottom.updateInfo(xs, ys, imgtmp, numBand);
					infoGeom.updateGeo(geoState);
				} else {
					double dmul = scaleFactor / 100.0;
					if (dmul < 1) {
						int xs, ys;
						ys = (int) (my / dmul);
						xs = (int) (mx / dmul);
						infoGeom.updateInfo(segments.size(), points.size(),
							polygons.size());
						infoBottom.updateInfo(xs, ys, imgtmp, numBand);
						infoGeom.updateGeo(geoState);
					} else {
						infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
						infoBottom.updateInfo(e.getX(), e.getY(), imgtmp, numBand);
						infoGeom.updateGeo(geoState);
					}
				}
			}
		}
		
		/**
		 * @param e Event
		 */
		public void mouseDragged(MouseEvent e) {
			if (mode != ModeGeom.POINT && action == GeomAction.ADD && geoState) {
				infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
				infoBottom.updateInfo(e.getX(), e.getY(), imgtmp, numBand);
				infoGeom.updateGeo(geoState);
			} else {
				int mx, my;
				mx = e.getX();
				my = e.getY();
				scaleFactor = (int) (100*w / (double) worg);
				double mul = (scaleFactor / 100.0);
				if (!geoState && mul > 1) {
					//geometry off -> doing zoom
					int xs, ys;
					ys = (int) ((my / mul) + 1);
					xs = (int) ((mx / mul) + 1);
					infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
					infoBottom.updateInfo(xs, ys, imgtmp, numBand);
					infoGeom.updateGeo(geoState);
				} else {
					double dmul = scaleFactor / 100.0;
					if (dmul < 1) {
						int xs, ys;
						ys = (int) (my / dmul);
						xs = (int) (mx / dmul);
						infoGeom.updateInfo(segments.size(), points.size(),
							polygons.size());
						infoBottom.updateInfo(xs, ys, imgtmp, numBand);
						infoGeom.updateGeo(geoState);
					} else {
						infoGeom.updateInfo(segments.size(), points.size(), polygons.size());
						infoBottom.updateInfo(e.getX(), e.getY(), imgtmp, numBand);
						infoGeom.updateGeo(geoState);
					}
				}
			}
			if (geoState) {
				if (action == GeomAction.SELECT) return; //Select
				if (mode == ModeGeom.SEGMENT || mode == ModeGeom.POLYGON) {
					if (dragged) {
						fin = new Point2D(e.getX(), e.getY());
						if (fin.getX() > w - 1) fin.setX(w - 1);
						if (fin.getY() > h - 1) fin.setY(h - 1);
						if (fin.getX() < 0) fin.setX(0);
						if (fin.getY() < 0) fin.setY(0);
						getParent().repaint();
					}
				}
			}
		}
	}

	/**
	 * Method which repaints the background bitmap and the geometric elements	 
	 * @param g Graphics
	 */
	public void paint(Graphics g) {
		paintComponent(g);
		g.setColor(backgroundColor);
		g.fillRect(0, 0, w, h);
		if (backGround != null && showBitmap)
			backGround.paintIcon(this, g, 0, 0);

		try {
			if (showPolygons) {
				ArrayList<Polygon2D> polys = new ArrayList<Polygon2D>();
				polys.addAll(polygons);
				if (sequence!=null) {
					for (Integer i : visualizing) {
						JIPImage img = sequence.getFrame(i);
						if (img.getType()==ImageType.POLY)  
							polys.addAll(((JIPImgGeometric)img).getData());
					}
				}
				for (Polygon2D poli : polys) {
					if (poli.getColor()!=null) 
						g.setColor(poli.getColor());
					else
						g.setColor(colorPolygon);
					int xpoint[] = new int[poli.length()];
					int ypoint[] = new int[poli.length()];
					ArrayList<Point2D> listp = poli.getData();
					for (int j = 0; j < listp.size(); j++) {
						xpoint[j] = getZoomValue(listp.get(j).getX());
						ypoint[j] = getZoomValue(listp.get(j).getY());
					}
		
					g.drawPolygon(xpoint, ypoint, xpoint.length);
					if (emphasizePolygons) {
						for (int j = 0; j < xpoint.length; j++)
							xpoint[j]++;
						g.drawPolygon(xpoint, ypoint, xpoint.length);
						for (int j = 0; j < xpoint.length; j++) {
							ypoint[j]++;
							xpoint[j]--;
						}
						g.drawPolygon(xpoint, ypoint, xpoint.length);
					}
				}
			}
			if (showEdges && sequence!=null) {
				ArrayList<Edge> edgeAux= new ArrayList<Edge>();
				for (Integer i : visualizing) {
					JIPImage img = sequence.getFrame(i);
					if (img.getType()==ImageType.EDGES) 
						edgeAux.addAll(((JIPImgGeometric)img).getData());
				}
				for (Edge edg : edgeAux) {
					if (edg.getColor() != null) 
						g.setColor(edg.getColor());
					else
						g.setColor(colorEdges);
					int xpoint[] = new int[edg.length()];
					int ypoint[] = new int[edg.length()];
					ArrayList<Point2D> liste = edg.getData();
					for (int j = 0; j < liste.size(); j++) {
						xpoint[j] = getZoomValue(liste.get(j).getX());
						ypoint[j] = getZoomValue(liste.get(j).getY());
					}
					for (int j = 0; j < xpoint.length; j++) {
						if (emphasizeEdges)
							g.fillOval(xpoint[j] - 2, ypoint[j] - 2, 4, 4);
						else
							g.fillOval(xpoint[j], ypoint[j], 1, 1);
					}
				}
			}
	
			if (showSegments) {
				ArrayList<Segment> segmentAux = new ArrayList<Segment>();
				segmentAux.addAll(segments);
				if (sequence!=null) {
					for (Integer i : visualizing) {
						JIPImage img = sequence.getFrame(i);
						if (img.getType()==ImageType.SEGMENT) 
							segmentAux.addAll(((JIPImgGeometric)img).getData());
					}
				}
				for (Segment s : segmentAux) {
					if (s.getColor() != null) 
						g.setColor(s.getColor());
					else
						g.setColor(colorLine);
					int x0 = getZoomValue(s.getBegin().getX());
					int y0 = getZoomValue(s.getBegin().getY());
					int x1 = getZoomValue(s.getEnd().getX());
					int y1 = getZoomValue(s.getEnd().getY());
					g.drawLine(x0, y0, x1, y1);
					if (emphasizeSegments) {
						g.drawLine(x0 + 1, y0, x1 + 1, y1);
						g.drawLine(x0, y0 + 1, x1, y1 + 1);
					}
				}
			}
			g.setColor(currentColorLine);
			if (dragged && action == GeomAction.ADD) {
				if (last == null || mode == ModeGeom.SEGMENT)
					g.drawLine(ini.getX(), ini.getY(), fin.getX(), fin.getY());
				if ((emphasizeSegments && mode == ModeGeom.SEGMENT)
					|| (emphasizePolygons && mode == ModeGeom.POLYGON && last == null)) {
					g.drawLine(ini.getX()+1, ini.getY(), fin.getX()+1, fin.getY());
					g.drawLine(ini.getX(), ini.getY()+1, fin.getX(), fin.getY()+1);
				}
			}
			if (showPoints) {
				ArrayList<Point2D> pointAux = new ArrayList<Point2D>();
				pointAux.addAll(points);
				if (sequence!=null) {
					for (Integer i : visualizing) {
						JIPImage img = sequence.getFrame(i);
						if (img.getType()==ImageType.POINT) 
							pointAux.addAll(((JIPImgGeometric)img).getData());
					}
				}
				for (Point2D p : pointAux) {
					if (p.getColor() != null) 
						g.setColor(p.getColor());
					else
						g.setColor(colorPoint);
					int xp = getZoomValue(p.getX());
					int yp = getZoomValue(p.getY());
					if (emphasizePoints)
						g.fillOval(xp - 2, yp - 2, 4, 4);
					else
						g.fillOval(xp, yp, 1, 1);
				}
			}
			if (showJuntions && sequence!=null) { 
				for (Integer a : visualizing) {
					JIPImage img = sequence.getFrame(a);
					if (img.getType()==ImageType.JUNCTION) {
						ArrayList<Junction> junctionAux = ((JIPImgGeometric)img).getData();
						for (Junction j : junctionAux) {
							if (j.getColor() != null) 
								g.setColor(j.getColor());
							else
								g.setColor(colorJunctions);
							int xp = getZoomValue(j.getX());
							int yp = getZoomValue(j.getY());
							int []sit=j.getSituation();
							for (int i=0; i<sit.length; i++) {
								int x1 = getZoomValue((int)(j.getR_e()*Math.cos(Math.toRadians(sit[i]))));
								int y1 = getZoomValue((int)(j.getR_e()*Math.sin(Math.toRadians(sit[i]))));
								g.drawLine(xp, yp, xp + x1, yp - y1);
								if (emphasizeJuntions) {
									g.drawLine(xp + 1, yp, xp + x1 + 1, yp - y1);
									g.drawLine(xp, yp + 1, xp + x1, yp - y1 + 1);
								}
							}
						}
					}
				}
			}
		} catch (JIPException exc) {logger.error(exc);}
		g.setColor(colorCurrentPolygon);
		ArrayList<Point2D> listp = currentPolygon.getData();
		for (int i = 0; i < currentPolygon.length() - 1; i++) {
			int x0 = getZoomValue(listp.get(i).getX());
			int y0 = getZoomValue(listp.get(i).getY());
			int x1 = getZoomValue(listp.get(i+1).getX());
			int y1 = getZoomValue(listp.get(i+1).getY());
			g.drawLine(x0, y0, x1, y1);
			if (emphasizePolygons) {
				g.drawLine(x0 + 1, y0, x1 + 1, y1);
				g.drawLine(x0, y0 + 1, x1, y1 + 1);
			}
		}
		if (geoState && action == GeomAction.SELECT && mode == ModeGeom.SEGMENT && showSegments) {
			if (selectedSegment != null) {
				g.setColor(currentColorLine);
				g.drawLine(selectedSegment.getBegin().getX(), selectedSegment.getBegin().getY(), 
						selectedSegment.getEnd().getX(), selectedSegment.getEnd().getY());
				g.drawLine(selectedSegment.getBegin().getX() + 1, selectedSegment.getBegin().getY(), 
						selectedSegment.getEnd().getX() + 1, selectedSegment.getEnd().getY());
				g.drawLine(selectedSegment.getBegin().getX(), selectedSegment.getBegin().getY() + 1, 
						selectedSegment.getEnd().getX(), selectedSegment.getEnd().getY() + 1);
			}
		}
		if (geoState && action == GeomAction.SELECT && mode == ModeGeom.JUNCTION && showJuntions) {
			if (selectedJunction != null) {
				g.setColor(currentColorLine);
				int xp = getZoomValue(selectedJunction.getX());
				int yp = getZoomValue(selectedJunction.getY());
				int []sit=selectedJunction.getSituation();
				for (int i=0; i<sit.length; i++) {
					int x1 = getZoomValue((int)(selectedJunction.getR_e()*Math.cos(Math.toRadians(sit[i]))));
					int y1 = getZoomValue((int)(selectedJunction.getR_e()*Math.sin(Math.toRadians(sit[i]))));
					g.drawLine(xp, yp, xp + x1, yp - y1);
					g.drawLine(xp + 1, yp, xp + x1 + 1, yp - y1);
					g.drawLine(xp, yp + 1, xp + x1, yp - y1 + 1);
				}
			}
		}
		if (geoState && action == GeomAction.SELECT && mode == ModeGeom.POLYGON && showPolygons) {
			if (selectedPolygon != null) {
				g.setColor(currentColorLine);
				int xpoint[] = new int[selectedPolygon.length()];
				int ypoint[] = new int[selectedPolygon.length()];
				ArrayList<Point2D> listaux = selectedPolygon.getData();
				for (int j = 0; j < listaux.size(); j++) {
					xpoint[j] = getZoomValue(listaux.get(j).getX());
					ypoint[j] = getZoomValue(listaux.get(j).getY());
				}
	
				g.drawPolygon(xpoint, ypoint, xpoint.length);
				for (int j = 0; j < xpoint.length; j++)
					xpoint[j]++;
				g.drawPolygon(xpoint, ypoint, xpoint.length);
				for (int j = 0; j < xpoint.length; j++) {
					ypoint[j]++;
					xpoint[j]--;
				}
				g.drawPolygon(xpoint, ypoint, xpoint.length);
			}
		}
		if (geoState && action == GeomAction.SELECT && mode == ModeGeom.POINT && showPoints) {
			if (selectedPoint != null) {
				g.setColor(colorCurrentPoint);
				g.drawOval(selectedPoint.getX() - 5, selectedPoint.getY() - 5, 10, 10);
			}
		}
	}
	
	public void mousePressed(MouseEvent evt) {   
		requestFocus();
	}   
	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
	public void mouseReleased(MouseEvent evt) {}
	public void mouseClicked(MouseEvent evt) {}
	// Event controllers
	public void keyPressed(KeyEvent evt) {}
	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {  
		int inc_x=0, inc_y=0;
		char key = evt.getKeyChar();
		int cc=evt.getModifiers();
		if (key == 'a' || key == 'A') {
			inc_x=-1; inc_y=0;
		}
		if (key == 's' || key == 'S') {
			if (cc == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
				gui2d.actions.save();
			}
			else {
				inc_x=1; inc_y=0;
			}
		}
		if (key == 'q' || key == 'Q') {
			if (cc == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
				gui2d.actions.exit();
			}
			else {
				inc_x=0; inc_y=-1;
			}
		}
		if (key == 'z' || key == 'Z') {
			if (cc == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
				gui2d.actions.undo();
			}
			else {
				inc_x=0; inc_y=1;
			}
		}
		try {
			if (key == 'm' || key == 'M') {
				if (cc == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
					zoomWindow(200);
				}
			}
			if (key == 'l' || key == 'L') {
				if (cc == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
					zoomWindow(50);
				}
			}
			if (key == 'r' || key == 'R') {
				if (cc == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
					gui2d.actions.redo();
				}
			}
			Point p=getLocationOnScreen();
			Robot rob = new Robot();
			rob.mouseMove(p.x+posMouse.getX()+inc_x, p.y+posMouse.getY()+inc_y);
		}catch (Exception e) {logger.error(e);}
	}
	
	public void mouseMoved(MouseEvent evt) {
		posMouse=new Point2D(evt.getX(), evt.getY());
	}
	public void mouseDragged(MouseEvent evt) {}

}
