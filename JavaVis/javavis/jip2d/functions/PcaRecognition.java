package javavis.jip2d.functions;

import cern.colt.matrix.impl.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It makes a recognition step of PCA. It shows the image number with a lowest distance
 * to the input image.<br />
 * It applies to BYTE, SHORT or FLOAT type and it has to be a sequence of images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>seq</em>: The input sequence with the input views (all must have equal size).</li>
 * <li><em>DB</em>: File which has the eigenspace.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The image number with a lowest distance to the input image.</li>
 * </ul><br />
 */
public class PcaRecognition extends Function2D{
	private static final long serialVersionUID = 5342769621646467355L;

	public PcaRecognition() {
		name = "PcaRecognition";
		description = "Applies the Pca Recognition step. Applies to BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Pca;
		
		ParamFile p1 = new ParamFile("DB", false, true);
		p1.setDescription("File which has the eigenSpace");
		p1.setDefault("eigenInformation.pca");
		addParam(p1);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t = img.getType();
		if (t != ImageType.BYTE && t != ImageType.FLOAT && t != ImageType.SHORT) 
			throw new JIPException("Function Pca can not be applied to this image format.");
		
		String fileDB = getParamValueString("DB");
		
		//Get image dimensions (all views with equal dimensions)
		int width = img.getWidth(); // width
		int height = img.getHeight(); // height
		int N = width * height; // Image dimension: N
		
		/************** Get the vectorized samples set *****************/
		// Each vectorized sample is a column in the samples matrix X
		DenseDoubleMatrix1D I = new DenseDoubleMatrix1D(N);

		int n = 0;
		// For each pixel in the image
		for (int y=0; y < height; y++)
			for (int x=0; x < width; x++)  
				I.set(n++, ((JIPImgBitmap)img).getPixel(0, x, y));
		
		/************** Read the data base *****************/
		try{
			DataInputStream inFile = new DataInputStream (new FileInputStream(fileDB));
			
			// Read sizes
			int M = inFile.readInt();
			int k = inFile.readInt();
			
			// Read the mean
			DenseDoubleMatrix1D average = new DenseDoubleMatrix1D(N);
			for (n=0; n < N; n++)
				average.set(n, inFile.readDouble());
			
			// Read eigenVectors
			DenseDoubleMatrix2D eVectors = new DenseDoubleMatrix2D(N, k);
			for (int i=0; i < k; i++) 
				for (n=0; n < N; n++) 
					eVectors.set(n, i, inFile.readDouble());
			
			// Read eigenvalues
			DenseDoubleMatrix1D eValues = new DenseDoubleMatrix1D(k);
			for (int i=0; i < k; i++)
				eValues.set(i, inFile.readDouble());
			
			// Read new coordinates
			DenseDoubleMatrix2D imgCoord = new DenseDoubleMatrix2D(k, M);
			for (int m=0; m < M; m++)
				for (int i=0; i < k; i++)
					imgCoord.set(i, m, inFile.readDouble());			
			
			inFile.close();
			
			/********** Transform the original image into the eigenspace ********/
			Algebra alg = new Algebra();
			DoubleMatrix2D eVt;
			DoubleMatrix1D gNewCoordinates;
			
			// Substract the mean
			for (n=0; n < N; n++)
				I.set(n, I.get(n)-average.get(n));
			
			// Multiply by the eigenVectors
			// Transpose eVectors
			eVt = eVectors.viewDice();
			gNewCoordinates = alg.mult(eVt, I);
			
			/******** Calculate the closest original image*******/
			// We use the euclidean distance
			double distMin = distance(gNewCoordinates, imgCoord, 0);
			double distAux;
			int imgChoose = 0;
			for (int i=1; i < M ; i++) {
				distAux = distance(gNewCoordinates, imgCoord, i);
				if(distAux < distMin) {
					distMin = distAux;
					imgChoose = i;
				}
			}
			System.out.println("The image with closest distance: " + imgChoose + " Distance: " + distMin);	
		}
		catch(IOException ex){
			throw new JIPException("Some error with the input file: " + ex);
		}
		return img;
	}

	/**
	 * Euclidean distance from the input image to a given DB image.
	 * @param Ie The input image.
	 * @param IDB The DB images.
	 * @param numImg Index of the image in the DB.
	 * @return The Euclidean distance.
	 */
	private double distance(DoubleMatrix1D Ie, DenseDoubleMatrix2D IDB, int numImg) {
		double dist = 0.0;
		
		for (int i=0; i < Ie.size(); i++)
			dist += Math.pow(Ie.get(i)-IDB.get(i,numImg), 2.0);

		return Math.sqrt(dist);
	}
}

