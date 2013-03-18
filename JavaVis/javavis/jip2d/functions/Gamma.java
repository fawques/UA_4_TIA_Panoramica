package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;

/**
 * It adjusts the intensity of the RGB bands in a COLOR image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR type.</li>
 * <li><em>r</em>: Integer value which indicates the percentage of variation in the red band. 
 * A value of 100 does not alter the image (default 100).</li>
 * <li><em>g</em>: Integer value which indicates the percentage of variation in the green band. 
 * A value of 100 does not alter the image (default 100).</li>
 * <li><em>b</em>: Integer value which indicates the percentage of variation in the blue band. 
 * A value of 100 does not alter the image (default 100).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A processed image, with the same input image type and new intensity values for the RGB 
 * bands.</li>
 * </ul><br />
 */
public class Gamma extends Function2D {
	private static final long serialVersionUID = 7924490703944874892L;

	public Gamma() {
		super();
		name = "Gamma";
		description = "Adjusts the intensity of the RGB bands of a COLOR image.";
		groupFunc = FunctionGroup.Adjustment;

		ParamInt p1 = new ParamInt("r", false, true);
		p1.setDefault(100);
		p1.setDescription("Red percentage");
		
		ParamInt p2 = new ParamInt("g", false, true);
		p2.setDefault(100);
		p2.setDescription("Green percentage");
		
		ParamInt p3 = new ParamInt("b", false, true);
		p3.setDefault(100);
		p3.setDescription("Blue percentage");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.COLOR) 
			throw new JIPException("Function Gamma can not be applied to this image type, must only be COLOR type.");

		JIPBmpColor res = null;
		int[] color = new int[3];
		color[0] = getParamValueInt("r");
		color[1] = getParamValueInt("g");
		color[2] = getParamValueInt("b");
		
		if (color[0] < 0 || color[1] < 0 || color[2] < 0)
			throw new JIPException ("Percentage has to be greater or equal than 0.");
			
		int width = img.getWidth();
		int height = img.getHeight();
		int totalPix = width*height;
		int rgbSize = 3;

		res = new JIPBmpColor(width,height);
		for (int nb=0; nb < rgbSize; nb++) {
			double[] bmp = ((JIPBmpColor)img).getAllPixels(nb);
			double[] bin = new double[totalPix];
			for (int i=0; i < totalPix; i++)
				bin[i] = Math.min((int) (bmp[i] * color[nb] / 100.0f), 255);
			res.setAllPixels(nb, bin);
		}
		return res;
	}
}

