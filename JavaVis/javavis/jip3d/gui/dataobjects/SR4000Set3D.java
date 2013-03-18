package javavis.jip3d.gui.dataobjects;

import java.io.FileWriter;
import java.io.IOException;

import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.PointSR4K;

import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

/**
 * SR4000Set3D Class.
 * Allows working with SR4000 camera data.
 * @author Miguel Cazorla
 */
public class SR4000Set3D extends ScreenData {
	private static final long serialVersionUID = 8539653941133443621L;
	
	private static final int nColumns=176;
	private static final int nRows=144;
	
	/**
	 * @uml.property  name="amplitude"
	 * @uml.associationEnd  
	 */
	private JIPBmpByte amplitude;

	/**
	 * @uml.property  name="confidenceMap"
	 * @uml.associationEnd  
	 */
	private JIPBmpByte confidenceMap;

	/**
	 * @uml.property  name="distances"
	 * @uml.associationEnd  
	 */
	private JIPBmpByte distances;
	/**
	 * @uml.property  name="timeStamp"
	 */
	private long timeStamp;

	public SR4000Set3D(ScreenOptions opt) {
		super(opt);
		opt.global_color = false;
		opt.improved = false;
		this.setType(ScreenOptions.tSR4000SET3D);
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

		if(scr_opt.global_color)
			geometry = new PointArray(size,PointArray.COORDINATES);
		else
			geometry = new PointArray(size,PointArray.COORDINATES|PointArray.COLOR_3);

		for(count=0;count<size;count++) {
			points[count] = new Point3f(((Point3D)elements[count]).getCoordsf());
		}

		geometry.setCoordinates(0, points);
		if(!scr_opt.global_color)
			for(count=0;count<size;count++)
				geometry.setColor(count, ((Point3D)elements[count]).color);
		Shape3D shape = new Shape3D(geometry,object_app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		tgRet.addChild(shape);

		return tgRet;
	}

	@Override
	public int readData(String file_name, String iPath, Document doc) {
		PointSR4K point;
		int numPoints, index=1;
		String[] dataRaw;

		name = file_name;
		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" +|\\n+");
		numPoints = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());
		
		//Read timestamp
		timeStamp = Integer.parseInt(doc.getElementsByTagName("timestamp").item(0).getTextContent());
		for (int count=0; count<numPoints; count++) {
			point = new PointSR4K(dataRaw, index);
			index += 7;
			data.insert(point.getCoords(), point);
		}
		scr_opt.num_points = data.size();
		return data.size();
	}

	@Override
	public void writeData(String name, String path) {
		int count;
		int num_points = data.size();
		Object []elements = data.elements();

		if(path.charAt(path.length()-1)!='/')
			path += '/';

		FileWriter fw;
		try
		{
			fw = new FileWriter(path+name);
			fw.write(num_points+" ");
			fw.write("\n");

			for(count=0;count<num_points;count++)
			{
				fw.write(((Point3D)elements[count]).toString());
				fw.write("\n");
			}
			fw.close();

		} catch(IOException e)
		{
			System.err.println("Error:" + e.getMessage());
		}

	}

	@Override
	public void applyTransform(MyTransform trans) {
		int size, count;
		Object []elements;
		Point3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Point3D)elements[count];
			element.applyTransform(trans);
			// TODO Like Neuron3D extends from Point3D, it will have to update its position in KDTree
		}
	}

	/**
	 * @return the ncolumns
	 */
	public static int getNcolumns() {
		return nColumns;
	}

	/**
	 * @return the nrows
	 */
	public static int getNrows() {
		return nRows;
	}

	@Override
	public void applyMirror(int plane) {
		int size, count;
		Object []elements;
		Point3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Point3D)elements[count];
			element.setValue(plane, -element.getValue(plane));
			// TODO Like Neuron3D extends from Point3D, it will have to update its position in KDTree
		}
	}

}
