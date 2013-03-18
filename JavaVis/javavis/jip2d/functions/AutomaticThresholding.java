package javavis.jip2d.functions;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It implements the Otsu's method for automatic thresholding.<br />
 * <em>Ref: Nobuyuki Otsu (1979). "A threshold selection method from gray-level 
 * histograms". IEEE Trans. Sys., Man., Cyber. 9: 62Ð66.</em><br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed BIT type image.</li>
 * </ul><br />
 */
public class AutomaticThresholding  extends Function2D {
	private static final long serialVersionUID = -4646182436875827014L;
	
	private static final int VALUES_PER_BAND = 256;
	
	public AutomaticThresholding() {
		super();
		name = "AutomaticThresholding";
		description = "Applies an automatic thresholding. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Segmentation;
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();
		
		if (t != ImageType.BIT && !(img instanceof JIPImgGeometric)) {
			int width = img.getWidth();
			int height = img.getHeight();
			int totalPix = width*height;
			JIPImgBitmap imgBmp = (JIPImgBitmap)img;
			int numBands = imgBmp.getNumBands();
			double[] bmp = new double[totalPix];
			boolean[] bin;
			double max = 0;
			JIPBmpBit res = new JIPBmpBit(numBands, width, height);
			
			switch (t) {
				case BYTE: 
				case COLOR: 
				case FLOAT: max = 256; break;
				case SHORT: max = 65536; break;
			}
			
			for (int nb=0; nb < numBands; nb++) {
				int histo[] = new int[(int)max];
				bmp = imgBmp.getAllPixels(nb);
				
				for (int k=0; k < totalPix; k++) 
					histo[(int)(bmp[k]*(t==ImageType.FLOAT?255:1))]++;
			    
			    // Total number of pixels
				int total = img.getHeight()*img.getWidth();
				float sum = 0;
				
				for (int t2=0; t2 < max; t2++) 
					sum += t2 * histo[t2];

				float sumB = 0;
				int wB = 0;
				int wF = 0;
				float varMax = 0;
				float threshold = 0;

				for (int t2=0; t2 < VALUES_PER_BAND; t2++) {
				   wB += histo[t2];					// Weight Background
				   if (wB == 0) continue;

				   wF = total - wB;					// Weight Foreground
				   if (wF == 0) break;

				   sumB += (float) (t2 * histo[t2]);

				   float mB = sumB / wB;			// Mean Background
				   float mF = (sum - sumB) / wF;    // Mean Foreground

				   // Calculate Between Class Variance
				   float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

				   // Check if new maximum found
				   if (varBetween > varMax) {
				      varMax = varBetween;
				      threshold = t2;
				   }
				}
				bin = new boolean[totalPix];
				
				for (int k=0; k < totalPix; k++) 
					bin[k] = (bmp[k]>threshold);
				
				System.out.println("Threshold="+threshold);
				
				res.setAllPixelsBool(nb, bin);
			}
			
			return res;
		}
		else
			throw new JIPException("Function AutomaticThresholding is not defined for this image type.");
	}
}

