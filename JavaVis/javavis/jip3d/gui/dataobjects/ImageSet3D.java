package javavis.jip3d.gui.dataobjects;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Image3D;
import javavis.jip3d.geom.MyKDTree;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import org.w3c.dom.Document;


public class ImageSet3D extends ScreenData {
	private static final long serialVersionUID = -1468467074623135969L;
	
	ArrayList<BranchGroup> bgList;
	ArrayList<Double> tsList;
	ArrayList<Image3D> imList;
	TransformGroup tGroup;

	public ImageSet3D(ScreenOptions opt) {
		super(opt);
		opt.global_color = false;
		opt.improved = false;
		this.setType(ScreenOptions.tIMAGESET3D);
		bgList = new ArrayList<BranchGroup>();
		tsList = new ArrayList<Double>();
		imList = new ArrayList<Image3D>();
		tGroup=new TransformGroup();
		tGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		tGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
	}
	
	public void show (int currentTL, boolean isAccum) {
		int count, size, auxCount=-1;
		size=bgList.size();
		double closest=Double.MAX_VALUE, auxVal;
		
		if (isAccum) {
			for (count=0; count<size; count++) {
				if (tsList.get(count) <= currentTL) {
					if (!bgList.get(count).isLive()) 
						tGroup.addChild(bgList.get(count));
				}
				else {
					if (bgList.get(count).isLive()) 
						bgList.get(count).detach();
				}
			}
		}
		else {
			for (count=0; count<size; count++) {
				if (bgList.get(count).isLive()) 
					bgList.get(count).detach();
				auxVal = Math.abs(currentTL-tsList.get(count));
				if (auxVal<closest) {
					closest=auxVal;
					auxCount=count;
				}
			}
			if (auxCount!=-1) {
					tGroup.addChild(bgList.get(auxCount));
			}
		}
	}
	
	public double getMinTimeStamp () {
		double value=Double.MAX_VALUE;
		
		for (double v : tsList) {
			if (v<value)
				value=v;
		}
		return value;
	}
	
	public double getMaxTimeStamp () {
		double value=Double.MIN_VALUE;
		
		for (double v : tsList) {
			if (v>value)
				value=v;
		}
		return value;
	}
	
	public int getNImages () {
		return tsList.size();
	}

	@Override
	protected TransformGroup paint() {
		Transform3D transform;
		OrientedShape3D oshape;
		ImageComponent2D imAux;
		int width, height;

		Geometry geometry;
		Texture2D texture;
		Appearance appear;
		MyTransform3D total_transform = new MyTransform3D();
		
		for(Image3D image : imList) {
			imAux = new ImageComponent2D(ImageComponent.FORMAT_RGBA, image.getData());
			width = imAux.getWidth();
			height = imAux.getHeight();
			
			geometry = createGeometry();
			texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,  width, height);
			texture.setImage(0, imAux);
			texture.setEnable(true);
			texture.setMagFilter(Texture.NICEST);
			appear = new Appearance();
			appear.setTexture(texture);
			appear.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0.1f));
			oshape = new OrientedShape3D(geometry, appear, OrientedShape3D.ROTATE_ABOUT_AXIS, new Point3f(image.getCoordsf()));
			total_transform.applyTransform(image.transform);
			transform = new Transform3D(total_transform.getMatrix4d());
			TransformGroup auxT = new TransformGroup(transform);
			auxT.addChild(oshape);
			BranchGroup bgAux = new BranchGroup();
			bgAux.setCapability(BranchGroup.ALLOW_DETACH);
			bgAux.addChild(auxT);
			bgList.add(bgAux);
			tsList.add(image.timeStamp);
			tGroup.addChild(bgAux);
		}
		return tGroup;
	}
	
	public Geometry createGeometry() { 
        QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);

        Point3f p = new Point3f(-1.0f,  1.0f,  0.0f);
        plane.setCoordinate(0, p);
        p.set(-1.0f, -1.0f,  0.0f);
        plane.setCoordinate(1, p);
        p.set(1.0f, -1.0f,  0.0f);
        plane.setCoordinate(2, p);
        p.set(1.0f,  1.0f,  0.0f);
        plane.setCoordinate(3, p);

        TexCoord2f q = new TexCoord2f( 0.0f,  1.0f);
        plane.setTextureCoordinate(0, 0, q);
        q.set(0.0f, 0.0f);
        plane.setTextureCoordinate(0, 1, q);
        q.set(1.0f, 0.0f);
        plane.setTextureCoordinate(0, 2, q);
        q.set(1.0f, 1.0f);
        plane.setTextureCoordinate(0, 3, q);

        return plane;
    }
	
	public void addPoint (Point3D p) {
		data.insert(p.getCoords(), p);
	}
	
	public int getNumElements() {
		return imList.size();
	}

	@Override
	public int readData(String file_name, String iPath, Document doc) {
		int num_img, index;
		Image3D image;
		String[] dataRaw;

		name = file_name;
		path = iPath;
		
		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" +|\\n+");
		num_img = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());

		for (int cont=0;cont<num_img;cont++) {
			index=8*cont+1;
			image = new Image3D(dataRaw, path, index);
			imList.add(image);
		}
		scr_opt.num_points = imList.size();

		return imList.size();
	}

	@Override
	public void writeData(String name, String path) {
		int num_points = imList.size();
		FileWriter fw;
		String header="<javavis3D>\n";
		String tail="</data>\n</javavis3D>";
		
		header += "<type>ImageSet3D</type>\n"+
				  "<nelements>"+num_points+"</nelements>\n<data>\n";

		if(path.charAt(path.length()-1)!='/')
			path += '/';
		
		try {
			fw = new FileWriter(path+name);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(header);
			for(int count=0;count<num_points;count++) {
				bw.write(imList.get(count).toString());
				bw.flush();
			}
			bw.write(tail);
			bw.flush();
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
		Image3D element;
		elements = elements();
		size = elements.length;
		data = new MyKDTree(3);

		for(count=0;count<size;count++)
		{
			element = (Image3D)elements[count];
			element.applyTransform(trans);
			data.insert(element.getCoords(), element);
		}
	}

	@Override
	public void applyMirror(int plane) {
		//Nothing to do.
	}

}
