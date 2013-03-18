package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It calculates the entropy of an image.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>entropy</em>: A float value indicating the entropy value for the input image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same input image.</li>
 * </ul><br />
 */
public class Entropy extends Function2D {
	private static final long serialVersionUID = 5002721718109542932L;

	private static final int HISTOGRAM_SIZE = 256;
	private static final int HISTOGRAM_G_SIZE = 181;
	
	public Entropy() {
		super();
		name = "Entropy";
		description = "Calculates the entropy of the image.";
		groupFunc = FunctionGroup.Others;

		ParamFloat p1 = new ParamFloat("entropy", false, false);

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Entropy can not be applied to this image format.");
		
		double[] pixels;
		int[] histo;
		float[] histoNorm;
		float entropy;
		JIPImgBitmap imgBmp;
		
		//First, it converts all possible formats into BYTE
		if (img.getType() == ImageType.COLOR) {
			ColorToGray fcg = new ColorToGray();
			fcg.setParamValue("gray", "BYTE");
			imgBmp = (JIPImgBitmap)fcg.processImg(img);
		}
		else if (img.getType() != ImageType.BYTE) {
			GrayToGray fgg = new GrayToGray();
			fgg.setParamValue("gray", "BYTE");
			imgBmp = (JIPImgBitmap)fgg.processImg(img);
		}
		else
			imgBmp = (JIPImgBitmap)img;
		
		pixels = imgBmp.getAllPixels();
		histo = new int[HISTOGRAM_SIZE];
		histoNorm = new float[HISTOGRAM_SIZE];
		
		for (double d:pixels) histo[(int)d]++;
		entropy = 0.0f;
		for (int i=0; i < HISTOGRAM_SIZE; i++) {
			if (histo[i] != 0) {
				histoNorm[i] = histo[i]/(float)pixels.length;
				entropy -= histoNorm[i]*Math.log(histoNorm[i]);
			}
		}
		
		// Trying gradient
		double mag;
		int[] histog = new int[HISTOGRAM_G_SIZE];
		float[] histogNorm = new float[HISTOGRAM_G_SIZE];
		double max = 0.0f;
		for (int x=1; x < imgBmp.getWidth(); x++)
			for (int y=1; y < imgBmp.getHeight(); y++) {
				mag = Math.sqrt(Math.pow((imgBmp.getPixel(x, y)-imgBmp.getPixel(x-1, y))/2.0, 2.0) + 
				              Math.pow((imgBmp.getPixel(x, y)-imgBmp.getPixel(x, y-1))/2.0, 2.0));
				histog[(int)mag]++;
				if (mag > max) max = mag;
			}
		
		for (int i=0; i < HISTOGRAM_G_SIZE; i++)
			System.out.print(histog[i] + " ");
		System.out.println();
		
		System.out.println("max = " + max);
		
		float entropyg = 0.0f;
		for (int i=0; i < HISTOGRAM_G_SIZE; i++) {
			if (histog[i] != 0) {
				histogNorm[i] = histog[i]/(float)pixels.length;
				entropyg -= histogNorm[i]*Math.log(histogNorm[i]);
			}
		}
		
		System.out.println("Entropy = " + entropy + "; Entropyg = " + entropyg);
		setParamValue("entropy", entropy);
		
		return img;
	}
}

