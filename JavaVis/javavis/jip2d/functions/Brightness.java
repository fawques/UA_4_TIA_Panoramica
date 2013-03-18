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
 * It adjusts the image brightness. If a value is greater/lower than the maximum/minimum 
 * value of the type (i.e., 255/0) it keeps the maximum/minimum value.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>perc</em>: Integer value which indicates the percentage of brightness (default 100).
 * <ul>
 * <li><em>perc</em> = 100: It does not alter the image.</li>
 * <li><em>perc</em> &lt; 100: It decreases the brightness.</li>
 * <li><em>perc</em> &gt; 100: It increases the brightness.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same input image with different brightness.</li>
 * </ul><br />
 */
public class Brightness extends Function2D {
	private static final long serialVersionUID = 2124203301892703158L;

	public Brightness() {
		super();
		name = "Brightness";
		description = "Adjusts the image brightness. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Adjustment;

		ParamInt p1 = new ParamInt("perc", false, true);
		p1.setDefault(100);
		p1.setDescription("Percentage (when 100% the image is not modified)");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPImgBitmap res = null;
		float perc = (float) getParamValueInt("perc") / 100.0f;
		ImageType t = img.getType();
		
		if (t == ImageType.BIT || img instanceof JIPImgGeometric) 
			throw new JIPException ("Function Brightness can not be applied to this image format.");
		
		if (perc < 0.0f)
			throw new JIPException ("Percentage has to be greater or equal than 0.");
		
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int totalPix = img.getWidth()*img.getHeight();
		int numBands = imgBmp.getNumBands();
		double max;
		
		switch (t) {
			case BYTE :
			case COLOR :
				max = 255;
				break;
			case SHORT :
				max = 65535;
				break;
			case FLOAT :
				max = 1;
				break;
			default :
				return img;
		}

		res = (JIPImgBitmap)imgBmp.clone();
		
		for (int nb=0; nb < numBands; nb++) {
			double[] bmp = imgBmp.getAllPixels(nb);
			double[] bin = new double[totalPix];
			
			for (int k=0; k < totalPix; k++)
				bin[k] = Math.min(bmp[k] * perc, max);
			res.setAllPixels(nb, bin);
		}
		
		return res;
	}
}

