package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Octree;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;

/**
 * It implements a filter to reduce the number of 3D points in a 3D image.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class PointFilter extends Function3D {

	public PointFilter() {
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Mapping;

		// resolution param. Cube side length for grouping points
		ParamFloat p1 = new ParamFloat("Resolution");
		p1.setValue(0.10f);

		this.addParam(p1);

	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		Octree total_data;
		Point3D bound_sup;
		Point3D bound_inf;
		float resolution = (float)this.getParamValueFloat("Resolution");
		Object []elements;
		Point3D element;
		int count;
		ArrayList<Point3D> complete_list;
		PointSet3D ret;
		double prog_inc;

		bound_sup = new Point3D(200, 200, 200);
		bound_inf = new Point3D(-200, -200, -200);
		total_data = new Octree(bound_inf, bound_sup, resolution);

		elements = scr_data.elements();
		prog_inc = 50.0/elements.length;

		for(count=0;count<elements.length;count++)
		{
			element = (Point3D) elements[count];
			total_data.insert(element);
			progress += prog_inc;
		}
		complete_list = total_data.getAll();

		ret = new PointSet3D(new ScreenOptions());
		ret.name = "ReducedPointSet";
		prog_inc = 50.0 / complete_list.size();
		for(count=0;count<complete_list.size();count++)
		{
			element = complete_list.get(count);
			ret.insert(element);
			progress += prog_inc;
		}
		result_list.add(ret);
	}

}
