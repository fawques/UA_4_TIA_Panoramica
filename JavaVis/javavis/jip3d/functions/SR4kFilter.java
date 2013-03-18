package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.PointSR4K;
import javavis.jip3d.gui.dataobjects.PointSet3D;

/**
 * It obtains information from a SR4000 camera data set, and filter the points which are out of
 * a confidence range.<br />
 * The screen data must be a set of SR4000 points.<br />
 */
public class SR4kFilter extends Function3D {

	public SR4kFilter() {
		name = "SR4kFilter";
		this.allowed_input = ScreenOptions.tSR4000SET3D;
		this.group = Function3DGroup.Others;

		//minimum distance
		ParamFloat p1 = new ParamFloat("distance");
		p1.setValue(1.0f);
		//minimum confidence
		ParamInt p2 = new ParamInt("confidence");
		p2.setValue(240);
		//minimum intensity
		ParamInt p3 = new ParamInt("intensity");
		p3.setValue(25);
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		PointSet3D result = new PointSet3D(new ScreenOptions());
		result.name = scrData.name+".p3d";
		Object []elements = scrData.elements();
		PointSR4K element;
		int count;
		double originDist;
		
		//get input parameters
		double minDist = this.getParamValueFloat("distance");
		int minConf = this.getParamValueInt("confidence");
		int minInt = this.getParamValueInt("intensity");
		
		for(count=0;count<elements.length;count++)
		{
			element = (PointSR4K)elements[count];
			originDist = element.getOriginDistance();
			if(originDist>minDist && element.confidence > minConf && (element.color.get().getRGB() &0xFF) > minInt/originDist)
				result.insert(new Point3D(element));
		}
		result_list.add(result);
	}

}
