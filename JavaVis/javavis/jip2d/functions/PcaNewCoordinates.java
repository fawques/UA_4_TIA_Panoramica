package javavis.jip2d.functions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;


import cern.colt.matrix.impl.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

/**
 * It calculates the new coordinates with Pca.<br />
 * It applies to BYTE, SHORT or FLOAT type and it has to be a sequence of images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>seq</em>: The input sequence with the input views (all must have equal size).</li>
 * <li><em>DB</em>: File which has the eigenspace.</li>
 * <li><em>perc</em>: Float value which indicates the percentage of the accumulative total (0..1) 
 * (default 0.9).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The image with the new coordinate.</li>
 * </ul><br />
 */
public class PcaNewCoordinates extends Function2D {
	private static final long serialVersionUID = -3877706238719331222L;

	public PcaNewCoordinates() {
		name = "PcaNewCoordinates";
		description = "Calculates the new coordinates with PCA. Applies to BYTE, SHORT or FLOAT type and it has to be a sequence.";
		groupFunc = FunctionGroup.Pca;
		
		ParamFile p1 = new ParamFile("DB", false, true);
		p1.setDescription("File which has the eigenSpace");
		p1.setDefault("eigenSpace.pca");
		
		ParamFloat p2 = new ParamFloat("perc", false, true);
		p2.setDescription("Percentage of the accumulative total (0..1)");
		p2.setDefault(0.9f);
		
		addParam(p1);
		addParam(p2);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException("Function Pca applies to complete sequence.");
	}
	
	public Sequence processSeq(Sequence seq) throws JIPException {
		for (int i=0; i < seq.getNumFrames(); i++) {
			ImageType t = seq.getFrame(i).getType();
			if (t != ImageType.BYTE && t != ImageType.FLOAT && t != ImageType.SHORT) 
				throw new JIPException("Function Pca can not be applied to these image format.");
		}
		
		Sequence result = seq;
		
		//Get image dimensions (all views with equal dimensions)
		int width = seq.getFrame(0).getWidth(); // width
		int height = seq.getFrame(0).getHeight(); // height
		int N = width * height; // Image dimension: N
		
		//We save the total percentage value accumulated.
		double percentage = getParamValueFloat("perc");
		
		//Read the file data: Size, average, eigenvectors and eigenvalues
		String fileDB = "eigenInformation.pca";
		String fileDB2 = getParamValueString("DB");

		try{
			// We create the file stream.
			DataInputStream inFile = new DataInputStream (new FileInputStream(fileDB));
			// First, get the number of input views (samples): M
			int M = inFile.readInt();
			
			/************** Get the vectorized samples set *****************/
			// Each vectorized sample is a column in the samples matrix X
			DenseDoubleMatrix2D X = new DenseDoubleMatrix2D(N, M);
			
	        // Each row in X
			int n = 0;
			// For each column in X
			for (int m=0; m < M; m++) {
				// For each pixel in the sample
				for (int y=0; y < height; y++)
					for (int x=0; x < width; x++)  
						// Build its associated column in X
						X.set(n++, m, ((JIPImgBitmap)seq.getFrame(m)).getPixel(0, x, y));
				n = 0;
			}			
			
			/************** Read the file data *****************/
			// Read the average
			DenseDoubleMatrix1D average = new DenseDoubleMatrix1D(N);
			for (n=0; n < N; n++)
				average.set(n, inFile.readDouble());
			
			// Read the eigenvectors
			DenseDoubleMatrix2D eVectors = new DenseDoubleMatrix2D(N, M);
			for (int m=0; m < M; m++) 
				for (n=0; n < N; n++) 
					eVectors.set(n, m, inFile.readDouble());
			
			// Read the eigenvalues
			DenseDoubleMatrix1D eValues = new DenseDoubleMatrix1D(M);
			for(int m=0; m < M; m++)
				eValues.set(m, inFile.readDouble());
			
			// Close the file stream
			inFile.close();
		
			/************** Calculate "k" *****************/
			// Election of "k" based on specified percentage.
			double accumulated = 0.0;
			double sumNValues = 0.0;
			double sumKValues = 0.0;
			int k = 0;
			
			// Add the N eigenvalues
			for (int m=0; m < M; m++)
				sumNValues += eValues.get(m);
			
			// We increase "k" until it exceeds or equals the percentage.
			for (int i=0; i < M && accumulated < percentage; i++){
				sumKValues += eValues.get(i);
				accumulated = sumKValues / sumNValues;
				k = i+1;
			}
			
			/************** Calculate the new image coordinates *****************/
			// Matrix where save all the new coordinates
			DenseDoubleMatrix2D GNC = new DenseDoubleMatrix2D(k, M);
			
			// Calculate the new image coordinates in the eigenspace
			for (int m=0; m < M; m++){
				
				// We use the matrix (G) where save the new image coordinate 
				DenseDoubleMatrix1D G = new DenseDoubleMatrix1D(N);
				
				// We copy the old values.
				G.assign(X.viewColumn(m));
			
				// We subtract the average to the original vector
				for (n=0; n < N; n++)
					G.set(n, G.get(n)-average.get(n));
				
				// Multiply the before result and the eigenvectors matrix
				Algebra alg = new Algebra();
				// Transpose eVectors
				DoubleMatrix2D eVt = eVectors.viewDice(); // k x N
				DoubleMatrix1D gNewCoordinates = alg.mult(eVt, G); // k x 1
				
				for (int i=0; i < k; i++)
					GNC.set(i, m, gNewCoordinates.get(i));	// 1 column of k x M			
			}
			
			System.out.println("Calculate new image coordinates");
			
			/************** Save in a file all the calculated data *****************/
			//Save in a file: Size, average, eigenvectors(k), eigenvalues(k), and
			//image coordinates in the new eigenspace

			//Create the file stream
			DataOutputStream outFile = new DataOutputStream (new FileOutputStream(fileDB2));
			//FileWriter out3D = new FileWriter("3DEigenSpace.txt");
			
			//Save the size (M)(k) and the average
			outFile.writeInt(M);
			outFile.writeInt(k);
			for (n=0; n < N; n++)
				outFile.writeDouble(average.get(n));

			//Save the eigenvectors in the file
			for (int i=0; i < k; i++)
				for (n=0; n < N; n++)
					outFile.writeDouble(eVectors.get(n,i));

			
			//Save the eigenvalues in the file
			for (int i=0; i < k; i++){
				outFile.writeDouble(eValues.get(i));
			}
			
			//Save the new image coordinates
			for (int m=0; m < M; m++)
				for (int i=0; i < k; i++)
					outFile.writeDouble(GNC.get(i,m));
			
			//Close the file stream
			outFile.close();
			
		}catch(IOException ex){
			throw new JIPException("PcaNewCoordinates: " + ex);
		}
		
		return result;
	}
}

