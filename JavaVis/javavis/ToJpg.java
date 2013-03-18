package javavis;

import javavis.base.JIPToolkit;
import javavis.jip2d.base.Sequence;


/**
 * It converts a JIP image into a JPEG image.
 * This class is called from the command line. 
 * Use:  java ToJpg <jip file>;
 */
public class ToJpg {

	/**
	 * Method which contains the main of the class (To run from command line).
	 * @param Imagen JIP image
	 */
	public static void main(String[] args) {
		// Checks the arguments
		if (args.length != 1)
			error("Incorrect arguments.");
		if (args[0].equals("-help"))
			help();
		if (!(new java.io.File(args[0])).isFile())
			error("File '" + args[0] + "' does not exist");

		// Loads the source image
		Sequence sequence = JIPToolkit.getSeqFromFile(args[0]);
		if (sequence == null)
			error("Image JIP not found: " + args[0]);

		// Gets the image name
		String name = null;
		if (args[0].lastIndexOf(".") == -1)
			name = args[0];
		else
			name = args[0].substring(0, args[0].lastIndexOf("."));

		// Converts the image into JPG format
		JIPToolkit.saveImgIntoFileJpg(sequence, 0, "", name + ".jpg");
		System.exit(0);

	}

	/**
	 * It shows an error message in the error output.
	 * @param str String which has the error message
	 */
	static void error(String str) {
		System.err.println("*** ERROR: " + str + " ***");
		System.err.println("");
		help();
	}

	/**
	 * It shows the command help in the screen.
	 */
	static void help() {
		System.out.println("ToJpg: Convert a JIP image into JPG format.");
		System.out.println("Use: java ToJpg <jip_file>");
		System.out.println("");
		System.exit(0);
	}
}
