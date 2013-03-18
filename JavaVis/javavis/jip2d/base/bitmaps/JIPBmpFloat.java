package javavis.jip2d.base.bitmaps;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Class to manage FLOAT type images. The data is float
*/
public class JIPBmpFloat extends JIPImgBitmap {
	private static final long serialVersionUID = 7062403684031783391L;
	
	/**
	 * Image data
	 * @uml.property  name="bmp"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	float[][] bmp = null;

	public JIPBmpFloat(JIPBmpFloat bitmap) throws JIPException {
		super(bitmap);
		bmp = (float[][])bitmap.bmp.clone();
		for (int i=0; i< bitmap.bmp.length; i++) {
			bmp[i]=(float[])bitmap.bmp[i].clone();
		}
	}

	public JIPBmpFloat(int w, int h) throws JIPException {
		super(w, h);
		bmp = new float[1][w * h];
	}

	public JIPBmpFloat(int b, int w, int h) throws JIPException {
		super(b, w, h);
		bmp = new float[b][w * h];
	}

	public JIPBmpFloat(int w, int h, float[] data) throws JIPException {
		super(w, h);
		if (w * h != data.length) 
			throw new JIPException("JIPBmpFloat: dimensions are not the same size of input data");
		bmp = new float[1][];
		bmp[0]= (float[]) data.clone();
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelFloat(int x, int y, float pix) throws JIPException {
		setPixelFloat(0, x, y, pix);
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelFloat(int b, int x, int y, float pix) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("setPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setPixel: band number out of range");
		bmp[b][x + y * width] = pix;
	}
	
	/**
	 * Get the value of a pixel of a bitmap.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public float getPixelFloat(int x, int y) throws JIPException {
		return getPixelFloat(0, x, y);
	}
	
	/**
	 * Get the value of a pixel of a bitmap.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public float getPixelFloat(int b, int x, int y) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("getPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getPixel: band number out of range");
		return bmp[b][x + y * width];
	}

	/**
	* Set all pixels of a bitmap.
	* @param data Array of values.
	*/
	public void setAllPixelsFloat(float[] data) throws JIPException {
		setAllPixelsFloat(0, data);
	}

	/**
	* Set all pixels of a bitmap.
	* @param data Array of values.
	*/
	public void setAllPixelsFloat(int b, float[] data) throws JIPException {
		if (width * height != data.length) 
			throw new JIPException("setAllPixels: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixels: band number out of range");
		bmp[b] = (float[]) data.clone();
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public float[] getAllPixelsFloat() {
		return bmp[0];
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public float[] getAllPixelsFloat(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		return bmp[b];
	}
	

	/*
	 * Implementations of the abstract methods
	 */
	public ImageType getType () {
		return ImageType.FLOAT;
	}
	
	public int getNumBands () {
		return bmp.length;
	}
	
	public void setPixel(int x, int y, double pix) throws JIPException {
		setPixel(0, x, y, pix);
	}

	public void setPixel(int b, int x, int y, double pix) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("setPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setPixel: band number out of range");
		bmp[b][x + y * width] = (float)pix;
	}
	
	public double getPixel (int x, int y) throws JIPException {
		return getPixel(0, x, y);
	}
	
	public double getPixel (int b, int x, int y) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("getPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getPixel: band number out of range");
		return bmp[b][x + y * width];
	}

	public void setAllPixels(double[] data) throws JIPException {
		setAllPixels(0, data);
	}

	public void setAllPixels(int b, double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("setAllPixelsDouble: null input data");
		if (width * height != data.length) 
			throw new JIPException("setAllPixelsDouble: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixelsDouble: band number out of range");
		bmp[b] = new float[data.length];
		for (int i=0; i<data.length; i++) 
			bmp[b][i]=(float)data[i];
	}

	public double[] getAllPixels() throws JIPException {
		return getAllPixels(0);
	}

	public double[] getAllPixels(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixelsDouble: band number out of range");
		double[] aux = new double[bmp[b].length];
		for (int i=0; i<bmp[b].length; i++) aux[i]=bmp[b][i];
		return aux;
	}
	
	public void removeBand(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("removeBand: band number out of range");
		if (b==0 && bmp.length==1) 
			throw new JIPException("removeBand: a one band image can not be removed");
		float[][]aux = new float[bmp.length-1][];
		System.arraycopy(bmp, 0, aux, 0, b);
		System.arraycopy(bmp, b+1, aux, b, bmp.length - b - 1);
		bmp=aux;
	}
	
	public void appendBand(double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("appendBand: null input data");
		if (width * height != data.length) 
			throw new JIPException("appendBand: dimensions of input data are not the same size of Bitmap");
		float[][]aux = new float[bmp.length+1][];
		aux[aux.length-1] = new float[width * height];
		System.arraycopy(bmp, 0, aux, 0, bmp.length);
		for (int i=0; i<data.length; i++) 
			aux[aux.length-1][i]=(float)data[i];
		bmp=aux;
	}
}
