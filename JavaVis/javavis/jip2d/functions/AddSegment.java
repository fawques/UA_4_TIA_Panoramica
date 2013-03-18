package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.Parameter;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It adds a new geometric segment, with the coordinates indicated as a parameter, in 
 * a SEGMENT type image.<br />
 * It applies to SEGMENT type image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a SEGMENT type image.</li>
 * <li><em>x0</em>: Integer value which indicates the X origin coordinate of the new point 
 * (default 0).</li>
 * <li><em>y0</em>: Integer value which indicates the Y origin coordinate of the new point 
 * (default 0).</li>
 * <li><em>x1</em>: Integer value which indicates the X destination coordinate of the new point 
 * (default 0).</li>
 * <li><em>y1</em>: Integer value which indicates the Y destination coordinate of the new point 
 * (default 0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The original SEGMENT image with the new segment.</li>
 * </ul><br />
 */
public class AddSegment extends Function2D {
	private static final long serialVersionUID = 4303209767641719076L;

	public AddSegment() {
		super();
		name = "AddSegment";
		description = "Adds a segment in a SEGMENT type image.";
		groupFunc = FunctionGroup.Geometry;

		Parameter p1 = new ParamInt("x0", true, true);
		p1.setDescription("X origin");
		
		Parameter p2 = new ParamInt("y0", true, true);
		p2.setDescription("Y origin");
		
		Parameter p3 = new ParamInt("x1", true, true);
		p3.setDescription("X destiny");
		
		Parameter p4 = new ParamInt("y1", true, true);
		p4.setDescription("Y destiny");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int x0 = getParamValueInt("x0");
		int y0 = getParamValueInt("y0");
		int x1 = getParamValueInt("x1");
		int y1 = getParamValueInt("y1");
		int width = img.getWidth();
		int height = img.getHeight();
		
		if (img.getType() != ImageType.SEGMENT) 
			throw new JIPException("The image must be SEGMENT type.");
		
		if (x0 < 0 || y0 < 0 || x1 < 0 || y1 < 0 || x0 > width - 1 || y0 > height - 1
			|| x1 > width - 1 || y1 > height - 1) 
			throw new JIPException("Dimensions exceed image size.");
		
		Point2D begin = new Point2D(x0, y0);
		Point2D end = new Point2D(x1, y1);
		Segment s = new Segment(begin, end);
		((JIPGeomSegment)img).addSegment(s);
		
		return img;
	}
}

