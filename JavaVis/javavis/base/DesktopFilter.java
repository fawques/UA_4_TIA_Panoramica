package javavis.base;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author Miguel Cazorla
 * @version 0.1
 * @date 5-2006
 */
public class DesktopFilter extends FileFilter {
	/**
	 * @uml.property  name="extensionAccept"
	 */
	private String extensionAccept="jdf";
	/**
	 * @uml.property  name="extensionName"
	 */
	private String extensionName="XML JavaVis Desktop Descriptor (.jdf)";
	/**
	 * @uml.property  name="type"
	 */
	private int type;
	
	
	public DesktopFilter(int t) {
		super();
		type=t;
	}
	
	public boolean accept(File arg0) {
		switch (type) {
			case 0: String extension = DesktopFilter.getExtension(arg0);
					return extensionAccept.equals(extension);
			case 1: if (arg0.isDirectory())
					return true;
			default: return true;
		}
	}

	public String getDescription() {
		return extensionName;
	}
	
    public static String getExtension(File arg0) {
        String extension = null;
        String fileName = arg0.getName();
       
        int i = fileName.lastIndexOf('.');

        if (i > 0 && i < fileName.length() - 1)
        	extension = fileName.substring(i + 1).toLowerCase();
        return extension;
    }

}
