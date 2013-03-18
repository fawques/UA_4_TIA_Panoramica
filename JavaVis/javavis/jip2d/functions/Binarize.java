package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpByte;

/**
 * It converts a BYTE image into a binary image. To do that, a pixel in the output image 
 * is 1 if the value in the corresponding input image is between the range [u1, u2], and 
 * 0 otherwise.<br />
 * It applies to BYTE image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE type.</li>
 * <li><em>u1</em>: Integer value which indicates the lower bound of the range to considerer the 
 * pixel as 1 (default 128).</li>
 * <li><em>u2</em>: Integer value which indicates the upper bound of the range to considerer the 
 * pixel as 1 (default 255).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The original image in BIT type.</li>
 * </ul><br />
 */
public class Binarize extends Function2D {
	private static final long serialVersionUID = -7262973524107183332L;

	public Binarize() {
		super();
		name = "Binarize";
		description = "Converts a BYTE image into binary image.";
		groupFunc = FunctionGroup.Segmentation;

		ParamInt p1 = new ParamInt("u1", false, true);
		p1.setDefault(128);
		p1.setDescription("Lower bound of the range to consider as 1");
		
		ParamInt p2 = new ParamInt("u2", false, true);
		p2.setDefault(255);
		p2.setDescription("Upper bound of the range to consider as 1");

		addParam(p1);
		addParam(p2);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPBmpBit res = null;
		int lowBoundRange = getParamValueInt("u1");
		int upperBoundRange = getParamValueInt("u2");
		
		if (img.getType() == ImageType.BYTE) {
			int width = img.getWidth();
			int height = img.getHeight();
			int totalPix = width*height;
			int numBands = ((JIPBmpByte)img).getNumBands();
			res = new JIPBmpBit(numBands, width, height);
			long totalPerc = totalPix * numBands;
			
			for (int nb=0; nb < numBands; nb++) {
				double[] bmp = ((JIPBmpByte)img).getAllPixels(nb);
				boolean[] bin = new boolean[totalPix];
				for (int k=0; k < totalPix; k++) {
					bin[k] = (bmp[k] >= lowBoundRange && bmp[k] <= upperBoundRange);
					percProgress = (100*((nb+1)*totalPix + k))/(int)totalPerc;
				}
				res.setAllPixelsBool(nb, bin);
			}
		}
		else
			throw new JIPException("Function Binarize only defined for BYTE images.");
		
		return res;
	}
}

