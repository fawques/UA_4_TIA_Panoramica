package javavis.jip2d.functions;

import java.awt.Image;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.util.HistoDistances;

/**
 * It calculates distances from the input directory with the image DB, it is used in 
 * different distances like L1 norm, L2 norm, Kullback-Leiber divergence and Jeffrey 
 * divergence and different values like number of bins to compare the results. 
 * Functions like CalcHistoDB and SearchImage are used to simplify this function.<br />
 * It applies to a sequence of images and these images must have COLOR type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <li><em>seq</em>: Sequence with images. Each image has to be a COLOR type.</li>
 * <li><em>type</em>: List which indicates the image type result (RGB, YCbCr, HSI) 
 * (default RGB).</li>
 * <li><em>disc</em>: Integer value which indicates the discretization (number of bins) (default
 * 20).</li>
 * <li><em>algorithm</em>: List of strings which indicates the algorithm of distance is going to
 * be used (L1, L2, Jeffrey-divergence, Kullback-Leibler divergence, All) (default L1).</li>
 * <li><em>createDB</em>: Boolean value indicating if it is going to create the histogram 
 * DB (default false).</li>
 * <li><em>dir</em>: Directory which contains the Data Base images.</li> 
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image (only one distance is used) or a processed sequence of images (one
 * for each distance).</li>
 * </ul><br />
 */
public class GroupImages extends Function2D { 
	private static final long serialVersionUID = -3221456052818528739L;

	private static final int NUM_ALGORITHMS = 4;
	
	public GroupImages(){
		super();
		name = "GroupImages";
		description = "It generates a matrix with the distances between a image DB";
		groupFunc = FunctionGroup.ImageDB;

		ParamList p1 = new ParamList("type", false, true);
		String []paux = new String[3];
		paux[0] = "RGB";
		paux[1] = "YCbCr";
		paux[2] = "HSI";
		p1.setDefault(paux);
		p1.setDescription("Type of the image");
		
		ParamInt p2 = new ParamInt("disc", false, true);
		p2.setDefault(20);
		p2.setDescription("Discretization (number of bins)");
		
		ParamList p3 = new ParamList("algorithm", false, true);
		String []palg = new String[5];
		palg[0] = "L1";
		palg[1] = "L2";
		palg[2] = "Jeffrey-divergence";
		palg[3] = "Kullback-Leibler divergence";
		palg[4] = "All";
		p3.setDefault(palg);
		p3.setDescription("Type of distance to use");
		
		ParamBool p4 = new ParamBool("createDB", false, true);
		p4.setDescription("Histograms DB to create?");
		p4.setDefault(false);
		
		ParamDir p5 = new ParamDir("dir", false, true);
		p5.setDescription("Directory to process");
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException("FGroupImage must be applied to the complete sequence.");
	}
	
	public Sequence processSeq(Sequence seq) throws JIPException {
		String dir = getParamValueString("dir");
		String fileDB = null;
		int disc = getParamValueInt("disc");
		String type = getParamValueString("type");
		ArrayList<String> nameImages = new ArrayList<String>();
		Sequence sequence = new Sequence();

		//creates the JIPSquence with the images of the directory
		doProcessing(sequence,dir,nameImages);
				
		//creates the name of the file where write the results 
		if (type.equals("RGB"))
			fileDB = "outMatrizRGB" + disc;
		else
			if (type.equals("HSI"))
				fileDB = "outMatrizHSI" + disc;
			else
				if (type.equals("YCbCr"))
					fileDB = "outMatrizYCbCr" + disc;

		return createSequence(sequence,nameImages,dir,fileDB,type,disc);
	}
	
	
	/**
	 * Method which creates a Sequence of the directory, the images can be directly 
	 * in the directory or in the directory could be other folders with the images 
	 * (only this two cases).
	 * @param sequence Sequence of the images.
	 * @param dir directory where are the images.
	 * @param nameImages ArrayList with the name of the images.
	 * @throws JIPException
	 */
	private void doProcessing (Sequence sequence, String dir, 
			ArrayList<String> nameImages) throws JIPException {
		File f = new File(dir);
		//Get the names of files and directories in the current directory
		String []clusters = f.list();
		String []images;
		JIPImage imgAux = null;
		int count = 0;
		
		sequence.setName("Images processed");
		for (int i=0; i < clusters.length; i++) {
			String group = dir + File.separator + clusters[i];
			File f2 = new File(group);
			//only processes the directories
			if (f2.isDirectory()) {
				images = f2.list();
				//processes all the images in the directory
				for (int pic=0; pic < images.length; pic++)  {
					String fileImg = group + File.separator + images[pic];
					Image imgAWT = JIPToolkit.getAWTImage(fileImg);
					if (imgAWT != null) {
						imgAux = JIPToolkit.getColorImage(imgAWT);
						imgAux.setName(images[pic]);
						sequence.addFrame(imgAux);
						nameImages.add(images[pic]);
						count++;
					}
				}
			}
			else {
				//Processes all the images in the directory
				Image imgAWT = JIPToolkit.getAWTImage(group);
				if (imgAWT != null) {
					imgAux = JIPToolkit.getColorImage(imgAWT);
					imgAux.setName(clusters[i]);
					sequence.addFrame(imgAux);
					nameImages.add(clusters[i]);
					count++;
				}
			}
		}
	}

	/**
	 * Method which creates a file with the histograms of the images.
	 * @param dir Directory to process.
	 * @param disc Number of bins.
	 * @param type Type of the image.
	 * @param fileDB File to store the histograms.
	 * @throws JIPException
	 */
	private void createDB(String dir, int disc, String type, String fileDB) throws JIPException {
		CalcHistoDB chDB = new CalcHistoDB();
		chDB.setParamValue("dir",dir);
		chDB.setParamValue("disc",disc);
		chDB.setParamValue("fileDB",fileDB);
		chDB.setParamValue("type",type);
		chDB.processImg(null);
	}
	
	/**
	 * Method which creates a sequence of the result image (only applies one distance)
	 * or result images (applies all the distances) to the directory with the images.
	 * @param sequence Sequence of the images from the directory to process.
	 * @param nameImages ArrayList with the names of the images.
	 * @param dir Directory to process.
	 * @param fileDB File with the histograms.
	 * @param type The image type.
	 * @param disc Number of bins.
	 * @return Sequence of the images with the results.
	 * @throws JIPException
	 */
	private Sequence createSequence (Sequence sequence, 
			ArrayList<String> nameImages, String dir, String fileDB, 
			String type, int disc) throws JIPException {
		String fileDBTests = "ResultMatriz.txt";
		String algorithm = getParamValueString("algorithm");
		String []palg = new String[4];
		palg[0] = "L1";
		palg[1] = "L2";
		palg[2] = "Jeffrey-divergence";
		palg[3] = "Kullback-Leibler divergence";
		float [][][] histogram1;
		boolean createDB = getParamValueBool("createDB");		
		
		//creates the histograms DB if it's necessary
		if (createDB)
			createDB(dir,disc,type,fileDB);
		
		//Now, gets the image histogram DB 
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
			System.out.println(e);
		} 
		catch (FileNotFoundException e) {
			throw new JIPException("GroupImages: file "+getParamValueString("imageDB")+" not found");
		}
		catch (IOException e) {
			throw new JIPException("GroupImages: a Input/Output exception occurred");
		}
		
		CalcHistoColor chc = new CalcHistoColor();
		chc.setParamValue("disc", disc);
		chc.setParamValue("type",type);

		double [][] distances = new double[sequence.getNumFrames()][histograms.size()];
		JIPImage result = JIPImage.newImage(sequence.getNumFrames(),histograms.size(),ImageType.FLOAT);
		PrintWriter pw = null;
		int position = -1;
		
		if (!algorithm.equals("All")) {
			try {
				pw = new PrintWriter (new FileWriter(fileDBTests));
				pw.println("Type of the image: "+type+"\t\tDistance: "+algorithm);
			} catch (IOException e)  {
				throw new JIPException("GroupImages: a Input/Output exception occurred");
			}
			
			for (int i=0; i < sequence.getNumFrames(); i++) {
				JIPImage imgMatrix = sequence.getFrame(i);

				//searchs the histogram of the image in the array
				position = searchPosition(imgMatrix,names);
				histogram1 = (float[][][])histograms.get(position);

				//writes in the result image the type and the distance used
				result.setName("Result->  type: " + type + " distance: " + algorithm);

				for (int j=0; j < histograms.size(); j++) {
					if (algorithm.equals("L1"))
						distances[i][j] = HistoDistances.calcL1(histogram1,(float[][][])histograms.get(j));
					else
						if (algorithm.equals("L2"))
							distances[i][j] = HistoDistances.calcL2(histogram1,(float[][][])histograms.get(j));
						else
							if (algorithm.equals("Jeffrey-divergence"))
								distances[i][j] = HistoDistances.jeffrey(histogram1,(float[][][])histograms.get(j));
							else
								if (algorithm.equals("Kullback-Leibler divergence"))
									distances[i][j] = HistoDistances.kullbackLeibler(histogram1,(float[][][])histograms.get(j));

					//writes in the file the distance calculated				
					pw.print(" " + (float)distances[i][j]);
				}
				pw.println();
			}
			//closes the file
			pw.close();
			//put the distances calculated in the result image
			introducePixels(result,sequence.getNumFrames(),histograms.size());
			
			OpenWindow fopw = new OpenWindow();
			//opens a window with the result image
			fopw.processImg(result);
			return sequence;
		}
		else {    //applies all the distances
			try {
				pw = new PrintWriter (new FileWriter(fileDBTests));
			} catch (IOException e)  {
				throw new JIPException("GroupImages: a Input/Output exception occurred");
			}
			
			for (int m=0; m < NUM_ALGORITHMS; m++) { //4 because it is the number of algorithms we have
				pw.println("Type of the image: " + type + "\t\tDistance: " + palg[m]);
						
				for (int i=0; i < sequence.getNumFrames(); i++) {	
					JIPImage imgMatrix = sequence.getFrame(i);

					//searchs the histogram of the image in the array
					position = searchPosition(imgMatrix,names);
					histogram1 = (float[][][])histograms.get(position);

					for (int j=0; j < histograms.size(); j++) {
						if (palg[m].equals("L1"))
							distances[i][j] = HistoDistances.calcL1(histogram1,(float[][][])histograms.get(j));
						else
							if (palg[m].equals("L2"))
								distances[i][j] = HistoDistances.calcL2(histogram1,(float[][][])histograms.get(j));
							else
								if (palg[m].equals("Jeffrey-divergence"))
									distances[i][j] = HistoDistances.jeffrey(histogram1,(float[][][])histograms.get(j));
								else
									if (palg[m].equals("Kullback-Leibler divergence"))
										distances[i][j] = HistoDistances.kullbackLeibler(histogram1,(float[][][])histograms.get(j));
						//writes in the file the distance calculated								
						pw.print(" " + (float)distances[i][j]);
					}
					pw.println();
				}
			}
			//closes the file
			pw.close();
			
			ArrayList<JIPImage> images = new ArrayList<JIPImage>();
			//put the distances calculated in the result images
			introduceAllPixels(images,sequence.getNumFrames(),sequence.getNumFrames(),type);
			
			//creates the sequence of the result images
			Sequence sequenceAlg = new Sequence((JIPImage)images.get(0));
			for (int i=1; i < images.size(); i++)
				sequenceAlg.addFrame((JIPImage)images.get(i));
			
			sequenceAlg.setName("Results");
			
			return sequenceAlg;
		}
	}

	/**
	 * Method which searches a position of an image in the array of histograms
	 * @param imgMatrix JIPImage.
	 * @param names ArrayList with the names of the images.
	 * @return An integer indicating the position in the array.
	 */
	private int searchPosition(JIPImage imgMatrix,ArrayList<String> names) {
		int pos = -1;
		String imgName = imgMatrix.getName();
		String auxName;
		
		for (int i=0; i < names.size(); i++) {
			auxName = names.get(i);
			if (auxName.endsWith(imgName)) {
				pos = i;
				i = names.size();
			}
		}
		
		return pos;
	}
	
	/**
	 * Method which gets the maximum and the minimum of the calculated values
	 * of the image to standardize in [0..1].
	 * @param maxmin ArrayList with these values.
	 * @throws JIPException
	 */
	private void maxMin(ArrayList<Double> maxmin) throws JIPException {
		double max, min;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		FileReader fileR = null;
		
		try {
			fileR = new FileReader("ResultMatriz.txt");
			StreamTokenizer st = new StreamTokenizer(fileR);
    						
			while (st.nextToken() != StreamTokenizer.TT_EOF)  {	
				switch (st.ttype) {
					case StreamTokenizer.TT_NUMBER:
		        		if (st.nval >= max)
		        			max = st.nval;
		        		if (st.nval <= min)
		        			min = st.nval;
						break;
				}
			}
			fileR.close();
		} catch (Exception e)  {
			throw new JIPException("GroupImages: a Input/Output exception occurred");
		}
		
		maxmin.add(max);
		maxmin.add(min);
	}
	
	/**
	 * Method which gets the maximum and the minimum of the calculated values
	 * of all the images to standardize in [0..1].
	 * @param maxmin ArrayList with these values.
	 * @throws JIPException
	 */
	private void maxMinAll(ArrayList<Double> maxmin) throws JIPException {
		String []palg = new String[4];
		palg[0] = "L1";
		palg[1] = "L2";
		palg[2] = "Jeffrey-divergence";
		palg[3] = "Kullback-Leibler";
		int count = 0;
		double max, min;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		FileReader fileR = null;
		
		try {
			fileR = new FileReader("ResultMatriz.txt");
			StreamTokenizer st = new StreamTokenizer(fileR);
    						
			while (st.nextToken() != StreamTokenizer.TT_EOF)  {	
				switch (st.ttype) {
					case StreamTokenizer.TT_WORD:
						if ((st.sval).equals(palg[count])) {
							if (count != 0) {
								maxmin.add(max);
								maxmin.add(min);
							}
							if (count == 3)
								st.nextToken();
							count++;
							min = Double.MAX_VALUE;
							max = Double.MIN_VALUE;
						}
						break;						
					case StreamTokenizer.TT_NUMBER:
		        		if (st.nval >= max)
		        			max = st.nval;
		        		if (st.nval <= min)
		        			min = st.nval;
						break;
				}
			}
			maxmin.add(max);
			maxmin.add(min);
			fileR.close();
		} catch (Exception e)  {
			throw new JIPException("GroupImages: a Input/Output exception occurred");
		}
	}
	
	/**
	 * Method which puts the values of the distances calculated in the result image.
	 * @param result Result image.
	 * @param numRow Number of rows.
	 * @param numCol Number of columns.
	 * @throws JIPException
	 */
	private void introducePixels(JIPImage result,int numRow,int numCol) throws JIPException{
		double min, max;
		double xPrime = 0.0;
		int row, column;
		row = column=0;
		FileReader fr = null;
		ArrayList<Double> maxmin = new ArrayList<Double>();
		
		maxMin(maxmin);
		
		max = maxmin.get(0);
		min = maxmin.get(1);
	
		try {
			fr = new FileReader("ResultMatriz.txt");
			StreamTokenizer st = new StreamTokenizer(fr);
			while (st.nextToken() != StreamTokenizer.TT_EOF)  {	
				switch (st.ttype) {
					case StreamTokenizer.TT_NUMBER:
						//modifies x for x' (standardized in [0..1])
						if (st.nval != 0.0)
							xPrime = (st.nval-min)/(max-min);
						else
							xPrime = 0.0;
						if (column < (numCol-1)) {
							((JIPImgBitmap)result).setPixel(row,column,xPrime);
							column++;
						}
						else {
							column = 0;
							row++;
						}
						break;
				}
			}
			//closes the file
			fr.close();
		} catch (Exception e)  {
			throw new JIPException("GroupImages: a Input/Output exception occurred");
		}
	}

	/**
	 * Method which puts the values of the distances calculated in the result images.
	 * @param list_images ArrayList with the result image of each distance.
	 * @param numRow Number of rows.
	 * @param numCol Number of columns.
	 * @param type The image type.
	 * @throws JIPException
	 */
	private void introduceAllPixels (ArrayList<JIPImage> list_images,
			int numRow, int numCol, String type) throws JIPException {
		String []palg = new String[4];
		palg[0] = "L1";
		palg[1] = "L2";
		palg[2] = "Jeffrey-divergence";
		palg[3] = "Kullback-Leibler";
		double min, max;
		double xPrime = 0.0;
		int row, column, count, countImg;
		row = column = count = countImg = 0;
		min = max = 0.0;
		FileReader fr = null;
		JIPImgBitmap image = null;
		ArrayList<Double> maxmin = new ArrayList<Double>();
		
		maxMinAll(maxmin);
		
		try {
			fr = new FileReader("ResultMatriz.txt");
			StreamTokenizer st = new StreamTokenizer(fr);
			while (st.nextToken() != StreamTokenizer.TT_EOF)  {	
				switch (st.ttype) {
					case StreamTokenizer.TT_WORD:
						if ((st.sval).equals(palg[count])) {
							if (count != 0) {
								image.setName(type + " " + palg[count-1]);
								list_images.add(image);
							} 
							image = (JIPImgBitmap)JIPImage.newImage(numRow,numCol,ImageType.FLOAT);
							
							max = maxmin.get(countImg);
							min = maxmin.get(countImg+1);
							
							if (count == 3)
								st.nextToken();
							countImg = countImg + 2;
							count++;
							column = row = 0;
						}
						break;						
					case StreamTokenizer.TT_NUMBER:
						//modifies x for x' (standardized in [0..1])
						if (st.nval != 0.0)
							xPrime = (st.nval-min)/(max-min);
						else
							xPrime = 0.0;
						if (column < (numCol-1)) {
							image.setPixel(row,column,xPrime);
							column++;
						}
						else {
							column = 0;
							row++;
						}
						break;
				}
			}
			image.setName(type + " " + palg[3] + " divergence");
			list_images.add(image);
			fr.close();
		} catch (Exception e)  {
			throw new JIPException("GroupImages: a Input/Output exception occurred");
		}
	}
}

