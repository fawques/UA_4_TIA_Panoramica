package javavis.jip3d.functions;

import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Neuron3D;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;

/**
 * It extracts the plane patches from a GNG structure.<br />
 * The screen data must be a set of 3D neurons.<br />
 */
public class PlanePatchGNG extends Function3D {

	double relacion = 2/Math.sqrt(3);
	double minRad = 0.01;

	public PlanePatchGNG()
	{
		super();
		this.allowed_input = ScreenOptions.tNEURONSET3D;
		this.group = Function3DGroup.Model3D;
		
		ParamList p5 = new ParamList("Process Type");
		String []paux = new String[2];
		paux[0] = "Lineal";
		paux[1] = "MultiThread";
		p5.setDefault(paux);
		ParamInt p1 = new ParamInt("Neighbor gen");
		p1.setValue(2);
		ParamFloat p2 = new ParamFloat("PlaneThick");
		p2.setValue(0.05f);
		ParamBool p3 = new ParamBool("Check integrity");
		p3.setValue(false);
		ParamScrData p4 = new ParamScrData("Source PointSet");
		
		this.addParam(p5);
		this.addParam(p1);
		this.addParam(p2);
		this.addParam(p3);
		this.addParam(p4);
	}
	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		PlaneSet3D new_plane_set = new PlaneSet3D(new ScreenOptions());
		new_plane_set.name = "patches" + scrData.name.substring(3);
		int gen = this.getParamValueInt("Neighbor gen");
		float thickness = (float)this.getParamValueFloat("PlaneThick");
		Object []elements;
		Neuron3D element;
		NormalFinder[] finders;
		Plane3D plane;
		
		PointSet3D pointSet = null;
		boolean checkIntegrity = this.getParamValueBool("Check integrity");
		if(checkIntegrity)
			pointSet = (PointSet3D)this.getParamValueScrData("Source PointSet");
		elements = scrData.elements();
		finders = new NormalFinder[elements.length];
		int count;
		
		//compute normals
		for(count=0;count<elements.length;count++)
		{
			element = (Neuron3D)elements[count];
			if(checkIntegrity)
			{
				if(check(element, pointSet, 0.05))
					finders[count] = new NormalFinder(element, thickness, gen, count);
			}
			else
				finders[count] = new NormalFinder(element, thickness, gen, count);
		}
		
		//gather results
		try {
			for(count=0;count<elements.length;count++)
			{
				if(finders[count]!=null)
				{
					finders[count].runner.join();
					if(finders[count].result!=null)
					{
						plane = new Plane3D(new Normal3D((Point3D)elements[count], new Vector3D(finders[count].result[0],
								finders[count].result[1], finders[count].result[2])));
						plane.radius = finders[count].result[3];
						new_plane_set.insert(plane);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result_list.add(new_plane_set);
	}
	
	private boolean check(Neuron3D neuron, PointSet3D points, double rad)
	{
		boolean ret = false;
		Object []neighbors = points.range(neuron, rad);
		if(neighbors.length>0) ret = true;
		return ret;
	}

	/**
	 * Class NormalFinder
	 * @author Miguel Cazorla
	 */
	public class NormalFinder implements Runnable
	{
		Thread runner;
		Neuron3D origin;
		int gen;
		double[] result;
		float thick;
		
		public NormalFinder(Neuron3D o, float t,int d, int n)
		{
			runner = new Thread(this, "thread"+n);
			origin = o;
			gen = d;
			thick = t;
			result = null;
			runner.start();
		}
		//@Override
		public void run()
		{
			int count;
			int generation = 0;
			Neuron3D []vector;
			Neuron3D neuron;
			ArrayList<Neuron3D> neighbors = new ArrayList<Neuron3D>();
			ArrayList<Neuron3D> nextgen = new ArrayList<Neuron3D>();
			ArrayList<Neuron3D> visited = new ArrayList<Neuron3D>();
			ArrayList<Point3D> vectores = new ArrayList<Point3D>();

			double [][]matrix = new double [3][3];
			double []singular_values;
			DenseDoubleMatrix2D ddmatrix;
			DoubleMatrix2D U;
			SingularValueDecomposition SVD;
			int vsize;
			double []data;
			double minDist, dist;
			double sigmaN, sigmaT;
			double thickness;
			
			//neighbor searching
			vector = origin.getNeuronNeighbors();
			visited.add(origin);
			
			minDist = 1000;
			for(Neuron3D n: vector)	neighbors.add(n);
			do {
				while(!neighbors.isEmpty()) 
				{
					neuron = neighbors.remove(0);
					dist = origin.getDistance(neuron);
					if(dist<minDist) minDist = dist;
					vectores.add(neuron.subPoint(origin));
					visited.add(neuron);
					for(Neuron3D n: neuron.getNeuronNeighbors())
					{
						if(neighbors.indexOf(n)==-1 && visited.indexOf(n)==-1) nextgen.add(n);
					}
				}
				neighbors.addAll(nextgen);
				nextgen.clear();
				generation++;
			} while (generation < gen);
			
			//normal computing
			vsize = vectores.size();
			if(vsize>9)
			{
				matrix[0][0] = matrix[0][1] = matrix[0][2] =
					matrix[1][0] = matrix[1][1] = matrix[1][2] =
					matrix[2][0] = matrix[2][1] = matrix[2][2] = 0;
				for(count=0;count<vsize;count++)
				{
					data = vectores.get(count).getCoords();
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
				U = SVD.getU();
				singular_values = SVD.getSingularValues();

				sigmaN = singular_values[2];
				sigmaT = Math.sqrt(singular_values[0]*singular_values[1]);

				thickness = Math.atan(relacion * sigmaN/sigmaT);
				if(thickness<thick)
				{
					result = new double[4];
					result[0] = U.getQuick(0, 2);
					result[1] = U.getQuick(1, 2);
					result[2] = U.getQuick(2, 2);
					result[3] = minDist<minRad?minRad:minDist;
				}
			}
		}
	}

}
