package javavis.jip2d.base.bitmaps;

import javavis.base.ImageType;
import javavis.base.JIPException;

/**
* Class to manage SHORT type images. The data is byte [0..65535]
* Short type uses the short primitive type of Java which stores the data in two complement,
* When using the Double methods, a value between 0..65535 will be returned. When using the
* getPixel method, a byte value -32768..32767 will be returned, being -32768 equals to 65535
*/
public class JIPBmpShort extends JIPImgBitmap {
	private static final long serialVersionUID = -7742990240235709822L;

	/**
	 * Image data
	 * @uml.property  name="bmp"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	short[][] bmp = null;

	public JIPBmpShort(JIPBmpShort bitmap) throws JIPException {
		super(bitmap);
		bmp = (short[][])bitmap.bmp.clone();
		for (int i=0; i< bitmap.bmp.length; i++) {
			bmp[i]=(short[])bitmap.bmp[i].clone();
		}
	}

	public JIPBmpShort(int w, int h) throws JIPException {
		super(w, h);
		bmp = new short[1][w * h];
	}

	public JIPBmpShort(int b, int w, int h) throws JIPException {
		super(b, w, h);
		bmp = new short[b][w * h];
	}

	public JIPBmpShort(int w, int h, short[] data) throws JIPException {
		super(w, h);
		if (w * h != data.length) 
			throw new JIPException("JIPBmpShort: dimensions are not the same size of input data");
		bmp = new short[1][];
		bmp[0]= (short[]) data.clone();
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelShort(int x, int y, short pix) throws JIPException {
		setPixelShort(0, x, y, pix);
	}

	/**
	* Set a value to a pixel.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @param value Value to assign.
	*/
	public void setPixelShort(int b, int x, int y, short pix) throws JIPException {
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
	public short getPixelShort(int x, int y) throws JIPException {
		return getPixelShort(0, x, y);
	}
	
	/**
	 * Get the value of a pixel of a bitmap.
	* @param x X Coord of the pixel (0 <= x <= width-1)
	* @param y Y Coord of the pixel (0 <= y <= height-1)
	* @return  Value of the pixel.
	*/
	public short getPixelShort(int b, int x, int y) throws JIPException {
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
	public void setAllPixelsShort(short[] data) throws JIPException {
		setAllPixelsShort(0, data);
	}

	/**
	* Set all pixels of a bitmap.
	* @param data Array of values.
	*/
	public void setAllPixelsShort(int b, short[] data) throws JIPException {
		if (width * height != data.length) 
			throw new JIPException("setAllPixels: dimensions of input data are not the same size of Bitmap");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("setAllPixels: band number out of range");
		bmp[b] = (short[]) data.clone();
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public short[] getAllPixelsShort() {
		return bmp[0];
	}

	/**
	 * Get all the values of the bitmap.
	 * @return Array containing the values.
	 */
	public short[] getAllPixelsShort(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		return bmp[b];
	}
	

	/*
	 * Implementations of the abstract methods
	 */
	public ImageType getType () {
		return ImageType.SHORT;
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
		if (pix>65535) pix=65535;
		bmp[b][x + y * width] = (short)pix;
	}
	
	public double getPixel (int x, int y) throws JIPException {
		return getPixel (0, x, y);
	}
	
	public double getPixel (int b, int x, int y) throws JIPException {
		if (x >= width || y >= height || x < 0 || y < 0) 
			throw new JIPException("getPixel: coordinates out of range");
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getPixel: band number out of range");
		return (bmp[b][x + y * width] & 0xFFFF);
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
		bmp[b] = new short[data.length];
		for (int i=0; i<data.length; i++) {
			if (data[i] > 65535) 
				data[i] = 65535;
			if (data[i] < 0.0) 
				data[i] = 0.0;
			bmp[b][i]=(short)data[i];
		}
	}

	public double[] getAllPixels() throws JIPException {
		return getAllPixels(0); 
	}

	public double[] getAllPixels(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("getAllPixels: band number out of range");
		double[] aux = new double[bmp[b].length];
		for (int i=0; i<bmp[b].length; i++) 
			aux[i]=bmp[b][i] & 0xFFFF;
		return aux;
	}
	
	public void removeBand(int b) throws JIPException {
		if (b<0 || b>=bmp.length) 
			throw new JIPException("removeBand: band number out of range");
		if (b==0 && bmp.length==1) 
			throw new JIPException("removeBand: a one band image can not be removed");
		short[][]aux = new short[bmp.length-1][];
		System.arraycopy(bmp, 0, aux, 0, b);
		System.arraycopy(bmp, b+1, aux, b, bmp.length - b - 1);
		bmp=aux;
	}
	
	public void appendBand(double[] data) throws JIPException {
		if (data==null) 
			throw new JIPException("appendBand: null input data");
		if (width * height != data.length) 
			throw new JIPException("appendBand: dimensions of input data are not the same size of Bitmap");
		short[][]aux = new short[bmp.length+1][];
		aux[aux.length-1] = new short[width * height];
		System.arraycopy(bmp, 0, aux, 0, bmp.length);
		for (int i=0; i<data.length; i++) {
			if (data[i] > 65535) 
				data[i] = 65535;
			if (data[i] < 0.0) 
				data[i] = 0.0;
			aux[aux.length-1][i]=(short)data[i];
		}
		bmp=aux;
	}
}
