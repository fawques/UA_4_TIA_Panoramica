package javavis.jip2d.functions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;
import javavis.jip2d.functions.adaBoost.adaBoost;
import javavis.jip2d.functions.adaBoost.strongLearner;

/**
 * It detects faces in an image.<br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * <li><em>LearnerFile</em>: File which contains the classification object obtained from 
 * training.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image.</li>
 * </ul><br />
 */
public class FaceDetector extends Function2D {

	private static final long serialVersionUID = -2842975233638032051L;

	public FaceDetector() {
		name = "FaceDetector";
		description = "Detects faces in an image.";
		groupFunc = FunctionGroup.Applic;
		
		ParamFile p1 = new ParamFile("LearnerFile", true, true);
		p1.setDescription("Classification object obtained from training");
		
		addParam(p1);
	}

	/* (non-Javadoc)
	 * @see javavis.jip2d.base.Function2D#processImg(javavis.jip2d.base.JIPImage)
	 */
	@Override
	public JIPImage processImg(JIPImage img) throws JIPException {
		//Obtain Learner
		strongLearner classifier = readResult(getParamValueString("LearnerFile"));
		
		return testImage(classifier, img);
	}
	

	/**
	 * Method which test an image for detecting faces.
	 * @param learner The classification object obtained from training.
	 * @param img The image to detect faces.
	 * @return The image result.
	 * @throws JIPException
	 */
	private JIPImage testImage(strongLearner learner, JIPImage img) throws JIPException
	{
		int rows = img.getHeight();
		int columns = img.getWidth();
		JIPGeomSegment res = new JIPGeomSegment(columns, rows);
		int row, col;
		int xSize = learner.xSize;
		int ySize = learner.ySize;
		rows -= ySize;
		columns -= xSize;
		
		ColorToGray fcg = new ColorToGray();
		IntegralImage fii = new IntegralImage();
		JIPBmpFloat iImage = (JIPBmpFloat)fii.processImg(fcg.processImg(img));
		
		for (row=0; row < rows; row++)
		{
			for (col=0; col < columns; col++)
			{
				if (learner.classify(iImage, col, row) == 1)
				{
					res.addSegment(new Segment(new Point2D(col, row), new Point2D(col+xSize, row)));
					res.addSegment(new Segment(new Point2D(col, row), new Point2D(col, row+ySize)));
					res.addSegment(new Segment(new Point2D(col+xSize, row), new Point2D(col+xSize, row+ySize)));
					res.addSegment(new Segment(new Point2D(col, row+ySize), new Point2D(col+xSize, row+ySize)));
				}
			}
		}
		
		return res;
	}

	
	/**
	 * Method which reads the file which contains the information about the classification
	 * object obtained from training.
	 * @param filename The file which contains the information.
	 * @return A learner object.
	 * @throws JIPException
	 */
	private strongLearner readResult(String filename) throws JIPException
	{
		adaBoost res;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			res = (adaBoost)in.readObject();
		} catch(IOException ioe) {
			throw new JIPException(ioe.getMessage());
		}catch (ClassNotFoundException cnfe) {
			throw new JIPException(cnfe.getMessage());
		}
		
		return res.result;
	}
}

