package javavis.jip2d.functions;

import java.util.ArrayList;
import java.lang.Math;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.*;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It estimates the saliency of each pixel of the image at an only scale using Kadir and 
 * Brady method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>scale</em>: Integer value which indicates the scale. It has to be greater than 4 (default 5).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image representing the saliency map.</li>
 * </ul><br />
 */
public class Saliency extends Function2D {
	
	private static final long serialVersionUID = -5543080812213811142L;
	
	public Saliency() {
		super();
		name = "Saliency";
		description = "Estimates a saliency map from the image. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.FeatureExtract;

		ParamInt p1 = new ParamInt("scale", true, true);
		p1.setDefault(5);
		p1.setDescription("Scale (>4)");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("This algorithm can not be applied to this image format");
		
		int scale = getParamValueInt("scale");
		
		if (scale < 5)
			throw new JIPException("Scale value must be greater than 4");
		
		JIPBmpByte imgByte = null;
		
		ImageType type = img.getType();
		int bins = 1000;
		int width = img.getWidth();
		int height = img.getHeight();
		
		if (type != ImageType.BYTE) {
			switch (type) {
				case BIT: throw new JIPException("This function requires a BYTE type image."); 
				case FLOAT:
				case SHORT: GrayToGray fgg = new GrayToGray();
							fgg.setParamValue("gray", "BYTE");
							imgByte = (JIPBmpByte)fgg.processImg(img);
							break;
				case COLOR: ColorToGray fcg = new ColorToGray();
							imgByte = (JIPBmpByte)fcg.processImg(img);
							break;
			}
		}
		else //If it's a BYTE type image, only copy the current img to imgByte.
		{
			imgByte = (JIPBmpByte)img.clone();
		}
			
		
		ArrayList<Coord> mask = createMask(scale);
		
		JIPBmpFloat result = new JIPBmpFloat(width,height);
		float []lookup = createLookUp(bins);
		
		int i;
		float maxEntropy = 0;
		for (int y=scale-1; y < height-scale+1; y++)
			for (int x=scale-1; x < width-scale+1; x++)
			{
				double []hist = new double[128];
				for (i=0; i < 128; i++)
					hist[i] = 0;
				for (i=0; i < mask.size(); i++)
					hist[(int)imgByte.getPixel(x+((Coord)mask.get(i)).x, y+((Coord)mask.get(i)).y)/2]++;
				float entropy = 0;
				for (i=0; i < 128; i++)
					if (hist[i] != 0)
					{
						hist[i] = hist[i]/(float)mask.size();
						entropy -= lookup[(int)Math.floor(hist[i]*bins)];
					}
				result.setPixelFloat(x, y, entropy);
				if (entropy > maxEntropy)
					maxEntropy = entropy;
			}
			
		for (int y=scale-1; y < height-scale+1; y++)
			for (int x=scale-1; x < width-scale+1; x++)
				result.setPixelFloat(x, y, result.getPixelFloat(x,y)/maxEntropy);
		
		return result;
	}
	
	
	/**
	 * Method which calculates the pixels to consider at maximum scale to calculate saliency.
	 * @param scale The scale.
	 * @return A list of coordinates.
	 */
	private ArrayList<Coord> createMask(int scale) {
		int maxDist = (scale-1)*(scale-1);
		ArrayList<Coord> mask = new ArrayList<Coord>();
		
		for (int y=-(scale-1); y <= scale-1; y++)
			for (double x=-(scale-1); x <= scale-1; x++)
			{
				double newX = 0;
				if (x != 0) newX = Math.abs(x) - 0.5;
				if ((y*y + newX*newX) <= maxDist)
				{
					Coord c = new Coord((int)x,y);
					mask.add(c);
				}
			}

		return mask;
	}
	
	/**
	 * Method which creates a look up table to avoid calculating logarithms during 
	 * scale saliency algorithm.
	 * @param bins The bins.
	 * @return A vector of real values.
	 */
	private float[] createLookUp(int bins) {
		float value;
		
		float []lookup = new float[bins+1];
		lookup[0] = 0;
		for (int i=1;i<=bins;i++)
		{
			value = i/(float)bins;
			lookup[i] = value*(float)Math.log(value);
		}
			
		return lookup;
	}
	
	
	/**
	 * Class coordinate
	 */
	private class Coord {
		public int x;
		public int y;
		
		public Coord(int xnew, int ynew)
		{
			x = xnew; y = ynew;
		}
		
		boolean equals(Coord c)
		{
			return (c.x == this.x && c.y == this.y);
		}
	}
}

