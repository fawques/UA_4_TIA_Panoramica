package javavis.jip2d.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;
import javavis.jip2d.functions.adaBoost.adaBoost;
import javavis.jip2d.functions.adaBoost.feature;
import javavis.jip2d.functions.adaBoost.strongLearner;
import javavis.jip2d.functions.adaBoost.trainExample;

/**
 * It detects faces in an image (training).<br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * <li><em>Face Train Set</em>: Folder with face images for training.</li>
 * <li><em>Non Face Train Set</em>: Folder with non-face images for training.</li>
 * <li><em>AdaBoost</em>: File with training result.</li>
 * <li><em>MaxIterations</em>: "Maximum number of iterations for AdaBoost.</li>
 * <li><em>threads</em>: Number of parallel threads used in training.</li>
 * <li><em>Face Test Set</em>: Folder with face images for training.</li>
 * <li><em>Non Face Test Set</em>: Folder with non-face images for training.</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>StrongLearner</em>: The StrongLearner object.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image.</li>
 * </ul><br />
 */
public class FaceDetectorTrain extends Function2D {

	private static final long serialVersionUID = 7623912995598488525L;
	
	private static final int TOTAL_TYPE = 6;
	
	ArrayList<feature> weakLearners;
	public int xSize, ySize; //learning images size 
	double progress;
	adaBoost boostingObject;
	
	public FaceDetectorTrain() {
		this.name = "FaceDetector - Training";
		this.description = "Detects faces in an image - Training.";
		this.groupFunc = FunctionGroup.Applic;
		
		weakLearners = new ArrayList<feature>();
		progress = 0;
		
		ParamDir p1 = new ParamDir("Face Train Set", false, true);
		p1.setDescription("Folder with face images for training");
		p1.setValue("/home/dviejo/tmp/TIA/faces/train/face/jpg");
		
		ParamDir p2 = new ParamDir("Non Face Train Set", false, true);
		p2.setDescription("Folder with non-face images for training");
		p2.setDefault("/home/dviejo/tmp/TIA/faces/train/non-face/jpg");
		
		ParamFile p3 = new ParamFile("AdaBoost", false, true);
		p3.setDescription("Training result");
		
		ParamInt p4 = new ParamInt("MaxIterations", false, true);
		p4.setDescription("Maximum number of iterations for AdaBoost");
		p4.setDefault(1);
		
		ParamObject p5 = new ParamObject("StrongLearner", false, false);
		
		ParamInt p6 =new ParamInt("threads", false, true);
		p6.setDefault(4);
		p6.setDescription("Number of parallel threads used in training");

		ParamDir p7 = new ParamDir("Face Test Set", false, true);
		p7.setDescription("Folder with face images for training");
		
		ParamDir p8 = new ParamDir("Non Face Test Set", false, true);
		p8.setDescription("Folder with non-face images for training");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
		addParam(p8);
	}

	/* (non-Javadoc)
	 * @see javavis.jip2d.base.Function2D#processImg(javavis.jip2d.base.JIPImage)
	 */
	@Override
	public JIPImage processImg(JIPImage img) throws JIPException {
		ArrayList<trainExample> trainSet = new ArrayList<trainExample>();
		
		int numIterations = this.getParamValueInt("MaxIterations");
		String boostingFile = this.getParamValueString("AdaBoost");
		int numThreads = this.getParamValueInt("threads");
		
		String facetrainsetName = this.getParamValueString("Face Train Set");
		int numFaces, numNonFaces;
		String path;
		File trainsetfolder = new File(facetrainsetName);
		if (!trainsetfolder.isDirectory()) throw new JIPException("Face set is not a folder");
		File []trainsetNames = trainsetfolder.listFiles();
		numFaces = trainsetNames.length;
		
		// we reed all the images from the dictory and we include them in trainSet
		for (File fpath: trainsetNames)
		{
			path = fpath.getAbsolutePath();
			if (path.endsWith(".jpg") || path.endsWith(".gif"))
				try {
					trainSet.add(new trainExample(1, path)); //1 for faces
				} catch(Exception e)
				{
					System.out.println("Error openning "+fpath.getAbsolutePath()+"\n"+e);
				}
		}
		String nonfacetrainsetName = this.getParamValueString("Non Face Train Set");
		trainsetfolder = new File(nonfacetrainsetName);
		
		if (!trainsetfolder.isDirectory()) throw new JIPException("Non Face set is not a folder");
		trainsetNames = trainsetfolder.listFiles();
		numNonFaces = trainsetNames.length;
		for (File fpath: trainsetNames)
		{
			path = fpath.getAbsolutePath();
			if (path.endsWith(".jpg") || path.endsWith(".gif"))
				try {
					trainSet.add(new trainExample(0, path)); //0 for non faces
				} catch(Exception e)
				{
					System.out.println("Error openning " + fpath.getAbsolutePath() + "\n" + e);
				}
		}
		System.out.println("Number of images in the training set: " + trainSet.size() + " (" + numFaces + ", " + numNonFaces + ")");
		
		xSize = trainSet.get(0).integralImage.getWidth();
		ySize = trainSet.get(0).integralImage.getHeight();
		
		boolean existsBoostingFile = true;
		
		//Try to recover adaBoost object from file
		if (!readResult(boostingFile))
		{
			System.out.println("File " + boostingFile + " not found. Creating weak classifiers");
			generateFeatures(xSize, ySize);
			System.out.println("Number of features for an image of " + xSize + "x" + ySize + " : " + weakLearners.size());
			boostingObject = new adaBoost(numFaces, numNonFaces, xSize, ySize, weakLearners);
			existsBoostingFile = false;
		}

		System.out.println("Starting AdaBoost training");
		boostingObject.adaBoostTrain(trainSet, numIterations, numThreads);
		
		if (existsBoostingFile)
			writeResult(boostingFile);
		else
			writeResult("trainingResultsFile");
		
		//set up output parameter
		this.setParamValue("StrongLearner", boostingObject.result);
				
		//perform a full test
		ArrayList<trainExample> testSet = new ArrayList<trainExample>();
		int numTestFaces, numTestNonFaces;
		String facetestSetName = this.getParamValueString("Face Test Set");
		File testSetfolder = new File(facetestSetName);
		if (!testSetfolder.isDirectory()) throw new JIPException("Test Face set is not a folder");
		File []testsetNames = testSetfolder.listFiles();
		numTestFaces = testsetNames.length;
		
		// we reed all the images from the directory and we include them in trainSet
		for (File fpath: testsetNames)
		{
			path = fpath.getAbsolutePath();
			if (path.endsWith(".jpg") || path.endsWith(".gif"))
				try {
					testSet.add(new trainExample(1, path)); //1 for faces
				} catch(Exception e)
				{
					System.out.println("Error openning "+fpath.getAbsolutePath()+"\n"+e);
				}
		}
		String nonfacetestSetName = this.getParamValueString("Non Face Test Set");
		testSetfolder = new File(nonfacetestSetName);
		if (!testSetfolder.isDirectory()) throw new JIPException("Test Face set is not a folder");
		testsetNames = testSetfolder.listFiles();
		numTestNonFaces = testsetNames.length;
		
		// we reed all the images from the directory and we include them in trainSet
		for (File fpath: testsetNames)
		{
			path = fpath.getAbsolutePath();
			if (path.endsWith(".jpg") || path.endsWith(".gif"))
				try {
					testSet.add(new trainExample(0, path)); //1 for faces
				} catch(Exception e)
				{
					System.out.println("Error openning "+fpath.getAbsolutePath()+"\n"+e);
				}
		}
		System.out.println("Number of images in the training set: " + testSet.size() + " ("+numTestFaces+", " + numTestNonFaces + ")");
		System.out.println("Test set results: ");
		boostingObject.testSet(testSet);

		return testImage(boostingObject.result, img);
	}
	

	/**
	 * Method which test an image for detecting faces.
	 * @param learner the classification object obtained from training
	 * @param img the image to detect faces
	 * @return the image result
	 * @throws JIPException
	 */
	private JIPImage testImage(strongLearner learner, JIPImage img) throws JIPException
	{
		int rows = img.getHeight();
		int columns = img.getWidth();
		JIPGeomSegment res = new JIPGeomSegment(columns, rows);
		int row, col;
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
	 * @param filename the file which contains the information
	 * @return a boolean value indicating true if it has been possible to read the file
	 * @throws JIPException
	 */
	private boolean readResult(String filename)
	{
		boolean res = true;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			boostingObject = (adaBoost)in.readObject();
		} catch(IOException ioe)
		{
			res = false;
		}catch (ClassNotFoundException cnfe)
		{
			res = false;
		}
		return res;
	}
	
	
	/**
	 * Method which writes into the file the information about the classification
	 * object obtained from training.
	 * @param filename the file which will contains the information
	 * @throws JIPException
	 */
	private void writeResult(String filename)
	{
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(filename);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(boostingObject);
		}
		catch (Exception e) {
			System.out.println("It has not been possible save the training result in the file.");
			System.out.println(e);
		}
		finally {
			try {
				if (oos != null && fos != null) {
					oos.close();
					fos.close();
				}
			}
			catch (Exception e) {System.out.println(e);}
		}
	}
	
	
	/**
	 * Method which generates features.
	 * @param xSize the x size
	 * @param ySize the y size
	 * @throws JIPException
	 */
	private void generateFeatures(int xSize, int ySize) throws JIPException
	{
		//we create features of 1x1 size until width x height (from the image). 
		//we distribute the image for each possible type of each size.
		int width, height, type;
		int i, j, popx, popy;

		feature classifier;
		
		for (height=1; height < ySize; height++)
		{
			for (width=1; width < xSize; width++)
			{
				for (type=1; type < TOTAL_TYPE; type++)
					try
					{
						classifier = new feature(width, height, type);
						//populate across the rest of the image
						popx = xSize - width + 1;
						popy = ySize - height + 1;
						for (j=0; j < popy; j++)
						{
							for (i=0; i < popx; i++)
								weakLearners.add(new feature(i, j, classifier));
						}
					} catch(Exception e){}
			}
		}	
	}
}

