package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javavis.jip3d.gui.dataobjects.SR4000Set3D;
import javavis.jip3d.gui.dataobjects.Trajectory;

/**
 * It obtains information from a trajectory 3D which contains SR4000 camera data set, and 
 * filter the points which are out of a confidence range.<br />
 * The screen data must be a trajectory 3D file.<br />
 */
public class SR4kFilterProcess extends Function3D {

	public SR4kFilterProcess() {
		name = "FSR4kProcess";
		this.allowed_input = ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Others;

		//minimum distance
		ParamFloat p1 = new ParamFloat("distance");
		p1.setValue(1.0f);
		//minimum confidence
		ParamInt p2 = new ParamInt("confidence");
		p2.setValue(240);
		//minimum intensity
		ParamInt p3 = new ParamInt("intensity");
		p3.setValue(25);
		
		ParamDir p4 = new ParamDir("Output");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		int count, total;
		String file_name, path;
		SR4000Set3D sr4kset;
		PointSet3D ptset;
		IODataFile iodf;
		
		//get input parameters
		double minDist = this.getParamValueFloat("distance");
		int minConf = this.getParamValueInt("confidence");
		int minInt = this.getParamValueInt("intensity");
		String ruta_puntos = this.getParamValueDir("Output");
		
		//filtering function
		SR4kFilter fsrf = new SR4kFilter();
		fsrf.setParamValue("distance", minDist);
		fsrf.setParamValue("confidence", minConf);
		fsrf.setParamValue("intensity", minInt);
		
		Trajectory traj = (Trajectory)scrData;
		total = traj.files.size();
		path = traj.path;
		for(count=0;count<total;count++)
		{
			file_name = traj.files.get(count);
			sr4kset = new SR4000Set3D(new ScreenOptions());
			sr4kset.name = file_name;
			iodf = new IODataFile(file_name, path);
			sr4kset = (SR4000Set3D)iodf.read();
			
			fsrf.proccessData(sr4kset);
			ptset = (PointSet3D)fsrf.result_list.get(0);
			ptset.writeData(file_name, ruta_puntos);
		}

	}

}
