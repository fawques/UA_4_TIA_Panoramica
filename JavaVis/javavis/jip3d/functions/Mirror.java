package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamList;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;

/**
 * It implements a mirror transformation, indicating the coordinates you want to fixed.<br />
 * The screen data can be of any type.<br />
 */
public class Mirror extends Function3D {

	public Mirror() {
		super();
		this.allowed_input = ScreenOptions.tALLTYPES;
		this.group = Function3DGroup.Transform;
		
		ParamList p1 = new ParamList("Plane");
		String []list = new String[3];
		list[0] = "XY";
		list[1] = "XZ";
		list[2] = "YZ";
		p1.setDefault(list);
		
		addParam(p1);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		String plane = getParamValueList("Plane");
		int coord = 0;
		
		if(plane.compareTo("XY")==0) coord = 2;
		if(plane.compareTo("XZ")==0) coord = 1;
		if(plane.compareTo("YZ")==0) coord = 0;
		scr_data.applyMirror(coord);
	}

}
