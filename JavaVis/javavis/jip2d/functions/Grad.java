package javavis.jip2d.functions;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It calculates the image gradient. To do that, for each pixel, it uses for X 
 * gradx=I(x+1,y)-I(x-1,y) and the same for Y axis.<br />
 * It applies to gray scale images (BYTE, BIT, SHORT or FLOAT).<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE, BIT, SHORT or FLOAT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A FLOAT image, which has double bands than the original image. The added bands have
 * the X gradient and the even bands the Y gradient.</li>
 * </ul><br />
 */
public class Grad extends Function2D {
	private static final long serialVersionUID = -1264860928372025034L;

	public Grad() {
		super();
		name = "Grad";
		description = "Calculates image gradient. Applies to gray image.";
		groupFunc = FunctionGroup.Edges;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.BIT || img.getType() == ImageType.BYTE 
				|| img.getType() == ImageType.SHORT || img.getType() == ImageType.FLOAT) 
		{
			int width = img.getWidth();
			int height = img.getHeight();
			int numBands = ((JIPImgBitmap)img).getNumBands();
			JIPBmpFloat res = new JIPBmpFloat(2 * numBands, width, height);
			double[] binflo = new double[width * height];
			double[] binflo2 = new double[width * height];
			double[] bmpflo;
			JIPImgBitmap img2;
			
			if (img.getType() != ImageType.FLOAT) {
				GrayToGray fgtg = new GrayToGray();
				fgtg.setParamValue("gray","FLOAT");
				img2 = (JIPImgBitmap)fgtg.processImg(img);
			}
			else img2 = (JIPBmpFloat)img;

			for (int nb=0; nb < numBands; nb++) {
				bmpflo = img2.getAllPixels(nb);
				for (int x=1; x < width-1; x++)  
					for (int y=1; y < height-1; y++) { 
						binflo[x + y * width] = bmpflo[x + 1 + y * width] - bmpflo[x - 1 + y * width];
						binflo2[x + y * width] = bmpflo[x + (y - 1) * width] - bmpflo[x
								+ (y + 1) * width];
					}
				res.setAllPixels(2*nb, binflo);
				res.setAllPixels(2*nb + 1, binflo2);
			}
			return res;
		}
		else 
			throw new JIPException("Function Grad can not be applied to this image type.");
	}
}

