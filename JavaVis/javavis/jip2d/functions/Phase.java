package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It calculates the phase magnitude using the arc tangent of the pixels from an image with 
 * an even number of bands. To do that, the image should be a gradient, i.e. each image has
 * to be two bands or more than two.<br />
 * It applies to gray (BYTE, BIT, SHORT or FLOAT) type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE, BIT, SHORT or FLOAT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed REAL type image with a half number of bands.</li>
 * </ul><br />
 * Additional notes: The input image should have a pair number of bands.<br />
 */ 
public class Phase extends Function2D {
	private static final long serialVersionUID = 7341222887994655211L;

	public Phase() {
		super();
		name = "Phase";
		description = "Calculates the phase of a complex image. Applies to gray type.";
		groupFunc = FunctionGroup.Edges;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();
    	if (img instanceof JIPImgGeometric || t == ImageType.COLOR) 
    		throw new JIPException("Function Phase can not be applied to this image format.");
    	
    	JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
    	if (numBands < 2) 
        	throw new JIPException("Function Phase can not be applied to an image with less than 2 bands.");

    	double[] bmp, bmp2;
    	double pp, pp2;
    	int width = img.getWidth();
		int height = img.getHeight();
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands / 2, width, height, ImageType.FLOAT);

		double[] bin = new double[width*height];

		for (int nb=0; nb < numBands; nb=nb+2) { 
			bmp = imgBmp.getAllPixels(nb);
			bmp2 = imgBmp.getAllPixels(nb + 1);
			for (int i=0; i < width*height; i++) { 
				pp = bmp[i];
				pp2 = bmp2[i];
				if (pp == 0.0 && pp2 == 0.0)
					bin[i] = 0;
				else
					bin[i] = Math.atan2(pp, pp2);
			}
			res.setAllPixels(nb/2, bin);
		}
		return res;
	} 
}

