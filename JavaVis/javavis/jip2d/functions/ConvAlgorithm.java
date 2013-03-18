package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It defines and applies several well known algorithms for edge detection.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type and it uses ConvolveImage method.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>method</em>: List which contains the methods that can be used in this function: 
 * Prewitt, Sobel or Laplacian (default Prewitt).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image, with the same type that the input image, and the double number of 
 * bands in case of using gray scale images.</li>
 * </ul><br />
 */
public class ConvAlgorithm extends Function2D {
	private static final long serialVersionUID = 7850747295611493227L;

	public ConvAlgorithm() {
		super();
		name = "ConvAlgorithm";
		description = "Edge detection algorithms. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Edges;

		ParamList p1 = new ParamList("method", false, true);
		String[] paux = new String[3];
		paux[0] = "Prewitt";
		paux[1] = "Sobel";
		paux[2] = "Laplacian";
		p1.setDefault(paux);
		p1.setDescription("Method to apply");

		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {	
		if (img instanceof JIPImgGeometric || img.getType() == ImageType.BIT) 
			throw new JIPException("Function ConvAlgorithm can not be applied to this image format.");

		String method = getParamValueString("method");
		Function2D convolution = new ConvolveImage();
		double mat[] = new double[9]; // The three algorithms defines a 3x3 convolve matrix
		JIPImgBitmap convo = (JIPImgBitmap)JIPImage.newImage(3, 3, ImageType.FLOAT);
		int numBands = ((JIPImgBitmap)img).getNumBands();

		if (method.equals("Prewitt")) {
			JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(2*numBands, img.getWidth(), img.getHeight(), img.getType());
			JIPImgBitmap aux, aux2;
			mat[0] = 1.0; mat[1] = 0.0; mat[2] = -1.0;
			mat[3] = 1.0; mat[4] = 0.0; mat[5] = -1.0;
			mat[6] = 1.0; mat[7] = 0.0; mat[8] = -1.0;
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			aux = (JIPImgBitmap)convolution.processImg(img);
			mat[0] = 1.0; mat[1] = 1.0; mat[2] = 1.0;
			mat[3] = 0.0; mat[4] = 0.0; mat[5] = 0.0;
			mat[6] = -1.0; mat[7] = -1.0; mat[8] = -1.0;
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			aux2 = (JIPImgBitmap)convolution.processImg(img);
			for (int nb=0; nb < numBands; nb++) {
				res.setAllPixels(nb*2, aux.getAllPixels(nb));
				res.setAllPixels(nb*2+1, aux2.getAllPixels(nb));
			}
			return res;
		}
		else if (method.equals("Sobel")) {
			JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(2*numBands, img.getWidth(), img.getHeight(), img.getType());
			JIPImgBitmap aux, aux2;
			mat[0] = 1.0; mat[1] = 0.0; mat[2] = -1.0;
			mat[3] = 2.0; mat[4] = 0.0; mat[5] = -2.0;
			mat[6] = 1.0; mat[7] = 0.0; mat[8] = -1.0;
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			aux = (JIPImgBitmap)convolution.processImg(img);
			mat[0] = 1.0; mat[1] = 2.0; mat[2] = 1.0;
			mat[3] = 0.0; mat[4] = 0.0; mat[5] = 0.0;
			mat[6] = -1.0; mat[7] = -2.0; mat[8] = -1.0;
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			aux2 = (JIPImgBitmap)convolution.processImg(img);
			for (int nb=0; nb < numBands; nb++) {
				res.setAllPixels(nb*2, aux.getAllPixels(nb));
				res.setAllPixels(nb*2+1, aux2.getAllPixels(nb));
			}
			return res;
		}
		else if (method.equals("Laplacian")) {
			mat[0] = 0.0; mat[1] = -1.0; mat[2] = 0.0;
			mat[3] = -1.0; mat[4] = 4.0; mat[5] = -1.0;
			mat[6] = 0.0; mat[7] = -1.0; mat[8] = 0.0;
			convo.setAllPixels(mat);
			convolution.setParamValue("image", convo);
			return convolution.processImg(img);
		}
		else throw new JIPException("ConvAlgorithm: algorithm to apply not recognized");
	}
}

