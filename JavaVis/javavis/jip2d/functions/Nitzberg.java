package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Point2D;

/**
 * It implements the Nitzberg method for edge and corner detection. When a pixel value 
 * is greater than the threshold, it is considered as a corner.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>thres</em>: Real value which indicates the value of the threshold for corner 
 * calculation (default 1.0).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A POINT image, where each point is a corner.</li>
 * </ul><br />
 */
public class Nitzberg extends Function2D {
	private static final long serialVersionUID = -5402145479686371334L;
	
	public Nitzberg() {
		super();
		name = "Nitzberg";
		description = "Applies the Nitzberg method for edge and corner detection. Applies to bitmap images.";
		groupFunc = FunctionGroup.Edges;

		ParamFloat p1 = new ParamFloat("thres", false, true);
		p1.setDescription("Threshold for corner calculation");
		p1.setDefault(1.0f);

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric)
			throw new JIPException("Function Nitzberg can not be applied to this image format.");
		
		JIPImgBitmap smooth = null;
		float threshold = getParamValueFloat("thres") * 0.0000001f;
		int n_cols = img.getWidth();
		int n_rows = img.getHeight();

		JIPImgBitmap ix2 = (JIPImgBitmap)JIPImage.newImage(n_cols, n_rows, ImageType.FLOAT);
		JIPImgBitmap ixy = (JIPImgBitmap)JIPImage.newImage(n_cols, n_rows, ImageType.FLOAT);
		JIPImgBitmap iy2 = (JIPImgBitmap)JIPImage.newImage(n_cols, n_rows, ImageType.FLOAT);
		JIPImgBitmap edgy = (JIPImgBitmap)JIPImage.newImage(n_cols, n_rows, ImageType.FLOAT);
		JIPImgBitmap corn = (JIPImgBitmap)JIPImage.newImage(n_cols, n_rows, ImageType.FLOAT);
		JIPGeomPoint dest_edges = (JIPGeomPoint)JIPImage.newImage(n_cols, n_rows, ImageType.POINT);

		// 1-d gaussian kernels
		double[] filter = new double[7];
		//filter[0]=filter[8]=0.0003677;
		filter[0]=filter[6]=0.0077312;
		filter[1]=filter[5]=0.066709;
		filter[2]=filter[4]=0.2408;
		filter[3]=0.36876;

		// Convert color to grayscale and from [0:255] to [0:1]
		if (img.getType()==ImageType.COLOR) {
			ColorToGray colorToGray = new ColorToGray();
			colorToGray.setParamValue("gray", "FLOAT");
			smooth = (JIPImgBitmap)colorToGray.processImg(img);
		}
		else {
			if (img.getType()==ImageType.BYTE || img.getType()==ImageType.BIT 
					|| img.getType()==ImageType.SHORT) {
				GrayToGray grayToGray = new GrayToGray();
				grayToGray.setParamValue("gray", "FLOAT");
				smooth = (JIPImgBitmap)grayToGray.processImg(img);
			}
			else if (img.getType()==ImageType.FLOAT) smooth = (JIPImgBitmap)img; 
				 else 
					throw new JIPException("Function Nitzberg can not be applied to this image format.");
		}
		
		// Eliminate the firsts and last columns and rows to avoid corner near these lines
		double ix, iy;
		int xl, xr, yl, yr;
		for (int col=5; col < n_cols-5; col++) {
			for (int row=5; row < n_rows-5; row++) {
				xl = col - 1;
				xr = col + 1;
				yl = row - 1;
				yr = row + 1;
				ix = (smooth.getPixel(xr, row) - smooth.getPixel(xl, row)) / 2.0;
				iy = (smooth.getPixel(col, yr) - smooth.getPixel(col, yl)) / 2.0;
				ix2.setPixel(col, row, ix * ix);
				ixy.setPixel(col, row, ix * iy);
				iy2.setPixel(col, row, iy * iy);
			}
		}

		xconvolve(ix2, filter);
		yconvolve(ix2, filter);
		xconvolve(iy2, filter);
		yconvolve(iy2, filter);
		xconvolve(ixy, filter);
		yconvolve(ixy, filter);

		double []eigen = new double[4];
		for (int col=5; col < n_cols-5; col++) {
			for (int row=5; row < n_rows-5; row++) {
				get_eigen(ix2.getPixel(col, row), ixy.getPixel(col, row), iy2.getPixel(col, row), eigen);
				edgy.setPixel(col, row, eigen[0]);
				corn.setPixel(col, row, eigen[1]);
			}
		}
		
		// Binarize    
		for (int col=5; col < n_cols-5; col++) 
			for (int row=5; row < n_rows-5; row++) 
				// If it is over a threshold and is a local maximum in a 7x7 neighborhood
				if (Math.abs(corn.getPixel(col, row)) > threshold && is_maximum(corn, col, row)) 
					dest_edges.addPoint(new Point2D(col, row));

		return dest_edges;
	}

	
	/**
	 * Method which indicate if a pixel is a local maximum.
	 * @param image The input image.
	 * @param column The current column.
	 * @param row The current row.
	 * @return A boolean indicating true if the pixel is a local maximum, false in otherwise.
	 * @throws JIPException
	 */
	private boolean is_maximum(JIPImgBitmap image, int col, int row) throws JIPException {
		double value = image.getPixel(col, row);

		return (value > image.getPixel(col - 3, row - 3)
				&& value > image.getPixel(col - 3, row - 2)
				&& value > image.getPixel(col - 3, row - 1)
				&& value > image.getPixel(col - 3, row)
				&& value > image.getPixel(col - 3, row + 1)
				&& value > image.getPixel(col - 3, row + 2)
				&& value > image.getPixel(col - 3, row + 3)
				&& value > image.getPixel(col - 2, row - 3)
				&& value > image.getPixel(col - 2, row - 2)
				&& value > image.getPixel(col - 2, row - 1)
				&& value > image.getPixel(col - 2, row)
				&& value > image.getPixel(col - 2, row + 1)
				&& value > image.getPixel(col - 2, row + 2)
				&& value > image.getPixel(col - 2, row + 3)
				&& value > image.getPixel(col - 1, row - 3)
				&& value > image.getPixel(col - 1, row - 2)
				&& value > image.getPixel(col - 1, row - 1)
				&& value > image.getPixel(col - 1, row)
				&& value > image.getPixel(col - 1, row + 1)
				&& value > image.getPixel(col - 1, row + 2)
				&& value > image.getPixel(col - 1, row + 3)
				&& value > image.getPixel(col, row - 3)
				&& value > image.getPixel(col, row - 2)
				&& value > image.getPixel(col, row - 1)
				&& value > image.getPixel(col, row + 1)
				&& value > image.getPixel(col, row + 2)
				&& value > image.getPixel(col, row + 3)
				&& value > image.getPixel(col + 1, row - 3)
				&& value > image.getPixel(col + 1, row - 2)
				&& value > image.getPixel(col + 1, row - 1)
				&& value > image.getPixel(col + 1, row)
				&& value > image.getPixel(col + 1, row + 1)
				&& value > image.getPixel(col + 1, row + 2)
				&& value > image.getPixel(col + 1, row + 3)
				&& value > image.getPixel(col + 2, row - 3)
				&& value > image.getPixel(col + 2, row - 2)
				&& value > image.getPixel(col + 2, row - 1)
				&& value > image.getPixel(col + 2, row)
				&& value > image.getPixel(col + 2, row + 1)
				&& value > image.getPixel(col + 2, row + 2)
				&& value > image.getPixel(col + 2, row + 3)
				&& value > image.getPixel(col + 3, row - 3)
				&& value > image.getPixel(col + 3, row - 2)
				&& value > image.getPixel(col + 3, row - 1)
				&& value > image.getPixel(col + 3, row)
				&& value > image.getPixel(col + 3, row + 1)
				&& value > image.getPixel(col + 3, row + 2)
				&& value > image.getPixel(col + 3, row + 3));
	}
	
	/**
	 * Method which make the x convolve.
	 * @param img The input image.
	 * @param filter The gaussian filter.
	 * @throws JIPException
	 */
	private void xconvolve(JIPImgBitmap img, double[] filter) throws JIPException {
		double[] pixels = img.getAllPixels();
		int width = img.getWidth();
		int height = img.getHeight();
		double[] result = new double[height*width];
		
		for (int row=0; row < height; row++) 
			for (int col=filter.length/2; col < width-filter.length/2; col++) {
				result[col+row*width] = 0.0;
				for (int i=-filter.length/2; i <= filter.length/2; i++) {
					result[col+row*width] += filter[i+filter.length/2]*pixels[col+i+row*width];
				}
			}
		img.setAllPixels(result);
	}
	
	/**
	 * Method which make the y convolve.
	 * @param img The input image.
	 * @param filter The gaussian filter.
	 * @throws JIPException
	 */
	private void yconvolve(JIPImgBitmap img, double[] filter) throws JIPException {
		double[] pixels = img.getAllPixels();
		int width = img.getWidth();
		int height = img.getHeight();
		double[] result = new double[height*width];
		
		for (int col=0; col < width; col++) 
			for (int row=filter.length/2; row < height-filter.length/2; row++) {
				result[col+row*width]=0.0;
				for (int i=-filter.length/2; i <= filter.length/2; i++) {
					result[col+row*width] += filter[i+filter.length/2]*pixels[col+(row+i)*width];
				}
			}
		img.setAllPixels(result);
	}

	/**
	 * Method which calculates the eigen vector.
	 * @param a The value of the first pixel.
	 * @param b The value of the second pixel.
	 * @param c The value of the third pixel.
	 * @param eigen The eigen vector where we will save the result
	 */
	private void get_eigen(double a, double b, double c, double eigen[]) {
		eigen[1] = (a*c-b*b) - 0.04*(a+c)*(a+c);
	}
}

