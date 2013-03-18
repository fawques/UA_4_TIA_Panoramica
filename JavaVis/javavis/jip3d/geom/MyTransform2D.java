package javavis.jip3d.geom;

import java.io.IOException;
import java.io.StreamTokenizer;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * A transform2D represents a 3 degrees of freedom transformation in 3D space. Normally with a 
 * translation in X, Z axis and a rotation around Y axle. Represented transformation is akin.
 * @author Diego Viejo
 */
public class MyTransform2D extends MyTransform{
	private static final long serialVersionUID = 8935805023452722317L;

	public MyTransform2D()
	{
		super();
	}

	public MyTransform2D(MyTransform2D t)
	{
		super(t);
	}

	public MyTransform2D(double tx, double tz, double ra)
	{
		double [][]values = new double[4][4];

		values[0][0] = Math.cos(ra);	values[0][1] = 0; 	values[0][2] = Math.sin(ra);	values[0][3] = tx;
		values[1][0] = 0;				values[1][1] = 1;	values[1][2] = 0;				values[1][3] = 0;
		values[2][0] = -Math.sin(ra);	values[2][1] = 0;	values[2][2] = Math.cos(ra);	values[2][3] = tz;
		values[3][0] = 0;				values[3][1] = 0;	values[3][2] = 0;				values[3][3] = 1;

		trX = tx;
		trZ = tz;
		angY = ra;

		mat = new DenseDoubleMatrix2D(values);
	}

	public MyTransform2D(DoubleMatrix2D m)
	{
		super(m);
	}

	public MyTransform2D(StreamTokenizer st)
	{
		double [][]values = new double[4][4];
		double tx, tz, ra;

		try {
			st.nextToken();
			tx = st.nval;

			st.nextToken();
			tz = st.nval;

			st.nextToken();
			ra = st.nval;

			values[0][0] = Math.cos(ra);	values[0][1] = 0; 	values[0][2] = Math.sin(ra);	values[0][3] = tx;
			values[1][0] = 0;				values[1][1] = 1;	values[1][2] = 0;				values[1][3] = 0;
			values[2][0] = -Math.sin(ra);	values[2][1] = 0;	values[2][2] = Math.cos(ra);	values[2][3] = tz;
			values[3][0] = 0;				values[3][1] = 0;	values[3][2] = 0;				values[3][3] = 1;

			trX = tx;
			trY = tz;
			angY = ra;

		} catch (IOException e) {
			System.err.println("Error on loading Transform2D from file");
			values[0][0] = values [1][1] = values[2][2] = values[3][3] = 1;
		}

		mat = new DenseDoubleMatrix2D(values);

	}

	public MyTransform2D(String[] dataRaw, int index)
	{
		double [][]values = new double[4][4];
		double tx, tz, ra;

		tx = Double.parseDouble(dataRaw[index++]);
		tz = Double.parseDouble(dataRaw[index++]);
		ra = Double.parseDouble(dataRaw[index++]);

		values[0][0] = Math.cos(ra);	values[0][1] = 0; 	values[0][2] = Math.sin(ra);	values[0][3] = tx;
		values[1][0] = 0;				values[1][1] = 1;	values[1][2] = 0;				values[1][3] = 0;
		values[2][0] = -Math.sin(ra);	values[2][1] = 0;	values[2][2] = Math.cos(ra);	values[2][3] = tz;
		values[3][0] = 0;				values[3][1] = 0;	values[3][2] = 0;				values[3][3] = 1;

		trX = tx;
		trY = tz;
		angY = ra;

		mat = new DenseDoubleMatrix2D(values);
	}

	public void setTraslation(double tx, double tz)
	{
		mat.setQuick(0, 3, tx);
		mat.setQuick(2, 3, tz);
		trX = tx;
		trZ = tz;
	}

	public void setRotation(double angle)
	{
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		angY = angle;

		mat.setQuick(0, 0, cos);	mat.set(0, 2, sin);
		mat.setQuick(2, 0, -sin);	mat.set(2, 2, cos);
	}

	public String toString()
	{
		String ret;
		float x, z, a;
		float EPS = 0.001f;

		x = (float)trX;
		z = (float)trZ;
		a = (float)angY;

		if(x>-EPS && x<EPS) x = 0;
		if(z>-EPS && z<EPS) z = 0;
		if(a>-EPS && a<EPS) a = 0;

		ret = x + " " + z + " " + a;

		return ret;
	}

	public double getAngle()
	{
		return angY;
	}

	@Override
	public MyTransform getInverse() {
		MyTransform2D ret;
		Algebra alg = new Algebra();
		DoubleMatrix2D result = alg.inverse(mat);
		ret = new MyTransform2D(result);
		return ret;
	}
}
