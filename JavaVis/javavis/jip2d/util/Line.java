package javavis.jip2d.util;

/**
 * Class used by HoughLine function
 */
public class Line {
	/**
	 * @uml.property  name="lTheta"
	 */
	double lTheta;
	/**
	 * @uml.property  name="lRho"
	 */
	double lRho; 
	public Line(double d1, double d2) {
		lTheta = d1;
		lRho = d2;
	}
	public double getTheta()  {return lTheta;}
	public double getRho()  {return lRho;}
}