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
 * It applies a transform (rotation and translation). The information is in degrees.<br />
 * The screen data can be of any type.<br />
 */
public class TransformDeg extends Function3D {
	static double ratio = Math.PI / 180.0;

	public TransformDeg() {
		super();
		this.allowed_input = ScreenOptions.tALLTYPES;
		this.group = Function3DGroup.Transform;

		ParamFloat p1 = new ParamFloat("angle X (Degrees)");
		p1.setValue(0.0f);
		ParamFloat p2 = new ParamFloat("angle Y (Degrees)");
		p2.setValue(0.0f);
		ParamFloat p3 = new ParamFloat("angle Z (Degrees)");
		p3.setValue(0.0f);
		ParamFloat p4 = new ParamFloat("traslation X");
		p4.setValue(0.0f);
		ParamFloat p5 = new ParamFloat("traslation Y");
		p5.setValue(0.0f);
		ParamFloat p6 = new ParamFloat("traslation Z");
		p6.setValue(0.0f);

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		double angx = this.getParamValueFloat("angle X (Degrees)");
		double angy = this.getParamValueFloat("angle Y (Degrees)");
		double angz = this.getParamValueFloat("angle Z (Degrees)");
		double tx = this.getParamValueFloat("traslation X");
		double ty = this.getParamValueFloat("traslation Y");
		double tz = this.getParamValueFloat("traslation Z");

		MyTransform tr = new MyTransform3D(tx, ty, tz, grad2Rad(angx), grad2Rad(angy), grad2Rad(angz));
		
		scr_data.applyTransform(tr);
	}

	private double grad2Rad(double grad)
	{
		return grad * ratio;
	}

}
