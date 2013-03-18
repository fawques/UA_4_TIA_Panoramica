package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It equalizes an image.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same input image type.</li>
 * </ul><br />
 */
public class Equalize extends Function2D {
	private static final long serialVersionUID = 5081225558906799271L;

	public Equalize() {
		super();
		name = "Equalize";
		description = "Equalizes an image. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Adjustment;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType type = img.getType();
		if (img instanceof JIPImgGeometric || type == ImageType.BIT) 
			throw new JIPException("Function Equalize can not be applied to this image type.");

		int width = img.getWidth();
		int height = img.getHeight();
		int totalPix = width*height;
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
		double[] bmp, bin = new double[totalPix];
		double max = 0;
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, type);
		
		switch (type) {
			case BYTE: 
			case COLOR: max = 256; break;
			case FLOAT:
			case SHORT: max = 65536; break;
		}

		for (int nb=0; nb < numBands; nb++) {
			double mini = Double.MAX_VALUE, vmini;
			int histo[] = new int[(int)max];
			bmp = imgBmp.getAllPixels(nb);

			for (int k=0; k < totalPix; k++) {
				if (bmp[k] < mini)
					mini = bmp[k];
			}
			
			for (int k=0; k < totalPix; k++) 
				histo[(int)(bmp[k]*(type==ImageType.FLOAT?255:1))]++;
				      
		    for (int i=0; i < max-1; i++) 
				histo[i+1] += histo[i];
		    
		    vmini = histo[(int)mini];

			for (int k=0; k < totalPix; k++) {
				bin[k] = (type==ImageType.FLOAT?1:max)*(histo[(int)(bmp[k]*(type==ImageType.FLOAT?255:1))] - vmini) / (totalPix - vmini);
			}

			res.setAllPixels(nb, bin);
		}
		
		return res;
	}
}

