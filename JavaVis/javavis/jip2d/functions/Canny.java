package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It implements the Canny method to edge detection. It applies an optimum filter which 
 * is convolutioned with the image causing a local maximum where an edge is located, 
 * reducing the noise effect at the same time. To get the best filter, two criteria 
 * are defined which should be maximized. To do that, it uses Equalize and Brightness
 * methods.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type (COLOR images are converted to Gray).
 * <br /><br /> 
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>sigma</em>: Integer value which indicates the level of gaussian smooth (default 1).</li>
 * <li><em>brightness</em>: Integer value which indicates the percentage of brightness 
 * (default 100).</li>
 * <ul>
 * <li><em>brightness</em> = 100: It does not alter the image.</li>
 * <li><em>brightness</em> &lt; 100: It decreases the brightness.</li>
 * <li><em>brightness</em> &gt; 100: It increases the brightness.</li>
 * </ul></li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A BYTE image processed, with the edges detected and the new level of brightness.</li>
 * </ul><br /><br />
 * <em>Additional notes</em>: Since a suppression of non-maximum values is applied, then the 
 * edges have a width of a pixel. This makes difficult to visualize and it is advisable to 
 * increase the brightness. To normalize the result, Equalize is called.<br />
 */
public class Canny extends Function2D {
	private static final long serialVersionUID = 3914477486873913843L;

	public Canny() {
		super();
		name = "Canny";
		description = "Detects edge using the Canny method. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Edges;
		
		ParamFloat p1 = new ParamFloat("sigma", false, true);
		p1.setDefault(1.0f);
		p1.setDescription("Level of gaussian smoothed");
		
		ParamInt p2 = new ParamInt("brightness", false, true);
		p2.setDefault(100);
		p2.setDescription("Brightness adjustment");

		addParam(p1);
		addParam(p2);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function Canny can not be applied to this image type.");
		
		JIPImgBitmap res, auxImg, auxImg2;
		ImageType type = img.getType();
		int width = img.getWidth();
		int height = img.getHeight();
		int totalPix = width*height;

		switch (type) {
			case COLOR :
				type = ImageType.BYTE;
				Function2D ctg = new ColorToGray();
				ctg.setParamValue("gray", "FLOAT");
				auxImg2 = (JIPImgBitmap)ctg.processImg(img);
				break;
			case BYTE :
			case SHORT :
				Function2D gtg = new GrayToGray();
				gtg.setParamValue("gray","FLOAT");
				auxImg2 = (JIPImgBitmap)gtg.processImg(img);
				break;
			case FLOAT :
				auxImg2 = (JIPImgBitmap)img;
				break;
			default :
				throw new JIPException("Function Canny can not be applied to this image type.");
		}

		res = (JIPImgBitmap)JIPImage.newImage(auxImg2.getNumBands(), width, height, type);
		float sigma = getParamValueFloat("sigma");
		int brightness = getParamValueInt("brightness");

		if (sigma > 0) {
			Function2D smoothing = new SmoothGaussian();
			smoothing.setParamValue("sigma", sigma);
			smoothing.setParamValue("method", "PAD");
			auxImg = (JIPImgBitmap)smoothing.processImg(auxImg2);
			if (smoothing.isInfo())
				info = "Canny info: " + smoothing.getInfo();
		}
		else 
			auxImg = auxImg2;
		
		auxImg2 = (JIPImgBitmap)JIPImage.newImage(auxImg.getNumBands(), width, height, ImageType.FLOAT);

		for (int b=0; b < auxImg.getNumBands(); b++) {
			double Dx, Dy;
			double[] bmp;
			double[] gradX = new double[totalPix], gradY = new double[totalPix];
			double[] mag = new double[totalPix], bin = new double[totalPix];
			bmp = auxImg.getAllPixels(b);

			for (int x=1; x < width-1; x++) {
				for (int y=1; y < height-1; y++) {
					Dx = (bmp[(y + 1) * width + x + 1] + 2 * bmp[y * width + x + 1]
							+ bmp[(y - 1) * width + x + 1] - bmp[(y + 1) * width + x - 1]
							- 2 * bmp[y * width + x - 1] - bmp[(y - 1) * width + x - 1]) / 8;
					Dy = (bmp[(y + 1) * width + x + 1] + 2 * bmp[(y + 1) * width + x]
							+ bmp[(y + 1) * width + x - 1] - bmp[(y - 1) * width + x + 1]
							- 2 * bmp[(y - 1) * width + x] - bmp[(y - 1) * width + x - 1]) / 8;
					gradX[y * width + x] = Dx;
					gradY[y * width + x] = Dy;
					mag[y * width + x] = Math.sqrt(Dx * Dx + Dy * Dy);
				}
			}
			
			double g; 				// Magnitude of the central pixel gradient
			double g1, g2; 			// Magnitudes of interpolated gradient
			double ga, gb, gc, gd;	// Magnitudes of the nearby pixels gradient
			double ux, uy;
			double dx, dy;

			// For each pixel, checks if it is a local maximum in a neighborhood 3x3
			// on the orientation of the gradient
			for (int x=1; x < width-1; x++) {
				for (int y=1; y < height-1; y++) {
					dx = gradX[y * width + x];
					dy = gradY[y * width + x];
					if (dx == 0.0 && dy == 0.0)
						continue;
					if (Math.abs(dy) > Math.abs(dx)) {
						ux = dx / dy;
						uy = 1.0;
						gb = mag[(y - 1) * width + x];
						gd = mag[(y + 1) * width + x];
						if (dx * dy < 0) {
							ga = mag[(y - 1) * width + x - 1];
							gc = mag[(y + 1) * width + x + 1];
						} else {
							ga = mag[(y - 1) * width + x + 1];
							gc = mag[(y + 1) * width + x - 1];
						}
					} 
					else {
						ux = dy / dx;
						uy = 1.0;
						gb = mag[y * width + x + 1];
						gd = mag[y * width + x - 1];
						if (dx * dy < 0) {
							ga = mag[(y + 1) * width + x + 1];
							gc = mag[(y - 1) * width + x - 1];
						} else {
							ga = mag[(y - 1) * width + x + 1];
							gc = mag[(y + 1) * width + x - 1];
						}
					}
					g1 = (ux * ga) + (uy - ux) * gb;
					g2 = (ux * gc) + (uy - ux) * gd;
					g = Math.sqrt(dx * dx + dy * dy);
					if (g > g1 && g >= g2)
						bin[y * width + x] = mag[y * width + x];
					else
						bin[y * width + x] = 0;
				}
			}
			auxImg2.setAllPixels(bin);
		}

		if (type != ImageType.FLOAT) {
			Function2D gtg = new GrayToGray();
			gtg.setParamValue("gray",type.toString());
			res = (JIPImgBitmap)gtg.processImg(auxImg2);
		}
		else 
			res = auxImg2;
		
		Function2D equ = new Equalize();
		res = (JIPImgBitmap)equ.processImg(res); 

		Function2D fbrightness = new Brightness();
		fbrightness.setParamValue("perc", brightness);
		res = (JIPImgBitmap)fbrightness.processImg(res); 
		if (fbrightness.isInfo())
			info = "Canny info: " + fbrightness.getInfo();
		
		return res;
	}
}

