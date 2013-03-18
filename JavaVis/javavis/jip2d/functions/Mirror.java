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
 * It makes a reversal of image pixels and geometric data.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The image with the pixels reverse from vertical axis.</li>
 * </ul><br />
 */
public class Mirror extends Function2D {
	private static final long serialVersionUID = 1393197595197881043L;

	public Mirror() {
		super();
		name = "Mirror";
		description = "Makes a reversal of image pixels and geometric data.";
		groupFunc = FunctionGroup.Manipulation;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		int width = img.getWidth();
		int height = img.getHeight();
		ImageType type = img.getType();

		if (img instanceof JIPImgBitmap) {
			int numBands = ((JIPImgBitmap)img).getNumBands();
			JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, type);
			for (int nb=0; nb < numBands; nb++)
				for (int y=0; y < height; y++)
					for (int x=0; x < width; x++) {
						double iPixel = ((JIPImgBitmap)img).getPixel(nb, x, y);
						res.setPixel(nb, x, y, ((JIPImgBitmap)img).getPixel(nb, width-x-1, y));
						res.setPixel(nb, width-x-1, y, iPixel);
					}
			return res;
		} else {
			JIPImgGeometric res = (JIPImgGeometric)JIPImage.newImage(width, height, type);
			JIPImgGeometric imgGeom = (JIPImgGeometric)img;
			switch(imgGeom.getType()) {
				case POINT: ArrayList<Point2D> points = new ArrayList<Point2D>(imgGeom.getData());
							for (Point2D p : points) 
								p.setX(width - 1 - p.getX());
							res.setData(points);
							break;
				case SEGMENT: ArrayList<Segment> segments = new ArrayList<Segment>(imgGeom.getData());
							for (Segment s : segments) {
								s.getBegin().setX(width -1 - s.getBegin().getX());
								s.getEnd().setX(width -1 - s.getEnd().getX());
							}
							res.setData(segments);
							break;
				case EDGES: ArrayList<Edge> edges = new ArrayList<Edge>(imgGeom.getData());
							ArrayList<Point2D> pointAux;
							for (Edge e : edges) {
								pointAux = e.getData();
								for (Point2D p : pointAux) 
									p.setX(width - 1 - p.getX());
							}
							res.setData(edges);
							break;
				case POLY: ArrayList<Polygon2D> polys = new ArrayList<Polygon2D>(imgGeom.getData());
							ArrayList<Point2D> pointAux2;
							for (Polygon2D pol : polys) {
								pointAux2 = pol.getData();
								for (Point2D p : pointAux2) 
									p.setX(width - 1 - p.getX());
							}
							res.setData(polys);
							break;
				case JUNCTION: ArrayList<Junction> junctions = new ArrayList<Junction>(imgGeom.getData());
							for (Junction j : junctions) 
								j.setX(width - 1 - j.getX());
							res.setData(junctions);
							break;
			}
			return res;
		}
	}
}

