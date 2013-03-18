package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It converts a gray scale image into another gray scale image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a gray scale image (BYTE, BIT, SHORT or 
 * FLOAT).</li>
 * <li><em>gray</em>: List which indicates the image type result (BYTE, BIT, SHORT, 
 * FLOAT) (default BYTE).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same image with different gray scale.</li>
 * </ul><br />
 */
public class GrayToGray extends Function2D {
	private static final long serialVersionUID = -8983716229889971105L;

	public GrayToGray() {
		super();
        name = "GrayToGray";
        description = "Converts a gray image into another gray image.";
        groupFunc = FunctionGroup.Transform;

        ParamList p1 = new ParamList("gray", false, true);
        String []paux = new String[4];
        paux[0] = "BYTE";
        paux[1] = "BIT";
        paux[2] = "SHORT";
        paux[3] = "FLOAT";
        p1.setDefault(paux);
        p1.setDescription("Type of gray for the result image");

		addParam(p1);
    }
    
    public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.COLOR || img instanceof JIPImgGeometric) 
			throw new JIPException("Function GrayToGray can not be applied to this image format.");
		
		String p1 = getParamValueString("gray");
		ImageType resType = Enum.valueOf(ImageType.class,p1);
		JIPImgBitmap imgBmp = (JIPImgBitmap)img;
		int nbands = imgBmp.getNumBands();
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(nbands,img.getWidth(),img.getHeight(),resType);
		double []bmp, bmpRes;
		double maxInput = 0, maxOutput = 0;

		switch (img.getType()) {
			case BYTE: maxInput = 255; break;
			case FLOAT:
			case BIT: maxInput = 1; break;
			case SHORT: maxInput = 65535; break;
		}
		switch (resType) {
			case BYTE: maxOutput = 255; break;
			case FLOAT:
			case BIT: maxOutput = 1; break;
			case SHORT: maxOutput = 65535; break;
		}
		for (int b=0; b < nbands; b++) {
			bmp = imgBmp.getAllPixels(b);
			bmpRes = new double[bmp.length];
			for (int i=0; i < bmp.length; i++) 
				bmpRes[i] = maxOutput*bmp[i]/maxInput;
			res.setAllPixels(b, bmpRes);
		}

		return res;
	}
}

