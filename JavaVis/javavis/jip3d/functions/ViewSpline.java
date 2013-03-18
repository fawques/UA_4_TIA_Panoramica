package javavis.jip3d.functions;

import javax.media.j3d.Alpha;
import javax.media.j3d.Transform3D;
import javax.swing.JFrame;

import com.sun.j3d.utils.behaviors.interpolators.RotPosScaleTCBSplinePathInterpolator;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.gui.SplineLapse;
import javavis.jip3d.gui.dataobjects.Spline3D;

/**
 * It shows a trajectory with images 3D defined in a spline 3D file. First of all, it must 
 * load an imageSet3D file and a spline3D file. Then, applies the function and it will shows
 * a "camera" where you will see the point from a trajectory defined in the spline 3D file.<br />
 * The screen data must be a set of 3D images and a spline 3D type.<br />
 */
public class ViewSpline extends Function3D {

	public ViewSpline()
	{
		super();

		this.allowed_input = ScreenOptions.tSPLINE3D;
		this.group = Function3DGroup.Others;
	}
	
	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		Spline3D sp3d = (Spline3D)scr_data;
		long time = (long)(SplineLapse.SLIDER_RANGE*sp3d.keyframes.length);
		Alpha alpha = new Alpha(-1, time);
		
		RotPosScaleTCBSplinePathInterpolator splineInterpolator = 
				new RotPosScaleTCBSplinePathInterpolator(alpha, this.getCanvas().tgView, new Transform3D(),  sp3d.keyframes);
		
		SplineLapse sl = new SplineLapse(this.getCanvas().getProp(), this.getCanvas().getGui3D(), splineInterpolator, sp3d.transforms);
		sl.setSize(500, 120);
		sl.setResizable(false);
		sl.setLocation(0, 600);
		sl.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		sl.addWindowListener(sl.getWindowListener());
		sl.setVisible(true);		
	}

}
