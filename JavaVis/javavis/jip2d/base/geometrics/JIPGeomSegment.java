package javavis.jip2d.base.geometrics;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Abstract class to define a SEGMENT geometric object. It contains geometric data
*/
public class JIPGeomSegment extends JIPImgGeometric implements Serializable {
	private static final long serialVersionUID = -7755470955703916794L;
	
	/**
	 * Constructor
	 */
	public JIPGeomSegment (JIPGeomSegment img) throws JIPException {
		super(img);
		data=new ArrayList<Segment>();
		ArrayList<Segment> aux = (ArrayList<Segment>)img.getData();
		for (Segment s : aux) {
			data.add(new Segment(s));
		}
	}

	/**
	 * Constructor
	 */
	public JIPGeomSegment (int w, int h) throws JIPException {
		super(w,h);
		data=new ArrayList<Segment>();
	}
	
	/**
	 * Adds a segment into the list of segment
	 */
	public void addSegment (Segment s) throws JIPException {
		data.add(s);
	}
	
	/**
	 * Gets a segment as a int array
	 * @param index Indicates the index (from 0 to n-1)
	 * @return int array: first element, X coordinate of the first point, second, Y coordinate of the first, 
	 * third element, X coordinate of the second point, fourth, Y coordinate of the second.
	 * @throws JIPException When index is out of bounds
	 */
	public Segment getSegment (int index) throws JIPException {
		if (index <0 || index > data.size()-1) {
			throw new JIPException ("JIPGeomSegment.getSegment: index of out bounds");
		}
		
		return (Segment)data.get(index);
	}
	
	public ImageType getType() {
		return ImageType.SEGMENT;
	}

}
