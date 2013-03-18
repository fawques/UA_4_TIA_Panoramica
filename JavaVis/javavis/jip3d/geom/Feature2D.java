package javavis.jip3d.geom;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class Feature2D
 */
public class Feature2D extends Point3D implements Serializable {

	private static final long serialVersionUID = -6227556960226912183L;

	/**
	 * @uml.property  name="laplacian"
	 */
	public int laplacian;
	/**
	 * @uml.property  name="orientation"
	 */
	public double orientation;

	/**
	 * @uml.property  name="scale"
	 */
	public double scale;

	/**
	 * @uml.property  name="descriptor"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	public double descriptor[];
	/**
	 * @uml.property  name="type"
	 */
	public int type;

	public Feature2D() {
		super();
		orientation = 0.0;
		scale = 0.0;
		descriptor = new double[1];
		type = 0;
		laplacian = 0;
	}

	public Feature2D(Feature2D source) {
		super(source);
		orientation = source.orientation;
		scale = source.scale;
		descriptor = (double[])source.descriptor.clone();
		type = source.type;
		laplacian = source.laplacian;
	}

	public Feature2D(StreamTokenizer st, int lengthDesc, int type) {
		visited = 0;
		this.type = type;

		try {
			switch (type) {
				case (1): //SIFT
				st.nextToken();
				posy = (int)Math.round(st.nval);
				st.nextToken();
				posx = (int)Math.round(st.nval);
				
				st.nextToken();
				scale = st.nval;
				st.nextToken();
				orientation = st.nval;
				
				descriptor = new double[lengthDesc];
				for (int i=0; i<lengthDesc; i++) {
					st.nextToken();
					descriptor[i] = (int)st.nval;
				}
				
				data = new DenseDoubleMatrix1D(4);
				data.set(0, 0);
				data.set(1, 0);
				data.set(2, 0);
				data.set(3, 1);
				break;
				
				case(2):  // SURF
				st.nextToken();
				posx = (int)Math.round(st.nval);
				st.nextToken();
				posy = (int)Math.round(st.nval);
				
				st.nextToken();
				scale = 1.0/(5*Math.sqrt(st.nval));
				st.nextToken();
				st.nextToken();
				st.nextToken();
				laplacian=(int)st.nval;
				// SURF has not orientation
				orientation=0.0;
				
				descriptor = new double[lengthDesc];
				for (int i=0; i<lengthDesc+1; i++) {
					// StreamTokenizer does not manage E or D sintax, this is a trick
					// If we are managing the 64+1 token (done because the last number can be a EorD
					if (i==lengthDesc) {
						if (st.nextToken()==StreamTokenizer.TT_WORD) {
							String aux=Double.toString(descriptor[i-1])+st.sval;
							descriptor[i-1]=Double.parseDouble(aux);
						}
						else 
							st.pushBack();
						break;
					}
					if (st.nextToken()==StreamTokenizer.TT_WORD) {
						String aux=Double.toString(descriptor[i-1])+st.sval;
						descriptor[i-1]=Double.parseDouble(aux);
						i--; // I know, I know, this is not good
					}
					else 
						descriptor[i] = st.nval;
				}
				
				data = new DenseDoubleMatrix1D(4);
				data.set(0, 0);
				data.set(1, 0);
				data.set(2, 0);
				data.set(3, 1);
				break;
				
				case(3):
				orientation=0.0;
				scale=1.0;
				st.nextToken();
				posy = (int)Math.round(st.nval);
				st.nextToken();
				posx = (int)Math.round(st.nval);
				
				descriptor = new double[lengthDesc];
				for (int i=0; i<lengthDesc; i++) {
					st.nextToken();
					descriptor[i] = (int)st.nval;
				}
				
				data = new DenseDoubleMatrix1D(4);
				data.set(0, 0);
				data.set(1, 0);
				data.set(2, 0);
				data.set(3, 1);
			}
		} catch (IOException e) {
			System.out.println("Error reading Point3D from file");
		}
	}

	public double getDistanceDescriptor (Feature2D p) {
		double dist=0;
		for (int i=0; i<descriptor.length; i++) {
			dist+=(descriptor[i]-p.descriptor[i])*(descriptor[i]-p.descriptor[i]);
		}
		return dist;
	}

	public Feature2D addPoint(Feature2D p)
	{
		Feature2D ret = new Feature2D(this);
		ret.data.assign(p.data, cern.jet.math.Functions.plus);
		ret.data.setQuick(3, 1);
		return ret;
	}

	public Feature2D subPoint(Feature2D p)
	{
		Feature2D ret = new Feature2D(this);
		ret.data.assign(p.data, cern.jet.math.Functions.minus);
		ret.data.setQuick(3, 1);
		return ret;
	}

	/**
	 * @return
	 * @uml.property  name="orientation"
	 */
	public double getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation
	 * @uml.property  name="orientation"
	 */
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	/**
	 * @return
	 * @uml.property  name="scale"
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @param scale
	 * @uml.property  name="scale"
	 */
	public void setScale(double scale) {
		this.scale = scale;
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
		ret=x+" "+y+" "+z+" "+" "+posy+" "+posx+" "+orientation+" "+scale;
		for (int i=0; i<descriptor.length; i++) {
			ret += " "+descriptor[i];
		}
		return ret;
	}


}
