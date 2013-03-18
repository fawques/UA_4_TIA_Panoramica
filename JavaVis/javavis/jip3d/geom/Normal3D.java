package javavis.jip3d.geom;

import java.io.Serializable;

/**
 * Class Normal3D
 */
public class Normal3D implements Serializable {

	private static final long serialVersionUID = 1429529497625472373L;
	/**
	 * @uml.property  name="origin"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public Point3D origin;
	/**
	 * @uml.property  name="vector"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public Vector3D vector;
	/**
	 * @uml.property  name="thickness"
	 */
	public double thickness;
	/**
	 * @uml.property  name="window"
	 */
	public double window;

	public Normal3D()
	{
		origin = new Point3D();
		vector = new Vector3D();
		thickness = -1;
		window = -1;
	}

	public Normal3D(Normal3D source)
	{
		origin = new Point3D(source.origin);
		vector = new Vector3D(source.vector);
		thickness = source.thickness;
		window = source.window;
	}

	public Normal3D(Point3D punto, Vector3D vect)
	{
		origin = new Point3D(punto);
		vector = new Vector3D(vect);
		vector.normalize();
		thickness = -1;
		window = -1;
	}

	public Normal3D(Point3D punto, Vector3D vect, double th, double win)
	{
		origin = new Point3D(punto);
		vector = new Vector3D(vect);
		vector.normalize();
		thickness = th;
		window = win;
	}

	public Normal3D(String[] dataRaw, int index)
	{
		// Not used
		origin = new Point3D(dataRaw, index, 0, false, false, false);
		index += 3;
		vector = new Vector3D(dataRaw, index);
		index += 3;
		vector.normalize();
		thickness = Double.parseDouble(dataRaw[index++]);
		window = Double.parseDouble(dataRaw[index]);
	}

	/**
	 * Compute angle between normal vectors
	 * @return
	 */
	public double getAngle(Normal3D n)
	{
		double cos = this.vector.dotProduct(n.vector);
		if(Math.abs(cos) > 1) return 0;
		return Math.acos(cos);
	}

	public String toString()
	{
		float th, win;
		float EPS = 0.001f;
		String ret = origin.toString();
		ret += "\n";
		ret += vector.toString();
		ret += "\n";

		if(thickness>-EPS && thickness<EPS) th = 0;
		else th = (float) thickness;

		if(window>-EPS && window<EPS) win = 0;
		else win = (float)window;

		ret += th + " " + win;

		return ret;
	}

	/**
	 * this method computes the rotational matrix for performing general rotations around this vector
	 * @param angle
	 * @return
	 */
	public double[][] generalRotationMatrix(double angle)
	{
		double [][] ret = new double[4][4];
		double [][]tras = new double[4][4];
		double sin, cos, aux;
		double [][]m1 = this.vector.tensorProduct(this.vector);
		double [][]m2 = this.vector.getSkewSymmetric();

		sin = Math.sin(angle);
		cos = Math.cos(angle);
		aux = 1 - cos;

		ret[0][0] = cos + aux*m1[0][0];			 ret[0][1] = aux*m1[0][1] + sin*m2[0][1];	ret[0][2] = aux*m1[0][2] + sin*m2[0][2];ret[0][3] = 0;
		ret[1][0] = aux*m1[1][0] + sin*m2[1][0]; ret[1][1] = cos + aux*m1[1][1];			ret[1][2] = aux*m1[1][2] + sin*m2[1][2];ret[1][3] = 0;
		ret[2][0] = aux*m1[2][0] + sin*m2[2][0]; ret[2][1] = aux*m1[2][1] + sin*m2[2][1];	ret[2][2] = cos + aux*m1[2][2];			ret[2][3] = 0;

		ret[3][0] = this.origin.getX() - this.origin.getX()*ret[0][0] - this.origin.getY()*ret[1][0] - this.origin.getZ()*ret[2][0];
		ret[3][1] = this.origin.getY() - this.origin.getX()*ret[0][1] - this.origin.getY()*ret[1][1] - this.origin.getZ()*ret[2][1];
		ret[3][2] = this.origin.getZ() - this.origin.getX()*ret[0][2] - this.origin.getY()*ret[1][2] - this.origin.getZ()*ret[2][2];
		ret[3][3] = 1;

		tras[0][0] = ret[0][0]; tras[0][1] = ret[1][0]; tras[0][2] = ret[2][0]; tras[0][3] = ret[3][0];
		tras[1][0] = ret[0][1]; tras[1][1] = ret[1][1]; tras[1][2] = ret[2][1]; tras[1][3] = ret[3][1];
		tras[2][0] = ret[0][2]; tras[2][1] = ret[1][2]; tras[2][2] = ret[2][2]; tras[2][3] = ret[3][2];
		tras[3][0] = ret[0][3]; tras[3][1] = ret[1][3]; tras[3][2] = ret[2][3]; tras[3][3] = ret[3][3];
		return tras;
	}


	public void applyTransform(MyTransform tr)
	{
		origin.applyTransform(tr);
		vector.applyTransform(tr);
	}
}
