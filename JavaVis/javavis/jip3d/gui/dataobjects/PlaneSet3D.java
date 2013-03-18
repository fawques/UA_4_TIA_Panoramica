package javavis.jip3d.gui.dataobjects;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

/**
 * PlaneSet3D Class
 * @author Miguel Cazorla
 */
public class PlaneSet3D extends ScreenData {
	private static final long serialVersionUID = 7860690628252146121L;

	/**
	 * @uml.property  name="divisions"
	 */
	public int divisions = 8;

	public PlaneSet3D(ScreenOptions opt) {
		super(opt);
		opt.color = new Color3f(0,0,1);
		this.setType(ScreenOptions.tPLANARSET3D);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet=new TransformGroup();
		Shape3D shape;
		int []strip;
		int len;
		LineStripArray limits;
		Point3f []points = new Point3f[1];
		ArrayList <Point3f>aPoints = new ArrayList<Point3f>();
		double inc_angle = 2*Math.PI/divisions;
		int count, i;
		Object []elements = data.elements();
		Plane3D plane;
		Point3D p3d, p3d1;
		strip = new int[elements.length];
		double []normal;
		double rad, a, aux;
		double []V = new double[3];
		double angle = 0;
		MyTransform t3d;
		double [][]matriz;

		for(count=0;count<elements.length;count++)
		{
			plane = (Plane3D)elements[count];
			if(plane.bounded)
			{
				len = plane.bounds.size();
				strip[count] = len + 1;
				for(i=0;i<len;i++)
				{
					p3d = plane.bounds.get(i);
					aPoints.add( new Point3f(p3d.getCoordsf()) );
				}
				p3d = plane.bounds.get(0);
				aPoints.add(new Point3f(p3d.getCoordsf()));
			}
			else
			{
				len = divisions;
				strip[count] = len + 1;
				normal = plane.vector.getCoords();
				rad = plane.radius;

				//generate bounding points
				if(normal[2]!=0)
				{
					if(normal[2]!=1)
					{
						a = -(normal[0]*normal[1]*2)/normal[2];
						V[0] = normal[1];
						V[1] = normal[0];
						V[2] = a;
					}
					else
					{
						V[0] = 1;
						V[1] = 0;
						V[2] = 0;
					}
				}
				else
				{
					V[0] = -normal[1];
					V[1] = normal[0];
					V[2] = 0;
				}
				aux = Math.sqrt(V[0]*V[0] + V[1]*V[1] + V[2]*V[2]);
				V[0] /= aux;
				V[1] /= aux;
				V[2] /= aux;
				p3d1 = new Point3D(plane.origin.getX() + V[0]*rad, plane.origin.getY() + V[1]*rad,
						plane.origin.getZ() + V[2]*rad);
				aPoints.add(new Point3f(p3d1.getCoordsf()));

				angle = inc_angle;
				p3d = new Point3D(p3d1);
				t3d = new MyTransform3D();
				matriz = plane.generalRotationMatrix(angle);
				t3d.assign(matriz);
				for(i=0;i<divisions-1;i++)
				{
					p3d.applyTransform(t3d);
					aPoints.add(new Point3f(p3d.getCoordsf()));
				}

				aPoints.add(new Point3f(p3d1.getCoordsf()));
			}

		}

		try {
			points = aPoints.toArray(points);
		} catch (Exception e) {
			System.out.println("PlaneSet3D::paint Error: Can not convert from ArrayList to Point3f[]");
			return tgRet;
		}

		len = points.length;
		limits = new LineStripArray(len,LineStripArray.COORDINATES|LineStripArray.COLOR_3, strip);
		for(count=0;count<len;count++)
		{
			limits.setCoordinate(count, points[count]);
			limits.setColor(count, scr_opt.color);
		}
		shape = new Shape3D(limits,this.object_app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape.setCapability(Shape3D.ALLOW_PICKABLE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);

		tgRet.addChild(shape);

		return tgRet;
	}

	@Override
	public int readData(String file_name, String iPath, Document doc) {
		int num_points, index;
		Plane3D plane;
		Point3D origin;
		String[] dataRaw;

		name = file_name;
		path = iPath;

		num_points = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());
		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" \\n+| +|\\n+");
		
		for(int count=0;count<num_points;count++) {
			index = 10*count+1;
			plane = new Plane3D(dataRaw, index);
			origin = plane.origin;
			data.insert(origin.getCoords(), plane);
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

		
		try {
			FileWriter fw = new FileWriter(path+name);
			BufferedWriter bw = new BufferedWriter(fw);
			String header="<javavis3D>\n";
			String tail="</data>\n</javavis3D>";
			
			header += "<type>PlaneSet3D</type>\n"+
					  "<nelements>"+data.num_elements+"</nelements>\n<data>\n";
			
			bw.write(header);
			bw.flush();
		
		
		

			for(count=0;count<num_points;count++)
			{
				fw.write(((Plane3D)elements[count]).toString());
				bw.newLine();
				bw.flush();
			}
			bw.write(tail);
			bw.flush();
			fw.close();
		} catch(IOException e) {
			System.err.println("PlaneSet3D::writeData Error: can not write data to: "+path+name);
		}

	}

	@Override
	public void applyTransform(MyTransform trans) {
		int len, count;
		Object []elements;
		Plane3D element;
		elements = elements();
		len = elements.length;

		for(count=0;count<len;count++)
		{
			element = (Plane3D)elements[count];
			element.applyTransform(trans);
		}

	}

	@Override
	public void applyMirror(int plane) {
		int size, count;
		Object []elements;
		Plane3D element;
		elements = elements();
		size = elements.length;

		for(count=0;count<size;count++)
		{
			element = (Plane3D)elements[count];
			element.origin.setValue(plane, -element.origin.getValue(plane));
			element.vector.setValue(plane, -element.vector.getValue(plane));
			if(element.bounded)
			{
				for(Point3D p: element.bounds)
				{
					p.setValue(plane, -p.getValue(plane));
				}
			}
		}
		
	}

}
