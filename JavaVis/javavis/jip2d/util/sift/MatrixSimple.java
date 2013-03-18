package javavis.jip2d.util.sift;


public class MatrixSimple {
	/**
	 * @uml.property  name="values" multiplicity="(0 -1)" dimension="2"
	 */
	double[][] values;
	/**
	 * @uml.property  name="xDim"
	 */
	int xDim;
	/**
	 * @uml.property  name="yDim"
	 */
	int yDim;
	
	public MatrixSimple(double[][] values) {
		this.values = values;
		xDim = values.length;
		yDim = values[0].length;
	}
	
	public void resolverSistLineal(double[] b) {
		// Gaussian Elimination Algorithm, as described by
		// "Numerical Methods - A Software Approach", R.L. Johnston

		// Forward elimination with partial pivoting
		for (int y = 0 ; y < (yDim - 1) ; ++y) {

			// Searching for the largest pivot (to get "multipliers < 1.0 to
			// minimize round-off errors")
			int yMaxIndex = y;
			double yMaxValue = Math.abs (values[y][y]);

			for (int py = y ; py < yDim ; ++py) {
				if (Math.abs (values[py][y]) > yMaxValue) {
					yMaxValue = Math.abs (values[py][y]);
					yMaxIndex = py;
				}
			}

			// if a larger row has been found, swap with the current one
			if (y!=yMaxIndex) {
				swapRow(y, yMaxIndex);
				double temp = b[y];
				b[y] = b[yMaxIndex];
				b[yMaxIndex] = temp;
			}

			// Now do the elimination left of the diagonal
			for (int py = y + 1 ; py < yDim ; ++py) {
				// always <= 1.0
				double elimMul = values[py][y] / values[y][y];

				for (int x = 0 ; x < xDim ; ++x)
					values[py][x] -= elimMul * values[y][x];

				b[py] -= elimMul * b[y];
			}
		}

		// Back substitution
		for (int y = yDim - 1 ; y >= 0 ; --y) {
			double solY = b[y];

			for (int x = xDim - 1 ; x > y ; --x)
				solY -= values[y][x] * b[x];

			b[y] = solY / values[y][y];
		}		
	}
	
	/**
	 * Exchanges two rows from the matrix.
	 * @param r1
	 * @param r2
	 */
	private void swapRow (int r1, int r2)
	{
		if (r1 == r2)
			return;

		for (int x = 0 ; x < xDim ; ++x) {
			double temp = values[r1][x];
			values[r1][x] = values[r2][x];
			values[r2][x] = temp;
		}
	}
}