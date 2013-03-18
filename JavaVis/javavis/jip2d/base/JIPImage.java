package javavis.jip2d.base;

import java.io.Serializable;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPBmpShort;
import javavis.jip2d.base.geometrics.JIPGeomEdges;
import javavis.jip2d.base.geometrics.JIPGeomJunctions;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPGeomPoly;
import javavis.jip2d.base.geometrics.JIPGeomSegment;

import org.apache.log4j.Logger;

/**
 * Abstract class which defines the image object. An image can be one of two types: bitmap or geometric. A bitmap is a "traditional" image (matrix of pixels). It can contain a set bands, where each band is a bitmap. The width, height and type of each band are the same to every band. The geometric type allows to use point data like segments, points, edges, using just the point coordinates.
 */
public abstract class JIPImage implements Serializable {
	private static final long serialVersionUID = 4946438402771713101L;
	private static Logger logger = Logger.getLogger(JIPImage.class);

	/**
	 * Name of the image
	 * @uml.property  name="name"
	 */
	protected String name = "[Unnamed]";
	
	/**
	 * Width of the image
	 * @uml.property  name="width"
	 */
	protected int width = -1;

	/**
	 * Height of the image
	 * @uml.property  name="height"
	 */
	protected int height = -1;
	
	
	/**
	 * Constructor
	 */
	public JIPImage (JIPImage img) throws JIPException {
		if (img == null) 
			throw new JIPException("JIPImage: null object passed as parameter"); 
		width=img.width;
		height=img.height;
	}

	/**
	 * Constructor
	 */
	public JIPImage (int w, int h) throws JIPException {
		if (w <= 0 || h <= 0) 
			throw new JIPException("JIPImage: dimensions can not be negative or zero");
		width=w;
		height=h;
	}
	
	/**
	 * Makes a copy of the current image
	 * @return
	 * @throws JIPException
	 */
	public JIPImage clone () {
		try {
			switch (getType()) {
				case BIT: return new JIPBmpBit((JIPBmpBit)this);
				case BYTE: return new JIPBmpByte((JIPBmpByte)this);
				case SHORT: return new JIPBmpShort((JIPBmpShort)this);
				case FLOAT: return new JIPBmpFloat((JIPBmpFloat)this);
				case COLOR: return new JIPBmpColor((JIPBmpColor)this);
				case POINT: return new JIPGeomPoint((JIPGeomPoint)this);
				case SEGMENT: return new JIPGeomSegment((JIPGeomSegment)this);
				case POLY: return new JIPGeomPoly((JIPGeomPoly)this);
				case EDGES: return new JIPGeomEdges((JIPGeomEdges)this);
				case JUNCTION: return new JIPGeomJunctions((JIPGeomJunctions)this);
				default: return null;
			}
		} catch (JIPException e) {logger.error(e); return null;}
	}
	
	/**
	 * Static method to create an image depending of its type
	 * @param w Width
	 * @param h Height
	 * @param t Type
	 * @return New Image
	 */
	public static JIPImage newImage (int w, int h, ImageType t) throws JIPException {
		return newImage(1, w, h, t);
	}
	
	/**
	 * Static method to create an image depending of its type
	 * @param b Band number
	 * @param w Width
	 * @param h Height
	 * @param t Type
	 * @return New Image
	 */	
	public static JIPImage newImage (int b, int w, int h, ImageType t) throws JIPException {
		switch (t) {
			case BIT: return new JIPBmpBit(b, w, h);
			case BYTE: return new JIPBmpByte(b, w, h);
			case SHORT: return new JIPBmpShort(b, w, h);
			case FLOAT: return new JIPBmpFloat(b, w, h);
			case COLOR: return new JIPBmpColor(w, h);
			case POINT: return new JIPGeomPoint(w, h);
			case SEGMENT: return new JIPGeomSegment(w, h);
			case POLY: return new JIPGeomPoly(w, h);
			case EDGES: return new JIPGeomEdges(w, h);
			case JUNCTION: return new JIPGeomJunctions(w, h);
			default: return null;
		}
	}

	/**
	 * Get the width of the image.
	 * @return  Number of columns.
	 * @uml.property  name="width"
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of the image.
	 * @return  Number of rows.
	 * @uml.property  name="height"
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * It gets the image name.
	 * @return  Image name.
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * It assigns the image name.	 
	 * @param nom  Image name.
	 * @uml.property  name="name"
	 */
	public void setName(String nom) {
		if (nom == null) 
			name = "[Unnamed]";
		else name = nom;
	}
	
	/**
	 * It gets the image type.
	 * @return  Constant which identify the type.
	 * @uml.property  name="type"
	 * @uml.associationEnd  readOnly="true"
	 */
	public abstract ImageType getType();
}
