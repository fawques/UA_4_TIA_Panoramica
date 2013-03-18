package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies a convolution with a 5x5 mask defined by the user. To do that, it uses the 
 * ConvolveImage method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>matrix</em>: Mask to be defined (default zero values).</li>
 * <li><em>mult</em>: Real value indicating the multiply value (default 1.0).</li>
 * <li><em>div</em>: Real value indicating the divide value (default 1.0).</li>
 * <li><em>method</em>: List of methods that indicates how to manager the borders (default ZERO).
 * <ul>
 * <li><em>ZERO</em>: New rows and columns are added (depending of the radius) and they take a 0 value.</li>
 * <li><em>PAD</em>: The same, but the new rows and columns take the value of the closest pixel in the image.</li> 
 * <li><em>WRAP</em>: The following row to the last is the first, the previous to the first is the last, and the same to columns.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same type that the input image.<br />
 * Warning: If the result of the division is negative or zero and the input image is not 
 * a FLOAT type, you will see the result as a black image.</li>
 * </ul><br />
 */
public class Convol5x5 extends Function2D {
	private static final long serialVersionUID = -85842734760757503L;

	private static final int TOTAL_INPUT = 27;
	private static final int MATRIX_SIZE = 25;
	
	public Convol5x5() {
		super();
		name = "Convol5x5";
		description = "Applies a convolution with a 5 x 5 mask. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Convolution;

		//[0,24] -> matrix 5x5; 25 -> mult; 26 -> div
		ParamFloat p;
		for (int i=0; i < TOTAL_INPUT; i++) {
			p = new ParamFloat("p" + i, false, true);
			if (i != 12 && i != 25 && i != 26)
				p.setDefault(0.0f);
			else
				p.setDefault(1.0f);
			p.setDescription("");
			addParam(p);
		}
		
		ParamList method = new ParamList("method", false, true);
		String []paux = new String[3];
		paux[0] = "ZERO";
		paux[1] = "WRAP";
		paux[2] = "PAD";
		method.setDefault(paux);
		method.setDescription("Method to work with limits");
		
		addParam(method);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric || img instanceof JIPBmpBit) 
			throw new JIPException("Function Convol5x5 can not be applied to this image format.");

		double[] mat = new double[MATRIX_SIZE];
		for (int i=0; i < MATRIX_SIZE; i++)
			mat[i] = getParamValueFloat("p"+i);
		
		float mult = getParamValueFloat("p25");
		float div = getParamValueFloat("p26");
		String method = getParamValueString("method");

		Function2D convolution = new ConvolveImage();
		JIPImage convo = JIPImage.newImage(5, 5, ImageType.FLOAT);
		((JIPImgBitmap)convo).setAllPixels(mat);

		convolution.setParamValue("image", convo);
		convolution.setParamValue("div", div);
		convolution.setParamValue("mult", mult);
		convolution.setParamValue("method", method);

		return convolution.processImg(img);
	}
}

