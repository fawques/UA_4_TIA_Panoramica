package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It calculates the intersection points of the segments in the input image.<br />
 * It applies to SEGMENT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a SEGMENT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A POINT image which represents intersection points.</li>
 * </ul><br />
 */
public class InterSegment extends Function2D {
	private static final long serialVersionUID = 5471936099222093701L;
	
	public InterSegment() {
		super();
		name = "InterSegment";
		description = "Calculates segment intersection. Applies to SEGMENT type.";
		groupFunc = FunctionGroup.Geometry;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int ax0, ay0, ax1, ay1, sx0, sy0, sx1, sy1;

		if (img.getType() != ImageType.SEGMENT) 
			throw new JIPException("Function InterSegment can not be applied to this image format.");
		
		JIPGeomSegment imgSeg = (JIPGeomSegment)img;

		ArrayList<Segment> segments = (ArrayList<Segment>)imgSeg.getData();
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		int segSize = segments.size();
		int[] dev = new int[2];
		
		for (int i=0; i < segSize-1; i++) {
			ax0 = segments.get(i).getBegin().getX();
			ay0 = segments.get(i).getBegin().getY();
			ax1 = segments.get(i).getEnd().getX();
			ay1 = segments.get(i).getEnd().getY();
			for (int j=i+1; j < segSize; j++) {
				sx0 = segments.get(j).getBegin().getX();
				sy0 = segments.get(j).getBegin().getY();
				sx1 = segments.get(j).getEnd().getX();
				sy1 = segments.get(j).getEnd().getY();
				if (intersect(ax0, ay0, ax1, ay1, sx0, sy0, sx1, sy1)) {
					cutPoint(ax0, ay0, ax1, ay1, sx0, sy0, sx1, sy1, dev);
					points.add(new Point2D(dev[0], dev[1]));
				}
			}
		}
		JIPGeomPoint res = new JIPGeomPoint(img.getWidth(), img.getHeight());
		res.setData(points);
		
		return res;
	}

	
	/**
	 * Method which calculate the overlap point between two segments. When we can use
	 * this method, we must check that the overlap point exist before.
	 * @param ax1 X coordinate of initial point of first segment.
	 * @param ay1 Y coordinate of initial point of first segment.
	 * @param bx1 X coordinate of final point of first segment.
	 * @param by1 Y coordinate of final point of first segment.
	 * @param cx1 X coordinate of initial point of second segment.
	 * @param cy1 Y coordinate of initial point of second segment.
	 * @param dx1 X coordinate of final point of second segment.
	 * @param dy1 Y coordinate of final point of second segment.
	 * @param dev Integer vector with a size of 2, with the result: dev[0](devx) puts in the 
	 * class variable the X coordinate of the point which belongs to the intersection; 
	 * dev[1](devy) puts in the class variable the Y coordinate of the point which belongs 
	 * to the intersection.
	 */
	private void cutPoint(int ax, int ay, int bx, int by, int cx, int cy,
		int dx, int dy, int[] dev) {
		double m1, n1, m2, n2, xx, yy;

		if ((ax - bx) == 0) {
			xx = ax;
			m2 = (cy - dy) / (double)(cx - dx);
			n2 = cy - ((cy - dy) / (double)(cx - dx)) * cx;
			yy = m2 * ax + n2;
		} else if ((cx - dx) == 0) {
			xx = cx;
			m1 = (ay - by) / (double)(ax - bx);
			n1 = ay - ((ay - by) / (double)(ax - bx)) * ax;
			yy = m1 * dx + n1;
		} else {
			m1 = (ay - by) / (double)(ax - bx);
			n1 = ay - ((ay - by) / (double)(ax - bx)) * ax;
			m2 = (cy - dy) / (double)(cx - dx);
			n2 = cy - ((cy - dy) / (double)(cx - dx)) * cx;

			xx = (n2 - n1) / (double)(m1 - m2);
			yy = m1 * xx + n1;
		}
		dev[0] = (int) xx;
		dev[1] = (int) yy;
	}

	/**
	 * Method which shows us if a point is on the left of two points.
	 * @param ax X coordinate of point number 1.
	 * @param ay Y coordinate of point number 1.
	 * @param bx X coordinate of point number 2.
	 * @param by Y coordinate of point number 2.
	 * @param cx X coordinate of point number 3.
	 * @param cy Y coordinate of point number 3.
	 * @return A boolean indicating true if point is on the left, false in otherwise.
	 */
	private boolean left(int ax, int ay, int bx, int by, int cx, int cy) {
		return area2(ax, ay, bx, by, cx, cy) > 0;
	}

	/**
	 * Method which calculate if two segments intersects.
	 * @param ax0 X coordinate of initial point of first segment.
	 * @param ay0 Y coordinate of initial point of first segment.
	 * @param ax1 X coordinate of final point of first segment.
	 * @param ay1 Y coordinate of final point of first segment.
	 * @param sx0 X coordinate of initial point of second segment.
	 * @param sy0 Y coordinate of initial point of second segment.
	 * @param sx1 X coordinate of final point of second segment.
	 * @param sy1 Y coordinate of final point of second segment.
	 * @return A boolean indicating true if there is same intersection, false in otherwise.
	 */
	private boolean intersect(int ax0, int ay0, int ax1, int ay1,
		int sx0, int sy0, int sx1, int sy1) {

		if (colinear(ax0, ay0, ax1, ay1, sx0, sy0) || colinear(ax0, ay0, ax1, ay1, sx1, sy1)
			|| colinear(sx0, sy0, sx1, sy1, ax0, ay0) || colinear(sx0, sy0, sx1, sy1, ax1, ay1))
			return false;

		return xor(left(ax0, ay0, ax1, ay1, sx0, sy0), left(ax0, ay0, ax1, ay1, sx1, sy1))
			&& xor(left(sx0, sy0, sx1, sy1, ax0, ay0), left(sx0, sy0, sx1, sy1, ax1, ay1));
	}

	/**
	 * Method which make a XOR function between two booleans.
	 * @param a The first boolean.
	 * @param b The second boolean.
	 * @return A boolean indicating true or false depending on the result of XOR function.
	 */
	private boolean xor(boolean a, boolean b) {
		return (!a && b) || (a && !b);
	}

	/**
	 * Method which calculate if three point are colinear.
	 * @param p1x X coordinate of the first point.
	 * @param p1y Y coordinate of the first point.
	 * @param p2x X coordinate of the second point.
	 * @param p2y Y coordinate of the second point.
	 * @param p3x X coordinate of the third point.
	 * @param p3y Y coordinate of the third point.
	 * @return A boolean true if the points are colinear, false in otherwise.
	 */
	private boolean colinear(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
		return area2(p1x, p1y, p2x, p2y, p3x, p3y) == 0;
	}

	/**
	 * Method which calculates the area (with sign) of the triangle formed by 3
	 * points.
	 * @param ax X coordinate of the first point.
	 * @param ay Y coordinate of the first point.
	 * @param bx X coordinate of the second point.
	 * @param by Y coordinate of the second point.
	 * @param cx X coordinate of the third point.
	 * @param cy Y coordinate of the third point.
	 * @return The area of the triangle.
	 */ 
	private int area2(int ax, int ay, int bx, int by, int cx, int cy) {
		return ax * by - ay * bx + ay * cx - ax * cy + bx * cy - cx * by;
	}
}

