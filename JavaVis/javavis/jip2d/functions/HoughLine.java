package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;
import javavis.jip2d.util.Line;

/**
 * It detects all the possible lines present in the image. Every pixel votes for the line it
 * belongs to, then we use the lines which number of votes are greater or equal than the
 * input threshold.<br />
 * It applies to BIT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BIT type image.</li>
 * <li><em>thres</em>: Integer value which indicates the minimal number of votes to accept 
 * the line (default 30).</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>lines</em>: A object list which contains the lines found.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A SEGMENT image which has detected lines.</li>
 * </ul><br />
 */
public class HoughLine extends Function2D {
	private static final long serialVersionUID = 3684910424267717380L;
	
	public HoughLine() {
		super();
		name = "HoughLine";
		description = "Detects lines in an input image. Applies to BIT type.";
		groupFunc = FunctionGroup.FeatureExtract;

		ParamInt p1 = new ParamInt("thres", false, true);
		p1.setDefault(30);
		p1.setDescription("Minimum number of votes");
		
		addParam(p1);
		
		// Output parameters
		ParamObject r1 = new ParamObject("lines", false, false);
		r1.setDescription("Lines found");
		
		addParam(r1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BIT) 
			throw new JIPException("Function HoughLine can only be applied to binarized images.");
		
		final int DISC_THETA = 180;  // Theta divisions (discretizations)
		final int DISC_RHO = 1000;   // Rho divisions (discretizations)

		JIPBmpBit imgBit = (JIPBmpBit)img;
		int width = img.getWidth();
		int height = img.getHeight();
		int threshold = getParamValueInt("thres");

		/* It's not necessary to use all the interval for angles (2*PI). Only half of this 
		 * interval (PI) is enough to detect all the lines in the image. This interval will 
		 * cover from -PI/2 to PI/2.
		 */ 
		double maxTheta = Math.PI / 2;
		double maxRho = Math.sqrt(width*width+height*height);  // image diagonal
		double incTheta = Math.PI / DISC_THETA;
		double incRho = 2*maxRho / DISC_RHO;
			
		// Accumulator space: 2D table that stores the number of votes (java initializes it)
		int[][] votes = new int[DISC_THETA][DISC_RHO];
		double dTheta, dRho;
		
		// Votation loop
		for (int x=0; x < width; x++)
			for (int y=0; y < height; y++)
				if (imgBit.getPixelBool(x,y))
					for (int theta=0; theta < DISC_THETA; theta++) {
						dTheta = -maxTheta + theta * incTheta;   // theta range is [-maxTheta, +maxTheta]
						dRho = x*Math.cos(dTheta) + y*Math.sin(dTheta) + maxRho;  // rho in range [-maxRho, +maxRho]
						votes[theta][(int) (dRho/incRho)]++;
					}

		ArrayList<Segment> linePoints = new ArrayList<Segment>();  // cut points of lines to be drawn
		ArrayList<Line> lineParms = new ArrayList<Line>();  // (theta, rho) pairs (lines) stored for extern calls

		// loop to draw all the lines that match or exceed the threshold
		for (int theta=0; theta < DISC_THETA; theta++)
			for (int rho=0; rho < DISC_RHO; rho++)
				if (votes[theta][rho] >= threshold) {
					dTheta = theta*incTheta - maxTheta; // retrieve "original" theta value 
					dRho = rho*incRho - maxRho;  // retrieve "original" rho value
					Line l = new Line(dTheta, dRho);
           			if (!lineParms.contains(l)) { // prevent from potential line duplicity					
           				lineParms.add(l);
           				linePoints.add(getCutPoints(dTheta, dRho, width, height));
           			}
				}

		setParamValue("lines", lineParms);
		JIPGeomSegment res = new JIPGeomSegment(width, height);
		res.setData(linePoints);
	
		return res;
	}

	
	/**
	 * Method which returns the segment where the line intersects the edge of the image to
	 * draw it later.
	 * @param theta Angle between abscise and perpendicular of the line.
	 * @param rho Distance from the origin to the line.
	 * @param width Image width.
	 * @param height Image height.
	 * @return A segment type, with the two cut points.
	 */
	private Segment getCutPoints(double theta, double rho, int width, int height) {
		int w1 = width-1;   // last x-pixel of the image
		int h1 = height-1;  // last y-pixel of the image
		Segment defHoriz = new Segment(new Point2D(0, (int)rho), new Point2D(w1, (int)rho));	// horizontal lines
		Segment defVert = new Segment(new Point2D((int)rho, 0), new Point2D((int)rho, h1));	// vertical lines
		if (Math.cos(theta) == 0)
			return defHoriz;
		else
			if (Math.sin(theta) == 0)
				return defVert;
			else {
				int[] pcp = new int[8];  // potential cut points
				int left = (int)(rho/Math.sin(theta)); // cut y-pixel on left edge of the image
				int right = (int)((rho-w1*Math.cos(theta))/Math.sin(theta)); // y-pixel right
				int up = (int)(rho/Math.cos(theta));  // x-pixel up
				int down = (int)((rho-h1*Math.sin(theta))/Math.cos(theta));  // x-pixel down
				int i=0;  // counts how many pixel coordinates are inside image width & height 
				if (left >= 0 && left <= h1)  {pcp[i++] = 0;  pcp[i++] = left;}
				if (right >= 0 && right <= h1)  {pcp[i++] = w1;  pcp[i++] = right;}			
				if (up > 0 && up < w1)  {pcp[i++] = up;  pcp[i++] = 0;}
				if (down > 0 && down < w1)  {pcp[i++] = down;  pcp[i++] = h1;}
				
				/* It corrects the particular case of the diagonals, in which the cut points 
				 * are the corners. Due to discretization and later cast from double to int 
				 * type, there can be an exception if 0, 1, 3, or 4 cut points (i= 0, 2, 6 or 
				 * 8 coordinates) are obtained; i=4 means correct (2 cut points), i=8 (4 cut 
				 * points) is not correct but we can discard the two last cut points. 
				 * Otherwise, the non-valid cut point is manually substituted by the correct 
				 * one of the diagonal. 
				 */	
				if (i != 4 && i != 8) {
					if (!(left >= 0 && left <= h1))
						left = (Math.min(Math.abs(left-0),Math.abs(left-h1)) == Math.abs(left-0)) ? 0 : h1;
					if (!(right >= 0 && right <= h1))
						right = (Math.min(Math.abs(right-0),Math.abs(right-h1)) == Math.abs(right-0)) ? 0 : h1;
					pcp[0] = 0;  pcp[1] = left;  // one corner
					pcp[2] = w1;  pcp[3] = right; // the other corner		
				}						
				return new Segment(new Point2D(pcp[0], pcp[1]), new Point2D(pcp[2], pcp[3]));
			}
	}
}

