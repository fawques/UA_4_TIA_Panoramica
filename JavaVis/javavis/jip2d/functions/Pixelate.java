package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It pixelates an image. Every pixel inside this window takes the average value of the 
 * original pixel, giving a pixelate effect.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>side</em>: Integer value which indicates the length of smooth window (default 2).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A pixelated image, with the same type that the input image.</li>
 * </ul><br />
 */
public class Pixelate extends Function2D {
	private static final long serialVersionUID = 3639217021560311842L;

	public Pixelate() {
		super();
		name = "Pixelate";
		description = "Pixelates an image. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("side", false, true);
		p1.setDefault(2);
		p1.setDescription("Length of the side");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Pixelate can not be applied to this image format.");
		
		int side = getParamValueInt("side");
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int width = img.getWidth();
		int height = img.getHeight();
		int numBands = imgBmp.getNumBands();
		double[] bmp, bin;
		int initX, initY, endX, endY, count;
		double aux;
		
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, img.getType());
		for (int nb=0; nb < numBands; nb++) {
			bmp = imgBmp.getAllPixels(nb);
			bin = new double[width * height];
			for (int h=0; h < height; h+=side)
				for (int w=0; w < width; w+=side) {
					initX = w - side; endX = w + side;
					initY = h - side; endY = h + side;
					if (initX < 0) initX = 0;
					if (endX >= width) endX = width - 1;
					if (initY < 0) initY = 0;
					if (endY >= height) endY = height - 1;
					aux = 0.0f;
					count = 0;
					for (int y=initY; y <= endY; y++)
						for (int x=initX; x <= endX; count++, x++)
							aux += bmp[x + y * width];
					aux /= count;
					for (int y=initY; y <= endY; y++)
						for (int x = initX; x <= endX; x++)
							bin[x + y * width] = aux;
				}
			res.setAllPixels(nb, bin);
		}
		
		return res;
	}
}

