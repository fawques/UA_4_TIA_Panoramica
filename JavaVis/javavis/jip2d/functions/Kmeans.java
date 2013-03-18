package javavis.jip2d.functions;

import java.util.Random;
import java.util.Arrays;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies the K-means algorithm to segment a gray or color image. We must provide 
 * the number of cluster to find. This method divides the image into 'k' homogeneous 
 * clusters.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>number</em>: Integer value which indicates the number of clusters (default 4).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A segmented image from input image.</li>
 * </ul><br />
 */
public class Kmeans extends Function2D {
	private static final long serialVersionUID = -2321246194138087467L;

	public Kmeans() {
		super();
		name = "Kmeans";
		description = "Applies the K-means algorithm. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Segmentation;

		ParamInt p1 = new ParamInt("number", false, true);
		p1.setDefault(4);
		p1.setDescription("Number of clusters");
		
		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPImage aux;
		int sets = getParamValueInt("number");
		if (sets <= 0) 
			throw new JIPException("Clusters number must be greater than 0");
		if (img instanceof JIPImgGeometric || img instanceof JIPBmpBit)
			throw new JIPException("Function Kmeans can not be applied to this image type.");
		
		if (img.getType() == ImageType.COLOR)
			aux = kmeans((JIPBmpColor)img, sets); 
		else 
			aux = kmeans((JIPImgBitmap)img, sets);
		
		return aux;
	}
	
	
	/**
	 * Method which calculates the kmeans.
	 * @param img The input image.
	 * @param sets The sets.
	 * @return The result.
	 * @throws JIPException
	 */
	private JIPImage kmeans (JIPBmpColor img, int sets) throws JIPException {		
		JIPBmpColor aux = (JIPBmpColor)img.clone();
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		double [] valuesR = new double [sets];	 //contains clusters values of actual iteration (RED)
		double [] valuesG = new double [sets];	//contains clusters values of actual iteration (GREEN)
		double [] valuesB = new double [sets];	//contains clusters values of actual iteration (BLUE)
		double [] valuePrevR = new double [sets];	//contains clusters values of previous iteration (RED)
		double [] valuePrevG = new double [sets];	//contains clusters values of previous iteration (GREEN)
		double [] valuePrevB = new double [sets];	//contains clusters values of previous iteration (BLUE)
		double [] valuePixelR;	//the original value of each pixel of image of the RED band.(they are never modified)
		double [] valuePixelG;	//the original value of each pixel of image of the GREEN band. (they are never modified)
		double [] valuePixelB;	//the original value of each pixel of image of the BLUE band. (they are never modified)
		int [] cluster = new int [width*height];		//It contains the class to which each pixel belongs
		double [] valueFinalR = new double [width*height]; //the final value of each pixel of the RED band
		double [] valueFinalG = new double [width*height]; //the final value of each pixel of the GREEN band
		double [] valueFinalB = new double [width*height]; //the final value of each pixel of the BLUE band
		boolean exit = true;
		double diff = 0;
		double min_dif;
		
		//It initializes the values of the classes so that these values are
		//uniformly distributed
		searchValues(sets, img, valuesR, valuesG, valuesB);
				
		for (int i=0; i < sets; i++) {
			valuePrevR[i] = 0;
			valuePrevG[i] = 0;
			valuePrevB[i] = 0;
		}
		
		valuePixelR = img.getAllPixelsRed();
		valuePixelG = img.getAllPixelsGreen();
		valuePixelB = img.getAllPixelsBlue();
		
		double dif1 = 0.0;
		double dif2 = 0.0;
		double dif3 = 0.0;
		double sumPow = 0.0;
		int elems[] = new int[sets];
				
		do {
			//it examines each pixel of the image and assigns 
			//it to one of the clusters depending on the minimum distance
			for (int i=0; i < valuePixelR.length; i++){
				min_dif = Double.MAX_VALUE;
				for (int j=0; j < valuesR.length; j++) {
					dif1 = Math.abs(valuePixelR[i] - valuesR[j]);
					dif2 = Math.abs(valuePixelG[i] - valuesG[j]);
					dif3 = Math.abs(valuePixelB[i] - valuesB[j]);
					sumPow = Math.pow(dif1, 2) + Math.pow(dif2, 2) + Math.pow(dif3, 2);
					diff = Math.sqrt(sumPow);
					if (diff < min_dif){
						min_dif = diff;
						cluster[i] = j;
					}
				}
			}
			
			System.arraycopy(valuesR,0,valuePrevR,0,sets);
			System.arraycopy(valuesG,0,valuePrevG,0,sets);
			System.arraycopy(valuesB,0,valuePrevB,0,sets);
			Arrays.fill(valuesR,0);
			Arrays.fill(valuesG,0);
			Arrays.fill(valuesB,0);
			Arrays.fill(elems,0);
			
			for (int j=0; j < cluster.length; j++) {
				valuesR[cluster[j]] += valuePixelR[j];
				valuesG[cluster[j]] += valuePixelG[j];
				valuesB[cluster[j]] += valuePixelB[j];
				elems[cluster[j]]++;
			}
			
			//the centers of the classes are recalculated
			for (int j=0; j < valuesR.length; j++)
				if (elems[j] == 0) {//it avoids that no class has 0 elements
					Random rand = new Random(); //it chooses random coordinates
					int x = rand.nextInt(width) ;
					int y = rand.nextInt(height);
					valuesR[j] = img.getPixelRed(x,y);
					valuesG[j] = img.getPixelGreen(x,y);
					valuesB[j] = img.getPixelBlue(x,y);
					elems[j]++;
				}
				else {
					valuesR[j] /= elems[j];
					valuesG[j] /= elems[j];
					valuesB[j] /= elems[j];
				}
			
			exit = true;
			for (int i=0; i < sets; i++) { 
				if (valuesR[i] != valuePrevR[i]){
					exit = false;
					break;
				}
			}
		} while (!exit);
		
		for (int i=0; i < valueFinalR.length; i++) {
			valueFinalR[i] = valuesR[cluster[i]];
			valueFinalG[i] = valuesG[cluster[i]];
			valueFinalB[i] = valuesB[cluster[i]];
		}
		
		aux.setAllPixelsBlue(valueFinalB);
		aux.setAllPixelsGreen(valueFinalG);
		aux.setAllPixelsRed(valueFinalR);
		
		return aux;
	}
	
	/**
	 * Method which searches values.
	 * @param classes The classes.
	 * @param img The input image.
	 * @param valuesR The values of R band.
	 * @param valuesG The values of G band.
	 * @param valuesB The values of B band.
	 * @throws JIPException
	 */
	private void searchValues(int classes, JIPBmpColor img, double[]valuesR, double[]valuesG, 
			double[]valuesB) throws JIPException {
		double [] vectorR = img.getAllPixelsRed();
		double [] vectorG = img.getAllPixelsGreen();
		double [] vectorB = img.getAllPixelsBlue();
		
		//it orders of minor to greater, we only ordered the RED Array
		//the other vectors, are ordered according to the RED vector
		Arrays.sort (vectorR);
		Arrays.sort (vectorG);
		Arrays.sort (vectorB);
				
		int sep = vectorR.length/classes;
		int j = 0, i = 0;
		while (i < vectorR.length && j < classes) {
			valuesR[j] = vectorR[i]; valuesG[j] = vectorG[i]; valuesB[j] = vectorB[i];
			i = i + sep;
			j++;
		}
	}
	
	/**
	 * Method which calculates the kmeans.
	 * @param img The input image.
	 * @param sets The sets.
	 * @return The result.
	 * @throws JIPException
	 */
	private JIPImage kmeans(JIPImgBitmap img, int sets) throws JIPException {
		JIPImgBitmap aux = (JIPImgBitmap)img.clone();
		int width = img.getWidth();
		int height = img.getHeight();
				
		double []value;	//contains clusters values of actual iteration 
		double []valuePrev = new double [sets];	//contains clusters values of previous iteration 
		double [] valuePixel;	//the original value of each pixel of image.  (they are never modified)
		int [] cluster = new int [width*height];	//It contains the class to which each pixel belongs
		double [] valueFinal = new double [width*height]; //the final value of each pixel
		boolean exit = true;
		double diff = 0;
		double min_dif;
		int min_class = 0;
				
		//it initializes the values of the classes so that these values are
		//uniformly distributed
		value = searchValues(sets, img);  	
		
		for (int i=0; i < sets; i++)
			valuePrev[i]=0;
		
		valuePixel = img.getAllPixels();
		
		int elems[] = new int[sets];
		
		do {
			//it examines each pixel of the image and assigns 
			//it to one of the clusters depending on the minimum distance
			for (int i=0; i < valuePixel.length; i++){
				min_dif = Double.MAX_VALUE;
				for (int j=0; j < value.length; j++) {
					diff = Math.abs(valuePixel[i]-value[j]);
					if (diff < min_dif){
						min_dif = diff;
						min_class = j;
					}
				}
				cluster[i] = min_class;							
			}
			System.arraycopy(value,0,valuePrev,0,sets);
			Arrays.fill(value,0);
			Arrays.fill(elems,0);
			
			for (int j=0; j < valuePixel.length; j++) {
				value[cluster[j]] += valuePixel[j];
				elems[cluster[j]]++;
			}
			
			//the centers of the classes are recalculated
			for (int j=0; j < sets; j++)
				if (elems[j] == 0) { //it avoids that no class has 0 elements
					Random rand = new Random();
					int x = rand.nextInt(width) ;//it chooses random coordinates
					int y = rand.nextInt(height);
					value[j] = img.getPixel(x,y);
					elems[j]++;
				}
				else value[j] /= elems[j];
			
			exit = true;
			for (int i=0; i < sets; i++) 
				if (value[i] != valuePrev[i]){
					exit = false;
					break;
				}
		} while (!exit);
		
		for (int i=0; i < valueFinal.length; i++)
			valueFinal[i] = value[cluster[i]];
		
		aux.setAllPixels(valueFinal);
		
		return aux;
	}
	
	/**
	 * Method which searches values.
	 * @param classes The classes.
	 * @param img The input image.
	 * @return The data of the searching.
	 * @throws JIPException
	 */
	private double[] searchValues (int classes, JIPImgBitmap img) throws JIPException {
		double [] vectorClasses = new double [classes];
		double [] vector = img.getAllPixels();
		
		//it orders from minor to greater, 
		Arrays.sort (vector);
		
		int sep = vector.length/classes;
		int j = 0, i = 0;
		
		while (i < vector.length&& j < classes) {
			vectorClasses[j] = vector[i];
			i = i + sep;
			j++;
		}
		return vectorClasses;
	}
}

