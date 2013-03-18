package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It converts a COLOR image into a BYTE, BIT, SHORT or FLOAT image.<br />
 * It is only applied to COLOR type.<br/><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR type.</li>
 * <li><em>gray</em>: List which indicates the image type result (BYTE, BIT, 
 * SHORT, FLOAT) (default BYTE).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>An image equivalent to the COLOR type but in gray scale.</li>
 * </ul><br />
 */
public class ColorToGray extends Function2D {
	private static final long serialVersionUID = -3289373731488957402L;

	public ColorToGray() {
		super();
		name = "ColorToGray";
		description = "It converts a COLOR image into gray scale.";
		groupFunc = FunctionGroup.Transform;

		ParamList p1 = new ParamList("gray", false, true);
		String []paux = new String[4];
		paux[0] = "BYTE";
		paux[1] = "BIT";
		paux[2] = "SHORT";
		paux[3] = "FLOAT";
		p1.setDefault(paux);
		p1.setDescription("Type of result image");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPImgBitmap res = null;
		String grayType = getParamValueString("gray");
		ImageType resType = ImageType.BYTE;
		
		if (grayType.equals("BIT"))
			resType = ImageType.BIT;
		else if (grayType.equals("BYTE"))
			resType = ImageType.BYTE;
		else if (grayType.equals("SHORT"))
			resType = ImageType.SHORT;
		else if (grayType.equals("FLOAT"))
			resType = ImageType.FLOAT;
		
		if (img.getType() == ImageType.COLOR) {
			int totalPix = img.getWidth()*img.getHeight();
			JIPBmpColor imgCol = (JIPBmpColor)img;
			byte[] red = imgCol.getAllPixelsByteRed();
			byte[] green = imgCol.getAllPixelsByteGreen();
			byte[] blue = imgCol.getAllPixelsByteBlue();
			double[] gray = new double[totalPix];
			double max = 0;

			switch (resType) {
				case BYTE: max = 255; break;
				case FLOAT:
				case BIT: max = 1; break;
				case SHORT: max = 65535; break;
			}
			for (int i=0; i < totalPix; i++)
			  gray[i] = max*Math.round(0.299 * (red[i]&0xFF) + 0.587 * (green[i]&0xFF) + 0.114 * 
					  (blue[i]&0xFF))/255.0;

			res = (JIPImgBitmap)JIPImage.newImage(img.getWidth(), img.getHeight(), resType);
			res.setAllPixels(gray);
		}
		else
			throw new JIPException("Function ColorToGray only applied to COLOR images.");
		
		return res;
	}
}

