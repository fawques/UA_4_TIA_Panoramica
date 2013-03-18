package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
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
 * It makes a reversal of image pixels and geometric data from horizontal axis.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The image with the pixels reverse over horizontal axis.</li>
 * </ul><br />
 */
public class Flip extends Function2D {
	private static final long serialVersionUID = 2452843773543656719L;

	public Flip() {
		super();
		name = "Flip";
		description = "Flips an image.";
		groupFunc = FunctionGroup.Manipulation;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int width = img.getWidth();
		int height = img.getHeight();
		ImageType type = img.getType();

		if (img instanceof JIPImgBitmap) {
			JIPImgBitmap imgBmp = (JIPImgBitmap)img;
			int numBands = imgBmp.getNumBands();
			JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, type);
			double imgPixel;
			
			for (int nb=0; nb < numBands; nb++)
				for (int y=0; y < height; y++)
					for (int x=0; x < width; x++) {
						imgPixel = imgBmp.getPixel(nb, x, y);
						res.setPixel(nb, x, y, imgBmp.getPixel(nb, x, height-y-1));
						res.setPixel(nb, x, height - y - 1, imgPixel);
					}
			
			return res;
			
		} else {
			JIPImgGeometric res = (JIPImgGeometric)JIPImage.newImage(width, height, type);
			JIPImgGeometric imgGeom = (JIPImgGeometric)img;
			switch (imgGeom.getType()) {
				case POINT: ArrayList<Point2D> points = new ArrayList<Point2D>();
							ArrayList<Point2D> aux = (ArrayList<Point2D>)imgGeom.getData();
							for (Point2D p : aux) 
								points.add(new Point2D(p.getX(), (height - 1 - p.getY())));
							res.setData(points);
							break;
				case SEGMENT: ArrayList<Segment> segments = new ArrayList<Segment>();
							ArrayList<Segment> auxP = (ArrayList<Segment>)imgGeom.getData();
							for (Segment s : auxP) {
								Segment s1 = new Segment(s);
								s1.getBegin().setY(height-1-s.getBegin().getY());
								s1.getEnd().setY(height-1-s.getEnd().getY());
								segments.add(s1);
							}
							res.setData(segments);
							break;
				case EDGES: ArrayList<Edge> edges = new ArrayList<Edge>();
							ArrayList<Edge> edgeAux = (ArrayList<Edge>)imgGeom.getData();
							ArrayList<Point2D> pointAux, pointRes;
							for (Edge e : edgeAux) {
								pointAux = e.getData();
								pointRes= new ArrayList<Point2D>();
								for (Point2D p : pointAux) {
									Point2D p1 = new Point2D(p);
									p1.setY(height-1-p1.getY());
									pointRes.add(p1);
								}
								edges.add(new Edge(pointRes));
							}
							res.setData(edges);
							break;
				case POLY: ArrayList<Polygon2D> polys = new ArrayList<Polygon2D>();
							ArrayList<Polygon2D> polyAux = (ArrayList<Polygon2D>)imgGeom.getData();
							ArrayList<Point2D> pointAux2, pointRes2;
							for (Polygon2D p : polyAux) {
								pointAux2 = p.getData();
								pointRes2= new ArrayList<Point2D>();
								for (Point2D po : pointAux2) {
									Point2D p1 = new Point2D(po);
									p1.setY(height-1-p1.getY());
									pointRes2.add(p1);
								}
								polys.add(new Polygon2D(pointRes2));
							}
							res.setData(polys);
							break;
				case JUNCTION: ArrayList<Junction> junctions = new ArrayList<Junction>(imgGeom.getData());
							for (Junction j : junctions) 
								j.setY(height-1-j.getY());
							res.setData(junctions);
							break;
			}
			
			return res;
		}
	}
}

