package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip2d.base.*;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPBmpShort;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It re-renders an image using a bilinear interpolation.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>step</em>: Real value which indicates the resizing ratio (default 1.5).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same type that the input image.</li>
 * </ul><br />
 */
public class InterpBi extends Function2D {
	private static final long serialVersionUID = -1232282732231182119L;

	public InterpBi() {
		super();
		name = "InterpBi";
		description = "Rerenders an image using a bilinear interpolation. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;
		
		ParamFloat p1 = new ParamFloat("step", false, true);
		p1.setDescription("The resizing ratio.");
		p1.setDefault(1.5f);
		
		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int width, height, numBands;
		JIPImgBitmap res = null, imgBmp = (JIPImgBitmap)img;
		int h1, w1;
		double x, y, step, pix;
		
		step = getParamValueFloat("step");
		width = (int)Math.floor(img.getWidth()/step);
		height = (int)Math.floor(img.getHeight()/step);
		numBands = imgBmp.getNumBands();
		
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function InterBi can not be applied to Geometric image types.");
		
		switch (img.getType()) {
			case BYTE :
				res = new JIPBmpByte(numBands, width,height);
				break;
			case SHORT :
				res = new JIPBmpShort(numBands, width,height);
				break;
			case FLOAT :
				res = new JIPBmpFloat(numBands, width,height);
				break;
			case BIT :
				res = new JIPBmpBit(numBands, width,height);
				break;
			case COLOR :
				res = new JIPBmpColor(width,height);
				break;
		}
		
		for (int b=0; b < numBands; b++) {
			for (int h=0; h < height; h++) {
				for (int w=0; w < width; w++) {
					h1 = (int)(h*step);
					w1 = (int)(w*step);
					x = (w*step) - w1;
					y = (h*step) - h1;
					if (w1 == img.getWidth()-1) w1--;  // Controls the limits
					if (h1 == img.getHeight()-1) h1--; // Controls the limits
					pix = calcInterp(imgBmp, b, h1, w1, x, y);
					res.setPixel(b, w, h, pix);				
				}
			}
		}
		
		return res;
	}
	
	
	/**
	 * Method which performs a bilinear interpolation.
	 * @param img The input image.
	 * @param b Number of bands.
	 * @param h1 Row in the upper left corner of the 4 pixels that are interpolated.
	 * @param w1 Column in the upper left corner of the 4 pixels that are interpolated.
	 * @param x Displacement of the point where we want the interpolation on c1.
	 * @param y Displacement of the point where we want the interpolation on f1.
	 * @return Interpolated pixel value.
	 * @throws JIPException 
	 */
	private double calcInterp(JIPImgBitmap img, int b, int h1, int w1, double x, double y) throws JIPException {
		double p00, p10, p01, p11;
		p00 = img.getPixel(b, w1, h1);
		p10 = img.getPixel(b, w1+1, h1);
		p01 = img.getPixel(b, w1, h1+1);
		p11 = img.getPixel(b, w1+1, h1+1);
		
		return (p10-p00)*x +(p01-p00)*y + (p11+p00-p01-p10)*x*y + p00;	
	}
}

