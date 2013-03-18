package javavis.jip2d.functions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpByte;

/**
 * It applies a convolution of an image using a matrix (mask) defined in a text file.<br />
 * In the first row of the file we get the width and height and the rest of rows 
 * define the matrix. To do that, it uses ConvolveImage method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>matrix</em>: File which contains the matrix we use to do the convolution (required) (.txt extension).</li>
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
 * <li>A processed image, with the same input image type.<br />
 * Warning: If the result of the division is negative or zero and the input image is not
 * a FLOAT type, you will see the result as a black image.</li>
 * </ul><br />
 */
public class ConvolveAscii extends Function2D {
	private static final long serialVersionUID = -2958725137803649960L;

	public ConvolveAscii() {
		super();
		name = "ConvolveAscii";
		description = "Applies a convolution of an image using a mask defined in a text file. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Convolution;

		ParamFile p1 = new ParamFile("matrix", true, true);
		p1.setDescription("Matrix for convolution (.txt extension)");
		
		ParamFloat p2 = new ParamFloat("mult", false, true);
		p2.setDefault(1.0f);
		p2.setDescription("Multiplier");
		
		ParamFloat p3 = new ParamFloat("div", false, true);
		p3.setDefault(1.0f);
		p3.setDescription("Divisor");
		
		ParamList p4 = new ParamList("method", false, true);
		String []paux = new String[3];
		paux[0] = "ZERO";
		paux[1] = "WRAP";
		paux[2] = "PAD";
		p4.setDefault(paux);
		p4.setDescription("Method to process the border");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPImage res = null;
		ImageType t = img.getType();
		if (t == ImageType.EDGES || t == ImageType.POINT || t == ImageType.SEGMENT || 
				t == ImageType.POLY || t == ImageType.BIT) 
			throw new JIPException("Function ConvolveAscii can not be applied to this image type.");
		
		String convoMat = getParamValueString("matrix");
		float mult = getParamValueFloat("mult");
		float div = getParamValueFloat("div");
		String method = getParamValueString("method");

		//Check the file extension (only .txt)
		String[] fileExtension = convoMat.split("\\.");
		if (!fileExtension[fileExtension.length -1].equals("txt"))
			throw new JIPException("File must be a .txt file.");
		
		try {
			FileInputStream convoF = new FileInputStream(convoMat);
			Reader r = new BufferedReader(new InputStreamReader(convoF));
			StreamTokenizer st = new StreamTokenizer(r);
			
			st.nextToken();
			int cw = (int) st.nval;
			st.nextToken();
			int ch = (int) st.nval;

			double[] mat = new double[cw * ch];
			for (int count=0; count < cw*ch; count++) {
				st.nextToken();
				if (st.ttype == StreamTokenizer.TT_EOF) 
					throw new JIPException("Error reading ASCII file. Incorrect data.");
				mat[count] = (float) st.nval;
			}
			
			//Incorrect data: more rows or columns than you has declared.
			st.nextToken();
			if (st.ttype != StreamTokenizer.TT_EOF)
				throw new JIPException ("Error reading ASCII file. Incorrect data.");
			
			Function2D convolution = new ConvolveImage();
			JIPBmpByte convo = new JIPBmpByte(cw, ch);
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			convolution.setParamValue("div", div);
			convolution.setParamValue("mult", mult);
			convolution.setParamValue("method", method);

			res = convolution.processImg(img);
			if (convolution.isInfo())
				info = "ConvolveAscii info: " + convolution.getInfo();
		} catch (FileNotFoundException e) {
			throw new JIPException("File Not Found.");
		} catch (IOException e) {
			throw new JIPException("IO Exception.");
		}
		
		return res;
	}
}

