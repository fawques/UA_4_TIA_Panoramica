package javavis.jip3d.functions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javavis.base.ColorTools;
import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;

/**
 * It maps a 2D image onto 3D range data. Extrinsic calibration parameters called Xoffset and 
 * Yoffset have to be known.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class FromImageTo3D extends Function3D {

	Vector3D o;
	double resolution;

	public FromImageTo3D() {
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Transform;

		o = new Vector3D(0,0,-1);

		ParamFile p1 = new ParamFile("Input Image");
		ParamFloat p2 = new ParamFloat("Distance");
		p2.setValue(-1000.0f);
		ParamFloat p3 = new ParamFloat("Xoffset");
		p3.setValue(0.0f);
		ParamFloat p4 = new ParamFloat("Yoffset");
		p4.setValue(0.0f);
		ParamFloat p5 = new ParamFloat("Resolution");
		p5.setValue(1.0f);
		ParamFloat p6 = new ParamFloat("MinDist");
		p6.setValue(16.0f);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		double dist = getParamValueFloat("Distance");
		String input_file = getParamValueFile("Input Image");
		BufferedImage image = null;
		Object []elements;
		Point3D element;
		Point3D origin = new Point3D();
		double prog_inc;
		int count;
		int color;
		double [] coords;
		double xoffset = getParamValueFloat("Xoffset");
		double yoffset = getParamValueFloat("Yoffset");
		resolution = getParamValueFloat("Resolution");
		double minimum = getParamValueFloat("MinDist");
		double dist_origin;
		
		try {
			image = ImageIO.read(new File(input_file));
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		int aux = 0;
		progress = 0;
		elements = scr_data.elements();
		prog_inc = 100.0 / elements.length;
		for(count=0;count<elements.length;count++)
		{
			progress += prog_inc;
			element = (Point3D)elements[count];
			dist_origin = element.getOriginDistance();
			coords = transform2(dist, element, origin);
			coords[0] -= xoffset;
			coords[1] = -coords[1] + yoffset;
			if(dist_origin<minimum || coords[0]<0 || coords[1]<0 || coords[0]>image.getWidth() || coords[1]>image.getHeight())
				color = ColorTools.convertRGBToInt(element.color);
			else
			{
				color = image.getRGB((int)coords[0], (int)coords[1]);

				if(color==0 || color == -16777216) 
					color = ColorTools.convertRGBToInt(element.color);
				else 
					aux++;
			}
			element.color = ColorTools.convertIntIntoRGB(color);
		}
		System.out.println("Num pixel altered: "+aux);
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

}
