package javavis.jip2d.base.geometrics;

import java.util.ArrayList;

public class Polygon2D extends GeomData {

	/**
	 * @uml.property  name="points"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.geometrics.Point2D"
	 */
	private ArrayList<Point2D> points;
	
	public Polygon2D () {
		points = new ArrayList<Point2D>();
	}
	
	public Polygon2D (ArrayList<Point2D> data) {
		points=new ArrayList<Point2D>();
		for (Point2D p : data) {
			this.points.add(new Point2D(p));
		}
	}
	
	public Polygon2D (Polygon2D p) {
		points=new ArrayList<Point2D>();
		for (Point2D po : p.points) {
			this.points.add(new Point2D(po));
		}
	}
	
	public String toString() {
		String ret="";
		int i=0;
		
		for (Point2D p : points) {
			ret += "x"+i+"="+p.getX()+" y"+i+"="+p.getY()+"\n";
		}
		return ret;
	}
	
	public int length () {
		return points.size();
	}

	public ArrayList<Point2D> getData() {
		return points;
	}

	public void setData(ArrayList<Point2D> points) {
		this.points = points;
	}
	
	public void addPoint(Point2D p) {
		points.add(p);
	}
}
