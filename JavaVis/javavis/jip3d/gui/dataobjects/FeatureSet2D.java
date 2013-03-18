package javavis.jip3d.gui.dataobjects;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Feature2D;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Point3D;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * FeatureSet2D Class
 * @author Miguel Cazorla
 */
public class FeatureSet2D extends ScreenData {
	private static final long serialVersionUID = 377620505581949245L;
	
	/**
	 * @uml.property  name="divisions"
	 */
	public int divisions = 16;
	static private float weight = 100.0f;

	public FeatureSet2D(ScreenOptions opt) {
		super(opt);
		opt.color = new Color3f(0,1,0);
		this.setType(ScreenOptions.tFEATURESET2D);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet=new TransformGroup();
		Shape3D shape;
		int strip[] = new int[data.elements().length];
		double dimension;
		
		LineStripArray limits;
		Point3f []points;
		ArrayList <Point3f>aPoints = new ArrayList<Point3f>();
		float inc_angle = (float)(2*Math.PI/divisions);
		
		int count=0;
		float[] coords = new float[3];
		Feature2D f2d;
		for (Object o: data.elements()) {
			f2d = (Feature2D)o;
			if (f2d.type==1) {
				dimension = f2d.scale/weight; // Just to visualize
			}
			else { //SURF or other
				dimension = 1/(f2d.scale*f2d.scale*weight/8);
			}
			for (int i=0; i<divisions; i++) {
				coords[0]=(float)(f2d.getX()+dimension*Math.cos(f2d.orientation+inc_angle*i)); 
				coords[1]=(float)(f2d.getY()+dimension*Math.sin(f2d.orientation+inc_angle*i)); 
				coords[2]=(float)f2d.getZ();
				aPoints.add(new Point3f(coords));
			}
			// The first point again
			coords[0]=(float)(f2d.getX()+dimension*Math.cos(f2d.orientation)); 
			coords[1]=(float)(f2d.getY()+dimension*Math.sin(f2d.orientation)); 
			coords[2]=(float)f2d.getZ();
			aPoints.add(new Point3f(coords));
			if (f2d.type==1) {
				// Now, the center point
				coords[0]=(float)(f2d.getX()); 
				coords[1]=(float)(f2d.getY()); 
				coords[2]=(float)f2d.getZ();
				aPoints.add(new Point3f(coords));
				strip[count++] = divisions + 2;
			}
			else
				strip[count++] = divisions + 1;
		}

		points = new Point3f[aPoints.size()];
		try
		{
			points = aPoints.toArray(points);
		} catch (Exception e)
		{
			System.out.println("FeatureSet2D::paint Error: Can not convert from ArrayList to Feature2DNew[]");
			return tgRet;
		}
		
		limits = new LineStripArray(points.length,LineStripArray.COORDINATES|LineStripArray.COLOR_3, strip);
		for(count=0;count<points.length;count++) {
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
		int nElements=-1, colorType;
		String[] dataRaw;
		String featFile;
		boolean yInverted=false, zInverted=false, imageCoords;
		ArrayList<double[]> coord3D, coord2D, neighbours;
		Point3D point;
		double[] xyCoords;
		NodeList aux;
		
		nElements = Integer.parseInt(doc.getElementsByTagName("nelements").item(0).getTextContent());
		aux=doc.getElementsByTagName("imagecoords");
		if (aux.getLength()==0)
			imageCoords = false;
		else
			imageCoords = Boolean.parseBoolean(doc.getElementsByTagName("imagecoords").item(0).getTextContent());
		aux=doc.getElementsByTagName("yinverted");
		if (aux.getLength()==0)
			yInverted = false;
		else
			yInverted = Boolean.parseBoolean(doc.getElementsByTagName("yinverted").item(0).getTextContent());
		aux=doc.getElementsByTagName("zinverted");
		if (aux.getLength()==0)
			zInverted = false;
		else
			zInverted = Boolean.parseBoolean(doc.getElementsByTagName("zinverted").item(0).getTextContent());
		aux=doc.getElementsByTagName("colortype");
		if (aux.getLength()==0)
			colorType = 0;
		else
			colorType = Integer.parseInt(doc.getElementsByTagName("colortype").item(0).getTextContent());
		featFile = doc.getElementsByTagName("featurefile").item(0).getTextContent();

		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" \\n+| +|\\n+");

		coord3D=new ArrayList<double[]>();
		coord2D=new ArrayList<double[]>();
		
		for(int count=0; count<nElements; count++) {
			point = new Point3D(dataRaw, count, colorType, imageCoords, yInverted, zInverted);
			if (point.isValid) {
				coord3D.add(point.getCoords());
				xyCoords = new double[2];
				xyCoords[0] = point.posx;
				xyCoords[1] = point.posy;
				coord2D.add(xyCoords);
			}
		}
		
		Reader fr;
		StreamTokenizer st;
		int num_points;
		int lengthDesc;
		Feature2D f2d;
		name = featFile;
		path = iPath;
		int WIDTH_NEIGH=20;

		try
		{
			// file_name contains the path
			fr=new FileReader(file_name+featFile);
			st=new StreamTokenizer(fr);
			st.resetSyntax(); 
			st.ordinaryChar('e');
			st.whitespaceChars(0, ' ');
			st.parseNumbers();  
			
			if (featFile.endsWith("sift")) {
				//read the number of points that the file has
				st.nextToken();
				num_points=(int)st.nval;
				st.nextToken();
				lengthDesc=((int)st.nval);
				
				boolean delete;
				for(int count=0; count<num_points; count++) {
					neighbours=new ArrayList<double[]>();
					delete=false;
					f2d = new Feature2D(st, lengthDesc, 1);
					for (int i=0; i<coord2D.size(); i++) {
						if (f2d.posx==coord2D.get(i)[0] && f2d.posy==coord2D.get(i)[1]) {
							f2d.setX(coord3D.get(i)[0]);
							f2d.setY(coord3D.get(i)[1]);
							f2d.setZ(coord3D.get(i)[2]);
							break;
						}
					}
					if (!delete) {
						data.insert(f2d.getCoords(), f2d);
					}
					neighbours.clear();
				}
			}
			else if (featFile.endsWith("surf")) {
				// Length of the descriptor
				st.nextToken();
				lengthDesc=((int)st.nval)-1;
				//read the number of points that the file has
				st.nextToken();
				num_points=(int)st.nval;
				
				boolean delete;
				for(int count=0; count<num_points; count++) {
					neighbours=new ArrayList<double[]>();
					delete=false;
					f2d = new Feature2D(st, lengthDesc, 2);
					for (int i=0; i<coord2D.size(); i++) {
						if (f2d.posx==coord2D.get(i)[0] && f2d.posy==coord2D.get(i)[1]) {
							f2d.setX(coord3D.get(i)[0]);
							f2d.setY(coord3D.get(i)[1]);
							f2d.setZ(coord3D.get(i)[2]);
							break;
						}
						// This is done to try to get 3d information when a 3D point is not present.
						if (distance(f2d.posx, f2d.posy, (int)coord2D.get(i)[0], (int)coord2D.get(i)[1])<WIDTH_NEIGH) {
							neighbours.add(coord3D.get(i));
						}
					}
					if (!delete)
						data.insert(f2d.getCoords(), f2d);
					else if (delete && neighbours.size()>10) {
						double x=0.0, y=0.0, z=0.0;
						for (double[] coord : neighbours) {
							x += coord[0];
							y += coord[1];
							z += coord[2];
						}
						x /= neighbours.size();
						y /= neighbours.size();
						z /= neighbours.size();
						f2d.setX(x);
						f2d.setY(y);
						f2d.setZ(z);
						data.insert(f2d.getCoords(), f2d);
					}
					neighbours.clear();
				}
			}
			else {
				st.nextToken();
				num_points=(int)st.nval;
				
				boolean delete;
				for(int count=0; count<num_points; count++) {
					neighbours=new ArrayList<double[]>();
					delete=false;
					f2d = new Feature2D(st, 128, 3); //TODO: fix the descriptor length
					for (int i=0; i<coord2D.size(); i++) {
						if (f2d.posx==coord2D.get(i)[0] && f2d.posy==coord2D.get(i)[1]) {
							f2d.setX(coord3D.get(i)[0]);
							f2d.setY(coord3D.get(i)[1]);
							f2d.setZ(coord3D.get(i)[2]);
							break;
						}
					}
					if (!delete) {
						data.insert(f2d.getCoords(), f2d);
					}
					else if (delete && neighbours.size()>10) {
							double x=0.0, y=0.0, z=0.0;
							for (double[] coord : neighbours) {
								x += coord[0];
								y += coord[1];
								z += coord[2];
							}
							x /= neighbours.size();
							y /= neighbours.size();
							z /= neighbours.size();
							f2d.setX(x);
							f2d.setY(y);
							f2d.setZ(z);
							data.insert(f2d.getCoords(), f2d);
						}
					neighbours.clear();
				}
			}
			scr_opt.num_points = data.size();

		} catch (IOException e) {
			System.out.println("Feature2D::readData Error: can not read data from "+file_name);
			return -1;
		}

		return data.size();
	}
	
	private double distance (int x1, int y1, int x2, int y2) {
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}

	@Override
	public void writeData(String name, String path) {
		if(path.charAt(path.length()-1)!='/')
			path += '/';

		FileWriter fw;
		try
		{
			fw = new FileWriter(path+name);
			fw.write(data.size()+" ");
			fw.write("\n");

			for (Feature2D f2d : (Feature2D[])data.elements()) 
				fw.write(f2d.toString()+"\n");

			fw.close();

		} catch(IOException e) {
			System.err.println("Error:" + e.getMessage());
		}

	}

	@Override
	public void applyTransform(MyTransform trans) {
		for (Object o : elements()) {
			((Feature2D)o).applyTransform(trans);
			//Like Neuron3D extends from Point3D, it will have to update its position in KDTree
		}
	}

	@Override
	public void applyMirror(int plane) {
		for (Object o : elements()) {
			((Feature2D)o).setValue(plane, -((Feature2D)o).getValue(plane));
			//Like Neuron3D extends from Point3D, it will have to update its position in KDTree
		}
	}

}
