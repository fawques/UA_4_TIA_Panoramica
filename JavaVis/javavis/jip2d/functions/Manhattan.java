package javavis.jip2d.functions;

import java.util.ArrayList;
import java.util.Arrays;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It finds the efficient orientation angle of the camera when it captured the image. To make it,
 * the function uses bayesian information. First, we obtain segments from image: Canny plus Flink plus 
 * SegEdges. Afterwards, some edges are removed in order to eliminate short ones. 
 * The main algorithm scans every possible orientations. For each orientation, it calculates 
 * for each segment its probability associated to an orientation. It accumulates the 
 * probability of each segment. Finally, it returns the orientation with a maximum number 
 * of votes.<br />
 * <em>J. Coughlan and A.L. Yuille. "Manhattan World: Orientation and Outlier Detection by 
 * Bayesian Inference". Neural Computation. Vol. 15, No. 5, pp. 1063-88. May 2003</em>.<br />
 * It applies to COLOR type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image of the camera to be processed. It has to be a COLOR type.</li>
 * <li><em>focal</em>: Integer value which indicates the focal length of the camera (default 500).</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>angle</em>: A real value which indicates the orientation angle of the camera.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image with found edges of REAL type.</li>
 * </ul><br />
 */
public class Manhattan extends Function2D {
	private static final long serialVersionUID = -7945018801531035140L;

	private static final int VOTES_SIZE = 91; //[-45, 45]
	
	public Manhattan() {
		super();
		name = "Manhattan";
		description = "Calculates the camera orientation angle.";
		groupFunc = FunctionGroup.Others;
		
		ParamInt p1 = new ParamInt("focal", false, true);
		p1.setDescription("Focal length");
		p1.setDefault(500);
		addParam(p1);
		
		//Output parameter
		ParamFloat r1 = new ParamFloat("angle", false, false);
		addParam(r1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.COLOR) 
			throw new JIPException("Manhattan can not be applied to this image format.");

		Canny c = new Canny();
		c.setParamValue("brightness", 300);
		JIPImage auxImg = c.processImg(img);
		Link l = new Link();
		JIPImage auxImg2 = l.processImg(auxImg);
		SegEdges e = new SegEdges();
		JIPImgGeometric res = (JIPImgGeometric) e.processImg(auxImg2);

		ArrayList<Segment> segments = (ArrayList<Segment>)res.getData();
		int width = img.getWidth();
		int height = img.getHeight();
		
		double[] votes = new double[VOTES_SIZE];
		Arrays.fill(votes, 0.0);

		double f = getParamValueInt("focal");
		double ang_tol = 4.0 * Math.PI / 180.0;
		double inbox = 0.9 / (4 * ang_tol);
		double outbox = 0.1 / (2 * Math.PI - 4 * ang_tol);
		double p_i = 0.2, p_j = 0.2, p_k = 0.2, p_outlier = 0.4;
		double sum = p_i + p_j + p_k + p_outlier;
		p_i /= sum;
		p_j /= sum;
		p_k /= sum;
		p_outlier /= sum;
		double or_k = 0.0;
		double out_vote = 1 / (2.0 * Math.PI) * p_outlier;

		for (int angle=-45; angle <= 45; angle++) {
			double phibesttan = Math.tan(Math.toRadians(angle));
			for (Segment s : segments) {
				int x1 = s.getBegin().getX();
				int y1 = s.getBegin().getY();
				int x2 = s.getEnd().getX();
				int y2 = s.getEnd().getY();
				int diffX = x2 - x1;
				int diffY = y1 - y2;

				double xAverage = (x1 + x2) / 2.0;
				double yAverage = (y1 + y2) / 2.0;
				double length = Math.sqrt(diffX * diffX + diffY * diffY);
				double orientation = Math.atan2(diffY, diffX);
				orientation += Math.PI / 2.0;
				int u = (int) xAverage - (width / 2);
				int v = (height / 2) - (int) yAverage;
				double or_i = Math.atan2((-f * phibesttan) - u, v);
				double or_j = Math.atan2((f / phibesttan) - u, v);

				double vote_i = (consistent(or_i, orientation, ang_tol) ? inbox : outbox) * p_i;
				double vote_j = (consistent(or_j, orientation, ang_tol) ? inbox : outbox) * p_j;
				double vote_k = (consistent(or_k, orientation, ang_tol) ? inbox : outbox) * p_k;

				votes[angle + 45] += Math.log(greater_than4(vote_i, vote_j, vote_k, out_vote))
						* length;
			}
		}

		int pos = -1;
		double auxD = -64000.0;
		for (int i=0; i < VOTES_SIZE; i++) 
			if (votes[i] > auxD) {
				auxD = votes[i];
				pos = i;
			}
			
		setParamValue("angle", (float)(pos - 45));
		info = "Angle obtained " + (pos-45);
		
		return res;
	}

	
	/**
	 * Method which calculates the module.
	 * @param x The first value.
	 * @param m The second value.
	 * @return The module value.
	 */
	private double module(double x, double m) {
		return (x - m * Math.floor(x / m));
	}

	/**
	 * Method which indicates if an angle is consistent.
	 * @param x The first value.
	 * @param y The second value.
	 * @param del_ang The angle value.
	 * @return A boolean indicating true if an angle is consistent, false in otherwise.
	 */
	private boolean consistent(double x, double y, double del_ang) {
		return ((module(x - y, Math.PI) < del_ang)
				|| (Math.PI - module(x - y, Math.PI) < del_ang));
	}

	/**
	 * Method which calculates the greater number between 4.
	 * @param x1 The first number.
	 * @param x2 The second number.
	 * @param x3 The third number.
	 * @param x4 The fourth number.
	 * @return The greatest number.
	 */
	private double greater_than4(double x1, double x2, double x3, double x4) {
		if (x1 >= x2 && x1 >= x3 && x1 >= x4)
			return x1;
		if (x2 >= x1 && x2 >= x3 && x2 >= x4)
			return x2;
		if (x3 >= x1 && x3 >= x2 && x3 >= x4)
			return x3;
		if (x4 >= x1 && x4 >= x2 && x4 >= x3)
			return x4;
		if (x1 == x2 && x1 == x3 && x1 == x4)
			return 0;
		return x1;
	}
}

