package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It converts a BYTE, BIT, SHORT or FLOAT image into a COLOR image.<br />
 * To do that, a conversion between original ranges (e.g. 0..65556) into color 
 * range (0..255) must be done.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a gray scale image (BYTE, BIT, SHORT or 
 * FLOAT).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>An image equivalent to the gray scale but in COLOR type.</li>
 * </ul><br />
 */
public class GrayToColor extends Function2D {
	private static final long serialVersionUID = -520184657904679850L;

	public GrayToColor() {
		super();
		name = "GrayToColor";
		description = "Converts a BYTE, BIT, SHORT or FLOAT image into a COLOR image.";
		groupFunc = FunctionGroup.Transform;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType type = img.getType();
		JIPBmpColor res = null;

		if (type == ImageType.FLOAT || type == ImageType.BYTE || type == ImageType.BIT || type == ImageType.SHORT) {
			res = new JIPBmpColor(img.getWidth(), img.getHeight());
			double[] temp = ((JIPImgBitmap)img).getAllPixels();
			// In case of float type, we assume that values in the float type are between 0.0 and 1.0
			if (type == ImageType.FLOAT || type == ImageType.BIT) { 
				for (int i=0; i < temp.length; i++) {
					temp[i] *= 255;
					if (temp[i] < 0.0) temp[i] = 0.0;
					if (temp[i] > 255) temp[i] = 255;
				}
			}
			else if (type == ImageType.SHORT) { 
				for (int i=0; i < temp.length; i++) {
					temp[i] = 255*temp[i]/65535;
					if (temp[i] < 0.0) temp[i] = 0.0;
					if (temp[i] > 255) temp[i] = 255;
				}
			}
			res.setAllPixels(temp);
		}
		else 
			throw new JIPException("Function GrayToColor can not be applied to this image format.");
		
		return res;
	} 
}

