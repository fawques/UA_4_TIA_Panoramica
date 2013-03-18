package javavis.jip2d.functions;

import java.io.*;
import java.util.ArrayList;
import java.awt.Image;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.util.HistoDistances;

/**
 * It calculates the histograms of the input image and compares it to the ones in the
 * image DB (got from the CalcHistoDB). Using the distance that the user indicates
 * with the parameter, the most similar image in the DB is returned and, 
 * depending on the percentage parameter, some additional images can be returned. It does 
 * not process an image, but the complete sequence (but only the first image is processed). 
 * The sequence returned is ordered by distance: first, the image with the less distance, 
 * then the next one and so on until the percentage parameter is reached.<br />
 * It applies to a COLOR type and it has to be a sequence.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>seq</em>: The input sequence to process. Each image has to be a COLOR images.</li>
 * <li><em>imageDB</em>: The file with the histogram Data Base.</li>
 * <li><em>disc</em>: Integer value which indicates the discretization value (number of bins) 
 * (default 20).</li>
 * <li><em>perc</em>: Real value which indicates the percentage (variation with respect the 
 * less distance) (default 0.1).</li>
 * <li><em>type</em>: List which indicates the type of the image (RGB, YCbCr, HSI) 
 * (default RGB).</li>
 * <li><em>algorithm</em>: List which indicates the distance algorithm to use (L1, L2, 
 * Jeffrey-divergence, Kullback-Leibler divergence) (default L1).</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>distance</em>: Real value which indicates the minimum distance.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The output sequence is the most similar images in the Data Base.</li>
 * </ul><br />
 */
public class SearchImage extends Function2D {
	private static final long serialVersionUID = 6455899959408417341L;

	public SearchImage() {
		super();
		name = "SearchImage";
		description = "Gets the images in a DB similars to the input image. Applies to a sequence with COLOR images.";
		groupFunc = FunctionGroup.ImageDB;

		ParamFile p1 = new ParamFile("imageDB", false, true);
		p1.setDescription("File with the histogram DB");
		p1.setDefault("out");
		
		ParamInt p2 = new ParamInt("disc", false, true);
		p2.setDefault(20);
		p2.setDescription("Discretization (number of bins)");
		
		ParamFloat p3 = new ParamFloat("perc", false, true);
		p3.setDefault(0.1f);
		p3.setDescription("Percentage 0.1=10%");
		
		ParamList p4 = new ParamList("type", false, true);
		String []paux = new String[3];
		paux[0] = "RGB";
		paux[1] = "YCbCr";
		paux[2] = "HSI";
		p4.setDefault(paux);
		p4.setDescription("Type of representation");
		
		ParamList p5 = new ParamList("algorithm", false, true);
		String []palg = new String[4];
		palg[0] = "L1";
		palg[1] = "L2";
		palg[2] = "Jeffrey-divergence";
		palg[3] = "Kullback-Leibler divergence";
		p5.setDefault(palg);
		p5.setDescription("Type of distance to use");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		
		//Output parameter
		ParamFloat r1 = new ParamFloat("distance", false, false);
		r1.setDescription("Result of the calculation");
		addParam(r1);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException("SearchImage must be applied to the complete sequence");
	}
		
	public Sequence processSeq(Sequence seq) throws JIPException {
		// First, it gets the first numFrame and checks if it is of color type
		JIPImage img = seq.getFrame(0);
		if (img == null || img.getType() != ImageType.COLOR) 
			throw new JIPException("SearchImage: image must be of color type");
				
		int disc = getParamValueInt("disc");
		String type = getParamValueString("type");
		// Processes the input image and gets the histogram
		CalcHistoColor chc = new CalcHistoColor();
		chc.setParamValue("disc", disc);
		chc.setParamValue("type", type);
		chc.processImg(img);
		if (chc.isInfo()) {
			info = "SearchImage info: "+chc.getInfo();
			return seq;
		}
		
		float [][][]histoImage =(float[][][])chc.getParamValueObj("histo");
		
		// Now, gets the image histogram DB 
		FileInputStream fos = null;
		ObjectInputStream oos = null;
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<float[][][]> histograms = new ArrayList<float[][][]>();
		try {
			fos = new FileInputStream(getParamValueString("imageDB"));
			oos = new ObjectInputStream(fos);
			while (true) {
				names.add(oos.readUTF());
				histograms.add((float[][][])oos.readObject());
			}
		}
		catch (EOFException e) {} // This is just to reach the end of file
		catch (ClassNotFoundException e) {
			throw new JIPException(e.getMessage());
		} 
		catch (FileNotFoundException e) {
			throw new JIPException("SearchImage: file "+getParamValueString("imageDB")+" not found");
		}
		catch (IOException e) {
			throw new JIPException("SearchImage: a Input/Output exception ocurred");
		}
		// If we are here, we have reached the end of the file
		try {
			if (oos != null && fos != null) {
				oos.close();
				fos.close();
			}
		}
		catch (Exception e) {System.out.println(e);}

		// And now, we have to search for images with low distances
		// but we have to know the algorithm to use
		double min = Double.MAX_VALUE;
		int index = -1;
		double []distances = new double[names.size()];
		String algo = getParamValueString("algorithm");
		
		for (int i=0; i < names.size(); i++) {
			if(algo.equals("L1"))
				distances[i] = HistoDistances.calcL1(histoImage, (float[][][])histograms.get(i));
			else if(algo.equals("L2"))
				distances[i] = HistoDistances.calcL2(histoImage, (float[][][])histograms.get(i));
			else if(algo.equals("Jeffrey-divergence"))
				distances[i] = HistoDistances.jeffrey(histoImage, (float[][][])histograms.get(i));
			else if(algo.equals("Kullback-Leibler divergence"))
				distances[i] = HistoDistances.kullbackLeibler(histoImage, (float[][][])histograms.get(i));
		 	
			if (distances[i] < min) {
				min = distances[i];
				index = i;
			}
		 } 
		
		// Returns the most similar image
		float perc = getParamValueFloat("perc");
		Image imgAWT = JIPToolkit.getAWTImage((String)names.get(index));
		if (imgAWT != null)
			img=JIPToolkit.getColorImage(imgAWT);
		
		seq.setFrame(img,0);
		int []indexes = new int[names.size()];
		indexes[0] = index;
		int max = 1;
		// And now, stores images with a distance less than the minimum increased
		// by the percentage parameter
		for (int i=0; i < distances.length; i++)
			if (index != i) {
				if (distances[i] <= min*(1+perc)) {
					imgAWT = JIPToolkit.getAWTImage((String)names.get(i));
					if (imgAWT != null) img=JIPToolkit.getColorImage(imgAWT);
					else continue;
					indexes[max]=-1;
					for (int j=1; j < max; j++) {
						if (distances[i] < distances[indexes[j]]) {
							seq.insertFrame(img,j);
							for (int k=max; k>j; k--) 
								indexes[k] = indexes[k-1];
							indexes[j] = i;
							break;
						}
					}
					if (indexes[max] == -1) {
						seq.addFrame (img);
						indexes[max] = i;
					}
					max++;
				}
			}
		System.out.println("Distance: " + (float)min);
		// Stores the minimun distance for later calculations 
		setParamValue("distance",(float)min);
		return seq;
	}
}

