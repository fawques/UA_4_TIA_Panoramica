package javavis.jip3d.base;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

/**
 * Class GeneralFilter
 */
public class GeneralFilter extends FileFilter {
	/**
	 * @uml.property  name="extensionAccept"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private ArrayList <String> extensionAccept;
	/**
	 * @uml.property  name="extensionName"
	 */
	private String extensionName="";
	/**
	 * @uml.property  name="type"
	 */
	private int type;


	public GeneralFilter(int t) {
		super();
		type=t;
		extensionAccept = new ArrayList<String>();
	}


	public boolean accept(File arg0) {
		boolean returnValue=false;
		int cont, tam;
		if (type == 0) {
			String extension = GeneralFilter.getExtension(arg0);
			tam = extensionAccept.size();
			for(cont=0;cont<tam&&!returnValue;cont++)
				returnValue=extensionAccept.get(cont).equals(extension);
		}
		if (arg0.isDirectory())
			returnValue=true;

		return returnValue;
	}

	public String getDescription() {
		return extensionName;
	}

	public void setDescription(String str)
	{
		extensionName = str;
	}
    public static String getExtension(File arg0) {
        String extension = null;
        String fileName = arg0.getName();

        int i = fileName.lastIndexOf('.');

        if (i > 0 && i < fileName.length() - 1)
        	extension = fileName.substring(i + 1).toLowerCase();
        return extension;
    }

    public void addExtension(String ext)
    {
    	this.extensionAccept.add(ext);
    	extensionName += ext+" ";
    }
}
