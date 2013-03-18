package javavis.jip3d.gui.dataobjects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.Neuron3D;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.w3c.dom.Document;

/**
 * NeuronSet3D Class
 * @author Miguel Cazorla
 */
public class NeuronSet3D extends ScreenData {
	private static final long serialVersionUID = 4135526725951827644L;
	
	/**
	 * Count the relations between neurons
	 * @uml.property  name="conexions"
	 */
	private int conexions;
	
	public NeuronSet3D(ScreenOptions opt) {
		super(opt);
		opt.color = new Color3f(0,1,0);
		this.setType(ScreenOptions.tNEURONSET3D);
	}

	@Override
	protected TransformGroup paint() {
		TransformGroup tgRet=new TransformGroup();
		Object []elements = data.elements();
		Point3f []points;
		points = new Point3f[elements.length];
		PointArray geometry;
		LineArray geomlines;
		Neuron3D[] n3dNeighbors;
		Neuron3D n3d;
		Point3f[] auxPoints = new Point3f[2];
		int contConexions=0;

		geometry = new PointArray(elements.length, PointArray.COORDINATES);
		geomlines = new LineArray(conexions*2, LineArray.COORDINATES|LineArray.COLOR_3);
		
		for(int count=0; count<elements.length; count++) {
			n3d = (Neuron3D)elements[count];
			points[count] = new Point3f(n3d.getCoordsf());
			n3dNeighbors=n3d.getNeuronNeighbors();
			for (int n=0; n<n3dNeighbors.length; n++) {
				auxPoints[0] = new Point3f(n3d.getCoordsf());
				auxPoints[1] = new Point3f(n3dNeighbors[n].getCoordsf());
				if (contConexions>conexions) System.out.println("me he pasado "+count);
				geomlines.setCoordinates(contConexions*2, auxPoints);
				geomlines.setColor(contConexions*2, scr_opt.color);
				geomlines.setColor(contConexions*2 + 1, scr_opt.color);
				contConexions++;
			}
		}

		geometry.setCoordinates(0, points);
		Shape3D shape = new Shape3D(geometry,object_app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		tgRet.addChild(shape);
		
		shape = new Shape3D(geomlines,this.object_app);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COORDINATE_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_COUNT_READ);
		shape.getGeometry().setCapability(GeometryArray.ALLOW_FORMAT_READ);
		tgRet.addChild(shape);

		return tgRet;
	}

	public int readData(String file_name, String iPath, Document doc) {
		String[] dataRaw;
		int num_neurons;
		Neuron3D n3d;
		Neuron3D[] n3dSet, n3dSetAux;
		int[] neighbors;
		int count=0;

		dataRaw = doc.getElementsByTagName("data").item(0).getTextContent().split(" +|\\n+");

		name = file_name;
		path = iPath;

		//Ignore first line
		count += 2;
		
		// Number of neurons
		num_neurons=Integer.parseInt(dataRaw[count++]);
		
		n3dSet= new Neuron3D[num_neurons];
		
		// First, read the neurons
		for (int i=0; i<num_neurons; i++) {
			if(dataRaw[count++].compareTo("nousada")!=0)
			{
				n3d = new Neuron3D(dataRaw, i, count);
				data.insert(n3d.getCoords(), n3d);
				n3dSet[i]=n3d;
				count += 4+n3d.getNeighbors().length;
			}
			else
				n3dSet[i] = null;
		}
		
		// Then, assign the neighbor neurons
		for (int i=0; i<num_neurons; i++) {
			if(n3dSet[i]!=null)
			{
				n3dSetAux=new Neuron3D[n3dSet[i].getNeighbors().length];
				neighbors=n3dSet[i].getNeighbors();
				conexions += n3dSetAux.length;
				for (int n=0; n<n3dSetAux.length; n++) {
					n3dSetAux[n]=n3dSet[neighbors[n]];
				}
				n3dSet[i].setNeuronNeighbors(n3dSetAux);
			}
		}
		
		scr_opt.num_points = data.size();

		return data.size();
	}

	@Override
	public void writeData(String name, String path) {
		if(path.charAt(path.length()-1)!='/')
			path += '/';

		FileWriter fw;
		try
		{
			ArrayList<Neuron3D> neuronList;
			fw = new FileWriter(path+name);
			fw.write("0\n");
			fw.write(data.size()+" ");
			fw.write("\n");

			Neuron3D f2d;
			int count;
			Object []elements = data.elements();
			neuronList = new ArrayList<Neuron3D>();
			for(count=0;count<elements.length;count++)
			neuronList.add((Neuron3D)elements[count]);

			for(count=0;count<elements.length;count++)
			{
				fw.write("n"+count+"\n");
				f2d = (Neuron3D)elements[count];
				fw.write(f2d.getX()*100+" "+f2d.getY()*100+" "+f2d.getZ()*100+" "+f2d.getNeighbors().length);
				for(Neuron3D nb : f2d.getNeuronNeighbors())
				{
					fw.write(" "+neuronList.indexOf(nb));
				}
				fw.write("\n");
				
			}

			fw.close();

		} catch(IOException e) {
			System.err.println("Error:" + e.getMessage());
		}

	}

	@Override
	public void applyTransform(MyTransform trans) {
		Object []elements;
		Neuron3D element;
		elements = elements();

		for(int cont=0;cont<elements.length;cont++)
		{
			element = (Neuron3D)elements[cont];
			element.applyTransform(trans);
			//Like Neuron3D extends from Point3D, it will have to update its position in KDTree
		}
	}

	@Override
	public void applyMirror(int plane) {
		Object []elements;
		Neuron3D element;
		elements = elements();

		for(int cont=0;cont<elements.length;cont++)
		{
			element = (Neuron3D)elements[cont];
			element.setValue(plane, -element.getValue(plane));
			//Like Neuron3D extends from Point3D, it will have to update its position in KDTree
		}
	}

}