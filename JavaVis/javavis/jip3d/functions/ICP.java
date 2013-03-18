package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.MyKDTree;
import javavis.jip3d.gui.dataobjects.PointSet3D;

import javax.vecmath.Color3f;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;

/**
 * It implements the Besl & McKay ICP algorithm. A Singular Value Decomposition based method is used 
 * for computing the rotation that best aligns the two set of points.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class ICP extends Function3D {

	double error;
	MyTransform3D tr3d_result;

	public ICP() {
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Egomotion;

		ParamScrData p1 = new ParamScrData("Next Object");
		ParamInt p2 = new ParamInt("Iterations");
		p2.setValue(40);
		ParamBool p3 = new ParamBool("Verbose");
		p3.setValue(true);

		this.addParam(p1);
		this.addParam(p2);
		this.addParam(p3);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		int iterations = this.getParamValueInt("Iterations");
		ScreenData scenedata = this.getParamValueScrData("Next Object");
		double error_prev;
		boolean verbose = getParamValueBool("Verbose");

		MyTransform3D auxT;
    	Point3D []sceneTr, sceneSubs;
    	MyKDTree modelSubs;
    	Point3D []scene, model;
    	Point3D modelM, sceneM;
    	Point3D []closest;
    	int count=0;
    	Object []elements;

    	double prog_inc = 100.0/iterations;

    	elements = scenedata.elements();
    	scene = new Point3D[elements.length];
    	for(count=0;count<elements.length;count++)
    		scene[count] = (Point3D)elements[count];

    	elements = scr_data.elements(); //model
    	model = new Point3D[elements.length];
    	for(count=0;count<elements.length;count++)
    		model[count] = (Point3D)elements[count];

    	//Calculate the average of the model and the scene. This result deduct to the set of points
    	//to avoid having to recalculate
    	modelM=calcMean(model);
    	modelSubs=subsMeanModel(model,modelM);

    	sceneM=calcMean(scene);
    	sceneSubs=subsMean(scene,sceneM);
		//We managed to find the translation between two data set.
    	//It has to find the transformation. We start from a initial register.
		auxT = new MyTransform3D();
		//The algorithm is calculating iteratively.
		//First, the closest points
		count = 0;
		error = Double.MAX_VALUE;
		do {
			count++;
			error_prev = error;
			//Apply the transformation to the data set.
			sceneTr = applyTrans (scene, auxT);

			//Find the closest points
			closest = findClosest(sceneTr, modelSubs, model);

			//Calculate the transformation from the closest points
			auxT = calcTrans (closest, sceneSubs, sceneM, modelM);

			progress += prog_inc;
			//Finish when we have 30 iterations or the error has fallen below the threshold
		} while (count<iterations && Math.abs(error-error_prev)>0.0001); 
		if(verbose)
			System.out.println("Iterations: "+count+"\n"+auxT.toString()+"\n");
		sceneTr = applyTrans (scene, auxT);

		tr3d_result = auxT;
		PointSet3D resulttras = new PointSet3D(new ScreenOptions());
		resulttras.name = "ICP";
		resulttras.scr_opt.width = 1;
		resulttras.scr_opt.global_color = true;
		resulttras.scr_opt.color = new Color3f(0,1,0);
		for(Point3D point: sceneTr)
			resulttras.insert(point);
		result_list.add(resulttras);

	}

	public MyTransform3D getTransform()
	{
		return tr3d_result;
	}

	private MyTransform3D calcTrans (Point3D []modelR, Point3D []sceneR,
    		Point3D sceneM, Point3D modelM) {
    	MyTransform3D auxT;
		double matrix[][]=new double [3][3];
		Point3D modelP, sceneP;

		matrix[0][0]=0.00d; matrix[0][1]=0.00d; matrix[0][2]=0.00d;
		matrix[1][0]=0.00d; matrix[1][1]=0.00d; matrix[1][2]=0.00d;
		matrix[2][0]=0.00d; matrix[2][1]=0.00d; matrix[2][2]=0.00d;
		for (int i=0; i<sceneR.length; i++) {
			modelP=modelR[i];
			sceneP=sceneR[i];
			matrix[0][0]+=sceneP.getX()*modelP.getX();
			matrix[0][1]+=sceneP.getX()*modelP.getY();
			matrix[0][2]+=sceneP.getX()*modelP.getZ();
			matrix[1][0]+=sceneP.getY()*modelP.getX();
			matrix[1][1]+=sceneP.getY()*modelP.getY();
			matrix[1][2]+=sceneP.getY()*modelP.getZ();
			matrix[2][0]+=sceneP.getZ()*modelP.getX();
			matrix[2][1]+=sceneP.getZ()*modelP.getY();
			matrix[2][2]+=sceneP.getZ()*modelP.getZ();
		}

		DenseDoubleMatrix2D mat= new DenseDoubleMatrix2D(matrix);
		SingularValueDecomposition SVD = new SingularValueDecomposition(mat);

		//Calculate the matrix U and V
		DenseDoubleMatrix2D UT, U, V, X;
		X=new DenseDoubleMatrix2D(3,3);

		U=(DenseDoubleMatrix2D)SVD.getU();
		V=(DenseDoubleMatrix2D)SVD.getV();
		UT=(DenseDoubleMatrix2D)U.viewDice(); //Calculate the transposed
		V.zMult(UT,X);

		Algebra algebra = new Algebra();
		if (algebra.det(X)< -0.9) {
			V.setQuick(0,2,-V.getQuick(0,2));
			V.setQuick(1,2,-V.getQuick(1,2));
			V.setQuick(2,2,-V.getQuick(2,2));
			V.zMult(UT,X);
		}

		auxT = new MyTransform3D(X);
		Point3D sceneRaux = new Point3D(sceneM);
		sceneRaux.applyTransform(auxT);
		double tx=modelM.getX()-sceneRaux.getX();
		double ty=modelM.getY()-sceneRaux.getY();
		double tz=modelM.getZ()-sceneRaux.getZ();
		auxT.setTranslation(tx, ty, tz);
    	return auxT;
    }

    //Return the vector with the point number in the other set which is the closest
    //Here is the place where you have to put the KD tree
    private Point3D[] findClosest (Point3D []scene, MyKDTree modeltree, Point3D []model) {
    	Point3D []closests = new Point3D[scene.length];
		double mindist;
		Point3D closest;
		ArrayList<Point3D> list = new ArrayList<Point3D>();
		for(int count=0;count<model.length;count++)
			list.add(model[count]);

		error=0.0;
		try {
			for (int p=0; p<scene.length; p++) {
				closest = (Point3D)modeltree.nearest(scene[p].getCoords());
				mindist = scene[p].getDistance(closest);
				closests[p] = closest;
				error+=mindist;
			}
    	} catch(Exception e) {
    		System.out.println("ICP::findClosestTree Error. Wrong key size");
    	}
    	error /= scene.length;
    	return closests;
    }

	private Point3D[] applyTrans(Point3D[] source, MyTransform tr2d)
	{
		Point3D []ret;
		int count, len;

		len = source.length;
		ret = new Point3D[len];

		for(count=0;count<len;count++)
		{
			ret[count] = new Point3D(source[count]);
			ret[count].applyTransform(tr2d);
		}

		return ret;
	}


    private Point3D[] subsMean (Point3D []set, Point3D mean) {
    	Point3D []setR=new Point3D[set.length];
    	for (int i=0; i<set.length; i++) {
    		setR[i] = set[i].subPoint(mean);
    	}
    	return setR;
    }

    /**
     * This method has been created to connect the set of points with the KD tree. The KD Tree 
     * returned has like keys the original point positions.
     * @param set
     * @param mean
     * @return
     */
    private MyKDTree subsMeanModel (Point3D []set, Point3D mean) {
    	MyKDTree ret = new MyKDTree(3);
    	Point3D TPoint;
    	for (int i=0; i<set.length; i++) {
    		TPoint = set[i].subPoint(mean);
    		ret.insert(set[i].getCoords(), TPoint);
    	}
    	return ret;
    }

    private Point3D calcMean (Point3D []set) {
    	double x, y, z;
    	x=0.0;
		y=0.0;
		z=0.0;
    	for (int i=0; i<set.length; i++) {
    		x += set[i].getX();
			y += set[i].getY();
			z += set[i].getZ();
    	}
    	x /= set.length;
		y /= set.length;
		z /= set.length;

    	Point3D paux = new Point3D(x, y, z);
    	return paux;
    }

}
