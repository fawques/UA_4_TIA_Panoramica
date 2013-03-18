package javavis.jip2d.functions;

import java.io.*;
import java.util.ArrayList;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamString;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;

/**
 * Learning process in the License Template application. It creates a file with
 * the histograms of the input image.<br />
 * It applies to bitmap images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>fileName</em>: The file name which contains the data.</li>
 * <li><em>string</em>: String value which contains the characters in the image.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image and a file with the histogram data of the input image.</li>
 * </ul><br />
 */
public class LearnLT extends Function2D{
	private static final long serialVersionUID = -6920601139042990149L;

	public LearnLT(){
		super();
		name = "LearnLT";
		description = "Learning process in the License Template application. Applies to bitmap image.";
		groupFunc = FunctionGroup.Applic;

		ParamFile p1 = new ParamFile("fileName", true, true);
		p1.setDescription("File name");
		
		ParamString p2 = new ParamString("string", false, true);
		p2.setDescription("String containing the characters in the image");
		p2.setDefault("ABCDEGHJLMNSXYZ0123456789");
		
		addParam(p1);
		addParam(p2);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		if (!(img instanceof JIPBmpColor)) 
			throw new JIPException("LearnLT can not be applied to this image format.");
		
		String filename = getParamValueString("fileName");
		String str = getParamValueString("string");
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
		
		try {
			RandomAccessFile outFile = new RandomAccessFile(filename, "rw");	
			ArrayList<int[]> histograms = (ArrayList<int[]>)func.getParamValueObj("histo"); 
			int[]aux;
			
			if (histograms.size() != str.length())
				throw new JIPException(histograms.size() + " " + str.length()); 
				//throw new JIPException("LearnLT: input string and numbers in the image must be the same.");
			
			outFile.writeInt(histograms.size());
			
			for (int b=0; b < histograms.size(); b++) { 							
				outFile.writeChar(str.charAt(b));
				aux = histograms.get(b);			
				outFile.writeInt(aux.length);
				for (int i=0; i < aux.length; i++) {
					outFile.writeInt(aux[i]);		
				}
			}
			
			outFile.close();
		} 
		catch (IOException e) {System.out.println(e);}
		
		return img;
	}
}
		
	