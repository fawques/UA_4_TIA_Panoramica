package javavis.jip2d.base.geometrics;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
*  class to define a JUNCTION geometric object. It contains geometric data
*/
public class JIPGeomJunctions extends JIPImgGeometric implements Serializable {
	private static final long serialVersionUID = -1183754399214648747L;
	
	/**
	 * Constructor
	 */
	public JIPGeomJunctions (JIPGeomJunctions img) throws JIPException {
		super(img);
		data=new ArrayList<Junction>();
		ArrayList<Junction> aux = (ArrayList<Junction>)img.getData();
		for (Junction s : aux) {
			data.add(new Junction(s));
		}
	}

	/**
	 * Constructor
	 */
	public JIPGeomJunctions (int w, int h) throws JIPException {
		super(w,h);
		data=new ArrayList<Junction>();
	}
	
	/**
	 * Adds a JunctionAux into the list of junctions
	 */
	public void addJunction (Junction j) throws JIPException {
		data.add(j);
	}
	
	/**
	 * Gets a junction
	 * @throws JIPException When index is out of bounds
	 */
	public Junction getJunction (int index) throws JIPException {
		if (index <0 || index > data.size()-1) {
			throw new JIPException ("JIPGeomJunction.getJunction: index of out bounds");
		}
		
		return (Junction)data.get(index);
	}
	
	public ImageType getType() {
		return ImageType.JUNCTION;
	}

}
