package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It applies a smoothing to the image. Two different algorithms can be applied: Average or 
 * Median. Average algorithm uses the ConvolveImage method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>radius</em>: Integer value which indicates the radius of the window (default 2).</li>
 * <li><em>alg</em>: Algorithm to smooth: Average or Median (default Average).</li>
 * <li><em>method</em>: List of methods that deals with the borders (default ZERO). 
 * <ul>
 * <li><em>ZERO</em>: Border pixels are marked as 0.</li>
 * <li><em>PAD</em>: The first row is duplicated so that the -1 row is the same. The same for 
 * last row and first and last columns.</li> 
 * <li><em>WRAP</em>: The -1 row is the last row and the n+1 row is the first. The same for 
 * columns.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same input image type.</li>
 * </ul><br />
 */
public class Smooth extends Function2D {
	private static final long serialVersionUID = 7850747295611493227L;

	public Smooth() {
		super();
		name = "Smooth";
		description = "Applies a smoothing to the image with Average or Median method. It applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Adjustment;

		ParamInt p1 = new ParamInt("radius", false, true);
		p1.setDefault(2);
		p1.setDescription("Radius");
		
		ParamList p2 = new ParamList("alg", false, true);
		String []paux = new String[2];
		paux[0] = "Average";
		paux[1] = "Median";
		p2.setDefault(paux);
		p2.setDescription("Method to smooth");
		
		ParamList p3 = new ParamList("method", false, true);
		paux = new String[3];
		paux[0] = "ZERO";
		paux[1] = "WRAP";
		paux[2] = "PAD";
		p3.setDefault(paux);
		p3.setDescription("Method to process the border");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {	
		if (img instanceof JIPImgGeometric || img.getType() == ImageType.BIT) 
			throw new JIPException("Function Smooth can not be applied to this image format.");

		int radius = getParamValueInt("radius");
		String method = getParamValueString("method");
		String alg = getParamValueString("alg");

		if (radius < 0)
			throw new JIPException ("Radius has to be greater or equal than 0.");
		
		if (alg.equals("Average")) {
			Function2D convolution = new ConvolveImage();
			int tam = radius * 2 + 1;
			double mat[] = new double[tam * tam];
			
			for (int a = 0; a < tam * tam; a++)
				mat[a] = 1.0f;
			
			JIPImgBitmap convo = (JIPImgBitmap)JIPImage.newImage(radius * 2 + 1, radius * 2 + 1, ImageType.FLOAT);
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			convolution.setParamValue("div", (float) ((radius * 2 + 1) * (radius * 2 + 1)));
			convolution.setParamValue("method", method);
			
			return convolution.processImg(img);
		}
		else if (alg.equals("Median")) {
			JIPImgBitmap imgBmp = (JIPImgBitmap)img;
			int numBands = imgBmp.getNumBands();
			int width = imgBmp.getWidth();
			int height = imgBmp.getHeight();
			JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, imgBmp.getType());
			double[] bmpf, binf, vectorf;
			
			for (int nb=0; nb < numBands; nb++) {
				bmpf = imgBmp.getAllPixels(nb);
				binf = new double[width * height];
				vectorf = new double[(radius + radius + 1) * (radius + radius + 1)];
				for (int h=0; h < height; h++)
					for (int w=0; w < width; w++) {
						int initX = w - radius, endX = w + radius;
						int initY = h - radius, endY = h + radius;
						if (initX < 0)
							initX = 0;
						if (endX >= width)
							endX = width - 1;
						if (initY < 0)
							initY = 0;
						if (endY >= height)
							endY = height - 1;
						int count = 0;
						for (int y=initY; y <= endY; y++)
							for (int x=initX; x <= endX; count++, x++)
								vectorf[count] = bmpf[x + y * width];
						binf[w + h * width] = median(vectorf, count - 1);
					}
				res.setAllPixels(nb, binf);
			}
			return res;
		}
		else throw new JIPException("Smooth: algorithm to apply not recognized");
	}

	/**
	 * It calculates the median in the elements of a type vector. We have reimplemented 
	 * the Arrays.sort because for short vectors it is more efficient.
	 * @param vector Vector which has the elements.
	 * @param length Length to use.
	 * @return The median of the input elements.
	 */
	public double median(double vector[], int length) {
		int k = length / 2;
		int left = 0;
		int right = length;
		double x = 0, aux;
		int i, j;
		while (left < right) {
			x = vector[k];
			i = left;
			j = right;
			do {
				while (vector[i] < x)
					i++;
				while (x < vector[j])
					j--;
				if (i <= j) {
					aux = vector[i];
					vector[i] = vector[j];
					vector[j] = aux;
					i++;
					j--;
				}
			}while (i <= j);
			
			if (j < k)
				left = i;
			if (k < i)
				right = j;
		}
		return x;
	}
}

