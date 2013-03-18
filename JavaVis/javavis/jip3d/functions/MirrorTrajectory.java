/**
 * 
 */
package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamList;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.gui.dataobjects.Trajectory;

/**
 * It implements a mirror transformation to all elements of a trajectory 3D. 
 * You can indicate the coordinates you want to fixed.<br />
 * The screen data must be a trajectory 3D file.<br />
 */
public class MirrorTrajectory extends Function3D {


	public MirrorTrajectory() {
		super();
		this.allowed_input = ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Transform;
		
		ParamList p1 = new ParamList("Plane");
		String []list = new String[3];
		list[0] = "XY";
		list[1] = "XZ";
		list[2] = "YZ";
		p1.setDefault(list);
		ParamDir p2 = new ParamDir("Output Folder");
		
		addParam(p2);
		addParam(p1);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		String plane = getParamValueList("Plane");
		String file_name, path;
		int file_number, count;
		Trajectory traj;
		ScreenData readData;
		IODataFile iodf;

		String outputFolder = getParamValueDir("Output Folder");
		
		Mirror fm = new Mirror();
		fm.setParamValue("Plane", plane);
		traj = (Trajectory)scr_data;
		file_number = traj.files.size();
		double prog_inc = 100.0 / file_number;
		path = traj.path;

		for(count=0; count<file_number; count++) {
			file_name = traj.files.get(count);
			System.out.println(file_name);
			iodf = new IODataFile(file_name, path);
			readData = iodf.read();

			fm.proccessData(readData); //cutted_points
			readData.writeData(file_name, outputFolder);
			progress += prog_inc;
		}
	}
}
