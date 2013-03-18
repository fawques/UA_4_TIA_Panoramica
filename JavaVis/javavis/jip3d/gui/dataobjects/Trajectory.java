package javavis.jip3d.gui.dataobjects;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public abstract class Trajectory extends ScreenData implements Serializable {
	private static final long serialVersionUID = 4110652216141190374L;

	/**
	 * @uml.property  name="files"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	public ArrayList <String>files;
	/**
	 * @uml.property  name="transforms"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip3d.geom.MyTransform"
	 */
	public ArrayList <MyTransform>transforms;

	/**
	 * @uml.property  name="tam"
	 */
	final public float size = 0.05f;

	/**
	 * @uml.property  name="path"
	 */
	public String path;

	public Trajectory(ScreenOptions opt)
	{
		super(opt);
		opt.color = new Color3f(1,0,0);
	}

	protected TransformGroup createAxis()
	{
		TransformGroup TGret = new TransformGroup();
		LineAttributes latt = new LineAttributes(1, LineAttributes.PATTERN_SOLID, true);
		Appearance app = new Appearance();
		LineArray lines = new LineArray(6, LineArray.COORDINATES|LineArray.COLOR_3);

		app.setLineAttributes(latt);

		lines.setCoordinate(0, new Point3f());
		lines.setCoordinate(1, new Point3f(size, 0, 0));
		lines.setCoordinate(2, new Point3f());
		lines.setCoordinate(3, new Point3f(0, size, 0));
		lines.setCoordinate(4, new Point3f());
		lines.setCoordinate(5, new Point3f(0, 0, size));

		lines.setColor(0, new Color3f(1, 0, 0));
		lines.setColor(1, new Color3f(1, 0, 0));
		lines.setColor(2, new Color3f(0, 1, 0));
		lines.setColor(3, new Color3f(0, 1, 0));
		lines.setColor(4, new Color3f(0, 0, 1));
		lines.setColor(5, new Color3f(0, 0, 1));

		Shape3D shape = new Shape3D(lines, app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape.setCapability(Shape3D.ALLOW_PICKABLE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		TGret.addChild(shape);


		return TGret;
	}
	
	public void writeData(String name, String path) {
		FileWriter fw=null;
		int aux;
		try {
			fw = new FileWriter(path+name);
			BufferedWriter bw = new BufferedWriter(fw);
			String header="<javavis3D>\n";
			String tail="</data>\n</javavis3D>";

			aux=files.size();
			header += "<type>"+(this instanceof Trajectory2D?"Trajectory2D":"Trajectory3D")+"</type>\n"+
					  "<nelements>"+aux+"</nelements>\n<data>\n";
			
			bw.write(header);
			for (int i=0; i<aux; i++) {
				bw.write(files.get(i)+ " " + transforms.get(i).toString()+"\n");
				bw.flush();
			}
			bw.write(tail);
			bw.flush();

			fw.close();
		} catch (IOException e) {
			System.out.println("Problems writing file: "+name+e);
		}
	}
}
