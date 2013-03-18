package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It sharpens an image using a convolution matrix (4-neighbors). To do that, it uses the
 * ConvolveImage method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>level</em>: Real value that specifies the mask to use. The 4-neighbors value is N=-level/10 and 
 * the center is (4*N)+1 (default 5.0).</li>
 * <li><em>strong</em>: Boolean value which indicates if the sharpen is strong or not (default not checked).</li>
 * <li><em>method</em>: List of methods that indicates how to manager the borders (default ZERO).
 * <ul>
 * <li><em>ZERO</em>: New rows and columns are added (depending of the radius) and they take a 0 value.</li>
 * <li><em>PAD</em>: The same, but the new rows and columns take the value of the closest pixel in the image.</li> 
 * <li><em>WRAP</em>: The following row to the last is the first, the previous to the first is the last, and the same to columns.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same input image type.</li>
 * </ul><br />
 */
public class Sharpen extends Function2D {
	private static final long serialVersionUID = 1147520040846581270L;

	public Sharpen() {
		super();
		name = "Sharpen";
		description = "Sharpens an image using a convolution matrix (4-neighbors). Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Convolution;

		ParamInt p1 = new ParamInt("level", false, true);
		p1.setDescription("Level of brightness (1..100)");
		p1.setDefault(5);
		
		ParamList p2 = new ParamList("method", false, true);
		String []paux = new String[3];
		paux[0] = "ZERO";
		paux[1] = "WRAP";
		paux[2] = "PAD";
		p2.setDefault(paux);
		p2.setDescription("Method to process the border");
		
		ParamBool p3 = new ParamBool ("strong", false, true);
		p3.setDescription("Indicates if the sharpen is strong");
		p3.setDefault(false);

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.BIT || img instanceof JIPImgGeometric) 
			throw new JIPException("Function Sharpen can not be applied to this image format.");
		
		String method = getParamValueString("method");
		int level = getParamValueInt("level"), aux;
		boolean strong = getParamValueBool("strong");

		if (level < 1 || level > 100) 
			throw new JIPException("Level parameter must be in range [1,100]");
		
		double[] mat = new double[9];
		double value = -level / 10.0;
		mat[1] = mat[3] = mat[5] = mat[7] = value;
		if (strong) {
			mat[0] = mat[2] = mat[6] = mat[8] = value;
			aux = -8;
		}
		else {
			aux = -4;
		}
		mat[4] = value * aux + 1;		
		
		Function2D convolution = new ConvolveImage();
		JIPImgBitmap convo = (JIPImgBitmap)JIPImage.newImage(3, 3, ImageType.FLOAT);
		convo.setAllPixels(mat);
		convolution.setParamValue("image", convo);
		convolution.setParamValue("div", 1.0f);
		convolution.setParamValue("mult", 1.0f);
		convolution.setParamValue("method", method);
		
		return convolution.processImg(img);
	}
}

