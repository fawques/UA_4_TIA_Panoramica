package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It skews the image, using an angle and one of its sides.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>angle</em>: Integer value which indicates the swoop angle, expressed in degrees.
 * Positive corresponds to swoop to the right. Range between -89 and 89 (default 15).</li>
 * <li><em>clipping</em>: Boolean value which indicates if it is necessary to clip the image 
 * in order to keep the original image dimensions. If it is checked, the output image will 
 * have the same size of the input image (default unchecked).</li>
 * <li><em>side</em>: String value which indicates the side to swoop (BOTTOM, UP, LEFT, RIGHT) 
 * (default BOTTOM).</li>
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same type that the input image.</li>
 * </ul><br />
 */
public class Skew extends Function2D {
	private static final long serialVersionUID = -2431379428816308525L;

	public Skew() {
		super();
		name = "Skew";
		description = "Skews the image, using an angle and one of its sides. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("angle", false, true);
		p1.setDefault(15);
		p1.setDescription("Swooping angle. <0 swoops on left, >0 on right.");

		ParamBool p2 = new ParamBool("clipping", false, true);
		p2.setDefault(false);
		p2.setDescription("Mantains the original dimensions.");
		
		ParamList p3 = new ParamList("side", false, true);
		String []paux = new String[4];
		paux[0] = "BOTTOM";
		paux[1] = "UP";
		paux[2] = "LEFT";
		paux[3] = "RIGHT";
		p3.setDefault(paux);
		p3.setDescription("Side to apply the skew");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();		
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Skew can not be applied to this image format.");

		JIPImgBitmap res = null;
		int angle = getParamValueInt("angle");
		if (Math.abs(angle) > 89 || Math.abs(angle) < -89) 
			throw new JIPException("Angle is incorrect.");

		boolean clipping = getParamValueBool("clipping");
		boolean isBOTTOM = false, isLEFT = false, isRIGHT = false, isUP = false;
		String side = getParamValueString("side");
		
		if (side.equals("BOTTOM")) isBOTTOM = true;
		if (side.equals("LEFT")) isLEFT = true;
		if (side.equals("RIGHT")) isRIGHT = true;
		if (side.equals("UP")) isUP = true;
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;

		int width = img.getWidth();
		int height = img.getHeight();
		int numBands = imgBmp.getNumBands();
		double ang = Math.toRadians(Math.abs(angle));
		double angleSin = Math.sin(ang);
		int nSize = 0;
		
		if (isBOTTOM || isUP) {
			nSize = (int) (width + height * angleSin);
			if (!clipping) res = (JIPImgBitmap)JIPImage.newImage(numBands, nSize, height, t);
			else res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, t);
		}
		else {
			nSize = (int) (height + width * angleSin);
			if (!clipping) res = (JIPImgBitmap)JIPImage.newImage(numBands, width, nSize, t);
			else res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, t);
		}
		
		double[] bmpflo, binflo;
		for (int nb=0; nb < numBands; nb++) {
			bmpflo = imgBmp.getAllPixels(nb);
			binflo = new double[nSize * height];
			int pos=0;
			for (int y=0; y < height; y++)
				for (int x=0; x < width; x++) {
					if (angle > 0) {
						if (isBOTTOM) pos = (int) (x + (height - y) * angleSin) + (y * nSize);
						if (isLEFT) pos = (int) (y + (width - (width - x)) * angleSin) * width + x;
						if (isRIGHT) pos = (int) (y + (width - x) * angleSin) * width + x;
						if (isUP) pos = (int) (x + (height - (height - y)) * angleSin) + (y * nSize);
					}
					else {
						if (isBOTTOM) pos = (int) (x + (height - (height - y)) * angleSin) + (y * nSize);
						if (isLEFT) pos = (int) (y + (width - x) * angleSin) * width + x;
						if (isRIGHT) pos = (int) (y + (width - (width - x)) * angleSin) * width + x;
						if (isUP) pos = (int) (x + (height - y) * angleSin) + (y * nSize);
					}
					binflo[pos] = bmpflo[x + y * width];
				}
			if (clipping) {
				double[] auxflo = new double[width * height];
				for (int x=0; x < width; x++)
					for (int y=0; y < height; y++) {
						if (angle > 0) {
							auxflo[x + y * width] = binflo[x + y * nSize];
						}
						else {
							if (isBOTTOM || isUP) 
								auxflo[x + y * width] = binflo[(int) (x + height * angleSin) + (y * nSize)];
							if (isLEFT || isRIGHT) 
								auxflo[x + y * width] = binflo[(int) (y + width * angleSin) * width + x];
						}
					}
				res.setAllPixels(nb, auxflo);
			} 
			else {
				res.setAllPixels(nb, binflo);
			}
		}
		return res;
	}
}

