package javavis.jip3d.gui.dataobjects;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;

import org.w3c.dom.Document;

import com.sun.j3d.utils.behaviors.interpolators.TCBKeyFrame;

import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;

public class Spline3D extends Trajectory {
	private static final long serialVersionUID = 8562147378419494892L;
	
	public TCBKeyFrame[] keyframes;

	ArrayList<Float> scaleX, scaleY, scaleZ;
	ArrayList<Float> tension, continuity, bias;
	
	static int LINEAR=1;

	public Spline3D(ScreenOptions opt) {
		super(opt);
		transforms = new ArrayList<MyTransform>();
		this.setType(ScreenOptions.tSPLINE3D);
		scaleX = new ArrayList<Float>(); 
		scaleY = new ArrayList<Float>(); 
		scaleZ = new ArrayList<Float>();
		tension = new ArrayList<Float>(); 
		continuity = new ArrayList<Float>();
		bias = new ArrayList<Float>();
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
		int num_transforms = transforms.size();
		Point3f point;
		int []strips = {num_transforms};
		TransformGroup []cameras = new TransformGroup[num_transforms];
		Transform3D transform;
		Point3D reference;
		int count;

		MyTransform tr_global = new MyTransform3D();
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
			cameras[count] = new TransformGroup(transform);
			tgRet.addChild(cameras[count]);

			reference.applyTransform(tr_global);
			point = new Point3f(reference.getCoordsf());
			lines.setCoordinate(count, point);
		}

		Shape3D shape = new Shape3D(lines, object_app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape.setCapability(Shape3D.ALLOW_PICKABLE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		tgLines.addChild(shape);
		tgRet.addChild(tgLines);

		return tgRet;
	}

	public int readData(String file_name, String iPath, Document doc) {
		int num_poses;
		float time=0.0f, accumTime;
		MyTransform3D t3d;
		MyTransform3D tr_global = new MyTransform3D();
		String[] dataRaw;
		int cont=1;
		
		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" +|\\n+");
		num_poses = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());

		name = file_name;
		path = iPath;

		keyframes = new TCBKeyFrame[num_poses];
		accumTime = 1.0f/(num_poses-1);
				
		for(int i=0;i<num_poses;i++) {
			t3d = new MyTransform3D(dataRaw, cont);
			cont += 6;
			transforms.add(t3d);
			tr_global.applyTransform(t3d);
			scaleX.add((float)Double.parseDouble(dataRaw[cont++]));
			scaleY.add((float)Double.parseDouble(dataRaw[cont++]));
			scaleZ.add((float)Double.parseDouble(dataRaw[cont++]));
			tension.add((float)Double.parseDouble(dataRaw[cont++]));
			continuity.add((float)Double.parseDouble(dataRaw[cont++]));
			bias.add((float)Double.parseDouble(dataRaw[cont++]));
			
			keyframes[i] = new TCBKeyFrame(time, LINEAR,
		            new Point3f((float)tr_global.getTrX(), (float)tr_global.getTrY(), (float)tr_global.getTrZ()),
		            createQuaternionFromEuler((float)tr_global.getAngX(), (float)tr_global.getAngY(), (float)tr_global.getAngZ()),
		            new Point3f(scaleX.get(i), scaleY.get(i), scaleZ.get(i)), tension.get(i),
		            continuity.get(i), bias.get(i));
			
			time += accumTime;
		}
		scr_opt.num_points = transforms.size();

		return 0;
	}
	
	public void writeData(String name, String path) {
		try {
			FileWriter fw = new FileWriter(path+name);
			BufferedWriter bw = new BufferedWriter(fw);
			String header="<javavis3D>\n";
			String tail="</data>\n</javavis3D>";
			int nelements=transforms.size();
			
			header += "<type>Spline3D</type>\n"+
					  "<nelements>"+nelements+"</nelements>\n<data>\n";
			
			bw.write(header);
			for (int i=0; i<nelements; i++) {
				bw.write(transforms.get(i)+" "+scaleX.get(i)+" "+scaleY.get(i)+" "+scaleZ.get(i)+" "+tension.get(i)+" "+continuity.get(i)+" "+bias.get(i));
				bw.newLine();
				bw.flush();
			}
			bw.write(tail);
			bw.flush();
			
			fw.close();
		} catch (IOException e) {
			System.out.println("Problems writing file: "+name+e);
		}
	}
	
	// convert an angular rotation about an axis to a Quaternion
	  static Quat4f createQuaternionFromAxisAndAngle(Vector3d axis, double angle) {
	    double sin_a = Math.sin(angle / 2);
	    double cos_a = Math.cos(angle / 2);

	    // use a vector so we can call normalize
	    Vector4f q = new Vector4f();

	    q.x = (float) (axis.x * sin_a);
	    q.y = (float) (axis.y * sin_a);
	    q.z = (float) (axis.z * sin_a);
	    q.w = (float) cos_a;

	    // It is necessary to normalise the quaternion
	    // in case any values are very close to zero.
	    q.normalize();

	    // convert to a Quat4f and return
	    return new Quat4f(q);
	  }

	  // convert three rotations about the Euler axes to a Quaternion
	  static Quat4f createQuaternionFromEuler(double angleX, double angleY,
	      double angleZ) {
	    // simply call createQuaternionFromAxisAndAngle
	    // for each axis and multiply the results
	    Quat4f qx = createQuaternionFromAxisAndAngle(new Vector3d(1, 0, 0),
	        angleX);
	    Quat4f qy = createQuaternionFromAxisAndAngle(new Vector3d(0, 1, 0),
	        angleY);
	    Quat4f qz = createQuaternionFromAxisAndAngle(new Vector3d(0, 0, 1),
	        angleZ);

	    // qx = qx * qy
	    qx.mul(qy);

	    // qx = qx * qz
	    qx.mul(qz);

	    return qx;
	  }

	@Override
	public void applyMirror(int plane) {
		// Nothing to do mirroring trajectories. Not applicable.
		
	}


}
