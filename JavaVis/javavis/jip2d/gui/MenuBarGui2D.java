package javavis.jip2d.gui;

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Properties;

import javavis.Commons;
import javavis.jip2d.base.FunctionList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Class which has the menu bar of the main program. Here is created the
 * upper menu, with all its deployed menus.
 */
public class MenuBarGui2D extends JMenuBar {
	public static final long serialVersionUID = 8044804452084319776L;

	/**
	 * @uml.property  name="nnew"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem nnew;
	/**
	 * @uml.property  name="open"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem open;
	/**
	 * @uml.property  name="capture"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem capture;
	/**
	 * @uml.property  name="save"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem save;
	/**
	 * @uml.property  name="saveas"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem saveas;
	/**
	 * @uml.property  name="savejpg"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem savejpg;
	/**
	 * @uml.property  name="exit"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem exit;
	/**
	 * @uml.property  name="zoom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem zoom;
	/**
	 * @uml.property  name="moreZoom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem moreZoom;
	/**
	 * @uml.property  name="lessZoom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem lessZoom;
	/**
	 * @uml.property  name="osize"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem osize;
	/**
	 * @uml.property  name="bcolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem bcolor;
	/**
	 * @uml.property  name="pcolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem pcolor;
	/**
	 * @uml.property  name="scolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem scolor;
	/**
	 * @uml.property  name="cscolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem cscolor;
	/**
	 * @uml.property  name="pointcolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem pointcolor;
	/**
	 * @uml.property  name="cpointcolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem cpointcolor;
	/**
	 * @uml.property  name="polycolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem polycolor;
	/**
	 * @uml.property  name="cpolycolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem cpolycolor;
	/**
	 * @uml.property  name="edgecolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem edgecolor;
	/**
	 * @uml.property  name="junctioncolor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem junctioncolor;
	/**
	 * @uml.property  name="hpoint"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem hpoint;
	/**
	 * @uml.property  name="hsegment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem hsegment;
	/**
	 * @uml.property  name="hpolygon"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem hpolygon;
	/**
	 * @uml.property  name="hedge"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem hedge;
	/**
	 * @uml.property  name="hjunction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem hjunction;
	/**
	 * @uml.property  name="vbitmap"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vbitmap;
	/**
	 * @uml.property  name="vsegment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vsegment;
	/**
	 * @uml.property  name="vpoint"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vpoint;
	/**
	 * @uml.property  name="vedge"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vedge;
	/**
	 * @uml.property  name="vpolygon"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vpolygon;
	/**
	 * @uml.property  name="vgeom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vgeom;

	/**
	 * @uml.property  name="vbottom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem vbottom;
	/**
	 * @uml.property  name="datasegment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem datasegment;
	/**
	 * @uml.property  name="iscale"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem iscale;
	/**
	 * @uml.property  name="slsegment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem slsegment;
	/**
	 * @uml.property  name="sl2points"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem sl2points;
	/**
	 * @uml.property  name="sselseg"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem sselseg;
	/**
	 * @uml.property  name="units"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem units;
	/**
	 * @uml.property  name="undo"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem undo;
	/**
	 * @uml.property  name="redo"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem redo;
	/**
	 * @uml.property  name="renameseq"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem renameseq;
	/**
	 * @uml.property  name="renameframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem renameframe;
	/**
	 * @uml.property  name="dupframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem dupframe;
	/**
	 * @uml.property  name="addframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem addframe;
	/**
	 * @uml.property  name="delframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem delframe;
	/**
	 * @uml.property  name="addbands"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem addbands;
	/**
	 * @uml.property  name="delband"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem delband;
	/**
	 * @uml.property  name="extband"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem extband;
	/**
	 * @uml.property  name="importascii"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem importascii;
	/**
	 * @uml.property  name="expascii"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem expascii;
	/**
	 * @uml.property  name="dellsegment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem dellsegment;
	/**
	 * @uml.property  name="dellpoint"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem dellpoint;
	/**
	 * @uml.property  name="delpoly"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem delpoly;
	/**
	 * @uml.property  name="delselec"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem delselec;
	/**
	 * @uml.property  name="addgeom"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem addgeom;
	/**
	 * @uml.property  name="addvoidgeom"
	 * @uml.associationEnd  readOnly="true"
	 */
	public JMenuItem addvoidgeom;
	/**
	 * @uml.property  name="segmentframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem segmentframe;
	/**
	 * @uml.property  name="pointframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem pointframe;
	/**
	 * @uml.property  name="polyframe"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem polyframe;
	/**
	 * @uml.property  name="emptyall"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem emptyall;
	/**
	 * @uml.property  name="emptypoints"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem emptypoints;
	/**
	 * @uml.property  name="emptysegments"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem emptysegments;
	/**
	 * @uml.property  name="emptypoly"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem emptypoly;
	/**
	 * @uml.property  name="about"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem about;
	/**
	 * @uml.property  name="help"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem help;

	/**
	 * Class constructor.
	 * @param listener ActionListener
	 * @param funclist2d List of aplicable functions which we have to put in the menu
	 * @param lastAction Last action which can be undoed to put its name in the
	 * undo option.
	 */
	public MenuBarGui2D(ActionListener listener, FunctionList funclist,
			Properties prop) {
		JMenu menu = new JMenu(prop.getProperty("File"));
		add(menu);

		nnew = new JMenuItem(prop.getProperty("New"), Commons.getIcon("new.jpg"));
		nnew.addActionListener(listener);
		menu.add(nnew);

		open = new JMenuItem(prop.getProperty("Open"), Commons.getIcon("open.jpg"));
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		open.addActionListener(listener);
		menu.add(open);

		capture= new JMenuItem(prop.getProperty("Capture"), Commons.getIcon("capture.gif"));
		capture.addActionListener(listener);
		menu.add(capture);

		save = new JMenuItem(prop.getProperty("Save"), Commons.getIcon("guardar_ascii.jpg"));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		save.addActionListener(listener);
		menu.add(save);

		saveas = new JMenuItem(prop.getProperty("SaveAs"), Commons.getIcon("guardar_ascii.jpg"));
		saveas.addActionListener(listener);
		menu.add(saveas);

		savejpg = new JMenuItem(prop.getProperty("SaveJPG"), Commons.getIcon("guardar_ascii.jpg"));
		savejpg.addActionListener(listener);
		menu.add(savejpg);

		menu.addSeparator();

		exit = new JMenuItem(prop.getProperty("Exit"), Commons.getIcon("salir.jpg"));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		exit.addActionListener(listener);
		menu.add(exit);

		menu = new JMenu(prop.getProperty("Appearance"));
		add(menu);

		zoom = new JMenuItem(prop.getProperty("Zoom"), Commons.getIcon("zoom.jpg"));
		zoom.addActionListener(listener);
		menu.add(zoom);

		moreZoom = new JMenuItem(prop.getProperty("moreZoom"));
		moreZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		moreZoom.addActionListener(listener);
		menu.add(moreZoom);

		lessZoom = new JMenuItem(prop.getProperty("lessZoom"));
		lessZoom.addActionListener(listener);
		lessZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(lessZoom);

		osize = new JMenuItem(prop.getProperty("OrigSize"));
		osize.addActionListener(listener);
		menu.add(osize);

		menu.addSeparator();

		bcolor = new JMenuItem(prop.getProperty("BackColor"));
		bcolor.addActionListener(listener);
		menu.add(bcolor);

		pcolor = new JMenuItem(prop.getProperty("PanelColor"));
		pcolor.addActionListener(listener);
		menu.add(pcolor);

		scolor = new JMenuItem(prop.getProperty("SegmentColor"));
		scolor.addActionListener(listener);
		menu.add(scolor);

		cscolor = new JMenuItem(prop.getProperty("CurrentSegmentColor"));
		cscolor.addActionListener(listener);
		menu.add(cscolor);

		pointcolor = new JMenuItem(prop.getProperty("PointColor"));
		pointcolor.addActionListener(listener);
		menu.add(pointcolor);

		cpointcolor = new JMenuItem(prop.getProperty("CurrentPointColor"));
		cpointcolor.addActionListener(listener);
		menu.add(cpointcolor);

		polycolor = new JMenuItem(prop.getProperty("PolygonColor"));
		polycolor.addActionListener(listener);
		menu.add(polycolor);

		cpolycolor = new JMenuItem(prop.getProperty("CurrentPolygonColor"));
		cpolycolor.addActionListener(listener);
		menu.add(cpolycolor);

		edgecolor = new JMenuItem(prop.getProperty("EdgeColor"));
		edgecolor.addActionListener(listener);
		menu.add(edgecolor);

		junctioncolor = new JMenuItem(prop.getProperty("JunctionColor"));
		junctioncolor.addActionListener(listener);
		menu.add(junctioncolor);

		menu.addSeparator();

		hsegment = new JCheckBoxMenuItem(prop.getProperty("HighlightSegment"), false);
		hsegment.addActionListener(listener);
		menu.add(hsegment);

		hpoint = new JCheckBoxMenuItem(prop.getProperty("HighlightPoint"), false);
		hpoint.addActionListener(listener);
		menu.add(hpoint);

		hpolygon = new JCheckBoxMenuItem(prop.getProperty("HighlightPolygon"), true);
		hpolygon.addActionListener(listener);
		menu.add(hpolygon);

		hedge = new JCheckBoxMenuItem(prop.getProperty("HighlightEdge"), true);
		hedge.addActionListener(listener);
		menu.add(hedge);

		hjunction = new JCheckBoxMenuItem(prop.getProperty("HighlightJunction"), true);
		hjunction.addActionListener(listener);
		menu.add(hjunction);

		menu = new JMenu(prop.getProperty("View"));
		add(menu);

		vbitmap = new JCheckBoxMenuItem(prop.getProperty("ViewBitmap"), true);
		vbitmap.addActionListener(listener);
		menu.add(vbitmap);

		vsegment = new JCheckBoxMenuItem(prop.getProperty("ViewSegment"), true);
		vsegment.addActionListener(listener);
		menu.add(vsegment);

		vpoint = new JCheckBoxMenuItem(prop.getProperty("ViewPoint"), true);
		vpoint.addActionListener(listener);
		menu.add(vpoint);

		vpolygon = new JCheckBoxMenuItem(prop.getProperty("ViewPolygon"), true);
		vpolygon.addActionListener(listener);
		menu.add(vpolygon);

		vedge = new JCheckBoxMenuItem(prop.getProperty("ViewEdge"), true);
		vedge.addActionListener(listener);
		menu.add(vedge);

		menu.addSeparator();

		vgeom = new JCheckBoxMenuItem(prop.getProperty("ViewGeometry"), false);
		vgeom.addActionListener(listener);
		menu.add(vgeom);

		vbottom = new JCheckBoxMenuItem(prop.getProperty("ViewInfoBottom"), true);
		vbottom.addActionListener(listener);
		menu.add(vbottom);

		menu = new JMenu(prop.getProperty("Scale"));
		add(menu);

		iscale = new JMenuItem(prop.getProperty("IntroduceScale"));
		iscale.addActionListener(listener);
		menu.add(iscale);

		slsegment = new JMenuItem(prop.getProperty("ScaleLastSegment"));
		slsegment.addActionListener(listener);
		menu.add(slsegment);

		sl2points = new JMenuItem(prop.getProperty("ScaleLast2Points"));
		sl2points.addActionListener(listener);
		menu.add(sl2points);

		sselseg = new JMenuItem(prop.getProperty("ScaleSelectedSegment"));
		sselseg.addActionListener(listener);
		menu.add(sselseg);

		units = new JMenuItem(prop.getProperty("Units"));
		units.addActionListener(listener);
		menu.add(units);

		menu = funclist.getFunctionMenu(prop.getProperty("Functions"), listener);
		add(menu);

		menu = new JMenu(prop.getProperty("Sequence"));
		add(menu);

		undo = new JMenuItem(prop.getProperty("Undo"), Commons.getIcon("undo.jpg"));
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		undo.addActionListener(listener);
		menu.add(undo);

		redo = new JMenuItem(prop.getProperty("Redo"), Commons.getIcon("redo.jpg"));
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		redo.addActionListener(listener);
		menu.add(redo);
		menu.addSeparator();

		renameseq = new JMenuItem(prop.getProperty("RenameSequence"));
		renameseq.addActionListener(listener);
		menu.add(renameseq);

		renameframe = new JMenuItem(prop.getProperty("RenameFrame"));
		renameframe.addActionListener(listener);
		menu.add(renameframe);
		menu.addSeparator();

		dupframe = new JMenuItem(prop.getProperty("DuplicateFrame"));
		dupframe.addActionListener(listener);
		menu.add(dupframe);

		addframe = new JMenuItem(prop.getProperty("AddFrames"));
		addframe.addActionListener(listener);
		menu.add(addframe);

		delframe = new JMenuItem(prop.getProperty("DeleteFrame"));
		delframe.addActionListener(listener);
		menu.add(delframe);
		menu.addSeparator();

		addbands = new JMenuItem(prop.getProperty("AddBands"));
		addbands.addActionListener(listener);
		menu.add(addbands);

		delband = new JMenuItem(prop.getProperty("DeleteBand"));
		delband.addActionListener(listener);
		menu.add(delband);

		extband = new JMenuItem(prop.getProperty("ExtractBand"));
		extband.addActionListener(listener);
		menu.add(extband);
		menu.addSeparator();

		menu = new JMenu(prop.getProperty("Geometry"));
		add(menu);

		datasegment = new JMenuItem(prop.getProperty("SegmentData"));
		datasegment.addActionListener(listener);
		menu.add(datasegment);

		importascii = new JMenuItem(prop.getProperty("ImportASCII"));
		importascii.addActionListener(listener);
		menu.add(importascii);

		expascii = new JMenuItem(prop.getProperty("ExportASCII"));
		expascii.addActionListener(listener);
		menu.add(expascii);

		menu.addSeparator();
		dellsegment = new JMenuItem(prop.getProperty("DeleteLastSegment"));
		dellsegment.addActionListener(listener);
		menu.add(dellsegment);

		dellpoint = new JMenuItem(prop.getProperty("DeleteLastPoint"));
		dellpoint.addActionListener(listener);
		menu.add(dellpoint);

		delpoly = new JMenuItem(prop.getProperty("DeletePolygon"));
		delpoly.addActionListener(listener);
		menu.add(delpoly);

		delselec = new JMenuItem(prop.getProperty("DeleteSelected"));
		delselec.addActionListener(listener);
		menu.add(delselec);
		menu.addSeparator();

		addgeom = new JMenuItem(prop.getProperty("AddGeometrySequence"));
		addgeom.addActionListener(listener);
		menu.add(addgeom);

		JMenu add2 = new JMenu(prop.getProperty("AddVoidGeometricFrame"));

		pointframe = new JMenuItem(prop.getProperty("PointFrame"));
		pointframe.addActionListener(listener);
		add2.add(pointframe);

		segmentframe = new JMenuItem(prop.getProperty("SegmentFrame"));
		segmentframe.addActionListener(listener);
		add2.add(segmentframe);

		polyframe = new JMenuItem(prop.getProperty("PolygonFrame"));
		polyframe.addActionListener(listener);
		add2.add(polyframe);

		menu.add(add2);

		add2 = new JMenu(prop.getProperty("EmptyGeometry"));

		emptyall = new JMenuItem(prop.getProperty("EmptyAll"));
		emptyall.addActionListener(listener);
		add2.add(emptyall);

		emptypoints = new JMenuItem(prop.getProperty("EmptyPoints"));
		emptypoints.addActionListener(listener);
		add2.add(emptypoints);

		emptysegments = new JMenuItem(prop.getProperty("EmptySegments"));
		emptysegments.addActionListener(listener);
		add2.add(emptysegments);

		emptypoly = new JMenuItem(prop.getProperty("EmptyPolygons"));
		emptypoly.addActionListener(listener);
		add2.add(emptypoly);

		menu.add(add2);

		menu = new JMenu(prop.getProperty("Help"));
		add(menu);

		help = new JMenuItem(prop.getProperty("Help"));
		help.setAccelerator(KeyStroke.getKeyStroke("F1"));
		help.addActionListener(listener);
		menu.add(help);
		about = new JMenuItem(prop.getProperty("About"));
		about.addActionListener(listener);
		menu.add(about);
	}
}
