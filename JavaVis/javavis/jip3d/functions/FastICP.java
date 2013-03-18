package javavis.jip3d.functions;

import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Color3f;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyKDTree;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Quaternion;
import javavis.jip3d.geom.Segment3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javavis.jip3d.gui.dataobjects.SegmentSet3D;

/**
 * It implements a improvement of the Besl & McKay ICP algorithm.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class FastICP extends Function3D {
	
	double error;
	MyTransform3D tr3d_result;

	public FastICP() {
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Egomotion;
		tr3d_result = null;

		ParamScrData p1 = new ParamScrData("Next Object");
		ParamInt p2 = new ParamInt("Iterations");
		p2.setValue(60);
		ParamBool p3 = new ParamBool("Intermediate");
		p3.setValue(false);
		ParamFloat p4 = new ParamFloat("Tx");
		p4.setValue(0.0f);
		ParamFloat p5 = new ParamFloat("Ty");
		p5.setValue(0.0f);
		ParamFloat p6 = new ParamFloat("Tz");
		p6.setValue(0.0f);
		ParamFloat p7 = new ParamFloat("Ax");
		p7.setValue(0.0f);
		ParamFloat p8 = new ParamFloat("Ay");
		p8.setValue(0.0f);
		ParamFloat p9 = new ParamFloat("Az");
		p9.setValue(0.0f);
		ParamBool p10 = new ParamBool("Verbose");
		p10.setValue(true);
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
		addParam(p8);
		addParam(p9);
		addParam(p10);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		int iterations = this.getParamValueInt("Iterations");
		ScreenData scenedata = this.getParamValueScrData("Next Object");
		double error_prev;
		boolean intermediate = this.getParamValueBool("Intermediate");
		boolean verbose = getParamValueBool("Verbose");
		
		double Tx = getParamValueFloat("Tx");
		double Ty = getParamValueFloat("Ty");
		double Tz = getParamValueFloat("Tz");
		double Ax = getParamValueFloat("Ax");
		double Ay = getParamValueFloat("Ay");
		double Az = getParamValueFloat("Az");

		MyTransform3D tr3d;
    	tr3d = new MyTransform3D(Tx, Ty, Tz, Ax, Ay, Az);
    	tr3d = new MyTransform3D();

		MyTransform3D auxT;
    	Point3D []sceneTr;
    	Object []scene;
    	ArrayList<Pair> closest;
    	int count=0;

    	double prog_inc = 100.0/iterations;

    	scene = scenedata.elements();

		count = 0;
		error = Double.MAX_VALUE;
		do {
			count++;
			error_prev = error;
			//Apply the transformation to the set of data
			sceneTr = applyTrans (scene, tr3d);
			//find the closest
			closest = findClosest(sceneTr, sceneTr, scr_data.getData(), 2200);

			//save the partial result
			if(intermediate)
				showPairs(closest, count);

			//obtain the transformation depending on the closest
			auxT = calcTrans (closest);
			//update total transformation
			tr3d.applyTransform(auxT);

			progress += prog_inc;
		} while (count<iterations && Math.abs(error-error_prev)>0.0001);
		sceneTr = applyTrans (scene, tr3d);
		tr3d_result = tr3d;

		PointSet3D resulttras = new PointSet3D(new ScreenOptions());
		resulttras.name = "ICPBasic";
		resulttras.scr_opt.width = 1;
		resulttras.scr_opt.global_color = true;
		resulttras.scr_opt.color = new Color3f(0,1,0);
		for(Point3D point: sceneTr)
			resulttras.insert(point);
		result_list.add(resulttras);
		if(verbose) System.out.println(tr3d);
	}
	
	public MyTransform3D getTransform()
	{
		return tr3d_result;
	}

	public MyTransform3D calcTrans(ArrayList<Pair> pairs)
	{
		MyTransform3D ret;
		Quaternion quat;
		double [][]sigmapx = new double[3][3];
		double [][]mean = new double[3][3];
		double []delta = new double[3];
		double [][]Q = new double[4][4];
		double meanx, meany, meanz;
		int size = pairs.size();
		double mean2x, mean2y, mean2z;
		Point3D modelP, sceneP;
		double trace;
		DenseDoubleMatrix2D A;
		DoubleMatrix2D V;
		DoubleMatrix2D D;
		EigenvalueDecomposition EVD;
		Point3D meanM, meanS;
		int best;
		double value;
		double var_total = 0;

		meanx = meany = meanz = 0;
		mean2x = mean2y = mean2z = 0;
		sigmapx[0][0] = sigmapx[0][1] = sigmapx[0][2] = 0;
		sigmapx[1][0] = sigmapx[1][1] = sigmapx[1][2] = 0;
		sigmapx[2][0] = sigmapx[2][1] = sigmapx[2][2] = 0;

		for(Pair p: pairs)
		{
			modelP = p.p_model;
			sceneP = p.p_scene;
			meanx += sceneP.getX() * p.dist;
			meany += sceneP.getY() * p.dist;
			meanz += sceneP.getZ() * p.dist;
			mean2x += modelP.getX() * p.dist;
			mean2y += modelP.getY() * p.dist;
			mean2z += modelP.getZ() * p.dist;
			var_total += p.dist;
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

		meanx /= var_total;
		meany /= var_total;
		meanz /= var_total;
		mean2x /= var_total;
		mean2y /= var_total;
		mean2z /= var_total;
		mean[0][0] = meanx * mean2x; mean[0][1] = meanx * mean2y; mean[0][2] = meanx * mean2z;
		mean[1][0] = meany * mean2x; mean[1][1] = meany * mean2y; mean[1][2] = meany * mean2z;
		mean[2][0] = meanz * mean2x; mean[2][1] = meanz * mean2y; mean[2][2] = meanz * mean2z;

		for(int i=0;i<3;i++)
			for(int j=0;j<3;j++)
				sigmapx[i][j] = (sigmapx[i][j]- mean[i][j]) / size;
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
		for(int i=1;i<4;i++)
			if(D.getQuick(i, i)>value)
			{
				value = D.getQuick(i, i);
				best = i;
			}
		
		quat = new Quaternion(V.getQuick(0, best), V.getQuick(1, best), V.getQuick(2, best), V.getQuick(3, best), 0, 0, 0);
		ret =  quat.getTransform();

		meanS = new Point3D(meanx, meany, meanz);
		meanM = new Point3D(mean2x, mean2y, mean2z);
		meanS.applyTransform(ret);

		ret.setTranslation((meanM.subPoint(meanS)).getCoords());

		return ret;
	}

    public ArrayList<Pair> findClosest(Object[] scenetr, Object []scene, MyKDTree model, int max) throws JIPException
    {
    	Point3D p_scene;
    	Point3D closest;
    	int count;
    	ArrayList<Pair> ret = new ArrayList<Pair>();
    	ArrayList<Pair> candidates = new ArrayList<Pair>();
    	error = 0.0;
    	Random generator = new Random(System.currentTimeMillis());
    	int pos, size;
    	double dist;
    	double mean = 0;
    	double var = 0;
    	double aux, weight;

    	for(count=0;count<max;count++)
    	{
    		try {
    			pos = generator.nextInt(scene.length);
        		p_scene = (Point3D)scenetr[pos];
    			closest = (Point3D)model.nearest(p_scene.getCoords());
    			dist = p_scene.getDistance(closest);
    			mean += dist;
    			candidates.add(new Pair(closest, (Point3D)scene[pos], dist));
    		} catch(Exception e)
    		{
    			throw new JIPException(e.getMessage());
    		}
    	}
    	size = candidates.size();
    	mean /= size;

    	for(Pair p:candidates)
    	{
    		aux = p.dist - mean;
    		var += aux * aux;
    	}
    	var /= size;
    	var = Math.sqrt(var);

    	for(Pair p:candidates)
    	{
    		if(p.dist<var)
    		{
    			dist = p.dist - mean;
    			weight = Math.exp(-(dist*dist)/var);
    			p.dist = weight;
    			ret.add(p);
    			error += p.dist;
    		}
    	}
    	error /= ret.size();
    	return ret;
    }

	private Point3D[] applyTrans(Object[] source, MyTransform3D tr3d)
	{
		Point3D []ret;
		int count, len;

		len = source.length;
		ret = new Point3D[len];

		for(count=0;count<len;count++)
		{
			ret[count] = new Point3D((Point3D)source[count]);
			ret[count].applyTransform(tr3d);
		}

		return ret;
	}

	/**
	 * Class Pair.
	 * @author  Miguel Cazorla
	 */
	private class Pair
	{
		public Point3D p_model;
		public Point3D p_scene;
		double dist;

		public Pair(Point3D m, Point3D s, double d)
		{
			p_model = m;
			p_scene = s;
			dist = d;
		}
	}

	private void showPairs(ArrayList<Pair> pairs, int count)
	{
		PointSet3D points;
		SegmentSet3D segments;
		Point3D point;

		points = new PointSet3D(new ScreenOptions());
		points.name = "Scene"+count;

		segments = new SegmentSet3D(new ScreenOptions());
		segments.name = "Pairs"+count;

		for(Pair p: pairs)
		{
			point = new Point3D(p.p_scene);
			points.insert(point);
			segments.insert(new Segment3D(point, p.p_model));
		}

		result_list.add(points);
		result_list.add(segments);
	}

}
