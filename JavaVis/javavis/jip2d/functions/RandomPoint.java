package javavis.jip2d.functions;

import java.util.Random;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.Point2D;

/**
 * It adds a number of new points with random coordinates.<br />
 * It applies to POINT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a POINT type image.</li>
 * <li><em>number</em>: Integer value which indicates the number of points to generate (default 100).
 * </li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Original POINT image with new points.</li>
 * </ul><br />
 */
public class RandomPoint extends Function2D {
	private static final long serialVersionUID = 5093291346461720543L;

	public RandomPoint() {
		super();
		name = "RandomPoint";
		description = "Adds some random points. Applies to POINT type.";
		groupFunc = FunctionGroup.Geometry;

		ParamInt p1 = new ParamInt("number", false, true);
		p1.setDescription("Number of points to add");
		p1.setDefault(100);

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int num = getParamValueInt("number");
		int width = img.getWidth();
		int height = img.getHeight();
		
		if (img.getType() != ImageType.POINT)
			throw new JIPException("Function RandomPoint can not be applied to this image format.");
		
		if (num <= 0) 
			throw new JIPException("Number of points incorrect.");

		Random rnd = new Random();
		int x0, y0;
		
		for (int i=0; i < num; i++) {
			x0 = Math.abs(rnd.nextInt() % width);
			y0 = Math.abs(rnd.nextInt() % height);
			((JIPGeomPoint)img).addPoint(new Point2D(x0, y0));
		}

		return img.clone();
	}
}

