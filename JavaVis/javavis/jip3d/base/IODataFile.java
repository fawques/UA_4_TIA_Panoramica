package javavis.jip3d.base;

import java.io.File;
import java.io.IOException;

import javavis.base.JIPException;
import javavis.jip3d.gui.dataobjects.FeatureSet2D;
import javavis.jip3d.gui.dataobjects.ImageSet3D;
import javavis.jip3d.gui.dataobjects.NeuronSet3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javavis.jip3d.gui.dataobjects.SR4000Set3D;
import javavis.jip3d.gui.dataobjects.Spline3D;
import javavis.jip3d.gui.dataobjects.Trajectory2D;
import javavis.jip3d.gui.dataobjects.Trajectory3D;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class IODataFile
 */
public class IODataFile {
	private String name;
	private String path;
	
	public IODataFile (String iname, String ipath) {
		name=iname;
		path=ipath;
	}
	
	public void write (ScreenData sd) throws JIPException {
		sd.writeData(name, path);
	}
	
	public ScreenData read () throws JIPException {
		Document doc=null;
		ScreenData sd=null;
		String type;
		
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path+name));
			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			throw new JIPException ("Problems reading file "+name);
		} catch (IOException e) {
			throw new JIPException ("Problems reading file "+name);
		} catch (SAXException e) {
			throw new JIPException ("Problems reading file "+name);
		}
		try {
			type = doc.getElementsByTagName("type").item(0).getTextContent();
			
			if (type.equals("PointSet3D")) {
				sd = new PointSet3D(new ScreenOptions());
			}
			else if (type.equals("NeuronSet3D")) {
				sd = new NeuronSet3D(new ScreenOptions());
			}
			else if (type.equals("ImageSet3D")) {
				sd = new ImageSet3D(new ScreenOptions());
			}
			else if (type.equals("Spline3D")) {
				sd = new Spline3D(new ScreenOptions());
			}
			else if (type.equals("Trajectory3D")) {
				sd = new Trajectory3D(new ScreenOptions());
			}
			else if (type.equals("Trajectory2D")) {
				sd = new Trajectory2D(new ScreenOptions(), 1.0);
			}
			else if (type.equals("SR4000Set3D")) {
				sd = new SR4000Set3D(new ScreenOptions());
			}
			else if (type.equals("PlaneSet3D")) {
				sd = new PlaneSet3D(new ScreenOptions());
			}
			else if (type.equals("FeatureSet2D")) {
				sd = new FeatureSet2D(new ScreenOptions());
				name = path;
			}
			sd.readData(name, path, doc);
		} catch (NullPointerException e) {
			throw new JIPException("Problems reading file "+name);
		}
		
		return sd;
	}

}
