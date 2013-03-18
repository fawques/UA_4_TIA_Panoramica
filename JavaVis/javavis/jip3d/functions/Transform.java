package javavis.jip3d.functions;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;

/**
 * It applies a transform (rotation and translation). The information is in radians.<br />
 * The screen data can be of any type.<br />
 */
public class Transform extends Function3D {
	
	public Transform() {
		super();
		this.allowed_input = ScreenOptions.tALLTYPES;
		this.group = Function3DGroup.Transform;

		ParamFloat p1 = new ParamFloat("angle X");
		p1.setValue(0.0f);
		ParamFloat p2 = new ParamFloat("angle Y");
		p2.setValue(0.0f);
		ParamFloat p3 = new ParamFloat("angle Z");
		p3.setValue(0.0f);
		ParamFloat p4 = new ParamFloat("traslation X");
		p4.setValue(0.0f);
		ParamFloat p5 = new ParamFloat("traslation Y");
		p5.setValue(0.0f);
		ParamFloat p6 = new ParamFloat("traslation Z");
		p6.setValue(0.0f);

		this.addParam(p1);
		this.addParam(p2);
		this.addParam(p3);
		this.addParam(p4);
		this.addParam(p5);
		this.addParam(p6);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		double angx = this.getParamValueFloat("angle X");
		double angy = this.getParamValueFloat("angle Y");
		double angz = this.getParamValueFloat("angle Z");
		double tx = this.getParamValueFloat("traslation X");
		double ty = this.getParamValueFloat("traslation Y");
		double tz = this.getParamValueFloat("traslation Z");
		
		MyTransform tr = new MyTransform3D(tx, ty, tz, angx, angy, angz);
		
		scr_data.applyTransform(tr);
	}
	
}
