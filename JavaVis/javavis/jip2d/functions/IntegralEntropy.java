package javavis.jip2d.functions;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It calculates integral entropy image.<br />
 * It applies to BYTE type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE type.</li>
 * <li><em>percentage</em>: Real value which indicates the percentage of entropy (default 0.25).</li>
 * <li><em>bins</em>: Integer value which indicates the number of bins (default 256).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The image does not change, but it shows the integral entropy data by Console.</li>
 * </ul><br />
 */
public class IntegralEntropy extends Function2D {
	private static final long serialVersionUID = -7864060115649821642L;

	public IntegralEntropy() {
		super();
		name = "IntegralEntropy";
		description = "Calculates integral entropy image. Applies to BYTE type.";
		groupFunc = FunctionGroup.FeatureExtract;
		
		ParamFloat perct = new ParamFloat("percentage", false, true);
		perct.setDefault(0.25f);
		
		ParamInt bins = new ParamInt("bins", false, true);
		bins.setDefault(256);

		addParam(perct);
		addParam(bins);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.BYTE) {
			int width = img.getWidth();
			int height = img.getHeight();
			double[] bmp;
			double aux, minValue, maxValue;
			int x_min = -1, y_min = -1;
			int x_max = -1, y_max = -1;
			int[] H11, H12, H21, H22;
			int bins = getParamValueInt("bins");
			int x, y;
			double p11, p12, p21, p22;
			double sump11, sump12, sump21, sump22;

			bmp = ((JIPImgBitmap)img).getAllPixels();
			
			int[][] H = new int[bmp.length][];
			int[][] R = new int[bmp.length][];
			
			// Calculates the Integral Histogram
			for (int w=1; w < width; w++) { // Must calculate the first row and first column
				R[w] = new int[bins];
				R[w][findBin(bmp[w], bins)]++;
			}
			H[0] = new int[bins];
			H[0][findBin(bmp[0], bins)]++;
			for (int h=1; h < height; h++) {
				H[h*width] = H[(h-1)*width].clone();
				H[h*width][findBin(bmp[h*width], bins)]++;
			}	
			for (int p=width+1; p < bmp.length; p++) {
				x = p%width;
				if (x >= 1) {
					y = p/width;
					R[p] = R[(y-1)*width+x].clone();
					H[p] = H[p-1].clone();
					for (int i=0; i < bins; i++) {
						H[p][i] += R[p][i];
					}
					R[p][findBin(bmp[p], bins)]++;
					H[p][findBin(bmp[p], bins)]++;
				}
			}
			
			minValue = Double.MAX_VALUE;
			maxValue = Double.MIN_VALUE;
			H11 = new int[bins];
			H12 = new int[bins];
			H21 = new int[bins];
			H22 = new int[bins];
			// Now, look for the minimum value
			for (int p=width+1; p < bmp.length; p++) {
				x = p%width;
				y = p/width;
				if (x >= 1 && x < width-1 && y < height-1) {
					H11 = H[p].clone();
					for (int i=0; i < bins; i++) {
						H12[i] = H[y*width+width-1][i]-H11[i];
						H21[i] = H[(height-1)*width+x][i]-H11[i];
						H22[i] = H[height*width-1][i]-(H11[i]+H12[i]+H21[i]);
					}
					p11 = 0.0;
					p12 = 0.0;
					p21 = 0.0;
					p22 = 0.0;
					sump11 = (x+1)*(y+1);
					sump12 = (y+1)*width-sump11;
					sump21 = (x+1)*height-sump11;
					sump22 = width*height-(sump11+sump12+sump21);
					for (int i=0; i < bins; i++) {
						p11 -= (H11[i]==0?0.0:(H11[i]/sump11)*Math.log(H11[i]/sump11));  
						p12 -= (H12[i]==0?0.0:(H12[i]/sump12)*Math.log(H12[i]/sump12));  
						p21 -= (H21[i]==0?0.0:(H21[i]/sump21)*Math.log(H21[i]/sump21));  
						p22 -= (H22[i]==0?0.0:(H22[i]/sump22)*Math.log(H22[i]/sump22));  
					}
					aux = p11/sump11+p12/sump12+p21/sump21+p22/sump22;
					if (aux < minValue) {
						x_min = x;
						y_min = y;
						minValue = aux;
					}
					if (aux > maxValue) {
						x_max = x;
						y_max = y;
						maxValue = aux;
					}
				}
			}
			System.out.println("x="+x_min+" y="+y_min+" minValue="+minValue);
			System.out.println("x="+x_max+" y="+y_max+" maxValue="+maxValue);
			
			return img;
		}
		else 
			throw new JIPException("Function IntegralImage can not be applied to this image type.");
	}
	
	
	/**
	 * Method which finds a bin.
	 * @param value The value.
	 * @param numBins The number of bins.
	 * @return The result.
	 */
	private final int findBin (double value, int numBins) {
		return (int)(value*numBins/256.0);
	}
}

