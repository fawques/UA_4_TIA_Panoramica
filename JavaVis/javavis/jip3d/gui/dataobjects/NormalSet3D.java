package javavis.jip3d.gui.dataobjects;

import java.io.FileWriter;
import java.io.IOException;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Point3D;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

/**
 * NormalSet3D Class
 * @author Miguel Cazorla
 */
public class NormalSet3D extends ScreenData {
	private static final long serialVersionUID = -7888161621635341841L;

	public NormalSet3D(ScreenOptions opt) {
		super(opt);
		opt.length = 0.1;
		opt.color = new Color3f(1,0,0);
		this.setType(ScreenOptions.tNORMALSET3D);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet=new TransformGroup();
		LineArray geometry;
		PointArray geom_points;
		Shape3D shape;
		Object []elements = data.elements();
		int tam = elements.length;
		Point3f[] points = new Point3f[2];
		Normal3D normal;
		Point3D point;
		int count;

		geometry = new LineArray(2*tam, LineArray.COORDINATES|LineArray.COLOR_3);
		geom_points = new PointArray(tam, PointArray.COORDINATES);

		for(count=0;count<tam;count++)
		{
			normal = (Normal3D)elements[count];
			point = normal.origin;
			points[0] = new Point3f(point.getCoordsf());
			points[1] = new Point3f(point.getCoordsf());
			points[1].x += normal.vector.getX() * scr_opt.length;
			points[1].y += normal.vector.getY() * scr_opt.length;
			points[1].z += normal.vector.getZ() * scr_opt.length;
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
		// This method is not going to be used anymore
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
				fw.write(((Normal3D)elements[count]).toString());
				fw.write("\n");
			}
			fw.close();

		} catch(IOException e)
		{
			System.err.println("NormalSet3D::writeData Error: can not write data to: "+path+name);
		}
	}

	@Override
	public void applyTransform(MyTransform trans) {
		int size, count;
		Object []elements;
		Normal3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Normal3D)elements[count];
			element.applyTransform(trans);
		}

	}

	@Override
	public void applyMirror(int plane) {
		int size, count;
		Object []elements;
		Normal3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Normal3D)elements[count];
			element.origin.setValue(plane, -element.origin.getValue(plane));
			element.vector.setValue(plane, -element.vector.getValue(plane));
		}
	}

}
