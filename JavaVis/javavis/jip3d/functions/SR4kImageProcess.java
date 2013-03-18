package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamString;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.gui.dataobjects.SR4000Set3D;
import javavis.jip3d.gui.dataobjects.Trajectory;

/**
 * It automates the image extraction process from a trajectory with SR4000 3D points data 
 * sets. After, it forms the 2D image with color information. It allows to select between an 
 * intensity image or a confidence image.<br />
 * The screen data must be a trajectory 3D file.<br />
 */
public class SR4kImageProcess extends Function3D {

	public SR4kImageProcess()
	{
		name = "SR4kImageProcess";
		this.allowed_input = ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Image;
		
		ParamDir p1 = new ParamDir("Output"); 
		ParamString p4 = new ParamString("SeqName"); 
		ParamList p2 = new ParamList("Format");
		String []list = new String[3];
		list[0] = "PNG/PGM";
		list[1] = "JPG";
		list[2] = "JIP";
		p2.setDefault(list);
		ParamList p3 = new ParamList("Type");
		String []type = new String[2];
		type[0] = "Intensity";
		type[1] = "Confidence";
		p3.setDefault(type);
		
		addParam(p1);
		addParam(p4);
		addParam(p3);
		addParam(p2);
	}
	
	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		String outputPath = getParamValueDir("Output");
		String name = getParamValueString("SeqName");
		String format = getParamValueList("Format");
		String type = getParamValueList("Type");
		IODataFile iodf;
		
		Function3D ftrans;
		int total, count;
		String fileName;
		SR4000Set3D sr4kset;
		String outputName;
		
		if(type.compareTo("Intensity")==0)
		{
			ftrans = new SR4kIntensity();
		}
		else ftrans = new SR4kConfidence();
		
		ftrans.setParamValue("format", format);
		
		Trajectory traj = (Trajectory)scr_data;
		total = traj.files.size();
		String path = traj.path;
		for(count=0;count<total;count++)
		{
			fileName = traj.files.get(count);
			sr4kset = new SR4000Set3D(new ScreenOptions());
			sr4kset.name = fileName;
			iodf = new IODataFile(fileName, path);
			sr4kset = (SR4000Set3D)iodf.read();
			
			outputName = outputPath + "/" + name;
			if(count<10)
				outputName += "00"+count;
			else if(count<100)
				outputName += "0"+count;
			else
				outputName += count;

			ftrans.setParamValue("Output", outputName);
			ftrans.proccessData(sr4kset);
		}

	}

}
