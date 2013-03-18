package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPGeomEdges;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It converts an Edge type image into a Segment type. It is used to use Canny, followed by 
 * Link and then this function.<br />
 * It applies to EDGE type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a EDGE type.</li>
 * <li><em>accuracy</em>: Real value which indicates the value to make a cut (default 2.0).</li>
 * <li><em>granularity</em>: Integer value which indicates the minimum length of segment 
 * to continue cutting (default 4).</li>
 * <li><em>magnitude</em>: Integer value which indicates the minimum length of the current 
 * segment (default 0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A SEGMENT image.</li>
 * </ul><br />
 */
public class SegEdges extends Function2D {
	private static final long serialVersionUID = -7418005545553156429L;
	/**
	 * Number of breakpoints
	 * @uml.property  name="nbreakpoints"
	 */
	private int nbreakpoints;
	
	public SegEdges() {
		super();
		name = "SegEdges";
		description = "Obtains segments from edges. Applies to EDGE type.";
		groupFunc = FunctionGroup.Edges;
		
		ParamFloat p1 = new ParamFloat("accuracy", false, true);
		p1.setDefault(2.0f);
		p1.setDescription("Lowest deviation from the pixel measure");
		
		ParamInt p2 = new ParamInt("granularity", false, true);
		p2.setDefault(4);
		p2.setDescription("Lowest length of segment during recursive subdivision.");
		
		ParamInt p3 = new ParamInt("magnitude", false, true);
		p3.setDefault(0);
		p3.setDescription("Lowest magnitude of actual segment.");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.EDGES) {
			ArrayList<Segment> segments = new ArrayList<Segment>();
			ArrayList<Segment> segAux;
			ArrayList<Edge> edges = (ArrayList<Edge>)((JIPGeomEdges)img).getData();
			float accuracy = getParamValueFloat("accuracy");
			int granularity = getParamValueInt("granularity");
			int magnitude = getParamValueInt("magnitude");
			
			for (int r=0; r < edges.size(); r++) {
				segAux = segEdgesIntoLines(edges.get(r), accuracy, magnitude, granularity);
				if (segAux!=null && segAux.size()>0) segments.addAll(segAux);
			}
			
			JIPGeomSegment res = (JIPGeomSegment)JIPImage.newImage(img.getWidth(), img.getHeight(), ImageType.SEGMENT);
			res.setData(segments);
			
			return res;
		} 
		else 
			throw new JIPException("Function SegEdges can not be applied to this image format.");
	}

	
	/**
	 * Method which processes a particular edge.
	 * @param edge Edge vector to process.
	 * @param accuracy The value to make a cut.
	 * @param magnitude The minimum length of segment to continue cutting.
	 * @param granularity The minimum length of the current segment.
	 * @return A list of segments.
	 */
	private ArrayList<Segment> segEdgesIntoLines(Edge edge, float accuracy, int magnitude, int granularity) {
		if (edge.length() <= 2) return null;
		
		int []breakpoints = new int[edge.length()];
		breakpoints[0] = 0;
		nbreakpoints = 1;
		ArrayList<Segment> segments = new ArrayList<Segment>();
		splitSegment(0, (edge.length())-1, accuracy, granularity, edge, breakpoints);
		breakpoints[nbreakpoints] = (edge.length()) - 1;

		ArrayList<Point2D> data = edge.getData();
		ArrayList<Float> values = edge.getValues();
		for (int i=0; i < nbreakpoints; i++) {
			if (magnitude > 0 && distance(data.get(breakpoints[i]), data.get(breakpoints[i+1])) < 2) {
				double t = 0.0;
				for (int j=breakpoints[i]; j <= breakpoints[i + 1]; j++)
					t += values.get(j);
				if (t / (breakpoints[i + 1] - breakpoints[i]) < magnitude)
					continue;
			}
			Point2D begin = new Point2D(data.get(breakpoints[i]));
			Point2D end = new Point2D(data.get(breakpoints[i+1]));
			segments.add(new Segment(begin, end));
		}
		return segments;
	}
	
	/**
	 * Method which calculate the distance between 2 points.
	 * @param p1 Value of the first point.
	 * @param p2 Value of the second point.
	 * @return The distance between 2 points.
	 */
	private double distance (Point2D p1, Point2D p2) {
		return Math.sqrt(Math.pow(p1.getX()-p2.getX(), 2.0)+Math.pow(p1.getY()-p2.getY(), 2.0));
	}

	/**
	 * Method which split an edge depending on the best ratio of deviation/length from 
	 * the segment.
	 * @param first The first point of the edge to use (initial segment point).
	 * @param last The last point of the edge to use (final segment point).
	 * @param accuracy The value to make a cut.
	 * @param granularity The minimum length of the current segment.
	 * @param edge Edge vector to process.
	 * @param breakpoints The breakpoints.
	 * @return The high length or ratio of the segment deviation.
	 */
	private float splitSegment(int first, int last, float accuracy, int granularity,
			Edge edge, int []breakpoints) {
		int save_nbreakpoints = nbreakpoints;
		float next1, next2, maxNext;
		float []next = new float[1];

		if (last - first <= granularity)
			return 0.0f;
		int maxp = maxPoint(first, last, next, accuracy, edge);
		next1 = splitSegment(first, maxp, accuracy, granularity, edge, 
				breakpoints);
		breakpoints[nbreakpoints++] = maxp;
		next2 = splitSegment(maxp, last, accuracy, granularity, edge, breakpoints);
		maxNext = next1 > next2 ? next1 : next2;

		if (maxNext > next[0]) 
			return maxNext;
		else {
			nbreakpoints = save_nbreakpoints;
			return next[0];
		}
	}
	
	/**
	 * Method which calculates the position of the most remote point in relation to the
	 * line which link to the first point and the last edge and it is contented in the 
	 * edge.
	 * @param first The first point of the edge to use (initial segment point).
	 * @param last The last point of the edge to use (final segment point).
	 * @param sig The square of the length.
	 * @param accuracy The value to make a cut.
	 * @param edge Edge vector to process.
	 * @return The position of the most remote point.
	 */
	private int maxPoint(int first, int last, float[] next, float accuracy, Edge edge) {
		int maxp; 
		float maxdev = 0.0f;
		ArrayList<Point2D> data = edge.getData();
		int x0 = data.get(first).getX();
		int y0 = data.get(first).getY();
		int dx = data.get(last).getX() - x0;
		int dy = data.get(last).getY() - y0;

		for (int i=maxp=first+1; i < last; i++) {
			float px = data.get(i).getX() - x0;
			float py = data.get(i).getY() - y0;

			double t = (dx * px + dy * py) / (double) (dx * dx + dy * dy);
			px -= dx * t;
			py -= dy * t;
			float dev = px * px + py * py;

			if (dev > maxdev) {
				maxdev = dev;
				maxp = i;
			}
		}

		if (maxdev < accuracy)
			maxdev = accuracy;

		next[0] = (dx * dx + dy * dy) / maxdev;
		return maxp;
	}
}

