package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It creates the Ring Mask necessary for Transformation-Ring-Projection operations. Each 
 * image corresponds with a concentric ring of the omnidirectional image and it is separated 
 * in a numFrame. The first numFrame is a mask, indicating for each pixel to which ring 
 * owns.<br />
 * It applies to bitmap images and it has to be a sequence.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>seq</em>: The sequence input. Each image has to be a bitmap image.</li>
 * <li><em>x</em>: Integer value which indicates the X coordinate of the center of the 
 * omnidirectional lens (default 241).</li>
 * <li><em>y</em>: Integer value which indicates the Y coordinate of the center of the 
 * omnidirectional lens (default 197).</li>
 * <li><em>rint</em>: Integer value which indicates the internal circumference radius (in pixels)
 * (default 25).</li>
 * <li><em>rext</em>: Integer value which indicates the external circumference radius (in pixels)
 * (default 151).</li>
 * <li><em>nrings</em>: Integer value which indicates the number of rings (default 4).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The nring frames and a mask image.</li>
 * </ul><br />
 */
public class RingCreateMask extends Function2D {
	private static final long serialVersionUID = 6965913568509743576L;

	public RingCreateMask() {
		super();
		name = "RingCreateMask";
		description = "Creates the Ring Mask necessary for Transformation-Ring-Projection operations. It has to be bitmap images and a sequence.";
		groupFunc = FunctionGroup.RingProjection;
		
		ParamInt p1 = new ParamInt("x", false, true);
		p1.setDefault(241);
		p1.setDescription("X coord of the center of the rings");
		
		ParamInt p2 = new ParamInt("y", false, true);
		p2.setDefault(197);
		p2.setDescription("Y coord of the center of the rings");
		
		ParamInt p3 = new ParamInt("rint", false, true);
		p3.setDefault(25);
		p3.setDescription("Internal circumference radius (in pixels)");
		
		ParamInt p4 = new ParamInt("rext", false, true);
		p4.setDefault(151);
		p4.setDescription("External circumference radius (in pixels)");
		
		ParamInt p5 = new ParamInt("nrings", false, true);
		p5.setDefault(4);
		p5.setDescription("Number of rings");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException("Function RingCreateMask is applied to complete sequence.");
	}
	
	public Sequence processSeq(Sequence seq) throws JIPException {
		JIPImage img = seq.getFrame(0);
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function RingCreateMask can only be applied to bitmaps images.");
		
		int cx =  getParamValueInt("x");
		int cy =  getParamValueInt("y");
		int nRings = getParamValueInt("nrings");
		double rInt =  getParamValueInt("rint");
		double rExt =  getParamValueInt("rext");
		double ri[] = new double[nRings+1];
		JIPImgBitmap maskimg = (JIPImgBitmap)JIPImage.newImage(1, img.getWidth(), img.getHeight(), ImageType.BYTE);
		JIPImgBitmap imgcuts[] = new JIPImgBitmap[nRings];
		
		for (int i=0; i < nRings; i++)
			imgcuts[i] = (JIPImgBitmap)JIPImage.newImage(1, img.getWidth(), img.getHeight(), img.getType());
		
		for (int i=0; i <= nRings; i++)
			ri[i] = (rExt-rInt)*i/nRings + rInt;
		
		for (int w=0; w < img.getWidth(); w++){
			for (int h=0; h < img.getHeight(); h++){
				double d = distance(w,h, cx,cy);
				for (int i=0; i <= nRings;i++){
					if (ri[i] > d){
						maskimg.setPixel(w,h,i);
						if (i > 0){
							if (img.getType() == ImageType.COLOR) {
								((JIPBmpColor)imgcuts[i-1]).setPixelRed(w,h,((JIPBmpColor)img).getPixelRed(w,h));
								((JIPBmpColor)imgcuts[i-1]).setPixelGreen(w,h,((JIPBmpColor)img).getPixelGreen(w,h));
								((JIPBmpColor)imgcuts[i-1]).setPixelBlue(w,h,((JIPBmpColor)img).getPixelBlue(w,h));
							}
							else
								imgcuts[i-1].setPixel(w, h, ((JIPImgBitmap)img).getPixel(w,h));
						}
						break;
					}
				}
			}
		}
			
		seq = new Sequence(maskimg);
		
		for(int i=0; i < nRings; i++)
			seq.addFrame(imgcuts[i]);
		
		return seq;
	}

	
	/**
	 * Method which calculates the distance between two points.
	 * @param x1 The X coordinate of the first point.
	 * @param y1 The Y coordinate of the first point.
	 * @param x2 The X coordinate of the second point.
	 * @param y2 The Y coordinate of the second point.
	 * @return The distance value.
	 */
	private double distance(int x1, int y1, int x2, int y2){
		double r1 = x1 - x2;
		double r2 = y1 - y2;
		
		return Math.sqrt(r1*r1 + r2*r2);
	}
}

