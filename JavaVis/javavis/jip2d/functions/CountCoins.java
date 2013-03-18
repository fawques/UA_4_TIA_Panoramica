package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.geometrics.JIPGeomPoly;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.functions.Binarize;
import javavis.jip2d.functions.Canny;
import javavis.jip2d.functions.GrayToGray;
import javavis.jip2d.functions.HoughCirc;
import javavis.jip2d.functions.RGBToColor;
import javavis.jip2d.functions.SegmentHSB;
import javavis.jip2d.util.Circumference;

/**
 * It counts the number of coins in an image. For do it, first of all the application uses 
 * Canny function to find edges in the original image. Then, this image is changed to gray 
 * scale with ColorToGray function (in BYTE type) and after it is changed to binary with
 * Binarize function. Next, we have to use RGBToColor (in HSB type) for converting the image
 * into HSB image. The reason is that we need to use SegmentHSB function for segmenting a HSB
 * image in order to get the coins pixels. After that, we use HoughCirc function, which 
 * detects circumferences that have some restrictions and we obtain the Hough transform of
 * the binary image. Finally, CountCoins has to decide that circumferences are valid. It 
 * finds which is the circumference that the pixels are marked with 1. A circumference will 
 * be coin if the number of pixels in it is mare than 40 per cent of its area.<br />
 * It applies to COLOR images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR image.</li>
 * <li><em>thres</em>: Integer value which indicates the minimum percentage of votes which 
 * a circumference has to own to be accepted. It will be admitted when the number of 
 * votes is more than or equal to '(threshold/100) * 2 * PI * r' (default 25). </li>
 * <li><em>Rmin</em>: Integer value which indicates the minimum radius we allow to owner 
 * circumference (default 10).</li>
 * <li><em>Rmax</em>: Integer value which indicates the maximum radius we allow to owner 
 * circumference (default 80).</li>
 * <li><em>h</em>: Real value which indicates the hue value to segment (default 0.17).</li>
 * <li><em>herror</em>: Real value which indicates the error admitted in hue band 
 * (default 0.03).</li>
 * <li><em>s</em>: Real value which indicates the saturation value to segment (default 0.35).</li>
 * <li><em>serror</em>: Real value which indicates the error admitted in saturation band 
 * (default 0.1).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A processed image, with the circumferences which have been recognized as coin.</li>
 * </ul><br />
 */
public class CountCoins extends Function2D {
	private static final long serialVersionUID = -5337180104629350329L;

	public CountCoins() {
		super();
		name = "CountCoins";
		description = "Application which counts the number of coins in an image. Applies to COLOR images.";
		groupFunc = FunctionGroup.Applic;
		
		ParamFloat p1 = new ParamFloat("h", false, true);
		p1.setDefault(0.17f);
		p1.setDescription("Value of Hue");
		
		ParamFloat p2 = new ParamFloat("herror", false, true);
		p2.setDefault(0.03f);
		p2.setDescription("Value of Hue error");
		
		ParamFloat p3 = new ParamFloat("s", false, true);
		p3.setDefault(0.35f);
		p3.setDescription("Value of Saturation");
		
		ParamFloat p4 = new ParamFloat("serror", false, true);
		p4.setDefault(0.1f);
		p4.setDescription("Value of Saturation error");
		
		ParamInt p5 = new ParamInt("thres", false, true);
		p5.setDefault(25);
		p5.setDescription("Minimum percentage of votes");
		
		ParamInt p6 = new ParamInt("Rmin", false, true);
		p6.setDefault(10);
		p6.setDescription("Minimum radius");
		
		ParamInt p7 = new ParamInt("Rmax", false, true);
		p7.setDefault(80);
		p7.setDescription("Maximum radius");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.COLOR) 
			throw new JIPException("Function CountCoins only applied to COLOR images.");
			
		int threshold = getParamValueInt("thres");
		int rmin = getParamValueInt("Rmin");
		int rmax = getParamValueInt("Rmax");
		int width = img.getWidth();
		int height = img.getHeight();

		/* Applies Canny method */
		Function2D canny = new Canny();
		canny.setParamValue("brightness", 200);
		JIPImage res = canny.processImg(img);

		/* Converts the image in Byte type.*/
		Function2D gray = new GrayToGray();
		gray.setParamValue("gray", "BYTE");
		JIPImage aux = gray.processImg(res);

		/* Binarizes.*/
		Function2D binarize = new Binarize();
		binarize.setParamValue("u1", 110);
		binarize.setParamValue("u2", 255);
		res = binarize.processImg(aux);

		/* Converts from RGB to HSB.*/
		Function2D rgbToH  = new RGBToColor();
		rgbToH.setParamValue("format", "HSB");
		aux = rgbToH.processImg(img);

		/* Segments the HSB image in order to get the coins pixels.*/
		Function2D segment = new SegmentHSB();
		segment.setParamValue("h", getParamValueFloat("h"));
		segment.setParamValue("herror", getParamValueFloat("herror"));
		segment.setParamValue("s", getParamValueFloat("s"));
		segment.setParamValue("serror", getParamValueFloat("serror"));
		JIPBmpBit hsbbin = (JIPBmpBit)segment.processImg(aux);

		/* Obtains the Hough transform of the binary image*/
		HoughCirc hough = new HoughCirc();
		hough.setParamValue("thres", threshold);
		hough.setParamValue("Rmin", rmin);
		hough.setParamValue("Rmax", rmax);
		hough.processImg(res);

		int numCirc = hough.getParamValueInt("ncirc");

		/* Array with votes from pixels in the image (a pixel votes for a
		 * circumference if it is included in it.
		 */
		int myArray[] = new int[numCirc];
		for (int i=0; i < numCirc; i++)
			myArray[i] = 0;
		ArrayList<Circumference> vecCirc = (ArrayList<Circumference>)hough.getParamValueObj("circum");
		for (int x=0; x < width; x++)
			for (int y=0; y < height; y++)
				if (hsbbin.getPixelBool(x, y))
					for (int count=0; count<numCirc; count++)
						if (belongTo(x, y, (Circumference) vecCirc.get(count)))
							myArray[count]++;

		int coins = 0;
		ArrayList<Polygon2D> points_of_circ = new ArrayList<Polygon2D>();
		/* We accept circumferences with a number of votes more than 40 percent.*/
		for (int i=0; i<numCirc; i++) {
			Circumference c2 = (Circumference) vecCirc.get(i);
			if (myArray[i] > (0.5 * Math.PI * c2.radius * c2.radius)) {
				points_of_circ.add(Circumference.getPoints((Circumference) vecCirc.get(i)));
				coins++;
			}
		}

		info = "Number of Coins found: " + coins;
		JIPGeomPoly finalRes = new JIPGeomPoly(width, height);
		finalRes.setData(points_of_circ);
		
		return finalRes;
	}

	
	/**
	 * Method which decides if a point (x,y) belongs to a circumference.
	 * @param x The x coordinate of the study point.
	 * @param y The y coordinate of the study point.
	 * @param circ Circumference where we check if the received point is included.
	 * @return A boolean value indicating true if the point belongs to circumference and 
	 * false in otherwise.
	 */
	public boolean belongTo(int x, int y, Circumference circ) {
		int c = x - circ.centerX;
		int d = y - circ.centerY;
		int r = (int) Math.sqrt(c * c + d * d);
		
		if (r <= circ.radius) return true;
		else return false;
	}
}

