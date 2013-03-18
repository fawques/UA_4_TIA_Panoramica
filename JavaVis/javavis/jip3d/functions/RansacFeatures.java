package javavis.jip3d.functions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamObject;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Feature2D;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Quaternion;
import javavis.jip3d.geom.Segment3D;
import javavis.jip3d.gui.dataobjects.FeatureSet2D;
import javavis.jip3d.gui.dataobjects.SegmentSet3D;

/**
 * It applies <a href="http://en.wikipedia.org/wiki/RANSAC">RANSAC method</a> to 2 sets of 2D 
 * features.<br />
 * The screen data must be a set of 2D features.<br />
 */
public class RansacFeatures extends Function3D {
	
	private MyTransform3D finalTransform;
	private double error;
	int finalMatches;

	public RansacFeatures() {
		super();
		this.allowed_input = ScreenOptions.tFEATURESET2D;
		this.group = Function3DGroup.Egomotion;

		finalTransform = null;
		error = Double.MAX_VALUE;

		ParamScrData p0 = new ParamScrData("Next Object");
		ParamInt p1 = new ParamInt("Iterations");
		p1.setValue(10);
		ParamInt p2 = new ParamInt("Inliers");
		p2.setValue(4);
		ParamFloat p3 = new ParamFloat("threshDescriptor");
		p3.setValue(25000000.0f);
		ParamInt p4 = new ParamInt("minElements");
		p4.setValue(5);
		ParamFloat p5 = new ParamFloat("dist_thresh");
		p5.setValue(0.03f);
		ParamObject p6 = new ParamObject("fileWriter");
		try {
			p6.setValue(new FileWriter("hh"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.addParam(p0);
		this.addParam(p1);
		this.addParam(p2);
		this.addParam(p3);
		this.addParam(p4);
		this.addParam(p5);
		this.addParam(p6);
	}

	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		ScreenData scenedata = this.getParamValueScrData("Next Object");
		double threshDescriptor = this.getParamValueFloat("threshDescriptor");
		double dist_thresh = this.getParamValueFloat("dist_thresh");
		int iterations = this.getParamValueInt("Iterations");
		int minElem = this.getParamValueInt("minElements");
		int inliers = this.getParamValueInt("Inliers");
		FileWriter fw = (FileWriter)this.getParamValueObject("fileWriter");

    	Object[] model, scene;
    	ArrayList<Pair> closest, consensus=null, bestConsensus=null;
    	MyTransform3D auxT= new MyTransform3D(), bestT=null;
    	double bestError=Double.MAX_VALUE;

    	scene = scenedata.elements();
    	model = scr_data.elements();

		//Find the closest points
		closest = findMatches(model, scene, threshDescriptor);

    	for (int i=0; i<iterations; i++) {
    		consensus= new ArrayList<Pair>();
    		//First, select n inliers randomly. We shuffle the matches and then select the 
    		//first n as the inliers
    		Collections.shuffle(closest);
    		//Find the consensus transformation
    		calcTrans (closest, inliers, auxT);
    		
    		//Find the matches that satisfied the transformation
    		for (int count=0; count<inliers; count++) 
    			consensus.add(closest.get(count));
    		for (int count=inliers; count<closest.size(); count++) 
    			if (distTransf(auxT, closest.get(count)) < dist_thresh) 
    				consensus.add(closest.get(count));
    		
    		// If the number of elements fitting the transformation is above the threshold
    		if (consensus.size() >= minElem) {
    			calcTrans (consensus, auxT);
    			double auxError = calculateError (consensus, auxT);
    			if (auxError<bestError) {
    				bestT=new MyTransform3D(auxT);
    				bestError=auxError;
    				bestConsensus=consensus;
    			}
    		}
    	}
    	error = bestError;
    	if (bestT!=null && bestError/bestConsensus.size() < 3*dist_thresh) {
    		System.out.println("Number of matches:"+bestConsensus.size());
	    	Feature2D []sceneTr = applyTrans (scene, bestT);
			FeatureSet2D resulttras = new FeatureSet2D(new ScreenOptions());
			resulttras.name = "ICPBasic";
			resulttras.scr_opt.width = 1;
			resulttras.scr_opt.global_color = true;
			resulttras.scr_opt.color = new Color3f(0,1,0);
			for(Feature2D point: sceneTr)
				resulttras.insert(point);
			result_list.add(resulttras);
			showPairs(closest, "First Matching");
			showPairs(applyTransform(bestConsensus, bestT), "Last Matching");
	    	finalMatches = bestConsensus.size();
	    	finalTransform = bestT;
		
			// Write into a file the results
			try {
				fw.write("\n"+bestT.toString()+"\n");
				if (bestConsensus.size()>5) {
					// First, store features with matches
					fw.write(bestConsensus.size()+"\n");
					for (int i=0; i< bestConsensus.size(); i++) {
						fw.write(bestConsensus.get(i).p_scene.toString()+"\n");
					}
					// Then, the rest of features
					fw.write(sceneTr.length-bestConsensus.size()+"\n");
					for (int i=0; i< sceneTr.length; i++) {
						Feature2D f = (Feature2D)sceneTr[i];
						if (!existsFeature (f, bestConsensus)) {
							fw.write(f.toString()+"\n");
						}
					}
				}
				else {
					// None enough matches were found
					fw.write("0\n");
					fw.write(sceneTr.length+"\n");
					for (int i=0; i< sceneTr.length; i++) {
						fw.write(((Feature2D)sceneTr[i]).toString()+"\n");
					}
				}
			} catch (IOException e) {
				System.out.println("Error RansacFeatures:"+e);
			}
    	}
    	else {
    		finalTransform=null;
    		System.out.println("No entro:"+scenedata.name+" "+scr_data.name+" "+(bestT==null?"nulo ":"nonulo ")+"bestError="+bestError);
    	}
	}
	
	public double distTransf (MyTransform3D transf, Pair p) {
		Point3D aux = new Point3D(p.p_scene);
		aux.applyTransform(transf);
		return p.p_model.getDistance(aux);
	}
	
	public double calculateError (ArrayList<Pair> listPoints, MyTransform3D transf) {
		double error=0.0;
		
		for (Pair p : listPoints) {
			error += distTransf(transf, p);
		}
		
		return error;
	}

	
	public ArrayList<Pair> findMatches (Object[] modelSet, Object[] sceneSet, double dist_thresh) throws JIPException {
    	ArrayList<Pair> candidates = new ArrayList<Pair>();
    	double dist, distMin, distMin2;
    	int indexMin=0;
    	Feature2D model, scene;
    	
    	for(int countModel=0;countModel<modelSet.length;countModel++) {
    		distMin=Double.MAX_VALUE;
    		distMin2=Double.MAX_VALUE;
    		model=(Feature2D)modelSet[countModel];
    		
    		for(int contScene=0;contScene<sceneSet.length;contScene++) {
        		scene=(Feature2D)sceneSet[contScene];
				//Take advantage of the Laplacian is case of SURF
				if (model.type==2 && model.laplacian!=scene.laplacian) continue; 
				dist=model.getDistanceDescriptor(scene);
				if (dist < distMin) {
					if (distMin<distMin2) distMin2=distMin;
	    			indexMin=contScene;
	    			distMin=dist;
				}
				else if (dist < distMin2) distMin2=dist;
			}
			if (distMin < 0.6*distMin2 && distMin < dist_thresh) {
        		scene=(Feature2D)sceneSet[indexMin];
				Pair p = new Pair(model, scene);
    			candidates.add(p);
    		}
    	}
    	
		return candidates;
	}
	
	boolean existsFeature (Feature2D f, ArrayList<Pair> closest) {
		for (Pair p : closest) {
			if (p.p_scene == f) {
				return true;
			}
		}
		return false;
	}
	
	ArrayList<Pair> applyTransform (ArrayList<Pair> closest, MyTransform3D tr3d) {
		Feature2D f2d;
		ArrayList<Pair> ret=new ArrayList<Pair>();
		
		for (Pair p : closest) {
			f2d=new Feature2D(p.p_scene);
			f2d.applyTransform(tr3d);
			ret.add(new Pair(p.p_model, f2d));
		}
		return ret;
	}
	
	double calcError (ArrayList<Pair> closest, MyTransform3D tr3d) {
		double er=0.0;
		Feature2D f2d;
		
		for (Pair p : closest) {
			f2d=new Feature2D(p.p_scene);
			f2d.applyTransform(tr3d);
			er=p.p_model.getDistance(f2d);
		}
		return er/closest.size();
	}

	/**
	 * Calculates the transformation from the matches, but using only the first "inliers" 
	 * elements.
	 * @param pairs
	 * @param inliers
	 * @return
	 */
	public void calcTrans(ArrayList<Pair> pairs, int inliers, MyTransform3D transf) {
		Quaternion quat;
		double [][]sigmapx = new double[3][3];
		double [][]mean = new double[3][3];
		double []delta = new double[3];
		double [][]Q = new double[4][4];
		double meanx, meany, meanz;
		int size = pairs.size();
		double mean2x, mean2y, mean2z;
		Feature2D modelP, sceneP;
		double trace;
		DenseDoubleMatrix2D A;
		DoubleMatrix2D V;
		DoubleMatrix2D D;
		EigenvalueDecomposition EVD;
		Point3D meanM, meanS;
		int best;
		double value;
		Pair p;

		meanx = meany = meanz = 0;
		mean2x = mean2y = mean2z = 0;
		sigmapx[0][0] = sigmapx[0][1] = sigmapx[0][2] = 0;
		sigmapx[1][0] = sigmapx[1][1] = sigmapx[1][2] = 0;
		sigmapx[2][0] = sigmapx[2][1] = sigmapx[2][2] = 0;

		for (int i=0; i<inliers && i<pairs.size(); i++) {
			p = pairs.get(i);
			modelP = p.p_model;
			sceneP = p.p_scene;
			meanx += sceneP.getX();
			meany += sceneP.getY();
			meanz += sceneP.getZ();
			mean2x += modelP.getX();
			mean2y += modelP.getY();
			mean2z += modelP.getZ();
			sigmapx[0][0]+=sceneP.getX()*modelP.getX();
			sigmapx[0][1]+=sceneP.getX()*modelP.getY();
			sigmapx[0][2]+=sceneP.getX()*modelP.getZ();
			sigmapx[1][0]+=sceneP.getY()*modelP.getX();
			sigmapx[1][1]+=sceneP.getY()*modelP.getY();
			sigmapx[1][2]+=sceneP.getY()*modelP.getZ();
			sigmapx[2][0]+=sceneP.getZ()*modelP.getX();
			sigmapx[2][1]+=sceneP.getZ()*modelP.getY();
			sigmapx[2][2]+=sceneP.getZ()*modelP.getZ();
		}

		meanx /= size;
		meany /= size;
		meanz /= size;
		mean2x /= size;
		mean2y /= size;
		mean2z /= size;
		mean[0][0] = meanx * mean2x; mean[0][1] = meanx * mean2y; mean[0][2] = meanx * mean2z;
		mean[1][0] = meany * mean2x; mean[1][1] = meany * mean2y; mean[1][2] = meany * mean2z;
		mean[2][0] = meanz * mean2x; mean[2][1] = meanz * mean2y; mean[2][2] = meanz * mean2z;

		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				sigmapx[i][j] = (sigmapx[i][j]- mean[i][j]) / size  ;
		delta[0] = sigmapx[1][2] - sigmapx[2][1];
		delta[1] = sigmapx[2][0] - sigmapx[0][2];
		delta[2] = sigmapx[0][1] - sigmapx[1][0];

		trace = sigmapx[0][0] + sigmapx[1][1] + sigmapx[2][2];

		Q[0][0] = trace; Q[0][1] = delta[0]; Q[0][2] = delta[1]; Q[0][3] = delta[2];
		Q[1][0] = delta[0]; Q[1][1] = sigmapx[0][0] + sigmapx[0][0] - trace; Q[1][2] = sigmapx[0][1] + sigmapx[1][0]; Q[1][3] = sigmapx[0][2] + sigmapx[2][0];
		Q[2][0] = delta[1]; Q[2][1] = sigmapx[1][0] + sigmapx[1][0]; Q[2][2] = sigmapx[1][1] + sigmapx[1][1] - trace; Q[2][3] = sigmapx[1][2] + sigmapx[2][1];
		Q[3][0] = delta[2]; Q[3][1] = sigmapx[2][0] + sigmapx[0][2]; Q[3][2] = sigmapx[2][1] + sigmapx[1][2]; Q[3][3] = sigmapx[2][2] + sigmapx[2][2] - trace;
		A = new DenseDoubleMatrix2D(Q);
		EVD = new EigenvalueDecomposition(A);
		D = EVD.getD();
		V = EVD.getV();
		best = 0;
		value = D.getQuick(0, 0);
		//Look for the maximum eigenvalue
		for (int i=1; i<4; i++)
			if (D.getQuick(i, i)>value) {
				value = D.getQuick(i, i);
				best = i;
			}
		quat = new Quaternion(V.getQuick(0, best), V.getQuick(1, best), V.getQuick(2, best), V.getQuick(3, best), 0, 0, 0);

		quat.insertValues(transf);
		
		meanS = new Point3D(meanx, meany, meanz);
		meanM = new Point3D(mean2x, mean2y, mean2z);
		meanS.applyTransform(transf);

		transf.setTranslation((meanM.subPoint(meanS)).getCoords());
	}
	
	
	public void calcTrans(ArrayList<Pair> pairs, MyTransform3D transf) {
		Quaternion quat;
		double [][]sigmapx = new double[3][3];
		double [][]mean = new double[3][3];
		double []delta = new double[3];
		double [][]Q = new double[4][4];
		double meanx, meany, meanz;
		int tam = pairs.size();
		double mean2x, mean2y, mean2z;
		Feature2D modelP, sceneP;
		double trace;
		DenseDoubleMatrix2D A;
		DoubleMatrix2D V;
		DoubleMatrix2D D;
		EigenvalueDecomposition EVD;
		int best;
		double value;
		Point3D meanM, meanS;

		meanx = meany = meanz = 0;
		mean2x = mean2y = mean2z = 0;
		sigmapx[0][0] = sigmapx[0][1] = sigmapx[0][2] = 0;
		sigmapx[1][0] = sigmapx[1][1] = sigmapx[1][2] = 0;
		sigmapx[2][0] = sigmapx[2][1] = sigmapx[2][2] = 0;

		for (Pair p : pairs) {
			modelP = p.p_model;
			sceneP = p.p_scene;
			meanx += sceneP.getX();
			meany += sceneP.getY();
			meanz += sceneP.getZ();
			mean2x += modelP.getX();
			mean2y += modelP.getY();
			mean2z += modelP.getZ();
			sigmapx[0][0]+=sceneP.getX()*modelP.getX();
			sigmapx[0][1]+=sceneP.getX()*modelP.getY();
			sigmapx[0][2]+=sceneP.getX()*modelP.getZ();
			sigmapx[1][0]+=sceneP.getY()*modelP.getX();
			sigmapx[1][1]+=sceneP.getY()*modelP.getY();
			sigmapx[1][2]+=sceneP.getY()*modelP.getZ();
			sigmapx[2][0]+=sceneP.getZ()*modelP.getX();
			sigmapx[2][1]+=sceneP.getZ()*modelP.getY();
			sigmapx[2][2]+=sceneP.getZ()*modelP.getZ();
		}

		meanx /= tam;
		meany /= tam;
		meanz /= tam;
		mean2x /= tam;
		mean2y /= tam;
		mean2z /= tam;
		mean[0][0] = meanx * mean2x; mean[0][1] = meanx * mean2y; mean[0][2] = meanx * mean2z;
		mean[1][0] = meany * mean2x; mean[1][1] = meany * mean2y; mean[1][2] = meany * mean2z;
		mean[2][0] = meanz * mean2x; mean[2][1] = meanz * mean2y; mean[2][2] = meanz * mean2z;

		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				sigmapx[i][j] = (sigmapx[i][j]- mean[i][j]) / tam  ;
		delta[0] = sigmapx[1][2] - sigmapx[2][1];
		delta[1] = sigmapx[2][0] - sigmapx[0][2];
		delta[2] = sigmapx[0][1] - sigmapx[1][0];

		trace = sigmapx[0][0] + sigmapx[1][1] + sigmapx[2][2];

		Q[0][0] = trace; Q[0][1] = delta[0]; Q[0][2] = delta[1]; Q[0][3] = delta[2];
		Q[1][0] = delta[0]; Q[1][1] = sigmapx[0][0] + sigmapx[0][0] - trace; Q[1][2] = sigmapx[0][1] + sigmapx[1][0]; Q[1][3] = sigmapx[0][2] + sigmapx[2][0];
		Q[2][0] = delta[1]; Q[2][1] = sigmapx[1][0] + sigmapx[1][0]; Q[2][2] = sigmapx[1][1] + sigmapx[1][1] - trace; Q[2][3] = sigmapx[1][2] + sigmapx[2][1];
		Q[3][0] = delta[2]; Q[3][1] = sigmapx[2][0] + sigmapx[0][2]; Q[3][2] = sigmapx[2][1] + sigmapx[1][2]; Q[3][3] = sigmapx[2][2] + sigmapx[2][2] - trace;
		A = new DenseDoubleMatrix2D(Q);
		EVD = new EigenvalueDecomposition(A);
		D = EVD.getD();
		V = EVD.getV();
		best = 0;
		value = D.getQuick(0, 0);
		//Look for the maximum eigenvalue
		for (int i=1; i<4; i++)
			if (D.getQuick(i, i)>value) {
				value = D.getQuick(i, i);
				best = i;
			}
		quat = new Quaternion(V.getQuick(0, best), V.getQuick(1, best), V.getQuick(2, best), V.getQuick(3, best), 0, 0, 0);

		quat.insertValues(transf);
		
		meanS = new Point3D(meanx, meany, meanz);
		meanM = new Point3D(mean2x, mean2y, mean2z);
		meanS.applyTransform(transf);

		transf.setTranslation((meanM.subPoint(meanS)).getCoords());
	}
	
	public int getFinalMatches () {
		return finalMatches;
	}
	
	public double getError() {
		return error;
	}
	
	public MyTransform3D getTransform () {
		return finalTransform;
	}
	
	private Feature2D[] applyTrans(Object[] source, MyTransform3D tr3d)
	{
		Feature2D []ret;
		int count, len;

		len = source.length;
		ret = new Feature2D[len];

		for(count=0;count<len;count++)
		{
			ret[count] = new Feature2D((Feature2D)source[count]);
			ret[count].applyTransform(tr3d);
		}

		return ret;
	}

	/**
	 * Class Pair.
	 * @author  Miguel Cazorla.
	 */
	private class Pair
	{
		public Feature2D p_model;
		public Feature2D p_scene;

		public Pair(Feature2D m, Feature2D s)
		{
			p_model = m;
			p_scene = s;
		}
	}

	private void showPairs(ArrayList<Pair> pairs, String name)
	{
		SegmentSet3D segments;
		Feature2D point;

		segments = new SegmentSet3D(new ScreenOptions());
		segments.name = name;

		for(Pair p: pairs)
		{
			point = new Feature2D(p.p_scene);
			segments.insert(new Segment3D(point, p.p_model));
		}

		result_list.add(segments);
	}

}
