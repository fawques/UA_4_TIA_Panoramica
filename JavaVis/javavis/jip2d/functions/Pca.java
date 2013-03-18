package javavis.jip2d.functions;

import cern.colt.matrix.impl.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * It implements the learning step for Principal Component Analysis (PCA). Given a set of 
 * input views it builds the associated eigenspace and projects them into such space. This 
 * is done by computing the averaged image and the eigenvalues and eigenvectors of the 
 * covariance matrix, and then using them to perform the projection of each view.<br />
 * <em>(H. Murase and S.K. Nayar. Visual Learning and Recognition of 3-D Objects from 
 * Appearance. International Journal of Computer Vision.(1995))</em><br />
 * It applies to BYTE, SHORT or FLOAT type and it has to be a sequence of images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>seq</em>: The input sequence with the input views (all must have equal size).</li>
 * <li><em>DB</em>: File where eigenspace is saved.</li>
 * <li><em>perc</em>: Float value which indicates the percentage of the accumulative total (0..1) 
 * (default 0.9).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A multi-band image encoding the eigenspace:
 * <ul>
 * <li>(i) The first band contains the relative importance of each eigenvalue.</li>
 * <li>(ii) The second one contains the averaged image (prototype).</li>
 * <li>(iii) Each of the following bands has a eigenvector (one per each input view to 
 * simplify computations)</li>
 * <li>(iv) Finally, the following bands are associated to the projection of each input 
 * images.</li>
 * </ul></li>
 * </ul><br />
 */
public class Pca extends Function2D {
	private static final long serialVersionUID = -3266974331538240778L;

	public Pca() {
		name = "Pca";
		description = "Learning step of PCA. Applies to BYTE, SHORT or FLOAT type and it has to be a sequence.";
		groupFunc = FunctionGroup.Pca;
		
		ParamFile p1 = new ParamFile("DB", false, true);
		p1.setDescription("File where eigenspace is saved");
		p1.setDefault("eigenInformation.pca");
		
		ParamFloat p2 = new ParamFloat("perc", false, true);
		p2.setDescription("Percentage of the accumulative total (0..1)");
		p2.setDefault(0.9f);
		
		addParam(p1);
		addParam(p2);
	}

	/*
	 * Not used in this application: only processSeq.
	 */
	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException("Function Pca applies to complete sequence.");
	}

	public Sequence processSeq(Sequence seq) throws JIPException {
		for (int i=0; i < seq.getNumFrames(); i++) {
			ImageType t = seq.getFrame(i).getType();
			if (t != ImageType.BYTE && t != ImageType.FLOAT && t != ImageType.SHORT) 
				throw new JIPException("Function Pca can not be applied to these image format.");
		}
		
		// Output File, where image size, mean and eigenspace is stored
		String ficExit =  getParamValueString("DB");
		// Percentage
		double percentage = getParamValueFloat("perc");
		
		// First, get the number of input views (samples): M
		int M = seq.getNumFrames();

		//Get image dimensions (all views with equal dimensions)
		int width = seq.getFrame(0).getWidth(); // width
		int height = seq.getFrame(0).getHeight(); // height
		int N = width * height; // Image dimension: N

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
		
		/************** Get the averaged view (prototype): P ***********/
		// Initialize its associated vector (with Colt it is automatically zeroed
		DenseDoubleMatrix1D P = new DenseDoubleMatrix1D(N);

		// Get the sum of all samples in X
		for (int m=0; m < M; m++)
			for (n=0; n < N; n++)
				P.set(n, P.get(n) + X.get(n, m));

		// Normalize each component
		for (n=0; n < N; n++)
			P.set(n, P.get(n) / M);
		
		/******************* Center the samples wrt P *******************/
		// Subtract P to each sample in X.
		for (int m=0; m < M; m++)
			for (n=0; n < N; n++)
				X.set(n, m, X.get(n, m) - P.get(n));
		
		/******************* Build the Covariance Matrix ****************/
		// Transpose X
		DoubleMatrix2D Xt = X.viewDice();

		// Matrix D = XtX (covariance matrix is Q = XXt)
		Algebra alg = new Algebra();
		DoubleMatrix2D D = alg.mult(Xt,X);
		
		/******************* Obtain the eigenspace **********************/
		// SVD decomposition of D
		SingularValueDecomposition svd = new SingularValueDecomposition(D);
		
		// In this case, the eigenvalues are in the diagonal 
		double[] eigenValues = svd.getSingularValues();
		
		// Eigenvectors of D are the columns of V
		DoubleMatrix2D V = svd.getV();
		
		// First M eigenvectors of the covariance matrix Q are given by XV divided by 
		// the corresponding eigenvalue 
		// 1)Divide each column of V (with dimension NxN) by the corresponding eigenvalue
		for (int m=0; m < M; m++)
			for (int p=0; p < M; p++) 
				V.set(m,p, V.get(m,p)/Math.sqrt(eigenValues[p]));
		
		// Then multiply X and the corrected V. Such a multiplication is of order NxMxMxM = NXM
		DoubleMatrix2D EQ = alg.mult(X,V);
		
		/******************* Store the eigenspace **********************/
        // Store the eigenvectors in additional bands
        // They are scaled for visualization purposes only
        // First Eigenvectors of Q are in the columns of EQ
		Sequence result = new Sequence();
		JIPImgBitmap output = (JIPImgBitmap)JIPImage.newImage(M + 2, width, height, ImageType.SHORT);
				
		/************** Calculate "k" *****************/
		// Election of "k" based on specified percentage.
		double accumulated = 0.0;
		double sumNValues = 0.0;
		double sumKValues = 0.0;
		int k = 0;
		
		// Add the N eigenvalues
		for(int m=0; m < M; m++)
			sumNValues += eigenValues[m];
		
		// We increase "k" until it exceeds or equals the percentage.
		for(int i=0; i < M && accumulated < percentage; i++){
			sumKValues += eigenValues[i];
			accumulated = sumKValues / sumNValues;
			k = i+1;
		}

		try{
			DataOutputStream outFile = new DataOutputStream (new FileOutputStream(ficExit));
			
			// Stores the number of samples
			outFile.writeInt(M);
			outFile.writeInt(k);
			
			// Stores the mean
			for (n=0; n < N; n++)
				outFile.writeDouble(P.get(n));
			
			double aux[] = new double[N];
			double aux2[] = new double[N]; 
			double val = 0.0; 
			for (int m=0; m < k; m++) {
				for (n=0; n < N; n++) {
					val = EQ.get(n,m);
					
					// Store the eigenvectors in the output file
					outFile.writeDouble(val);
					
					// aux y aux2 are used for creating the output image
					// where it represents the space created.
					aux2[n] = val;  
					aux[n] = val*65535 + 20000;
				}
				output.setAllPixels(m + 2, aux);
			}
			
			// Store the eigenValues
			for (int p=0; p < k; p++)
				outFile.writeDouble(eigenValues[p]);
			
			outFile.close();
		}catch(IOException ex){
			throw new JIPException("Some error using the output file: " + ex);
		}
		
		// Instead of storing the eigenvalues, we store in the first band
		// the relative importance of each eigenvalue, which is more useful 
		// for recognition purposes
		
		// Total variability
		double total = 0.0d;
		for (int i=0; i < M; i++)
			total = total + eigenValues[i];
		// Store both the relative importance (first band) and prototype (second)
		int a = 0; 
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// For eigenvalues consider only the first M (others are neligible)
				if (a < M)
					output.setPixel(0, x, y, (eigenValues[a] / total) * 65535);
				// For the prototype
				output.setPixel(1, x, y, ((P.get(a) + 600) / 1200) * 65535);
				a++;
			}
		}
		
		/******************* Store the eigenspace **********************/
		result.addFrame(output);
		result.setName(seq.getName());
		
		return result;
	}
}

