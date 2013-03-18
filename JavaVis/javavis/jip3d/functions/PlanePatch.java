package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Normal3D;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;

/**
 * It extracts the plane patches from a set of 3D points.<br />
 * The screen data must be a set of 3D points or a set of 3D normals.<br />
 */
public class PlanePatch extends Function3D {

	private double minimum = 9;
	private double pi2 = Math.PI/2.0;

	public PlanePatch()
	{
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D|ScreenOptions.tNORMALSET3D;
		this.group = Function3DGroup.Model3D;

		ParamFloat p1 = new ParamFloat("Window size");
		p1.setValue(0.03f);
		ParamFloat p2 = new ParamFloat("Minimum size");
		p2.setValue(0.150f);
		ParamFloat p3 = new ParamFloat("PlaneThick");
		p3.setValue(0.005f);

		addParam(p1);
		addParam(p2);
		addParam(p3);

	}
	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		Object []elements = scr_data.elements();;

		if(scr_data.getType() == ScreenOptions.tPOINTSET3D)
			patchPointSet(scr_data, elements);
		else
			patchNormalSet(scr_data);

	}

	private void patchPointSet(ScreenData scr_data, Object[] elements) throws JIPException
	{
		PlaneSet3D new_plane_set = new PlaneSet3D(new ScreenOptions());
		new_plane_set.name = "patches" + scr_data.name.substring(3);
		NormalSVD FSVD = new NormalSVD();
		double win_size = this.getParamValueFloat("Window size");
		double min_size = this.getParamValueFloat("Minimum size");
		double thickness = this.getParamValueFloat("PlaneThick");

		Object []neighbors;
		Point3D element;
		Plane3D new_plane;
		int count, count2;
		int len;
		double rad;
		double []normal;
		Vector3D vec_aux;
		Normal3D normalVector;
		double angle;

		elements = scr_data.elements();
		len = elements.length;

		double prog_inc = 100.0 / len;

		for(count2=0;count2<len;count2++)
		{
			element = (Point3D)elements[count2];
			// has been this element visited?
			if(element.visited<1)
			{
				element.visited = 3;
				rad = element.getOriginDistance() * win_size;
				if(rad<min_size) rad = min_size;

				try
				{
					neighbors = scr_data.range(element, rad);
					if(neighbors.length > minimum)
					{
						element = NormalSVD.calcCentroid(neighbors);
						normal = FSVD.applySVD(element, neighbors, thickness); //saliency
						if(normal!=null)
						{
							if(normal[3]==1)
							{
								vec_aux = new Vector3D(normal[0], normal[1], normal[2]);
								normalVector = new Normal3D(element, vec_aux, normal[4], rad);
								vec_aux = new Vector3D(element);
								angle = vec_aux.getAngle(normalVector.vector);
								if(angle<pi2)
								{
									normal[0] = -normal[0];
									normal[1] = -normal[1];
									normal[2] = -normal[2];
									vec_aux = new Vector3D(normal[0], normal[1], normal[2]);
									normalVector = new Normal3D(element, vec_aux, normal[4], rad);
								}
								//added to avoid overlap
								rad *= 1.6;
								neighbors = scr_data.range(element, rad);
								for(count=0;count<neighbors.length;count++)
								{
									((Point3D)neighbors[count]).visited = 1;

								}
								//create a new plane
								new_plane = new Plane3D(normalVector);
								new_plane_set.insert(new_plane);
							}
						}
						else 
						{
							for(count=0;count<neighbors.length;count++)
							{
								element = (Point3D)neighbors[count];
								element.visited = 2;
							}
						}
					}
				} catch(Exception e) {
					System.err.println(e.getMessage());
					result_list = null;
					return;
				}
			}
			progress += prog_inc;
		}

		for(count=0;count<len;count++)
		{
			element = (Point3D) elements[count];
			element.visited = 0;
		}
		result_list.add(new_plane_set);
	}

	//Write this method to work when normals are already computed 
	//(not necessary compute them again)
	private void patchNormalSet(ScreenData scr_data)
	{
	}
}
