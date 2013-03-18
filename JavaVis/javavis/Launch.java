package javavis;

import java.awt.Image;
import java.awt.MediaTracker;

import javax.swing.ImageIcon;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.base.ParamType;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.Sequence;

/**
 * Class to execute function from command line.
 * Internally, when the launch object is executed, the parameters are checked and assigned to the function. 
 * Then the input sequence is loaded and output filename is obtained (by default out.jip). In case of any 
 * required value is not set, an error is shown. All function have -help option, that show help text 
 * in standard output.
 */
public class Launch {
	/**
	 * Contains output filename. This variable gets value after invoke parseArgs() method
	 * @uml.property  name="fileDest"
	 */
	private String fileDest;

	/**
	 * Contains input sequence to process. This variable gets value after invoke successfully 
	 * parseArgs() method.
	 * @uml.property  name="source"
	 * @uml.associationEnd  
	 */
	private Sequence source;
	
	/**
	 * Contains the function object
	 * @uml.property  name="function"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Function2D function;
	
	/** Constructor. */
	public Launch(String nameFunction) {
		try {
			function = (Function2D)Class.forName("javavis.jip2d.functions."+nameFunction.trim()).newInstance();
		}
		catch (ClassNotFoundException e) {
			System.err.println("Function: "+nameFunction+" not found.\n");
			helpUsage();
			System.exit(-1);
		}
		catch (NoClassDefFoundError e) {
			System.err.println("Function: "+nameFunction+" not found.\n");
			helpUsage();
			System.exit(-1);
		}
		catch (Exception ec) {
			System.err.println("Exception "+ec);
			System.exit(-1);
		}
	}

	/**
	* Invokes the execution of the function with arguments in a vector.
	* These arguments could be:
	* -help -> Shows descriptions of the function and parameters.
	* -name_parameter -> Assign value to a parameter of the function assigned to the command. 
	* This value will be the next argument (it will check that the typematch and that the 
	* parameter exists).
	* name_file -> First occurrence is obligatory and will refer to the input sequence filename 
	* to process (it will check that file exists and his type was JIP).  
	* The second occurrence is optional and will specify an output filename for the sequence
	* (by default out.jip).
	* In case of any error, execution will show an error and the help lines of the command.
	* 
	* @param args Arguments vector obtained from command line of the application.
	*/

	public void execute(String[] args) {
		source = null;
		fileDest = null;
		if (function != null && parseArgs(args)) {
			// Apply function and stores output
			Sequence dest = null;
			try {
				dest = function.processSeq(source);
			}
			catch (JIPException e) {
				System.out.println("Error="+e.getMessage());
				System.exit(-1);
			}
			if (function.isInfo()) System.out.println("Information = "+function.getInfo());		
			JIPToolkit.saveSeqIntoFile(dest, fileDest);
		}
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
	* This help is made from here names and descriptions on the associated 
	* function and its parameters.   
	*/
	public void help() {
		String[] params = function.getInputParamNames();
		int i;
		String aux1;

		System.out.println("");
		System.out.println("FUNCTION:");
		System.out.println("  " + function.getDescription());
		System.out.println("");
		System.out.println("Instructions for use: java "
				+ function.getName().substring(1)
				+ " <parameters> <infile> [<outfile>]");
		System.out.println("  <infile>:  Source file to process [REQUIRED]");
		System.out.println("  <outfile>: Destination file [Default: out.jip]");
		System.out.println("");
		System.out.println("Parameters:");
		System.out.println("  -help");
		System.out.println("    Shows method of use");
		if (params != null) {
			for (i=0; i<params.length; i++) {
				aux1 = "  -" + params[i] + " ";

				try {
					switch (function.getParamType(params[i])) {
						case BOOL :
							aux1 = aux1 + "<boolean>";
							break;
						case INT :
							aux1 = aux1 + "<integer>";
							break;
						case FLOAT :
							aux1 = aux1 + "<real>";
							break;
						case STRING :
						case LIST :
						case FILE :
							aux1 = aux1 + "<string>";
							break;
						case IMAGE :
							aux1 = aux1 + "<image>";
							break;
					}
				
					if (function.isInputParamRequired(params[i]))
						aux1 = aux1 + " [REQUIRED]";
					else {
						aux1 = aux1 + " [Default: ";
						switch (function.getParamType(params[i])) {
							case BOOL :
								aux1 = aux1 + function.getParamValueBool(params[i]);
								break;
							case INT :
								aux1 = aux1 + function.getParamValueInt(params[i]);
								break;
							case FLOAT :
								aux1 = aux1 + function.getParamValueFloat(params[i]);
								break;
							case STRING :
								aux1 = aux1 + function.getParamValueString(params[i]);
								break;
							case IMAGE :
								aux1 = aux1 + function.getParamValueImg(params[i]).getName();
								break;
						}
						aux1 = aux1 + "]";
					}
					System.out.println(aux1);
					System.out.println("    " + function.getParamDescr(params[i]));
				}
				catch (JIPException e) {System.out.println("Launch: "+e);}
					
			}
		}
		System.out.println("");
	}
	

	/**
	* It shows in the standard error output the usage help.
	*/
	public static void helpUsage() {
		System.out.println("Usage: java Launch nameFunction parameters");
		System.out.println("Where nameFunction is the name of the function to use");
		System.out.println("Use");
		System.out.println("    java javavis.Launch nameFunction -help");
		System.out.println(" in order to know function parameters\n");
		System.out.println(" Example: java javavis.Launch Canny -sigma 1.5 lenna.jip out.jip");
	}

	/**
	* This method checks the arguments then it prepares the function to execute it. 
	* It checks if parameters are in the function function, if value have a correct type then
	* it is assigned and it loads input sequence with function variable call source.
	* If the parameter is a pIMAGE type, you have to assign the first numFrame of file as
	* parameter value and check if is not null and it has JIP format.
	* It assigns the name of the output file to fileDest variable (default out.jip, if you do not specify the arguments)
	* and check that every parameters have a value. if something is incorrect, this method shows
	* an error and return FALSE value. if we are used execute() method, we do not use this method before
	* because execute() do it. This method is used when we want to control the function run and we do not use execute().
	* For example, if we desire to run the command in a output sequence numFrame to numFrame, because we want to access to
	* some numFrame processing results. We can use this method firstly to check the arguments and load the input sequence
	* in the source variable, before the method function.processImg(numFrame) will process the frames in a cycle, then it
	* obtains the results and shows it, and it does the output sequence to store it in the end of fileDest file.
	* @param args Vector of arguments from the command line.
	* @return TRUE if everything has been correctly.
	*/
	public boolean parseArgs(String[] args) {
		ParamType pType;
		String fileSrc = null;
		String pName = null;
		String pVal = null;
		
		for (int i=1; i<args.length; i++) {
			if (args[i].trim().startsWith("-")) {
				if (args[i].equals("-help")) {
					help();
					return false;
				}
				pName = args[i].trim().substring(1);
				if (!function.isInputParam(pName)) {
					error("Parameter -" + pName + " unknown");
					return false;
				}
				if (i+1 == args.length) {
					error("Parameter -" + pName + " does not have value");
					return false;
				}
				pVal = args[i+1].trim();
				try {
					pType = function.getParamType(pName);
					switch (pType) {
						case BOOL :
							try {
								function.setParamValue(pName, Boolean.parseBoolean(pVal));
							}
							catch (NumberFormatException e) {
								error("Parameter -" + pName + " expects a BOOLEAN value");
								return false;
							}
							break;
						case INT :
							try {
								function.setParamValue(pName,Integer.parseInt(pVal));
							}
							catch (NumberFormatException e) {
								error("Parameter -" + pName + " expects an INTEGER value");
								return false;
							}
							break;
						case FLOAT :
							try {
								function.setParamValue(pName, Float.parseFloat(pVal));
							}
							catch (NumberFormatException e) {
								error("Parameter -" + pName + " expects a FLOAT value");
								return false;
							}
							break;
						case STRING :
						case FILE :
							function.setParamValue(pName, pVal);
							break;
						case IMAGE :
							Sequence auxSeq = JIPToolkit.getSeqFromFile(pVal);
							if (auxSeq != null)
								function.setParamValue(pName, auxSeq.getFrame(0));
							else {
								error("Parameter -" + pName + " expects a JIP file");
								return false;
							}
							break;
					}
					i++;
				}catch (JIPException e) {System.out.println("Launch: "+e);}
			} 
			else 
				if (fileSrc == null) fileSrc = args[i].trim();
				else 
					if (fileDest == null) fileDest = args[i].trim();
					else {
						error("Value " + args[i] + " does not have an associated parameter");
						return false;
					}
		}
		if (fileSrc == null) {
			error("Source file is required to process it.");
			return false;
		}
		if (!(new java.io.File(fileSrc)).isFile()) {
			error("File '" + fileSrc + "' not found");
			return false;
		}
		
		if (fileSrc.endsWith(".jip")) 
			source = JIPToolkit.getSeqFromFile(fileSrc);
		else {
			ImageIcon aux = new ImageIcon(fileSrc);
			if (aux.getImageLoadStatus() == MediaTracker.ERRORED)
				return false;
			Image image = aux.getImage();
			try {
				source = new Sequence(JIPToolkit.getColorImage(image));
			}
			catch (JIPException e) {System.out.println(e);}
		}
		
		if (source == null) {
			error("File '" + fileSrc + "' does not have a JIP format");
			return false;
		}
		if (fileDest == null)
			fileDest = "out.jip";
		if (!function.paramsOK()) {
			error("Some required parameter are not especified");
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Launch.helpUsage();
			System.exit(-1);
		}
		Launch lau = new Launch(args[0]);
		lau.execute(args);
		System.exit(0);
	}
}
