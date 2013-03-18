package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.util.NewWindow;

import javax.swing.JFrame;

/**
 * It checks image type and afterwards a new window is created and shown. If the number of 
 * frames has several bands, only the first one will be shown.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image to show in the window. It has to be a bitmap image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A new window with the current image.</li>
 * </ul><br />
 */
public class OpenWindow extends Function2D {
	private static final long serialVersionUID = 2688851364816071757L;

	public OpenWindow() {
		super();
		name = "OpenWindow";
		description = "Open the image in a new window. Applies to bitmap images.";
		groupFunc = FunctionGroup.Manipulation;
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgBitmap) {
			JFrame frame = new NewWindow(img);
			frame.setSize(img.getWidth() + 5, img.getHeight() + 35);
			frame.setVisible(true);
		}
		else throw new JIPException("Function OpenWindow can not be applied to this image format.");
			
		return img;
	}
}

