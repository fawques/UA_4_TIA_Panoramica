package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamImage;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies a convolution of an image using the information from another image as 
 * convolution mask.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>image</em>: Image to be used in convolution as mask. It has to be a bitmap image in 
 * a JIP file (required).</li>
 * <li><em>mult</em>: Real value which indicates the multiply value (default 1.0).</li>
 * <li><em>div</em>: Real value which indicates the divide value (default 1.0).</li>
 * <li><em>method</em>: List of methods that indicates how to manager the borders (default ZERO).
 * <ul>
 * <li><em>ZERO</em>: New rows and columns are added (depending of the radius) and they take a 0 value.</li>
 * <li><em>PAD</em>: The same, but the new rows and columns take the value of the closest pixel in the image.</li> 
 * <li><em>WRAP</em>: The following row to the last is the first, the previous to the first is the last, and the same to columns.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the input image type.<br />
 * Warning: If the result of the division is negative or zero and the input image is not
 * a FLOAT type, you will see the result as a black image.</li>
 * </ul><br />
 */
public class ConvolveImage extends Function2D {
	private static final long serialVersionUID = -6863560219268735784L;

	/**
	 * @uml.property  name="mask" multiplicity="(0 -1)" dimension="1"
	 */
	double []mask;

	public ConvolveImage() {
		super();
		name = "ConvolveImage";
		description = "Applies a convolution for an image using the information from another image as convolution mask. Applies COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Convolution;

		ParamImage p1 = new ParamImage("image", true, true);
		p1.setDescription("Image for convolution");
		
		ParamFloat p2 = new ParamFloat("mult", false, true);
		p2.setDefault(1.0f);
		p2.setDescription("Multiplier");
		
		ParamFloat p3 = new ParamFloat("div", false, true);
		p3.setDefault(1.0f);
		p3.setDescription("Divisor");
		
		ParamList p4 = new ParamList("method", false, true);
		String []paux = new String[2];
		paux[0] = "PAD";
		paux[1] = "ZERO";
		//String []paux = new String[3];
		//paux[0]="ZERO";
		//paux[1]="PAD";
		//paux[2]="WRAP";
		p4.setDefault(paux);
		p4.setDescription("Method to process edges");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric || img instanceof JIPBmpBit) 
			throw new JIPException("Function ConvolveImage can not be applied to this image type.");
		
		JIPImage convo = getParamValueImg("image");
		if (convo instanceof JIPImgGeometric) 
			throw new JIPException("Function ConvolveImage can not be applied with this image type.");
		
		float mult = getParamValueFloat("mult");
		float div = getParamValueFloat("div");
		String method = getParamValueString("method");
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;

		//////////////////////////////
		int maskWidth, maskHeight;
		double[] bitmap;
		int width, height; //input image dimensions
		boolean isZERO = false;
		int radiusW, radiusH;
		int oddW, oddH;
		int count;
		int row, col;
		width = img.getWidth();
		height = img.getHeight();
		maskWidth = convo.getWidth();
		maskHeight = convo.getHeight();
		radiusW = (maskWidth)/2;
		radiusH = (maskHeight)/2;
		oddW = maskWidth%2;
		oddH = maskHeight%2;
		int numBands = imgBmp.getNumBands();
		double []result = new double[width*height];
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, imgBmp.getType());
		
		if (div == 0.0)
			throw new JIPException ("Divisor has not to be 0.");
			
		double ratio = mult / div;

		int i, j, pos;
		int newrow, newcol;
		double []A;
		
		if (method.equals("ZERO")) isZERO = true;
		if (convo instanceof JIPBmpColor) {
			Function2D ctg = new ColorToGray();
			JIPImage imgAux = ctg.processImg(convo);
			mask = ((JIPImgBitmap)imgAux).getAllPixels();
		}
		else
			mask = ((JIPImgBitmap)convo).getAllPixels();
		
		for (int nb=0; nb < numBands; nb++) {
			bitmap = imgBmp.getAllPixels(nb);
			for (count=0; count < bitmap.length; count++) {
				row = count / width;
				col = count % width;
				if (isZERO && (row < radiusH || row > height-radiusH || col < radiusW || col > width-radiusW))
					result[count] = 0;
				else {
					A = new double[mask.length];
					pos = 0;
					for (i=row-radiusH; i < row+radiusH+oddH; i++)
					{
						if (i < 0)
							newrow = 0;
						else if (i >= height)
							newrow = (height-1)*width;
						else
							newrow = i*width;
						for (j=col-radiusW; j < col+radiusW+oddW; j++) {
							if (j < 0)
								newcol = 0;
							else if (j >= width)
								newcol = width -1;
							else newcol = j;
							
							A[pos] = bitmap[newrow + newcol];
							pos++;
						}
					}
					result[count] = reduce(A) * ratio;
				}
			}
			res.setAllPixels(nb, result);
		}
		/////////////////////////////
		return res;
	}
	
	
	/**
	 * Method which reduces a matrix into a real value applying a mask.
	 * @param A The matrix to reduce.
	 * @return A real value with the result of reducing the matrix.
	 */
	private double reduce(double []A)
	{
		double result = 0.0;
		
		for (int i=0; i < A.length; i++) {
			result += A[i] * mask[i];
		}
		
		return result;
	}
}

