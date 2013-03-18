package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.util.MatMorph;

/**
 * It implements the closure operation. The code reads a file which contains the
 * description of the structural element (se), that is, it contains the dimensions 
 * of the mask (width and height), in the first row, and the matrix of 0s & 1s which 
 * defines the se. To do that, first it applies to the input image the Dilate
 * operation. After, it applies to the result the Erosion operation.<br />
 * It applies to COLOR, BYTE, BIT or SHORT type.<br /><br /> 
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, BIT or SHORT type.</li>
 * <li><em>level</em>: File which contains the description of the structural element.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Closured image in gray scale.</li>
 * </ul><br />
 */
public class Closure extends Function2D {
	private static final long serialVersionUID = -2316890482034204804L;

	public Closure() {
		super();
		name = "Closure";
		description = "Applies the closure operation. Applies to COLOR, BYTE, BIT or SHORT type.";
		groupFunc = FunctionGroup.Math_morph;

		ParamFile p1 = new ParamFile("se", true, true);
		p1.setDescription("Structural Element");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();
		
		if (t == ImageType.EDGES || t == ImageType.POINT || t == ImageType.SEGMENT
				|| t == ImageType.POLY || t == ImageType.FLOAT) 
			throw new JIPException("Function Closure can not be applied to this image type.");

		MatMorph mm = new MatMorph (getParamValueString("se"));
		if (!mm.isCorrect()) 
			throw new JIPException("Error reading the structural element.");
		
		JIPImage auxImg = mm.dilate((JIPImgBitmap)img);
		return mm.erode((JIPImgBitmap)auxImg);	
	}
}

