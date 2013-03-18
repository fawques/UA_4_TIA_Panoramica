package javavis.jip2d.util;

/**
 * This is a final class consisting of distances between image histograms.
 */
public final class HistoDistances {
	
	/**
	 * It calculates the L1 norm from two histograms
	 * @param histo1 First histogram
	 * @param histo2 Second histogram
	 * @return distance (L1) between this two histograms
	 */
	static public double calcL1 (float[][][]histo1, float[][][]histo2) {
		double acum = 0.0;
		for (int i=0; i<histo1.length; i++)
			for (int j=0; j<histo1.length; j++)
				for (int k=0; k<histo1.length; k++)
					acum += Math.abs(histo1[i][j][k]-histo2[i][j][k]);

		return acum;
	}
	
	/**
	 * It calculates the L2 norm from two histograms
	 * @param histo1 First histogram
	 * @param histo2 Second histogram
	 * @return distance (L2) between this two histograms
	 */
	static public double calcL2 (float[][][]histo1, float[][][]histo2) {
		double acum = 0.0;
		for (int i=0; i<histo1.length; i++)
			for (int j=0; j<histo1.length; j++)
				for (int k=0; k<histo1.length; k++)
					acum += ((histo1[i][j][k]-histo2[i][j][k])*(histo1[i][j][k]-histo2[i][j][k]));
		
		acum = Math.sqrt(acum);
		return acum;
	}
	
	/**
	 * It calculates the Kullback-Leibler divergence from two histograms
	 * @param histo1 First histogram
	 * @param histo2 Second histogram
	 * @return distance between this two histograms
	 */
	static public double kullbackLeibler (float[][][]histo1, float[][][]histo2) {
		double acum;
		acum = 0.0;
		for (int i=0; i<histo1.length; i++)
			for (int j=0; j<histo1.length; j++)
				for (int k=0; k<histo1.length; k++) {
			 	 if(histo1[i][j][k]==0.0)
			 	 	acum += 0.0;
			 	 else
			 	 	if(histo2[i][j][k]==0.0)
			 	 		acum += histo1[i][j][k]*(Math.log(histo1[i][j][k]/0.00000001));
			 	 	else
			 	 		acum += histo1[i][j][k]*(Math.log(histo1[i][j][k]/histo2[i][j][k]));
				}
		return acum;
	}
	
	/**
	 * It calculates the Jeffrey-divergence from two histograms
	 * @param histo1 First histogram
	 * @param histo2 Second histogram
	 * @return distance between this two histograms
	 */
	static public double jeffrey (float[][][]histo1, float[][][]histo2) {
		double acum, acumAux1, acumAux2;
		acum = acumAux1 = acumAux2 = 0.0;
		for (int i=0; i<histo1.length; i++)
			for (int j=0; j<histo1.length; j++)
				for (int k=0; k<histo1.length; k++) {
					if(histo1[i][j][k]==0.0 && histo2[i][j][k]==0.0)
						 acumAux1 = acumAux2 = 0.0;	
					else {	
						if(histo1[i][j][k]==0.0) {
							acumAux1 = 0.0;
							acumAux2 = histo2[i][j][k]*(Math.log(histo2[i][j][k]/0.00000001));
						}
						else {
							if(histo2[i][j][k]==0.0) {
								acumAux1 = histo1[i][j][k]*(Math.log(histo1[i][j][k]/0.00000001));
								acumAux2 = 0.0;
							}
							else {
								acumAux1 = histo1[i][j][k]*(Math.log(histo1[i][j][k]/histo2[i][j][k]));
								acumAux2 = histo2[i][j][k]*(Math.log(histo2[i][j][k]/histo1[i][j][k])); 	
							}
						}
					}
					acum += acumAux1 + acumAux2;
				}
		return acum;
	}
}
