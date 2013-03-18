package javavis.jip3d.gui.dataobjects;

import java.util.ArrayList;

import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform2D;
import javavis.jip3d.geom.Point3D;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

/**
 * A trajectory2D represents a set of transformations in 2D space. Normally with a translation in X, Z axis
 * and a rotation around Y axle.
 * @author Diego Viejo
 */
public class Trajectory2D extends Trajectory {
	private static final long serialVersionUID = -7665129261248658695L;
	/**
	 * @uml.property  name="height"
	 */
	double height;

	public Trajectory2D(ScreenOptions opt, double h)
	{
		super(opt);
		files = new ArrayList<String>();
		transforms = new ArrayList<MyTransform>();
		height = h;
		this.setType(ScreenOptions.tTRAJ2D);
		scr_opt.alpha = 0.5f;
	}

	protected TransformGroup paint() {

		TransformGroup tgRet = new TransformGroup();
		TransformGroup tgLines = new TransformGroup();
		LineStripArray lines;
		int num_transforms = transforms.size();
		Point3f point;
		int []strips = {num_transforms};
		TransformGroup []camera = new TransformGroup[num_transforms];
		Transform3D transform;
		Point3D reference;
		int count;

		MyTransform2D tr_global = new MyTransform2D();
		MyTransform tr_current;

		//Trajectory 2D transparency
		TransparencyAttributes trans_att = new TransparencyAttributes(TransparencyAttributes.BLENDED, (float) scr_opt.alpha);
		object_app.setTransparencyAttributes(trans_att);

		lines = new LineStripArray(num_transforms, LineStripArray.COORDINATES, strips);

		for(count=0;count<num_transforms;count++)
		{
			reference = new Point3D();
			tr_current = transforms.get(count);
			tr_global.applyTransform(tr_current); 

			transform = new Transform3D(tr_global.getMatrix4d());
			camera[count] = new TransformGroup(transform);
			camera[count].addChild(createAxis());
			tgRet.addChild(camera[count]);

			reference.applyTransform(tr_global);
			point = new Point3f(reference.getCoordsf());
			lines.setCoordinate(count, point);
		}

		Shape3D forma = new Shape3D(lines, object_app);
		forma.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		forma.setCapability(Shape3D.ALLOW_PICKABLE_READ);
		forma.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		forma.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		forma.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		tgLines.addChild(forma);
		tgRet.addChild(tgLines);

		return tgRet;
	}

	@Override
	public int readData(String file_name, String iPath, Document doc) {
		int num_poses;
		String[] dataRaw;
		int count=1;

		name = file_name;
		path = iPath;

		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" \\n+| +|\\n+");
		num_poses = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());
		
		for(int i=0; i<num_poses; i++) {
			files.add(dataRaw[count++]);
			transforms.add(new MyTransform2D(dataRaw, count));
			count += 3;
		}
		scr_opt.num_points = files.size();

		return 0;
	}


	@Override
	public void applyTransform(MyTransform trans) {
		this.transforms.get(0).applyTransform(trans);
	}


	@Override
	public void applyMirror(int plane) {
		// Nothing to do mirroring trajectories. Not applicable.
	}

}
