package javavis.jip2d.base.bitmaps;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Class to manage COLOR type images. The data contains three bands of byte data [0..255]
* Byte type use the byte primitive type of Java which stores the data in two complement,
* When using the Double methods, a value between 0..255 will be returned. When using the
* getPixel method, a byte value -128..127 will be returned, being -127 equals to 255
*/
public class JIPBmpColor extends JIPImgBitmap {
	private static final long serialVersionUID = -6880017854868951201L;

	/**
	 * Image data
	 * @uml.property  name="bmp"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	byte[][] bmp = null;

	public JIPBmpColor(JIPBmpColor bitmap) throws JIPException {
		super(bitmap);
		bmp = (byte[][])bitmap.bmp.clone();
		for (int i=0; i< bitmap.bmp.length; i++) {
			bmp[i]=(byte[])bitmap.bmp[i].clone();
		}
	}

	public JIPBmpColor(int w, int h) throws JIPException {
		super(w, h);
		bmp = new byte[3][w * h];
	}

	public JIPBmpColor(int w, int h, byte[][] data) throws JIPException {
		super(w, h);
		if (data.length!=3) 
			throw new JIPException("JIPBmpColor: data must contain three bands");
		if (w * h != data[0].length || w * h != data[1].length || w * h != data[2].length) 
			throw new JIPException("JIPBmpColor: dimensions are not the same size of one of the bands");
		bmp= (byte[][]) data.clone();
		for (int i=0; i< data.length; i++) {
			bmp[i]=(byte[])data[i].clone();
		}
	}

	/**
	* Set a value to a pixel at the red band (0).
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelByteRed(int x, int y, byte pix) throws JIPException {
		setPixelByte(0, x, y, pix);
	}

	/**
	* Set a value to a pixel at the green band (1).
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelByteGreen(int x, int y, byte pix) throws JIPException {
		setPixelByte(1, x, y, pix);
	}

	/**
	* Set a value to a pixel at the blue band (0).
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelByteBlue(int x, int y, byte pix) throws JIPException {
		setPixelByte(2, x, y, pix);
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
	 * Get the value of a pixel at the red band (0).
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public byte getPixelByteRed(int x, int y) throws JIPException {
		return getPixelByte(0, x, y);
	}
	
	/**
	 * Get the value of a pixel at the green band (1).
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public byte getPixelByteGreen(int x, int y) throws JIPException {
		return getPixelByte(1, x, y);
	}
	
	/**
	 * Get the value of a pixel at the blue band (2).
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public byte getPixelByteBlue(int x, int y) throws JIPException {
		return getPixelByte(2, x, y);
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
	* Set all pixels of a bitmap at the red band (0).
	* @param data Array of values.
	*/
	public void setAllPixelsByteRed(byte[] data) throws JIPException {
		setAllPixelsByte(0, data);
	}

	/**
	* Set all pixels of a bitmap at the green band (1).
	* @param data Array of values.
	*/
	public void setAllPixelsByteGreen(byte[] data) throws JIPException {
		setAllPixelsByte(1, data);
	}

	/**
	* Set all pixels of a bitmap at the blue band (2).
	* @param data Array of values.
	*/
	public void setAllPixelsByteBlue(byte[] data) throws JIPException {
		setAllPixelsByte(2, data);
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
	 * Get all the values of the bitmap at the red band(0).
	 * @return Array containing the values.
	 */
	public byte[] getAllPixelsByteRed() {
		return bmp[0];
	}

	/**
	 * Get all the values of the bitmap at the green band(1).
	 * @return Array containing the values.
	 */
	public byte[] getAllPixelsByteGreen() {
		return bmp[1];
	}

	/**
	 * Get all the values of the bitmap at the blue band(2).
	 * @return Array containing the values.
	 */
	public byte[] getAllPixelsByteBlue() {
		return bmp[2];
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
		return ImageType.COLOR;
	}
	
	public int getNumBands () {
		return bmp.length;
	}
	
	public void setPixel(int x, int y, double pix) throws JIPException {
		setPixel(0, x, y, pix);
		setPixel(1, x, y, pix);
		setPixel(2, x, y, pix);
	}
	
	public void setPixelRed(int x, int y, double pix) throws JIPException {
		setPixel(0, x, y, pix);
	}
	
	public void setPixelGreen(int x, int y, double pix) throws JIPException {
		setPixel(1, x, y, pix);
	}
	
	public void setPixelBlue(int x, int y, double pix) throws JIPException {
		setPixel(2, x, y, pix);
	}

	public void setPixel(int b, int x, int y, double pix) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("setPixel: coordinates ("+x+", "+y+") out of the range (0.."+width+", "+"0.."+height+").");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setPixel: band number out of range");
		if (pix<0) pix=0;
		if (pix>255.0) pix=255.0;
		bmp[b][x + y * width] = (byte)pix;
	}
	
	public double getPixel (int x, int y) throws JIPException {
		return getPixel(0, x, y);
	}
	
	public double getPixelRed (int x, int y) throws JIPException {
		return getPixel(0, x, y);
	}
	
	public double getPixelGreen (int x, int y) throws JIPException {
		return getPixel(1, x, y);
	}
	
	public double getPixelBlue (int x, int y) throws JIPException {
		return getPixel(2, x, y);
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
		setAllPixels(1, data);
		setAllPixels(2, data);
	}

	public void setAllPixelsRed(double[] data) throws JIPException {
		setAllPixels(0, data);
	}

	public void setAllPixelsGreen(double[] data) throws JIPException {
		setAllPixels(1, data);
	}

	public void setAllPixelsBlue(double[] data) throws JIPException {
		setAllPixels(2, data);
	}

	public void setAllPixels(int b, double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("setAllPixels: null input data");
		if (width * height != data.length) 
			throw new JIPException("setAllPixels: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixels: band number out of range");
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

	public double[] getAllPixelsRed() throws JIPException {
		return getAllPixels(0);
	}

	public double[] getAllPixelsGreen() throws JIPException {
		return getAllPixels(1);
	}

	public double[] getAllPixelsBlue() throws JIPException {
		return getAllPixels(2);
	}

	public double[] getAllPixels(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		double[] aux = new double[bmp[b].length];
		for (int i=0; i<bmp[b].length; i++) 
			aux[i]=(bmp[b][i] & 0xFF);
		return aux;
	}
	
	public void removeBand(int b) throws JIPException {
		throw new JIPException("removeBand: a band from a color image can not be removed");
	}
	
	public void appendBand(double[] data) throws JIPException {
		throw new JIPException("appendBand: a band can not be appended to a color image");
	}
}
