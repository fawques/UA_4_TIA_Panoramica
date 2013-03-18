package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Junction;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It rotates an image with a given angle.<br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * <li><em>angle</em>: Integer value which indicates the rotation angle, expressed in degrees.
 * Positive corresponds clockwise, negative anticlockwise (default 30).</li>
 * <li><em>clipping</em>: Boolean value which indicates if it is necessary to clip the image 
 * in order to keep the original image dimensions. If it is checked, the output image will 
 * have the same size of the input image (default unchecked).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The rotated image.</li>
 * </ul><br />
 */
public class Rotate extends Function2D {
	private static final long serialVersionUID = -6309640810930939257L;

	public Rotate() {
		super();
		name = "Rotate";
		description =
			"Rotate the image with the angle value (<0 clockwise, >0 anticlockwise).";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("angle", false, true);
		p1.setDefault(30);
		p1.setDescription("Rotation angle in degrees");
		
		ParamBool p2 = new ParamBool("clipping", false, true);
		p2.setDefault(false);
		p2.setDescription("Make clipping");

		addParam(p1);
		addParam(p2);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int ang = getParamValueInt("angle");
		boolean clip = getParamValueBool("clipping");
		int widthInit = img.getWidth();
		int heightInit = img.getHeight();
		int widthFinal = widthInit;
		int heightFinal = heightInit;
		ImageType type = img.getType();
		int cx = widthInit / 2;
		int cy = heightInit / 2;
		double sin = Math.sin(Math.toRadians(ang));
		double cos = Math.cos(Math.toRadians(ang));
		int ind;
		int xs, ys;

		if (ang % 360 == 0) ang = 0;
		if (ang == 0) return img;

		if (!clip) {
			widthFinal = (int) (Math.abs(widthInit * cos) + Math.abs(heightInit * sin));
			heightFinal = (int) (Math.abs(widthInit * sin) + Math.abs(heightInit * cos));
			cx = (widthFinal / 2);
			cy = (heightFinal / 2);
		} 
		if (img instanceof JIPImgBitmap) {
			JIPImgBitmap imgBmp = (JIPImgBitmap)img;
			int numBands = imgBmp.getNumBands();
			JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, widthFinal, heightFinal, type);

			double dtemp11 = cy * sin - cx * cos;
			double dtemp21 = -cx * sin - cy * cos;
			
			double[] bmp, bin;

			for (int nb=0; nb < numBands; nb++) {
				bmp = imgBmp.getAllPixels(nb);
				bin = new double[widthFinal * heightFinal];
				for (int y=0; y < heightFinal; y++) {
					ind = y * widthFinal;
					double dtemp31 = (dtemp11 - y * sin) + (widthInit / 2.0);
					double dtemp41 = (dtemp21 + y * cos) + (heightInit / 2.0);
					for (int x=0; x < widthFinal; x++) {
						xs = (int) ((x * cos) + dtemp31);
						ys = (int) ((x * sin) + dtemp41);
						if ((xs >= 0) && (xs < widthInit) && (ys >= 0) && (ys < heightInit))
							bin[ind++] = bmp[widthInit * ys + xs];
						else
							bin[ind++] = 0;
					}
				}
				res.setAllPixels(nb, bin);
			}
			return res;
			
		} else {
			JIPImgGeometric res = (JIPImgGeometric)JIPImage.newImage(widthFinal, heightFinal, type);
			sin = Math.sin(-Math.toRadians(ang));
			cos = Math.cos(-Math.toRadians(ang));
			switch (type) {
				case POINT: ArrayList<Point2D> listpoint = (ArrayList<Point2D>)(((JIPImgGeometric)img).getData());
							ArrayList<Point2D> pointres = new ArrayList<Point2D>();
							for (Point2D p : listpoint) {
								Point2D p1 = rotatePoint(p, widthInit, heightInit, widthFinal, heightFinal, cos, sin);
								if (p1 != null)
									pointres.add(p1);
							}
							res.setData(pointres);
							break;
				case SEGMENT: ArrayList<Segment> listsegment = (ArrayList<Segment>)(((JIPImgGeometric)img).getData());
							ArrayList<Segment> segmentres = new ArrayList<Segment>();
							for (Segment s : listsegment) {
								Point2D p = s.getBegin();
								Point2D begin = rotatePoint(p, widthInit, heightInit, widthFinal, heightFinal, cos, sin);
								if (begin == null)
									continue;
								p = s.getEnd();
								Point2D end = rotatePoint(p, widthInit, heightInit, widthFinal, heightFinal, cos, sin);
								if (end == null)
									continue;
								Segment s1 = new Segment(begin, end);
								segmentres.add(s1);
							}
							res.setData(segmentres);
							break;
				case JUNCTION: ArrayList<Junction> listjunc = (ArrayList<Junction>)(((JIPImgGeometric)img).getData());
							ArrayList<Junction> juncres = new ArrayList<Junction>();
							for (Junction j : listjunc) {
								Point2D p = new Point2D(j.getX(), j.getY());
								Point2D p1 = rotatePoint(p, widthInit, heightInit, widthFinal, heightFinal, cos, sin);
								if (p1 == null)
									continue;
								int[] sit = j.getSituation(), sitaux = new int[j.getSituation().length];
								for (int i=0; i<sit.length; i++) {
									sitaux[i]=(sit[i]+ang)%360;
									if (sitaux[i]<0) sitaux[i] += 360;
								}
								Junction j1 = new Junction(p1.getX(), p1.getY(), j.getR_i(), j.getR_e(), sitaux);
								juncres.add(j1);
							}
							res.setData(juncres);
							break;
				case POLY: ArrayList<Polygon2D> listpolys = (ArrayList<Polygon2D>)(((JIPImgGeometric)img).getData());
							ArrayList<Polygon2D> polyres = new ArrayList<Polygon2D>();
							for (Polygon2D pol : listpolys) {
								ArrayList<Point2D> points = pol.getData();
								ArrayList<Point2D> listp = new ArrayList<Point2D>();
								boolean out = false;
								for (Point2D p : points) {
									Point2D p1 = rotatePoint(p, widthInit, heightInit, widthFinal, heightFinal, cos, sin);
									if (p1 == null) {
										out=true;
										break;
									}
									listp.add(p1);
								}
								if (out) 
									continue;
								Polygon2D polr = new Polygon2D(listp);
								polyres.add(polr);
							}
							res.setData(polyres);
							break;
				case EDGES: ArrayList<Edge> listEdges = (ArrayList<Edge>)(((JIPImgGeometric)img).getData());
							ArrayList<Edge> edgeres = new ArrayList<Edge>();
							for (Edge edg : listEdges) {
								ArrayList<Point2D> points = edg.getData();
								ArrayList<Point2D> listp = new ArrayList<Point2D>();
								ArrayList<Float> values = edg.getValues();
								boolean out = false;
								for (Point2D p : points) {
									Point2D p1 = rotatePoint(p, widthInit, heightInit, widthFinal, heightFinal, cos, sin);
									if (p1 == null) {
										out=true;
										break;
									}
									listp.add(p1);
								}
								if (out) 
									continue;
								Edge polr = new Edge(listp, values);
								edgeres.add(polr);
							}
							res.setData(edgeres);
							break;
			}
			return res;
		}
	}
	
	
	/**
	 * Method which rotates a point.
	 * @param p The point to rotate.
	 * @param wini The init width.
	 * @param hini The init height.
	 * @param w The image result width.
	 * @param h The image result height.
	 * @param cos The cosine of the angle.
	 * @param sin The sine of the angle.
	 * @return Return the point rotated.
	 */
	private Point2D rotatePoint (Point2D p, int wini, int hini, int w, int h, double cos, double sin) {
		int xDes = p.getX() - wini/2;
		int yDes = hini/2 - p.getY();
		int x = (int) (xDes*cos + yDes*sin) + w/2;
		int y = h/2 - (int) (yDes*cos - xDes*sin);
		if (x >= 0 && x <= w && y >= 0 && y <= h) {
			return new Point2D(x, y);
		}
		else return null;
	}
}

