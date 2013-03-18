package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It calculates the histograms of a color image. The histogram is calculated from the
 * RGB image and the discretization parameter indicates the number of bins.<br />
 * It applies to COLOR image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR image.</li>
 * <li><em>disc</em>: Integer value which indicates the discretization (number of bins)
 * (default 20).</li>
 * <li><em>type</em>: List which indicates the image type to calculate the histogram 
 * (RGB, HSB, YCbCr, HSI) (default RGB).</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>histo</em>: The histogram in a 3 dimensions float array.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The input image remains the same.</li>
 * <ul><br />
 */
public class CalcHistoColor extends Function2D {
	private static final long serialVersionUID = 7003490716984734494L;

	private static final float VALUES_PER_BAND = 256.0f;
	
	public CalcHistoColor() {
		super();
		name = "CalcHistoColor";
		description = "Calculates the histogram of a color image. Applies to COLOR image.";
		groupFunc = FunctionGroup.ImageDB;

		ParamInt p1 = new ParamInt("disc", false, true);
		p1.setDefault(20);
		p1.setDescription("Discretization (number of bins)");
		
		ParamList p2 = new ParamList("type", false, true);
		String []paux = new String[4];
		paux[0] = "RGB";
		paux[1] = "HSB";
		paux[2] = "YCbCr";
		paux[3] = "HSI";
		p2.setDefault(paux);
		p2.setDescription("Color format");
		
		addParam(p1);
		addParam(p2);
		
		// Output parameter
		ParamObject o1 = new ParamObject("histo", false, false);
		o1.setDescription("3 dimensions float array");
		addParam(o1);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		// As we have 256 values for each band, we divide this value
		// by the discretization parameter (number of bins) to get 
		// the number of values in each bin
		String type = getParamValueString("type");
		int disc = getParamValueInt("disc");
		int size = img.getWidth()*img.getHeight();
		float binSize = 1.01f/disc;
		float [][][]acumF = new float[disc][disc][disc];
		JIPImgBitmap image;
		double[] F, S, T;

		// Cheks is the img is a color image
		if (img.getType() == ImageType.COLOR) {
			if (type.equals("RGB")) {
				binSize = VALUES_PER_BAND/disc;
		  		F = ((JIPImgBitmap)img).getAllPixels(0);
		  		S = ((JIPImgBitmap)img).getAllPixels(1);
		  		T = ((JIPImgBitmap)img).getAllPixels(2);
			}
			else {
				RGBToColor frtc = new RGBToColor();
				frtc.setParamValue("format", type);
	
				image = (JIPImgBitmap)frtc.processImg(img);
				F = image.getAllPixels(0);
				S = image.getAllPixels(1);
				T = image.getAllPixels(2);
			}
			
	  		// Calculates the histogram
	  		for (int s=0; s < size; s++) 
	  			acumF[(int)(F[s]/binSize)][(int)(S[s]/binSize)]
  									[(int)(T[s]/binSize)]++;
	  		
	  		// Normalize, so that the sum is 1
	  		for (int i=0; i < disc; i++) 
	  			for (int j=0; j < disc; j++)
	  				for (int k=0; k < disc; k++)
	  					acumF[i][j][k] = acumF[i][j][k] / size;

	  		// The histogram calculated is stored		  		
	  		setParamValue("histo",acumF);
		}
		else
			throw new JIPException("Function CalcHistoColor only defined for COLOR images.");
		
		return img;
	}
}

