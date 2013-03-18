package javavis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/**
 * This executable class allows to convert any 3D data in our format, just adding the header and tail of the XML file.
 * To do that, XML arguments can be passed. For example, if the argument type=data is passed, the label <type>data</type> 
 * is added to the output file.
 */
public class Convert3DData {
	/**
	 * Directory where to apply the cleaning
	 */
	private String dir;
	
	/**
	 * File extension of the input files
	 */
	private String ext;

	
	/** Constructor. */
	public Convert3DData(String idir, String iext) {
		dir = idir;
		ext = iext;
	}

	/**
	 * Shows an error and help lines in standard error output.
	 * @param str Error to show.
	 */
	public void error(String str) {
		System.err.println("*** ERROR: " + str + " ***");
		help();
	}

	/**
	* It shows in the standard error output the function help lines.
	*/
	static public void help() {
		System.out.println("Usage:");
		System.out.println("    java Convert3DData dir ext avoidLines timeStamp label1=text1 label2=text2 ...  ");
		System.out.println("  where:\n" +
				"dir is the directory to process;\n" +
				"ext is the file extension to process\n" +
				"avoidLines indicates how many lines are deleted \n" +
				"timeStamp indicates =0 (no timestamp present) or >0 in which line is present\n" +
				"or not, and labeln=textn are pairs of XML label and text..");
	}
	
	/**
	 * Execute the cleaning
	 */
	public void execute (String[] args) {
		File f = new File(dir);
		String []files = f.list();
		FileReader fr;
		BufferedReader br;
		FileWriter fw;
		BufferedWriter bw;
		String header="<javavis3D>\n";
		String tail="</data>\n</javavis3D>";
		String[] aux;
		String line, auxFile, auxString;
		int numElements;
		int removeLines=Integer.parseInt(args[2]);
		int timeStamp=Integer.parseInt(args[3]);
		int nTimeStamp=0;
		
		for (int i=4; i<args.length; i++) {
			aux = args[i].split("=");
			header += "\t<"+aux[0]+">"+aux[1]+"</"+aux[0]+">\n";
		}

		for (String fi : files)
			if (fi.matches("\\w*."+ext)) {
				System.out.println("Processing "+fi);
				numElements = 0;
				auxString = header;

				try {
					fr = new FileReader(dir+"//"+fi);
					br = new BufferedReader(fr);
					
					while ((line=br.readLine())!=null) {
						numElements++;
						if (timeStamp>0 && timeStamp==numElements) 
							nTimeStamp = Integer.parseInt(line);
					}
					numElements -= removeLines;
					if (timeStamp>0)
						auxString += "\t<timestamp>"+nTimeStamp+"</timestamp>\n";
					auxString += "\t<nelements>"+numElements+"</nelements>\n<data>\n";
					fr.close();
					fr = new FileReader(dir+"//"+fi);
					br = new BufferedReader(fr);
					
					auxFile = fi.substring(0, fi.lastIndexOf("."))+".xml";
					fw = new FileWriter(dir+"//"+auxFile);
					bw = new BufferedWriter(fw);
					
					bw.write(auxString);
					for (int i=0; i<removeLines; i++)
						line=br.readLine();
					while ((line=br.readLine())!=null) {
						bw.write(line);
						bw.newLine();
						bw.flush();
					}
					bw.write(tail);
					bw.flush();
					
					fw.close();
					fr.close();
				} 
				catch (IOException e) {
					System.out.println("Convert3DData::readData Error: can not read data from "+dir+"//"+fi);
					return;
				}
			}
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			Convert3DData.help();
			System.exit(-1);
		}
		Convert3DData c3dp = new Convert3DData(args[0], args[1]);
		c3dp.execute(args);
		System.exit(0);
	}
}
