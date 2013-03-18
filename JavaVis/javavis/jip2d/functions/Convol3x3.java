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
 * It applies a convolution with a 3x3 mask defined by the user. To do that, it uses the 
 * ConvolveImage method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>matrix</em>: Mask to be defined (default zero values).</li>
 * <li><em>mult</em>: Real value which indicates the multiply value (default 1.0).</li>
 * <li><em>div</em>: Real value which indicates the divide value (default 1.0).</li>
 * <li><em>method</em>: List of methods that indicates how to manager the borders (default ZERO).
 * <ul>
 * <li><em>ZERO</em>: New rows and columns are added (depending of the radius) and they take 
 * a 0 value.</li>
 * <li><em>PAD</em>: The same, but the new rows and columns take the value of the closest 
 * pixel in the image.</li> 
 * <li><em>WRAP</em>: The following row to the last is the first, the previous to the first 
 * is the last, and the same to columns.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same input image type.<br />
 * Warning: If the result of the division is negative or zero and the input image is not 
 * a FLOAT type, you will see the result as a black image.</li>
 * </ul><br />
 */
public class Convol3x3 extends Function2D {
	private static final long serialVersionUID = -2215425311082781579L;

	public Convol3x3() { 
		super();
		name = "Convol3x3";
		description = "Applies a convolution with a 3 x 3 mask. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Convolution;

		ParamFloat p1 = new ParamFloat("a1", false, true);
		p1.setDefault(0.0f);
		p1.setDescription("");
		
		ParamFloat p2 = new ParamFloat("a2", false, true);
		p2.setDefault(0.0f);
		p2.setDescription("");
		
		ParamFloat p3 = new ParamFloat("a3", false, true);
		p3.setDefault(0.0f);
		p3.setDescription("");
		
		ParamFloat p4 = new ParamFloat("b1", false, true);
		p4.setDefault(0.0f);
		p4.setDescription("");
		
		ParamFloat p5 = new ParamFloat("b2", false, true);
		p5.setDefault((float) 1);
		p5.setDescription("");
		
		ParamFloat p6 = new ParamFloat("b3", false, true);
		p6.setDefault(0.0f);
		p6.setDescription("");
		
		ParamFloat p7 = new ParamFloat("c1", false, true);
		p7.setDefault(0.0f);
		p7.setDescription("");
		
		ParamFloat p8 = new ParamFloat("c2", false, true);
		p8.setDefault(0.0f);
		p8.setDescription("");
		
		ParamFloat p9 = new ParamFloat("c3", false, true);
		p9.setDefault(0.0f);
		p9.setDescription("");
		
		ParamFloat p10 = new ParamFloat("mult", false, true);
		p10.setDefault(1.0f);
		p10.setDescription("Multiplier");
		
		ParamFloat p11 = new ParamFloat("div", false, true);
		p11.setDefault(1.0f);
		p11.setDescription("Divisor");
		
		ParamList p12 = new ParamList("method", false, true);
		String []paux = new String[3];
		paux[0] = "ZERO";
		paux[1] = "WRAP";
		paux[2] = "PAD";
		p12.setDefault(paux);
		p12.setDescription("How to process the border");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
		addParam(p8);
		addParam(p9);
		addParam(p10);
		addParam(p11);
		addParam(p12);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric || img instanceof JIPBmpBit) 
			throw new JIPException("Function Convol3x3 can not be applied to this image format.");

		double mat[] = new double[9];
		mat[0] = getParamValueFloat("a1");
		mat[1] = getParamValueFloat("a2");
		mat[2] = getParamValueFloat("a3");
		mat[3] = getParamValueFloat("b1");
		mat[4] = getParamValueFloat("b2");
		mat[5] = getParamValueFloat("b3");
		mat[6] = getParamValueFloat("c1");
		mat[7] = getParamValueFloat("c2");
		mat[8] = getParamValueFloat("c3");
		float mult = getParamValueFloat("mult");
		float div = getParamValueFloat("div");
		String method = getParamValueString("method");

		Function2D convolution = new ConvolveImage();
		JIPImage convo = JIPImage.newImage(3, 3, ImageType.FLOAT);
		((JIPImgBitmap)convo).setAllPixels(mat);

		convolution.setParamValue("image", convo);
		convolution.setParamValue("div", div);
		convolution.setParamValue("mult", mult);
		convolution.setParamValue("method", method);

		return convolution.processImg(img);
	}
}

