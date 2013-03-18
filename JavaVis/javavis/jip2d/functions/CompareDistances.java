package javavis.jip2d.functions;

import java.io.*;
import java.awt.Image;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;

/**
 * It calculates the distances of the input directory with the image DB. Different distances 
 * are used like L1 norm, L2 norm, Kullback-Leibler divergence and Jeffrey divergence and 
 * different values like number of binds to compare the result. It is used the functions  
 * CalcHistoDB and SearchImage to help in this calculation. 
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * <li><em>dir</em>: Directory which contains the Data Base images.</li>
 * <li><em>image</em>: Directory which contains the images to prove.</li>
 * <li><em>type</em>: List which indicates the image type result (RGB, YCbCr, HSI) 
 * (default RGB).</li>
 * <li><em>createDB</em>: Boolean value which indicates if the function has to create 
 * the histogram DB (default false).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The image does not experience any change. The results are written in some files 
 * depending on the number of bins (disc) and are arranged to export to a datasheet like 
 * Excel (for example).</li>
 * </ul><br />
 */
public class CompareDistances extends Function2D{
	private static final long serialVersionUID = -4173628485139333543L;

	public CompareDistances() {
		super();
		name = "CompareDistances";
		description = "Search the most similar image with all the distances.";
		groupFunc = FunctionGroup.ImageDB;

		ParamDir p1 = new ParamDir("dir", false, true);
		p1.setDescription("Directory to process");
		
		ParamDir p2 = new ParamDir("image", false, true);
		p2.setDescription("Directory with the images to prove");
		
		ParamList p3 = new ParamList("type", false, true);
		String []paux = new String[3];
		paux[0] = "RGB";
		paux[1] = "YCbCr";
		paux[2] = "HSI";
		p3.setDefault(paux);
		p3.setDescription("Type of the image");
		
		ParamBool p4 = new ParamBool("createDB", false, true);
		p4.setDescription("Histograms DB to create?");
		p4.setDefault(false);			
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		String imageParameter = getParamValueString("image");
		String type = getParamValueString("type");
		boolean createDB = getParamValueBool("createDB");
		String dir = getParamValueString("dir");
		int disc = 5;
		String fileDB = "out" + type + disc;
		
		//creates the histograms DB if it's necessary
		if (createDB)
			while (disc <= 30) {
				//creates a file for each value of disc [5..30]
				createDB(img,fileDB,disc,dir,type);
				disc += 5;
				fileDB = "out" + type + disc;
			}
		
		//creates an array with the images to analyze
		File f = new File(imageParameter);
		String []images = f.list();
		int nImages = 0;
		int countImgs = 0;
		
		//counts the number of images to prove
		for (int i=0; i < images.length; i++)
			if (images[i].endsWith(".jpg") || images[i].endsWith(".JPG"))
				nImages++;
		
		String[] imagesNames = new String[nImages];
		
		for (int i=0; i < images.length; i++)
			if (images[i].endsWith(".jpg") || images[i].endsWith(".JPG")) {
				imagesNames[countImgs] = "" + images[i];
				countImgs++;
			}
		
		//calls a function which generates the test
		prove(imagesNames,type);
		
		return img;
	}
	
	
	/**
	 * Method which creates a file with the histograms of the images.
	 * @param imgAux Image to create the histogram.
	 * @param fileDBAux File to store the histogram.
	 * @param discAux Number of bins.
	 * @param dirAux Directory to process.
	 * @param typeAux Image type.
	 * @throws JIPException.
	 */
	private void createDB(JIPImage imgAux, String fileDBAux, int discAux, String dirAux, String typeAux) throws JIPException {
		CalcHistoDB chdb = new CalcHistoDB();
		//puts the parameters to create the histogram DB
		chdb.setParamValue("dir",dirAux);
		chdb.setParamValue("disc",discAux);
		chdb.setParamValue("fileDB",fileDBAux);
		chdb.setParamValue("type",typeAux);
		//calls the function CalcHistoDB
		chdb.processImg(imgAux);
	}
	
	/**
	 * Method which writes in the file the name of the image with it is calculating the 
	 * distances in function of the parameters.
	 * @param imgnames Array with the names of the images.
	 * @param pos The position of the current image in the array.
	 * @param pw File where writes the name of the image.
	 */
	private void nameImgFile(String[] imgNames, int pos, PrintWriter pw) {
		pw.println();
		pw.println("Image name -> " + imgNames[pos]);
	}
	
	/**
	 * Method which writes in the file the results to apply SearchImage.
	 * @param auxImg A JIPImage.
	 * @param fileDB1 File with the histogram DB.
	 * @param disc1 Number of bins.
	 * @param type The image type.
	 * @param algorithm The distance.
	 * @param pw File where write the name of the image.
	 * @throws JIPException
	 */
	private void writeFile(JIPImage auxImg, String fileDB1, int disc1, String type, String algorithm, PrintWriter pw) throws JIPException
	{
		Sequence auxImg2; 
		SearchImage si = new SearchImage();
		
		//creates Sequence
		auxImg2 = new Sequence(auxImg);
		//puts the values of the parameters of SearchImage
		si.setParamValue("imageDB",fileDB1);
		si.setParamValue("disc", disc1);
		si.setParamValue("perc", 0.1f); 
		si.setParamValue("type",type);
		si.setParamValue("algorithm",algorithm);
		si.processSeq(auxImg2);

		//writes in the file
		if (algorithm.equals("L1"))
			pw.print("" + disc1 + "     " + si.getParamValueFloat("distance"));
		else {
			if (algorithm.equals("Kullback-Leibler divergence"))
				pw.println("     " + si.getParamValueFloat("distance"));
			else
				pw.print("     " + si.getParamValueFloat("distance"));
		}
	}
	
	/**
	 * Method which puts in order data of the file to export to Excel (for example).
	 * @param fileResults File with the results.
	 * @param type Image type.
	 * @throws JIPException
	 */
	private void readFile(String fileResults, String type) throws JIPException {
		String file = fileResults;
		String fileExit;
		int bins = 5;
		String str;
		char[] character;
		int N_VAL = 4;
		
		while (bins <= 30) {
			try {
				StreamTokenizer st = new StreamTokenizer(new FileReader(file));
    			fileExit = "Resuls" + type + bins + ".txt";
    			PrintWriter pwFile = new PrintWriter (new FileWriter(fileExit));
    			pwFile.println("Number de bins -> " + bins);
				
				while (st.nextToken() != StreamTokenizer.TT_EOF) {	
					switch (st.ttype) {
						case StreamTokenizer.TT_WORD:
			            	break;
						case StreamTokenizer.TT_NUMBER:
			        		if (st.nval == bins) {
			        			for (int i=0; i < N_VAL; i++) {
			        				st.nextToken();
			        				str = "" + st.nval;
			        				character = str.toCharArray();		
			        				
			        				for (int j=0; j < character.length; j++) {
			        					if (character[j] == '.')
			        						character[j] = ',';
			        				}
			        				pwFile.print(character);
			        				pwFile.print(";");
			        			}
			        			pwFile.println();
			        		}
			        		break;
					}
				}
				pwFile.close();
				bins = bins + 5;
			} catch (IOException e)  {
				throw new JIPException("CompareDistances: an Input/Output exception ocurred while puts in order data");
			}
		}
	}
	
	/**
	 * Method which is the main function, it calls the other functions to calculate and 
	 * put in order the results of the comparison of the images.
	 * @param imgNames Array with the name of the images.
	 * @param type The image type.
	 * @throws JIPException
	 */
	private void prove(String[] imgNames, String type) throws JIPException {
		String imageParameter = getParamValueString("image"); 
		JIPImage auxImg = null;
		String fileDBTests = "DBTests"+type+".txt";
		PrintWriter pw = null;
		int disc = 5;
		String fileDB = "out" + type + disc;
		String []algorithm = new String[4];
		algorithm[0] = "L1";
		algorithm[1] = "L2";
		algorithm[2] = "Jeffrey-divergence";
		algorithm[3] = "Kullback-Leibler divergence";
		
		//open the file
		try{
			pw = new PrintWriter (new FileWriter(fileDBTests));
			pw.println(type);
		}catch (Exception e) {
			throw new JIPException("CompareDistances: an Input/Output exception ocurred while writes in a file");
		}
		
		for (int i=0; i < imgNames.length; i++) {
			Image imgAWT = JIPToolkit.getAWTImage(imageParameter + File.separator + imgNames[i]);
			if (imgAWT != null) {
				auxImg = JIPToolkit.getColorImage(imgAWT);
							
				//writes in the file the name of the current image
				nameImgFile(imgNames,i,pw);  
				
				//the value of disc [5..30]
				while (disc <= 30) {
					//writes in the file the results of apply the four distances
					for (int num = 0; num < algorithm.length; num++) 
						writeFile(auxImg,fileDB,disc,type,algorithm[num],pw);
					
					//updates values of the parameters
					disc += 5;
					fileDB = "out" + type + disc;
				}

				//updates values of the next image
				if (disc == 35) {
					disc = 5;
					fileDB = "out" + type + disc;
				}
			}
		} 
		//closes the file
		pw.close();

		//organizes the results in the file in order of the number of bins and the type of the image
		readFile(fileDBTests,type);
	}
}

