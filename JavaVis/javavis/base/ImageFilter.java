package javavis.base;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * It implements an extension filter for the dialog box to open and close files 
 */
public class ImageFilter extends FileFilter {
	/** String text for jip */
	final static String jip = "jip";

	/**
	* Filter used for .jip extensions
	* @param f File to analyse
	* @return TRUE if file is a directory or .jip. Else return FALSE.
	*/
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;

		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			String extension = s.substring(i + 1).toLowerCase();
			return jip.equals(extension);
		}
		return false;
	}

	/**
	* Description of the filter
	* @return Returns a string with the description of the filter
	*/
	public String getDescription() {
		return "JIP Sequences";
	}
}
