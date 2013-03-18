package javavis.jip2d.functions;

import java.math.BigInteger;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamImage;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It makes arithmetical operations (add, divide, and, etc.) between images and/or a value.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap type.</li>
 * <li><em>op</em>: List which indicates the operator to use (default EXP).</li>
 * <li><em>value</em>: Constant float value. In case you use this operator, the image operator
 * is ignored (default 0.0).</li>
 * <li><em>imgOp</em>: Image which is used as second operator if it is not a fixed value. It
 * has to be a JIP file.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image depending on selected operator.</li>
 * </ul><br />
 */
public class Op extends Function2D {
	private static final long serialVersionUID = 8402536396742702064L;

	public Op() {
		super();
		name = "Op";
		description = "Make arithmetical operations between input pixels and a value or another image.";
		groupFunc = FunctionGroup.Others;

		ParamList p1 = new ParamList("op", false, true);
		String []paux = new String[14];
		paux[0] = OperationType.EXP.toString();
		paux[1] = OperationType.LOG.toString();
		paux[2] = OperationType.SQRT.toString();
		paux[3] = OperationType.SQUARE.toString();
		paux[4] = OperationType.ADD.toString();
		paux[5] = OperationType.SUBST.toString();
		paux[6] = OperationType.MULT.toString();
		paux[7] = OperationType.DIV.toString();
		paux[8] = OperationType.DIST.toString();
		paux[9] = OperationType.MAX.toString();
		paux[10] = OperationType.MIN.toString();
		paux[11] = OperationType.AND.toString();
		paux[12] = OperationType.OR.toString();
		paux[13] = OperationType.XOR.toString();
		p1.setDefault(paux);
		p1.setDescription("Operation to apply");
		
		ParamFloat p2 = new ParamFloat("value", false, true);
		p2.setDefault(0.0f);
		p2.setDescription("Operator. If you choose this operator, the image operator is ignored.");
		
		ParamImage p3 = new ParamImage("imgOp", false, true);
		p3.setDescription("Image operator. It has to be a JIP file.");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Op can not be applied to this image format.");
		
		JIPImgBitmap res = null;
		String op = getParamValueString("op");
		OperationType operation = Enum.valueOf(OperationType.class, op);
		float value = getParamValueFloat("value");
		JIPImgBitmap imgOp = (JIPImgBitmap)getParamValueImg("imgOp");

		int width = img.getWidth();
		int height = img.getHeight();
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int numBands = imgBmp.getNumBands();
		ImageType type = img.getType();

		if (imgOp != null) {
			if (type != imgOp.getType()) 
				throw new JIPException("Image format must be identical.");
			
			if (width != imgOp.getWidth() || height != imgOp.getHeight()) 
				throw new JIPException("Image sizes must be identical.");
			
			if (numBands != imgOp.getNumBands()) 
				throw new JIPException("Number of bands must be identical.");
		}
		
		if ((operation == OperationType.AND || operation == OperationType.OR || operation == OperationType.XOR) && type == ImageType.FLOAT) 
			throw new JIPException("Logical operators can not be applied to this image format.");

		res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, type);
		// For each band
		for (int nb=0; nb < numBands; nb++) {
				double valAux = value;
				double[] bmp = imgBmp.getAllPixels(nb);
				double[] bin = new double[width * height];
				for (int y=0; y < height; y++)
					for (int x=0; x < width; x++) {
						if (imgOp != null) 
							valAux = imgOp.getPixel(nb, x, y);
						bin[x + y * width] = calcValue(bmp[x + y * width], operation, valAux);
					}
				res.setAllPixels(nb, bin);
		}
		return res;
	}

	
	/**
	 * Method which calculates the operation value. 
	 * @param val The first value.
	 * @param p Operation type.
	 * @param valAux The second value (if you put a value in the value parameter).
	 * @return The result.
	 */
	private double calcValue(double val, OperationType p, double valAux) {
		double valRes = 0;
		BigInteger val1, val2;
		switch (p) {
			case EXP :
				valRes = Math.exp(val);
				break;
			case LOG :
				valRes = Math.log(val);
				break;
			case SQRT :
				valRes = Math.sqrt(val);
				break;
			case SQUARE :
				valRes = Math.pow(val, 2.0);
				break;
			case ADD :
				valRes = val + valAux;
				break;
			case SUBST :
				valRes = val - valAux;
				break;
			case MULT :
				valRes = val * valAux;
				break;
			case DIV :
				if (valAux == 0)
					valRes = 1000000;
				else
					valRes = val / valAux;
				break;
			case DIST :
				valRes = Math.pow(val - valAux, 2.0);
				break;
			case MAX :
				valRes = Math.max(val, valAux);
				break;
			case MIN :
				valRes = Math.min(val, valAux);
				break;
			case AND :
				val1 = new BigInteger("" + val);
				val2 = new BigInteger("" + valAux);
				valRes = (val1.and(val2)).intValue();
				break;
			case OR :
				val1 = new BigInteger("" + val);
				val2 = new BigInteger("" + valAux);
				valRes = (val1.or(val2)).intValue();
				break;
			case XOR :
				val1 = new BigInteger("" + val);
				val2 = new BigInteger("" + valAux);
				valRes = (val1.xor(val2)).intValue();
				break;
		}
		return valRes;
	}
	
	
	enum OperationType {/**
	 * @uml.property  name="eXP"
	 * @uml.associationEnd  
	 */
	EXP, /**
	 * @uml.property  name="lOG"
	 * @uml.associationEnd  
	 */
	LOG, /**
	 * @uml.property  name="sQRT"
	 * @uml.associationEnd  
	 */
	SQRT, /**
	 * @uml.property  name="sQUARE"
	 * @uml.associationEnd  
	 */
	SQUARE, /**
	 * @uml.property  name="aDD"
	 * @uml.associationEnd  
	 */
	ADD, /**
	 * @uml.property  name="sUBST"
	 * @uml.associationEnd  
	 */
	SUBST, /**
	 * @uml.property  name="mULT"
	 * @uml.associationEnd  
	 */
	MULT, /**
	 * @uml.property  name="dIV"
	 * @uml.associationEnd  
	 */
	DIV, /**
	 * @uml.property  name="dIST"
	 * @uml.associationEnd  
	 */
	DIST, /**
	 * @uml.property  name="mAX"
	 * @uml.associationEnd  
	 */
	MAX, /**
	 * @uml.property  name="mIN"
	 * @uml.associationEnd  
	 */
	MIN, /**
	 * @uml.property  name="aND"
	 * @uml.associationEnd  
	 */
	AND, /**
	 * @uml.property  name="oR"
	 * @uml.associationEnd  
	 */
	OR, /**
	 * @uml.property  name="xOR"
	 * @uml.associationEnd  
	 */
	XOR;}
}

