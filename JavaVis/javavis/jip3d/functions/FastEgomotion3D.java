package javavis.jip3d.functions;

import java.util.ArrayList;
import java.util.Random;

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
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Segment3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;
import javavis.jip3d.gui.dataobjects.SegmentSet3D;

/**
 * It calculates the best transformation from two sets of planes. It is used for computing 
 * Egomotion between two sets of 3D plane patches. It is an improvement of the
 * Egomotion 3D, developed for the Diego Viejo Hernando's doctoral thesis.<br />
 * The screen data must be a set of 3D planes.<br />
 */
public class FastEgomotion3D extends Function3D {

	private MyTransform3D transform;
	private double error;
	double alpha;
	static final double pi2 = Math.PI/2.0;

	public FastEgomotion3D() {
		super();
		this.allowed_input = ScreenOptions.tPLANARSET3D;
		this.group = Function3DGroup.Egomotion;

		transform = null;
		error = Double.MAX_VALUE;

		ParamScrData p1 = new ParamScrData("Model");
		ParamFloat p2 = new ParamFloat("Alpha");
		p2.setValue(0.15f);
		ParamInt p3 = new ParamInt("Iterations");
		p3.setValue(75);
		ParamFloat p4 = new ParamFloat("Selected");
		p4.setValue(0.65f);
		ParamBool p5 = new ParamBool("Intermediate");
		p5.setValue(false);
		ParamBool p6 = new ParamBool("Verbose");
		p6.setValue(true);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
	}

	@Override
	public void proccessData(ScreenData model) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		ScreenData scene = this.getParamValueScrData("Model");
		alpha = this.getParamValueFloat("Alpha");
		int iterations = this.getParamValueInt("Iterations");
		double perc_selected = this.getParamValueFloat("Selected");
		int count;
		double prev_error;
		MyTransform3D tr3d = new MyTransform3D();
		MyTransform3D tr3d_inc = new MyTransform3D();
		Object []sceneelements = scene.elements();
		Object []modelelements = model.elements();
		Object []scene_tr = new Plane3D[sceneelements.length];
		double []translation;
		boolean intermediate = this.getParamValueBool("Intermediate");
		boolean verbose = getParamValueBool("Verbose");

		ArrayList<Pair> pairs;

		double prog_inc = 50.0/iterations;

		//Rotation loop
		count = 0;
		do {
			progress += prog_inc;
			count++;
			prev_error = error;
			scene_tr = applyTransform(sceneelements, tr3d);

			pairs = findPairs(sceneelements, scene_tr, modelelements, count);

			if(intermediate)
				showPairs(pairs, count);

			tr3d = computeRotation(pairs);
		} while(count<iterations && Math.abs(error-prev_error)>0.001);
		if(verbose) System.out.print("Error: "+(float)error+" Iteraciones: "+count);

		progress = 50;
		MyTransform3D tr3d_2 = new MyTransform3D();
		//rotate scene patches
		Object []scene_rotated = applyTransform(sceneelements, tr3d);

		ArrayList<Vector3D> maindirscene = findMainDirections(scene_rotated);
		ArrayList<ArrayList<Plane3D>> segmented_scene_planes = segmentDirection(scene_rotated, maindirscene);

		ArrayList<Vector3D> maindirmodel = findMainDirections(modelelements);
		ArrayList<ArrayList<Plane3D>> segmented_model_planes = segmentDirection(modelelements, maindirmodel);

		//translation loop
		count = 0;
		error = Double.MAX_VALUE;
		do
		{
			count++;
			progress += prog_inc;
			prev_error = error;
			Plane3D [] scene_selected = selectPlanes(segmented_scene_planes, perc_selected);
			applyTransformThis(scene_rotated, tr3d_inc);
			Plane3D [] model_selected = selectPlanes(segmented_model_planes, perc_selected);
			//recompute pairs but with rotated model patches
			pairs = findPairsTrans(scene_selected, scene_rotated, model_selected, modelelements, count);
			
			if(intermediate)
				showPairs(pairs, count);
			//compute translation
			translation = computeTranslation(pairs);
			tr3d_inc.setTranslation(translation);
			tr3d_2.applyTransform(tr3d_inc);
		} while(count<iterations && Math.abs(error-prev_error)>0.0001);
		
		if(verbose) System.out.println(", "+count+" Error: "+(float)error);
		
		tr3d.applyTransform(tr3d_2);
		transform = tr3d;
		Object[] model_translated = applyTransform(sceneelements, tr3d);

		//generate output 3d scene
		PlaneSet3D resulttras = new PlaneSet3D(new ScreenOptions());
		resulttras.name = "egomotion3D";
		resulttras.scr_opt.width = 1;
		resulttras.scr_opt.color = new Color3f(0,1,0);
		resulttras.scr_opt.global_color = true;
		
		for(count=0;count<model_translated.length;count++)
			resulttras.insert((Plane3D)model_translated[count]);
		result_list.add(resulttras);
		
		if(verbose) System.out.println("T: "+tr3d.toString()+"\n");
	}

	public ArrayList<Pair> findPairs(Object[] scene_original, Object []scene, Object []model, int it)
	{
		Vector3D [] maindirs;
		ArrayList<Vector3D>forces = new ArrayList<Vector3D>();
		ArrayList<Pair> candidates = new ArrayList<Pair>();
		ArrayList<Pair> candidates1 = new ArrayList<Pair>();
		ArrayList<Pair> candidates2 = new ArrayList<Pair>();
		ArrayList<Pair> candidates3 = new ArrayList<Pair>();
		double m1, m2, m3, v1, v2, v3;
		double n1, n2, n3; //normalization factors for variances v1, v2 and v3
		double d1, d2, d3; //distances to main directions
		Vector3D force;
		Pair pair;

		ArrayList<Pair> ret = new ArrayList<Pair>();
		Plane3D p_model;
		Plane3D p_scene;
		int count;
		double dist;
		double weight;
		double aux;

		error = 0;
		m1 = m2 = m3 = v1 = v2 = v3 = 0;
		for(count=0;count<scene.length;count++)
		{
			p_scene = (Plane3D)scene[count];
			p_model = (Plane3D)model[findClosest(p_scene, model)];
			dist = p_scene.getAngle(p_model);
			if(dist<0.9)
			{
				candidates.add(new Pair(p_model, (Plane3D)scene_original[count], p_scene, dist));
				force = new Vector3D(p_scene.vector);
				force.normalize();
				forces.add(force);
			}
		}

		maindirs = findMainDirection(forces.toArray());
		//We put each match into its main direction set
		for(count=0;count<forces.size();count++)
		{
			force = forces.get(count);
			pair = candidates.get(count);
			d1 = force.getAngle(maindirs[0]);
    		if(d1>pi2) d1 = Math.PI - d1;
			d2 = force.getAngle(maindirs[1]);
    		if(d2>pi2) d2 = Math.PI - d2;
			d3 = force.getAngle(maindirs[2]);
    		if(d3>pi2) d3 = Math.PI - d3;

    		if(d1<d2 && d1<d3)
    		{
				candidates1.add(pair);
				m1 += pair.distance;
    		}
    		else if(d2<d1 && d2<d3)
    		{
				candidates2.add(pair);
				m2 += pair.distance;
    		}
    		else
    		{
				candidates3.add(pair);
				m3 += pair.distance;
    		}
		}

		m1 /= candidates1.size();
		m2 /= candidates2.size();
		m3 /= candidates3.size();
		for(Pair p: candidates1)
		{
			aux = p.distance - m1;
			v1 += aux * aux;
		}
		for(Pair p: candidates2)
		{
			aux = p.distance - m2;
			v2 += aux * aux;
		}
		for(Pair p: candidates3)
		{
			aux = p.distance - m3;
			v3 += aux * aux;
		}
		v1 /= candidates1.size();
		v2 /= candidates2.size();
		v3 /= candidates3.size();
		n1 = Math.sqrt(2 * Math.PI * v1);
		n2 = Math.sqrt(2 * Math.PI * v2);
		n3 = Math.sqrt(2 * Math.PI * v3);
		v1 *= 2;
		v2 *= 2;
		v3 *= 2;
		
		double weight1, weight2, weight3;
		double error1, error2, error3;
		weight1 = weight2 = weight3 = 0;
		error1 = error2 = error3 = 0;
		
		for(Pair p: candidates1)
		{
			dist = p.distance - m1;
			if(v1>0)
				weight = Math.exp(-(dist*dist)/(v1)) / n1;
			else weight = 1;
			weight1 += weight;
			error1 += p.distance * weight;
			p.distance = weight;
			ret.add(p);
		}
		if(candidates1.size()>0)
			error1 /= weight1;
		for(Pair p: candidates2)
		{
			dist = p.distance - m2;
			if(v2>0)
				weight = Math.exp(-(dist*dist)/(v2)) / n2;
			else weight = 1;
			weight2 += weight;
			error2 += p.distance * weight;
			p.distance = weight;
			ret.add(p);
		}
		if(candidates2.size()>0)
			error2 /= weight2;
		for(Pair p: candidates3)
		{
			dist = p.distance - m3;
			if(v3>0)
				weight = Math.exp(-(dist*dist)/(v3)) / n3;
			else weight = 1;
			weight3 += weight;
			error3 += p.distance * weight;
			p.distance = weight;
			ret.add(p);
		}
		if(candidates3.size()>0)
			error3 /= weight3;

		error = error1 + error2 + error3;
		error /= 3;
		return ret;
	}

	public ArrayList<Pair> findPairsTrans(Object []scene_selected, Object []scene, Object[]model_selected, Object []model, int iteration)
	{
		Vector3D [] maindirs;
		ArrayList<Vector3D>forces = new ArrayList<Vector3D>();
		ArrayList<Pair> candidates = new ArrayList<Pair>();
		Point3D projection;
		Vector3D force;
		Pair pair;

		ArrayList<Pair> ret = new ArrayList<Pair>();
		ArrayList<Pair> candidates1 = new ArrayList<Pair>();
		ArrayList<Pair> candidates2 = new ArrayList<Pair>();
		ArrayList<Pair> candidates3 = new ArrayList<Pair>();
		Plane3D p_model;
		Plane3D p_scene;
		int count;
		double dist;
		double weight;
		double aux;
		double m1, m2, m3, v1, v2, v3;
		double n1, n2, n3; //normalization factors for variances v1, v2 and v3
		double d1, d2, d3; //distances to main directions

		error = 0;
		m1 = m2 = m3 = v1 = v2 = v3 = 0;
		for(count=0;count<scene_selected.length;count++)
		{
			p_scene = (Plane3D)scene_selected[count];
			p_model = findClosestTrans(p_scene, model);
			if(Math.abs(p_model.anglePlane(p_scene))<0.25)
			{
//				dist = Math.abs(p_scene.pointDistance(p_model.origin)) + p_scene.getAngle(p_model);
				dist = Math.abs(p_scene.pointDistance(p_model.origin)) + p_scene.origin.getDistance(p_model.origin);
				error += Math.abs(p_scene.pointDistance(p_model.origin));
//				if(dist<3)
				{
					candidates.add(new Pair(p_model, p_scene, p_scene, dist));
					projection = new Point3D(p_model.pointProjection(p_scene.origin));
					forces.add(new Vector3D(projection.subPoint(p_scene.origin)));
				}
			}
		}
		for(count=0;count<model_selected.length;count++)
		{
			p_model = (Plane3D)model_selected[count];
			p_scene = findClosestTrans(p_model, scene);
			error += Math.abs(p_scene.pointDistance(p_model.origin));
			if(Math.abs(p_model.anglePlane(p_scene))<0.25)
			{
//				dist = Math.abs(p_scene.pointDistance(p_model.origin)) + p_scene.getAngle(p_model);
				dist = Math.abs(p_scene.pointDistance(p_model.origin)) + p_scene.origin.getDistance(p_model.origin);
//				if(dist<3) 
				{
					candidates.add(new Pair(p_model, p_scene, p_scene, dist));
					projection = new Point3D(p_model.pointProjection(p_scene.origin));
					forces.add(new Vector3D(projection.subPoint(p_scene.origin)));
				}
			}
		}
		error /= scene_selected.length + model_selected.length;

		maindirs = findMainDirection(forces.toArray());
		//We put each match into its main direction set
		for(count=0;count<forces.size();count++)
		{
			force = forces.get(count);
			pair = candidates.get(count);
			d1 = force.getAngle(maindirs[0]);
    		if(d1>pi2) d1 = Math.PI - d1;
			d2 = force.getAngle(maindirs[1]);
    		if(d2>pi2) d2 = Math.PI - d2;
			d3 = force.getAngle(maindirs[2]);
    		if(d3>pi2) d3 = Math.PI - d3;

    		if(d1<d2 && d1<d3)
    		{
				candidates1.add(pair);
				m1 += pair.distance;
    		}
    		else if(d2<d1 && d2<d3)
    		{
				candidates2.add(pair);
				m2 += pair.distance;
    		}
    		else
    		{
				candidates3.add(pair);
				m3 += pair.distance;
    		}
		}

		m1 /= candidates1.size();
		m2 /= candidates2.size();
		m3 /= candidates3.size();
		
		for(Pair p: candidates1)
		{
			aux = p.distance - m1;
			v1 += aux * aux;
		}
		for(Pair p: candidates2)
		{
			aux = p.distance - m2;
			v2 += aux * aux;
		}
		for(Pair p: candidates3)
		{
			aux = p.distance - m3;
			v3 += aux * aux;
		}
		
		v1 /= candidates1.size();
		v2 /= candidates2.size();
		v3 /= candidates3.size();
		v1 *= 2;
		v2 *= 2;
		v3 *= 2;
		n1 = Math.sqrt(2 * Math.PI * v1);
		n2 = Math.sqrt(2 * Math.PI * v2);
		n3 = Math.sqrt(2 * Math.PI * v3);
		
		double w1, w2, w3;
		double e1,e2,e3;
		w1 = w2 = w3 = 0;
		e1 = e2 = e3 = 0;
		
		for(Pair p: candidates1)
		{
			dist = p.distance - m1;
//			if(Math.abs(dist)<v1)
			{
				if(v1>0)
					weight = Math.exp(-(dist*dist)/(v1)) / n1;
				else weight = 1;
				e1 += p.distance * weight;
				p.distance = weight;
				w1 += weight;
				ret.add(p);
			}
		}
		e1 /= w1;
		for(Pair p: candidates2)
		{
			dist = p.distance - m2;
//			if(Math.abs(dist)<v2)
			{
				if(v2>0)
					weight = Math.exp(-(dist*dist)/(v2)) / n2;
				else weight = 1;
				e2 += p.distance * weight;
				p.distance = weight;
				w2 += weight;
				ret.add(p);
			}
		}
		e2 /= w2;
		for(Pair p: candidates3)
		{
			dist = p.distance - m3;
//			if(Math.abs(dist)<v3)
			{
				if(v3>0)
					weight = Math.exp(-(dist*dist)/(v3)) / n3;
				else weight = 1;
				e3 += p.distance * weight;
				p.distance = weight;
				w3 += weight;
				ret.add(p);
			}
		}
		e3 /= w3;
//		error = e1 + e2 + e3;
//		error /= 3;
		return ret;
	}


	/**
	 * This method visits all the Plane3D objects from an array and retrieves who that
	 * have minimum distance to p_scene planar patch
	 * @param p_model
	 * @param scene
	 * @return
	 */
	public int findClosest(Plane3D p_model, Object[]scene)
	{
		int ret = -1;
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
				ret = count;
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
		ret *= alpha;
		ret += angle;
		
		return ret;
	}

	private double calcDist2(Plane3D p_scene, Plane3D p_model)
	{
		double dist;
		double angle = p_scene.getAngle(p_model);
		dist = p_scene.origin.getDistance(p_model.origin);

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

		// Calculate the matrix U and V
		DenseDoubleMatrix2D VT, V, X;
		X=new DenseDoubleMatrix2D(3,3);

		U=(DenseDoubleMatrix2D)SVD.getU();
		V=(DenseDoubleMatrix2D)SVD.getV();
		VT=(DenseDoubleMatrix2D)V.viewDice(); //Calculate the transposed
		U.zMult(VT,X);

		Algebra algebra = new Algebra();

		if (algebra.det(X)< -0.9) { //-0.9
			U.setQuick(0,2,-U.getQuick(0,2));
			U.setQuick(1,2,-U.getQuick(1,2));
			U.setQuick(2,2,-U.getQuick(2,2));
			U.zMult(VT,X);
		}

		ret = new MyTransform3D(X);
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
			projection = p.model.pointProjection(p.scene.origin);
			v = new Vector3D(projection.subPoint(p.scene.origin));
			norms[count] = v.module;
			v.normalize();
			vectors[count] = v;
			count++;
		}
		Vector3D []main_directions = findMainDirection(vectors);
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

	/**
	 * Class Pair.
	 * @author  Miguel Cazorla
	 */
	public class Pair
	{
		public Plane3D model;
		public Plane3D scene;
		public Plane3D scene_tr;
		public double distance;

		public Pair()
		{
			model = null;
			scene = null;
			distance = 0.0;
		}

		public Pair(Plane3D m, Plane3D s, Plane3D sr, double d)
		{
			model = m;
			scene = s;
			scene_tr = sr;
			distance = d;
		}
	}

	public Object[] applyTransform(Object[] scene, MyTransform3D tr3d)
	{
		Plane3D plane;
		int cont;
		Object []ret = new Object[scene.length];

		for(cont=0;cont<scene.length;cont++)
		{
			plane = new Plane3D((Plane3D)scene[cont]);
			plane.applyTransform(tr3d);
			ret[cont] = plane;
		}
		return ret;
	}

	/**
	 * Apply tr3d transform on scene
	 * @param scene
	 * @param tr3d
	 * @return
	 */
	public void applyTransformThis(Object[] scene, MyTransform3D tr3d)
	{
		int cont;

		for(cont=0;cont<scene.length;cont++)
		{
			((Plane3D)scene[cont]).applyTransform(tr3d);
		}
	}

	public MyTransform3D getTransform()
	{
		return transform;
	}

	private Vector3D []findMainDirection(Object []vectors)
	{
		Vector3D []ret = new Vector3D[3];
    	int count;
		double [][]matrix = new double [3][3];
		DenseDoubleMatrix2D ddmatrix;
		DoubleMatrix2D U;
		SingularValueDecomposition SVD;
		double []data;

		matrix[0][0] = matrix[0][1] = matrix[0][2] =
			matrix[1][0] = matrix[1][1] = matrix[1][2] =
			matrix[2][0] = matrix[2][1] = matrix[2][2] = 0;

		for(count=0;count<vectors.length;count++)
		{
			data = ((Vector3D)vectors[count]).getCoords();
			matrix[0][0] += data[0] * data[0];
			matrix[0][1] += data[0] * data[1];
			matrix[0][2] += data[0] * data[2];
			matrix[1][1] += data[1] * data[1];
			matrix[1][2] += data[1] * data[2];
			matrix[2][2] += data[2] * data[2];
		}
		matrix[1][0] = matrix[0][1];
		matrix[2][0] = matrix[0][2];
		matrix[2][1] = matrix[1][2];

		ddmatrix = new DenseDoubleMatrix2D(matrix);
		SVD = new SingularValueDecomposition(ddmatrix);
		U = SVD.getU();
		ret[0] = new Vector3D(U.getQuick(0, 0), U.getQuick(1, 0), U.getQuick(2, 0));
		ret[1] = new Vector3D(U.getQuick(0, 1), U.getQuick(1, 1), U.getQuick(2, 1));
		ret[2] = new Vector3D(U.getQuick(0, 2), U.getQuick(1, 2), U.getQuick(2, 2));

		return ret;
	}


    public ArrayList<Vector3D> findMainDirections(Object []scene)
    {
    	int count;
    	Plane3D plane;
    	ArrayList<Vector3D>ret = new ArrayList<Vector3D>();
    	Vector3D v1, v2, v3;
		double [][]matrix = new double [3][3];
		DenseDoubleMatrix2D ddmatrix;
		DoubleMatrix2D U;
		SingularValueDecomposition SVD;
		double []data;

		matrix[0][0] = matrix[0][1] = matrix[0][2] =
			matrix[1][0] = matrix[1][1] = matrix[1][2] =
			matrix[2][0] = matrix[2][1] = matrix[2][2] = 0;

		for(count=0;count<scene.length;count++)
		{
			plane = (Plane3D)scene[count];
			data = plane.vector.getCoords();
			matrix[0][0] += data[0] * data[0];
			matrix[0][1] += data[0] * data[1];
			matrix[0][2] += data[0] * data[2];
			matrix[1][1] += data[1] * data[1];
			matrix[1][2] += data[1] * data[2];
			matrix[2][2] += data[2] * data[2];

		}
		matrix[1][0] = matrix[0][1];
		matrix[2][0] = matrix[0][2];
		matrix[2][1] = matrix[1][2];

		ddmatrix = new DenseDoubleMatrix2D(matrix);
		SVD = new SingularValueDecomposition(ddmatrix);
		U = SVD.getU();
		v1 = new Vector3D(U.getQuick(0, 0), U.getQuick(1, 0), U.getQuick(2, 0));
		v2 = new Vector3D(U.getQuick(0, 1), U.getQuick(1, 1), U.getQuick(2, 1));
		v3 = new Vector3D(U.getQuick(0, 2), U.getQuick(1, 2), U.getQuick(2, 2));
		ret.add(v1); ret.add(v2); ret.add(v3);
		return ret;
    }

    public ArrayList<ArrayList<Plane3D>> segmentDirection(Object []elements, ArrayList<Vector3D> dirs)
    {
    	ArrayList<ArrayList<Plane3D>> ret = new ArrayList<ArrayList<Plane3D>>();
    	ArrayList<Plane3D> set1 = new ArrayList<Plane3D>();
    	ArrayList<Plane3D> set2 = new ArrayList<Plane3D>();
    	ArrayList<Plane3D> set3 = new ArrayList<Plane3D>();
    	int count;
    	double d1, d2, d3;
    	Plane3D plane;

    	for(count=0;count<elements.length;count++)
    	{
    		plane = (Plane3D)elements[count];
    		d1 = plane.vector.getAngle(dirs.get(0));
    		if(d1>pi2) d1 = Math.PI - d1;
    		d2 = plane.vector.getAngle(dirs.get(1));
    		if(d2>pi2) d2 = Math.PI - d2;
    		d3 = plane.vector.getAngle(dirs.get(2));
    		if(d3>pi2) d3 = Math.PI - d3;

    		if(d1<d2 && d1<d3) set1.add(plane);
    		else if(d2<d1 && d2<d3) set2.add(plane);
    		else set3.add(plane);
    	}
    	ret.add(set1);
    	ret.add(set2);
    	ret.add(set3);
    	return ret;
    }

    public Plane3D [] selectPlanes(ArrayList<ArrayList<Plane3D>>sets, double perc)
    {
    	int max;
    	Plane3D []ret;
    	ArrayList<Plane3D> set1 = sets.get(0);
    	ArrayList<Plane3D> set2 = sets.get(1);
    	ArrayList<Plane3D> set3 = sets.get(2);
    	int size1, size2, size3;
    	int elements;
    	Random generator = new Random(System.currentTimeMillis());
    	int count, pos;

    	size1 = set1.size();
    	size2 = set2.size();
    	size3 = set3.size();
    	max = (int)((size1 + size2 + size3) * perc / 3);
    	elements = max;
    	int total = 0;
    	if (elements<size1) total += elements;
    	else total+=size1;
    	if (elements<size2) total += elements;
    	else total+=size2;
    	if (elements<size3) total += elements;
    	else total+=size3;
    	ret = new Plane3D[total];
    	pos = 0;
    	if(elements<size1)
    	{
    		for(count=0;count<elements;count++,pos++)
    		{
    			ret[pos] = set1.get(generator.nextInt(size1));
    		}
    	}
    	else //the same size
    	{
    		for(count=0;count<size1;count++,pos++)
    		{
    			ret[pos] = set1.get(count);
    		}
    	}
    	if(elements<size2)
    	{
    		for(count=0;count<elements;count++,pos++)
    		{
    			ret[pos] = set2.get(generator.nextInt(size2));
    		}
    	}
    	else //the same size
    	{
    		for(count=0;count<size2;count++,pos++)
    		{
    			ret[pos] = set2.get(count);
    		}
    	}
    	if(elements<size3)
    	{
    		for(count=0;count<elements;count++,pos++)
    		{
    			ret[pos] = set3.get(generator.nextInt(size3));
    		}
    	}
    	else //the same size
    	{
    		for(count=0;count<size3;count++,pos++)
    		{
    			ret[pos] = set3.get(count);
    		}
    	}
    	return ret;
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
    		pairsSegment.insert(new Segment3D(new Point3D(p.model.origin), new Point3D(p.scene_tr.origin)));
    		mov.insert(new Segment3D(new Point3D(p.model.origin), p.scene_tr.pointProjection(p.model.origin)));
    		pset.insert(new Plane3D(p.scene_tr));
    	}
    	result_list.add(pset);
    	result_list.add(pairsSegment);
    	result_list.add(mov);
    }

}
