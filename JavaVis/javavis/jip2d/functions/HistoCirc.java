package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;

/**
 * It calculates a circular histogram of a binary image. It is used in RecogLT function. 
 * It applies to BIT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BIT type image.</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>histo</em>: A circular histogram from the input image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same binary image.</li>
 * </ul><br />
 */
public class HistoCirc extends Function2D {
	private static final long serialVersionUID = -7262973524107183332L;

	public HistoCirc() {
		super();
		name = "HistoCirc";
		description = "Gets a circular histogram from a binay image. Applies to BIT type.";
		groupFunc = FunctionGroup.Applic;
		
		//Output parameter
		ParamObject p1 = new ParamObject("histo", false, false);
		addParam(p1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BIT) 
			throw new JIPException("HistoCirc only defined for BIT type images");
		
		JIPBmpBit imgBmp = (JIPBmpBit)img;
		ArrayList<int[]> histoList = new ArrayList<int[]>();
		int width = img.getWidth();
		int height = img.getHeight();
		int maxDist = (int)Math.sqrt(Math.pow(width/2, 2.0) + Math.pow(height/2, 2.0)) + 1;
		int[] histo;
		int dist;
		
		// For each band in the image
		for (int b=0; b < imgBmp.getNumBands(); b++) {
			histo = new int[maxDist];
			for (int h=0; h < height; h++) 
				for (int w=0; w < width; w++) 
					if (imgBmp.getPixelBool(b,w,h)) {
						dist = (int)Math.sqrt(Math.pow(h-height/2, 2.0) + Math.pow(w-width/2, 2.0));
						histo[dist]++;
					}
			for (int i=1; i < histo.length; i++)
				histo[i] += histo[i-1];
			histoList.add(histo);
		}

		setParamValue("histo", histoList);

		return img;
	}
}

