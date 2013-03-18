package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies a gaussian smoothing to the image. To do that, this function uses the 
 * ConvolveImage method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>sigma</em>: Integer value which indicates the level of gaussian smooth (default 2).</li>
 * <li><em>axis</em>: Filter orientation (horizontal, vertical or both) (default both).</li>
 * <li><em>method</em>: List of methods that treats the borders (default ZERO).
 * <ul>
 * <li><em>ZERO</em>: Border pixels are marked as 0.</li>
 * <li><em>PAD</em>: The first row is duplicated so that the -1 row is the same. The same for 
 * last row and first and last columns.</li> 
 * <li><em>WRAP</em>: The -1 row is the last row and the n+1 row is the first. The same for 
 * columns.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same input image type.</li>
 * </ul><br />
 */
public class SmoothGaussian extends Function2D {
	private static final long serialVersionUID = -1139660566687024448L;

	public SmoothGaussian() {
		super();
		name = "SmoothGaussian";
		description = "Applies a gaussian smoothing to the image. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Adjustment;

		ParamFloat p1 = new ParamFloat("sigma", false, true);
		p1.setDefault(2.0f);
		p1.setDescription("Level of gaussian smooth.");
		
		ParamList p2 = new ParamList("axis", false, true);
		String []paux2 = new String[3];
		paux2[0] = "Both";
		paux2[1] = "Horizontal";
		paux2[2] = "Vertical";
		p2.setDefault(paux2);
		p2.setDescription("Filter orientation");
		
		ParamList p3 = new ParamList("method", false, true);
		String []paux = new String[3];
		paux[0] = "ZERO";
		paux[1] = "WRAP";
		paux[2] = "PAD";
		p3.setDefault(paux);
		p3.setDescription("How to process the border");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric || img.getType() == ImageType.BIT) 
			throw new JIPException("Function SmoothGaussian can not be applied to this image format.");

		float sigma = getParamValueFloat("sigma");
		String axis = getParamValueString("axis");
		String method = getParamValueString("method");
		
		if (sigma < 0.0)
			throw new JIPException ("Sigma has to be greater or equal than 0.");
		
		if (sigma == 0.0)
			return img;

		int radius = (int) (sigma * 3.0 + 0.5);
		int diameter = radius * 2 + 1;
		double[] vector = new double[diameter];

		double mult = 1.0 / (sigma * Math.sqrt(2.0 * Math.PI));
		double factor = -1.0 / (2.0 * sigma * sigma);

		for (int i = 0, r = -radius; r <= radius; r++, i++) {
			vector[i] = mult * Math.exp(factor * r * r);
		}

		Function2D convolution = new ConvolveImage();

		JIPImage convo = JIPImage.newImage(diameter, 1, ImageType.FLOAT);
		((JIPImgBitmap)convo).setAllPixels(vector);
		JIPImage convo2 = JIPImage.newImage(1, diameter, ImageType.FLOAT);
		((JIPImgBitmap)convo2).setAllPixels(vector);
		convolution.setParamValue("method", method);

		if (axis.equals("Horizontal")) {
			convolution.setParamValue("image", convo);
			return convolution.processImg(img);
		}
		if (axis.equals("Vertical")) {
			convolution.setParamValue("image", convo2);
			return convolution.processImg(img);
		}
		if (axis.equals("Both")) {
			convolution.setParamValue("image", convo);
			JIPImage aux = convolution.processImg(img);
			convolution.setParamValue("image", convo2);
			return convolution.processImg(aux);
		}
		throw new JIPException("Invalid axis");
	}
}

