package javavis.jip2d.functions;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.io.IOException;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamString;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.functions.Equalize;
import javavis.jip2d.functions.RGBToColor;
import javavis.jip2d.functions.SegmentHSB;
import javavis.jip2d.functions.Closure;

/**
 * It recognizes license plates.<br />
 * It applies to BYTE, BIT, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE, BIT, SHORT or FLOAT type.</li>
 * <li><em>file</em>: File with the histogram DB.</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>lt</em>: String value which indicates the result of the recognizing process.</li>
 * </ul><br />
 * <strong>Image result:</strong>
 * <ul>
 * <li>A processed image.</li>
 * </ul><br />
 */
public class RecogLT extends Function2D {
	private static final long serialVersionUID = -7262973524107183332L;

	public RecogLT() {
		super();
		name = "RecogLT";
		description = "Recognizes license plates";
		groupFunc = FunctionGroup.Applic;

		ParamFile p1 = new ParamFile("file", false, true);
		p1.setDescription("File with the histogram DB");
		
		addParam(p1);
		
		//Output parameter
		ParamString p2 = new ParamString("lt", false, false);
		p2.setDescription("Result of the recognizing process");

		addParam(p2);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (!(img instanceof JIPBmpColor)) 
			throw new JIPException("Function RecogLT can not be applied to this image format.");

		String fileName = getParamValueString("file");
		int numHistos, histoLength;
		char character;
		ArrayList<Character> listChar = new ArrayList<Character>();
		ArrayList<int[]> histoOrig = new ArrayList<int[]>();
		int[] histo;

		// Read the histograms stored in a file (from LearnLT)
		try{
			RandomAccessFile inFile = new RandomAccessFile(fileName, "rw");
			numHistos = inFile.readInt();
			for (int h=0; h < numHistos; h++) {
				character = inFile.readChar();
				listChar.add(character);
				histoLength = inFile.readInt();
				histo = new int[histoLength];
				for (int i=0; i < histoLength; i++) {
					histo[i] = inFile.readInt();					
				}
				histoOrig.add(histo);
			}
			inFile.close();
		} catch(IOException e){System.out.println(e);}

		JIPImage res;
		Function2D func;

		func = new Equalize();
		res = func.processImg(img);

		func = new RGBToColor();
		func.setParamValue("format", "HSB");
		img = func.processImg(res);

		func = new SegmentHSB();
		func.setParamValue("h", 0.45f);
		func.setParamValue("herror", 0.5f);
		func.setParamValue("s", 0.4f);
		func.setParamValue("serror", 0.4f);
		func.setParamValue("b", 0.15f);
		func.setParamValue("berror", 0.28f);
		res = func.processImg(img);

		func = new Closure();
		func.setParamValue("se", "Images//se.txt");
		img = func.processImg(res);

		func = new DivideBlobs();
		res = func.processImg(img);

		func = new HistoCirc();
		img = func.processImg(res);
		
		ArrayList<int[]> histoList = (ArrayList<int[]>)func.getParamValueObj("histo");
		
		// Now, a comparison between every histogram of the current image and the histograms
		// from the file are done. 
		String result = "";
		for (int[] h1 : histoList) {
			double best = Double.MAX_VALUE, dist;
			int bestInd = -1;
			for (int i=0; i < histoOrig.size(); i++) {
				dist = compareHistos(h1, histoOrig.get(i));
				if (best > dist) {
					best = dist;
					bestInd = i;
				}
			}
			result += listChar.get(bestInd).toString();
		}
		System.out.println(result);
		setParamValue("lt", result);

		return img;
	}
	
	
	/**
	 * Method which compares the Euclidean distance between two histograms.
	 * @param h1 The first histogram.
	 * @param h2 The second histogram.
	 * @return The Euclidean distance.
	 */
	private double compareHistos (int[] h1, int[] h2){
		double sum = 0;
		for (int i=0; i < h1.length; i++)
			sum += Math.pow(h1[i]-h2[i],2);		
		
		return (Math.sqrt(sum));
	}
}

