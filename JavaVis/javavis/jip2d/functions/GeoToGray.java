package javavis.jip2d.functions;

import java.util.ArrayList;
import java.util.Arrays;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It converts the geometric elements of a geometric image in a gray scale bitmap. It uses 
 * GrayToGray method.
 * It applies to POINT, SEGMENT, POLY or EDGES type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a POINT, SEGMENT, POLY or EDGES type.</li>
 * <li><em>gray</em>: List which contains the image type result (BYTE, BIT, SHORT, FLOAT) (default BYTE).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Image in gray scale.</li>
 * </ul><br />
 */
public class GeoToGray extends Function2D {
	private static final long serialVersionUID = -4763689691943809535L;

	public GeoToGray() {
		super();
		name = "GeoToGray";
		description = "Converts a geometric image into a gray scale. Applies to POINT, SEGMENT, POLY or EDGES type.";
		groupFunc = FunctionGroup.Geometry;

		ParamList p1 = new ParamList("gray", false, true);
		String []paux = new String[4];
		paux[0] = "BYTE";
		paux[1] = "BIT";
		paux[2] = "SHORT";
		paux[3] = "FLOAT";
		p1.setDefault(paux);
		p1.setDescription("Type of gray");

		addParam(p1);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();
		if (!(img instanceof JIPImgGeometric)) 
			throw new JIPException("Function GeoToGray can only be applied to a geometric image.");

		int width = img.getWidth();
		int height = img.getHeight();
		int totalPix = width*height;
		JIPImgBitmap res = null;
		String gray = getParamValueString("gray");

		double pix[] = new double[totalPix];
		Arrays.fill(pix,0);
		JIPImgGeometric imgGeom = (JIPImgGeometric)img;
		if (t == ImageType.SEGMENT) {
			ArrayList<Segment> aux = (ArrayList<Segment>)imgGeom.getData();
			for (Segment s : aux) {
				drawline(s.getBegin().getX(), s.getBegin().getY(), s.getEnd().getX(), s.getEnd().getY(), width, height, pix);
			}
		} else if (t == ImageType.POINT) {
			ArrayList<Point2D> aux = (ArrayList<Point2D>)imgGeom.getData();
			for (Point2D p : aux) {
				drawline(p.getX(), p.getY(), p.getX(), p.getY(), width, height, pix);
			}
		} else if (t == ImageType.POLY) {
			ArrayList<Polygon2D> aux = (ArrayList<Polygon2D>)imgGeom.getData();
			ArrayList<Point2D> points;
			for (Polygon2D pol : aux) {	
				points = (ArrayList<Point2D>)pol.getData();
				Point2D current=points.get(0), next;
				for (int i=1; i < points.size(); i++) {
					next = points.get(i);
					drawline(current.getX(), current.getY(), next.getX(), next.getY(), width, height, pix);
					current = next;
				}
				next = points.get(0);
				drawline(current.getX(), current.getY(), next.getX(), next.getY(), width, height, pix);
			}
		} else if (t == ImageType.EDGES) {
			ArrayList<Edge> aux = (ArrayList<Edge>)imgGeom.getData();
			ArrayList<Point2D> points;
			for (Edge edg : aux) {
				points = (ArrayList<Point2D>)edg.getData();
				for (Point2D p : points) {
					drawline(p.getX(), p.getY(), p.getX(), p.getY(), width, height, pix);
				}
			}
		} 
		res = new JIPBmpFloat(width, height);
		res.setAllPixels(pix);

		Function2D conversion = new GrayToGray();
		conversion.setParamValue("gray", gray);
		
		return conversion.processImg(res);
	}
	
	
	/** 
	 * Method which draws the line which coordinate is transfered by parameter,
	 * by means of Bressenham algorithm.
	 * @param x0 X coordinate of initial point.
	 * @param y0 Y coordinate of inicial point.
	 * @param x1 X coordinate of the last point.
	 * @param y1 Y coordinate of the last point.
	 * @param icol Column.
	 * @param irow Row.
	 * @param pix Array of pixels.
	 */
	private static void drawline(int x0, int y0, int x1, int y1, int icol, int irow, double pix[]) {
		int xmin, xmax; /* Coordinates of the line */
		int ymin, ymax;
		int dir; /* Search direction */
		int dx, dy;

		/* Increments East, North-East, South, South-East, North */
		int incrE, incrNE, incrSE;
		int d, x, y;
		int mpCase, done;
		
		xmin = x0;
		xmax = x1;
		ymin = y0;
		ymax = y1;

		dx = xmax - xmin;
		dy = ymax - ymin;

		if (dx * dx > dy * dy) /* horizontal search */ {
			dir = 0;
			if (xmax < xmin) {
				xmin ^= xmax;
				xmax ^= xmin;
				xmin ^= xmax;
				ymin ^= ymax;
				ymax ^= ymin;
				ymin ^= ymax;
			}
			dx = xmax - xmin;
			dy = ymax - ymin;

			if (dy >= 0) {
				mpCase = 1;
				d = 2 * dy - dx;
			} else {
				mpCase = 2;
				d = 2 * dy + dx;
			}

			incrNE = 2 * (dy - dx);
			incrE = 2 * dy;
			incrSE = 2 * (dy + dx);
		} else { /* vertical search */
			dir = 1;
			if (ymax < ymin) {
				xmin ^= xmax;
				xmax ^= xmin;
				xmin ^= xmax;
				ymin ^= ymax;
				ymax ^= ymin;
				ymin ^= ymax;
			}
			dx = xmax - xmin;
			dy = ymax - ymin;

			if (dx >= 0) {
				mpCase = 1;
				d = 2 * dx - dy;
			} else {
				mpCase = 2;
				d = 2 * dx + dy;
			}

			incrNE = 2 * (dx - dy);
			incrE = 2 * dx;
			incrSE = 2 * (dx + dy);
		}

		/* Start the search */
		x = xmin;
		y = ymin;
		done = 0;

		while (done == 0) {
			if (x > 0 && x < icol && y > 0 && y < irow)
				pix[y * icol + x] = 1.0f;

			/* Move to the next p */
			switch (dir) {
				case 0 : /* horizontal */ {
						if (x < xmax) {
							switch (mpCase) {
								case 1 :
									if (d <= 0) {
										d += incrE;
										x++;
									} else {
										d += incrNE;
										x++;
										y++;
									}
									break;

								case 2 :
									if (d <= 0) {
										d += incrSE;
										x++;
										y--;
									} else {
										d += incrE;
										x++;
									}
									break;
							}
						} else
							done = 1;
					}
					break;

				case 1 : /* vertical */ {
						if (y < ymax) {
							switch (mpCase) {
								case 1 :
									if (d <= 0) {
										d += incrE;
										y++;
									} else {
										d += incrNE;
										y++;
										x++;
									}
									break;

								case 2 :
									if (d <= 0) {
										d += incrSE;
										y++;
										x--;
									} else {
										d += incrE;
										y++;
									}
									break;
							}
						} else
							done = 1;
					}
					break;
			}
		}
	}
}

