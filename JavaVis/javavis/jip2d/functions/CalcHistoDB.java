package javavis.jip2d.functions;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.awt.Image;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamString;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;

/**
 * It calculates the histograms of a set of images in a directory. It gets the directory name
 * and gets all the subdirectories in it. Every directory is a cluster containing more 
 * images (all of them in the same group). It uses CalcHistoColor method to get the histogram 
 * of each image.<br />
 * It applies to COLOR image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>dir</em>: Directory that contains the Data Base images. Each image has to be a COLOR type.</li>
 * <li><em>disc</em>: Integer value which indicates the discretization (number of bins) 
 * (default 20).</li>
 * <li><em>fileDB</em>: String value which indicates the name of the file where the function will save the 
 * results (default "out").</li>
 * <li><em>type</em>: List which indicates the image type to calculate the histogram 
 * (RGB, HSB, YCbCr, HSI) (default RGB).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The input image remains the same.</li>
 * <ul><br />
 */
public class CalcHistoDB extends Function2D {
	private static final long serialVersionUID = -3069473379369433977L;

	public CalcHistoDB() {
		super();
		name = "CalcHistoDB";
		description = "Calculates the histograms of images in a directory.";
		groupFunc = FunctionGroup.ImageDB;

		ParamDir p1 = new ParamDir("dir", false, true);
		p1.setDescription("Directory to process");
		
		ParamInt p2 = new ParamInt("disc", false, true);
		p2.setDefault(20);
		p2.setDescription("Discretization (number of bins)");
		
		ParamString p3 = new ParamString("fileDB", false, true);
		p3.setDefault("out");
		p3.setDescription("File to save the results");
		
		ParamList p4 = new ParamList("type", false, true);
		String []paux = new String[3];
		paux[0] = "RGB";
		paux[1] = "YCbCr";
		paux[2] = "HSI";
		p4.setDefault(paux);
		p4.setDescription("Type of the image");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		// First, open the file where it saves the histograms
		String fileDB = getParamValueString("fileDB");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(fileDB);
			oos = new ObjectOutputStream(fos);
			int disc = getParamValueInt("disc");
			String type = getParamValueString("type");
			String dir = getParamValueString("dir");
			File fileDir = new File(dir);
			
			// Get the names of files and directories in the current directory
			String []clusters = fileDir.list();
			String []images;
			JIPImage imgAux = null;
			CalcHistoColor chc = new CalcHistoColor();
			
			chc.setParamValue("disc", disc);
			chc.setParamValue("type",type);
			for (String clus : clusters) {
				String group = dir + File.separator + clus;
				File fileGroup = new File(group);
				// Only processes the directories
				if (fileGroup.isDirectory()) {
					images = fileGroup.list();
					// Processes all the images in the directory
					for (String im : images) {
						String fileImg = group+File.separator+im;
						Image imgAWT = JIPToolkit.getAWTImage(fileImg);
						
						if (imgAWT != null) 
							imgAux = JIPToolkit.getColorImage(imgAWT);
						else 
							continue;
						
						// Do not process files which are not images
						if (imgAux != null) {
							chc.processImg(imgAux);
							if (chc.isInfo()) {
								info = "CalcHistoColor info: " + chc.getInfo();
								continue;
							}
							float [][][]acumF = (float[][][])chc.getParamValueObj("histo");
							// Stores the filename and the histogram
							oos.writeUTF(fileImg);
							oos.writeObject(acumF);
						}
						else 
							info = "CalcHistoDB: some files are not images (JPEG, GIF)";
					}
				}
				else //The images are directly in the folder
				{
					//Processes all the images in the directory
					Image imgAWT = JIPToolkit.getAWTImage(group);
					
					if (imgAWT != null)
						imgAux = JIPToolkit.getColorImage(imgAWT);
					else 
						continue;
					
					// Do not process files which are not images
					if (imgAux != null) {
						chc.processImg(imgAux);
						if (chc.isInfo()) {
							info = "CalcHistoColor info: " + chc.getInfo();
							continue;
						}
						float [][][]acumF = (float[][][])chc.getParamValueObj("histo");
						// Stores the filename and the histogram
						oos.writeUTF(group);
						oos.writeObject(acumF);
					}
					else 
						info = "CalcHistoDB: some files are not images (JPEG, GIF)";
				}
			}
		}
		catch (IOException e) {
			throw new JIPException("FCalcHistDB: error opening or writing in file " + fileDB);
		}
		finally {
			try {
				if (oos != null && fos != null) {
					oos.close();
					fos.close();
				}
			}
			catch (IOException e) {System.out.println(e);} 				
		}
		
		return img;
	}
}

