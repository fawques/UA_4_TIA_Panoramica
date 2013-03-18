package javavis.jip2d.functions.adaBoost;

import java.io.Serializable;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.functions.ColorToGray;
import javavis.jip2d.functions.IntegralImage;

/**
 * holds one train example. It will be an image and its type: face(1) or non-face(0). Image is converted to integralImage
 * @author dviejo
 *
 */
public class trainExample implements Serializable
{
	private static final long serialVersionUID = 8509329710834494730L;
	public int type;
	public JIPBmpFloat integralImage;
	
	public trainExample()
	{
		integralImage = null;
	}
	
	public trainExample(int t, String filePath) throws JIPException
	{
		type = t;
		JIPBmpColor input = (JIPBmpColor)JIPToolkit.getColorImage(JIPToolkit.getAWTImage(filePath));
		ColorToGray fcg = new ColorToGray();
		fcg.setParamValue("gray", "BYTE");
		IntegralImage fii = new IntegralImage();
		
		integralImage = (JIPBmpFloat)fii.processImg(fcg.processImg(input));
	}
}
