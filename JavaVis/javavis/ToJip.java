package javavis;

import java.awt.Image;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;


/**
 * It converts a GIF or JPEG image in a JIP format.
 * This class is called from the command line. 
 * Use:  java ToJip <gif_or_jpeg_file>;
 */
public class ToJip {

	/**
	 * Method which contains the main of the class (To run from command line).
	 * @param Imagen GIF or JPG image
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
		Image awtimg = JIPToolkit.getAWTImage(args[0]);
		if (awtimg == null)
			error("Image GIF/JPEG not found: " + args[0]);

		// Gets the image name
		String name = null;
		if (args[0].lastIndexOf(".") == -1)
			name = args[0];
		else
			name = args[0].substring(0, args[0].lastIndexOf("."));

		// Converts the image into JIPformat
		try {
			JIPImage img = JIPToolkit.getColorImage(awtimg);
			img.setName(name);
			Sequence seq = new Sequence(img);
			seq.setName(name);
	
			//Save the image
			JIPToolkit.saveSeqIntoFile(seq, name + ".jip");
		}
		catch (JIPException e){System.out.println("ToJip: "+e);} 
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
		System.out.println("ToJip: Convert a GIF/JPG image to JIP format.");
		System.out.println("Use: java ToJip <gif_or_jpeg_file>");
		System.out.println("");
		System.exit(0);
	}
}
