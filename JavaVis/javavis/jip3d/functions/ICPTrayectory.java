package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javavis.jip3d.gui.dataobjects.Trajectory;
import javavis.jip3d.gui.dataobjects.Trajectory3D;

/**
 * It applies ICP function, ICPQuat function or Fast ICP function to a trajectory files.<br />
 * The screen data must be a trajectory 2D file or a trajectory 3D file.<br />
 */
public class ICPTrayectory extends Function3D {

	public ICPTrayectory() {
		super();

		this.allowed_input = ScreenOptions.tTRAJ2D | ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Egomotion;
		
		ParamBool p1 = new ParamBool("Ground Truth");
		p1.setValue(false);
		ParamInt p2 = new ParamInt("Step");
		p2.setValue(1);
		ParamList p3 = new ParamList("Algorithm");
		String []list = new String[3];
		list[2] = "Vanilla ICP (quaternions)";
		list[1] = "Vanilla ICP (SVD)";
		list[0] = "Fast ICP";
		p3.setDefault(list);

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		PointSet3D points1, points2;
		int file_number;
		int count;
		String file_name;
		Trajectory traj;
		String path;
		int step = getParamValueInt("Step");
		MyTransform transform, new_transform, total_transform;
		double errX, errY, errZ, errAX, errAY, errAZ;
		double errorPos, errorAng, errorPosTotal=0, errorAngTotal=0;
		int numPoses = 0;
		IODataFile iodf;
		
		boolean groundTruth = getParamValueBool("Ground Truth");
		String algorithm = getParamValueList("Algorithm");
		Function3D Icp = null;
		if(algorithm.compareTo("Vanilla ICP (quaternions)")==0)
		{
			Icp = new ICPQuat();
		} else if(algorithm.compareTo("Vanilla ICP (SVD)")==0)
		{
			Icp = new ICP();
		} else Icp = new FastICP();
		
		if(groundTruth) Icp.setParamValue("Verbose", false);
		else Icp.setParamValue("Verbose", true);
		Trajectory3D new_traj = new Trajectory3D(new ScreenOptions());
		
		traj = (Trajectory)scr_data;
		file_number = traj.files.size();
		path = traj.path;

		double prog_inc = 100.0/(file_number-1);
		file_name = traj.files.get(0);
		new_traj.files.add(file_name);
		new_traj.transforms.add(traj.transforms.get(0));

		iodf = new IODataFile(file_name, path);
		points2 = (PointSet3D)iodf.read();
		new_transform = new MyTransform3D();
		
		total_transform = new MyTransform3D();
		for(count=1;count<file_number;count++)
		{

			points1 = points2;
			file_name = traj.files.get(count);
			
			transform = new MyTransform3D(traj.transforms.get(count));
			total_transform.applyTransform(transform);

			if(count%step==0)
			{
				numPoses++;
				iodf = new IODataFile(file_name, path);
				points2 = (PointSet3D)iodf.read();
				Icp.setParamValue("Next Object", points2);
				Icp.proccessData(points1);
				if(algorithm.compareTo("Vanilla ICP (quaternions)")==0)
				{
					new_transform = ((ICPQuat)Icp).getTransform();
				} else if(algorithm.compareTo("Vanilla ICP (SVD)")==0)
				{
					new_transform = ((ICP)Icp).getTransform();
				} else 
					new_transform = ((FastICP)Icp).getTransform();
				if(groundTruth)
				{
					errX = total_transform.trX - new_transform.trX;
					errY = total_transform.trY - new_transform.trY;
					errZ = total_transform.trZ - new_transform.trZ;
					errAX = total_transform.angX - new_transform.angX;
					errAY = total_transform.angY - new_transform.angY;
					errAZ = total_transform.angZ - new_transform.angZ;
					errorPos = errX*errX + errY*errY + errZ*errZ;
					errorPosTotal += errorPos;
					errorPos = Math.sqrt(errorPos);
					errorAng = errAX*errAX+errAY*errAY+errAZ*errAZ;
					errorAngTotal += errorAng;
					errorAng = Math.sqrt(errorAng);
					System.out.println(count+": Pos: "+errorPos+" Ang: "+errorAng);
				}
				else
					System.out.println(file_name+" "+new_transform);
				
				transform.applyTransform(new_transform);
				new_traj.files.add(file_name);
				new_traj.transforms.add(transform);
				total_transform = new MyTransform3D();
			}
			progress += prog_inc;

		}
		new_traj.scr_opt.num_points = file_number;
		new_traj.path = traj.path;
		new_traj.name = "fastICP.6dof";
		result_list.add(new_traj);
		
		if(groundTruth)
		{
			System.out.println("Pose RMS: "+Math.sqrt(errorPosTotal/numPoses));
			System.out.println("Ang. RMS: "+Math.sqrt(errorAngTotal/numPoses));
		}

	}

}
