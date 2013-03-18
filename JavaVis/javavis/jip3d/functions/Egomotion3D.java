package javavis.jip3d.functions;

import java.util.ArrayList;

import javax.vecmath.Color3f;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Segment3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;
import javavis.jip3d.gui.dataobjects.SegmentSet3D;

/**
 * It calculates the best transformation from two sets of planes. It is used for computing 
 * Egomotion between two sets of 3D plane patches.<br />
 * The screen data must be a set of 3D planes.<br />
 */
public class Egomotion3D extends Function3D {
	
	private double alpha;
	private MyTransform3D transform;
	private double error;

	public Egomotion3D() {
		super();
		this.allowed_input = ScreenOptions.tPLANARSET3D;
		this.group = Function3DGroup.Egomotion;

		transform = null;
		error = Double.MAX_VALUE;
		alpha = 0.025;

		ParamScrData p1 = new ParamScrData("Model");
		ParamFloat p2 = new ParamFloat("Alpha");
		p2.setValue(0.15f);
		ParamInt p3 = new ParamInt("Iterations");
		p3.setValue(75);
		ParamBool p4 = new ParamBool("Intermediate");
		p4.setValue(false);
		ParamBool p5 = new ParamBool("Verbose");
		p5.setValue(true);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
	}

	@Override
	public void proccessData(ScreenData scene) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		ScreenData model = this.getParamValueScrData("Model");
		alpha = this.getParamValueFloat("Alpha");
		int iterations = this.getParamValueInt("Iterations");
		int count = 0;
		double prev_error;
		MyTransform3D tr3d = new MyTransform3D();
		Object []elements = model.elements();
		Object []model_tr;
		double []translation;
		boolean intermediate = this.getParamValueBool("Intermediate");
		boolean verbose = getParamValueBool("Verbose");
		ArrayList<Pair> pairs;
		double prog_inc = 50.0/iterations;

		do {
			progress += prog_inc;
			count++;
			prev_error = error;
			model_tr = applyTransform(elements, tr3d);
			pairs = findPairs(elements, model_tr, scene.elements(), count);

			if(intermediate)
				showPairs(pairs, count);

			tr3d = computeRotation(pairs);
			error = tr3d.getAngX()*tr3d.getAngX() + tr3d.getAngY()*tr3d.getAngY() + tr3d.getAngZ()*tr3d.getAngZ();
		} while(count<iterations && Math.abs(error-prev_error)>0.0002);
		
		if(verbose) System.out.print("Iteraciones: "+count);

		progress = 50;
		Object []rotated_elements = applyTransform(elements, tr3d);
		MyTransform3D tr3d_new = new MyTransform3D();
		MyTransform3D tr3d_inc = new MyTransform3D();;
		count = 0;
		error = Double.MAX_VALUE;
		do {
			count++;
			progress += prog_inc;
			prev_error = error;
			model_tr = applyTransform(rotated_elements, tr3d_new);
			//recompute pairs but with rotated model patches
			pairs = findPairsTrans(model_tr, model_tr, scene.elements(), count);
			//compute translation
			translation = computeTranslation(pairs);
			if(intermediate)
			{
				showPairs(pairs, count);
				System.out.println("partial tr: "+translation[0]+", "+translation[1]+", "+translation[2]);
			}
			tr3d_inc.setTranslation(translation);
			tr3d_new.applyTransform(tr3d_inc);
			error = (translation[0]*translation[0] + translation[1]*translation[1] + translation[2]*translation[2]);
		} while(count<iterations && error>0.00025);
		
		if(verbose) System.out.println(", "+count);
		
		tr3d.setTranslation(tr3d_new.getTranslation());
		model_tr = applyTransform(elements, tr3d);

		transform = tr3d;

		PlaneSet3D resulttras = new PlaneSet3D(new ScreenOptions());
		resulttras.name = "egomotion3D";
		resulttras.scr_opt.width = 1;
		resulttras.scr_opt.color = new Color3f(0,1,0);
		resulttras.scr_opt.global_color = true;
		
		for(count=0;count<model_tr.length;count++)
			resulttras.insert((Plane3D)model_tr[count]);
		result_list.add(resulttras);
		if(verbose) System.out.println("T: "+tr3d.toString()+"\n");
	}

	public ArrayList<Pair> findPairs(Object []model, Object []model_tr, Object []scene, int iteration)
	{
		ArrayList<Pair> ret = new ArrayList<Pair>();
		Plane3D p_model;
		Plane3D p_scene;
		int count;
		double dist;
		double weight;
		double aux;
		double average, variance;

		average = 0;
		for(count=0;count<model_tr.length;count++)
		{
			p_model = (Plane3D)model_tr[count];
			p_scene = findClosest(p_model, scene);
			if(p_scene.getAngle(p_model)<0.9)
			{
				dist = p_scene.getAngle(p_model);
				average += dist;
				ret.add(new Pair((Plane3D)model[count], p_scene, dist));
			}
		}

		int size = ret.size();
		average /= size;
		variance = 0.0000001;
		for(count=0;count<size;count++)
		{
			aux = ret.get(count).distance - average;
			variance += aux * aux;
		}
		variance /= size;
		variance *= 2;
		double normalization = 2* Math.PI * variance;
		for(count=0;count<size;count++)
		{
			dist = ret.get(count).distance - average;
			weight = Math.exp(-(dist*dist)/(variance)) / normalization;
			ret.get(count).distance = weight;
		}
		return ret;
	}

	public ArrayList<Pair> findPairsTrans(Object []model, Object []model_tr, Object []scene, int iteration)
	{
		ArrayList<Pair> ret = new ArrayList<Pair>();
		Plane3D p_model;
		Plane3D p_scene;
		int count;
		double dist;
		double weight;
		double aux;
		double average, variance;

		average = 0;
		for(count=0;count<model_tr.length;count++)
		{
			p_model = (Plane3D)model_tr[count];
			p_scene = findClosestTrans(p_model, scene);
			if(Math.abs(p_model.anglePlane(p_scene))<0.2)
			{
				dist = Math.abs(p_scene.pointDistance(p_model.origin)) + p_scene.getAngle(p_model);
				average += dist;
				ret.add(new Pair((Plane3D)model[count], p_scene, dist));
			}
		}

		int size = ret.size();
		average /= size;
		variance = 0.0000001;
		for(count=0;count<size;count++)
		{
			aux = ret.get(count).distance - average;
			variance += aux * aux;
		}
		variance /= size;
		variance *= 2;
		double normalization = 1; //2* Math.PI * variance;
		for(count=0;count<size;count++)
		{
			dist = ret.get(count).distance - average;
			//this checkout is only for synthetic tests
			if(variance>0)
				weight = Math.exp(-(dist*dist)/(variance)) / normalization;
			else weight = 1;

			ret.get(count).distance = weight;
		}
		return ret;
	}


	/**
	 * This method visits all the Plane3D objects from an array and retrieves who that
	 * have minimum distance to p_scene planar patch
	 * @param p_model
	 * @param scene
	 * @return
	 */
	public Plane3D findClosest(Plane3D p_model, Object[]scene)
	{
		Plane3D ret = null;
		double best_dist = Double.MAX_VALUE;
		double dist;
		Plane3D p_scene;
		int count;

		for(count=0;count<scene.length;count++)
		{
			p_scene = (Plane3D)scene[count];
			dist = calcDist(p_model, p_scene);
			if(dist<best_dist)
			{
				best_dist = dist;
				ret = p_scene;
			}
		}

		return ret;
	}

	/**
	 * This method visits all the Plane3D objects from an array and retrieves who that
	 * have minimum distance to p_scene planar patch
	 * @param p_model
	 * @param scene
	 * @return
	 */
	public Plane3D findClosestTrans(Plane3D p_model, Object[]scene)
	{
		Plane3D ret = null;
		double best_dist = Double.MAX_VALUE;
		double dist;
		Plane3D p_scene;
		int count;

		for(count=0;count<scene.length;count++)
		{
			p_scene = (Plane3D)scene[count];
			dist = calcDist2(p_model, p_scene);
			if(dist<best_dist)
			{
				best_dist = dist;
				ret = p_scene;
			}
		}

		return ret;
	}
	
	
	private double calcDist(Plane3D p_scene, Plane3D p_model)
	{
		double angle = p_scene.getAngle(p_model);
		double ret = p_scene.origin.getDistance(p_model.origin);

		ret *= alpha; //0.025 in poli
		ret += angle;
		return ret;
	}

	private double calcDist2(Plane3D p_scene, Plane3D p_model)
	{
		double angle = p_scene.getAngle(p_model);
		double dist = p_scene.origin.getDistance(p_model.origin);

		return dist + angle;
	}


	public MyTransform3D computeRotation(ArrayList<Pair> pairs)
	{
		MyTransform3D ret = null;
		double [][]matrix = new double [3][3];
		DenseDoubleMatrix2D ddmatrix;
		DoubleMatrix2D U;
		SingularValueDecomposition SVD;

		matrix[0][0] = matrix[0][1] = matrix[0][2] =
			matrix[1][0] = matrix[1][1] = matrix[1][2] =
			matrix[2][0] = matrix[2][1] = matrix[2][2] = 0;

		for(Pair p: pairs)
		{
			matrix[0][0] += p.distance * p.model.vector.getX() * p.scene.vector.getX();
			matrix[0][1] += p.distance * p.model.vector.getX() * p.scene.vector.getY();
			matrix[0][2] += p.distance * p.model.vector.getX() * p.scene.vector.getZ();

			matrix[1][0] += p.distance * p.model.vector.getY() * p.scene.vector.getX();
			matrix[1][1] += p.distance * p.model.vector.getY() * p.scene.vector.getY();
			matrix[1][2] += p.distance * p.model.vector.getY() * p.scene.vector.getZ();

			matrix[2][0] += p.distance * p.model.vector.getZ() * p.scene.vector.getX();
			matrix[2][1] += p.distance * p.model.vector.getZ() * p.scene.vector.getY();
			matrix[2][2] += p.distance * p.model.vector.getZ() * p.scene.vector.getZ();
		}

		ddmatrix = new DenseDoubleMatrix2D(matrix);
		SVD = new SingularValueDecomposition(ddmatrix);

		//Calculate the matrix U and V
		DenseDoubleMatrix2D UT, V, X;
		X=new DenseDoubleMatrix2D(3,3);

		U=(DenseDoubleMatrix2D)SVD.getU();
		V=(DenseDoubleMatrix2D)SVD.getV();
		UT=(DenseDoubleMatrix2D)U.viewDice(); //Calculates the transposed
		V.zMult(UT,X);

		Algebra algebra = new Algebra();
		
		if (algebra.det(X)< 0) { //-0.9
			V.setQuick(0,2,-V.getQuick(0,2));
			V.setQuick(1,2,-V.getQuick(1,2));
			V.setQuick(2,2,-V.getQuick(2,2));
			V.zMult(UT,X);
		}

		ret = new MyTransform3D(X);
		return ret;
	}

	private Vector3D []findMainDirections(Vector3D []vectors)
	{
		Vector3D []ret = new Vector3D[3];
    	int count;
		double [][]auxMatrix = new double [3][3];
		DenseDoubleMatrix2D matrix;
		DoubleMatrix2D U;
		SingularValueDecomposition SVD;
		double []data;

		auxMatrix[0][0] = auxMatrix[0][1] = auxMatrix[0][2] =
			auxMatrix[1][0] = auxMatrix[1][1] = auxMatrix[1][2] =
			auxMatrix[2][0] = auxMatrix[2][1] = auxMatrix[2][2] = 0;

		for(count=0;count<vectors.length;count++)
		{
			data = vectors[count].getCoords();
			auxMatrix[0][0] += data[0] * data[0];
			auxMatrix[0][1] += data[0] * data[1];
			auxMatrix[0][2] += data[0] * data[2];
			auxMatrix[1][1] += data[1] * data[1];
			auxMatrix[1][2] += data[1] * data[2];
			auxMatrix[2][2] += data[2] * data[2];
		}
		auxMatrix[1][0] = auxMatrix[0][1];
		auxMatrix[2][0] = auxMatrix[0][2];
		auxMatrix[2][1] = auxMatrix[1][2];

		matrix = new DenseDoubleMatrix2D(auxMatrix);
		SVD = new SingularValueDecomposition(matrix);
		U = SVD.getU();
		ret[0] = new Vector3D(U.getQuick(0, 0), U.getQuick(1, 0), U.getQuick(2, 0));
		ret[1] = new Vector3D(U.getQuick(0, 1), U.getQuick(1, 1), U.getQuick(2, 1));
		ret[2] = new Vector3D(U.getQuick(0, 2), U.getQuick(1, 2), U.getQuick(2, 2));

		return ret;
	}

	public double[] computeTranslation(ArrayList<Pair> pairs)
	{
		double []ret = new double[3];
		int size = pairs.size();
		Vector3D v;
		Vector3D []vectors = new Vector3D[size]; //translation vectors
		double []norms = new double[size];
		double cos_angle;
		double total1, total2, total3;
		int count;
		Point3D projection;
		double []contribution = new double[3];

		count = 0;
		for(Pair p:pairs)
		{
			projection = p.scene.pointProjection(p.model.origin);
			v = new Vector3D(projection.subPoint(p.model.origin));
			norms[count] = v.module;
			v.normalize();
			vectors[count] = v;
			count++;
		}
		Vector3D []main_directions = findMainDirections(vectors);

		total1 = total2 = total3 = 0;
		contribution[0] = contribution[1] = contribution[2] = 0;
		count = 0;
		for(Pair p:pairs)
		{
			//main direction #1
			cos_angle = main_directions[0].dotProduct(vectors[count]);
			contribution[0] += norms[count] * cos_angle * p.distance;
			total1 += p.distance * Math.abs(cos_angle);

			//main direction #2
			cos_angle = main_directions[1].dotProduct(vectors[count]);
			contribution[1] += norms[count] * cos_angle * p.distance;
			total2 += p.distance * Math.abs(cos_angle);

			//main direction #3
			cos_angle = main_directions[2].dotProduct(vectors[count]);
			contribution[2] += norms[count] * cos_angle * p.distance;
			total3 += p.distance * Math.abs(cos_angle);

			count++;
		}
		if(total1>0)
			contribution[0] /= total1;
		else contribution[0] = 0;
		if(total2>0)
			contribution[1] /= total2;
		else contribution[1] = 0;
		if(total3>0)
			contribution[2] /= total3;
		else contribution[2] = 0;

		ret[0] = main_directions[0].getX() * contribution[0] +
			main_directions[1].getX() * contribution[1] + main_directions[2].getX() * contribution[2];
		ret[1] = main_directions[0].getY() * contribution[0] +
			main_directions[1].getY() * contribution[1] + main_directions[2].getY() * contribution[2];
		ret[2] = main_directions[0].getZ() * contribution[0] +
			main_directions[1].getZ() * contribution[1] + main_directions[2].getZ() * contribution[2];

		return ret;
	}

	/** This function computes the translation vector for aligning two set of matched patches 
	 * like a pondered mean of pair-wise match translation vectors.
	 * @param pairs have the information of matched planar patches
	 * @return three-coordinates of translation vector
	 */
	public double [] computeTranslation2(ArrayList<Pair> pairs)
	{
		double []ret = new double[3];
		double total;
		Vector3D v;
		Point3D projection;
		double contribution;

		ret[0] = ret[1] = ret[2] = 0;
		total = 0;

		for(Pair p: pairs)
		{
			projection = p.scene.pointProjection(p.model.origin);
			v = new Vector3D(projection.subPoint(p.model.origin));
			contribution = p.distance;
			ret[0] += v.getX() * contribution;
			ret[1] += v.getY() * contribution;
			ret[2] += v.getZ() * contribution;
			total +=  contribution;
		}
		if(total>0)
		{
			ret[0] /= total;
			ret[1] /= total;
			ret[2] /= total;
		}

		return ret;
	}

	/** This function computes the translation vector for aligning two set of matched patches 
	 * like the vector between the center of mass of the two set of patches that are involved 
	 * into the matches.
	 * @param pairs have the information of matched planar patches
	 * @return three-coordinates of translation vector
	 */
	public double [] computeTranslation3(ArrayList<Pair> pairs)
	{
		double []centre1 = new double[3];
		double []centre2 = new double[3];
		double total;
		Vector3D v;
		Point3D cm1;
		double contribution;

		centre1[0] = centre1[1] = centre1[2] = 0;
		centre2[0] = centre2[1] = centre2[2] = 0;
		total = 0;

		for(Pair p: pairs)
		{
			contribution = p.distance;
			centre1[0] += p.scene.origin.getX() * contribution;
			centre1[1] += p.scene.origin.getY() * contribution;
			centre1[2] += p.scene.origin.getZ() * contribution;
			centre2[0] += p.model.origin.getX() * contribution;
			centre2[1] += p.model.origin.getY() * contribution;
			centre2[2] += p.model.origin.getZ() * contribution;
			total +=  contribution;
		}

		if(total>0)
		{
			centre1[0] /= total;
			centre1[1] /= total;
			centre1[2] /= total;
			centre2[0] /= total;
			centre2[1] /= total;
			centre2[2] /= total;
		}

		cm1 = new Point3D(centre1);
		v = new Vector3D(cm1.subPoint(new Point3D(centre2)));
		return v.getCoords();
	}


	/**
	 * Class Pair
	 * @author  Miguel Cazorla
	 */
	public class Pair
	{
		public Plane3D model;
		public Plane3D scene;
		public double distance;

		public Pair()
		{
			model = null;
			scene = null;
			distance = 0.0;
		}

		public Pair(Plane3D m, Plane3D s, double d)
		{
			model = m;
			scene = s;
			distance = d;
		}
	}

	public Object[] applyTransform(Object[] scene, MyTransform3D tr3d)
	{
		Plane3D plane;
		int count;
		Object []ret = new Object[scene.length];

		for(count=0;count<scene.length;count++)
		{
			plane = new Plane3D((Plane3D)scene[count]);
			plane.applyTransform(tr3d);
			ret[count] = plane;
		}
		return ret;
	}

	public MyTransform3D getTransform()
	{
		return transform;
	}

    public Plane3D[] subsMean (Object []set, Point3D mean) {
    	Plane3D []setR=new Plane3D[set.length];
    	Plane3D plane;
    	Point3D origin;
    	for (int i=0; i<set.length; i++) {
    		plane = (Plane3D)set[i];
    		origin = plane.origin.subPoint(mean);
    		setR[i] = new Plane3D(new Normal3D(origin,plane.vector));
    	}
    	return setR;
    }

    public Point3D calcMean (Object []set) {
    	double x, y, z;
    	Plane3D plane;

    	x=0.0;
		y=0.0;
		z=0.0;
    	for (int i=0; i<set.length; i++) {
    		plane = (Plane3D)set[i];
    		x += plane.origin.getX();
			y += plane.origin.getY();
			z += plane.origin.getZ();
    	}
    	x /= set.length;
		y /= set.length;
		z /= set.length;

    	Point3D paux = new Point3D(x, y, z);
    	return paux;
    }

    private void showPairs(ArrayList<Pair> pairs, int count)
    {
    	SegmentSet3D pairsSegment;
    	SegmentSet3D mov;
    	PlaneSet3D pset;
    	pairsSegment = new SegmentSet3D(new ScreenOptions());
    	mov = new SegmentSet3D(new ScreenOptions());
    	pset = new PlaneSet3D(new ScreenOptions());
    	pairsSegment.name = "pairs"+count;
    	pairsSegment.scr_opt.width = 1;
    	pairsSegment.scr_opt.color = new Color3f(1,0,0);
    	mov.name = "mov"+count;
    	mov.scr_opt.width = 1;
    	mov.scr_opt.color = new Color3f(0,0.5f,1);
    	pset.name = "patches"+count;
    	pset.scr_opt.width = 1;
    	pset.scr_opt.color = new Color3f(1,0.5f,0);
    	for(Pair p: pairs)
    	{
    		pairsSegment.insert(new Segment3D(new Point3D(p.model.origin), p.scene.origin));
    		mov.insert(new Segment3D(new Point3D(p.model.origin), p.scene.pointProjection(p.model.origin)));
    		pset.insert(new Plane3D(p.model));
    	}
    	result_list.add(pairsSegment);
    	result_list.add(mov);
    	result_list.add(pset);

    }

}
