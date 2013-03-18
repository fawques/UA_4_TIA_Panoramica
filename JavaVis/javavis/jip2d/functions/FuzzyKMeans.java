package javavis.jip2d.functions;

import java.util.Arrays;
import java.lang.Math;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.RGBBandType;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies the K-means algorithm to segment a gray or color image, but in the Fuzzy 
 * way, calculating the probability to each cluster. This method divides the image into 
 * 'k' homogeneous clusters.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>number</em>: Integer value which indicates the number of clusters (default 4).</li>
 * <li><em>threshold</em>: Integer value which indicates the finishing condition (default 6).</li>
 * <li><em>iter</em>: Integer value which indicates the maximum number of iterations (default 50).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A segmented image from input image.</li>
 * </ul><br />
 */
public class FuzzyKMeans extends Function2D {
	private static final long serialVersionUID = -6916738905053544345L;
	
	public FuzzyKMeans() {
		super();
		name = "FuzzyKMeans";
		description = "Applies the fuzzy k-means algorithm. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Segmentation;

		ParamInt p1 = new ParamInt("number", false, true);
		p1.setDescription("Number of clusters");
		p1.setDefault(4);
		
		ParamInt p2 = new ParamInt("threshold", false, true);
		p2.setDescription("Threshold");
		p2.setDefault(6);
		
		ParamInt p3 = new ParamInt("iter", false, true);
		p3.setDescription("Max number of iterations");
		p3.setDefault(50);
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPImage aux = null;
		
		if (img instanceof JIPImgGeometric || img.getType() == ImageType.BIT)
			throw new JIPException("Function FuzzyKMeans can not be applied to this image format.");
		
		int numberOfClasses = getParamValueInt("number");
		if (numberOfClasses <= 0) 
			throw new JIPException("Clusters number must be greater than 0.");

		int threshold = getParamValueInt("threshold");
		if (threshold <= 0) 
			throw new JIPException("Threshold must be greater than 0.");

		int iter = getParamValueInt("iter");
		if (iter <= 0) 
			throw new JIPException("Number of iteration must be greater than 0.");
		
		if (img.getType() == ImageType.COLOR)
			aux = fuzzyKMeansColor(img, numberOfClasses, threshold, iter);
		else
			aux = fuzzyKMeansGray(img, numberOfClasses, threshold, iter);
		
		return aux;
	}

	
	/**
	 * Method which makes the FuzzyKMeans function with COLOR images.
	 * @param img The input image.
	 * @param numberOfClasses The number of classes.
	 * @param threshold The threshold value.
	 * @param kMaxIteration The maximum number of iteration.
	 * @return The processed image.
	 * @throws JIPException
	 */
	private JIPImage fuzzyKMeansColor(JIPImage img, int numberOfClasses, 
			int threshold, int kMaxIteration) throws JIPException {
		
		JIPBmpColor aux = (JIPBmpColor)img.clone();
		double[][] pixelVector = new double[3][];
		int numPixels = aux.getHeight()*aux.getWidth();;
		
		pixelVector[RGBBandType.RED.ordinal()]   = aux.getAllPixelsRed();
		pixelVector[RGBBandType.GREEN.ordinal()] = aux.getAllPixelsGreen();
		pixelVector[RGBBandType.BLUE.ordinal()]  = aux.getAllPixelsBlue();
		
		// Find initial guesses for center values
		double[][] centerValues = findInitialCenters (numberOfClasses, pixelVector);
		double[][] lastValues = new double[3][numberOfClasses];
		System.arraycopy(centerValues[0],0,lastValues[0],0,numberOfClasses);
		System.arraycopy(centerValues[1],0,lastValues[1],0,numberOfClasses);
		System.arraycopy(centerValues[2],0,lastValues[2],0,numberOfClasses);
		
		double [][]probMatrix = new double[numberOfClasses][numPixels];
		double total, auxDif;
		for (int iteration=0; iteration < kMaxIteration; iteration++) {
			// Update the progress bar
			percProgress = (int)(100*iteration/(double)kMaxIteration);
			// compute the table P(Wm / Xj) for each class _m_ and each point _j_
			for (int pixelIndex=0; pixelIndex < numPixels; pixelIndex++) {
				// get probability for current pixel and each cluster
				total=0;
				for (int classIndex=0; classIndex < numberOfClasses; classIndex++) {
					// get the distance for each channel to its center in the current pixel
					auxDif = Math.pow(pixelVector [RGBBandType.RED.ordinal()][pixelIndex] - 
							centerValues[RGBBandType.RED.ordinal()][classIndex], 2.0) +
							Math.pow(pixelVector [RGBBandType.GREEN.ordinal()][pixelIndex] - 
									centerValues[RGBBandType.GREEN.ordinal()][classIndex], 2.0) +
							Math.pow(pixelVector [RGBBandType.BLUE.ordinal()][pixelIndex] - 
									centerValues[RGBBandType.BLUE.ordinal()][classIndex], 2.0);
					
					if (auxDif == 0) probMatrix [classIndex][pixelIndex] = 1.0;
					else probMatrix [classIndex][pixelIndex] = 1.0/auxDif; 
					
					total += probMatrix [classIndex][pixelIndex];
				}
				
				// normalize probabilities
				for (int classIndex=0; classIndex < numberOfClasses; classIndex++)
					probMatrix [classIndex][pixelIndex] /= total;
			}

			double divisor, dividendR, dividendG, dividendB;
			// now compute new center
			for (int classIndex=0; classIndex < numberOfClasses; classIndex++) {
				// get the divisor and the dividend
				divisor = 0; dividendR = 0; dividendG = 0; dividendB=0; 
				for (int pixelIndex=0; pixelIndex < numPixels; pixelIndex++) {
					// dividend is P(w/x) * x 
					dividendR += probMatrix [classIndex][pixelIndex] * pixelVector [0][pixelIndex];
					dividendG += probMatrix [classIndex][pixelIndex] * pixelVector [1][pixelIndex];
					dividendB += probMatrix [classIndex][pixelIndex] * pixelVector [2][pixelIndex];
					// divisor is the sum of all probabilities
					divisor  += probMatrix [classIndex][pixelIndex]; 
				}
				// get the new center
				centerValues [0][classIndex] = (int)(dividendR / divisor);
				centerValues [1][classIndex] = (int)(dividendG / divisor);
				centerValues [2][classIndex] = (int)(dividendB / divisor);
			}

			// check for end criteria (minor changes?)
			double changes = 0;
			for (int classIndex=0; classIndex < numberOfClasses; classIndex++) {
				double value = 0;
				for (int colorIndex=0; colorIndex < 3; colorIndex++) {
					value += Math.pow(centerValues[colorIndex][classIndex] - lastValues[colorIndex][classIndex], 2.0);
					// get the new values for the next iteration
					lastValues[colorIndex][classIndex] = centerValues[colorIndex][classIndex]; 
				}
				changes += Math.sqrt (value);
			}
			if (changes < threshold)
				break;
		}

		double bestProb;
		// fit each pixel in the proper class		
		for (int i=0; i < numPixels; i++) {
			// get the maximum probability
			bestProb = 0;
			for (int classIndex=0; classIndex < numberOfClasses; classIndex++)
				if (probMatrix [classIndex][i] > bestProb) {
					pixelVector [0][i] = centerValues [0][classIndex];
					pixelVector [1][i] = centerValues [1][classIndex];
					pixelVector [2][i] = centerValues [2][classIndex];
					bestProb  = probMatrix [classIndex][i];
				}
		}
		
		aux.setAllPixelsRed (pixelVector[RGBBandType.RED.ordinal()]);
		aux.setAllPixelsGreen (pixelVector[RGBBandType.GREEN.ordinal()]);
		aux.setAllPixelsBlue (pixelVector[RGBBandType.BLUE.ordinal()]);

		return aux;
	}
	
	/**
	 * Method which makes the FuzzyKMeans function with GRAY images.
	 * @param img The input image.
	 * @param numberOfClasses The number of classes.
	 * @param threshold The threshold value.
	 * @param kMaxIteration The maximum number of iteration.
	 * @return The processed image.
	 * @throws JIPException
	 */
	private JIPImage fuzzyKMeansGray(JIPImage img, int numberOfClasses, 
			int threshold, int kMaxIteration) throws JIPException {
		JIPImgBitmap aux = (JIPImgBitmap)img.clone();
		double []pixelVector = aux.getAllPixels();
		// Find initial guesses for center values
		double []centerValues = findInitialCenters (numberOfClasses, pixelVector);
		double []lastValues = new double[centerValues.length];
		System.arraycopy(centerValues,0,lastValues,0,centerValues.length);
		double [][]probMatrix = new double[numberOfClasses][pixelVector.length];
		double total, auxDif;
		
		for (int iteration=0; iteration < kMaxIteration; iteration++) {
			// Update the progress bar
			percProgress = (int)(100*iteration/(double)kMaxIteration);
        	// compute the table P(Wm / Xj) for each class _m_ and each point _j_
        	for (int pixelIndex=0; pixelIndex < pixelVector.length; pixelIndex++) {
        		// get probability for current pixel and each cluster
        		total = 0;
        		for (int classIndex=0; classIndex < numberOfClasses; classIndex++) {
        			auxDif = pixelVector[pixelIndex] - centerValues[classIndex];
        			if (auxDif==0) probMatrix [classIndex][pixelIndex] = 1.0;
        			else probMatrix [classIndex][pixelIndex] = Math.pow(auxDif, -2.0); 
        			total += probMatrix [classIndex][pixelIndex];
        		}
        		for (int classIndex=0; classIndex < numberOfClasses; classIndex++) 
        			probMatrix [classIndex][pixelIndex] /= total;
        	}
        	
        	// now compute new center
        	for (int classIndex=0; classIndex < numberOfClasses; classIndex++) {
        		// get the divisor and the dividend
        		double divisor = 0, dividend = 0;
        		for (int pixelIndex = 0; pixelIndex < pixelVector.length; pixelIndex++) {
        			// dividend is P(w/x) * x 
        			dividend += probMatrix[classIndex][pixelIndex] * pixelVector[pixelIndex];
        			// divisor is the sum of all probabilities
        			divisor  += probMatrix[classIndex][pixelIndex];
        		}
        		// get the new center
        		centerValues [classIndex] = (int)(dividend / divisor);
        	}
        	
        	// check for end criteria (minor changes?)
        	int  changes = 0;
        	for (int classIndex=0; classIndex < numberOfClasses; classIndex++) {
        		changes += Math.abs(centerValues [classIndex] - lastValues [classIndex]);
        		// get last values
        		lastValues [classIndex] = centerValues [classIndex];
        	}
        	if (changes < threshold) 
        		break;
        }
        
		// Assigns the cluster value to each pixel
        for (int pixelIndex=0; pixelIndex < pixelVector.length; pixelIndex++) {
        	// get the maximum probability
        	double bestProb = 0;
        	for (int classIndex=0; classIndex < numberOfClasses; classIndex++)
        		if (probMatrix [classIndex][pixelIndex] > bestProb) {
        			pixelVector [pixelIndex] = centerValues [classIndex];
        			bestProb  = probMatrix [classIndex][pixelIndex];
        		}
        }
        
        aux.setAllPixels(pixelVector);
		return aux;
	}
		
	/**
	 * Find the initial centers of the clusters (for color images). Just sorts the arrays and
	 * take a guess for each cluster.
	 * @param numberOfClasses Number of classes.
	 * @param orgPixelVector Original data.
	 * @return Array with numberOfClasses guesses.
	 */
	private double[][] findInitialCenters (int numberOfClasses, double [][]orgPixelVector) {
		double [][] classesVector = new double [3][numberOfClasses];
		double []pixelVector = new double[orgPixelVector[0].length];;
		
		for (int colorIndex = 0; colorIndex < 3; colorIndex++) {
			System.arraycopy(orgPixelVector[colorIndex],0,pixelVector,0,orgPixelVector[colorIndex].length);
			// incremental sort
			Arrays.sort (pixelVector);
		
			int sep = pixelVector.length / numberOfClasses;
			for (int i=0, j=0; i < pixelVector.length && j < numberOfClasses; j++) {
				classesVector[colorIndex][j] = pixelVector[i];
				i += sep;
			}
		}
		
		return classesVector;
	}

	/**
	 * Find the initial centers of the cluster. Just sorts the arrays and
	 * take a guess for each cluster.
	 * @param numberOfClasses Number of classes.
	 * @param orgPixelVector Original data.
	 * @return Array with numberOfClasses guesses.
	 */
	private double[] findInitialCenters (int numberOfClasses, double []orgPixelVector) {
		double [] classesVector = new double[numberOfClasses];
		double [] pixelVector = new double[orgPixelVector.length];
		System.arraycopy(orgPixelVector,0,pixelVector,0,orgPixelVector.length);
		
		Arrays.sort (pixelVector); // minor to greater sort
				
		int sep = pixelVector.length / numberOfClasses;
		for (int i=0, j=0; i < pixelVector.length && j < numberOfClasses; j++) {
			classesVector[j] = pixelVector[i];
			i += sep;
		}
		return classesVector;
	}
}

