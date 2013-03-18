package javavis.jip2d.util.sift;

import java.util.*;

import javavis.base.JIPException;
import javavis.jip2d.base.*;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.functions.*;

/**
 * Gaussian pyramid which samples the space of scales with Gaussians.
 * It is calculated with the method of "fast computation of characteristic scale using a
 * half octave pyramid" - Crowley, Riff, Piater.
 */
public class GaussianPyramid {
	/**
	 * Sigma of the Gaussian used for smoothing
	 */
	public final static float SIGMA = 1.6f;
	/**
	 * Scaling between pyramid's levels
	 */
	public final static float SCALE = 2f;
	/**
	 * Minimum absolute value value which has to achieve an extreme point in order to be considered
	 * as a SIFT point.  
	 */
	public final static float MIN_EXTREME = 0.03f;
	
	/**
	 * Set of DoGs
	 * @uml.property  name="dogs"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.util.sift.ImageDoG"
	 */
	private ArrayList<ImageDoG> dogs;
	
	/**
	 * Set of smoothed images
	 * @uml.property  name="smooths"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPImgBitmap"
	 */
	private ArrayList<JIPImage> smooths;
	
	/**
	 * Width and height of the first level of the pyramid.
	 * @uml.property  name="initWidth"
	 */
	private int inicWidth;
	/**
	 * Width and height of the first level of the pyramid.
	 * @uml.property  name="initHeight"
	 */
	private int inicHeight;
	/**
	 * Number of levels of the pyramid.
	 * @uml.property  name="levels"
	 */
	private int levels;
	
	/**
	 * @param img image from which the pyramid is calculated
	 * @param numOctaves Number of octaves to generate
	 * @throws JIPException 
	 */
	public GaussianPyramid(JIPImgBitmap img, int numOctaves) throws JIPException {
		JIPImgBitmap imgGaussCurr, imgGaussPrev;
		SmoothGaussian gauss;
		InterpBi interp;
		ImageDoG dog;
		
		inicWidth = img.getWidth();
		inicHeight = img.getHeight();
		
		dogs = new ArrayList<ImageDoG>();
		smooths = new ArrayList<JIPImage>();
		gauss = new SmoothGaussian();
		gauss.setParamValue("sigma", SIGMA-1.0f);
		interp = new InterpBi();
		interp.setParamValue("step", 0.5f);
		img=(JIPBmpFloat)interp.processImg(img);
		interp.setParamValue("step", SCALE);
		gauss.setParamValue("sigma", SIGMA);
		imgGaussPrev = (JIPImgBitmap) gauss.processImg(img);
		// Generates the levels of the pyramid. Each level consists of 3 Gaussians and 2 DoGs
		levels = 0;
		for (int i=0; i<numOctaves; i++) {
			smooths.add(imgGaussPrev); //p0 is saved
			//Generate image p1 (sigma = 2^level*sqrt(2))
			imgGaussCurr = (JIPImgBitmap) gauss.processImg(imgGaussPrev);
			dog = ImageDoG.substract(imgGaussCurr, imgGaussPrev);
			dog.sigma = Math.pow(2.0, levels);
			dogs.add(dog);
			//Generate image p2 (sigma = 2^(level+1))
			imgGaussPrev = imgGaussCurr; 
			smooths.add(imgGaussPrev); //p1 is saved
			imgGaussCurr = (JIPImgBitmap) gauss.processImg(gauss.processImg(imgGaussCurr));
			dog = ImageDoG.substract(imgGaussCurr, imgGaussPrev);
			dog.sigma = Math.pow(2.0, levels)*Math.sqrt(2.0);
			dogs.add(dog);
			//Re-sampling
			imgGaussPrev = (JIPImgBitmap) interp.processImg(imgGaussCurr);
			levels++;
		}
	}
	
	/**
	 * Get SIFT points detected with the pyramid.
	 * @return
	 */
	public ArrayList<SiftPoint> getPointsSIFT(float endThreshold) {
		ArrayList<SiftPoint> points;
		ImageDoG dogAct, dogSup, dogInf;
		boolean isEnd;
		double upperScale, lowerScale;
		double pix;
		int numDoGs;

		points = new ArrayList<SiftPoint>();
		//if there are (JIPImgBitmap) interp.processImg(imgGaussAct)1 a N DoGs,
		//it has to see from 2 to la N-1 to find ends
		numDoGs = dogs.size();
		dogAct = (ImageDoG) dogs.get(0);
		dogInf = (ImageDoG) dogs.get(1);
		for(int i=2; i<=numDoGs-1; i++) {
			dogSup = dogAct;
			dogAct = dogInf;
			dogInf = (ImageDoG) dogs.get(i);
			//if the upper/lower scale has the same size than the current,
			//one of upper/lower point will have the same coordiantes. In otherwise, 
			//it depends on the difference scale. Calculate this scale.
			if (dogSup.width > dogAct.width) 
				upperScale = SCALE;
			else
				upperScale = 1.0;
			if (dogInf.width < dogAct.width) 
				lowerScale = 1.0/SCALE;
			else
				lowerScale = 1.0;
			//Find in all pixels the local ends.
			for(int x=2; x<dogAct.width-2; x++)
				for(int y=2; y<dogAct.height-2; y++) {
					isEnd = false;
					pix = dogAct.pixels[x][y];
					//is a maximum local?
					if (isMaximum(pix, x, y, dogAct.pixels) && 
					    isMaximum(pix, (int)(x*upperScale), (int)(y*upperScale), dogSup.pixels) &&
						isMaximum(pix, (int)(x*lowerScale), (int)(y*lowerScale), dogInf.pixels))
								isEnd = true;
					//is a minimum local?
					if (!isEnd)
						if (isMinimum(pix, x, y, dogAct.pixels) && 
							isMinimum(pix, (int)(x*upperScale), (int)(y*upperScale), dogSup.pixels) &&
							isMinimum(pix, (int)(x*lowerScale), (int)(y*lowerScale), dogInf.pixels))
								isEnd = true;						
					//save it if is an end and its absolute value is greater than the threshold
					if ((isEnd)&& Math.abs(pix)>endThreshold) {
						float level = (float)inicWidth/(float)dogAct.width;
						points.add(new SiftPoint(x*level, y*level, x, y, i-1, dogAct.sigma, level));
					}
				}
		}
			
		return points;	
	}
	
	private boolean isMaximum(double pix, int x, int y, double[][] level) {
		if ((pix>level[x-1][y-1])&&(pix>level[x][y-1])&&(pix>level[x+1][y-1])
				&&(pix>level[x-1][y])&&(pix>=level[x][y])&&(pix>level[x+1][y])
				&&(pix>level[x-1][y+1])&&(pix>level[x][y+1])&&(pix>level[x+1][y+1]))	
				return true;
			else
				return false;
	}

	private boolean isMinimum(double pix, int x, int y, double[][] level) {
		if ((pix<level[x-1][y-1])&&(pix<level[x][y-1])&&(pix<level[x+1][y-1])
				&&(pix<level[x-1][y])&&(pix<=level[x][y])&&(pix<level[x+1][y])
				&&(pix<level[x-1][y+1])&&(pix<level[x][y+1])&&(pix<level[x+1][y+1]))	
				return true;
			else
				return false;		
	}

	/**
	 * Returns a JIPImage with the DoGs of the pyramid. Each band of the
	 * image is a DoG, so there will be 2 x levels number of bands.
	 * @param scalar determines whether an image should be scaled so that
	 * all the levels of the pyramid have the same size, or on the contrary 
	 * they should maintain their actual size. In this latter case the rest of
	 * the image is filled in with zeros (as in JavaVis all the bands have to
	 * be of the same size).
	 * @throws JIPException 
	 */
	public JIPImage getImagenDoGs(boolean scalar) throws JIPException {

		JIPBmpFloat res;
		ImageDoG dog;
		double max, min, maxImag, minImag;
		InterpBi interp = null;
		JIPBmpFloat scaledImg = null;
		
		if (scalar) {
			interp = new InterpBi();
		}
		
		res = new JIPBmpFloat(2*levels, inicWidth, inicHeight);

		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		Iterator<ImageDoG> it = dogs.iterator();
		while(it.hasNext()) {
			dog = (ImageDoG) it.next();
			//find the minimum global
			minImag = dog.getMin();
			if (minImag<min)
				min = minImag;
			//find the maximum global
			maxImag = dog.getMax();
			if (maxImag>max)
				max = maxImag;				
		}
		//compose the bands of the image
		it = dogs.iterator();
		for(int banda=0; banda<2*levels; banda++) {
			dog = (ImageDoG) it.next();
			//Scale the images
			if (scalar) {
				interp.setParamValue("step", (float)dog.width/(float)inicWidth);
				scaledImg = (JIPBmpFloat) interp.processImg(dog.getJIPImage()); 
				for(int x=0; x<inicWidth; x++)
					for(int y=0; y<inicHeight; y++) 
						res.setPixel(banda, x, y, scaledImg.getPixel(x, y));							
			}
			else
				for(int x=0; x<dog.width; x++)
					for(int y=0; y<dog.height; y++) 
						res.setPixel(banda, x, y, (float)dog.pixels[x][y]);
		}
		return res;
	}
	
	public ImageDoG getDoG(int n) {
		return (ImageDoG) dogs.get(n);
	}
	
	public JIPImage getImgSuav(int n) {
		return (JIPImage) smooths.get(n);
	}
	
	/**
	 * Get the standard deviation used for generating the smoothed image.
	 * @param numImag order of the image in the pyramid (starting from 0).
	 * @return
	 */
	public float getSigma(int numImag) {
		int level, pos;
		
		level = numImag / 2;
		pos = numImag % 2;
		if (pos==0)
			return (float) Math.pow(2, level);
		else
			return (float) (Math.pow(2,level)*Math.sqrt(2.0));
	}
	
	/**
	 * Returns the "nominal" standard deviation used for generating 
	 * a smoothed image. The actual sigma is much larger due to the
	 * scales of the images. 
	 * @param numImag
	 * @return
	 */
	public float getSigmaNominal(int numImag) {
		int pos;
		
		pos = numImag % 2;
		if (pos==0)
			return 1.0f;
		else
			return (float)Math.sqrt(2.0);
	}
	
	public int getInitWidth() {
		return inicWidth;
	}

}