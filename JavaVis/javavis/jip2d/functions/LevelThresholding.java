package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It finds the optimal threshold to binarize an image.<br />
 * <em>Ref: Nobuyuki Otsu (1979). "A threshold selection method from gray-level 
 * histograms". IEEE Trans. on SMC. Vol. 9 Num. 1 1979</em><br />
 * It applies to BYTE or SHORT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE or SHORT type.</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A binary image.</li>
 * </ul><br />
 */
public class LevelThresholding extends Function2D {
	private static final long serialVersionUID = -8333742650073304009L;

	public LevelThresholding() {
		super();
		name = "LevelThresholding";
		description = "Finds the optimal threshold to binarize an image. Applies to BYTE or SHORT type.";
		groupFunc = FunctionGroup.Segmentation;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BYTE && img.getType() != ImageType.SHORT) 
			throw new JIPException("Function LevelThresholding can not be applied to this image format.");
		
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
		int totalPix = imgBmp.getWidth()*imgBmp.getHeight();
		JIPBmpBit res = (JIPBmpBit)JIPImage.newImage(img.getWidth(), img.getHeight(), ImageType.BIT);
		double[] histo, mean, w, sigma;
		boolean[] bin;
		int max, threshold;
		double acum = 1.0/totalPix; // Used to normalize
		double meanTotal, maxValue; 
		
		if (img.getType() == ImageType.SHORT)  
			max = 65536;
		else
			max = 256;
		
		for (int nb=0; nb < numBands; nb++) {
			double[] bmp = imgBmp.getAllPixels(nb);
			histo = new double[max];
			w = new double[max];
			sigma = new double[max];
			mean = new double[max];
			bin = new boolean[totalPix];
			// First, calculate the histogram, normalizing so that the sum of all values is 1.0
			for (int k=0; k < totalPix; k++) {
				histo [(int)bmp[k]] += acum;
			}
			// Acumulate the histogram and calculates the mean
			meanTotal = 0.0;
			for (int k=0; k<max; k++) {
				for (int i=0; i <= k; i++) {
					w[k] += histo[i];
					mean[k] += i*histo[i];
				}
				meanTotal += k*histo[k];
			}
			// Calculates sigma
			for (int k=0; k < max; k++) {
				sigma[k] = Math.pow(meanTotal*w[k]-mean[k], 2.0)/(w[k]*(1-w[k]));
			}
			// Find the highest value
			threshold = 0;
			maxValue = 0.0;
			for (int k=0; k < max; k++) {
				if (maxValue<sigma[k]) {
					threshold = k;
					maxValue = sigma[k];
				}
			}
			// Binarizes the image 
			for (int k=0; k < totalPix; k++) {
				bin[k] = (bmp[k]>threshold);
			}
		
			res.setAllPixelsBool(nb, bin);
		}
		return res;
	}
}

