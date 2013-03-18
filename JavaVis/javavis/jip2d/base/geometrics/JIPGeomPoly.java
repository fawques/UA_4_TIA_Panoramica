package javavis.jip2d.base.geometrics;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Abstract class to define a POLY geometric object. It contains geometric data
*/
public class JIPGeomPoly extends JIPImgGeometric implements Serializable {
	private static final long serialVersionUID = -7755470955703916794L;
	
	/**
	 * Constructor
	 */
	public JIPGeomPoly (JIPGeomPoly img) throws JIPException {
		super(img);
		data=new ArrayList<Polygon2D>();
		ArrayList<Polygon2D> aux = (ArrayList<Polygon2D>)img.getData();
		for (Polygon2D s : aux) {
			data.add(new Polygon2D(s));
		}
	}

	/**
	 * Constructor
	 */
	public JIPGeomPoly (int w, int h) throws JIPException {
		super(w,h);
		data=new ArrayList<Polygon2D>();
	}
	
	/**
	 * Adds an poly at the end of the list
	 * @param d A list of integers defining a polygon
	 * @throws JIPException
	 */
	public void addPoly (Polygon2D d) throws JIPException {
		if (d==null) {
			throw new JIPException ("JIPGeomPoly.addPoly: data null");
		}
		data.add(d);
	}
	
	/**
	 * Gets a polygon
	 * @param index Index of the polygon to return
	 * @return A list of integers, representing a polygon
	 * @throws JIPException
	 */
	public Polygon2D getPoly (int index) throws JIPException {
		if (index < 0 || index > data.size()-1) {
			throw new JIPException ("JIPGeomPoly.getPoly: index out of bounds");
		}
		return (Polygon2D)data.get(index);
	}
	
	public ImageType getType() {
		return ImageType.POLY;
	}

}
