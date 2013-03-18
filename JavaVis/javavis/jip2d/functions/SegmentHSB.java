package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It segments a HSB image using H (hue), S (saturation) and B (brightness) and binarizes 
 * the image.<br />
 * It applies to HSB image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a HSB image.</li>
 * <li><em>h</em>: Real value which indicates the hue value to binarize (default 0.5).</li>
 * <li><em>herror</em>: Real value which indicates the error admitted in hue band (default 0.01).</li>
 * <li><em>s</em>: Real value which indicates the saturation value to binarize (default 0.5).</li>
 * <li><em>serror</em>: Real value which indicates the error admitted in saturation band (default 0.5).</li>
 * <li><em>b</em>: Real value which indicates the brightness value to binarize (default 0.5).</li>
 * <li><em>berror</em>: Real value which indicates the error admitted in brightness band (default 0.5).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A processed binary image.</li>
 * </ul><br />
 */
public class SegmentHSB extends Function2D {
	private static final long serialVersionUID = -2759433399980478199L;

	public SegmentHSB() {
		super();
		name = "SegmentHSB";
		description = "Segments an HSB image and binarizes it.";
		groupFunc = FunctionGroup.Segmentation;

		ParamFloat p1 = new ParamFloat("h", false, true);
		p1.setDefault(0.5f);
		p1.setDescription("Hue value");
		
		ParamFloat p2 = new ParamFloat("herror", false, true);
		p2.setDefault(0.01f);
		p2.setDescription("Margin of hue error");
		
		ParamFloat p3 = new ParamFloat("s", false, true);
		p3.setDefault(0.5f);
		p3.setDescription("Saturation value");
		
		ParamFloat p4 = new ParamFloat("serror", false, true);
		p4.setDefault(0.5f);
		p4.setDescription("Margin of saturation error");
		
		ParamFloat p5 = new ParamFloat("b", false, true);
		p5.setDefault(0.5f);
		p5.setDescription("Brightness value");
		
		ParamFloat p6 = new ParamFloat("berror", false, true);
		p6.setDefault(0.5f);
		p6.setDescription("Margin of brightness error");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.FLOAT || ((JIPImgBitmap)img).getNumBands() <3) 
			throw new JIPException("Function SegmentHSB can only be applied to a FLOAT image and" +
					" its band number must be at least 3 (HSB image).");

		float valueH =  getParamValueFloat("h");
		float errorH =  getParamValueFloat("herror");
		float valueS =  getParamValueFloat("s");
		float errorS =  getParamValueFloat("serror");
		float valueB =  getParamValueFloat("b");
		float errorB =  getParamValueFloat("berror");
		int width = img.getWidth();
		int height = img.getHeight();
		boolean []all = new boolean[width*height];
		float[] H = ((JIPBmpFloat)img).getAllPixelsFloat(0);
		float[] S = ((JIPBmpFloat)img).getAllPixelsFloat(1);
		float[] B = ((JIPBmpFloat)img).getAllPixelsFloat(2);
		float max = valueH+errorH;
		float min = valueH-errorH;
		
		for (int i=0; i < width*height; i++) {
			if (min < 0.0 && H[i] > 0.5) 
				H[i] -= 1.0;
			if (max > 1.0 && H[i] < 0.5) 
				H[i] += 1.0;
			all[i] = (H[i] >= min) && (H[i] <= max)
					&& (S[i] >= valueS - errorS) && (S[i] <= valueS + errorS) 
					&& (B[i] >= valueB - errorB) && (B[i] <= valueB + errorB);
		}
		
		JIPBmpBit res = (JIPBmpBit)JIPImage.newImage(width, height, ImageType.BIT);
		res.setAllPixelsBool(all);
		
		return res;
	}
}

