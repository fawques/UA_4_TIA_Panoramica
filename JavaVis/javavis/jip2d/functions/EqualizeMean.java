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
 * It moves the weighted mean of the histogram to a given level.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>level</em>: Integer value which indicates the level to move the mean (default 128).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same input image types.</li>
 * </ul><br />
 */
public class EqualizeMean extends Function2D {
	private static final long serialVersionUID = 7432834000464545790L;

	public EqualizeMean() {
		name = "EqualizeMean";
		description = "Moves the weighted mean of the histogram to a given level. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Adjustment;
		
		ParamInt p1 = new ParamInt("level", false, true);
		p1.setDescription("Level to move the mean");
		p1.setDefault(128);
		
		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType type = img.getType();
		
		if(img instanceof JIPImgGeometric || type == ImageType.BIT) {
			throw new JIPException("Function Equalize can not be applied to this image type.");
		}
		
		JIPImgBitmap bmp = (JIPImgBitmap)img;
		int level = getParamValueInt("level");
		if (level < 0)
			throw new JIPException("Level has to be greater or equal than 0.");
		
		double mean, rate;
		double []pixels;
		int max = 0;
		int numBands = bmp.getNumBands();
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int totalPix = width*height;
		JIPImgBitmap res = (JIPImgBitmap)JIPImgBitmap.newImage(numBands, width, height, type);
		
		switch (type) {
			case BYTE: 
			case COLOR: 
			case FLOAT: max = 256; break;
			case SHORT: max = 65536; break;
		}
		
		for (int nb=0; nb < numBands; nb++) {
			pixels = bmp.getAllPixels(nb);
			// First, calculate the histogram
			int histo[] = new int[max];
			for (int k=0; k < totalPix; k++) 
				histo[(int)(pixels[k]*(type==ImageType.FLOAT?255:1))]++;
			// Then, calculate weighted mean
			mean = 0.0;
			for (int i=0; i < max; i++) {
				mean += i*histo[i];
			}
			mean /= totalPix;
			rate = level / mean;
			for (int k=0; k < totalPix; k++) 
				pixels[k] *= rate;
			res.setAllPixels(nb,pixels);
		}
		
		return res;
	}
}

