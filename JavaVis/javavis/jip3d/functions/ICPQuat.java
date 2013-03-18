package javavis.jip3d.functions;

import java.util.ArrayList;

import javax.vecmath.Color3f;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyKDTree;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Quaternion;
import javavis.jip3d.gui.dataobjects.PointSet3D;

/**
 * It implements the Besl & McKay ICP algorithm. A Singular Value Decomposition based method is used 
 * for computing the rotation that best aligns the two set of points. The best rotation is 
 * computed by a quaternions based method.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class ICPQuat extends Function3D {
	
	double error;
	Point3D modelM, sceneM;
	MyTransform3D tr3d_result;
	
	public ICPQuat() {
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Egomotion;

		ParamScrData p1 = new ParamScrData("Next Object");
		ParamInt p2 = new ParamInt("Iterations");
		p2.setValue(40);

		this.addParam(p1);
		this.addParam(p2);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		int iterations = this.getParamValueInt("Iterations");
		ScreenData scenedata = this.getParamValueScrData("Next Object");
		double error_prev;

		MyTransform3D tr3d;
    	Point3D []sceneTr;
    	Object []scene, model;
    	ArrayList<Pair> closest;
    	int count=0;
    	Object []elements;

    	double prog_inc = 100.0/iterations;

    	scene = scenedata.elements();
    	double x = 0;
    	double y = 0;
    	double z = 0;
    	for(count=0;count<scene.length;count++)
    	{
    		x += ((Point3D)scene[count]).getX();
    		y += ((Point3D)scene[count]).getY();
    		z += ((Point3D)scene[count]).getZ();
    	}
    	x /= scene.length;
    	y /= scene.length;
    	z /= scene.length;
    	sceneM = new Point3D(x, y, z);
    	System.out.println("MEdia de la escena: "+x+" "+y+" "+z);
    	x = y = z = 0;
    	elements = scr_data.elements(); //model

    	model = new Point3D[elements.length];
    	for(count=0;count<elements.length;count++)
    	{
    		model[count] = (Point3D)elements[count];
    		x += ((Point3D)model[count]).getX();
    		y += ((Point3D)model[count]).getY();
    		z += ((Point3D)model[count]).getZ();
    	}
    	x /= model.length;
    	y /= model.length;
    	z /= model.length;
    	modelM = new Point3D(x, y, z);
    	System.out.println("MEdia del modelo: "+x+" "+y+" "+z);

    	tr3d = new MyTransform3D();
		count = 0;
		error = Double.MAX_VALUE;
		do {
			count++;
			error_prev = error;
			//Apply the transformation to the data set.
			sceneTr = applyTrans (scene, tr3d);
			//Find the closest points
			closest = findClosest(sceneTr, scene, scr_data.getData());
			//Calculate the transformation from the closest points
			tr3d = calcTrans (closest);

			progress += prog_inc;
			//Finish when we have 30 iterations or the error has fallen below the threshold
		} while (count<iterations && Math.abs(error-error_prev)>0.0001);
		
		System.out.println("Iterations: "+count+"\n"+tr3d.toString()+"\n");

		sceneTr = applyTrans (scene, tr3d);
		PointSet3D resulttras = new PointSet3D(new ScreenOptions());
		resulttras.name = "ICPBasic";
		resulttras.scr_opt.width = 1;
		resulttras.scr_opt.global_color = true;
		resulttras.scr_opt.color = new Color3f(0,1,0);
		for(Point3D point: sceneTr)
			resulttras.insert(point);
		result_list.add(resulttras);
		tr3d_result = tr3d;
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

		meanx = meany = meanz = 0;
		mean2x = mean2y = mean2z = 0;
		sigmapx[0][0] = sigmapx[0][1] = sigmapx[0][2] = 0;
		sigmapx[1][0] = sigmapx[1][1] = sigmapx[1][2] = 0;
		sigmapx[2][0] = sigmapx[2][1] = sigmapx[2][2] = 0;

		for(Pair p: pairs)
		{
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
				sigmapx[i][j] = (sigmapx[i][j]  / size) - mean[i][j];
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
		meanS = new Point3D(sceneM);
		
		meanM = new Point3D(mean2x, mean2y, mean2z);
		meanM = new Point3D(modelM);
		meanS.applyTransform(ret);
		
		ret.setTranslation((meanM.subPoint(meanS)).getCoords());

		return ret;
	}

    public ArrayList<Pair> findClosest(Object[] scenetr, Object []scene, MyKDTree model) throws JIPException
    {
    	Point3D p_scene;
    	Point3D closest;
    	int count;
    	ArrayList<Pair> ret = new ArrayList<Pair>();
    	error = 0.0;
    	for(count=0;count<scene.length;count++)
    	{
    		try {
        		p_scene = (Point3D)scenetr[count];
    			closest = (Point3D)model.nearest(p_scene.getCoords());
    			error += p_scene.getDistance(closest);
    			ret.add(new Pair(closest, (Point3D)scene[count]));
    		} catch(Exception e) {
    			throw new JIPException(e.getMessage());
    		}
    	}
    	error /= scene.length;
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
	 * @author Miguel Cazorla
	 */
	private class Pair
	{
		public Point3D p_model;
		public Point3D p_scene;

		public Pair(Point3D m, Point3D s)
		{
			p_model = m;
			p_scene = s;
		}
	}

}
