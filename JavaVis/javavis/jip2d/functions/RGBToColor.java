package javavis.jip2d.functions;

import java.awt.Color;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It converts a RGB image into another color format (HSB, HSI or YCbCr).<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR image.</li>
 * <li><em>format</em>: List which indicates the image type result (HSB, HSI, YCbCr)
 * (default HSB).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same COLOR image in HSB, HSI or YCbCr format.</li>
 * </ul><br />
 */
public class RGBToColor extends Function2D {
	private static final long serialVersionUID = 3353071832710785482L;

	public RGBToColor() {
		super();
		name = "RGBToColor";
		description = "Converts RGB image into another color format.";
		groupFunc = FunctionGroup.Transform;
		
		ParamList p = new ParamList("format", false, true);
		p.setDescription("Format to convert to: HSB, HSI, YCbCr");
		String[] list = new String[3];
		list[0] = "HSB";
		list[1] = "HSI";
		list[2] = "YCbCr";
		p.setDefault(list);
		
		addParam(p);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.COLOR) 
			throw new JIPException("Function RGBToColor can only be applied to color images.");
		
		JIPBmpColor imgColor = (JIPBmpColor)img;
		int size = img.getWidth()*img.getHeight();
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(3, img.getWidth(), img.getHeight(), ImageType.FLOAT);
		double[] F = new double[size];
		double[] S = new double[size];
		double[] T = new double[size];
		double[] R = imgColor.getAllPixelsRed();
		double[] G = imgColor.getAllPixelsGreen();
		double[] B = imgColor.getAllPixelsBlue();
		
		String format = getParamValueString("format");
		if (format.equals("HSB")) {
			float vector[] = new float[3];
			for (int i=0; i<size; i++) {
				Color.RGBtoHSB((int)R[i], (int)G[i], (int)B[i], vector);
				F[i] = vector[0];
				S[i] = vector[1];
				T[i] = vector[2];
			}
		} else if (format.equals("HSI")) {
			double sigma, aux, aux2;
			for (int i=0; i<size; i++) {
				T[i] = (R[i]+G[i]+B[i])/765.0f; //Normalizing [0..1]
				S[i] = 1.0f-((3.0f/(R[i]+G[i]+B[i]))*Math.min(R[i],Math.min(G[i],B[i])));
				aux = (1/2.0f)*((R[i]-G[i])+(R[i]-B[i]));
				aux2 = Math.sqrt(((R[i]-G[i])*(R[i]-G[i]))+((R[i]-B[i])*(G[i]-B[i])));
				sigma = Math.acos(aux/aux2);
			
				if (B[i] <= G[i])
					F[i] = sigma/(2.0*Math.PI);
				else
					F[i] = (2.0*Math.PI-sigma)/(2.0*Math.PI);
			}
		} else if (format.equals("YCbCr")) {
			for (int i=0; i<size; i++) {
				F[i] = (65.481*R[i]+128.553*G[i]+24.966*B[i])/55845.0;
				S[i] = (28560+(-37.797*R[i]+-74.203*G[i]+112.0*B[i]))/57120.0;
				T[i] = (28560+(112.0*R[i]+-93.786*G[i]+-18.214*B[i]))/57120.0;
			}	
		} else throw new JIPException ("RGBToColor: output format not recognized");
		
		res.setAllPixels(0,F);
		res.setAllPixels(1,S);
		res.setAllPixels(2,T);
		
		return res;
	}
}

