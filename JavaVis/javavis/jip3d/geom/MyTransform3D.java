package javavis.jip3d.geom;

import java.io.IOException;
import java.io.StreamTokenizer;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

public class MyTransform3D extends MyTransform {
	private static final long serialVersionUID = 8155151159563499740L;

	public MyTransform3D()
	{
		super();
	}

	public MyTransform3D(MyTransform t)
	{
		super(t);
	}

	public MyTransform3D(double tx, double ty, double tz, double ax, double ay, double az)
	{
		super();
		trX = tx;
		trY = ty;
		trZ = tz;
		angX = ax;
		angY = ay;
		angZ = az;
		double cosx = Math.cos(ax);
		double sinx = Math.sin(ax);
		double cosy = Math.cos(ay);
		double siny = Math.sin(ay);
		double cosz = Math.cos(az);
		double sinz = Math.sin(az);

		mat.set(0, 3, tx);
		mat.set(1, 3, ty);
		mat.set(2, 3, tz);

		mat.set(0, 0, cosy*cosz); mat.set(0, 1, -cosy*sinz); mat.set(0, 2, siny);
		mat.set(1, 0, sinx*siny*cosz + cosx*sinz); mat.set(1, 1, -sinx*siny*sinz + cosx*cosz); mat.set(1, 2, -sinx*cosy);
		mat.set(2, 0, -cosx*siny*cosz + sinx*sinz); mat.set(2, 1, cosx*siny*sinz + sinx*cosz); mat.set(2, 2, cosx*cosy);
	}

	public MyTransform3D(DoubleMatrix2D m)
	{
		super(m);
	}

	public MyTransform3D(StreamTokenizer st)
	{
		super();
		try
		{
			st.nextToken();
			trX = st.nval;

			st.nextToken();
			trY = st.nval;

			st.nextToken();
			trZ = st.nval;

			st.nextToken();
			angX = st.nval;

			st.nextToken();
			angY = st.nval;

			st.nextToken();
			angZ = st.nval;
			double cosx = Math.cos(angX);
			double sinx = Math.sin(angX);
			double cosy = Math.cos(angY);
			double siny = Math.sin(angY);
			double cosz = Math.cos(angZ);
			double sinz = Math.sin(angZ);

			mat.set(0, 3, trX);
			mat.set(1, 3, trY);
			mat.set(2, 3, trZ);
			mat.set(3, 3, 1);

			mat.set(0, 0, cosy*cosz); mat.set(0, 1, -cosy*sinz); mat.set(0, 2, siny);
			mat.set(1, 0, sinx*siny*cosz + cosx*sinz); mat.set(1, 1, -sinx*siny*sinz + cosx*cosz); mat.set(1, 2, -sinx*cosy);
			mat.set(2, 0, -cosx*siny*cosz + sinx*sinz); mat.set(2, 1, cosx*siny*sinz + sinx*cosz); mat.set(2, 2, cosx*cosy);
		} catch(IOException e)
		{
			System.out.println("Error: MyTransform3D. Can't read file");
		}
	}

	public MyTransform3D(String[] dataRaw, int index)
	{
		super();
		
		trX = Double.parseDouble(dataRaw[index++]);
		trY = Double.parseDouble(dataRaw[index++]);
		trZ = Double.parseDouble(dataRaw[index++]);
		angX = Double.parseDouble(dataRaw[index++]);
		angY = Double.parseDouble(dataRaw[index++]);
		angZ = Double.parseDouble(dataRaw[index]);
		double cosx = Math.cos(angX);
		double sinx = Math.sin(angX);
		double cosy = Math.cos(angY);
		double siny = Math.sin(angY);
		double cosz = Math.cos(angZ);
		double sinz = Math.sin(angZ);

		mat.set(0, 3, trX);
		mat.set(1, 3, trY);
		mat.set(2, 3, trZ);
		mat.set(3, 3, 1);

		mat.set(0, 0, cosy*cosz); mat.set(0, 1, -cosy*sinz); mat.set(0, 2, siny);
		mat.set(1, 0, sinx*siny*cosz + cosx*sinz); mat.set(1, 1, -sinx*siny*sinz + cosx*cosz); mat.set(1, 2, -sinx*cosy);
		mat.set(2, 0, -cosx*siny*cosz + sinx*sinz); mat.set(2, 1, cosx*siny*sinz + sinx*cosz); mat.set(2, 2, cosx*cosy);
	}

	public void setTranslation(double []trans)
	{
		setTranslation(trans[0], trans[1], trans[2]);
	}

	public void setTranslation(double tx, double ty, double tz)
	{
		trX = tx;
		trY = ty;
		trZ = tz;
		mat.set(0, 3, tx);
		mat.set(1, 3, ty);
		mat.set(2, 3, tz);
	}

	public void setRotation(double ax, double ay, double az)
	{
		double cosx = Math.cos(ax);
		double sinx = Math.sin(ax);
		double cosy = Math.cos(ay);
		double siny = Math.sin(ay);
		double cosz = Math.cos(az);
		double sinz = Math.sin(az);

		angX = ax;
		angY = ay;
		angZ = az;

		mat.set(0, 0, cosy*cosz); mat.set(0, 1, -cosy*sinz); mat.set(0, 2, siny);
		mat.set(1, 0, sinx*siny*cosz + cosx*sinz); mat.set(1, 1, -sinx*siny*sinz + cosx*cosz); mat.set(1, 2, -sinx*cosy);
		mat.set(2, 0, -cosx*siny*cosz + sinx*sinz); mat.set(2, 1, cosx*siny*sinz + sinx*cosz); mat.set(2, 2, cosx*cosy);
	}

	public String toString()
	{
		String ret = "";
		double eps = 0.001;
		float end_value;
		double value;


	    value = mat.get(0, 3); //tx
		if(value>-eps && value<eps) end_value = 0;
		else end_value = (float)value;
		ret += end_value + " ";

	    value = mat.get(1, 3); //ty
		if(value>-eps && value<eps) end_value = 0;
		else end_value = (float)value;
		ret += end_value + " ";

	    value = mat.get(2, 3); //tz
		if(value>-eps && value<eps) end_value = 0;
		else end_value = (float)value;
		ret += end_value + " ";

	    value = angX;
		if(value>-eps && value<eps) end_value = 0;
		else end_value = (float)value;
		ret += end_value + " ";

	    value = angY;
		if(value>-eps && value<eps) end_value = 0;
		else end_value = (float)value;
		ret += end_value + " ";

	    value = angZ;
		if(value>-eps && value<eps) end_value = 0;
		else end_value = (float)value;
		ret += end_value;

		return ret;
	}
	
	public String toStringExtended()
	{
		String ret = "";
		ret += mat.toString();
		return ret;
	}

	public double getTrX() {
		return trX;
	}

	public void setTrX(double trX) {
		this.trX = trX;
	}

	public double getTrY() {
		return trY;
	}

	public void setTrY(double trY) {
		this.trY = trY;
	}

	public double getTrZ() {
		return trZ;
	}

	public void setTrZ(double trZ) {
		this.trZ = trZ;
	}

	public double getAngX() {
		return angX;
	}

	public void setAngX(double angX) {
		this.angX = angX;
	}

	public double getAngY() {
		return angY;
	}

	public void setAngY(double angY) {
		this.angY = angY;
	}

	public double getAngZ() {
		return angZ;
	}

	public void setAngZ(double angZ) {
		this.angZ = angZ;
	}

	public double[] getTranslation()
	{
		double []ret = new double[3];
		ret[0] = trX;
		ret[1] = trY;
		ret[2] = trZ;
		return ret;
	}

	@Override
	public MyTransform3D getInverse()
	{
		MyTransform3D ret;
		Algebra alg = new Algebra();
		DoubleMatrix2D result = alg.inverse(mat);
		ret = new MyTransform3D(result);
		return ret;
	}
}
