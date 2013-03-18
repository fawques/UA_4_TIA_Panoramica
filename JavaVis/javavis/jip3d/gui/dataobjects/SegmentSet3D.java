package javavis.jip3d.gui.dataobjects;

import java.io.FileWriter;
import java.io.IOException;


import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Segment3D;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;


public class SegmentSet3D extends ScreenData {
	private static final long serialVersionUID = 8833454068500402734L;

	public SegmentSet3D(ScreenOptions opt) {
		super(opt);
		this.setType(ScreenOptions.tSEGMENTSET3D);
	}

	@Override
	public void applyTransform(MyTransform trans) {
		int size, count;
		Object []elements;
		Segment3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Segment3D)elements[count];
			element.applyTransform(trans);
		}
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet=new TransformGroup();
		LineArray geometry;
		PointArray geom_points;
		Shape3D shape;
		Object []elements = data.elements();
		int size = elements.length;
		Point3f[] points = new Point3f[2];
		Segment3D segment;
		int count;


		geometry = new LineArray(2*size, LineArray.COORDINATES|LineArray.COLOR_3);
		geom_points = new PointArray(size, PointArray.COORDINATES);

		for(count=0;count<size;count++)
		{
			segment = (Segment3D)elements[count];
			points[0] = new Point3f(segment.begin.getCoordsf());
			points[1] = new Point3f(segment.end.getCoordsf());
			geometry.setCoordinates(count*2, points);
			geometry.setColor(count*2, scr_opt.color);
			geometry.setColor(count*2 + 1, scr_opt.color);

			geom_points.setCoordinate(count, points[0]);
		}

		shape = new Shape3D(geometry,this.object_app);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		tgRet.addChild(shape);

		shape = new Shape3D(geom_points,this.object_app);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		tgRet.addChild(shape);

		return tgRet;
	}

	@Override
	public int readData(String file_name, String iPath, Document doc) {
		return -1;
		// This method is not going to be used anymore.
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
			fw.write(num_points);
			fw.write("\n");

			for(count=0;count<num_points;count++)
			{
				fw.write(((Segment3D)elements[count]).toString());
				fw.write("\n");
			}
			fw.close();

		} catch(IOException e)
		{
			System.err.println("NormalSet3D::writeData Error: can not write data to: "+path+name);
		}
	}

	@Override
	public void applyMirror(int plane) {
		int size, count;
		Object []elements;
		Segment3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Segment3D)elements[count];
			element.begin.setValue(plane, -element.begin.getValue(plane));
			element.end.setValue(plane, -element.end.getValue(plane));
		}
	}
}
