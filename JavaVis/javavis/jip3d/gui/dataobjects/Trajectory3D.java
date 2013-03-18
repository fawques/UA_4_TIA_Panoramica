package javavis.jip3d.gui.dataobjects;

import java.util.ArrayList;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;

/**
 * A trajectory3D.
 * @author Diego Viejo
 */
public class Trajectory3D extends Trajectory {
	private static final long serialVersionUID = 6962474425809749448L;

	public Trajectory3D(ScreenOptions opt) {
		super(opt);
		files = new ArrayList<String>();
		transforms = new ArrayList<MyTransform>();
		this.setType(ScreenOptions.tTRAJ3D);
	}

	@Override
	public void applyTransform(MyTransform trans) {
		this.transforms.get(0).applyTransform(trans);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet = new TransformGroup();
		TransformGroup tgLines = new TransformGroup();
		LineStripArray lines;
		int num_poses = transforms.size();
		Point3f point;
		int []strips = {num_poses};
		TransformGroup []cameras = new TransformGroup[num_poses];
		Transform3D transform;
		Point3D reference;
		int count;
		Shape3D forma;

		MyTransform tr_global = new MyTransform3D();
		MyTransform tr_current;

		//Trajectory 2D transparency
		TransparencyAttributes trans_att = new TransparencyAttributes(TransparencyAttributes.BLENDED, (float) scr_opt.alpha);
		object_app.setTransparencyAttributes(trans_att);

		if(num_poses>1)
		{
			lines = new LineStripArray(num_poses, LineStripArray.COORDINATES, strips);

			for(count=0;count<num_poses;count++)
			{
				reference = new Point3D();
				tr_current = transforms.get(count);
				tr_global.applyTransform(tr_current);

				transform = new Transform3D(tr_global.getMatrix4d());
				cameras[count] = new TransformGroup(transform);
				cameras[count].addChild(createAxis());
				tgRet.addChild(cameras[count]);

				reference.applyTransform(tr_global);
				point = new Point3f(reference.getCoordsf());
				lines.setCoordinate(count, point);
			}
			forma = new Shape3D(lines, object_app);
			forma.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
			forma.setCapability(Shape3D.ALLOW_PICKABLE_READ);
			forma.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
			forma.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
			forma.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		}
		else forma = new Shape3D();

		tgLines.addChild(forma);
		tgRet.addChild(tgLines);

		return tgRet;

	}

	
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
			transforms.add(new MyTransform3D(dataRaw, count));
			count += 6;
		}
		scr_opt.num_points = files.size();

		return 0;

	}

	@Override
	public void applyMirror(int plane) {
		// Nothing to do mirroring trajectories. Not applicable.
	}

}
