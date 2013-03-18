package javavis.jip2d.base.bitmaps;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Class to manage BIT type images. The data is boolean [0..1]
* When using the Double methods, a 0 or 1 value will be returned. When using the
* getPixel method, a boolean value 
*/
public class JIPBmpBit extends JIPImgBitmap {
	private static final long serialVersionUID = -839307623137706078L;

	/**
	 * Image data
	 * @uml.property  name="bmp"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	boolean[][] bmp = null;

	public JIPBmpBit(JIPBmpBit bitmap) throws JIPException {
		super(bitmap);
		bmp = (boolean[][])bitmap.bmp.clone();
		for (int i=0; i< bitmap.bmp.length; i++) {
			bmp[i]=(boolean[])bitmap.bmp[i].clone();
		}
	}

	public JIPBmpBit(int w, int h) throws JIPException {
		super(w, h);
		bmp = new boolean[1][w * h];
	}

	public JIPBmpBit(int b, int w, int h) throws JIPException {
		super(b, w, h);
		bmp = new boolean[b][w * h];
	}

	public JIPBmpBit(int w, int h, boolean[] data) throws JIPException {
		super(w, h);
		if (w * h != data.length) 
			throw new JIPException("JIPBmpBit: dimensions are not the same size of input data");
		bmp = new boolean[1][];
		bmp[0]= (boolean[]) data.clone();
	}


	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelBool(int x, int y, boolean pix) throws JIPException {
		setPixelBool(0, x, y, pix);
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelBool(int b, int x, int y, boolean pix) throws JIPException {
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
	public boolean getPixelBool(int x, int y) throws JIPException {
		return getPixelBool(0, x, y);
	}
	
	/**
	 * Get the value of a pixel of a bitmap.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public boolean getPixelBool(int b, int x, int y) throws JIPException {
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
	public void setAllPixelsBool(boolean[] data) throws JIPException {
		setAllPixelsBool(0, data);
	}

	/**
	* Set all pixels of a bitmap.
	* @param data Array of values.
	*/
	public void setAllPixelsBool(int b, boolean[] data) throws JIPException {
		if (width * height != data.length) 
			throw new JIPException("setAllPixels: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixels: band number out of range");
		bmp[b] = (boolean[]) data.clone();
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public boolean[] getAllPixelsBool() {
		return bmp[0];
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public boolean[] getAllPixelsBool(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		return bmp[b];
	}
	

	/*
	 * Implementations of the abstract methods
	 */
	public ImageType getType () {
		return ImageType.BIT;
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
		bmp[b][x + y * width] = (pix>=0 && pix<0.5?false:true);
	}
	
	public double getPixel (int x, int y) throws JIPException {
		return getPixel(0, x, y);
	}
	
	public double getPixel (int b, int x, int y) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("getPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getPixel: band number out of range");
		return (bmp[b][x + y * width]?1.0:0.0);
	}

	public void setAllPixels(double[] data) throws JIPException {
		setAllPixels(0, data);
	}

	public void setAllPixels(int b, double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("setAllPixels: null input data");
		if (width * height != data.length) 
			throw new JIPException("setAllPixels: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixels: band number out of range");
		bmp[b] = new boolean[data.length];
		for (int i=0; i<data.length; i++) 
			bmp[b][i]=(data[i]>=0 && data[i]<0.5?false:true);
	}

	public double[] getAllPixels() throws JIPException {
		return getAllPixels(0);
	}

	public double[] getAllPixels(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		double[] aux = new double[bmp[b].length];
		for (int i=0; i<bmp[b].length; i++) 
			aux[i]=(bmp[b][i]?1.0:0.0);
		return aux;
	}
	
	public void removeBand(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("removeBand: band number out of range");
		if (b==0 && bmp.length==1) 
			throw new JIPException("removeBand: a one band image can not be removed");
		boolean[][]aux = new boolean[bmp.length-1][];
		System.arraycopy(bmp, 0, aux, 0, b);
		System.arraycopy(bmp, b+1, aux, b, bmp.length - b - 1);
		bmp=aux;
	}
	
	public void appendBand(double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("appendBand: null input data");
		if (width * height != data.length) 
			throw new JIPException("appendBand: dimensions of input data are not the same size of Bitmap");
		boolean[][]aux = new boolean[bmp.length+1][];
		aux[aux.length-1] = new boolean[width * height];
		System.arraycopy(bmp, 0, aux, 0, bmp.length);
		for (int i=0; i<data.length; i++) 
			aux[aux.length-1][i]=(data[i]==0?false:true);
		bmp=aux;
	}
}
