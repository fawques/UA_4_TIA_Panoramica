package javavis.jip2d.base.geometrics;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Abstract class to define a POINT geometric object. It contains geometric data
*/
public class JIPGeomPoint extends JIPImgGeometric implements Serializable {
	private static final long serialVersionUID = -7755470955703916794L;

	/**
	 * Constructor
	 */
	public JIPGeomPoint (JIPGeomPoint img) throws JIPException {
		super(img);
		data=new ArrayList<Point2D>();
		ArrayList<Point2D> aux = (ArrayList<Point2D>)img.getData();
		for (Point2D s : aux) {
			data.add(new Point2D(s));
		}
	}

	/**
	 * Constructor
	 */
	public JIPGeomPoint (int w, int h) throws JIPException {
		super(w,h);
		data=new ArrayList<Point2D>();
	}
	
	/**
	 * Adds a point into the list of points
	 */
	public void addPoint (Point2D p) throws JIPException {
		data.add(p);
	}
	
	/**
	 * Gets a point as a int array
	 * @param index Indicates the index (from 0 to n-1)
	 * @return int array: first element, X coordinate, second, Y coordinate
	 * @throws JIPException When index is out of bounds
	 */
	public Point2D getPoint (int index) throws JIPException {
		if (index <0 || index > data.size()-1) {
			throw new JIPException ("JIPGeomPoint.getPoint: index of out bounds");
		}
		return (Point2D)data.get(index);
	}
	
	public ImageType getType() {
		return ImageType.POINT;
	}

}
