package javavis.jip3d.functions;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.vecmath.Color3b;

import javavis.base.ColorTools;
import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamString;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;

/**
 * It gets a 2D image from a set of 3D points. No internal order from sensor is used. Image 
 * RGB values are extracted from 3D depth. The function writes extrinsic parameters to the 
 * standard output. Those parameters can be used for mapping the generated image onto the 3D 
 * data following the opposite procedure in FromImageTo3D
 * function.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class From3DToImage2 extends Function3D {

	Vector3D o;
	Point3D reference;
	String filename;

	public From3DToImage2() {
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Transform;

		o = new Vector3D(0,0,-1);

		//if setted, automatic 'focus' distance is used
		ParamBool p1 = new ParamBool("Auto");
		p1.setValue(false);
		//if setted, 'focus' distance is computed as the distance of the mass center
		//from all the points in the set (slow)
		ParamBool p2 = new ParamBool("Mean");
		p2.setValue(true);
		//if no automatic 'focus' distance, this is used
		ParamFloat p3 = new ParamFloat("Distance");
		p3.setValue(-100.0f);
		ParamFloat p4 = new ParamFloat("MinDist");
		p4.setValue(1.5f);
		ParamFloat p12 = new ParamFloat("MaxDist");
		p12.setValue(80.1f);
		ParamString p5 = new ParamString("Output File Name");
		p5.setValue("Output");
		ParamDir p6 = new ParamDir("Output Path");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p12);
		addParam(p5);
		addParam(p6);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		double dist;
		double [][]dist_image;
		BufferedImage image;
		int i,j;
		int count;
		double []min_bounds;
		double []max_bounds;
		Object[] elements;
		Point3D element;
		int []X;
		int []Y;
		dist = 0;

		//Normal vector estimation function
		NormalSVD fsvd = new NormalSVD();
		double max_dist = getParamValueFloat("MaxDist");

		progress = 0.0;
		double prog_inc;
		double minimum = getParamValueFloat("MinDist");
		addFileExt(getParamValueDir("Output Path")+"/"+getParamValueString("Output File Name"));

		//recover some important information
		int d_minY, d_maxY, d_minX, d_maxX;
		d_minX = d_minY = Integer.MAX_VALUE;
		d_maxX = d_maxY = Integer.MIN_VALUE;
		//we also compute the center of mass for the set
		elements = scr_data.elements();
		X = new int[elements.length];
		Y = new int[elements.length];

		//get distance
		boolean auto = getParamValueBool("Auto");
		boolean mean;
		if(auto)
		{
			min_bounds = scr_data.getMinRange();
			max_bounds = scr_data.getMaxRange();
			mean = getParamValueBool("Mean");
			if(mean)
			{
				//overflow problems?
				for(count=0;count<elements.length;count++)
				{
					element = (Point3D)elements[count];
					dist += element.getZ();
				}
				dist /= elements.length;
			}
			else
			{
				dist = (min_bounds[2] + max_bounds[2]) / 2;
			}
		}
		else dist = getParamValueFloat("Distance");

		System.out.println("Distance: "+dist);
		
		reference = new Point3D(0,0,dist);
		double []coords = new double[2];
		double origin_dist;
		double rad;
		double []normal;
		Object []neighs;
		Point3D paux;

		prog_inc = 50.0/elements.length;
		for(count=0;count<elements.length;count++)
		{
			progress += prog_inc;
			element = (Point3D)elements[count];
			origin_dist = element.getOriginDistance();
			if(origin_dist>minimum)
			{
				//image coordinates computation
				coords = transform2(dist, element, new Point3D());
				X[count] = (int)coords[0];
				Y[count] = (int)coords[1];

				if(X[count]<d_minX)
					d_minX = X[count];
				if(X[count]>d_maxX)
					d_maxX = X[count];
				if(Y[count]<d_minY)
					d_minY = Y[count];
				if(Y[count]>d_maxY)
					d_maxY = Y[count];
			}
		}

		//picture sizing
		int xoffset, yoffset;
		int width, height;
		width = d_maxX - d_minX +1;
		height = d_maxY - d_minY +1;
		xoffset = d_minX;
		yoffset = d_maxY;
		System.out.println("image size: "+width+"x"+height);
		System.out.println("image offset: "+xoffset+"x"+yoffset);

		int greycolor;
		int color;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		dist_image = new double[height][width];
		for(i=0;i<height;i++)
			for(j=0;j<width;j++)
				dist_image[i][j] = Double.MAX_VALUE;
		for(count=0;count<elements.length;count++)
		{
			progress += prog_inc;
			element = (Point3D)elements[count];
			origin_dist = element.getOriginDistance();
			if(origin_dist>minimum && origin_dist<dist_image[-Y[count]+yoffset][X[count]-xoffset])
			{
				//normal vector computation
				rad = origin_dist * 0.025; //win_size manually setted to 0.025
				neighs = scr_data.range(element, rad);
				paux = NormalSVD.calcCentroid(neighs);
				normal = fsvd.applySVD(paux, neighs, 10); //thickness manually setted to 10

				if(normal!=null)
				{
					if(normal[4]<0.05) //from red to yellow
					{
						greycolor = (int)(255*((normal[4]/0.05)));
						color = ColorTools.convertRGBToInt(new Color3b((byte)255, (byte)greycolor, (byte)0));
					}
					else if(normal[4]<1.0) //from yellow to green
					{
						greycolor = (int)(255*(1-(normal[4])));
						color = ColorTools.convertRGBToInt(new Color3b((byte)greycolor, (byte)255, (byte)0));
					}
					else if(normal[4]<2.0) //from green to cyan
					{
						greycolor = (int)(255*((normal[4]-1)));
						color = ColorTools.convertRGBToInt(new Color3b((byte)0, (byte)255, (byte)greycolor));
					}
					else //blue
						color = ColorTools.convertRGBToInt(new Color3b((byte)0, (byte)0, (byte)255));
					image.setRGB(X[count]-xoffset, -Y[count]+yoffset, color);
					dist_image[-Y[count]+yoffset][X[count]-xoffset] = origin_dist;
				}
				else
				{
					greycolor = (int)(255*(1-(origin_dist/max_dist)));
					image.setRGB(X[count]-xoffset, -Y[count]+yoffset, ColorTools.convertRGBToInt(new Color3b((byte)greycolor, (byte)greycolor, (byte)greycolor)));
					dist_image[-Y[count]+yoffset][X[count]-xoffset] = origin_dist;
				}
			}
		}

		//write to a file
		System.out.println("Writting image file: "+filename);
		
		try {
			File outputFile = new File(filename);
			ImageIO.write(image, "PNG", outputFile);
		} catch(Exception e)
		{
			System.out.println(e.toString());
			System.out.println("File not saved");
		}
	}

	/**
	 * This function perform a projection from a 3D point on a cylinder.
	 * @param distance cylinder radius
	 * @param p Point to be projected
	 * @param origin Coordinates origin. Usually (0,0,0)
	 * @return x,y position of p point in cylinder coordinates
	 */
	public double [] transform2(double distance, Point3D p, Point3D origin)
	{
		double []ret = new double[2];
		Point3D P = p.subPoint(origin);
		Vector3D Vr = new Vector3D(0, 0, -1);
		Vector3D Vx = new Vector3D(P.getX(), 0, P.getZ());
		Vector3D Vp;
		double Xp, Zp;
		double angle_x;
		double angle_y;
		double dist = -distance;

		Vx.normalize();
		Xp = Vx.getX() * dist;
		Zp = Vx.getZ() * dist;

		Vx = new Vector3D(p);
		Vp = new Vector3D(Xp, 0 , Zp);
		angle_x = Vr.getAngle(Vp);
		angle_y = Vx.getAngle(Vp);

		ret[0] = angle_x * dist;
		ret[1] = Math.tan(angle_y) * dist;
		if(p.getX()<0) ret[0] = -ret[0];
		if(p.getY()<0) ret[1] = -ret[1];

		return ret;
	}


	/**
	 * This method adds file name extension if it doesn't exist
	 * @param name
	 */
	public void addFileExt(String name)
	{
		String ext = name.substring(name.length()-3);
		ext.toLowerCase();
		if(name.charAt(name.length()-4)!='.')
			filename = name+".png";
		else filename = name.substring(0, name.length()-3) + "png";
	}

}
