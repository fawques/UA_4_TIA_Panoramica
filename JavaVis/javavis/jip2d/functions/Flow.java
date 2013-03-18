package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It implements the optical flow algorithm which is defined by differential method of 
 * B. K. P. Horn and B. G. Schunck, “Determining optical flow,” AI, vol. 17, pp. 185-204, 
 * 1986.<br />
 * It gets the spatial X & Y gradients and the temporal gradient (T) of two images of the
 * input sequence. An iterative HORN & SCHUCK algorithm are used and finally the flows of
 * the image are represented.<br />
 * It applies to a sequence of images and these images must be BYTE type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <li><em>seq</em>: Sequence with two images. Each image has to be a BYTE type.</li>
 * <li><em>iter</em>: Integer value which indicates the number of iterations for the algorithm 
 * (default 12).</li>
 * <li><em>lambda</em>: Real value which indicates the noise reduction factor (default 2.0).</li>
 * <li><em>sizeBloq</em>: Integer value which indicates the separation between represented flow 
 * (default 5).</li>
 * <li><em>factor</em>: Real value which indicates the factor which allows to enlarge the 
 * represented flow (default 1.0).</li>
 * <li><em>elim</em>: Integer value which indicates the eliminates flow from length &lt; elim*factor 
 * (default 0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Sequence with an image where the flow origin is painted in green and the remainder
 * of it in red. The points where the motion is very small or it is not been painted in blue.
 * </ul><br />
 */
public class Flow extends Function2D {
	private static final long serialVersionUID = -4783214630724479756L;

	public Flow() {
		super();
		name = "Flows";
		description = "Calculates Ix, Iy & It gradients. Applies to sequence and BYTE type.";
		groupFunc = FunctionGroup.Others;

		ParamInt p1 = new ParamInt("iter", false, true);
		p1.setDescription("Number of iterations for the algorithm.");
		p1.setDefault(12);
		
		ParamFloat p2 = new ParamFloat("lambda", false, true);
		p2.setDescription("Noise reduction factor");
		p2.setDefault(2.0f);
		
		ParamInt p3 = new ParamInt("sizeBloq", false, true);
		p3.setDescription("Separation between represented flow");
		p3.setDefault(5);
		
		ParamFloat p4 = new ParamFloat("factor", false, true);
		p4.setDescription("Allows to enlarge the represented flow");
		p4.setDefault(1.0f);
		
		ParamInt p5 = new ParamInt("elim", false, true);
		p5.setDescription("Eliminates flow of length < elim*factor");
		p5.setDefault(0);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {			
		throw new JIPException("Please, select Complete Sequence when applying this function");
	}

	public Sequence processSeq(Sequence seq) throws JIPException {
		if (seq.getFrame(0).getType() != ImageType.BYTE) 
			throw new JIPException("Function Flow can only be applied to byte format");

		int numFrames = seq.getNumFrames(); 
		if (numFrames < 2) 
			throw new JIPException("You need at least 2 frames.");
		
		int numIter = getParamValueInt("iter");
		double lambda = getParamValueFloat("lambda");
		int width = seq.getFrame(0).getWidth();
		int height = seq.getFrame(0).getHeight();
		int numPixels = width * height;
		JIPBmpByte image1;
		JIPBmpByte image2 = (JIPBmpByte)seq.getFrame(0);

		for (int nf=0; nf < numFrames-1; nf++) {
			image1 = image2;
			image2 = (JIPBmpByte)seq.getFrame(nf+1);
			if (image2.getType() != ImageType.BYTE) 
				throw new JIPException("Flow can only be applied to byte format");
	
			double[] gradientX = new double[numPixels];
			double[] gradientY = new double[numPixels];
			double[] gradientT = new double[numPixels];
			for (int i=0; i < numPixels; i++) {
				gradientX[i] = 0;
				gradientY[i] = 0;
				gradientT[i] = 0;
			}
	
			// X gradient
			for (int y=0; y < height-1; y++) 
				for (int x=0; x < width-1; x++) { 
					double m1 = image1.getPixel(0, x + 1, y) - image1.getPixel(0, x, y);
					double m2 = image1.getPixel(0, x + 1, y + 1) - image1.getPixel(0, x, y + 1);
					double m3 = image2.getPixel(0, x + 1, y) - image2.getPixel(0, x, y);
					double m4 = image2.getPixel(0, x + 1, y + 1) - image2.getPixel(0, x, y + 1);
					gradientX[y * width + x] = ((m1 + m2 + m3 + m4) / 4);
				}
			// Y gradient
			for (int y=0; y < height-1; y++) 
				for (int x=0; x < width-1; x++) { 
					double m1 = image1.getPixel(0, x, y + 1) - image1.getPixel(0, x, y);
					double m2 = image1.getPixel(0, x + 1, y + 1) - image1.getPixel(0, x + 1, y);
					double m3 = image2.getPixel(0, x, y + 1) - image2.getPixel(0, x, y);
					double m4 = image2.getPixel(0, x + 1, y + 1) - image2.getPixel(0, x + 1, y);
					gradientY[y * width + x] = (m1 + m2 + m3 + m4) / 4;
				}
			// T gradient
			for (int y=0; y < height-1; y++)
				for (int x=0; x < width-1; x++) { 
					double m1 = image2.getPixel(0, x, y) - image1.getPixel(0, x, y);
					double m2 = image2.getPixel(0, x, y + 1) - image1.getPixel(0, x, y + 1);
					double m3 = image2.getPixel(0, x + 1, y) - image1.getPixel(0, x + 1, y);
					double m4 = image2.getPixel(0, x + 1, y + 1) - image1.getPixel(0, x + 1, y + 1);
					gradientT[y * width + x] = (m1 + m2 + m3 + m4) / 4;
				}
	
			double[] initializationU = new double[numPixels];
			double[] initializationV = new double[numPixels];
			for (int i=0; i < numPixels; i++) {
				initializationU[i] = 0.0;
				initializationV[i] = 0.0;
			}
			
			double newAngle = 0.0;
			for (int y=1; y < height-1; y++) 
				for (int x=1; x < width-1; x++) { 
					double Ix = gradientX[y * width + x];
					double Iy = gradientY[y * width + x];
					if (!(Ix == 0.0 && Iy == 0.0)) {
						double norma = Math.sqrt(Ix * Ix + Iy * Iy);
						double mod = -gradientT[y * width + x] / norma;
						double angle = Math.atan(Iy / Ix);
						if (Ix >= 0.0 && Iy >= 0.0) { 
							newAngle = angle; 
						} else if (Ix < 0.0 && Iy > 0.0) {
							newAngle = Math.PI + angle; 
						} else if (Ix < 0.0 && Iy <= 0.0) { 
							newAngle = Math.PI + angle; 
						} else if (Ix >= 0.0 && Iy < 0.0) { 
							newAngle = 2 * Math.PI + angle; 
						}
						initializationU[y * width + x] = mod * Math.cos(newAngle);
						initializationV[y * width + x] = mod * Math.sin(newAngle);
					}
				}
			
			double[] currentU;
			double[] currentV; 
			double[] newU = new double[numPixels];
			double[] newV = new double[numPixels];
			for (int i=0; i < numPixels; i++) {
				newV[i] = 0.0;
				newU[i] = 0.0;
			}
	
			currentU = average(initializationU, width, height);
			currentV = average(initializationV, width, height);
			for (int nt=0; nt < numIter; nt++) {
				for (int y=1; y < height-1; y++) { 
					for (int x=1; x < width-1; x++) {
						double numerator = gradientX[y * width + x] * currentU[y * width + x] + gradientY[y * width + x] * 
								currentV[y * width + x] + gradientT[y * width + x];
						double denominator = lambda + gradientX[y * width + x] * 
								gradientX[y * width + x] + gradientY[y * width + x] * 
								gradientY[y * width + x];
						newU[y * width + x] = currentU[y * width + x]
								- (gradientX[y * width + x] * numerator / denominator);
						newV[y * width + x] = currentV[y * width + x]
								- (gradientY[y * width + x] * numerator / denominator);
					}
				}
				currentU = average(newU, width, height);
				currentV = average(newV, width, height);
			}
			JIPImage temp = paintFlows(width, height, newU, newV, 
					getParamValueInt("sizeBloq"), getParamValueFloat("factor"), 
					getParamValueInt("elim"));
			temp.setName("Final result");
			seq.addFrame(temp);
		}
		return seq;
	}

	
	/**
	 * Method which does the average of a image with a 3x3 operator (mask), with values 
	 * from left-right and top-down: 1/12, 1/6, 1/12, 1/6, 0 1/6, 1/12, 1/6, 1/12.
	 * @param gradient An array with image pixel values which are concatenated by rows.
	 * @param w The image width.
	 * @param h The image height.
	 * @return an array with the average pixels of the image.
	 */
	private double[] average(double[] gradient, int w, int h) {
		double[] averaged = new double[w * h];
		for (int i=0; i < w*h; i++)
			averaged[i] = 0.0;

		for (int y=1; y < h-1; y++) 
			for (int x=1; x < w-1; x++) { 
				double auxAverage = (1.0 / 12.0) * (gradient[(y - 1) * w + (x - 1)] 
						+ gradient[(y - 1) * w + (x + 1)] 
						+ gradient[(y + 1) * w + (x - 1)] 
						+ gradient[(y + 1) * w + (x + 1)]);
				auxAverage += (1.0 / 6.0) * (gradient[(y - 1) * w + (x)]
						+ gradient[y * w + (x - 1)]
						+ gradient[y * w + (x + 1)]
						+ gradient[(y + 1) * w + x]);
				averaged[y * w + x] = auxAverage;
			}
		return averaged;
	}
	
	/**
	 * Method which draws the detected flow in the result image.
	 * @param w The image width.
	 * @param h The image height.
	 * @param U Results of the HORN & SCHUNCK algorithm for a U component.
	 * @param V Results of the HORN & SCHUNCK algorithm for a V component.
	 * @param sizeBloq Separation between represented flow.
	 * @param factor allows to enlarge the represented flow.
	 * @param elim Eliminates flow of length < elim*factor.
	 * @return A result image with the painted flows.
	 * @throws JIPException
	 */
	private JIPImage paintFlows(int w, int h, double[] U, double[] V, int sizeBloq, 
			float factor, int elim) throws JIPException {
		ArrayList<Segment> vec = new ArrayList<Segment>();
		for (int y=1; y < h-1; y+=sizeBloq)
			for (int x=1; x < w-1; x+=sizeBloq) {
				int i2 = Math.round((float) V[y * w + x] * factor);
				int j2 = Math.round((float) U[y * w + x] * factor);
				if (Math.sqrt(i2*i2 + j2*j2) > factor*elim) {
					vec.add(new Segment(new Point2D(x, y), new Point2D(x+j2, y+i2)));
				}
			}
		JIPGeomSegment res = new JIPGeomSegment (w, h);
		res.setData(vec);
		
		return res;
	} 
}

