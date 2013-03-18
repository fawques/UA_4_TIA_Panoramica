package javavis.jip2d.functions;

import java.util.Random;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It introduces random noise depending on the noise level indicated by the user.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>level</em>: Integer value which indicates the noise level. The value is a range 
 * between 0 and 100: if the value is 0, the image does not change (default 20).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The original image with random noise.</li>
 * </ul><br />
 */
public class AddNoise extends Function2D {
	private static final long serialVersionUID = -5543080812213828342L;

	public AddNoise() {
		super();
		name = "AddNoise";
		description = "Introduces random noise. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("level", false, true);
		p1.setDefault(20);
		p1.setDescription("Noise level [0..100]");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function AddNoise can not be applied to this image format.");
		
		float level = getParamValueInt("level")/100.0f;
		Random rnd = new Random();
		ImageType t = img.getType();
		int width = img.getWidth();
		int height = img.getHeight();
		int totalPix = width*height;
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
		int max = 0;
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, t);
		
		switch (t) {
			case BYTE :
			case COLOR :
				max = 255;
				break;
			case SHORT :
				max = 65535;
				break;
			case FLOAT :
			case BIT :
				max = 1;
				break;
		}
		
		if (t != ImageType.COLOR) {
			for (int nb=0; nb < numBands; nb++) {
				double[] bmp = imgBmp.getAllPixels(nb);
				for (int k=0; k < totalPix; k++)
					if (rnd.nextFloat() < level) {	
						bmp[k] += rnd.nextGaussian()*max;
						if (bmp[k] > max) bmp[k] = max;
						if (bmp[k] < 0) bmp[k] = 0.0;
					}
				res.setAllPixels(nb, bmp);
			}
		}
		else {
			double[] R, G, B;
			R = ((JIPBmpColor)imgBmp).getAllPixelsRed();
			G = ((JIPBmpColor)imgBmp).getAllPixelsGreen();
			B = ((JIPBmpColor)imgBmp).getAllPixelsBlue();
			for (int k=0; k < totalPix; k++)
				if (rnd.nextFloat() < level) {
					R[k] = Math.abs(rnd.nextGaussian())*max;
					if (R[k] > max) R[k] = max;
					G[k] = Math.abs(rnd.nextGaussian())*max;
					if (G[k] > max) G[k] = max;
					B[k] = Math.abs(rnd.nextGaussian())*max;
					if (B[k] > max) B[k] = max;
				}
			res.setAllPixels(0, R);
			res.setAllPixels(1, G);
			res.setAllPixels(2, B);
		}
		
		return res;
	}
}

