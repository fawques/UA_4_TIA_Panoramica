package javavis.jip3d.geom;

import java.io.Serializable;
import javavis.base.ColorTools;
import javax.vecmath.Color3b;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class Point3D
 */
public class Point3D implements Serializable {
	private static final long serialVersionUID = -124467053871436174L;

	/**
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	DenseDoubleMatrix1D data;
	/**
	 * @uml.property  name="color"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public Color3b color;
	/**
	 * @uml.property  name="posx"
	 */
	public int posx;

	/**
	 * @uml.property  name="posy"
	 */
	public int posy;
	
	private boolean imageCoords;
	
	public boolean isValid=true;

	/**
	 * visited inform whether a point has been visited during an algorithm or not. Is commonly used to speed up 3D functions. In the future, this attribute should be moved to a Point3D subclass.
	 * @uml.property  name="visited"
	 */
	public int visited;

	/**
	 * Default constructor. Creates a void 3D point.
	 *
	 */
	public Point3D()
	{
		data = new DenseDoubleMatrix1D(4);
		data.assign(0);
		data.set(3, 1);
		color = new Color3b();
		posx = posy = 0;
		imageCoords = false;
		visited = 0;
	}

	public Point3D(Point3D source)
	{
		data = new DenseDoubleMatrix1D(4);
		data.assign(source.data);
		color = new Color3b(source.color);
		posx = source.posx;
		posy = source.posy;
		visited = source.visited;
	}

	public Point3D(String[]dataRaw, int index, int iColor, boolean imageValues, boolean yInverted, boolean zInverted)
	{
		double x,y,z;
		int col, r, g, b;
		int i,j;
		visited = 0;

		imageCoords = imageValues;
		
		if (dataRaw[index].equals("nan")) {
			isValid = false;
			return;
		}

		x = Double.parseDouble(dataRaw[index++]);
		y = Double.parseDouble(dataRaw[index++]);
		y = (yInverted?-y:y);
		z = Double.parseDouble(dataRaw[index++]);
		z = (zInverted?-z:z);

		if (iColor==1) {
			col = Integer.parseInt(dataRaw[index++]);
			color = new Color3b(ColorTools.convertIntIntoRGB(col));
		}
		else if (iColor==3) {
			r = Integer.parseInt(dataRaw[index++]);
			g = Integer.parseInt(dataRaw[index++]);
			b = Integer.parseInt(dataRaw[index++]);
			color = new Color3b((byte)r, (byte)g, (byte)b);
		}
		else color = new Color3b((byte)0,(byte)0,(byte)0);

		if (imageValues) {
			i = Integer.parseInt(dataRaw[index++]);
			j = Integer.parseInt(dataRaw[index]);
			posx = j;
			posy = i;
		}

		data = new DenseDoubleMatrix1D(4);
		data.set(0, x);
		data.set(1, y);
		data.set(2, z);
		data.set(3, 1);
	}

	/**
	 * Creates a 3D point from its coordinates. It is a black color point.
	 * @param x coordinate along X axis
	 * @param y coordinate along Y axis
	 * @param z coordinate along Z axis
	 */
	public Point3D(double x, double y, double z)
	{
		data = new DenseDoubleMatrix1D(4);
		data.set(0, x);
		data.set(1, y);
		data.set(2, z);
		data.set(3, 1);
		color = new Color3b();
		posx = posy = 0;
		imageCoords = false;
		visited = 0;
	}

	/**
	 * Creates a 3D point from its coordinates. It is a color point.
	 * @param x coordinate along X axis
	 * @param y coordinate along Y axis
	 * @param z coordinate along Z axis
	 * @param col color information
	 * @param i Internal sensor geometry: row position
	 * @param j Internal sensor geometry: column position
	 */
	public Point3D(double x, double y, double z, Color3b col, int i, int j)
	{
		data = new DenseDoubleMatrix1D(4);
		data.set(0, x);
		data.set(1, y);
		data.set(2, z);
		data.set(3, 1);
		color = new Color3b(col);
		posx = j;
		posy = i;
		imageCoords = true;
		visited = 0;
	}

	/**
	 * Creates a 3D point from a vector. It is a gray scaled point.
	 * @param coords vector with point coordinates
	 * @param c gray scaled color
	 */
	public Point3D(double []coords)
	{
		data = new DenseDoubleMatrix1D(4);
		data.set(0, coords[0]);
		data.set(1, coords[1]);
		data.set(2, coords[2]);
		data.set(3, 1);
		color = new Color3b();
		visited = 0;
		imageCoords = false;
	}


	/**
	 * Get point coordinates into a vector
	 * @return a double vector with point coordinates inside
	 */
	public double[] getCoords()
	{
		double []array =  data.toArray();
		double []ret = new double[3];
		ret[0] = array[0];
		ret[1] = array[1];
		ret[2] = array[2];

		return ret;
	}
	
	/**
	 * gets point value in axis @param axis
	 * @param axis
	 * @return Point value in that axis
	 */
	public double getValue(int axis)
	{
		return data.get(axis);
	}
	
	public double getX()
	{
		return data.get(0);
	}
	public double getY()
	{
		return data.get(1);
	}
	public double getZ()
	{
		return data.get(2);
	}

	/**
	 * Sets new value for axis
	 * @param axis
	 * @param val
	 */
	public void setValue(int axis, double val)
	{
		data.set(axis, val);
	}
	
	public void setX(double val)
	{
		data.set(0, val);
	}
	public void setY(double val)
	{
		data.set(1, val);
	}
	public void setZ(double val)
	{
		data.set(2, val);
	}


	/**
	 * Get point coordinates into a vector
	 * @return a float vector with point coordinates inside
	 */
	public float[] getCoordsf()
	{
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		return new float[] {(float)X, (float)Y, (float)Z};
	}

	/**
	 * Get the color of the point
	 * @return  A Color3b object with color point information
	 * @uml.property  name="color"
	 */
	public Color3b getColor()
	{
		return color;
	}

	/**
	 * Returns a String that contains the values of the point
	 * @return a String with the values
	 */
	public String toStringRGB()
	{
		String ret;
		float EPS = 0.001f;
		float x,y,z;
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);

		x=(float)(X>-EPS && X<EPS?0.0:X);
		y=(float)(Y>-EPS && Y<EPS?0.0:Y);
		z=(float)(Z>-EPS && Z<EPS?0.0:Z);
		
		ret=x+" "+y+" "+z+" "+color.x+" "+color.y+" "+color.z+" ";
		if (imageCoords) 
			ret += posy+" "+posx;
		return ret;
	}

	/**
	 * Returns a String that contains the values of the point
	 * @return a String with the values
	 */
	public String toString()
	{
		String ret;
		float EPS = 0.001f;
		float x,y,z;
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);

		x=(float)(X>-EPS && X<EPS?0.0:X);
		y=(float)(Y>-EPS && Y<EPS?0.0:Y);
		z=(float)(Z>-EPS && Z<EPS?0.0:Z);
		
		ret=x+" "+y+" "+z+" ";
		if (imageCoords) 
			ret += posy+" "+posx;
		return ret;
	}

	public Point3D addPoint(Point3D p)
	{
		Point3D ret = new Point3D(this);
		ret.data.assign(p.data, cern.jet.math.Functions.plus);
		ret.data.setQuick(3, 1);
		return ret;
	}

	public Point3D subPoint(Point3D p)
	{
		Point3D ret = new Point3D(this);
		ret.data.assign(p.data, cern.jet.math.Functions.minus);
		ret.data.setQuick(3, 1);
		return ret;
	}

	/**
	 * Returns euclidean distance from this point to the coordinates origin.
	 * @return The distance computed
	 */
	public double getOriginDistance()
	{
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		return Math.sqrt(X*X + Y*Y + Z*Z);
	}

	/**
	 * Returns euclidean distance form this point to the point received as parameter
	 * @param x X coordinate of the second point
	 * @param y Y coordinate of the second point
	 * @param z Z coordinate of the second point
	 * @return The distance computed
	 */
	public double getDistance(double x, double y, double z)
	{
		return Math.sqrt(getDistance2(x, y, z));
	}

	/**
	 * Returns squared euclidean distance form this point to a second point received as parameter
	 * @param x X coordinate of the second point
	 * @param y Y coordinate of the second point
	 * @param z Z coordinate of the second point
	 * @return The distance computed
	 */
	public double getDistance2(double x, double y, double z)
	{
		double ret;
		double a, b, c;
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		a = X - x;
		b = Y - y;
		c = Z- z;
		ret = a*a + b*b + c*c;

		return ret;
	}

	public double getDistance(Point3D p)
	{
		return getDistance(p.getX(), p.getY(), p.getZ());
	}

	public double getDistance2(Point3D p)
	{
		return getDistance2(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * This method is been added to support Octree class
	 */
	public void addData(Point3D p, int weight)
	{
		int add;
		if(weight == 0)
		{
			data.set(0, p.getX());
			data.set(1, p.getY());
			data.set(2, p.getZ());
			this.color.x = p.color.x;
			this.color.y = p.color.y;
			this.color.z = p.color.z;
		}
		else
		{
			data.set(0, (data.get(0)*weight + p.getX())/(weight+1));
			data.set(1, (data.get(1)*weight + p.getY())/(weight+1));
			data.set(2, (data.get(2)*weight + p.getZ())/(weight+1));
			//Test whether this has color
			add = (int)(this.color.x&255)+(int)(this.color.y&255)+(int)(this.color.z&255);
			if(add < 3)
			{
				this.color.x = p.color.x;
				this.color.y = p.color.y;
				this.color.z = p.color.z;
			}
		}
	}

	public void applyTransform(MyTransform t)
	{
		DoubleMatrix1D result;
		result = t.mat.zMult(data, null);
		data.assign(result);
	}

	public int isVisited() {
		return visited;
	}

	/**
	 * @param visited
	 * @uml.property  name="visited"
	 */
	public void setVisited(int visited) {
		this.visited = visited;
	}
}
