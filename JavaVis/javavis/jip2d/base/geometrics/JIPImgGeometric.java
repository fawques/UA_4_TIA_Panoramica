package javavis.jip2d.base.geometrics;

import java.io.Serializable;
import java.util.ArrayList;
import javavis.base.JIPException;
import javavis.jip2d.base.JIPImage;

/**
* Abstract class to define a geometric object. It contains geometric data, implemented by an array of Integer
*/
public abstract class JIPImgGeometric extends JIPImage implements Serializable {
	private static final long serialVersionUID = -7755470955703916794L;
	
	/**
	 * Data. For each type, a different type is stored in this arraylist
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.geometrics.GeomData"
	 */
	ArrayList data;

	/**
	 * Constructor
	 */
	public JIPImgGeometric (JIPImgGeometric img) throws JIPException {
		super(img);
	}

	/**
	 * Constructor
	 */
	public JIPImgGeometric (int w, int h) throws JIPException {
		super(w,h);
	}
	

	/**
	 * Sets the data for the geometric type
	 * @param data  Data to set
	 * @uml.property  name="data"
	 */
	public void setData (ArrayList data) {
		this.data=data;
	}
	
	/**
	 * Gets the data from the geometric type
	 * @return  Geometric data
	 * @uml.property  name="data"
	 */
	public ArrayList getData () {
		return data;
	}
	
	/**
	 * Gets the length of the geometric data
	 * @return Length of the geometric data
	 */
	public int getLength() {
		return data.size();
	}
	
}
