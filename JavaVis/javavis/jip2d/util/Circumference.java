package javavis.jip2d.util;

import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Polygon2D;

/**
 * It implements a class with auxiliary methods to work with
 * circumferences in the HoughCirc function and in the cuentaMonedas application.
 */
public class Circumference extends Object {
	/**
	 * X coordinate of the center of circumference
	 * @uml.property  name="centerX"
	 */
	public int centerX;
	/**
	 * Y coordinate of the center of circumference
	 * @uml.property  name="centerY"
	 */
	public int centerY;

	/**
	 * Radius of the circumference (The value has to be integer type because we are worked with pixels)
	 * @uml.property  name="radio"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.geometrics.Polygon2D"
	 */
	public int radius;

	/**
	*It constructs a circumference without its values are started.
	*/
	public Circumference() {
	}

	/**
	*It constructs a circumference with its member variables start with the received values
	*as parameters.
	*/
	public Circumference(int cx, int cy, int r) {
		centerX = cx;
		centerY = cy;
		radius = r;
	}

	/**
	  *It returns the radius of circumference, it is calculated from its center 
	  *and a point which belong to the circumference.<BR>	  
	*/
	public static int calculatesRadius(int cent_X, int cent_Y, int coord_x, int coord_y) {
		int c, d;
		c = coord_x - cent_X;
		d = coord_y - cent_Y;
		return (int) Math.sqrt(c * c + d * d);
	}

	/**
	  *It returns all points that form the perimeter of circumference, from the center and the
	  *radius of the circumference.<BR>
	*/
	public static Polygon2D getPoints(Circumference circ) {
		int min_x, max_x, a, temp;

		//Calculates the maximum and minimum value that 'x' could have in the equation of the
		//circumference
		min_x = circ.centerX - circ.radius;
		max_x = circ.centerX + circ.radius;

		Polygon2D points = new Polygon2D();

		// Top of circumference,replace 'x' by 'y'
		for (int X = min_x; X <= max_x; X++) {
			a = X - circ.centerX;
			temp = circ.centerY + (int) Math.sqrt(circ.radius * circ.radius - a * a);

			points.addPoint(new Point2D(X, temp));
		}

		// Bottom of circumference,replace 'x' by 'y'
		for (int X = max_x - 1; X >= min_x + 1; X--) {
			a = X - circ.centerX;
			temp = circ.centerY - (int) Math.sqrt(circ.radius * circ.radius - a * a);

			points.addPoint(new Point2D(X, temp));
		}
		return points;
	}
}
