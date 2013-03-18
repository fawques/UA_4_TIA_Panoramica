package javavis.jip3d.base;

import java.io.Serializable;

import javax.vecmath.Color3f;

/**
 * Class ScreenOptions.
 */
public class ScreenOptions  implements Serializable{
	private static final long serialVersionUID = 2650475040078570146L;

	public static final int tUNDEFINED = 0;
	public static final int tPOINTSET3D = 1;
	public static final int tTRAJ2D = 2;
	public static final int tTRAJ3D = 4;
	public static final int tPLANARSET3D = 8;
	public static final int tNORMALSET3D = 16;
	public static final int tCYLINDERSET3D = 32;
	public static final int tVECTORSET3D = 64;
	public static final int tSEGMENTSET3D = 128;
	public static final int tFEATURESET2D = 257;
	public static final int tNEURONSET3D = 513;
	public static final int tSR4000SET3D = 1025;
	public static final int tIMAGESET3D = 2048;
	public static final int t3DMODEL = 4096;
	public static final int tSPLINE3D = 8192;
	

	public static final int tALLTYPES = tPOINTSET3D | tTRAJ2D | tTRAJ3D | tPLANARSET3D | tNORMALSET3D | tCYLINDERSET3D | tVECTORSET3D | 
										tSEGMENTSET3D | tFEATURESET2D | tNEURONSET3D | tSR4000SET3D | tIMAGESET3D | tSPLINE3D | t3DMODEL;


	/**
	 * @uml.property  name="color"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public Color3f color;
	/**
	 * @uml.property  name="global_color"
	 */
	public boolean global_color; //for PointSet3D
	/**
	 * @uml.property  name="improved"
	 */
	public boolean improved;	//for PointSet3D
	/**
	 * @uml.property  name="shine"
	 */
	public float shine;
	/**
	 * @uml.property  name="width"
	 */
	public double width;
	/**
	 * @uml.property  name="type"
	 */
	public int type;
	/**
	 * @uml.property  name="alpha"
	 */
	public double alpha;		//for trajectory
	/**
	 * @uml.property  name="length"
	 */
	public double length; 	//for normal vectors
	/**
	 * @uml.property  name="is_visible"
	 */
	public boolean is_visible;
	/**
	 * @uml.property  name="num_points"
	 */
	public int num_points; //for statistics
	/**
	 * @uml.property  name="num_points"
	 */
	public boolean hasChanged; // Indicates when a BranchGroup must be again generated.

	public ScreenOptions()
	{
		color = new Color3f();
		global_color = true;  //for PointSet3D
		improved = false;		//for PointSet3D
		shine = 10f;
		width = 1;
		type = tALLTYPES;
		alpha = 0;
		is_visible = false;
		length = 0.1;			//for NormalSet3D
		num_points = 0;
		hasChanged = false;
	}

	public ScreenOptions(Color3f c, float s)
	{
		color = new Color3f(c);
		global_color = true;  //for PointSet3D
		shine = s;
		width = 2;
		type = tALLTYPES;
		alpha = 0;
		is_visible = false;
		length = 0.1;
		num_points = 0;
		hasChanged = false;
	}

}
