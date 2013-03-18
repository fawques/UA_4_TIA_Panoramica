package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It extends a catadrioptic image as it is a cylinder.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>x</em>: Integer value which indicates the X coordinate of the center of the 
 * omnidirectional lens (default 241).</li>
 * <li><em>y</em>: Integer value which indicates the Y coordinate of the center of the 
 * omnidirectional lens (default 197).</li>
 * <li><em>rint</em>: Integer value which indicates the internal circumference radius (in pixels)
 * (default 25).</li>
 * <li><em>rext</em>: Integer value which indicates the external circumference radius (in pixels)
 * (default 151).</li>
 * <li><em>maxw</em>: Integer value which indicates the rectangular image width (default 900).</li>
 * <li><em>maxh</em>: Integer value which indicates the rectangular image height (default 200).</li>
 * <li><em>sphmodel</em>: Boolean value which indicates the spherical model for height (loses 
 * resolution) (default true).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Output rectified image.</li>
 *</ul><br />
*/
public class RectifyOmnidir extends Function2D {
	private static final long serialVersionUID = 7653378936800769989L;
	
	public RectifyOmnidir() {
		super();
		name = "RectifyOmnidir";
		description = "Transforms a catadioptric image into a rectangular one. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.RingProjection;

		ParamInt p1 = new ParamInt("x", false, true);
		p1.setDefault(241);
		p1.setDescription("X coord of the center of the omnidirectional lens");
		
		ParamInt p2 = new ParamInt("y", false, true);
		p2.setDefault(197);
		p2.setDescription("Y coord of the center of the omnidirectional lens");
		
		ParamInt p3 = new ParamInt("rint", false, true);
		p3.setDefault(25);
		p3.setDescription("Internal circumference radius (in pixels)");
		
		ParamInt p4 = new ParamInt("rext", false, true);
		p4.setDefault(151);
		p4.setDescription("External circumference radius (in pixels)");
		
		ParamInt p5 = new ParamInt("maxw", false, true);
		p5.setDefault(900);
		p5.setDescription("Rectangular image width");
		
		ParamInt p6 = new ParamInt("maxh", false, true);
		p6.setDefault(200);
		p6.setDescription("Rectangular image height");
		
		ParamBool p7 = new ParamBool("sphmodel", false, true);
		p7.setDefault(true);
		p7.setDescription("Spherical model for height (loses resolution)");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric || img.getType() == ImageType.BIT) 
			throw new JIPException("Function RectifyOmnidir can not be applied to this image type.");

		int cx =  getParamValueInt("x");
		int cy =  getParamValueInt("y");
		int rInt =  getParamValueInt("rint");
		int rExt =  getParamValueInt("rext");
		int maxw =  getParamValueInt("maxw");
		int maxh =  getParamValueInt("maxh");
		boolean sph_model = getParamValueBool("sphmodel");
		JIPImgBitmap imgrect = (JIPImgBitmap)JIPImage.newImage(maxw, maxh, img.getType());
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		
		double anglescale = Math.PI * 2.0 / maxw;
		double distnorm = rExt-rInt;
		
		for (int x=0; x < maxw; x++){
			double angle = x * anglescale;
			for (int y=0; y < maxh; y++){
				double dist  = fh(sph_model, (maxh - y)/(double)maxh) * distnorm + rInt;
				double xomni = dist * Math.sin(angle) + cx;
				double yomni = dist * Math.cos(angle) + cy;
				if (imgBmp instanceof JIPBmpColor) {
					imgrect.setPixel(0,x,y,imgBmp.getPixel(0,(int)xomni, (int)yomni));
					imgrect.setPixel(1,x,y,imgBmp.getPixel(1,(int)xomni, (int)yomni));
					imgrect.setPixel(2,x,y,imgBmp.getPixel(2,(int)xomni, (int)yomni));
				}
				else 
					imgrect.setPixel(x,y,imgBmp.getPixel((int)xomni, (int)yomni));
			}
		}

		return imgrect;
	}

	
	/**
	 * Method which models the mapping between radius and height.
	 * @param sph_model The model.
	 * @param x X.
	 * @return The mapping between radius and height.
	 */
	private double fh (boolean sph_model, double x){
		if (sph_model)
			return x*x;
		else
			return x;
	}	
}

