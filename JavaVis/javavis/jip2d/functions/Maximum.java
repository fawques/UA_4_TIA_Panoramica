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
 * It changes the intensity value for the maximum value in a neighborhood window with a 
 * specified size.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>radius</em>: Integer value which indicates the radius of the neighborhood windows (default 2).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same type that the input image.</li>
 * </ul><br />
 */
public class Maximum extends Function2D {
	private static final long serialVersionUID = -4448500937301416124L;

	public Maximum() {
		super();
		name = "Maximum";
		description = "Changes the intensity value for maximum value in a neighbourhood window with a specified size. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("radius", false, true);
		p1.setDefault(2);
		p1.setDescription("Radius");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Maximum can not be applied to this image format.");
		
		int radius = getParamValueInt("radius");
		ImageType type = img.getType();
		int width = img.getWidth();
		int height = img.getHeight();
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();

		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, type);
		for (int nb=0; nb < numBands; nb++) {
			double[] bmp = imgBmp.getAllPixels(nb);
			double[] bin = new double[width * height];
			for (int h=0; h < height; h++)
				for (int w=0; w < width; w++) {
					int initX = w - radius, endX = w + radius;
					int initY = h - radius, endY = h + radius;
					if (initX < 0)
						initX = 0;
					if (endX >= width)
						endX = width - 1;
					if (initY < 0)
						initY = 0;
					if (endY >= height)
						endY = height - 1;
					double aux = 0;
					for (int y=initY; y <= endY; y++)
						for (int x=initX; x <= endX; x++)
							if (aux < bmp[x + y * width])
								aux = bmp[x + y * width];
					bin[w + h * width] = aux;
				}
			res.setAllPixels(nb, bin);
		}
		return res;
	}
}

