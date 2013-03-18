package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.util.HistogramWindow;

import javax.swing.JFrame;

/**
 * It calculates the histogram of an input image. It does not manage geometric frames, 
 * and it do not modify the input number of frames.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same input image.</li>
 * </ul><br />
 * Additional notes: This function just creates <em>javavis.jip2d.base.HistogramWindow</em> 
 * and there is where the histogram is calculated.
 */ 
public class Histogram extends Function2D {
	private static final long serialVersionUID = 4868691655276493208L;

	public Histogram() {
		super();
		name = "Histogram";
		description = "Show the histogram of an image. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Others;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {        
		if (!(img instanceof JIPImgGeometric) && img.getType() != ImageType.BIT) {
			JFrame frame = new HistogramWindow((JIPImgBitmap)img);
			frame.setSize(290, 405);
			frame.setVisible(true);
		}
		else throw new JIPException("Function Histogram can not be applied to this image format.");
		
		return img;
	}
}

