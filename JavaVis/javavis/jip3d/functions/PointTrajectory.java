package javavis.jip3d.functions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.IODataFile;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Octree;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javavis.jip3d.gui.dataobjects.Trajectory;

/**
 * It represents the points obtained from consecutive 3D images along a trajectory. It can also
 * applies a point density reduction to reduce the amount of data in the result.<br />
 * The screen data must be a trajectory 2D file or a trajectory 3D file.<br />
 */
public class PointTrajectory extends Function3D {

	public PointTrajectory()
	{
		super();

		this.allowed_input = ScreenOptions.tTRAJ2D | ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Mapping;

		ParamFloat p1 = new ParamFloat("Resolution");
		p1.setValue(0.10f);
		ParamBool p2 = new ParamBool("Bidimensional");
		p2.setValue(false);
		ParamBool p3 = new ParamBool("Separated");
		p3.setValue(true);
		ParamBool p4 = new ParamBool("Verbose");
		p4.setValue(false);
		ParamFloat p5 = new ParamFloat("Bound Size");
		p5.setValue(2000);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		Octree total_data;
		Point3D bound_sup;
		Point3D bound_inf;
		float resolution = (float)this.getParamValueFloat("Resolution");
		boolean bidim = this.getParamValueBool("Bidimensional");
		boolean separate = this.getParamValueBool("Separated");
		boolean verbose = this.getParamValueBool("Verbose");
		float bound = (float)this.getParamValueFloat("Bound Size");
		PointSet3D points;
		PointSet3D newPointSet = null;
		PointSet3D ret;
		int file_number;
		int count, count_points;
		String file_name;
		Trajectory traj;
		Object []elements;
		Point3D element;
		ArrayList<Point3D> complete_list;
		String path;
		MyTransform transform, total_transform;
		IODataFile iodf;

		bound_sup = new Point3D(bound, bound, bound);
		bound_inf = new Point3D(-bound, -bound, -bound);
		total_data = new Octree(bound_inf, bound_sup, resolution);

		traj = (Trajectory)scr_data;
		file_number = traj.files.size();
		path = traj.path;

		double prog_inc = 98.0/file_number;

		total_transform = new MyTransform3D();
		for(count=0;count<file_number;count++)
		{
			file_name = traj.files.get(count);
			transform = new MyTransform3D(traj.transforms.get(count));
//			readJip3DFile(path+file_name);
			iodf = new IODataFile(file_name, path);
			points = (PointSet3D)iodf.read();
			total_transform.applyTransform(transform);
			if(verbose) System.out.println("Pose "+count+": "+total_transform.toString()+"\n");
			
			if(separate)
			{
				newPointSet = new PointSet3D(new ScreenOptions());
				newPointSet.name = file_name;
				if(verbose) newPointSet.scr_opt.is_visible = true;
				newPointSet.isTimeStamp=points.isTimeStamp;
				newPointSet.timeStamp=points.timeStamp;
			}

			elements = points.elements();
			for(count_points=0;count_points<elements.length;count_points++)
			{
				element = (Point3D)elements[count_points];
				element.applyTransform(total_transform);
				if(bidim)
					element.setY(0.0);
				if(separate)
					newPointSet.insert(element);
				else
					total_data.insert(element);
			}
			if(separate) result_list.add(newPointSet);
			progress += prog_inc;
		}
		
		if(!separate)
		{
			total_data.cleanTree(7);
			complete_list = total_data.getAll();
			ret = new PointSet3D(new ScreenOptions());
			ret.name = "CompletePointSet";
			for(count=0;count<complete_list.size();count++)
			{
				element = complete_list.get(count);
				ret.insert(element);
			}
			result_list.add(ret);
		}
		progress = 100.0;
	}

	/**
	 * This method has the same functionality than PointSet3D readData method. But in this case, points are stored in an 
	 * ArrayList structure instead of a KD-Tree. This saves a lot of memory and allows reading large input files.
	 * @param inputFile
	 * @return
	 * @throws JIPException
	 */
	private ArrayList<Point3D> readJip3DFile(String inputFile) throws JIPException
	{
		ArrayList<Point3D> ret = new ArrayList<Point3D>();
		Document doc=null;
		String type;
		int colorType;
		boolean imageCoords;
		
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(inputFile));
//			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			throw new JIPException ("Problems reading file "+name);
		} catch (IOException e) {
			throw new JIPException ("Problems reading file "+name);
		} catch (SAXException e) {
			throw new JIPException ("Problems reading file "+name);
		}
		try {
			type = doc.getElementsByTagName("type").item(0).getTextContent();
			
			if (!type.equals("PointSet3D")) 
				throw new JIPException("PointSet3D file expected but "+type+" received");
			
			int nElements, index;
			String[] dataRaw;
			boolean yInverted=false, zInverted=false;
			Point3D point;
			NodeList aux;
			
			nElements = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());
			aux=doc.getElementsByTagName("imagecoords");
			if (aux.getLength()==0)
				imageCoords = false;
			else
				imageCoords = Boolean.parseBoolean(doc.getElementsByTagName("imagecoords").item(0).getTextContent());
			aux=doc.getElementsByTagName("yinverted");
			if (aux.getLength()==0)
				yInverted = false;
			else
				yInverted = Boolean.parseBoolean(doc.getElementsByTagName("yinverted").item(0).getTextContent());
			aux=doc.getElementsByTagName("zinverted");
			if (aux.getLength()==0)
				zInverted = false;
			else
				zInverted = Boolean.parseBoolean(doc.getElementsByTagName("zinverted").item(0).getTextContent());
			aux=doc.getElementsByTagName("colortype");
			if (aux.getLength()==0)
				colorType = 0;
			else
				colorType = Integer.parseInt(doc.getElementsByTagName("colortype").item(0).getTextContent());

//			NodeList nl = doc.getDocumentElement().getChildNodes();
//			System.out.println(nl.item(nl.getLength()-2).getNodeName()+": "+nl.item(nl.getLength()-2).getTextContent());
//			dataRaw = nl.item(nl.getLength()-2).getNodeValue().split(" \\n+| +|\\n+");
			
			dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" \\n+| +|\\n+");
			for(int count=0;count<nElements;count++) {
				index = count*(3+colorType+(imageCoords?2:0))+1;
				point = new Point3D(dataRaw, index, colorType, imageCoords, yInverted, zInverted);
				if (point.isValid)
					ret.add(point);
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new JIPException("Problems reading file "+name);
		}


		return ret;
	}
}
