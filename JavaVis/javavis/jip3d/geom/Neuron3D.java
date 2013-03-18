package javavis.jip3d.geom;

import java.util.Arrays;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class Neuron3D
 */
public class Neuron3D extends Point3D {
	private static final long serialVersionUID = -7022111033233049197L;
	
	/**
	 * @uml.property  name="neuronNeighbors"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	Neuron3D neuronNeighbors[];
	/**
	 * @uml.property  name="neighbors" multiplicity="(0 -1)" dimension="1"
	 */
	int neighbors[];

	/**
	 * @uml.property  name="number"
	 */
	int number;

	/**
	 * Default constructor. Creates a void neuron 3D.
	 */
	public Neuron3D()
	{
		super();
		neighbors=null;
		number=0;
	}
	
	public Neuron3D(Neuron3D source)
	{
		super(source);
		neighbors=Arrays.copyOf(source.neighbors,source.neighbors.length);
		number=source.number;
	}
	
	public Neuron3D(String[] dataRaw, int n, int cont) {
		int neighs;
		
		number=n;
		
		data = new DenseDoubleMatrix1D(4);
		data.set(0, Double.parseDouble(dataRaw[cont++])/100);
		data.set(1, Double.parseDouble(dataRaw[cont++])/100);
		data.set(2, Double.parseDouble(dataRaw[cont++])/100);
		data.set(3, 1);
		
		neighs = Integer.parseInt(dataRaw[cont++]);
		
		neighbors = new int[neighs];
		for (int i=0; i<neighs; i++) {
			neighbors[i] = Integer.parseInt(dataRaw[cont++]);
		}
	}

	/**
	 * @return
	 * @uml.property  name="neuronNeighbors"
	 */
	public Neuron3D[] getNeuronNeighbors() {
		return neuronNeighbors;
	}

	/**
	 * @param neuronNeighbors
	 * @uml.property  name="neuronNeighbors"
	 */
	public void setNeuronNeighbors(Neuron3D[] neuronNeighbors) {
		this.neuronNeighbors = neuronNeighbors;
	}

	/**
	 * @return
	 * @uml.property  name="neighbors"
	 */
	public int[] getNeighbors() {
		return neighbors;
	}

	/**
	 * @param neighbors
	 * @uml.property  name="neighbors"
	 */
	public void setNeighbors(int[] neighbors) {
		this.neighbors = neighbors;
	}

}
