package javavis.jip3d.functions;

import java.io.FileWriter;
import java.io.IOException;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamInt;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.gui.dataobjects.FeatureSet2D;
import javavis.jip3d.gui.dataobjects.Trajectory;
import javavis.jip3d.gui.dataobjects.Trajectory3D;

public class CalcErrorTrajFeaturesRansac extends Function3D {

	/**
	 * @uml.property  name="dataSaved"
	 */
	private int dataSaved;
	
	public CalcErrorTrajFeaturesRansac()
	{
		super();
		dataSaved=1;

		this.allowed_input = ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Egomotion;

		ParamDir p2 = new ParamDir("Features Directory");
		p2.setValue("/Users/miguel/Desktop/prueba/pasilloDiego");
		ParamInt p1 = new ParamInt("Value");
		p1.setValue(1);

		addParam(p2);
		addParam(p1);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		String feat_dir = this.getParamValueDir("Features Directory");
		int value = this.getParamValueInt("Value");
		String file_name, path;
		int file_number;
		long total_time, t1, t2;
		Trajectory traj;
		FeatureSet2D features, featuresPrev;
		double error=0.0;
		int matches=0;
		MyTransform trans=new MyTransform3D();
		IODataFile iodf;

		RansacFeatures icpFeat = new RansacFeatures();
		Trajectory3D traj3D = new Trajectory3D(new ScreenOptions());
		traj3D.name = "trajectory";
		traj3D.path = feat_dir;

		total_time = 0;
		traj = (Trajectory)scr_data;
		file_number = traj.files.size();
		double prog_inc = 100.0 / file_number;
		path = traj.path;
		
		FileWriter fw=null; //Used for writing features used and egomotion found
		try
		{
			fw = new FileWriter(path+"/output");
			fw.write(file_number+"\n");
			icpFeat.setParamValue("fileWriter", fw);
		} catch(IOException e) {
			System.err.println("Error:" + e.getMessage());
		}
		
		//Read the first file
		file_name = traj.files.get(0);
		iodf = new IODataFile(file_name, path);
		featuresPrev = (FeatureSet2D)iodf.read();
		String aux=file_name.substring(0, file_name.lastIndexOf("."))+".pts";
		traj3D.files.add(aux);
		traj3D.transforms.add(trans);
		
		double []error_ind = new double[file_number];
		double []matches_ind = new double[file_number];
		double []time_ind = new double[file_number];
		double []error_tras = new double[file_number];
		double []error_ang = new double[file_number];
		
		double auxError;
		int contCorrect=0;
		
		for(int cont=1;cont<file_number;cont=cont+value)
		{
			System.out.println(cont);
			// Read the next set
			file_name = traj.files.get(cont);
			iodf = new IODataFile(file_name, path);
			features = (FeatureSet2D)iodf.read();

			t1 = System.currentTimeMillis();
			try {
				icpFeat.setParamValue("Next Object", features);
				icpFeat.proccessData(featuresPrev);
				t2 = System.currentTimeMillis();
				total_time += t2 -t1;
				time_ind[cont]=t2 -t1;
				auxError = icpFeat.getError();
				matches += icpFeat.getFinalMatches();
				trans = icpFeat.getTransform();
				if (trans!=null) {
					error += auxError;
					matches_ind[cont]=icpFeat.getFinalMatches();
					error_ind[cont]=icpFeat.getError();
					contCorrect++;
				}
				else {
					trans = new MyTransform3D();
					error_ind[cont]=0;
				}
					
				// Must store the points files, instead of sift files
				aux=file_name.substring(0, file_name.lastIndexOf("."))+".pts";
				traj3D.files.add(aux);
				traj3D.transforms.add(trans);
				error_tras[cont] = Math.sqrt(trans.trX*trans.trX+trans.trZ*trans.trZ+trans.trZ*trans.trZ);
				error_ang[cont] = Math.sqrt(trans.angX*trans.angX+trans.angZ*trans.angZ+trans.angZ*trans.angZ);
				System.out.println("Trans="+trans+" Error="+auxError+" Matches="+icpFeat.getFinalMatches()+" Time="+(t2-t1)/1000.0);
			} 
			catch (Exception e) {
				System.out.println("Set "+file_name+" gives an error"+e);
			}
			finally {
				featuresPrev = features;
				progress += prog_inc;
			}
			if (cont%2==0) {
				traj3D.writeData("trajectory.txt", feat_dir);
				writeData("stadistics", feat_dir, cont, time_ind, matches_ind, error_ind);
			}
			System.out.println("Correct poses="+contCorrect);
		}
		try {
			fw.close();
		} catch(IOException e) {
			System.err.println("Error:" + e.getMessage());
		}
			
		/*traj3D.writeData("trajectory.txt", feat_dir);
		double mean_time=total_time/(float)(file_number-1);
		double mean_error=error/(file_number-1);
		double mean_matches=matches/(file_number-1);
		double var_time=0.0, var_error=0.0, var_matches=0.0;
		for (int i=1; i<file_number; i++) {
			var_time += (mean_time-time_ind[i])*(mean_time-time_ind[i]);
			var_matches += (mean_matches-matches_ind[i])*(mean_matches-matches_ind[i]);
			var_error += (mean_error-error_ind[i])*(mean_error-error_ind[i]);
		}
		var_time=Math.sqrt(var_time)/(file_number-1);
		var_error=Math.sqrt(var_error)/(file_number-1);
		var_matches=Math.sqrt(var_matches)/(file_number-1);
		writeData("stadistics", feat_dir, file_number, time_ind, matches_ind, error_ind);
		System.out.println("Time (mean): "+mean_time/1000+"s Error (mean)="+mean_error+"m Matches(mean)="+mean_matches);
		System.out.println("Time (var): "+var_time/1000+"s Error (var)="+var_error+"m Matches(var)="+var_matches);*/
		double meanTras=0.0, varTras=0.0;
		double meanAng=0.0, varAng=0.0;
		for (int i=1; i<file_number;i=i+value) {
			meanTras += error_tras[i];
			meanAng += error_ang[i];
		}
		meanTras /= contCorrect;
		meanAng /= contCorrect;
		for (int i=1; i<file_number;i=i+value) {
			varTras += (error_tras[i]-meanTras)*(error_tras[i]-meanTras);
			varAng += (error_ang[i]-meanAng)*(error_ang[i]-meanAng);
		}
		varAng /= contCorrect;
		varTras /= contCorrect;
		System.out.println(meanTras+" "+varTras+" "+meanAng+" "+varAng);
	}
	
	public void writeData(String name, String path, int file_number, double []time_ind, double []matches_ind,
			double []error_ind) {
		FileWriter fw = null;
		if(path.charAt(path.length()-1)!='/')
			path += '/';
		try
		{
			fw = new FileWriter(path+name, true);
			if (dataSaved==1) fw.write("ind time matches error\n");

			for(int cont=dataSaved;cont<file_number;cont++)
			{
				fw.write(cont+ "   " + time_ind[cont] + "  "+matches_ind[cont]+" "+error_ind[cont]);
				fw.write(String.valueOf("\n"));
			}
			dataSaved=file_number;

		} catch(IOException e) 
		{
			System.err.println("Error saving Trajectory to: "+path+name);
		} 
		finally {
			if (fw!=null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
