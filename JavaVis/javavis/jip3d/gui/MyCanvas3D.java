package javavis.jip3d.gui;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javavis.base.JIPException;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.ImageSet3D;
import javavis.jip3d.gui.dataobjects.SceneModel;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BadTransformException;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Locale;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
//Uncomment next line for zoomming camera by mouse middle button
//import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Class MyCanvas3D
 */
public class MyCanvas3D extends Canvas3D {

	private static final long serialVersionUID = 4750736186582863505L;

	/**
	 * @uml.property  name="myLocale"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Locale myLocale;
	/**
	 * @uml.property  name="myBranchGroup"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public BranchGroup myBranchGroup;
	/**
	 * @uml.property  name="myViewingPlatform"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	ViewingPlatform myViewingPlatform;
	/**
	 * @uml.property  name="view"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	View view;
	/**
	 * @uml.property  name="tgView"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public TransformGroup tgView;
	/**
	 * @uml.property  name="globalx"
	 */
	double globalx;
	/**
	 * @uml.property  name="globaly"
	 */
	double globaly;
	/**
	 * @uml.property  name="globalz"
	 */
	double globalz;
	/**
	 * @uml.property  name="orx"
	 */
	double orx;
	/**
	 * @uml.property  name="ory"
	 */
	double ory;
	/**
	 * @uml.property  name="orz"
	 */
	double orz;
	/**
	 * @uml.property  name="menu"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="canvas:javavis.jip3d.gui.Gui3D"
	 */
	Gui3D menu;
	/**
	 * @uml.property  name="scr_data"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip3d.gui.ScreenData"
	 */
	ArrayList <ScreenData> scr_data;
	/**
	 * @uml.property  name="func_panel"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="canvas:javavis.jip3d.gui.FunctionPanel"
	 */
	FunctionPanel func_panel;
	/**
	 * @uml.property  name="DEBUG"
	 */
	boolean DEBUG;
    /**
	 * @uml.property  name="prop"
	 */
    Properties prop;

	public MyCanvas3D(GraphicsConfiguration arg0, Gui3D parent, Properties pr, boolean debug) {
		super(arg0);
		scr_data = new ArrayList<ScreenData>();
		menu = parent;

		DEBUG = debug;
		prop = pr;

		//Create the list of functions
        func_panel = new FunctionPanel(this);

		//Defined the universe and the locale
		VirtualUniverse myUniverse = new VirtualUniverse();
		myLocale = new Locale(myUniverse);
		view = buildView();
		construirRamaVista(view, new Vector3f(0.0f, 0.0f, 5.0f)); //0.85
		changeToRobotView();
		menu.poseUpdate(globalx, globaly, globalz, orx, ory, orz);
		this.setSize(640, 480);
		updateView();

		this.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e) {
				updateView();
				if(menu!=null)
					menu.poseUpdate(globalx, globaly, globalz, orx, ory, orz);
			}

			public void keyReleased(KeyEvent e) {
				updateView();
				if(menu!=null)
					menu.poseUpdate(globalx, globaly, globalz, orx, ory, orz);
			}

			public void keyTyped(KeyEvent e) {
			}
		});
	}
	
	public Gui3D getGui3D () {
		return menu;
	}
	
	public Properties getProp () {
		return prop;
	}

	private View buildView()
	{
		View myview = new View();
		myview.addCanvas3D(this);
		PhysicalBody mybody = new PhysicalBody();

		myview.setPhysicalBody(mybody);

		myview.setPhysicalEnvironment(new PhysicalEnvironment());
		myview.setBackClipDistance(5000.0f);
		myview.setFrontClipDistance(0.001f);
		return(myview);
	}

	/**
	 * Build the view branch. It is the bridge between the view and the universe.
	 */
	private void construirRamaVista(View miview, Vector3f position)
	{
		myBranchGroup = new BranchGroup();
		myBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		myBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		myBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		TransformGroup TGV = new TransformGroup();
		//Delete
		TransformGroup tgaux = new TransformGroup();
		TransformGroup tgauxX;
		TransformGroup tgauxY;
		TransformGroup tgauxZ;
		Transform3D tr3d;
		Appearance app = new Appearance();
		ColoringAttributes ca;
		BranchGroup bgaux = new BranchGroup();
		if(DEBUG)
		{
			Cylinder cil;
			cil = new Cylinder(0.25f, 0.5f);
			tgaux.addChild(cil);
			bgaux.addChild(tgaux);
			tr3d = new Transform3D();
			tr3d.setTranslation(new Vector3d(5,0,0));
			tgauxX = new TransformGroup(tr3d);
			cil = new Cylinder(0.25f, 0.5f);
			ca = new ColoringAttributes(1,0,0,ColoringAttributes.SHADE_GOURAUD);
			app.setColoringAttributes(ca);
			cil.setAppearance(app);
			tgauxX.addChild(cil);
			bgaux.addChild(tgauxX);
			//Y axis
			tr3d = new Transform3D();
			tr3d.setTranslation(new Vector3d(0,5,0));
			tgauxY = new TransformGroup(tr3d);
			cil = new Cylinder(0.25f, 0.5f);
			ca = new ColoringAttributes(0,1,0,ColoringAttributes.SHADE_GOURAUD);
			app = new Appearance();
			app.setColoringAttributes(ca);
			cil.setAppearance(app);
			tgauxY.addChild(cil);
			bgaux.addChild(tgauxY);
			//Z axis
			tr3d = new Transform3D();
			tr3d.setTranslation(new Vector3d(0,0,5));
			tgauxZ = new TransformGroup(tr3d);
			cil = new Cylinder(0.25f, 0.5f);
			ca = new ColoringAttributes(0,0,1,ColoringAttributes.SHADE_GOURAUD);
			app = new Appearance();
			app.setColoringAttributes(ca);
			cil.setAppearance(app);
			tgauxZ.addChild(cil);
			bgaux.addChild(tgauxZ);
			myLocale.addBranchGraph(bgaux);
		}

		//Set the ViewPlatform
		myViewingPlatform = new ViewingPlatform(3);
		myViewingPlatform.getViewPlatform().setViewAttachPolicy(View.NOMINAL_HEAD);
		TGV.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		TGV.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Transform3D t = new Transform3D();

		TGV.setTransform(t);

		tgView = myViewingPlatform.getViewPlatformTransform();

		//This 'behavior' handles the keyboard despite obstacles
		KeyNavigatorBehavior conducta = new KeyNavigatorBehavior(tgView);
		conducta.setSchedulingBounds(new BoundingSphere(new Point3d(),5000.0));

		//Add mouse interaction
		MouseTranslate mt = new MouseTranslate();
		mt.setTransformGroup(tgView);
		mt.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));

		//Uncomment next lines to zoom camera by mouse middle button
		//MouseWheelZoom mwz = new MouseWheelZoom();
		//mwz.setTransformGroup(TGVista);
		//mwz.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));

		MouseZoom mz = new MouseZoom();
		mz.setTransformGroup(tgView);
		mz.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));

		TGV.addChild(myViewingPlatform);

		myBranchGroup.addChild(TGV);

		myBranchGroup.addChild(conducta);
		myBranchGroup.addChild(mt);
		myBranchGroup.addChild(mz);

		//add a white background color
		Background bg = new Background(new Color3f(1,1,1));
		bg.setApplicationBounds(new BoundingSphere(new Point3d(), 1000.0));
		myBranchGroup.addChild(bg);

		//add the branch view to the tree
		myLocale.addBranchGraph(myBranchGroup);
		miview.attachViewPlatform(myViewingPlatform.getViewPlatform());
	}

	public void changeToCenitalView()
	{
		double [] or = {0, 1, 0};

		changeView(new Vector3f(0, 20, 0), or);
	}

	public void changeToRobotView()
	{
		double [] or = {0, 0, 1};

		changeView(new Vector3f(), or);
	}


	public void changeView(Vector3f position, double[]orientation)
	{
		Transform3D t;
		Matrix3d candidate;
		double angle;
		Vector3D v_or = new Vector3D(orientation);
		Vector3D ejeZ = new Vector3D(0, 0, 1);
		Vector3D director;

		//degenerated cases
		if(v_or.getZ()==1)
		{
			director = new Vector3D(0, 1, 0);
			angle = 0;
		}
		else if(v_or.getZ()==-1)
		{
			director = new Vector3D(0, 1, 0);
			angle = Math.PI;
		}
		else
		{
			director = ejeZ.crossProduct(v_or);
			angle = Math.asin(director.module);
			director.normalize();
		}

		double []rot_mat = director.generalRotationMatrix(angle);
		t = new Transform3D();
		candidate = new Matrix3d(rot_mat);
		t.setRotation(candidate);
		t.setTranslation(position);
		try {
			tgView.setTransform(t);
		} catch(BadTransformException e) {
			System.out.println(e.toString());
		}
		this.updateView();
		if(menu!=null)
			menu.poseUpdate(globalx, globaly, globalz, orx, ory, orz);

	}

	public void updateView()
	{
		Transform3D orig = new Transform3D();
		Vector3d vector = new Vector3d();
		Matrix3f mat = new Matrix3f();

		tgView.getTransform(orig);
		orig.get(mat, vector);
		globalx = (float)vector.x;
		globaly = (float)vector.y;
		globalz = (float)vector.z;

		orx = -mat.m02;
		ory = -mat.m12;
		orz = -mat.m22;
	}

	public void saveScreenData(ScreenData selected, String name, String path)
	{
		ScreenData obj = selected;
		IODataFile iodf = new IODataFile(name, path);
		try {
			iodf.write(obj);
		} catch (JIPException e) {
			System.out.println("Problems writing object "+obj.name+" to file "+name);
		}
	}

	public void saveJIP3D(ScreenData selected, String name, String path)
	{
		if(DEBUG)
		{
			System.out.println("Saving "+path+name);
		}
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try
		{
			fos = new FileOutputStream(path+name);
			out = new ObjectOutputStream(fos);
			out.writeObject(selected);
			out.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public void openJIP3D(String name, String path)
	{
		ScreenData data;
		if(DEBUG)
			System.out.println("Opening "+path+name);
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(path+name);
			in = new ObjectInputStream(fis);
			data = (ScreenData)in.readObject();
			data.scr_opt.is_visible = true;
			scr_data.add(data);
			createSelection();
			this.reDraw();
		} catch(IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public void addScreenData(String name, String path)
	{
		ScreenData sd=null;
		
		IODataFile iodf = new IODataFile(name, path);
		try {
			sd = iodf.read();
			sd.scr_opt.is_visible = true;
			scr_data.add(sd);
		} catch (JIPException e) {
			System.out.println("Problems reading file "+name);
			return;
		}
		createSelection();
		this.reDraw();
	}

	public void addScreen3DModel(String name, String path)
	{
		SceneModel sm = new SceneModel(new ScreenOptions());
		sm.readData(name, path);
		sm.scr_opt.is_visible = true;
		scr_data.add(sm);
		createSelection();
		this.reDraw();
	}

	/**
	 * Re-Draw all elements loaded into the 3D Scene
	 */
	public void reDraw()
	{
		BranchGroup BG;
		
		for(ScreenData it: scr_data)
		{
			if (!it.isBGCreated()) {
				it.build();
			}
			if (it.scr_opt.hasChanged) {
				BG = it.getBG();
				if (BG!=null)
					BG.detach();
				it.build();
			}
			if (it.isVisible()) {
				BG=it.getBG();
				if (!BG.isLive())
					myLocale.addBranchGraph(BG);
			}
			else
				it.detach();
		}
	}

	public void reDraw(int pos)
	{
		ScreenData elem = scr_data.get(pos);
		reDraw(elem);
	}

	public void reDraw(ScreenData elem)
	{
		if (!elem.isBGCreated()) {
			elem.build();
		}
		if (elem.scr_opt.hasChanged) {
			BranchGroup BG = elem.getBG();
			if (BG!=null)
				BG.detach();
			elem.build();
		}
		BranchGroup BG = elem.getBG();
		if(BG!=null) {
			if (elem.isVisible()) {
				if (!BG.isLive()) 
					myLocale.addBranchGraph(BG);
			}
			else
				BG.detach();
		}
	}
	
	public void reDraw (int currentTL, boolean isAccum) {
		BranchGroup BG;
		ScreenData auxSC=null;
		double closest=Double.MAX_VALUE, auxVal;
		if (isAccum) {
			for(ScreenData it: scr_data)
			{
				if (it.getType() == ScreenOptions.tIMAGESET3D) {
					it.scr_opt.is_visible=true;
					BG = it.getBG();
					if(BG!=null && !BG.isLive())
						myLocale.addBranchGraph(BG);
					((ImageSet3D)it).show(currentTL, isAccum);
				}
				else {
					if (it.isTimeStamp && it.timeStamp<=currentTL) {
						it.scr_opt.is_visible=true;
						BG = it.getBG();
						if(BG!=null && !BG.isLive())
							myLocale.addBranchGraph(BG);
					}
					else 
						it.detach();
				}
			}
		}
		else {
			for(ScreenData it: scr_data)
			{
				it.scr_opt.is_visible=false;
				it.detach();
				if (it.getType() == ScreenOptions.tIMAGESET3D) {
					it.scr_opt.is_visible=true;
					BG = it.getBG();
					if(BG!=null && !BG.isLive())
						myLocale.addBranchGraph(BG);
					((ImageSet3D)it).show(currentTL, isAccum);
				}
				else {
					if (it.isTimeStamp) {
						auxVal = Math.abs(currentTL-it.timeStamp);
						if (auxVal<closest) {
							closest=auxVal;
							auxSC=it;
						}
					}
				}
			}
			if (auxSC!=null) {
				auxSC.scr_opt.is_visible=true;
				BG = auxSC.getBG();
				if(BG!=null && !BG.isLive())
					myLocale.addBranchGraph(BG);
			}
		}
	}
	
	public int getMinTimeStamp () {
		double value=Double.MAX_VALUE, aux;
		
		for(ScreenData it: scr_data)
		{
			if (it.getType() == ScreenOptions.tIMAGESET3D) {
				aux=((ImageSet3D)it).getMinTimeStamp();
				if (aux<value)
					value=aux;
			}
			else if (it.isTimeStamp && it.timeStamp<value) {
				 value=it.timeStamp;
			}
		}
		return (int)value;
	}
	
	public int getMaxTimeStamp () {
		double value=Double.MIN_VALUE, aux;
		
		for(ScreenData it: scr_data)
		{
			if (it.getType() == ScreenOptions.tIMAGESET3D) {
				aux=((ImageSet3D)it).getMaxTimeStamp();
				if (aux>value)
					value=aux;
			}
			if (it.isTimeStamp && it.timeStamp>value) {
				 value=it.timeStamp;
			}
		}
		return (int)value;
	}
	
	public int getNumberTimeStamp () {
		int count=0;
		
		for(ScreenData it: scr_data)
		{
			if (it.getType() == ScreenOptions.tIMAGESET3D) {
				count += ((ImageSet3D)it).getNImages();
			}
			if (it.isTimeStamp) {
				 count++;
			}
		}
		return count;
	}


	public ArrayList<String> getNames(int valid_types, ArrayList<Integer> positions)
	{
		ArrayList<String> ret = new ArrayList<String>();
		int count;
		int size = scr_data.size();
		ScreenData scr_obj;

		for(count=0;count<size;count++)
		{
			scr_obj = scr_data.get(count);
			if((scr_obj.getType() & valid_types)!=0)
			{
				ret.add(scr_obj.name);
				positions.add(count);
			}
		}
		return ret;
	}

	public ArrayList<String> getNames(int valid_types)
	{
		ArrayList<String> ret = new ArrayList<String>();
		for(ScreenData obj: scr_data)
		{
			if((obj.getType() & valid_types)!=0)
				ret.add(obj.name);
		}
		return ret;
	}

	public void createSelection()
	{
		menu.createSelect(scr_data);
	}

	public void removeScreenData(ScreenData object)
	{
		BranchGroup BG = object.BGPaint;
		if(BG!=null&&BG.isLive()) BG.detach();
		scr_data.remove(object);
		createSelection();
		reDraw();
	}

	public boolean launchFunction(Function3D func, ScreenData selected)
	{
		FunctionParamDialog pdiag;

		if(!func.loadData(selected))
		{
			menu.dialogs.error("Incorrect input data type", "MyCanvas3D::launchFunction Error");
			return false;
		}
		else
		{
			// get Parameters
			if(!func.param_list.isEmpty())
			{
				try {
					pdiag = new FunctionParamDialog(menu.dialogs, func.param_list, scr_data, selected);
					pdiag.setModal(true);
					pdiag.setVisible(true);
					if(pdiag.isCancelled()) return false;
				} catch (JIPException e) {
					menu.dialogs.error(e.getLocalizedMessage(), "MyCanvas3D::launchFunction Error");
					return false;
				}
			}
			//finally, we launch the function
			func.start();
		}
		return true;

	}

	/**
	 * This method is called when a function has finished successfully
	 */
	public void functionEnded(Function3D func)
	{
		int count, size;
		if(!func.cancelled && func.result_list!=null)
		{
			size = func.result_list.size();
			for(count=0;count<size;count++) {
				func.result_list.get(count).scr_opt.hasChanged = true;
				scr_data.add(func.result_list.get(count));
			}
		}
		menu.getObjectSelected().scr_opt.hasChanged = true;
		menu.endFunctionAction(func.name, func.elapsed_time);
		menu.setCurrent_func(null);
		createSelection();
		this.reDraw();
	}

}
