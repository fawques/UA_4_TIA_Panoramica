package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It calculates the negative of an image.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The negative image from the input image.</li>
 * </ul><br />
 */
public class Negate extends Function2D {
	private static final long serialVersionUID = -5089690362652154611L;

	public Negate() {
		super();
		name = "Negate";
		description = "Calculates the negative of an image. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric)
			throw new JIPException("Function Negate can not be applied to this image format.");
		
		int width = img.getWidth();
		int height = img.getHeight();
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
		ImageType type = img.getType();
		int numPixels = width*height;
		double maximum = 1;

		switch (type) {
			case BIT :
			case FLOAT:
				maximum = 1;
				break;
			case BYTE :
			case COLOR :
				maximum = 255;
				break;
			case SHORT :
				maximum = 65535;
				break;
			default :
		}
		
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, type);
		double[] bmp;
		
		for (int nb=0; nb < numBands; nb++) {
			bmp = imgBmp.getAllPixels(nb);
			for (int i=0; i < numPixels; i++)
				bmp[i] = maximum - bmp[i];
			res.setAllPixels(nb, bmp);
		}
		
		return res;
	}
}

