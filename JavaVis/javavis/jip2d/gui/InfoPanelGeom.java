package javavis.jip2d.gui;

import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;

import javavis.jip2d.base.geometrics.Junction;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.base.geometrics.Segment;

import javax.swing.*;

/**
 * Class which has the information about the elements of the
 * program, the cursor position, its value, band and numFrame where
 * we are working etc...
 */
public class InfoPanelGeom extends JPanel {
	private static final long serialVersionUID = 5263348773283877531L;

	/**
	 * The canvas
	 * @uml.property  name="canvas"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="infoGeom:javavis.jip2d.gui.Canvas2D"
	 */
	Canvas2D canvas;

	/**
	 * Number of segments
	 * @uml.property  name="nSegment"
	 */
	int nSegment = 0;
	
	/**
	 * Initial x
	 * @uml.property  name="xini"
	 */
	int xini;
	
	/**
	 * Initial y
	 * @uml.property  name="yini"
	 */
	int yini;

	/**
	 * Segments label
	 * @uml.property  name="segments"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel segments;

	/**
	 * Points label
	 * @uml.property  name="points"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel points;

	/**
	 * Geometry label
	 * @uml.property  name="geom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel geom;

	/**
	 * Polygons label
	 * @uml.property  name="polygons"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel polygons;

	/**
	 * Start selection label
	 * @uml.property  name="secondLine"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel secondLine;

	/**
	 * End selection label
	 * @uml.property  name="thirdLine"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel thirdLine;

	/**
	 * Segment length label
	 * @uml.property  name="fourthLine"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel fourthLine;

	/**
	 * Type of selection label
	 * @uml.property  name="firstLine"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel firstLine;

	/**
	 * Buttons for select or add geometrical objects
	 * @uml.property  name="bpoint"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JRadioButton bpoint;
	/**
	 * Buttons for select or add geometrical objects
	 * @uml.property  name="bsegment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JRadioButton bsegment;
	/**
	 * Buttons for select or add geometrical objects
	 * @uml.property  name="bpoly"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JRadioButton bpoly;
	/**
	 * Buttons for select or add geometrical objects
	 * @uml.property  name="bjunc"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JRadioButton bjunc;
	
	/**
	 * @uml.property  name="actionSel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JComboBox actionSel;

	/**
	 * String which shows the units
	 * @uml.property  name="unit"
	 */
	String unit;
	
	/**
	 * Scale
	 * @uml.property  name="scale"
	 */
	float scale = 1;

	/**
	 * Number format
	 * @uml.property  name="nf"
	 */
	NumberFormat nf;
	
	/**
	 * @uml.property  name="prop"
	 */
	Properties prop;
	
	/**
	 * Indicates if we are selecting or adding geometry
	 */
	public enum GeomAction {/**
	 * @uml.property  name="sELECT"
	 * @uml.associationEnd  
	 */
	SELECT, /**
	 * @uml.property  name="aDD"
	 * @uml.associationEnd  
	 */
	ADD}

	/**
	* 	Class constructor. It creates all the information panel on the left in 
	* the main program window.
	* @param c Geometric canvas
	*/
	public InfoPanelGeom(Canvas2D c, Properties propi) {
		canvas = c;
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		prop = propi;

		setBorder(BorderFactory.createEtchedBorder());

		unit = prop.getProperty("Pixels");
		segments = new JLabel("# "+prop.getProperty("Segments")+":");
		points = new JLabel("# "+prop.getProperty("Points")+" :");
		polygons = new JLabel("# "+prop.getProperty("Polygons")+" :");
		geom = new JLabel(prop.getProperty("Geometry")+":");

		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		Spring yPad = Spring.constant(1);
		Spring xSpring = Spring.constant(5);
		Spring ySpring = yPad;
		Spring maxWidthSpring = Spring.constant(0);

		SpringLayout.Constraints cons;

		add(segments);
		cons = layout.getConstraints(segments);
		cons.setX(xSpring);
		ySpring = Spring.constant(10); 
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));

		add(points);
		cons = layout.getConstraints(points);
		cons.setX(xSpring);
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));

		add(polygons);
		cons = layout.getConstraints(polygons);
		cons.setX(xSpring);
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));

		add(geom);
		cons = layout.getConstraints(geom);
		cons.setX(xSpring);
		ySpring = Spring.sum(ySpring, Spring.constant(10)); 
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));

		JPanel auxPanelMode = new JPanel();
		auxPanelMode.setLayout(new BoxLayout(auxPanelMode, BoxLayout.Y_AXIS));
		auxPanelMode.setBorder(BorderFactory.createTitledBorder(" "+
				prop.getProperty("Mode")));

		ButtonGroup geoTipo = new ButtonGroup();
		
		bsegment = new JRadioButton(prop.getProperty("Segments"), true);
		bsegment.addActionListener(canvas);
		auxPanelMode.add(bsegment);
		geoTipo.add(bsegment);

		bpoint = new JRadioButton(prop.getProperty("Points"));
		bpoint.addActionListener(canvas);
		auxPanelMode.add(bpoint);
		geoTipo.add(bpoint);

		bpoly = new JRadioButton(prop.getProperty("Polygons"));
		bpoly.addActionListener(canvas);
		auxPanelMode.add(bpoly);
		geoTipo.add(bpoly);

		bjunc = new JRadioButton(prop.getProperty("JunctionAux"));
		bjunc.addActionListener(canvas);
		auxPanelMode.add(bjunc);
		geoTipo.add(bjunc);
		
		add(auxPanelMode);
		cons = layout.getConstraints(auxPanelMode);
		cons.setX(xSpring);
		ySpring = Spring.sum(ySpring, Spring.constant(10)); 
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));
		//End of RadioButton
		
		actionSel = new JComboBox();
		actionSel.addItem(prop.getProperty("Select"));
		actionSel.addItem(prop.getProperty("Add"));
		actionSel.setSelectedIndex(GeomAction.SELECT.ordinal());
		actionSel.addActionListener(canvas);
		actionSel.setMaximumSize(new Dimension(125, 50));
		
		JPanel auxPanelAction = new JPanel();
		auxPanelAction.setLayout(new BoxLayout(auxPanelAction, BoxLayout.Y_AXIS));
		auxPanelAction.setBorder(BorderFactory.createTitledBorder(prop.getProperty("Action")));
		auxPanelAction.setMaximumSize(new Dimension(125, 50));
		auxPanelAction.add(actionSel);
		
		add(auxPanelAction);
		cons = layout.getConstraints(auxPanelAction);
		cons.setX(xSpring);
		ySpring = Spring.sum(ySpring, Spring.constant(10)); 
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));

		JPanel auxPanelSelection = new JPanel();
		auxPanelSelection.setLayout(new BoxLayout(auxPanelSelection, BoxLayout.Y_AXIS));
		auxPanelSelection.setBorder(BorderFactory.createTitledBorder(prop.getProperty("Selection")));
		auxPanelSelection.setMaximumSize(new Dimension(0,0));

		firstLine = new JLabel(" "); 
		auxPanelSelection.add(firstLine);

		secondLine = new JLabel(" ");
		auxPanelSelection.add(secondLine);
		
		thirdLine = new JLabel(" ");
		auxPanelSelection.add(thirdLine);

		fourthLine = new JLabel(" ");
		auxPanelSelection.add(fourthLine);
		
		add(auxPanelSelection);
		cons = layout.getConstraints(auxPanelSelection);
		cons.setX(xSpring);
		ySpring = Spring.sum(ySpring, Spring.constant(10)); 
		cons.setY(ySpring);
		ySpring = Spring.sum(yPad, cons.getConstraint("South"));
		maxWidthSpring = Spring.max(maxWidthSpring, cons.getConstraint("East"));
		
		cons = layout.getConstraints(this);
		cons.setConstraint("East", maxWidthSpring);
		cons.setConstraint("South", ySpring);

		updateSelVoid();
	}

	/**
	 * Method which changes the actual scale for another that it receive as parameter
	 * @param esc New value of the scale
	 */
	public void changeScale(float esc) {
		scale = esc;
	}

	/**
	 * Method which changes the actual units for another that it receive as parameter
	 * @param esc New value of the units
	 */
	public void changeUnits(String uni) {
		if (uni.length() > 15)
			uni = uni.substring(0, 15);
		unit = uni;
	}

	/**
	 * Method which updates the state of the geometry which can be enable or
	 * disable
	 * @param geoEst Showing or not showing geometry (enable/disable)
	 */
	public void updateGeo(boolean geoEst) {
			geom.setText(prop.getProperty("Geometry")+(geoEst?": On":": Off"));
	}

	/**
	 * Method which updates the information of the panel, it can be the width,
	 * the height, number of segments, image, bands, etc...
	 * @param numSeg Number of segments
	 * @param numPoint Number of points
	 * @param numPoly  Number of polygons
	 */
	public void updateInfo(int numSeg, int numPoint, int numPoly) {
		segments.setText("# "+prop.getProperty("Segments")+": " + numSeg);
		points.setText("# "+prop.getProperty("Points")+": " + numPoint);
		polygons.setText("# "+prop.getProperty("Polygons")+": " + numPoly);
	}

	/**
	 * Method which updates the panel section which corresponds to the selection.
	 * @param p Point2D
	 */
	public void updateSelPoint(Point2D p) {
		firstLine.setText(prop.getProperty("Type")+": Point");
		secondLine.setText("Coords: (" + p.getX() + "," + p.getY() + ")");
	}
	
	public void updateSelSegment(Segment s) {
		firstLine.setText(prop.getProperty("Type")+": Segment");
		secondLine.setText(prop.getProperty("Start")+": (" + s.getBegin().getX() + "," + s.getBegin().getY() + ")");
		thirdLine.setText(prop.getProperty("End")+": (" + s.getEnd().getX() + "," + s.getEnd().getY() + ")");
		fourthLine.setText(prop.getProperty("Length")+": "+ nf.format(scale * dist(s.getBegin(), s.getEnd())) + " " + unit);
	}
	
	public void updateSelJunction(Junction j) {
		firstLine.setText(prop.getProperty("Type")+": JunctionAux");
		secondLine.setText(prop.getProperty("Center")+":(" + j.getX() + "," + j.getY() + ")");
		int []limits=j.getSituation();	
		thirdLine.setText(prop.getProperty("Limits")+": [" + limits[0] + " " + limits[1] + (limits.length>2?" "+limits[2]:"]")+(limits.length==3?"]":""));
		if (limits.length>3) {
			fourthLine.setText(limits[3] + (limits.length>4?" "+limits[4]+(limits.length>5?limits[5]:""):"")+"]");
		}
	}
	
	public void updateSelVoid() {
		firstLine.setText("Nothing selected ");
		secondLine.setText(" ");
		thirdLine.setText(" ");
		fourthLine.setText(" ");
	}
	
	public void updateSelPolygon(Polygon2D p) {
		ArrayList<Point2D> points = p.getData();
		firstLine.setText(prop.getProperty("Type")+":  Polygon");
		secondLine.setText(prop.getProperty("Start")+":  (" + points.get(0).getX() + "," + points.get(0).getY() + ")");
		double l=0.0;
		Point2D current=points.get(0);
		for (int i=1; i<points.size(); i++) {
			Point2D next=points.get(i);
			l += dist(current, next);
			current=next;
		}
		l += dist(current, points.get(0));
		thirdLine.setText("Number of points:  "+ p.getData().size());
		fourthLine.setText(prop.getProperty("Length")+":  "+ nf.format(l));
	}
	
	private double dist(Point2D p1, Point2D p2) {
		return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2.0)+ Math.pow(p1.getY()-p2.getY(), 2.0));
	}
	
}
