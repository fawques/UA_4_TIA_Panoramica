package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies the Mean-means algorithm to segment a gray image.<br />
 * It applies to BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE, SHORT or FLOAT type.</li>
 * <li><em>h</em>: Integer value which indicates the spatial radius (default 3).</li>
 * <li><em>d</em>: Integer value which indicates the color distance (default 25).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A segmented image from input image.</li>
 * </ul><br />
 */
public class MeanShift extends Function2D {
	private static final long serialVersionUID = 2164907395829204535L;

	public MeanShift() {
		super();
		name = "MeanShift";
		description = "Applies the Mean-Shift method. Applies to BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Segmentation;

		ParamInt p1 = new ParamInt("h", false, true);
		p1.setDefault(3);
		p1.setDescription("Spatial radius");
		
		ParamInt p2 = new ParamInt("c", false, true);
		p2.setDefault(25);
		p2.setDescription("Color distance");
		
		addParam(p1);
		addParam(p2);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPImage aux;
		int spatialRatius = getParamValueInt("h");
		int colorDist = getParamValueInt("c");
		
		if (spatialRatius <= 0) 
			throw new JIPException("Spatial radius must be greater than 0.");
		
		if (colorDist <= 0) 
			throw new JIPException("Color distance must be greater than 0.");
		
		if (img instanceof JIPImgGeometric || img instanceof JIPBmpBit || img instanceof JIPBmpColor)
			throw new JIPException("Function MeanShift can not be applied to this image type.");
		
		if (img.getType() == ImageType.COLOR)
			aux = meanshift((JIPBmpColor)img, spatialRatius, colorDist); 
		else 
			aux = meanshift((JIPImgBitmap)img, spatialRatius, colorDist);
		
		return aux;
	}
	
	
	//This method has errors.
	/**
	 * Method which calculates the meanshift function for a color image.
	 * @param img The input image.
	 * @param rad The spatial radius.
	 * @param colorDist The color distance.
	 * @return The result to apply the meanshift function.
	 * @throws JIPException
	 */
	//This method has errors.
	private JIPImage meanshift (JIPBmpColor img, int rad, int colorDist) throws JIPException {
		int width = img.getWidth();
		int height = img.getHeight();
		int colorDist2 = colorDist*colorDist, rad2 = rad*rad;
		JIPBmpColor result = (JIPBmpColor)JIPImage.newImage(img.getWidth(), img.getHeight(), img.getType());

		double[] pixelsRed = img.getAllPixelsRed();
		double[] pixelsGreen = img.getAllPixelsGreen();
		double[] pixelsBlue = img.getAllPixelsBlue();
		double[] pixelsResultR = new double[width*height];
		double[] pixelsResultG = new double[width*height];
		double[] pixelsResultB = new double[width*height];

		double shift = 0;
		int iters = 0;
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {

				int xc = x;
				int yc = y;
				int xcOld, ycOld;
				double YcOld, IcOld, QcOld;
				int pos = y*width + x;
				double Rc = 0.299f  *pixelsRed[pos] + 0.587f *pixelsGreen[pos] + 0.114f  *pixelsBlue[pos]; // Y
				double Gc = 0.5957f *pixelsRed[pos] - 0.2744f*pixelsGreen[pos] - 0.3212f *pixelsBlue[pos]; // I
				double Bc = 0.2114f *pixelsRed[pos] - 0.5226f*pixelsGreen[pos] + 0.3111f *pixelsBlue[pos]; // Q
				double RcInic = Rc, GcInic = Gc, BcInic = Bc;

				iters = 0;
				do {
					xcOld = xc;
					ycOld = yc;
					YcOld = Rc;
					IcOld = Gc;
					QcOld = Bc;

					double mx = 0;
					double my = 0;
					double mY = 0;
					double mI = 0;
					double mQ = 0;
					int num=0;

					for (int ry=-rad; ry <= rad; ry++) {
						int y2 = yc + ry; 
						if (y2 >= 0 && y2 < height) {
							for (int rx=-rad; rx <= rad; rx++) {
								int x2 = xc + rx; 
								if (x2 >= 0 && x2 < width) {
									if (ry*ry + rx*rx <= rad2) {
										double Y2 = RcInic;
										double I2 = GcInic;
										double Q2 = BcInic;

										double dY = Rc - Y2;
										double dI = Gc - I2;
										double dQ = Bc - Q2;

										if (dY*dY+dI*dI+dQ*dQ <= colorDist2) {
											mx += x2;
											my += y2;
											mY += Y2;
											mI += I2;
											mQ += Q2;
											num++;
										}
									}
								}
							}
						}
					}
					double num_ = 1f/num;
					Rc = mY*num_;
					Gc = mI*num_;
					Bc = mQ*num_;
					xc = (int) (mx*num_+0.5);
					yc = (int) (my*num_+0.5);
					int dx = xc-xcOld;
					int dy = yc-ycOld;
					double dY = Rc-YcOld;
					double dI = Gc-IcOld;
					double dQ = Bc-QcOld;

					shift = dx*dx+dy*dy+dY*dY+dI*dI+dQ*dQ; 
					iters++;
				}
				while (shift > 3 && iters < 100);

				double r_ = (Rc + 0.9563f*Gc + 0.6210f*Bc);
				double g_ = (Rc - 0.2721f*Gc - 0.6473f*Bc);
				double b_ = (Rc - 1.1070f*Gc + 1.7046f*Bc);

				pixelsResultR[pos] = r_;
				pixelsResultG[pos] = g_;
				pixelsResultB[pos] = b_;
			}

		}
		result.setAllPixelsRed(pixelsResultR);
		result.setAllPixelsGreen(pixelsResultG);
		result.setAllPixelsBlue(pixelsResultB);
		return result;
	}
	
	/**
	 * Method which calculates the meanshift function for a gray image.
	 * @param img The input image.
	 * @param rad The spatial radius
	 * @param colorDist The color distance.
	 * @return The result to apply the meanshift function.
	 * @throws JIPException
	 */
	private JIPImage meanshift (JIPImgBitmap img, int rad, int colorDist) throws JIPException {
		int width = img.getWidth();
		int height = img.getHeight();
		int colorDist2 = colorDist*colorDist, rad2 = rad*rad;
		JIPImgBitmap result = (JIPImgBitmap)JIPImage.newImage(img.getWidth(), img.getHeight(), img.getType());
		double[] pixelsf = img.getAllPixels();
		double[] pixelsResult = new double[width*height];

		double shift = 0;
		int iters = 0;
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {

				int xc = x;
				int yc = y;
				int xcOld, ycOld;
				double YcOld;
				int pos = y*width + x;
				double Yc = pixelsf[pos];

				iters = 0;
				do {
					xcOld = xc;
					ycOld = yc;
					YcOld = Yc;

					double mx = 0;
					double my = 0;
					double mY = 0;
					int num=0;

					for (int ry=-rad; ry <= rad; ry++) {
						int y2 = yc + ry; 
						if (y2 >= 0 && y2 < height) {
							for (int rx=-rad; rx <= rad; rx++) {
								int x2 = xc + rx; 
								if (x2 >= 0 && x2 < width) {
									if (ry*ry + rx*rx <= rad2) {

										double Y2 = pixelsf[y2*width + x2];

										double dY = Yc - Y2;

										if (dY*dY <= colorDist2) {
											mx += x2;
											my += y2;
											mY += Y2;
											num++;
										}
									}
								}
							}
						}
					}
					double num_ = 1f/num;
					Yc = mY*num_;
					xc = (int) (mx*num_+0.5);
					yc = (int) (my*num_+0.5);
					int dx = xc-xcOld;
					int dy = yc-ycOld;
					double dY = Yc-YcOld;

					shift = dx*dx+dy*dy+dY*dY; 
					iters++;
				}
				while (shift > 3 && iters < 100);

				pixelsResult[pos] = Yc;
			}
		}
		result.setAllPixels(pixelsResult);
		return result;
	}
}

