package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamFile;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.PointSR4K;
import javavis.jip3d.gui.dataobjects.SR4000Set3D;

/**
 * It obtains the confidence image from a SR4000 camera data set.<br />
 * The screen data must be a set of SR4000 points.<br />
 */
public class SR4kConfidence extends Function3D {

	public SR4kConfidence() {
		name = "SR4kConfidence";
		this.allowed_input = ScreenOptions.tSR4000SET3D;
		this.group = Function3DGroup.Image;

		ParamFile p1 = new ParamFile("Output"); 
		addParam(p1);
	}

	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		String completePath = getParamValueFile("Output");
		int count, x, y, color;
		double []pixels;
		double maxValue = 0.0;
		JIPBmpByte confidence = new JIPBmpByte(SR4000Set3D.getNcolumns(), SR4000Set3D.getNrows());
		
		Object []elements = scrData.elements();
		PointSR4K point;
		
		pixels = new double[SR4000Set3D.getNcolumns() * SR4000Set3D.getNrows()];
		for(count=0;count<elements.length;count++)
		{
			point = (PointSR4K)elements[count];
			x = point.posx;
			y = point.posy;
			color = point.confidence;
			pixels[y * SR4000Set3D.getNcolumns() + x] = color; 
			if(color > maxValue) maxValue = color;
		}
		double auxConf=255.0/maxValue;
		for (count=0; count<elements.length; count++) {
			pixels[count] *= auxConf;
		}

		confidence.setAllPixels(pixels);
		
		//write into file
		if(completePath.substring(completePath.lastIndexOf('.')+1).compareTo("jip")!=0)
		{
			completePath += ".jip";
		}
		JIPToolkit.saveImageIntoFile(confidence, completePath);
	}

}
