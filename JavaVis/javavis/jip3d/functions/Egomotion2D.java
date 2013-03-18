package javavis.jip3d.functions;

import java.util.ArrayList;

import javax.vecmath.Color3f;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyKDTree;
import javavis.jip3d.geom.MyTransform2D;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;

/**
 * It calculates the best transformation from two sets of planes. It is used for computing 
 * Egomotion between two sets of 3D plane patches. It is assumed that the transformation which 
 * aligns those sets has 3 degrees of freedom.<br />
 * The screen data must be a set of 3D planes.<br />
 */
public class Egomotion2D extends Function3D {
	
	private double alpha;
	private double beta;
	private MyTransform2D transform;
	private double[]error;

	public Egomotion2D() {
		super();
		this.allowed_input = ScreenOptions.tPLANARSET3D;
		this.group = Function3DGroup.Egomotion;

		transform = null;
		error = null;
		alpha = 0.025;
		beta = 0.025;

		ParamScrData p1 = new ParamScrData("Next Object");
		ParamFloat p2 = new ParamFloat("Alpha");
		p2.setValue(0.1025f);
		ParamFloat p3 = new ParamFloat("Beta");
		p3.setValue(0.015f);
		ParamFloat p4 = new ParamFloat("Initial Variance");
		p4.setValue(0.3f);
		ParamFloat p5 = new ParamFloat("Final Variance");
		p5.setValue(0.0035f);
		ParamInt p6 = new ParamInt("Iterations");
		p6.setValue(75);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		ScreenData model = this.getParamValueScrData("Next Object");
		alpha = this.getParamValueFloat("Alpha");
		beta = this.getParamValueFloat("Beta");
		double init_var = this.getParamValueFloat("Initial Variance");
		double final_var = this.getParamValueFloat("Final Variance");
		int iterations = this.getParamValueInt("Iterations");
		int count;
		MyTransform2D tr2d = new MyTransform2D();
		double var = init_var;
		double incvar = (var-final_var)/15;
		Object []elements = model.elements();
		Plane3D [] elements_model = new Plane3D[elements.length];
		Plane3D [] elements_modeltr;
		Plane3D [] elements_scene;

		double prog_inc = 50.0/iterations;
		double angle, prev_angle;
		double []traslation;

		for(count=0;count<elements.length;count++)
			elements_model[count] = (Plane3D)elements[count];

		elements = scr_data.elements();
		elements_scene = new Plane3D[elements.length];
		
		for(count=0;count<elements.length;count++)
			elements_scene[count] = (Plane3D)elements[count];

		if(model.getType() != ScreenOptions.tPLANARSET3D)
		{
			this.dialog.error("Error: input data type", "Egomotion2D");
			result_list = null;
			return;
		}
		int []closests = new int[model.scr_opt.num_points];

		count = 0;
		angle = 0;
		do {
			prev_angle = angle;
			elements_modeltr = this.applyTransformation(elements_model, tr2d);

			tr2d = findClosestRotation(elements_scene, elements_model, elements_modeltr, closests, var);
			angle = tr2d.getAngle();
			count++;
			if(var>final_var+incvar)
				var -= incvar;
			else var = final_var;

			progress += prog_inc;
		} while(count<iterations && (Math.abs(angle - prev_angle) > 0.0001));
		System.out.print("Number of iterations: "+count);

		elements_modeltr = this.applyTransformation(elements_model, tr2d);

		count = 0;
		var = init_var;
		double []prev_trasl = new double[2];
		prev_trasl[0] = prev_trasl[1] = 0;
		do {
			elements_modeltr = this.applyTransformation(elements_model, tr2d);

			//takes each intermediate step
			progress = 50;

			traslation = findClosestTranslation(elements_scene, elements_modeltr, elements_modeltr, closests, var);
			prev_trasl[0] += traslation[0];
			prev_trasl[1] += traslation[1];
			tr2d.setTraslation(prev_trasl[0], prev_trasl[1]);
			count++;
			if(var>final_var+incvar)
				var -= incvar;
			else var = final_var;

			progress += prog_inc;
		} while(count<iterations && (Math.abs(traslation[0]) > 0.0005 || Math.abs(traslation[1]) > 0.0005));

		elements_modeltr = this.applyTransformation(elements_model, tr2d);
		transform = tr2d;
		error = calcError(scr_data.getData(), elements_modeltr, closests);
		System.out.println(tr2d.toString());

		PlaneSet3D resulttras = new PlaneSet3D(new ScreenOptions());
		resulttras.name = "egomotion2D";
		resulttras.scr_opt.width = 1;
		resulttras.scr_opt.color = new Color3f(0,1,0);
		resulttras.scr_opt.global_color = true;
		
		for(Plane3D plane: elements_modeltr)
			resulttras.insert(plane);
		result_list.add(resulttras);
	}

	private MyTransform2D findClosestRotation(Plane3D [] elements_scene, Plane3D[] model, Plane3D[] modeltr, int []closests, double variance)
	{
		MyTransform2D ret = new MyTransform2D();
		double angle_res = 0;
		double total = 0;
		double []distances;
		double []angles;

		Plane3D element_scene;
		Plane3D element_model;
		Plane3D element_modeltr;
		double angle;
		double dist;
		double total_dist;
		double less_total;
		double less_angle = 100;
		double less_dist = 100;
		int len, len2;
		int count, count2;
		double dist_aux;

		len = elements_scene.length;
		len2 = model.length;
		distances = new double[len2];
		angles = new double[len2];

		for(count=0;count<len2;count++)
		{
			element_modeltr = modeltr[count];
			less_total = Double.MAX_VALUE;
			for(count2=0;count2<len;count2++)
			{
				element_scene = elements_scene[count2];
				angle = element_modeltr.vector.getX()*element_scene.vector.getX() + element_modeltr.vector.getZ()*element_scene.vector.getZ();
				angle /= Math.sqrt(element_modeltr.vector.getX()*element_modeltr.vector.getX()+ element_modeltr.vector.getZ()*element_modeltr.vector.getZ()) * Math.sqrt(element_scene.vector.getX()*element_scene.vector.getX()+element_scene.vector.getZ()*element_scene.vector.getZ());
				angle = Math.acos(angle);

				dist_aux = Math.abs(element_scene.pointDistance(element_modeltr.origin));
				dist = dist_aux + element_scene.origin.getDistance(element_modeltr.origin);

				total_dist = angle + beta * Math.abs(dist);
				if(total_dist<less_total)
				{
					less_total = total_dist;
					less_angle = angle;
					less_dist = dist;
					closests[count] = count2;
				}
			}
			distances[count] = Math.exp(-(less_angle*less_angle)/(variance))+ beta * Math.exp(-(less_dist*less_dist)/variance);
			total += distances[count];
			element_model = model[count];
			element_scene = elements_scene[closests[count]];
			angle = element_model.vector.getX()*element_scene.vector.getX() + element_model.vector.getZ()*element_scene.vector.getZ();
			angle /= Math.sqrt(element_model.vector.getX()*element_model.vector.getX()+ element_model.vector.getZ()*element_model.vector.getZ()) * Math.sqrt(element_scene.vector.getX()*element_scene.vector.getX()+element_scene.vector.getZ()*element_scene.vector.getZ());
			//degenerate case: by precision errors, angle could be > 1
			if(angle>1) angle = 0;
			else
				angle = Math.acos(angle);
			if((element_model.vector.getZ()*element_scene.vector.getX() - element_model.vector.getX()*element_scene.vector.getZ())<0)
				angle = -angle;
			angles[count] = angle;
		}

		if(total!=0)
			for(count=0;count<len2;count++)
			{
				distances[count] /= total;
				angle_res += angles[count] * distances[count];
			}

		ret.setRotation(angle_res);
		return ret;
	}

	private Plane3D[] applyTransformation(Plane3D[] source, MyTransform2D tr2d)
	{
		Plane3D []ret;
		int count, len;

		len = source.length;
		ret = new Plane3D[len];

		for(count=0;count<len;count++)
		{
			ret[count] = new Plane3D(source[count]);
			ret[count].applyTransform(tr2d);
		}
		return ret;
	}

	public double[]calcError(MyKDTree scene, Plane3D[] modeltr, int[]closests)
	{
		double []ret = new double[2]; //traslation and angle
		Object [] elements_scene;
		Plane3D element_scene;
		int count, len = closests.length;
		double angle, dist;

		ret[0] = ret[1] = 0;
		elements_scene = scene.elements();

		for(count=0;count<len;count++)
		{
			element_scene = (Plane3D)elements_scene[closests[count]];
			angle = modeltr[count].anglePlane(element_scene);
			dist = modeltr[count].pointDistance(element_scene.origin);

			ret[0] += dist;
			ret[1] += angle;
		}
		ret[0] /= len;
		ret[1] /= len;
		return ret;
	}

	private double [] findClosestTranslation(Plane3D[] elements_scene, Plane3D[] model, Plane3D[] modeltr, int []closests, double variance)
	{
		double []ret = new double[2];
		double total;
		double []distances;
		double [][]traslations;

		Plane3D element_scene;
		Plane3D element_model;
		Plane3D element_modeltr;
		double angle;
		double dist;
		double total_dist;
		double less_total;
		int len, len2;
		int count, count2;

		len = elements_scene.length;
		len2 = model.length;
		distances = new double[len2];
		traslations = new double[2][len2];
		Point3D projection;

		total = 0;
		for(count=0;count<len2;count++)
		{
			element_modeltr = modeltr[count];
			less_total = Double.MAX_VALUE;
			for(count2=0;count2<len;count2++)
			{
				element_scene = elements_scene[count2];
				angle = element_modeltr.vector.getX()*element_scene.vector.getX() + element_modeltr.vector.getZ()*element_scene.vector.getZ();
				angle /= Math.sqrt(element_modeltr.vector.getX()*element_modeltr.vector.getX()+ element_modeltr.vector.getZ()*element_modeltr.vector.getZ()) * Math.sqrt(element_scene.vector.getX()*element_scene.vector.getX()+element_scene.vector.getZ()*element_scene.vector.getZ());
				angle = Math.acos(angle);

				dist = Math.abs(element_scene.pointDistance(element_modeltr.origin)) +
				Math.abs(element_modeltr.origin.getDistance(element_scene.origin));

				total_dist = angle + alpha * Math.abs(dist);
				if(total_dist<less_total)
				{
					less_total = total_dist;
					closests[count] = count2;
				}
			}
			distances[count] = Math.exp(-(less_total*less_total)/(variance));
			total += distances[count];
			element_model = model[count];
			element_scene = elements_scene[closests[count]];
			projection = element_scene.pointProjection(element_model.origin);
			traslations[0][count] = projection.getX()-element_model.origin.getX();
			traslations[1][count] = projection.getZ()-element_model.origin.getZ();
		}

		ret[0] = 0;
		ret[1] = 0;
		if(total!=0)
			for(count=0;count<len2;count++)
			{
				distances[count] /= total;
				ret[0] += traslations[0][count] * distances[count];
				ret[1] += traslations[1][count] * distances[count];
			}

		return ret;
	}

	/**
	 * Getter function of the parameter <em>transform</em>.
	 * @return The Transform
	 */
	public MyTransform2D getTransform()
	{
		return transform;
	}

	/**
	 * Getter function of the parameter <em>error</em>.
	 * @return The error
	 */
	public double[] getError()
	{
		return error;
	}

}
