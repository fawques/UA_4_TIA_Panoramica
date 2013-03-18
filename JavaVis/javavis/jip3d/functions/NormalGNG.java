package javavis.jip3d.functions;

import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Neuron3D;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.NormalSet3D;

/**
 * It calculates the normal vectors associated to a neuronal network. You can choose the 
 * number of neighbors to consider for the calculation of the normal.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class NormalGNG extends Function3D {

	public NormalGNG()
	{
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Normals;
		
		ParamInt p1 = new ParamInt("Neighbor gen");
		p1.setValue(2);

		addParam(p1);
	}
	
	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		int gen = this.getParamValueInt("Neighbor gen");
		Object []elements;
		Neuron3D element;
		NormalFinder[] finders;
		NormalSet3D normals = new NormalSet3D(new ScreenOptions());
		normals.name = "NormalSet"+scrData.name.substring(scrData.name.length()-3);
		
		elements = scrData.elements();
		finders = new NormalFinder[elements.length];
		int count;
		
		//compute normals
		for(count=0;count<elements.length;count++)
		{
			element = (Neuron3D)elements[count];
			finders[count] = new NormalFinder(element, gen, count);
		}
		
		//gather results
		try {
			for(count=0;count<elements.length;count++)
			{
				finders[count].runner.join();
				if(finders[count].result!=null) normals.insert(finders[count].result);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result_list.add(normals);
	}
	
	/**
	 * Class NormalFinder.
	 * @author  Miguel Cazorla
	 */
	public class NormalFinder implements Runnable
	{
		Thread runner;
		Neuron3D origin;
		int gen;
		Normal3D result;
		
		public NormalFinder(Neuron3D o, int d, int n)
		{
			runner = new Thread(this, "thread"+n);
			origin = o;
			gen = d;
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
			DenseDoubleMatrix2D ddmatrix;
			DoubleMatrix2D U;
			SingularValueDecomposition SVD;
			int vsize;
			double []data;
			
			//neighbor searching
			vector = origin.getNeuronNeighbors();
			visited.add(origin);
			for(Neuron3D n: vector)	neighbors.add(n);
			do {
				while(!neighbors.isEmpty()) 
				{
					neuron = neighbors.remove(0);
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

				//singular_values = SVD.getSingularValues();
				U = SVD.getU();
				result = new Normal3D(origin, new Vector3D(U.getQuick(0, 2),U.getQuick(1, 2),U.getQuick(2, 2)));
			}
		}
	}

}
