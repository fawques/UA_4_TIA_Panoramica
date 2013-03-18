package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It crops a rectangular area of the image. Crop receives the coordinates of the rectangle.
 * If the width or height are greater than the image size, a zero region is added.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>x</em>: Integer value which indicates the X coordinate of the upper left corner
 * in the clipping (default 0).</li>
 * <li><em>y</em>: Integer value which indicates the Y coordinate of the upper left corner
 * in the clipping (default 0).</li>
 * <li><em>width</em>: Integer value which indicates the width of the clipping rectangle 
 * (default 0).</li>
 * <li><em>height</em>: Integer value which indicates the height of the clipping rectangle 
 * (default 0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A frame of the image with the same type of the input image.</li>
 * </ul><br />
 */
public class Crop extends Function2D {
	private static final long serialVersionUID = 6824908906482994407L;

	public Crop() {
		super();
		name = "Crop";
		description = "Cuts out a rectangular area in an image. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("x", false, true);
		p1.setDefault(0);
		p1.setDescription("X coordinate of the upper left corner in the clipping");
		
		ParamInt p2 = new ParamInt("y", false, true);
		p2.setDefault(0);
		p2.setDescription("Y coordinate of the upper left corner in the clipping");
		
		ParamInt p3 = new ParamInt("w", false, true);
		p3.setDescription("Width of the rectangle");
		
		ParamInt p4 = new ParamInt("h", false, true);
		p4.setDescription("Height of the rectangle");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Crop can not be applied to this image type.");
		
		int xRec = getParamValueInt("x");
		int yRec = getParamValueInt("y");
		int widthRec = getParamValueInt("w");
		int heightRec = getParamValueInt("h");
		
		double fpix;
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int width = imgBmp.getWidth();
		int height = imgBmp.getHeight();
		int numBands = imgBmp.getNumBands();
		JIPImgBitmap res = null;
		
		if (xRec >= 0 && yRec >= 0 && xRec < width && yRec < height && widthRec > 0 && heightRec > 0) {
			res = (JIPImgBitmap)JIPImage.newImage(numBands, widthRec, heightRec, t);
			for (int nb=0; nb < numBands; nb++)
				for (int y=0; y < heightRec; y++)
					for (int x=0; x < widthRec; x++) {
						fpix = (xRec + x < width && yRec + y < height)
							 ? imgBmp.getPixel(nb, xRec + x, yRec + y) : 0;
						res.setPixel(nb, x, y, fpix);
					}
		}
		else 
			throw new JIPException("Dimensions exceed image size.");
			
		return res;
	}
}

