package javavis.jip2d.functions;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Random;

import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.util.Distances;
import javavis.jip2d.util.HistoDistances;

/**
 * It calculates the histograms of the input image and compares them to the ones in the imageDB
 * (got from the CalcHistoDB).<br />
 * It applies to a sequence of images (but only the first image is processed).<br /><br />
 * <strong>Input parameters:</strong><br />
 * <li><em>seq</em>: Sequence with two images. Each image has to be a BYTE type.</li>
 * <li><em>dir</em>: Directory with the images to group.</li>
 * <li><em>histo</em>: Boolean value which indicates if it is necessary creates histograms (default
 * false).</li>
 * <li><em>numk</em>: Integer value which indicates the number of groups (default 2).</li>
 * <li><em>type</em>: List which contains the image type result (RGB, YCbCr, HSI) 
 * (default RGB).</li>
 * <li><em>algorithm</em>: List of strings which indicates the algorithm of distance is going to
 * be used (L1, L2, Jeffrey-divergence, Kullback-Leibler divergence, All) (default L1).</li>
 * <li><em>findBest</em>: Boolean value which indicates true if you want to apply KMeans until
 * the result is acceptable (default false).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A ordered sequence by distance: Firstly, the image with the less distance, then the
 * next one and so on until the percentage parameter is reached).</li>
 * </ul><br />
 */
public class HistoKMeans extends Function2D{
	private static final long serialVersionUID = 7556500542430306880L;

	public HistoKMeans() {
		super();
		name = "HistoKMeans";
		description = "Groups the image histograms in k groups.";
		groupFunc = FunctionGroup.ImageDB;

		ParamDir p1 = new ParamDir("dir", false, true);
		p1.setDescription("Directory to process");
		
		ParamBool p2 = new ParamBool("histo", false, true);
		p2.setDescription("Create histograms");
		p2.setDefault(false);
		
		ParamInt p3 = new ParamInt("numk", false, true);
		p3.setDescription("Number of groups");
		p3.setDefault(2);
		
		ParamList p4 = new ParamList("type", false, true);
		String []paux = new String[3];
		paux[0] = "RGB";
		paux[1] = "YCbCr";
		paux[2] = "HSI";
		p4.setDefault(paux);
		p4.setDescription("Type of the image");
		
		ParamList p5 = new ParamList("algorithm", false, true);
		String []palg = new String[4];
		palg[0] = "L1";
		palg[1] = "L2";
		palg[2] = "Jeffrey-divergence";
		palg[3] = "Kullback-Leibler divergence";
		p5.setDefault(palg);
		p5.setDescription("Type of distance to use");
		
		ParamBool p6 = new ParamBool("findBest", false, true);
		p6.setDescription("Find the best groups of K");
		p6.setDefault(false); 
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		String dir = getParamValueString("dir");
		boolean createHis = getParamValueBool("histo");
		String type = getParamValueString("type");
		int numK = getParamValueInt("numk");
		int disc = 20; //default value
		String fileDB = "outkmeans";
		boolean findBest = getParamValueBool("findBest");
		boolean right = true;
		File f;
		String pathDes, image;
		
		//check it is necessary to create histograms
		if (createHis)
			createDB(dir,disc,type,fileDB);
		
		//gets the image histogram DB
		FileInputStream fos = null;
		ObjectInputStream oos = null;
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<float[][][]> histograms = new ArrayList<float[][][]>();
		try {
			fos = new FileInputStream(fileDB);
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
			throw new JIPException("FKMeans: file "+getParamValueString("fileDB")+" not found");
		}
		catch (IOException e) {
			throw new JIPException("FKMeans: a Input/Output exception occurred");
		}

		//creates an array of distance
		Distances[] v_distances = new Distances[names.size()];

		//applies the algorithm until the result is correct
		do {
			right = kmeans(names,histograms,v_distances);
		} while(findBest && right);
		

		//creates the folders with the new groups
		//(there are any problem if the images and the new folders are
		//in the same hard disk)
		for (int group=1; group <= numK; group++) {
			 pathDes = "group" + group;
			 f = new File(pathDes);
			 
			 if (!f.mkdirs()) 
				 throw new JIPException ("HistoKMeans: can not create a dir");
			 
			 for (int j=0; j < names.size(); j++) {
			 	if (v_distances[j].getGroup() == group) {
			 		image = names.get(j).substring(names.get(j).lastIndexOf(File.separator));
			 		pathDes = "group"+group+image;
			 	}
			 }
		}
		return img;
	}

	
	/**
	 * Method which is the main function of KMeans.
	 * @param names ArrayList with the names of the images.
	 * @param histograms ArrayList with the histograms of the images.
	 * @param v_distances Array with the images with their distance, name and group.
	 * @return Boolean value indicating true if the group is acceptable, false when in some 
	 * group there is only an image (result not acceptable).
	 * @throws JIPException
	 */
	private boolean kmeans(ArrayList<String> names, ArrayList<float[][][]> histograms,Distances[] v_distances) 
			throws JIPException{
		int numImg = names.size();
		int numK = getParamValueInt("numk");
		Distances[] v_distancesPrev = new Distances[numImg];
		boolean converges = false;
		int iteration = 0;
		int []positions = new int[numK];
		ArrayList<float[][][]> averageHisto = new ArrayList<float[][][]>();

		//reserves the memory of the positions of the arrays
		for(int i=0; i < numImg; i++) {
			v_distances[i] = new Distances();
			v_distancesPrev[i] = new Distances();
		}
		
		//select the initial histograms by chance (the same number as k)
		selectInitialHistograms(positions,numImg,numK);

		//copies the base histograms to start KMedias
		for (int k=0; k < numK; k++)
			averageHisto.add(histograms.get(positions[k]));

		//calculates the distances 
		distance(names,histograms,averageHisto,v_distances,numK);
		
		//calculates the averages histograms
		calculateAveragesHisto(names,histograms,averageHisto,v_distances,numK);
		
		do {
			//copies the previous distances
			copyDistances(v_distances,v_distancesPrev);

			//calculates the distances
			distance(names,histograms,averageHisto,v_distances,numK);

			//calculates the averages histograms
			calculateAveragesHisto(names,histograms,averageHisto,v_distances,numK);

			//compares the arrays, if are the same it is the end
			//if are not the same continues
			converges = compareDistances(v_distances,v_distancesPrev);
			iteration++;
		}while(converges == false);
		
		//checks the number of the images in each group		
		if (countImgs(v_distances,numK))
			return true;
		else 
			return false;
	}
	
	/**
	 * Method which counts the images that belongs to a concrete group in each group must 
	 * have more than one, because if there are only one the groups are not correct.
	 * @param v_distances Array with the images with their distance, name and group.
	 * @param numK Number of groups.
	 * @return Boolean indicating true if there is only an image, false when there are 
	 * more than one image.
	 */
	private boolean countImgs(Distances[] v_distances,int numK) {
		int[] numImgs = new int[numK];
		
		for (int group=0; group < numK; group++) {
			for (int j=0; j < v_distances.length; j++) {
				if(v_distances[j].getGroup() == (group+1))
					numImgs[group]++;
			}
			if (numImgs[group] == 1)
				return true;
		}
		return false;
	}
	
	/**
	 * Method which creates a file with the histograms of the images.
	 * @param dir Directory to process.
	 * @param disc Number of bins.
	 * @param type Type of the image.
	 * @param fileDB File to store the histograms.
	 * @throws JIPException
	 */
	private void createDB (String dir,int disc, String type, String fileDB) throws JIPException {
		CalcHistoDB chDB = new CalcHistoDB();
		chDB.setParamValue("dir",dir);
		chDB.setParamValue("disc",disc);
		chDB.setParamValue("fileDB",fileDB);
		chDB.setParamValue("type",type);
		chDB.processImg(null);
	}
	
	/**
	 * Method which searches the minimum of an array.
	 * @param vector Real vector where search the minimum.
	 * @return The position of the minimum value in the vector.
	 */
	private int minimum(double[] vector) {
		int position = -1;
		double value = 100.0;
		
		for (int i=0; i < vector.length; i++)
			if (value > vector[i]) {
				value = vector[i];
				position = i;
			}

		return position;
	}
		
	/**
	 * Method which copies an array to another array.
	 * @param v_distances Array with the images with their distance, name and group.
	 * @param v_distancesPrev Previous array with the images with their distance, name and 
	 * group.
	 */
	private void copyDistances(Distances[] v_distances,Distances[] v_distancesPrev) {
		for (int i=0; i < v_distances.length; i++)
				v_distancesPrev[i].copyDis(v_distances[i]);
	}
	
	/**
	 * Method which selects two base histograms with a random function.
	 * @param positions Positions of the arrays where are the base histograms.
	 * @param numImg Number of images.
	 * @param k Number of groups.
	 */
	private void selectInitialHistograms(int[] positions, int numImg, int k) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int size = 0;
		Integer numAux;
		Random r = new Random();
		
		//one element
		list.add(r.nextInt(numImg));
		//two elements
		while (list.size() < 2) {
			numAux = r.nextInt(numImg);
			if (numAux.compareTo(list.get(0)) < 0)
				list.add(0,numAux);
			else
				if (!numAux.equals(list.get(0)))
					list.add(numAux);
		}

		//more than 2 elements
		while (list.size() < k) {
			numAux = r.nextInt(numImg);
			size = list.size();
			for (int i=0; i < size; i++) {
				if(numAux.equals(list.get(i)))
					i = size;
				else {
					if (numAux.compareTo(list.get(i))<0) {
						if (i == 0) { //first position
							list.add(0,numAux);
							i = size;
						}
						else { //whatever intermediate position
							list.add(i,numAux);
							i = size;
						}					
					}
					//last position
					if (i == (size-1) && numAux.compareTo(list.get(i)) > 0) {
						list.add(numAux);
						i = size;
					}
				}
			}
		}
		
		for (int i=0; i < list.size(); i++)
			positions[i] = list.get(i);
	}
	
	/**
	 * Method which calculates the distance between two histograms.
	 * @param nameImg ArrayList with the name of the images.
	 * @param aux_histo ArrayList with the histograms of the images.
	 * @param averageHisto ArrayList with the average histograms.
	 * @param v_distances Array with the images with their distance, name and group.
	 * @param numK Number of groups.
	 * @throws JIPException
	 */
	private void distance(ArrayList<String> nameImg, ArrayList<float[][][]> aux_histo, 
			ArrayList<float[][][]> averageHisto, Distances[] v_distances, int numK) throws JIPException {
		double[] distance = new double[numK];
		int position = 0;
		
		// And now, we have to search for images with low distances
		// but we have to know the algorithm to use
		String algorithm = getParamValueString("algorithm");
		for (int j=0; j < nameImg.size(); j++) {
		  	v_distances[j].setNameImg(nameImg.get(j));
		  	//calculates the distance between two histograms
			for(int i=0; i < numK; i++) { 
				if (algorithm.equals("L1"))
					distance[i] = HistoDistances.calcL1(averageHisto.get(i), aux_histo.get(j));
				else if (algorithm.equals("L2"))
					distance[i] = HistoDistances.calcL2(averageHisto.get(i), aux_histo.get(j));
				else if (algorithm.equals("Jeffrey-divergence"))
					distance[i] = HistoDistances.jeffrey(averageHisto.get(i), aux_histo.get(j));
				else if (algorithm.equals("Kullback-Leibler divergence"))
					distance[i] = HistoDistances.kullbackLeibler(averageHisto.get(i), aux_histo.get(j));
			}
			//searchs the minimun distance
			position = minimum(distance);

			//puts the image in the group with the minimum distance
			v_distances[j].setDistance(distance[position]);
			v_distances[j].setGroup(position+1);
		}
	}
	
	/**
	 * Method which compares both arrays of Distances, if are the same returns true.
	 * @param v_distances Array with the images with their distance, name and group
	 * @param v_distancesPrev Previous array with the images with their distance, name and 
	 * group.
	 * @return Boolean indicating true if the arrays are the same, false in otherwise.
	 */
	private boolean compareDistances(Distances[] v_distances, Distances[] v_distancesPrev) {
		int count = 0;
		
		for (int i=0; i < v_distances.length; i++)
			if (v_distances[i].isEquals(v_distancesPrev[i]))
				count++;
		
		if (count == v_distances.length)
			return true;
		else
			return false;
	}

	/**
	 * Method which calculates the new average histograms of each group.
	 * @param names ArrayList with the names of the images.
	 * @param histograms ArrayList with the histograms of the images.
	 * @param averageHisto ArrayList with the average histograms.
	 * @param v_distances Array with the images with their distance, name and group.
	 * @param numK Number of groups.
	 */
	private void calculateAveragesHisto (ArrayList<String> names,
			ArrayList<float[][][]> histograms, ArrayList<float[][][]> averageHisto,
			Distances[] v_distances, int numK) {
		float[][][] auxHisto2 = histograms.get(0);
		float[][][] auxHisto = new float[auxHisto2.length][auxHisto2.length][auxHisto2.length];
		float numImgs = 0.0f;
		
		for (int group=1; group <= numK; group++) {
			for (int i=0; i < auxHisto.length; i++) {
				for (int j=0; j < auxHisto.length; j++) {
					for (int k=0; k < auxHisto.length; k++) {
						for (int pos=0; pos < histograms.size(); pos++) {
							if ((v_distances[pos].getGroup() == group) && 
											((v_distances[pos].getNameImg()).equals(names.get(pos)))) {	
								auxHisto2 = histograms.get(pos);
								auxHisto[i][j][k] +=  auxHisto2[i][j][k];
								numImgs++;
							}
						}
						auxHisto[i][j][k] = auxHisto[i][j][k] / numImgs;
						//updates values
						numImgs = 0.0f;
					}
				}
			}
			//copies the new average histogram
			averageHisto.add(auxHisto);
			//update values
			auxHisto = new float[auxHisto2.length][auxHisto2.length][auxHisto2.length];
		}
	}	
}

