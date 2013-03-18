package javavis.jip2d.functions;

import java.awt.Image;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamFile;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;

/**
 * It loads the image (jpeg or gif) in the file indicated as parameter.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>file</em>: The file name.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The load image will be shown in the panel.</li>
 * </ul><br />
 */
public class LoadImage extends Function2D {
	private static final long serialVersionUID = -4783214630724479756L;

	public LoadImage() {
		super();
		name = "LoadImage";
		description = "Loads an image.";
		groupFunc = FunctionGroup.Others;

		ParamFile p1 = new ParamFile("file", false, true);
		p1.setDescription("File name.");
		addParam(p1);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {			
		String nameFile = getParamValueString("file");
		JIPImage res = null;
		
		if (nameFile.endsWith(".jpg") || nameFile.endsWith(".gif") ||
				nameFile.endsWith(".jpeg")) {
			Image auxImg = JIPToolkit.getAWTImage(nameFile);
			if (auxImg == null) throw new JIPException("Some error with file " + nameFile);
			res = JIPToolkit.getColorImage(auxImg);
		}
		else if (nameFile.endsWith(".jip")) {
			res = JIPToolkit.getSeqFromFile(nameFile).getFrame(0);
		}
		
		return res;
	}
}

