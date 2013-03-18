package javavis.jip3d.functions;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.media.j3d.GraphicsContext3D;
import javax.media.j3d.Raster;
import javax.media.j3d.Transform3D;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamString;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.MyTransform;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.gui.MyCanvas3D;
import javavis.jip3d.gui.dataobjects.Trajectory;

/**
 * It records a sequence of images at different points of view. A Trajectory3D must be 
 * declared and this function grabs a picture of the scene at each pose in the trajectory.<br />
 * The screen data must be a trajectory 3D file.<br />
 */
public class RecordVideo extends Function3D {

	public RecordVideo() {
		super();
		this.allowed_input = ScreenOptions.tTRAJ3D;
		this.group = Function3DGroup.Others;

		ParamString p1 = new ParamString("Output File Name");
		p1.setValue("Output");
		ParamDir p2 = new ParamDir("Output Path");

		addParam(p1);
		addParam(p2);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		String filename = getParamValueString("Output File Name");
		String path = getParamValueDir("Output Path");
		int num_poses, count;
		Trajectory traj;
		File outputFile;
		String nextFileName;
		MyTransform tr_global = new MyTransform3D();
		MyTransform tr_current;
		Transform3D transform;

		traj = (Trajectory) scr_data;
		num_poses = traj.files.size();

		for(count=0;count<num_poses;count++)
		{
			nextFileName = getNextFileName(path+"/"+filename, count);
			tr_current = new MyTransform3D(traj.transforms.get(count));
			tr_current.applyTransform(tr_global);
			tr_global = tr_current;
			transform = new Transform3D(tr_global.getMatrix4d());

			this.getCanvas().tgView.setTransform(transform);

			outputFile = new File(nextFileName);
			try {
				ImageIO.write(getSnapShot(getCanvas()), "PNG", outputFile);
				Thread.sleep(750); //this is to ensure the scene has been repainted after transformation
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method obtains a snapshot from the canvas3D. It grabs the scene just as it
	 * is watched by the user
	 * @param canvas3d
	 * @return
	 */
	public BufferedImage getSnapShot(MyCanvas3D canvas3d)
	{
		GraphicsContext3D ctx = canvas3d.getGraphicsContext3D();
		Dimension scrDim = canvas3d.getSize();

		// setting raster component
		Raster ras =
			new javax.media.j3d.Raster(
			new javax.vecmath.Point3f(-1.0f, -1.0f, -1.0f),
			javax.media.j3d.Raster.RASTER_COLOR,
			0,
			0,
			scrDim.width,
			scrDim.height,
			new javax.media.j3d.ImageComponent2D(
			javax.media.j3d.ImageComponent.FORMAT_RGB,
			new java.awt.image.BufferedImage(scrDim.width, scrDim.height, java.awt.image.BufferedImage.TYPE_INT_RGB)),
			null);

		ctx.readRaster(ras);
		BufferedImage img = ras.getImage().getImage();
		return img;
	}

	/**
	 * This method is used for obtaining a new file name in a sequence. It merges a file
	 * root plus a sequence number [0,999], plus an image file extension.
	 * @param root File name root
	 * @param count Sequence number
	 * @return Complete file name
	 */
	public String getNextFileName(String root, int count)
	{
		String ret;
		if(count<10)
			ret = root + "00" + count;
		else if(count<100)
			ret = root + "0" + count;
		else ret = root + count;
		return ret + ".png";
	}

}
