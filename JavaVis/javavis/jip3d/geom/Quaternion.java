package javavis.jip3d.geom;

import java.io.Serializable;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 * Extended 7D quaternion for ICP Method
 * @author Diego Viejo
 */
public class Quaternion implements Serializable {
	private static final long serialVersionUID = 5007837818970163109L;

	/**
	 * rotation quaternion components
	 * @uml.property  name="q0"
	 */
	public double q0; //

	/**
	 * @uml.property  name="q1"
	 */
	public double q1;

	/**
	 * @uml.property  name="q2"
	 */
	public double q2;

	/**
	 * @uml.property  name="q3"
	 */
	public double q3;
	
	/**
	 * translation quaternion components
	 * @uml.property  name="q4"
	 */
	public double q4; 
	
	/**
	 * @uml.property  name="q5"
	 */
	public double q5;

	/**
	 * @uml.property  name="q6"
	 */
	public double q6;

	public Quaternion()
	{
		q0 = 1;
		q1 = q2 = q3 = q4 = q5 = q6 = 0;
	}

	public Quaternion(double nq0, double nq1, double nq2, double nq3, double nq4, double nq5, double nq6)
	{
		q0 = nq0;
		q1 = nq1;
		q2 = nq2;
		q3 = nq3;
		q4 = nq4;
		q5 = nq5;
		q6 = nq6;
	}

	public Quaternion(double []data)
	{
		q0 = data[0];
		q1 = data[1];
		q2 = data[2];
		q3 = data[3];
		q4 = data[4];
		q5 = data[5];
		q6 = data[6];
	}

	public Quaternion plus(Quaternion b)
	{
		return new Quaternion(q0+b.q0, q1+b.q1, q2+b.q2, q3+b.q3, q4+b.q4, q5+b.q5, q6+b.q6);
	}

	public Quaternion sub(Quaternion b)
	{
		return new Quaternion(q0-b.q0, q1-b.q1, q2-b.q2, q3-b.q3, q4-b.q4, q5-b.q5, q6-b.q6);
	}

	public double norm()
	{
		return Math.sqrt(q0*q0 + q1*q1 + q2*q2 + q3*q3 + q4*q4 + q5*q5 + q6*q6);
	}

	public void scale(double value)
	{
		q0 *= value;
		q1 *= value;
		q2 *= value;
		q3 *= value;
		q4 *= value;
		q5 *= value;
		q6 *= value;
	}

	public void insertValues (MyTransform3D ret)
	{
		DenseDoubleMatrix2D m = new DenseDoubleMatrix2D(4,4);
		m.setQuick(0, 0, (q0*q0 + q1*q1 - q2*q2 + q3*q3)); m.setQuick(0, 1, 2*(q1*q2 - q0*q3)); m.setQuick(0, 2, 2*(q1*q3 + q0*q2)); m.setQuick(0, 3, q4);
		m.setQuick(1, 0, 2*(q1*q2 + q0*q3)); m.setQuick(1, 1, (q0*q0 + q2*q2 - q1*q1 - q3*q3)); m.setQuick(1, 2, 2*(q2*q3 - q0*q1)); m.setQuick(1, 3, q5);
		m.setQuick(2, 0, 2*(q1*q3 - q0*q2)); m.setQuick(2, 1, 2*(q2*q3 + q0*q1)); m.setQuick(2, 2, (q0*q0 + q3*q3 - q1*q1 - q2*q2)); m.setQuick(2, 3, q6);
		m.setQuick(3, 0, 0); m.setQuick(3, 1, 0); m.setQuick(3, 2, 0); m.setQuick(3, 3, 1);
		ret.setTransform(m);
	}

	public MyTransform3D getTransform()
	{
		DenseDoubleMatrix2D m = new DenseDoubleMatrix2D(4,4);
		MyTransform3D ret;
		m.setQuick(0, 0, (q0*q0 + q1*q1 - q2*q2 + q3*q3)); m.setQuick(0, 1, 2*(q1*q2 - q0*q3)); m.setQuick(0, 2, 2*(q1*q3 + q0*q2)); m.setQuick(0, 3, q4);
		m.setQuick(1, 0, 2*(q1*q2 + q0*q3)); m.setQuick(1, 1, (q0*q0 + q2*q2 - q1*q1 - q3*q3)); m.setQuick(1, 2, 2*(q2*q3 - q0*q1)); m.setQuick(1, 3, q5);
		m.setQuick(2, 0, 2*(q1*q3 - q0*q2)); m.setQuick(2, 1, 2*(q2*q3 + q0*q1)); m.setQuick(2, 2, (q0*q0 + q3*q3 - q1*q1 - q2*q2)); m.setQuick(2, 3, q6);
		m.setQuick(3, 0, 0); m.setQuick(3, 1, 0); m.setQuick(3, 2, 0); m.setQuick(3, 3, 1);
		ret = new MyTransform3D(m);
		return ret;
	}

	public String toString()
	{
		String ret =  "("+(float)q0+", "+(float)q1+", "+(float)q2+", "+(float)q3;
		ret += ", "+(float)q4 + ", "+(float)q5 + ", "+(float)q6+")";
		return ret;
	}
}
