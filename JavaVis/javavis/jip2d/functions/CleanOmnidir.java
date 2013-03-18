package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;

/**
 * It eliminates the exterior area of an omnidirectional image.<br />
 * It applies to COLOR image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be COLOR image.</li>
 * <li><em>x</em>: Integer value which indicates the X coordinate of the center of the
 * omnidirectional lens (default 241).</li>
 * <li><em>y</em>: Integer value which indicates the Y coordinate of the center of the
 * omnidirectional lens (default 197).</li>
 * <li><em>rint</em>: Integer value which indicates the radius (in pixels) of the internal 
 * circumference (default 25).</li>
 * <li><em>rext</em>: Integer value which indicates the radius (in pixels) of the external 
 * circumference (default 151).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same input image without the inner and outer area of the omnirectional image.</li>
 * </ul><br />
 */
public class CleanOmnidir extends Function2D {
	private static final long serialVersionUID = -6647025430380827941L;

	public CleanOmnidir() {
		super();
		name = "CleanOmnidir";
		description = "Paints in black the useless circular zones of an Omnidirectional Image. Applies to COLOR image.";
		groupFunc = FunctionGroup.RingProjection;

		ParamInt p1 = new ParamInt("x", false, true);
		p1.setDefault(241);
		p1.setDescription("X coordinate of the center of the omnidirectional lens");
		
		ParamInt p2 = new ParamInt("y", false, true);
		p2.setDefault(197);
		p2.setDescription("Y coordinate of the center of the omnidirectional lens");
		
		ParamInt p3 = new ParamInt("rint", false, true);
		p3.setDefault(25);
		p3.setDescription("Radius of the internal circumference (in pixels)");
		
		ParamInt p4 = new ParamInt("rext", false, true);
		p4.setDefault(151);
		p4.setDescription("Radius of the external circumference (in pixels)");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.COLOR)
			throw new JIPException("Function CleanOmnidir can not be applied to this image format.");

		JIPBmpColor imgCol = (JIPBmpColor)img;
		int x = getParamValueInt("x");
		int y = getParamValueInt("y");
		double rInt = getParamValueInt("rint");
		double rExt = getParamValueInt("rext");
		
		for (int w=0; w < img.getWidth(); w++) {
			for (int h=0; h < img.getHeight(); h++) {
				double d = distance(w, h, x, y); 
				if (d < rInt || d > rExt) {
					//Set pixel to Black.
					imgCol.setPixelRed(w, h, 0);
					imgCol.setPixelGreen(w, h, 0);
					imgCol.setPixelBlue(w, h, 0);
				}
			}
		}

		return img;
	}

	
	/**
	 * Method which calculates the distance between the center of the omnidirectional lens 
	 * and a point of the image.
	 * @param x1 X coordinate of the point of the image.
	 * @param y1 Y coordinate of the point of the image.
	 * @param x2 X coordinate of the center of the omnidirectional lens.
	 * @param y2 Y coordinate of the center of the omnidirectional lens.
	 * @return The distance value.
	 */
	private double distance(int x1, int y1, int x2, int y2){
		double r1 = x1 - x2;
		double r2 = y1 - y2;
		
		return Math.sqrt(r1*r1 + r2*r2);
	}
}

