package javavis.jip2d.functions;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.Parameter;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.Point2D;

/**
 * It adds a new geometric point, with the coordinates indicated as a parameter, in 
 * a POINT type image.<br />
 * It applies to POINT type image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a POINT type image.</li>
 * <li><em>x</em>: Integer value which indicates the X coordinate of the new point (default 0).</li>
 * <li><em>y</em>: Integer value which indicates the Y coordinate of the new point (default 0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The original POINT image with the new point.</li>
 * </ul><br />
 */
public class AddPoint extends Function2D {
	private static final long serialVersionUID = 667525781927539405L;

	public AddPoint() {
		super();
		name = "AddPoint";
		description = "Adds a point in a POINT type image.";
		groupFunc = FunctionGroup.Geometry;

		Parameter p1 = new ParamInt("x", true, true);
		p1.setDescription("X origin");
		
		Parameter p2 = new ParamInt("y", true, true);
		p2.setDescription("Y origin");

		addParam(p1);
		addParam(p2);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int x = getParamValueInt("x"); 
		int y = getParamValueInt("y"); 
		int width = img.getWidth();
		int height = img.getHeight();

		if (img.getType() != ImageType.POINT)
			throw new JIPException("The image must be POINT type.");
		
		if (x < 0 || y < 0 || x > width - 1 || y > height - 1) 
			throw new JIPException("Dimensions exceed image size.");
		
		Point2D p = new Point2D(x, y);
		((JIPGeomPoint)img).addPoint(p);
		
		return img.clone();
	}
}

