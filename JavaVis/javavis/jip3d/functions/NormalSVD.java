package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.NormalSet3D;

import javax.vecmath.Color3f;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

/**
 * It calculates the normal vectors by SVD associated to a neuronal network. You can choose the 
 * number of neighbors to consider for the calculation of the normal. The minimum number of
 * neighbors must be 9 because it is a required of SVD estimates.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class NormalSVD extends Function3D {

	private double minimum = 9;
	private double pi2 = Math.PI/2.0;
	double relation = 2/Math.sqrt(3);

	public NormalSVD()
	{
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Normals;

		ParamFloat p1 = new ParamFloat("Window size");
		p1.setValue(0.025f);
		ParamFloat p2 = new ParamFloat("Minimum size");
		p2.setValue(0.0f);
		ParamFloat p3 = new ParamFloat("PlaneThick");
		p3.setValue(0.005f);

		this.addParam(p1);
		this.addParam(p2);
		this.addParam(p3);
	}

	//@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		double win_size = this.getParamValueFloat("Window size");
		double min_size = this.getParamValueFloat("Minimum size");
		double thickness = this.getParamValueFloat("PlaneThick");

		Object []elements;
		Object []vecinos;
		Point3D element;
		int count, len;
		double rad;
		double []normal;
		Normal3D normalVector;
		Vector3D vec_aux;
		double angle;
		Point3D paux;

		//Returning screenData
		NormalSet3D normals = new NormalSet3D(new ScreenOptions());
		normals.name = "NormalSet"+scr_data.name.substring(scr_data.name.length()-3);
		NormalSet3D saliencies = new NormalSet3D(new ScreenOptions());
		saliencies.scr_opt.color = new Color3f(0, 0, 1);
		saliencies.name = "SalienceSet"+scr_data.name.substring(scr_data.name.length()-3);

		elements = scr_data.elements();
		len = elements.length;

		double prog_inc = 100.0/len;

		for(count=0;count<len;count++)
		{
			progress += prog_inc;
			element = (Point3D)elements[count];
			rad = element.getOriginDistance() * win_size;
			if(rad<min_size) rad = min_size;

			try
			{
				vecinos = scr_data.range(element, rad);
				if(vecinos.length>minimum)
				{
					paux = calcCentroid(vecinos);
					normal = applySVD(paux, vecinos, thickness);
					if(normal!=null && normal[3]!=2)
					{
						vec_aux = new Vector3D(normal[0], normal[1], normal[2]);
						normalVector = new Normal3D(element, vec_aux, thickness, rad);
						vec_aux = new Vector3D(element);
						if(normal[3]==1)
						{
							//The normal has to be in front of the camera
							angle = vec_aux.getAngle(normalVector.vector);

							if(angle<pi2)
							{
								normal[0] = -normal[0];
								normal[1] = -normal[1];
								normal[2] = -normal[2];
								vec_aux = new Vector3D(normal[0], normal[1], normal[2]);
								normalVector = new Normal3D(element, vec_aux, thickness, rad);
							}
							normals.insert(normalVector);
						}
						else if(normal[3]==0)
						{
							saliencies.insert(normalVector);
						}
					}
				}
			}catch(Exception e)
			{
				System.err.println(e.getMessage());
				result_list = null;
				return;
			}
		}
		result_list.add(normals);
		result_list.add(saliencies);
	}

	static public Object[] subAverage(Object []set, Point3D average)
	{
		int size = set.length;
		Object []ret = new Object[size];
		int count;
		Point3D element;

		for(count=0;count<size;count++)
		{
			element = ((Point3D)set[count]).subPoint(average);
			ret[count] = element;
		}

		return ret;
	}

	static public Point3D calcCentroid(Object []set)
	{
		Point3D ret;
		double x,y,z;
		int count;
		x = y = z = 0;
		for(count=0;count<set.length;count++)
		{
			x += ((Point3D)set[count]).getX();
			y += ((Point3D)set[count]).getY();
			z += ((Point3D)set[count]).getZ();
		}
		x /= set.length;
		y /= set.length;
		z /= set.length;
		ret = new Point3D(x, y, z);
		return ret;
	}

	/**
	 *
	 * @param source
	 * @param pset
	 * @param threshold
	 * @return vector with the normal and the vector sigmaT
	 */
	public double[] applySVD(Point3D source, Object []pset, double threshold)
	{
		double []ret = new double[5];
		double [][]matrix = new double [3][3];
		double []singular_values;
		DenseDoubleMatrix2D ddmatrix;
		DoubleMatrix2D U;
		SingularValueDecomposition SVD;
		int i, minor;
		double sigmaN, sigmaT;
		double thickness;
		int planeType;
		double []data;
		Object []neightbords = subAverage(pset, source);

		matrix[0][0] = matrix[0][1] = matrix[0][2] =
			matrix[1][0] = matrix[1][1] = matrix[1][2] =
			matrix[2][0] = matrix[2][1] = matrix[2][2] = 0;
		for(i=0;i<neightbords.length;i++)
		{
			data = ((Point3D)neightbords[i]).getCoords();
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

		singular_values = SVD.getSingularValues();
		minor = 2;
		sigmaN = singular_values[2];
		sigmaT = Math.sqrt(singular_values[0]*singular_values[1]);

		thickness = Math.atan(relation * sigmaN/sigmaT);

		//are neighbor points belonging to a line?
		double lineness = (singular_values[0] - singular_values[1]) / (singular_values[0] + singular_values[1] + singular_values[2]);
		if(thickness<threshold && lineness<0.5)
		{
			planeType = 1; //is on a planar surface
		}
		else
		{
			if(lineness<0.03)
				planeType = 3;
			else if (lineness<0.045) //For outdoor
				planeType = 0;
			else planeType = 2;
		}

		if(planeType!=2 )
		{
			ret[3] = planeType;
			U = SVD.getU();
			if(planeType==1)
			{
				ret[0] = U.get(0, minor);
				ret[1] = U.get(1, minor);
				ret[2] = U.get(2, minor);
				ret[4] = thickness;
			}
			else
			{
				ret[0] = U.get(0, 0);
				ret[1] = U.get(1, 0);
				ret[2] = U.get(2, 0);
				ret[4] = lineness;
			}
		}
		else
			ret = null;
		return ret;
	}

}
