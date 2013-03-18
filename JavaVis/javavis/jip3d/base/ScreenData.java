package javavis.jip3d.base;


import java.io.Serializable;


import javavis.jip3d.geom.Feature2D;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Segment3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.geom.MyKDTree;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.TransformGroup;

import org.w3c.dom.Document;

/**
 * Class ScreenData. It represents objects which will be drawn on the screen. It has the headers
 * read/write in file and visualize on screen. 
 * @author Diego Viejo
 */

public abstract class ScreenData implements Serializable{
	private static final long serialVersionUID = 2767756987547500603L;

	/**
	 * @uml.property  name="name"
	 */
	public String name;
	
	/**
	 * @uml.property  name="path"
	 * @uml.associationEnd  
	 */
	public String path;

	/**
	 * @uml.property  name="object_app"
	 * @uml.associationEnd  
	 */
	transient public Appearance object_app;
	/**
	 * @uml.property  name="object_material"
	 * @uml.associationEnd  
	 */
	transient public Material object_material; //for lighting
	/**
	 * @uml.property  name="object_coloring"
	 * @uml.associationEnd  
	 */
	transient public ColoringAttributes object_coloring; //for not lighting;
	/**
	 * @uml.property  name="latt"
	 * @uml.associationEnd  
	 */
	transient public LineAttributes latt;

	/**
	 * @uml.property  name="bGPaint"
	 * @uml.associationEnd  
	 */
	transient public BranchGroup BGPaint; //transient objects won't be persistent

	/**
	 * @uml.property  name="scr_opt"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public ScreenOptions scr_opt;

	/**
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	protected MyKDTree data;
	
	/**
	 * members for time lapse
	 */
	public boolean isTimeStamp;
	public int timeStamp;

	/**
	 * @uml.property  name="dEBUG"
	 */
	protected boolean DEBUG;

	public ScreenData(ScreenOptions opt)
	{
		scr_opt = opt;
		data = new MyKDTree(3);
		DEBUG = false;
		isTimeStamp=false;
	}

	public abstract int readData(String name, String path, Document doc);

	public abstract void writeData(String name, String path);

	protected abstract TransformGroup paint();
	
	public void detach() {
		if(BGPaint!=null&&BGPaint.isLive()) {
			BGPaint.detach();
			this.scr_opt.is_visible=false;
		}
	}
	
	public BranchGroup getBG () {
		if (BGPaint==null) {
			build();
		}
		return BGPaint;
	}
	
	public boolean isBGCreated() {
		return BGPaint!=null;
	}
	
	public void build () {
		BGPaint = new BranchGroup();
		BGPaint.setCapability(BranchGroup.ALLOW_DETACH);

		object_app = new Appearance();
		object_material = new Material();
		object_coloring = new ColoringAttributes();
		latt = new LineAttributes((float)scr_opt.width, LineAttributes.PATTERN_SOLID, true);

		//material values
		object_material.setDiffuseColor(scr_opt.color);
		object_material.setShininess(scr_opt.shine);

		//colored attributes values
		object_coloring.setColor(scr_opt.color);

		//point attributes values
		PointAttributes point_att = new PointAttributes((float)scr_opt.width, scr_opt.improved);

		//appearance
		object_app.setColoringAttributes(object_coloring);
		object_app.setMaterial(object_material);
		object_app.setLineAttributes(latt);
		object_app.setPointAttributes(point_att);

		BGPaint.addChild(this.paint());
		
		scr_opt.hasChanged=false;
	}

	public int getType()
	{
		return scr_opt.type;
	}

	public void setType(int t)
	{
		scr_opt.type = t;
	}

	public boolean isVisible()
	{
		return scr_opt.is_visible;
	}

	public void setVisible(boolean v)
	{
		scr_opt.is_visible = v;
	}

	public MyKDTree getData()
	{
		return data;
	}

	public Object []elements()
	{
		return data.elements();
	}

	public Object []range(double []min, double []max)
	{
		Object []ret;
		try
		{
			ret = data.range(min, max);
		} catch(Exception e)
		{
			ret = null;
		}
		return ret;
	}

	public Object []range(Point3D key, double rad)
	{
		Object []ret;
		try
		{
			ret = data.range(key, rad);
		} catch(Exception e)
		{
			ret = null;
		}
		return ret;
	}

	public double [] getMinRange()
	{
		return data.minRange;
	}

	public double [] getMaxRange()
	{
		return data.maxRange;
	}


	public void insert(Object obj)
	{
		if(obj instanceof Point3D)
			data.insert(((Point3D)obj).getCoords(), obj);
		else if(obj instanceof Vector3D)
			data.insert(((Vector3D)obj).getCoords(), obj);
		else if(obj instanceof Normal3D)
			data.insert(((Normal3D)obj).origin.getCoords(), obj);
		else if(obj instanceof Plane3D)
			data.insert(((Plane3D)obj).origin.getCoords(), obj);
		else if(obj instanceof Segment3D)
			data.insert(((Segment3D)obj).begin.getCoords(), obj);
		else if(obj instanceof Feature2D)
			data.insert(((Feature2D)obj).getCoords(), obj);
		scr_opt.num_points++;
	}

	public abstract void applyTransform(MyTransform trans);
	public abstract void applyMirror(int plane);
	
	public String toString()
	{
		return name;
	}

}
