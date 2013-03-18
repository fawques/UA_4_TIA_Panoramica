package javavis.jip3d.geom;

import javax.vecmath.Color3b;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/**
 * Class Point SR4K or Point SR4000
 * @author Diego Viejo
 */
public class PointSR4K extends Point3D {
	private static final long serialVersionUID = -4341775468110824954L;

	/**
	 * @uml.property  name="confidence"
	 */
	public int confidence;
	
	public PointSR4K() {
		super();
		confidence = 0;
	}

	public PointSR4K(PointSR4K source) {
		super(source);
		this.confidence = source.confidence;
	}

	public PointSR4K(String[] dataRaw, int count) {
		double x,y,z;
		byte col;
		int i,j, aux;
		visited = 0;

		x = Double.parseDouble(dataRaw[count++]);
		y = Double.parseDouble(dataRaw[count++]);
		z = Double.parseDouble(dataRaw[count++]);

		aux = Integer.parseInt(dataRaw[count++]);
		col = (byte) (aux&0xFF);

		confidence = Integer.parseInt(dataRaw[count++]);

		i = Integer.parseInt(dataRaw[count++]);
		j = Integer.parseInt(dataRaw[count]);

		data = new DenseDoubleMatrix1D(4);
		data.set(0, x);
		data.set(1, y);
		data.set(2, z);
		data.set(3, 1);
		color = new Color3b(col, col, col);
		posx = j;
		posy = i;
	}

	public PointSR4K(double x, double y, double z) {
		super(x, y, z);
	}

	public PointSR4K(double x, double y, double z, Color3b col, int confidence, int i, int j) {
		super(x, y, z, col, i, j);
		this.confidence = confidence;
	}

	public PointSR4K(double[] coords) {
		super(coords);
	}

}
