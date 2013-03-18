package javavis.jip3d.gui.dataobjects;

import java.io.IOException;
import java.net.URL;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.PointLight;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;


/**
 * SceneModel is a special class. This class is thought to load an lightweight object with
 * the complete description of a scene.
 * @author Diego Viejo
 */
public class SceneModel extends ScreenData {
	private static final long serialVersionUID = 7164552527634919314L;
	private SceneBase sb;
	
	public SceneModel(ScreenOptions opt) {
		super(opt);
		opt.global_color = false;
		opt.improved = false;
		this.setType(ScreenOptions.t3DMODEL);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet = new TransformGroup();
		
		//ambient light
		AmbientLight alight = new AmbientLight();
		alight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000));
		//alight.setEnable(true); Not needed, automatically set by the previous instruction
		tgRet.addChild(alight);
		//In the future, light points should be user-added to the virtual world
		//point light
		PointLight plight = new PointLight();
		plight.setPosition(new Point3f(100f, 100f, 100f));
		plight.setInfluencingBounds(new BoundingSphere(new Point3d(), 1000));
		tgRet.addChild(plight);
		
		BranchGroup bg = sb.getSceneGroup();
		bg.detach();
		tgRet.addChild(bg);
		return tgRet;
	}
	
	
	public int readData(String name, String path) {
		String descr = null;
		ObjectFile loader = new ObjectFile();
		loader.setFlags (Loader.LOAD_ALL
				| ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);
		
		try {
			
			URL url = new URL ("file", "", -1, path+name);
			sb = (SceneBase) loader.load(url);
			descr = sb.getDescription();
			if(descr!=null)
			{
				System.out.println("Information of the object 3D");
				System.out.println(descr);
			}
			System.out.println("Num children: "+sb.getSceneGroup().numChildren());
			
			scr_opt.num_points = 1;
			this.name = name;
			
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	@Override
	public void writeData(String name, String path) {
	}

	public int readData(String name, String iPath, Document doc) {
		return 0;
	}

	@Override
	public void applyTransform(MyTransform trans) {
	}

	@Override
	public void applyMirror(int plane) {
	}

}
