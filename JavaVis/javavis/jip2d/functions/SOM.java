package javavis.jip2d.functions;

import java.io.*;
import java.util.ArrayList;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamString;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;

/**
 * It makes the training of a SOM, applied to the image histogram problems.<br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * </ul></li>
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>imageDB</em>: File with the histogram Data Base.</li>
 * <li><em>disc</em>: Integer value which indicates the discretization value (number of bins) 
 * (default 20).</li>
 * <li><em>rows</em>: Integer value which indicates the number of rows of the SOM (default 5).</li>
 * <li><em>columns</em>: Integer value which indicates the number of columns of the SOM (default 5).
 * </li>
 * <li><em>learning</em>: Real value which indicates the initial learning index (default 0.01).</li>
 * <li><em>iter</em>: Integer value which indicates the number of iterations (default 200).</li>
 * <li><em>outputFile</em>: File to save the results (default HTML extension).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The same image and the training of a SOM.</li>
 * </ul><br />
 */
public class SOM extends Function2D {
	private static final long serialVersionUID = 7243549182678063124L;

	public SOM() {
		name = "SOM";
		description = "Makes the training step of a SOM.";
		groupFunc = FunctionGroup.ImageDB;
		
		ParamFile p1 = new ParamFile("imageDB", true, false);
		p1.setDescription("File with the histogram DB");
		
		ParamInt p2 = new ParamInt("disc", true, false);
		p2.setDefault(20);
		p2.setDescription("Discretization (number of bins)");
		
		ParamInt p3 = new ParamInt("rows", true, false);
		p3.setDefault(5);
		p3.setDescription("Number of rows of the SOM");
		
		ParamInt p4 = new ParamInt("columns", true, false);
		p4.setDefault(5);
		p4.setDescription("Number of columns of the SOM");
		
		ParamFloat p5 = new ParamFloat("learning", true, false);
		p5.setDefault(0.01f);
		p5.setDescription("Initial learning index");
		
		ParamInt p6 = new ParamInt("iter", true, false);
		p6.setDefault(200);
		p6.setDescription("Number of iterations");
		
		// Number of iterations in the learning phase of the SOM 
		ParamString p7 = new ParamString("outputFile", true, false);
		p7.setDefault("file.html");
		p7.setDescription("File to save the results");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
	}

	public JIPImage processImg (JIPImage img) throws JIPException {
		double radius, approxRate, errorCuant;
		Neuron nWinner;
		ArrayList<ImageAttributes>[][] classification;
		
		String fileDB = getParamValueString("imageDB");
		int disc = getParamValueInt("disc");
		int rowsSOM = getParamValueInt("rows");
		int colsSOM = getParamValueInt("columns");
		double TAI = getParamValueFloat("learning");
		int numIter = getParamValueInt("iter");
		
		MySOM som = new MySOM (rowsSOM, colsSOM, disc);
		
		ArrayList<ImageAttributes> histograms = readHistograms (fileDB, disc);
		
		if (histograms.isEmpty()) 
			throw new JIPException("DB is empty or corrupted.");
		
		double radiusMap = Math.max(rowsSOM, colsSOM) / 2.0;
		double constTime = numIter / Math.log(radiusMap);
		
		for (int i=0; i < numIter; i++) {	
			radius = radiusMap * Math.exp(-i/constTime);
			approxRate = TAI * Math.exp(-i/(double)numIter); 
			
			errorCuant = 0.0;
			for (int ej=0; ej < histograms.size(); ej++) {
				nWinner = som.searchWinner(histograms.get(ej).getHistogram());
				if (nWinner == null) 
					throw new JIPException("There is no winner neuron");
				errorCuant += som.getDistanceToWinner();
				som.refreshWeights(histograms.get(ej).getHistogram(), radius, nWinner, approxRate);
			}
		}

		classification = new ArrayList[rowsSOM][colsSOM];
		for (int i=0; i < rowsSOM; i++)
			for (int j=0; j < colsSOM; j++)
				classification[i][j] = new ArrayList<ImageAttributes>();
		
		for (int ej=0; ej < histograms.size(); ej++) {
			nWinner = som.searchWinner(histograms.get(ej).getHistogram());
			classification[nWinner.getRow()][nWinner.getColumn()].add(histograms.get(ej));
		}
		
		PrintWriter p;
		try {
			p = new PrintWriter (new FileOutputStream (getParamValueString("outputFile")));
			showClassification (classification, rowsSOM, colsSOM, p);
				
			showRepresentativeImage(classification, rowsSOM, colsSOM, disc, som, p);
			p.close();
		}
		catch (Exception e) {
			throw new JIPException("Error creating the HTML file.");
		}
			
		return img;
	}
	
	
	/**
	 * Method which reads a histogram.
	 * @param imageDB File name which contains the histogram DB.
	 * @param disc Discretization value (number of bins).
	 * @return An array of image attributes.
	 * @throws JIPException
	 */
	private ArrayList<ImageAttributes> readHistograms (String imageDB, int disc) throws JIPException {
		FileInputStream fis;
		ObjectInputStream ois;
		ImageAttributes attrib;
		
		ArrayList<ImageAttributes> list = new ArrayList<ImageAttributes>();
		
		try {
			fis = new FileInputStream (imageDB);
			ois = new ObjectInputStream (fis);
			
			while (ois.available()>0) {
				attrib = new ImageAttributes (ois.readUTF(), (float[][][])ois.readObject());
				list.add(attrib);
			}	
			
			fis.close();
			ois.close();
		}
		catch (Exception e) {
			throw new JIPException("Error reading " + imageDB);
		}
		
		return list;
	}
	
	/**
	 * Method which shows the images for each neuron.
	 * @param classification The classification.
	 * @param rowsSOM Number of rows of the SOM
	 * @param colsSOM Number of columns of the SOM
	 * @param p Place where the data will be written.
	 */
	private void showClassification (ArrayList[][] classification, int rowsSOM, int colsSOM, PrintWriter p) {
		String imagePath;

		p.println("<html><br>");
		p.println("<head>");
		p.println("<title>SOM applied to histogram images</title>");
		p.println("</head>");
		p.println("<body>");
		for (int i=0; i < rowsSOM; i++)
			for (int j=0; j < colsSOM; j++) 
				if (!classification[i][j].isEmpty()) {
					p.println ("<hr>");
					p.println ("<center><h1>NEURON ("+i+","+j+")</h1></center><br>");
					for (int k=0; k < classification[i][j].size(); k++) {
						imagePath = "file:///"+((ImageAttributes)classification[i][j].get(k)).getPath();
						p.println ("<a href=\""+imagePath+"\">"+"<img src=\""+imagePath+"\" width=\"132\" height=\"92\">&nbsp;"+"</a>");
					}
				}			
	}
	
	/**
	 * Method which shows a web page with the representative images.
	 * @param classification The classification.
	 * @param rowsSOM Number of rows of the SOM
	 * @param colsSOM Number of columns of the SOM
	 * @param disc Discretization value (number of bins).
	 * @param som The SOM object.
	 * @param p Place where the data will be written.
	 */
	private void showRepresentativeImage (ArrayList[][] classification, int rowsSOM, int colsSOM, int disc, MySOM som, PrintWriter p) {
		float[][][] histoMedium = new float[disc][disc][disc];
		float[][][] histoTemp;
		double distance, minDist;
		String representImage = "";
		
		p.println ("<hr>");
		p.println ("<hr>");
		p.println ("<br><br><center><h1>Representative images</h1></center><br>");
		p.println ("<center><table border=\"1\" cellpadding=\"2\">");
		
		for (int i=0; i < rowsSOM; i++) {
			p.println ("<tr heigth=\"92\">");
			for (int j=0; j < colsSOM; j++) {
				p.println ("<td width=\"132\">");
				if (!classification[i][j].isEmpty()) {
					initializeMeanHisto (histoMedium, disc);
					for (int k=0; k < classification[i][j].size(); k++) {	
						histoTemp = ((ImageAttributes)classification[i][j].get(k)).getHistogram();
						histoMedium = sumHistos (histoTemp, histoMedium, disc);
					}
							
					for (int a=0; a < disc; a++)
						for (int b=0; b < disc; b++)
							for (int c=0; c < disc; c++)
								histoMedium[a][b][c] /= classification[i][j].size();
					
					minDist = Double.MAX_VALUE;
					for (int k=0; k < classification[i][j].size(); k++) {
						distance = som.distanceL1(((ImageAttributes)classification[i][j].get(k)).getHistogram(), histoMedium);
						if (distance<minDist) {
							representImage =  ((ImageAttributes)classification[i][j].get(k)).getPath();
							minDist = distance;
						}
					}			
					representImage = "file:///"+representImage;
					p.println ("<a href=\""+representImage+"\">"+"<img src=\""+representImage+"\" width=\"132\" height=\"92\">"+"</a>");
				}
				p.println ("</td>");
			}
			p.println ("</tr>");
		}	
					
		p.println ("</table></center>");
		p.println("</body>");
		p.println("</html>");		
	}
	
	/**
	 * Method which sums the result of 2 histograms.
	 * @param h1 The first histogram.
	 * @param h2 The second histogram.
	 * @param disc Discretization value (number of bins).
	 * @return The sum of the histograms.
	 */
	private float[][][] sumHistos (float[][][] h1, float[][][] h2, int disc) {
		float[][][] res = new float[disc][disc][disc];
		for (int i=0; i < disc; i++)
			for (int j=0; j < disc; j++)
				for (int k=0; k < disc; k++)
					res[i][j][k] = h1[i][j][k] + h2[i][j][k];		
		return res;
	}
	
	/**
	 * Method which initializes the mean histogram.
	 * @param meanHisto The histogram which we have to initialize.
	 * @param disc Discretation value (number of bins).
	 */
	private void initializeMeanHisto (float[][][] meanHisto, int disc) {
		for (int i=0; i < disc; i++)
			for (int j=0; j < disc; j++)
				for (int k=0; k < disc; k++) 
					meanHisto[i][j][k] = 0;
	}
	
	
	/**
	 * Auxiliary class to manage the histograms
	 */
	private class ImageAttributes {
		/**
		 * @uml.property  name="path"
		 */
		String path;
		/**
		 * @uml.property  name="histogram"
		 */
		float[][][] histogram;
		
		public ImageAttributes (String _path, float[][][] histo) {
			path = _path;
			histogram = histo;
		}
		
		public String getPath () { return path;	}		
		public float[][][] getHistogram () { return histogram; }
	}

	
	/**
	 * This class implements a neuron, part of a SOM, applied to image histograms.
	 */
	class Neuron {
		int row;
		int column;
		float [][][] weights;
		int disc;		
		
		public Neuron (int f, int c, int _disc) {
			row = f;
			column = c;
			disc = _disc;
			weights = new float [disc][disc][disc];
			setWeights ();
		}
		
		/**
		 * Set the initial values of the neuron in a random way, so that the sum of 
		 * all of these values is 1.
		 */
		private void setWeights () {
			double sum = 0.0;
			
			for (int i=0; i < disc; i++)
				for (int j=0; j < disc; j++)
					for (int k=0; k < disc; k++) {
						weights[i][j][k] = (float)Math.random();
						sum += weights[i][j][k];
					}
			
			for (int i=0; i < disc; i++)
				for (int j=0; j < disc; j++)
					for (int k=0; k < disc; k++) {
						weights[i][j][k] /= sum;
					}
		}
	
		public void refreshWeights (float[][][] example, double radius, double approxRate, double dist) {
			for (int i=0; i < disc; i++)
				for (int j=0; j < disc; j++)
					for (int k=0; k < disc; k++)
						weights[i][j][k] += (example[i][j][k] - weights[i][j][k]) 
								* Math.exp(-Math.pow(dist,2)/(2*Math.pow(radius,2))) * approxRate; 
		}
		
		public int getRow () { return row; }
		public int getColumn () { return column; }
		public float[][][] getWeights () { return weights; }
	}
	
	
	/**
	 * This class represents a SOM, applied to image histograms.
	 */
	class MySOM {
		/**
		 * @uml.property  name="numRows"
		 */
		int numRows;
		/**
		 * @uml.property  name="numColumns"
		 */
		int numColumns;
		/**
		 * @uml.property  name="neurons"
		 * @uml.associationEnd  multiplicity="(0 -1)"
		 */
		Neuron [][] neurons;
		double distanceToWinner;	
		
		public MySOM (int fs, int cs, int disc) {
			numRows = fs;
			numColumns = cs;
			neurons = new Neuron [numRows][numColumns];
			
			for (int i=0; i<numRows; i++)
				for (int j=0; j<numColumns; j++)
					neurons[i][j] = new Neuron (i, j, disc);
		}
		
		/**
		 * Gets the closest neuron (distance L1) to the example.
		 */
		public Neuron searchWinner (float[][][] histo) {
			Neuron n = null;
			double distance, minDist = Double.MAX_VALUE;
					
			for (int i=0; i < numRows; i++)
				for (int j=0; j < numColumns; j++) {
					distance = distanceL1(histo, neurons[i][j].getWeights());
					if (distance<minDist) {
						n = neurons[i][j];
						minDist = distance;
					}
				}
			
			distanceToWinner = minDist;
			return n;
		}
		
		/**
		 * Refresh the weights of all the neurons inside a certain radius from
		 * the winner neuron.
		 */
		public void refreshWeights (float[][][] example, double radius, Neuron nWinner, double approxRate) {
			double eucDist;
			
			for (int i=0; i < numRows; i++) {
				for (int j=0; j < numColumns; j++) {
					eucDist = Math.sqrt(Math.pow(i-nWinner.getRow(),2) + Math.pow (j-nWinner.getColumn(),2));
					if (eucDist <= radius)
						neurons[i][j].refreshWeights (example, radius, approxRate, euclideanDistance(neurons[i][j].getWeights(), nWinner.getWeights()));
				}				 
			}		
		}
			
		/** 
		 * Euclidean Distance.
		 */
		public double euclideanDistance (float[][][] h1, float[][][] h2) {
			int disc = h1.length;
			double distance = 0.0;
					
			for (int i=0; i < disc; i++)
				for (int j=0; j < disc; j++)
					for (int k=0; k < disc; k++) {
						distance += Math.pow(h1[i][j][k] - h2[i][j][k], 2); 					
					}
			
			return Math.sqrt(distance);
		}
		
		/**
		 * Returns the L1 norm.
		 */
		public double distanceL1 (float[][][] h1, float[][][] h2) {
			int disc = h1.length;
			double distance = 0.0;
					
			for (int i=0; i < disc; i++)
				for (int j=0; j < disc; j++)
					for (int k=0; k < disc; k++)
						distance += Math.abs(h1[i][j][k] - h2[i][j][k]); 
			
			return distance;
		}
		
		/**
		 * @return   the number of rows
		 * @uml.property  name="numRows"
		 */
		public int getNumRows () { return numRows; }
		/**
		 * @return   the number of columns
		 * @uml.property  name="numColumns"
		 */
		public int getNumColumns () { return numColumns; }
		public double getDistanceToWinner () {return distanceToWinner; }
	}

}
	
