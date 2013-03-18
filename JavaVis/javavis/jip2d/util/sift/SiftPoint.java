package javavis.jip2d.util.sift;

import java.io.Serializable;

public class SiftPoint implements Serializable{
	private static final long serialVersionUID = 12348932421112321L;
	
	/**
	 * coords. of the point
	 * @uml.property  name="x"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	public double x;

	/**
	 * coords. of the point
	 * @uml.property  name="y"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	public double y;
	
	/**
	 * Reference to DoG image
	 * @uml.property  name="numDoG"
	 */
	public int numDoG;
	
	/**
	 * Sigma from the DoG image
	 * @uml.property  name="sigma"
	 */
	public double sigma;
	
	/**
	 * coords. of the DoG image.
	 * @uml.property  name="xDog"
	 */
	public double xDog;

	/**
	 * coords. of the DoG image.
	 * @uml.property  name="yDog"
	 */
	public double yDog;
	
	/**
	 * Orientation
	 * @uml.property  name="orientation"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	public double orientation;
	
	/**
	 * Neighborhood descriptor
	 * @uml.property  name="descriptor" multiplicity="(0 -1)" dimension="1"
	 */
	public double[] descriptor;
	
	/**
	 * Point level
	 * @uml.property  name="level"
	 */
	public double level;
	
	public SiftPoint(double x, double y, int xDog, int yDog, int numDoG, double sigma, double level) {
		this.x = x;
		this.y = y;
		this.yDog = yDog;
		this.xDog = xDog;
		this.numDoG = numDoG;
		this.sigma = sigma;
		this.level = level;
	}
	
	public SiftPoint(SiftPoint p) {
		x = p.x;
		y = p.y;
		yDog = p.yDog;
		xDog = p.xDog;
		numDoG = p.numDoG;
		orientation = p.orientation;
		sigma = p.sigma;
	}
	
	public double calcDif(SiftPoint p2) {		
		double[] d2;	
		double dist = 0.0;
			
		d2 = p2.descriptor;	
		for(int i=0; i<descriptor.length; i++)
			dist += descriptor[i]*d2[i];
		return Math.acos(dist);	
			
	}
}
