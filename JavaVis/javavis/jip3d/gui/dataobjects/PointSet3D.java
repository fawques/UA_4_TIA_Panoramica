package javavis.jip3d.gui.dataobjects;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyKDTree;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Point3D;

import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * PointSet3D Class
 * @author Diego Viejo
 */
public class PointSet3D extends ScreenData {
	private static final long serialVersionUID = -2897087012180220064L;
	
	private boolean imageCoords=false;
	private int colorType=0;

	public PointSet3D(ScreenOptions opt) {
		super(opt);
		opt.global_color = false;
		opt.improved = false;
		this.setType(ScreenOptions.tPOINTSET3D);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet=new TransformGroup();
		int size, count;
		Object []elements = data.elements();
		Point3f []points;
		size = elements.length;
		points = new Point3f[size];
		PointArray geometry;

		//when global color is set, all the points share the same color. Extra data structure
		//for color is  not needed. 
		if(scr_opt.global_color)
			geometry = new PointArray(size,PointArray.COORDINATES);
		else
			geometry = new PointArray(size,PointArray.COORDINATES|PointArray.COLOR_3);

		for(count=0;count<size;count++)
			points[count] = new Point3f(((Point3D)elements[count]).getCoordsf());

		geometry.setCoordinates(0, points);
		//if no global color is set, we have to give color information for each point in the set
		if(!scr_opt.global_color)
			for(count=0;count<size;count++)
				geometry.setColor(count, ((Point3D)elements[count]).color);
		Shape3D shape = new Shape3D(geometry,object_app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		tgRet.addChild(shape);

		return tgRet;
	}
	
	public void addPoint (Point3D p) {
		data.insert(p.getCoords(), p);
	}
	
	public int getNumElements() {
		return data.size();
	}

	public int readData (String file_name, String iPath, Document doc) {
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
		aux=doc.getElementsByTagName("timestamp");
		if (aux.getLength()==0)
			timeStamp = 0;
		else {
			timeStamp = Integer.parseInt(doc.getElementsByTagName("timestamp").item(0).getTextContent());
			isTimeStamp=true;
		}
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

		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" \\n+| +|\\n+");

		name = file_name;
		path = name;
		
		for(int count=0;count<nElements;count++) {
			index = count*(3+colorType+(imageCoords?2:0))+1;
			point = new Point3D(dataRaw, index, colorType, imageCoords, yInverted, zInverted);
			if (point.isValid)
				data.insert(point.getCoords(), point);
		}
		scr_opt.num_points = data.size();

		return data.size();
	}
	
	public void writeData(String name, String path) {
		try {
			FileWriter fw = new FileWriter(path+name);
			BufferedWriter bw = new BufferedWriter(fw);
			String header="<javavis3D>\n";
			String tail="</data>\n</javavis3D>";
			
			header += "<type>PointSet3D</type>\n"+
					  "<imagecoords>"+imageCoords+"</imagecoords>\n"+
					  "<colortype>"+(colorType>0?3:0)+"</colortype>\n"+
					  "<nelements>"+data.num_elements+"</nelements>\n<data>\n";
			
			bw.write(header);
			String aux;
			Object []elements = data.elements();
			if (colorType>0) {
				for (int i=0; i<data.num_elements; i++) {
					aux=((Point3D)elements[i]).toStringRGB();
					bw.write(aux);
					bw.newLine();
					bw.flush();
				}
			}
			else {
				for (int i=0; i<data.num_elements; i++) {
					bw.write(((Point3D)elements[i]).toString());
					bw.newLine();
					bw.flush();
				}
			}
			bw.write(tail);
			bw.flush();
			
			fw.close();
		} catch (IOException e) {
			System.out.println("Problems writing file: "+name+e);
		}
	}

	@Override
	public void applyTransform(MyTransform trans) {
		int size, count;
		Object []elements;
		Point3D element;
		elements = elements();
		size = elements.length;
		data = new MyKDTree(3);

		for(count=0;count<size;count++)
		{
			element = (Point3D)elements[count];
			element.applyTransform(trans);
			data.insert(element.getCoords(), element);
		}
	}

	@Override
	public void applyMirror(int plane) {
		int size, count;
		Object []elements;
		Point3D element;
		elements = elements();
		size = elements.length;
		data = new MyKDTree(3);

		for(count=0;count<size;count++)
		{
			element = (Point3D)elements[count];
			element.setValue(plane, -element.getValue(plane));
			data.insert(element.getCoords(), element);
		}
	}

}
