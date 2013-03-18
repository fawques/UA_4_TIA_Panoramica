package javavis.jip3d.geom;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class Plane3D
 */
public class Plane3D extends Normal3D implements Serializable {
	private static final long serialVersionUID = 3067105178111905318L;

	/**
	 * @uml.property  name="bounded"
	 */
	public boolean bounded;
	/**
	 * @uml.property  name="textured"
	 */
	public boolean textured;
	/**
	 * @uml.property  name="radius"
	 */
	public double radius;
	/**
	 * @uml.property  name="bounds"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip3d.geom.Point3D"
	 */
	public ArrayList<Point3D>bounds;

	public Plane3D()
	{
		super();
		bounded = false;
		textured = false;
		radius = 0;
		bounds = null;
	}

	public Plane3D(Plane3D source)
	{
		super(source);
		bounded = source.bounded;
		radius = source.radius;
		if(bounded) bounds = new ArrayList<Point3D>(source.bounds);
		else bounds = null;
		textured = source.textured;
	}

	public Plane3D(Normal3D n)
	{
		super(n);
		bounded = false;
		radius = n.window;
		bounds = null;
		textured = false;
	}

	public Plane3D(Normal3D n, ArrayList<Point3D> b)
	{
		super(n);
		bounded = true;
		radius = 0;
		bounds = new ArrayList<Point3D>(b);
		textured = false;
	}

	public Plane3D(String[] dataRaw, int index) {
		super(dataRaw, index);
		index += 8;

		int n_points;
		int text;
		double rad;

		//bounds
		rad = Double.parseDouble(dataRaw[index++]);

		if(rad==0) {
			bounded = true;
			n_points = Integer.parseInt(dataRaw[index++]);
			bounds = new ArrayList<Point3D>(n_points);
			for(int cont=0;cont<n_points;cont++)
				bounds.add(new Point3D(dataRaw, index, 0, false, false, false));
		}
		else {
			radius = rad;
			bounded = false;
			bounds = null;
		}

		//texture
		text = Integer.parseInt(dataRaw[index++]);
		//Plane textures not implemented yet
		if(text!=0) ;
	}

	public String toString()
	{
		float EPS = 0.001f;
		float r, th, win;

		String ret = this.origin.toString();
		ret += "\n";
		ret += this.vector.toString();
		ret += "\n";
		if(thickness>-EPS && thickness<EPS) th = 0;
		else th = (float) thickness;

		if(window>-EPS && window<EPS) win = 0;
		else win = (float)window;

		ret += th +" " + win + "\n";

		//If plane has computed bounds, a zero is added and then the number of boundary points
		//then, the list of points
		if(bounded)
		{
			ret += "0 "+bounds.size()+"\n";
			for(Point3D it: bounds)
				ret += it.toString();
		}
		else
		{
			if(radius>-EPS && radius<EPS) r = 0;
			else r = (float)radius;
			ret += r;
		}

		//If plane has texture information texture information is written, or zero in the other case
		ret+="\n0";
		ret += "\n";
		ret += "\n";

		return ret;
	}

	public double pointDistance(Point3D p)
	{
		double ret;
		Point3D sub = p.subPoint(this.origin);
		ret = this.vector.dotProduct(new Vector3D(sub));

		return ret;
	}

	public double signedPointDistance(Point3D p)
	{
		double D = -this.vector.dotProduct(new Vector3D(this.origin));
		double ret = this.vector.dotProduct(new Vector3D(p)) + D;

		return ret;
	}

	public void applyTransform(MyTransform tr)
	{
		this.origin.applyTransform(tr);
		this.vector.applyTransform(tr);
		if(bounded)
			for(Point3D p: bounds)
				p.applyTransform(tr);
	}

	public Point3D pointProjection(Point3D p)
	{
		Point3D ret = new Point3D(p);
		double scalar = this.pointDistance(p);
		Vector3D vaux = new Vector3D(this.vector);
		vaux.scaleVector(scalar);
		return ret.subPoint(vaux);
	}

	public double anglePlane(Plane3D p)
	{
		double dot = this.vector.dotProduct(p.vector);
		if(Math.abs(dot)>1) return 0;
		return Math.acos(dot);
	}
}
