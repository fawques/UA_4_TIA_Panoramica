package javavis.jip3d.geom;

import java.io.Serializable;

import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.math.Functions;

/**
 * Class Vector3D
 */
public class Vector3D extends Point3D implements Serializable {

	private static final long serialVersionUID = 347049841820465894L;

	/**
	 * @uml.property  name="module"
	 */
	public double module;

	/**
	 * Default constructor. Creates a void 3D vector.
	 *
	 */
	public Vector3D()
	{
		super();
		module = 0;
	}

	/**
	 * Creates a 3D point from its coordinates. It is a gray scaled point.
	 * @param x coordinate along X axis
	 * @param y coordinate along Y axis
	 * @param z coordinate along Z axis
	 */
	public Vector3D(double x, double y, double z)
	{
		super(x, y, z);
		module = Math.sqrt(x*x + y*y + z*z);
	}

	public Vector3D(Point3D p)
	{
		super(p);
		double []coords;
		coords = p.getCoords();
		module = Math.sqrt(coords[0]*coords[0] + coords[1]*coords[1] + coords[2]*coords[2]);
	}

	public Vector3D(Vector3D source)
	{
		super(source);
		module = Math.sqrt(data.get(0)*data.get(0) + data.get(1)*data.get(1) + data.get(2)*data.get(2));
	}

	public Vector3D(String[] dataRaw, int index) {
		super();
		double X,Y,Z;

		X = Double.parseDouble(dataRaw[index++]);
		data.set(0, X);

		Y = Double.parseDouble(dataRaw[index++]);
		data.set(1, Y);

		Z = Double.parseDouble(dataRaw[index++]);
		data.set(2, Z);

		module = Math.sqrt(X*X + Y*Y + Z*Z);
	}

	/**
	 * Creates a 3D point from a vector. It is a gray scaled point.
	 * @param coords vector with point coordinates
	 * @param c gray scaled color
	 */
	public Vector3D(double []coords)
	{
		super(coords);
		module = Math.sqrt(coords[0]*coords[0] + coords[1]*coords[1] + coords[2]*coords[2]);
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

		if(X>-EPS && X<EPS)
			x=0;
		else x=(float)X;
		if(Y>-EPS && Y<EPS)
			y=0;
		else y=(float)Y;
		if(Z>-EPS && Z<EPS)
			z=0;
		else z=(float)Z;
		ret=x+" "+y+" "+z;
		return ret;
	}

	//operations on vectors
	public void scaleVector(double scale_factor)
	{
		data.assign(Functions.mult(scale_factor));
	}

	public double dotProduct(Vector3D v)
	{
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		double ret = (X * v.getX() + Y * v.getY() + Z * v.getZ());
		return ret;
	}

	public Vector3D crossProduct(Vector3D v)
	{
		Vector3D ret;
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		ret = new Vector3D((Y * v.getZ() - Z * v.getY()), (Z * v.getX()- X*v.getZ()), (X*v.getY() - Y * v.getX()));
		return ret;
	}

	/**
	 * Returns the angle (in radians) between two vectors. The angle is in the range 0 through pi.
	 * @param v
	 * @return
	 */
	public double getAngle(Vector3D v)
	{
		double cos = this.dotProduct(v) / (this.module * v.module);
		return Math.acos(cos);
	}

	/**
	 * This method computes the tensor product of two vectors: u (x) v
	 * @param v the second vector for the tensor product
	 * @return
	 */
	public double[][] tensorProduct(Vector3D v)
	{
		double [][] ret = new double[3][3];
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		ret[0][0] = X * v.getX(); ret[0][1] = X * v.getY(); ret[0][2] = X * v.getZ();
		ret[1][0] = Y * v.getX(); ret[1][1] = Y * v.getY(); ret[1][2] = Y * v.getZ();
		ret[2][0] = Z * v.getX(); ret[2][1] = Z * v.getY(); ret[2][2] = Z * v.getZ();
		return ret;
	}

	/**
	 * SkewSymmetric matrix. This method builds the SkewSymmetric matrix
	 * described on pages 119-120 of Geometric Tools for Computer Graphics (Schneider03)
	 * This matrix is used to compute the cross product as a matrix-vector multiplication
	 * @return
	 */
	public double[][] getSkewSymmetric()
	{
		double [][]ret = new double[3][3];
		double X = data.get(0);
		double Y = data.get(1);
		double Z = data.get(2);
		ret[0][0] = 0;	ret[0][1] = Z;	ret[0][2] = -Y;
		ret[1][0] = -Z;	ret[1][1] = 0;	ret[1][2] = X;
		ret[2][0] = Y;	ret[2][1] = -X;	ret[2][2] = 0;
		return ret;
	}


	/**
	 * This method computes the rotational matrix for performing general rotations around this vector
	 * @param angle
	 * @return
	 */
	public double[] generalRotationMatrix(double angle)
	{
		double [] ret = new double[9];
		double sin, cos, aux;
		double [][]m1 = this.tensorProduct(this);
		double [][]m2 = this.getSkewSymmetric();

		sin = Math.sin(angle);
		cos = Math.cos(angle);
		aux = 1 - cos;

		//transpose
		ret[0] = cos + aux*m1[0][0];		  ret[3] = aux*m1[0][1] + sin*m2[0][1];	ret[6] = aux*m1[0][2] + sin*m2[0][2];
		ret[1] = aux*m1[1][0] + sin*m2[1][0]; ret[4] = cos + aux*m1[1][1];			ret[7] = aux*m1[1][2] + sin*m2[1][2];
		ret[2] = aux*m1[2][0] + sin*m2[2][0]; ret[5] = aux*m1[2][1] + sin*m2[2][1];	ret[8] = cos + aux*m1[2][2];

		return ret;
	}

	/**
	 * Perform normalization over vector. After this operation, vector module is 1.
	 * @return old vector module on successful. 0 in otherwise.
	 */
	public double normalize()
	{
		double ret = module;
		if(module!=0)
		{
			data.assign(Functions.div(module));
			module = 1;
			data.set(3, 1);
		}
		return ret;
	}

	public void applyTransform(MyTransform t)
	{
		DoubleMatrix1D result;
		MyTransform3D tr = new MyTransform3D(t);
		tr.set(0, 3, 0);
		tr.set(1, 3, 0);
		tr.set(2, 3, 0);
		result = tr.mat.zMult(data, null);
		data.assign(result);
	}
	
	/**
	 * Computes the sum of two vectors
	 * @param v The second vector
	 * @return A new vector that is the sum of this and v
	 */
	public Vector3D addVector(Vector3D v)
	{
		Vector3D ret = new Vector3D(this.getX() + v.getX(), this.getY() + v.getY(), this.getZ() + v.getZ());
		return ret;
	}

	/**
	 * Computes the subtraction of two vectors
	 * @param v The second vector
	 * @return A new vector that is the sum of this and v
	 */
	public Vector3D subVector(Vector3D v)
	{
		Vector3D ret = new Vector3D(v.getX() - this.getX(), v.getY() - this.getY(), v.getZ() - this.getZ());
		return ret;
	}

	public double getDistance(Vector3D v)
	{
		Vector3D sub = this.subVector(v);
		return sub.module;
	}

}
