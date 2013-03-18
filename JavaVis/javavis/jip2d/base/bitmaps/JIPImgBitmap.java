package javavis.jip2d.base.bitmaps;

import java.io.Serializable;

import javavis.base.JIPException;
import javavis.jip2d.base.JIPImage;

/**
*  Abstract class which defines a bitmap image. The abstract methods (double values) are declared to define a
*  normalized way to access to a pixel value. Internally, double values are converted to a specified (boolean, byte, etc.)
*  value.
*/
public abstract class JIPImgBitmap extends JIPImage implements Serializable {
	private static final long serialVersionUID = 1531335296997938391L;

	/**
	 * Constructor
	 */
	public JIPImgBitmap (JIPImgBitmap img) throws JIPException {
		super(img);
	}

	/**
	 * Constructor
	 */
	public JIPImgBitmap (int w, int h) throws JIPException {
		super(w,h);
	}

	/**
	 * Constructor
	 */
	public JIPImgBitmap (int b, int w, int h) throws JIPException {
		super(w,h);
		if (b<=0) throw new JIPException("JIPImgBitmap: number of bands can not be less or equal to zero");
	}

	/**
	* Set a value to a pixel. This method will convert the double value to a
	* primitive type (e.g. byte) internally
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public abstract void setPixel (int x, int y, double value) throws JIPException;

	/**
	* Set a value to a pixel in a specified band. This method will convert the double value to a
	* primitive type (e.g. byte) internally
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public abstract void setPixel (int b, int x, int y, double value) throws JIPException;
	
	/**
	 * Get the value of a pixel of a bitmap. This method will convert the double value to a
	* primitive type (e.g. byte) internally
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public abstract double getPixel (int x, int y) throws JIPException;
	
	/**
	 * Get the value of a pixel of a bitmap from a specified band. This method will convert the double value to a
	* primitive type (e.g. byte) internally
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public abstract double getPixel (int b, int x, int y) throws JIPException;
	
	/**
	* Set all pixels to a bitmap (to the first band). This method will convert the double value to a
	* primitive type (e.g. byte) internally
	* @param data Array of values.
	*/
	public abstract void setAllPixels(double[] data) throws JIPException;
	
	/**
	* Set all pixels to a bitmap of a specified band. This method will convert the double value to a
	* primitive type (e.g. byte) internally
	* @param data Array of values.
	*/
	public abstract void setAllPixels(int b, double[] data) throws JIPException;

	/**
	 * Get all the values of the bitmap.This method will convert the internal type value 
	 * to a double value
	 * @return Array containing the values.
	 */
	public abstract double[] getAllPixels() throws JIPException;

	/**
	 * Get all the values of the bitmap (from a specified band).This method will convert the internal type value 
	 * to a double value
	 * @return Array containing the values.
	 */
	public abstract double[] getAllPixels(int b) throws JIPException;

	/**
	 * Get the number of bands of the image
	 * @return int value with the number of bands.
	 */
	public abstract int getNumBands();
	
	/**
	 * Removes the band indicated by parameter
	 * @param b Band number to delete
	 * @throws JIPException
	 */
	public abstract void removeBand(int b) throws JIPException;
	
	/**
	 * Append a band at the end of the image
	 */
	public abstract void appendBand(double[] data) throws JIPException;
	
}
