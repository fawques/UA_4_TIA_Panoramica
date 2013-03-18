package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.util.MatMorph;

/**
 * It implements the opening morphological operation. The code reads a file which contains the
 * description of the structural element (se), that is, it contains the dimensions 
 * of the mask (width and height) in the first row, and matrix of 0s & 1s which defines the se.<br />
 * It applies to COLOR, BYTE, BIT or SHORT type.<br /><br /> 
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, BIT or SHORT type.</li>
 * <li><em>level</em>: File which contains the description of the structural element.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Opened image in gray scale.</li>
 * </ul><br />
 */
public class Opening extends Function2D {
	private static final long serialVersionUID = 221852059049077418L;

	public Opening() {
		super();
		name = "Opening";
		description = "Applies the opening operation. Applies to COLOR, BYTE, BIT or SHORT type.";
		groupFunc = FunctionGroup.Math_morph;

		ParamFile p1 = new ParamFile("se", true, true);
		p1.setDescription("Structural Element");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Opening can not be applied to this image type.");

		MatMorph mm = new MatMorph (getParamValueString("ee"));
		if (!mm.isCorrect()) 
			throw new JIPException("Error reading the structural element.");

		JIPImage auxImg = mm.erode((JIPImgBitmap)img);
		return mm.dilate((JIPImgBitmap)auxImg);	
	}
}

