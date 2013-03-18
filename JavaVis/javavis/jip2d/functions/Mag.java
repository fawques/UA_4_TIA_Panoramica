package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It calculates the gradient magnitude using the square root of the sum of the square of
 * the pixels from an image with an even number of bands.<br />
 * It applies to BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE, SHORT or FLOAT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The original image with a half number of bands.</li>
 * </ul><br />
 */
public class Mag extends Function2D {
	private static final long serialVersionUID = 5577393934194722663L;

	public Mag() {
		super();
	    name = "Mag";
	    description = "Calculates the magnitude in a gradient image. Applies to BYTE, SHORT or FLOAT type.";
	    groupFunc = FunctionGroup.Edges;
    }

    public JIPImage processImg(JIPImage img) throws JIPException {
    	ImageType t = img.getType();
    	if (img instanceof JIPImgGeometric || t == ImageType.COLOR|| t == ImageType.BIT) 
    		throw new JIPException("Function Mag can not be applied to this image format.");

		int totalPix = img.getWidth()*img.getHeight();
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands/2, img.getWidth(), img.getHeight(), t);
		
		double [] bmp, bmp2;
		double [] bin = new double[totalPix];
		double pp, pp2;
		for (int nb=0; nb < numBands; nb=nb+2) { 
			bmp = imgBmp.getAllPixels(nb);
			bmp2 = imgBmp.getAllPixels(nb+1);
		  	for (int k=0; k < totalPix; k++) {
		  		pp = bmp[k];
	  			pp2 = bmp2[k];
	  			bin[k] = Math.sqrt(pp*pp + pp2*pp2);
		  	}  
		  	res.setAllPixels(nb/2, bin);
		}
		return res;
	}
}

