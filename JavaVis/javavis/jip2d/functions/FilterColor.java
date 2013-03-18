package javavis.jip2d.functions;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * It applies a filter color to an image color. If the image is a RGB one, converts it into 
 * HSB format. If not, it assumes that the input image is HSB format (3 float bands).<br />
 * It applies to COLOR (RGB or HSB) type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR (RGB or HSB) type.</li>
 * <li><em>hmean</em>: Real value which indicates the mean of hue value (default 0.2).</li>
 * <li><em>hvar</em>: Real value which indicates the variance of hue value (default 0.02).</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>histo</em>: A histogram object of the output image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed FLOAT image.</li>
 * </ul><br />
 */
public class FilterColor extends Function2D {
	private static final long serialVersionUID = -2021033445346710154L;

	public FilterColor() {
		super();
	    name = "FilterColor";
	    description = "Applies a filter color to a color (RGB or HSB) image.";

		ParamFloat p1 = new ParamFloat("hmean", false, true);
		p1.setDefault(0.2f);
		p1.setDescription("Median of Hue value");
		
		ParamFloat p2 = new ParamFloat("hvar",false, true);
		p2.setDefault(0.02f);
		p2.setDescription("Variance of Hue value");
		
		addParam(p1);
		addParam(p2);
		
		//Output parameter
		ParamObject p3 = new ParamObject("histo",false, false);
		p3.setDescription("Histogram of the result image");

		addParam(p3);
    }

    public JIPImage processImg(JIPImage img) throws JIPException {
    	if (!(img instanceof JIPImgBitmap) || 
    			(img.getType() != ImageType.COLOR && !(((JIPImgBitmap)img).getNumBands() == 3 && 
    			 img.getType() == ImageType.FLOAT))) 
    		throw new JIPException("Function FilterColor can be only applied to RGB or HSB images");
		
    	JIPImgBitmap imgBmp = null;
    	if (img.getType() == ImageType.COLOR) {
    		RGBToColor func = new RGBToColor();
    		func.setParamValue("format", "HSB");
    		imgBmp = (JIPImgBitmap)func.processImg(img);
    	}
    	else imgBmp = (JIPImgBitmap)img;
    	
		float mean = getParamValueFloat("hmean");
		float var = getParamValueFloat("hvar");
		double twoPiVar = 1.0/(Math.sqrt(2.0f*Math.PI)*var);
		double var2 = 2.0*var*var;
		int size = imgBmp.getWidth()*imgBmp.getHeight();
    	JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(1, imgBmp.getWidth(), imgBmp.getHeight(), ImageType.FLOAT);
    	double[] values = imgBmp.getAllPixels();
    	double[] result = new double[size];
    	double value;
    	int histo[] = new int[100]; // Histogram
    	
    	// We have to consider that Hue color is circular, i.e., 1.0==0.0
    	for (int i=0; i < size; i++) {
    		value = values[i];
    		if (value < 3*var && mean>1.0-3*var) value += 1.0;
    		if (value > 1.0-3*var && mean<3*var) value -= 1.0;
    		result[i] = twoPiVar*Math.exp(-Math.pow(value-mean,2.0f)/var2);
    		histo[(int)(99*result[i]/twoPiVar)]++;
    	}
    	
    	res.setAllPixels(result);
    	setParamValue("histo", histo);
    	
		return res;
	}
}

