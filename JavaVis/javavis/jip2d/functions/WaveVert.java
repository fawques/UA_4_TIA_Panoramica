package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies a wave effect to the input image, with vertical distortion.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>numWaves</em>: Integer value which indicates the number of waves (default 5).</li>
 * <li><em>perc</em>: Integer value which indicates the distortion percentage (default 5).</li>
 * <li><em>disp</em>: Integer value which indicates the wave displacement (default 0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with wave distortion and the same type that the input image.</li>
 * </ul><br />
 */
public class WaveVert extends Function2D {
	private static final long serialVersionUID = -705618644823140479L;

	public WaveVert() {
		super();
		name = "WaveVert";
		description = "Applies a wave effect to the input image, with vertical distortion. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("numWaves", false, true);
		p1.setDefault(5);
		p1.setDescription("Number of waves");
		
		ParamInt p2 = new ParamInt("perc", false, true);
		p2.setDefault(5);
		p2.setDescription("Distortion percentage");
		
		ParamInt p3 = new ParamInt("disp", false, true);
		p3.setDefault(0);
		p3.setDescription("Scrolling");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function WaveVert can not be applied to this image format.");
		
		ImageType t = img.getType();
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int nWaves = getParamValueInt("numWaves");
		int radius1 = getParamValueInt("perc");
		int displacement = getParamValueInt("disp");
		int width = img.getWidth();
		int height = img.getHeight();
		int numBands = imgBmp.getNumBands();
		double frequence = (nWaves * Math.PI * 2.0) / height;
		double disp = (displacement * nWaves * Math.PI * 2.0) / 100.0;
		double radius = (width * radius1) / 100.0;
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, t);

		for (int nb=0; nb < numBands; nb++) {
			double[] bmp = imgBmp.getAllPixels(nb);
			double[] bin = new double[width * height];
			for (int x=0; x < width; x++) {
				int yDisp = (int) (Math.sin(x * frequence + disp) * radius);
				for (int y=0; y < height; y++) {
					if ((yDisp >= 0) && (yDisp < height))
						bin[x + y * width] = bmp[x + yDisp * width];
					else
						bin[x + y * width] = 0;
					yDisp++;
				}
			}
			res.setAllPixels(nb, bin);
		}
		return res;
	}
}

