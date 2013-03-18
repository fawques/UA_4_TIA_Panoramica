package javavis.jip3d.gui;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javavis.Gui;
import javavis.base.Kinect;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BadTransformException;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.Locale;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.ViewingPlatform;

class KinectLife extends JFrame {
	private static final long serialVersionUID = -6967009058285257908L;
	
	Locale myLocale;
	BranchGroup myBranchGroup;
	BranchGroup currentBG;
	ViewingPlatform myViewingPlatform;
	View view;
	TransformGroup tgView;
	Canvas3D canvas;
	Kinect kinect;
	Transform3D orig = new Transform3D();
	boolean running=false;
	PointArray geometry;
	double[] points;
	byte[] colors;
	
	public KinectLife (GraphicsConfiguration arg0, Kinect kinecti) {
		super();
		WindowListener l = this.new MyWindowAdapter(this);
		this.addWindowListener(l);
		this.setDefaultCloseOperation(Gui.DO_NOTHING_ON_CLOSE);
		
		kinect=kinecti;
		canvas = new Canvas3D(arg0);
		add(canvas, BorderLayout.CENTER);
		VirtualUniverse miUniverso = new VirtualUniverse();
		myLocale = new Locale(miUniverso);
		view = buildView();
		buildBranchView(view, new Vector3f(0.0f, 0.0f, 5.0f)); 
		points = new double[3*kinect.getLength()];
		colors = new byte[3*kinect.getLength()];
		currentBG=captureKinect();
		myLocale.addBranchGraph(currentBG);
		
		// Add the view branch to the tree
		double [] or = {0, 0, -1};
		changeView(new Vector3f(), or);
		this.setSize(640, 480);
		updateViewWithTransform();

		this.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				updateViewWithTransform();
			}
			public void keyReleased(KeyEvent e) {
				updateViewWithTransform();

			}
			public void keyTyped(KeyEvent e) {
			}
		});
	}
	
	public void updateView() {
		running=true;
    	UpdateView mpb = new UpdateView();
		Thread th2 = new Thread(mpb);
		th2.start();
	}

	private View buildView() {
		View myview = new View();
		myview.addCanvas3D(canvas);
		PhysicalBody mybody = new PhysicalBody();

		myview.setPhysicalBody(mybody);

		myview.setPhysicalEnvironment(new PhysicalEnvironment());
		myview.setBackClipDistance(5000.0f);
		myview.setFrontClipDistance(0.001f);
		return(myview);
	}
	
	private BranchGroup captureKinect () {
		TransformGroup tgaux = new TransformGroup();
		Appearance app = new Appearance();
		BranchGroup bgaux =new BranchGroup();
		bgaux.setCapability(BranchGroup.ALLOW_DETACH);
		
		PointSet3D pset = kinect.getPointSet3D();
		geometry = new PointArray(kinect.getLength(),PointArray.COORDINATES|PointArray.COLOR_3| PointArray.BY_REFERENCE);
		geometry.setCapability(PointArray.ALLOW_REF_DATA_WRITE);
		if (pset!=null) {
			Object[] elements = pset.elements();
			for(int count=0;count<elements.length;count++) {
				points[3*count] = ((Point3D)elements[count]).getX();
				points[3*count+1] = ((Point3D)elements[count]).getY();
				points[3*count+2] = ((Point3D)elements[count]).getZ();
				colors[3*count] = ((Point3D)elements[count]).color.x;
				colors[3*count+1] = ((Point3D)elements[count]).color.y;
				colors[3*count+2] = ((Point3D)elements[count]).color.z;
			}
		}
		geometry.setCoordRefDouble(points);
		geometry.setColorRefByte(colors);
		Shape3D shape = new Shape3D(geometry,app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		tgaux.addChild(shape);
		bgaux.addChild(tgaux);
		return bgaux;
	}

	private void buildBranchView(View myview, Vector3f position) {
		myBranchGroup = new BranchGroup();
		TransformGroup TGV = new TransformGroup();
		//Set the ViewPlatform
		myViewingPlatform = new ViewingPlatform(3);
		myViewingPlatform.getViewPlatform().setViewAttachPolicy(View.NOMINAL_HEAD);
		TGV.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		TGV.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Transform3D t = new Transform3D();

		TGV.setTransform(t);

		tgView = myViewingPlatform.getViewPlatformTransform();

		//This 'behavior' handles the keyboard despite obstacles
		KeyNavigatorBehavior behavior = new KeyNavigatorBehavior(tgView);
		behavior.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));

		//Add mouse interaction
		MouseTranslate mt = new MouseTranslate();
		mt.setTransformGroup(tgView);
		mt.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));
		
		MouseZoom mz = new MouseZoom();
		mz.setTransformGroup(tgView);
		mz.setSchedulingBounds(new BoundingSphere(new Point3d(),1000.0));

		TGV.addChild(myViewingPlatform);

		myBranchGroup.addChild(TGV);
		myBranchGroup.addChild(behavior);
		myBranchGroup.addChild(mt);
		myBranchGroup.addChild(mz);

		//add a white background color
		Background bg = new Background(new Color3f(1,1,1));
		bg.setApplicationBounds(new BoundingSphere(new Point3d(), 1000.0));
		myBranchGroup.addChild(bg);

		//add the branch view to the tree
		myLocale.addBranchGraph(myBranchGroup);
		myview.attachViewPlatform(myViewingPlatform.getViewPlatform());
	}


	public void changeView(Vector3f position, double[]orientation) {
		Transform3D t;
		Matrix3d candidate;
		double angle;
		Vector3D v_or = new Vector3D(orientation);
		Vector3D ejeZ = new Vector3D(0, 0, 1);
		Vector3D director;

		//degenerated cases
		if(v_or.getZ()==1)
		{
			director = new Vector3D(0, 1, 0);
			angle = 0;
		}
		else if(v_or.getZ()==-1)
		{
			director = new Vector3D(0, 1, 0);
			angle = Math.PI;
		}
		else
		{
			director = ejeZ.crossProduct(v_or);
			angle = Math.asin(director.module);
			director.normalize();
		}

		double []rot_mat = director.generalRotationMatrix(angle);
		t = new Transform3D();
		candidate = new Matrix3d(rot_mat);
		t.setRotation(candidate);
		t.setTranslation(position);
		try {
			tgView.setTransform(t);
		} catch(BadTransformException e) {
			System.out.println(e.toString());
		}
		this.updateViewWithTransform();
	}

	public void updateViewWithTransform() {
		tgView.getTransform(orig);
	}

	class UpdateView implements Runnable {
    	public UpdateView () {
    	}
    	public void run () {
    		while (running) {
        		try {
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
				geometry.updateData(new GeometryUpdater(){
			        public void updateData(Geometry geometry){
			        	kinect.getPoints(points, colors);
				        }
				      });
    			updateViewWithTransform();
    		}
    	}
    }
	
	class MyWindowAdapter extends WindowAdapter {
		KinectLife kl;
		
		MyWindowAdapter (KinectLife kli) {
			kl=kli;
		}
		public void windowClosing(WindowEvent e) {
			running=false;
			kl.dispose();
			kinect.disconnect();
		}
	};
}
