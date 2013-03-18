package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javavis.jip3d.gui.dataobjects.Trajectory;

/**
 * It applies the plane patch functions to a trajectory file.<br />
 * The screen data must be a trajectory 2D file or a trajectory 3D file.<br />
 */
public class PlanePatchTrajectory extends Function3D {

	public PlanePatchTrajectory()
	{
		super();

		this.allowed_input = ScreenOptions.tTRAJ2D | ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Model3D;

		ParamFloat p11 = new ParamFloat("Window size");
		p11.setValue(0.015f);
		ParamFloat p12 = new ParamFloat("Minimum size");
		p12.setValue(0.085f);
		ParamFloat p13 = new ParamFloat("PlaneThick");
		p13.setValue(0.05f);
		ParamDir p2 = new ParamDir("Directorio Planos");

		addParam(p11);
		addParam(p12);
		addParam(p13);
		addParam(p2);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		String path_planes = this.getParamValueDir("Directorio Planos");
		String file_name, path;
		int file_number, count;
		Trajectory traj;
		IODataFile iodf;

		long total_time, t1, t2;
		int total_points, total_planes;
		PointSet3D points;
		PlaneSet3D planes;

		PlanePatch seg_planos = new PlanePatch();
		//Parameters for SR4000 points
		seg_planos.setParamValue("Window size", this.getParamValueFloat("Window size"));
		seg_planos.setParamValue("Minimum size", this.getParamValueFloat("Minimum size"));
		seg_planos.setParamValue("PlaneThick", this.getParamValueFloat("PlaneThick"));

		total_time = 0;
		total_points = 0;
		total_planes = 0;
		traj = (Trajectory)scr_data;
		file_number = traj.files.size();
		double prog_inc = 100.0 / file_number;
		path = traj.path;
		
		for(count=0;count<file_number;count++)
		{
			file_name = traj.files.get(count);
			System.out.println(file_name);
			iodf = new IODataFile(file_name, path);
			points = (PointSet3D)iodf.read();

			
			t1 = System.currentTimeMillis();
			seg_planos.proccessData(points);
			t2 = System.currentTimeMillis();
			total_time += t2 -t1;
			planes = (PlaneSet3D)seg_planos.result_list.remove(0);
			total_planes += planes.scr_opt.num_points;
			planes.writeData(file_name, path_planes);
			progress += prog_inc;
		}
		
		System.out.println("Statistics:");
		System.out.println("Points per image (mean): "+total_points/file_number);
		System.out.println("Planes per image (mean):"+total_planes/file_number);
		System.out.println("Time for extracting planes per image (mean): "+total_time/file_number);
	}

}
