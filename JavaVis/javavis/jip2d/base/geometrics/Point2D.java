package javavis.jip2d.base.geometrics;

import java.awt.Color;

public class Point2D extends GeomData {
	
	/**
	 * @uml.property  name="x"
	 */
	private int x;
	/**
	 * @uml.property  name="y"
	 */
	private int y;
	
	public Point2D(int xi, int yi) {
		x=xi;
		y=yi;
	}
	
	public Point2D(int xi, int yi, Color c) {
		x=xi;
		y=yi;
		color=c;
	}
	
	public Point2D(Point2D p) {
		x=p.x;
		y=p.y;
		color=p.color;
	}
	
	public String toString() {
		return "x="+x+" y="+y;
	}

	/**
	 * @return
	 * @uml.property  name="x"
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x
	 * @uml.property  name="x"
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return
	 * @uml.property  name="y"
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y
	 * @uml.property  name="y"
	 */
	public void setY(int y) {
		this.y = y;
	}

}
