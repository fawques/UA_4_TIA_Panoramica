package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;

/**
 * It creates an image with a Gabor filter on the basis of orientation and scale.<br />
 * Firstly, we set the coordinate origin in the center of the image, convert the 
 * orientation to radians, set the wavelength (2*scale) and recall the image with 
 * the values of the filter. A gaussian value is assigned in the pixel on the basis
 * of sine or cosine. It uses the ConvolveImage method.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. </li>
 * <li><em>rows</em>: Integer value which indicates the number of rows of the generated image (default 5).</li>
 * <li><em>columns</em>: Integer value which indicates the number of columns of the generated image (default 5).</li>
 * <li><em>scale</em>: Integer value which indicates the filter value of scale (Gaussian sigma) (default 0).</li>
 * <li><em>orientation</em>: Integer value which indicates the filter value of orientation (degrees) (default 0).</li>
 * <li><em>type</em>: Boolean value which indicates the filter type to use (wave to compress in the 
 * gaussian) (unchecked = cosine, checked = sine) (default cosine).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed FLOAT image.</li>
 * </ul><br />
 */
public class Gabor extends Function2D {
	private static final long serialVersionUID = 4650745710291222027L;

	public Gabor() {
		super();
		name = "Gabor";
		description = "Creates an image with a Gabor filter.";
		groupFunc = FunctionGroup.Convolution;

		ParamInt p1 = new ParamInt("rows", false, true);
		p1.setDefault(5);
		p1.setDescription("Filter rows");
		
		ParamInt p2 = new ParamInt("columns", false, true);
		p2.setDefault(5);
		p2.setDescription("Filter columns");
		
		ParamFloat p3 = new ParamFloat("scale", false, true);
		p3.setDefault(1.0f);
		p3.setDescription("Filter scale");
		
		ParamFloat p4 = new ParamFloat("orientation", false, true);
		p4.setDefault(0.0f);
		p4.setDescription("Filter orientation");
		
		ParamBool p5 = new ParamBool("type", false, true);
		p5.setDefault(false);
		p5.setDescription("Filter type (no marked: GCos, marked: GSin)");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPBmpFloat result = null;  

		int rows = getParamValueInt("rows");
		int columns = getParamValueInt("columns");
		float scale = getParamValueFloat("scale");
		float orientation = getParamValueFloat("orientation");
		boolean type = getParamValueBool("type");

		double[] map = new double[columns * rows];
		orientation *= Math.PI / 180.0;
		double length = 2.0 * scale;

		for (int x=-columns/2; x <= columns/2; x++) {
			for (int y=-rows/2; y <= rows/2; y++) {
				if (y+rows/2 >= rows || x+columns/2 >= columns) continue;
				float exponential = (float) Math.exp(- (x * x + y * y) / (2 * scale * scale));
				if (!type)
					map[(x+columns/2) * columns + (y+rows/2)] = Math.cos((2 * Math.PI / length)
						* (x * Math.cos(orientation) + y * Math.sin(orientation))) * exponential;
				else
					map[(x+columns/2) * columns + (y+rows/2)] = Math.sin((2 * Math.PI / length) 
						* (x * Math.cos(orientation) + y * Math.sin(orientation))) * exponential;
			}
		}
		result = new JIPBmpFloat(columns, rows);
		result.setAllPixels(map);

		return result;
	}
}

