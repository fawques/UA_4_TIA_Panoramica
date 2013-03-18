package javavis.jip2d.base.geometrics;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Abstract class to define a EDGES geometric object. It contains geometric data
*/
public class JIPGeomEdges extends JIPImgGeometric implements Serializable {
	private static final long serialVersionUID = -7755470955703916794L;

	/**
	 * Constructor
	 */
	public JIPGeomEdges (JIPGeomEdges img) throws JIPException {
		super(img);
		data=new ArrayList<Edge>();
		ArrayList<Edge> aux = (ArrayList<Edge>)img.getData();
		for (Edge s : aux) {
			data.add(new Edge(s));
		}
	}

	/**
	 * Constructor
	 */
	public JIPGeomEdges (int w, int h) throws JIPException {
		super(w,h);
		data=new ArrayList<Edge>();
	}
	
	/**
	 * Adds an edge at the end of the list
	 * @param d A list of integers defining an edge
	 * @throws JIPException
	 */
	public void addEdge (Edge d) throws JIPException {
		if (d==null) {
			throw new JIPException ("JIPGeomEdges.addEdge: data null");
		}
		data.add(d);
	}
	
	/**
	 * Gets an edge
	 * @param index Index of the edge to return
	 * @return A list of integers, representing an edge
	 * @throws JIPException
	 */
	public Edge getEdge (int index) throws JIPException {
		if (index < 0 || index > data.size()-1) {
			throw new JIPException ("JIPGeomEdge.getEdge: index out of bounds");
		}
		return (Edge)data.get(index);
	}
	
	public ImageType getType() {
		return ImageType.EDGES;
	}

}
