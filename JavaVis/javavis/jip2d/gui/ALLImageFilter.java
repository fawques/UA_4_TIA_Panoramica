package javavis.jip2d.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Make a filter for extension files: jpeg, jpg and gif.  	 
 */
public class ALLImageFilter extends FileFilter {
	/** Text string to jpeg */
	final static String jpeg = "jpeg";
	/** Text string to jpg*/
	final static String jpg = "jpg";
	/** Text string to gif*/
	final static String gif = "gif";
	/** Text string to png*/
	final static String png = "png";

	/**
	 * Shows if the file is accepted, that is, if it is a directory, or its extension is
	 * jpeg, jpg, png o gif.
	 * @param f File to analyse
	 * @return TRUE if file is a directory or its extension is jpeg, jpg, png or gif, 
	 * else return FALSE.	
	 */
	public boolean accept(File f) {
		if (f.isDirectory()) 
			return true;

		String s = f.getName();
		int i = s.lastIndexOf('.');

		if ((i > 0) && (i < s.length() - 1)) {
			String extension = s.substring(i + 1).toLowerCase();

			if (gif.equals(extension)
				|| png.equals(extension)
				|| jpeg.equals(extension)
				|| jpg.equals(extension))
				return true;
			else
				return false;
		}

		return false;
	}

	/**
	* Description of the filter
	* @return Returns a string with the filter description.
	*/
	public String getDescription() {
		return "Images (.jpg, .jpeg, .gif, .png)";
	}
}
