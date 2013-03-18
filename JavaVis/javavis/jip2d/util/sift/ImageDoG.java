package javavis.jip2d.util.sift;

import javavis.base.JIPException;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * Stores the images of which the Gaussian pyramid consists.
 * The pixel values can contain negative values.
 */
public class ImageDoG {
	/**
	 * @uml.property  name="pixels" multiplicity="(0 -1)" dimension="2"
	 */
	public double[][] pixels;
	/**
	 * @uml.property  name="width"
	 */
	public int width;
	/**
	 * @uml.property  name="height"
	 */
	public int height;
	/**
	 * Represents the sigma2, i.e., the images are smoothed by a sigma (sigma1 - sigma2)
	 * @uml.property  name="sigma"
	 */
	public double sigma;
	
	public ImageDoG(double[][] pixels, int w, int h) {
		this.pixels = pixels;
		this.width = w;
		this.height = h;
	}

	public ImageDoG(double[][] pixels) {
		this.pixels = pixels;
		this.width = pixels[0].length;
		this.height = pixels.length;
	}
	
	
	public ImageDoG(int w, int h) {
		this.width = w;
		this.height = h;
		pixels = new double[w][h];
	}
	
	/**
	 * Deduct two JavaVis images, and obtains a ImageDoG
	 * @param imgA image 1
	 * @param imgB image 2
	 * @return The image result
	 * @throws JIPException 
	 */
	public static ImageDoG substract(JIPImgBitmap imgA, JIPImgBitmap imgB) throws JIPException {
		ImageDoG res;
		int w, h;
		
		w = imgA.getWidth();
		h = imgA.getHeight();
		res = new ImageDoG(w, h);		
		for(int x=0; x<w; x++) 
			for(int y=0; y<h; y++) 
				res.pixels[x][y] = imgA.getPixel(x,y) - imgB.getPixel(x,y);		
		
		return res;
	}
	
	/**
	 * Returns the lowest gray level of the image.
	 */
	public double getMin() {
		double min;
		
		min = Double.MAX_VALUE;
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				if (pixels[x][y]<min)
					min = pixels[x][y];
		return min;		
	}

	/**
	 * Returns the highest gray level of the image.
	 */
	public double getMax() {
		double max;
		
		max = Double.MIN_VALUE;
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				if (pixels[x][y]>max)
					max = pixels[x][y];
		return max;		
	}

	
	/**
	 * Scales the gray values of the image, in a specified range.
	 * @param min gray value, it will scale to 0
	 * @param max gray value, it will scale to 0
	 * @return The scaled gray values
	 */
	public void scalePixels(double min, double max) {
		double range;
		
		range = max - min;
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				pixels[x][y] = (pixels[x][y]-min)/range;
	}
	
	/**
	 * Returns a FLOAT JIPImage, built from this image
	 * @return
	 * @throws JIPException 
	 */
	public JIPImage getJIPImage() throws JIPException {
		JIPImgBitmap img;
		int x,y;
		
		img = new JIPBmpFloat(width, height);
		for(x=0; x<width; x++)
			for(y=0; y<height; y++)
				img.setPixel(x, y, (float)pixels[x][y]);
		return img;
	}
	
	
}