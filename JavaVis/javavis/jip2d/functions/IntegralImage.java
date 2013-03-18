package javavis.jip2d.functions;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It calculates integral image.<br />
 * It applies to BYTE type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image.</li>
 * </ul><br />
 */
public class IntegralImage extends Function2D {
	private static final long serialVersionUID = -7864060115649821642L;

	public IntegralImage() {
		super();
		name = "IntegralImage";
		description = "Calculates integral image. Applies to BYTE type.";
		groupFunc = FunctionGroup.FeatureExtract;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.BYTE) {
			int width = img.getWidth();
			int height = img.getHeight();
			JIPBmpFloat res = new JIPBmpFloat(width, height);
			double[] bmp;
			double[] QI;
			double[] R;

			bmp = ((JIPImgBitmap)img).getAllPixels();
			QI = new double[bmp.length];
			R = new double[bmp.length];
			
			for (int x=0; x < width; x++) 
				R[x] = bmp[x];
			QI[0] = bmp[0];
			for (int y=1; y < height; y++) 
				QI[y*width] = QI[(y-1)*width] + bmp[y*width];

			
			// Calculates the Integral Image of entropy
			for (int x=1; x < width; x++)  
				for (int y=1; y < height; y++) { 
					R[y*width+x] = R[(y-1)*width+x] + bmp[y*width+x];
					QI[y*width+x] = QI[y*width+x-1] + R[y*width+x];
				}
			
			res.setAllPixels(QI);
			return res;
		}
		else 
			throw new JIPException("Function IntegralImage can not be applied to this image type.");
	}
}

