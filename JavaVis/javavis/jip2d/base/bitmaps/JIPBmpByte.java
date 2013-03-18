package javavis.jip2d.base.bitmaps;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Class to manage BYTE type images. The data is byte [0..255]
* Byte type use the byte primitive type of Java which stores the data in two complement,
* When using the Double methods, a value between 0..255 will be returned. When using the
* getPixel method, a byte value -128..127 will be returned, being -127 equals to 255
*/
public class JIPBmpByte extends JIPImgBitmap {
	private static final long serialVersionUID = -6880017854868951201L;

	/**
	 * Image data
	 * @uml.property  name="bmp"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	byte[][] bmp = null;

	public JIPBmpByte(JIPBmpByte bitmap) throws JIPException {
		super(bitmap);
		bmp = (byte[][])bitmap.bmp.clone();
		for (int i=0; i< bitmap.bmp.length; i++) {
			bmp[i]=(byte[])bitmap.bmp[i].clone();
		}
	}

	public JIPBmpByte(int w, int h) throws JIPException {
		super(w, h);
		bmp = new byte[1][w * h];
	}

	public JIPBmpByte(int b, int w, int h) throws JIPException {
		super(b, w, h);
		bmp = new byte[b][w * h];
	}

	public JIPBmpByte(int w, int h, byte[] data) throws JIPException {
		super(w, h);
		if (w * h != data.length) 
			throw new JIPException("JIPBmpByte: dimensions are not the same size of input data");
		bmp = new byte[1][];
		bmp[0]= (byte[]) data.clone();
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelByte(int x, int y, byte pix) throws JIPException {
		setPixelByte(0, x, y, pix);
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelByte(int b, int x, int y, byte pix) throws JIPException {
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
	public byte getPixelByte(int x, int y) throws JIPException {
		return getPixelByte(0, x, y);
	}
	
	/**
	 * Get the value of a pixel of a bitmap.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public byte getPixelByte(int b, int x, int y) throws JIPException {
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
	public void setAllPixelsByte(byte[] data) throws JIPException {
		setAllPixelsByte(0, data);
	}

	/**
	* Set all pixels of a bitmap.
	* @param data Array of values.
	*/
	public void setAllPixelsByte(int b, byte[] data) throws JIPException {
		if (width * height != data.length) 
			throw new JIPException("setAllPixels: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixels: band number out of range");
		bmp[b] = (byte[]) data.clone();
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public byte[] getAllPixelsByte() {
		return bmp[0];
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public byte[] getAllPixelsByte(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		return bmp[b];
	}
	

	/*
	 * Implementations of the abstract methods
	 */
	public ImageType getType () {
		return ImageType.BYTE;
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
		if (pix<0) pix=0;
		if (pix>255) pix=255;
		bmp[b][x + y * width] = (byte)pix;
	}
	
	public double getPixel (int x, int y) throws JIPException {
		return getPixel(0, x, y);
	}
	
	public double getPixel (int b, int x, int y) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("getPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getPixel: band number out of range");
		return (bmp[b][x + y * width] & 0xFF);
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
		bmp[b] = new byte[data.length];
		for (int i=0; i<data.length; i++) {
			if (data[i] > 255) 
				data[i] = 255;
			if (data[i] < 0.0) 
				data[i] = 0.0;
			bmp[b][i]=(byte)data[i];
		}
	}

	public double[] getAllPixels() throws JIPException {
		return getAllPixels(0);
	}

	public double[] getAllPixels(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixelsDouble: band number out of range");
		double[] aux = new double[bmp[b].length];
		for (int i=0; i<bmp[b].length; i++) 
			aux[i] = (bmp[b][i] & 0xFF);
		return aux;
	}
	
	public void removeBand(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("removeBand: band number out of range");
		if (b==0 && bmp.length==1) 
			throw new JIPException("removeBand: a one band image can not be removed");
		byte[][]aux = new byte[bmp.length-1][];
		System.arraycopy(bmp, 0, aux, 0, b);
		System.arraycopy(bmp, b+1, aux, b, bmp.length - b - 1);
		bmp=aux;
	}
	
	public void appendBand(double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("appendBand: null input data");
		if (width * height != data.length) 
			throw new JIPException("appendBand: dimensions of input data are not the same size of Bitmap");
		byte[][]aux = new byte[bmp.length+1][];
		System.arraycopy(bmp, 0, aux, 0, bmp.length);
		aux[aux.length-1] = new byte[width * height];
		for (int i=0; i<data.length; i++) {
			if (data[i] > 255) 
				data[i] = 255;
			if (data[i] < 0.0) 
				data[i] = 0.0;
			aux[aux.length-1][i]=(byte)data[i];
		}
		bmp=aux;
	}
}
