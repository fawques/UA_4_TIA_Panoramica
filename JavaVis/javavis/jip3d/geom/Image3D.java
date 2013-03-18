package javavis.jip3d.geom;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class Image3D
 */
public class Image3D implements Serializable {
	private static final long serialVersionUID = 8390838829142437949L;
	
	BufferedImage data;
	
	DenseDoubleMatrix1D pose;
	
	String fileName;
	
	public double timeStamp;
	
	public MyTransform3D transform;
	
	static int WIDTH=256; //Must be power of 2
	

	/**
	 * Default constructor. Creates a void 3D point.
	 *
	 */
	public Image3D()
	{
		pose = new DenseDoubleMatrix1D(4);
		pose.assign(0);
		pose.set(3, 1);
		data=null;
		fileName=null;
	}

	public Image3D(Image3D source)
	{
		data = new BufferedImage(source.data.getWidth(), source.data.getHeight(), source.data.getType());
		data.setData(source.data.getRaster());
		pose = new DenseDoubleMatrix1D(4);
		pose.assign(source.pose);
		fileName = new String(source.fileName);
	}
	
	public Image3D(String[] dataRaw, String path, int index)
	{
		BufferedImage auxdata;
		try {
			fileName = dataRaw[index++];
			auxdata = ImageIO.read(new File(path+fileName));
			int[] b = auxdata.getRGB(0, 0, auxdata.getWidth(), auxdata.getHeight(), null, 0, auxdata.getWidth());
			int height = WIDTH;
			int[] auxb = new int[WIDTH*height];
			int ratioX=(int)(auxdata.getWidth()/(double)WIDTH);
			int ratioY=(int)(auxdata.getHeight()/(double)height);
			for (int j=0; j<height; j++) 
				for (int i=0; i<WIDTH; i++)
					auxb[j*WIDTH+i] = b[(int)((j*ratioY)*auxdata.getWidth()+(i*ratioX))];
				
			data = new BufferedImage(WIDTH, height, auxdata.getType());
			data.setRGB(0, 0, WIDTH, height, auxb, 0, WIDTH);
			
			timeStamp= Integer.parseInt(dataRaw[index++]);
			transform = new MyTransform3D(dataRaw, index);
	
			pose = new DenseDoubleMatrix1D(4);
			pose.set(0, transform.getTrX());
			pose.set(1, transform.getTrY());
			pose.set(2, transform.getTrZ());
			pose.set(3, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BufferedImage getData () {
		return data;
	}

	/**
	 * Get point coordinates into a vector
	 * @return a double vector with point coordinates inside
	 */
	public double[] getCoords()
	{
		double []array =  pose.toArray();
		double []ret = new double[3];
		ret[0] = array[0];
		ret[1] = array[1];
		ret[2] = array[2];

		return ret;
	}

	public double getX()
	{
		return pose.get(0);
	}
	public double getY()
	{
		return pose.get(1);
	}
	public double getZ()
	{
		return pose.get(2);
	}

	public void setX(double val)
	{
		pose.set(0, val);
	}
	public void setY(double val)
	{
		pose.set(1, val);
	}
	public void setZ(double val)
	{
		pose.set(2, val);
	}


	/**
	 * Get point coordinates into a vector
	 * @return a float vector with point coordinates inside
	 */
	public float[] getCoordsf()
	{
		double X = pose.get(0);
		double Y = pose.get(1);
		double Z = pose.get(2);
		return new float[] {(float)X, (float)Y, (float)Z};
	}

	/**
	 * Returns a String that contains the values of the point
	 * @return a String with the values
	 */
	public String toString()
	{
		return fileName+" "+timeStamp+" "+transform+"\n";
	}

	/**
	 * Returns euclidean distance from this point to the coordinates origin.
	 * @return The distance computed
	 */
	public double getOriginDistance()
	{
		double X = pose.get(0);
		double Y = pose.get(1);
		double Z = pose.get(2);
		return Math.sqrt(X*X + Y*Y + Z*Z);
	}

	/**
	 * Returns euclid distance form this point to the point received as parameter
	 * @param x X coordinate of the second point
	 * @param y Y coordinate of the second point
	 * @param z Z coordinate of the second point
	 * @return The distance computed
	 */
	public double getDistance(double x, double y, double z)
	{
		return Math.sqrt(getDistance2(x, y, z));
	}

	
	public double getDistance2(double x, double y, double z)
	{
		double ret;
		double a, b, c;
		double X = pose.get(0);
		double Y = pose.get(1);
		double Z = pose.get(2);
		a = X - x;
		b = Y - y;
		c = Z- z;
		ret = a*a + b*b + c*c;

		return ret;
	}

	public double getDistance(Image3D p)
	{
		return getDistance(p.getX(), p.getY(), p.getZ());
	}

	public double getDistance2(Image3D p)
	{
		return getDistance2(p.getX(), p.getY(), p.getZ());
	}

	public void applyTransform(MyTransform t)
	{
		DoubleMatrix1D result;
		result = t.mat.zMult(pose, null);
		pose.assign(result);
	}

}

