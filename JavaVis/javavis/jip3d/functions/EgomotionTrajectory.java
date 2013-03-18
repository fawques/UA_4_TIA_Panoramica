package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamString;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform2D;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;
import javavis.jip3d.gui.dataobjects.Trajectory;
import javavis.jip3d.gui.dataobjects.Trajectory2D;
import javavis.jip3d.gui.dataobjects.Trajectory3D;

/**
 * It applies Egomotion 2D function or FastEgomotion3D function to a trajectory files.<br />
 * The screen data must be a trajectory 2D file or a trajectory 3D file.<br />
 */
public class EgomotionTrajectory extends Function3D {

	public EgomotionTrajectory() {
		super();
		this.allowed_input = ScreenOptions.tTRAJ2D | ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Egomotion;

		ParamString p1 = new ParamString("Name");
		p1.setValue("FastEgomotion.6dof");
		ParamBool p2 = new ParamBool("3DOFF");
		p2.setValue(false);
		ParamFloat p3 = new ParamFloat("Alpha");
		p3.setValue(0.15f); //0.0125
		ParamBool p4 = new ParamBool("Ground Truth");
		p4.setValue(false);
		ParamInt p5 = new ParamInt("Step");
		p5.setValue(1);

		addParam(p1);
		addParam(p3);
		addParam(p2);
		addParam(p4);
		addParam(p5);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();

		String output_name = this.getParamValueString("Name");
		boolean three_dof = this.getParamValueBool("3DOFF");
		Trajectory traj = (Trajectory)scr_data;
		int count, size, position;
		PlaneSet3D old_planes, planes;
		ScreenData readData;
		ArrayList<PlaneSet3D> planesVector = new ArrayList<PlaneSet3D>();
		String file_name;
		double alpha = this.getParamValueFloat("Alpha");
		int step = this.getParamValueInt("Step");
		boolean groundTruth = getParamValueBool("Ground Truth");
		MyTransform gt_trans, new_trans, total_trans;
		double errX, errY, errZ, errAX, errAY, errAZ;
		double errorPos, errorPosTotal=0;
		double errorAng, errorAngTotal=0;
		IODataFile iodf;

		long total_time, t1, t2;
		int numPoses = 0;

		Function3D egomotion;
		Trajectory new_traj;
		if(three_dof)
		{
			egomotion = new Egomotion2D();
			new_traj = new Trajectory2D(new ScreenOptions(),0);
		}
		else
		{
//			egomotion = new FastEgomotion3D();
			egomotion = new Egomotion3D();
//			egomotion.setParamValue("Selected", 0.79);
			egomotion.setParamValue("Alpha", alpha);
			if(groundTruth) egomotion.setParamValue("Verbose", false);
			new_traj = new Trajectory3D(new ScreenOptions());
		}
		new_traj.name = output_name;
		new_traj.path = traj.path;

		size = traj.files.size();
		total_time = 0;

		double prog_inc = 100.0 / size;

		//read all data sets
		for(count=0;count<size;count++)
		{
			file_name = traj.files.get(count);
			iodf = new IODataFile(file_name, traj.path);
			readData = iodf.read();
			planesVector.add((PlaneSet3D)readData);
		}

		new_traj.files.add(traj.files.get(0));
		if(three_dof)
			new_traj.transforms.add(new MyTransform2D());
		else
			new_traj.transforms.add(new MyTransform3D());
		for(count=1;count<size;count++)
		{

			planes = planesVector.get(count);
			if(three_dof)
				egomotion.setParamValue("Next Object", planes);
			else
				egomotion.setParamValue("Model", planes);
			position = count - step;
			if(position<0) position += size;
			old_planes = planesVector.get(position);
			t1 = System.currentTimeMillis();
			egomotion.proccessData(old_planes);
			t2 = System.currentTimeMillis();
			total_time += t2 - t1;
//			new_trans = ((FastEgomotion3D)egomotion).getTransform();
			new_trans = ((Egomotion3D)egomotion).getTransform();

			if(count%step==0)
			{
				numPoses++;
				new_traj.files.add(traj.files.get(count));
				if(three_dof)
					new_traj.transforms.add(((Egomotion2D)egomotion).getTransform());
				else
				{
					new_traj.transforms.add(new_trans);
				}
			}
			if(groundTruth)
			{
				total_trans = new MyTransform3D();
				for(int i=count-step+1;i<=count;i++)
				{
					position = i;
					if(position<0) position+=size;
					gt_trans = traj.transforms.get(position);
					total_trans.applyTransform(gt_trans);
				}
				errX = total_trans.trX - new_trans.trX;
				errY = total_trans.trY - new_trans.trY;
				errZ = total_trans.trZ - new_trans.trZ;
				errAX = total_trans.angX - new_trans.angX;
				errAY = total_trans.angY - new_trans.angY;
				errAZ = total_trans.angZ - new_trans.angZ;
				errorPos = errX*errX + errY*errY + errZ*errZ;
				errorPosTotal += errorPos;
				errorPos = Math.sqrt(errorPos);
				errorAng = errAX*errAX+errAY*errAY+errAZ*errAZ;
				errorAngTotal += errorAng;
				errorAng = Math.sqrt(errorAng);
				System.out.println(count+": Pos: "+errorPos+" Ang: "+errorAng);
			}
			progress += prog_inc;
		}

		if(groundTruth)
		{
			System.out.println("Pose RMS: "+Math.sqrt(errorPosTotal/size));
			System.out.println("Ang. RMS: "+Math.sqrt(errorAngTotal/size));
		}
		
		System.out.println("\nEgomotion Time (mean): "+total_time/(size)+" ms");
		System.out.println("Tam: "+size+" step: "+ step+" total time: "+total_time);
		
		new_traj.scr_opt.num_points = numPoses;
		new_traj.writeData(output_name, traj.path);
		this.result_list.add(new_traj);
	}
	
}
