package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.PointSR4K;
import javavis.jip3d.gui.dataobjects.SR4000Set3D;

/**
 * It obtains the intensity image from a SR4000 camera data set.<br />
 * The screen data must be a set of SR4000 points.<br />
 */
public class SR4kIntensity extends Function3D {

	public SR4kIntensity() {
		name = "SR4kIntensity";
		this.allowed_input = ScreenOptions.tSR4000SET3D;
		this.group = Function3DGroup.Image;

		ParamFile p1 = new ParamFile("Output"); 
		ParamList p2 = new ParamList("format");
		String []list = new String[3];
		list[0] = "PNG/PGM";
		list[1] = "JPG";
		list[2] = "JIP";
		p2.setDefault(list);
		
		addParam(p1);
		addParam(p2);
	}

	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		String completePath = getParamValueFile("Output");
		String path = completePath.substring(0, completePath.lastIndexOf('/')+1);
		String name = completePath.substring(completePath.lastIndexOf('/')+1);
		int count, x, y, color;
		double []pixels;
		double maxValue = 0.0;
		JIPBmpByte amplitude = new JIPBmpByte(SR4000Set3D.getNcolumns(), SR4000Set3D.getNrows());
		String format = getParamValueList("format");
		
		Object []elements = scrData.elements();
		PointSR4K point;
		
		pixels = new double[SR4000Set3D.getNcolumns() * SR4000Set3D.getNrows()];
		for(count=0;count<elements.length;count++)
		{
			point = (PointSR4K)elements[count];
			x = point.posx;
			y = point.posy;
			color = point.getColor().get().getRGB() & 255;
			pixels[y * SR4000Set3D.getNcolumns() + x] = color; 
			if(color > maxValue) maxValue = color;
		}
		double auxAmp=255.0/maxValue;
		//Equalization of the amplitude
		int []histo=new int[256];
		for (int i=0; i<elements.length; i++) {
			pixels[i] *= auxAmp;
			histo[(int)(pixels[i])]++;
		}
		for (int i=0; i<254; i++) {
			histo[i+1]+=histo[i];
		}
		for (int i=0; i<elements.length; i++) {
			pixels[i]=255*(histo[(int)(pixels[i])]-1)/(elements.length-1);
		}

		amplitude.setAllPixels(pixels);
		
		//write into file
		if(format.compareTo("PNG/PGM")==0)
		{
			Sequence seq = new Sequence();
			seq.addFrame(amplitude);
			JIPToolkit.saveImgIntoFilePng(seq, 0, path, name);
		}
		else if(format.compareTo("JPG")==0)
		{
			String ext = name.substring(name.lastIndexOf('.')+1).toLowerCase(); 
			if(ext.compareTo("jpg")!=0 && ext.compareTo("jpeg")!=0)
				name += ".jpg";
			Sequence seq = new Sequence();
			seq.addFrame(amplitude);
			JIPToolkit.saveImgIntoFileJpg(seq, 0, path, name);
		}
		else
		{
			if(completePath.substring(completePath.lastIndexOf('.')+1).compareTo("jip")!=0)
			{
				completePath += ".jip";
			}
			JIPToolkit.saveImageIntoFile(amplitude, completePath);
		}
		
	}

}
