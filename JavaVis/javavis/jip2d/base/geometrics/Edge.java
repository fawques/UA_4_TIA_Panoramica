package javavis.jip2d.base.geometrics;

import java.util.ArrayList;
import java.util.Collections;

public class Edge extends GeomData {
	
	/**
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.geometrics.Point2D"
	 */
	ArrayList<Point2D> data;
	/**
	 * @uml.property  name="values"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Float"
	 */
	ArrayList<Float> values; // Values of the original image
	
	public Edge () {
		data = new ArrayList<Point2D>();
		values = new ArrayList<Float>();
	}
	
	public Edge (Edge e) {
		data = new ArrayList<Point2D>();
		for (Point2D p : e.data) {
			data.add(new Point2D(p));
		}
		values = new ArrayList<Float>();
		for (Float f : e.values) {
			values.add(f);
		}
	}
	
	public Edge (ArrayList<Point2D> data) {
		this.data = new ArrayList<Point2D>();
		for (Point2D p : data) {
			this.data.add(new Point2D(p));
		}
		values=new ArrayList<Float>();
	}
	
	public Edge (ArrayList<Point2D> data, ArrayList<Float> values) {
		this.data = new ArrayList<Point2D>();
		for (Point2D p : data) {
			this.data.add(new Point2D(p));
		}
		this.values=new ArrayList<Float>();
		this.values = new ArrayList<Float>();
		for (Float f : values) {
			this.values.add(f);
		}
	}
	
	public String toString() {
		String ret="";
		int i=0;
		
		for (Point2D p : data) {
			ret += "x"+i+"="+p.getX()+" y"+i+"="+p.getY()+"\n";
		}
		return ret;
	}
	
	/**
	 * Reverse the list of points
	 */
	public void reverse() {
		Collections.reverse(data);
		Collections.reverse(values);
	}
	
	public int length () {
		return data.size();
	}

	public ArrayList<Point2D> getData() {
		return data;
	}

	public void setData(ArrayList<Point2D> data) {
		this.data = data;
	}
	
	public ArrayList<Float> getValues() {
		return values;
	}

	public void setValues(ArrayList<Float> values) {
		this.values = values;
	}

	public void addPoint(Point2D p) {
		data.add(p);
	}

	public void addValue(float v) {
		values.add(v);
	}
}
