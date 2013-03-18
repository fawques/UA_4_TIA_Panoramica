package javavis.jip2d.functions;

import java.util.Random;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It adds a number of new segments with random coordinates.<br />
 * It applies to SEGMENT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a SEGMENT type image.</li>
 * <li><em>number</em>: Integer value which indicates the number of segments to generate (default 
 * 100).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Original SEGMENT image with new segments.</li>
 * </ul><br />
 */
public class RandomSegment extends Function2D {
	private static final long serialVersionUID = 4452325343561072193L;

	public RandomSegment() {
		super();
		name = "RandomSegment";
		description = "Adds random segments. Applies to SEGMENT type.";
		groupFunc = FunctionGroup.Geometry;

		ParamInt p1 = new ParamInt("number", false, true);
		p1.setDescription("Number of segments to add");
		p1.setDefault(100);

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int num = getParamValueInt("number");
		int width = img.getWidth();
		int height = img.getHeight();
		
		if (img.getType() != ImageType.SEGMENT) 
			throw new JIPException("Function RandomSegment can not be applied to this image format.");
		
		if (num <= 0) 
			throw new JIPException("Number of segments incorrect.");

		Random rnd = new Random();
		
		for (int i=0; i < num; i++) {
			Point2D begin = new Point2D(Math.abs(rnd.nextInt() % width), Math.abs(rnd.nextInt() % height));
			Point2D end = new Point2D(Math.abs(rnd.nextInt() % width), Math.abs(rnd.nextInt() % height));
			((JIPGeomSegment)img).addSegment(new Segment(begin, end));
		}
		return img.clone();
	}
}

