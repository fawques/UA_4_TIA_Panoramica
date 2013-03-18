package javavis.base;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;

import javax.vecmath.Color3b;

import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.ImageGenerator;
import org.OpenNI.ImageMetaData;
import org.OpenNI.OutArg;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

public class Kinect {
	private OutArg<ScriptNode> scriptNode;
    private Context context;
    private DepthGenerator depthGen;
    private ImageGenerator imgGen;
    int width, height;
    private boolean connected=false;
    DepthMetaData depthMD;
    ImageMetaData imgMD;
    
	final String SAMPLE_XML_FILE = "./resources/SamplesConfig.xml";  
	
	public Kinect() {
		try {
	        scriptNode = new OutArg<ScriptNode>();
	        context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);
	
	        if (context==null) {
	        	connected=false;
	        	return;
	        }
	        imgGen = ImageGenerator.create(context);
	        depthGen = DepthGenerator.create(context);
	        if (depthGen.isCapabilitySupported("AlternativeViewPoint")) {
	        	depthGen.getAlternativeViewpointCapability().setViewpoint(imgGen); // Se hace para cambiar el punto de vista al de la imagen y que los puntos 3D estŽn alineados con los datos RGB
	        }
	        depthMD = depthGen.getMetaData();
	        imgMD = imgGen.getMetaData();
	        
	        
	        width = depthMD.getFullXRes();
	        height = depthMD.getFullYRes();
	        
	        connected = true;
	        
	    } catch (GeneralException e) {
	        e.printStackTrace();
	    }
    }
	
	public void disconnect () {
		try {
			context.stopGeneratingAll();
		} catch (StatusException e) {
			e.printStackTrace();
		}
		context.release();
		connected=false;
	}
	
	public int getLength() {
		return width*height;
	}
	
	public boolean isConnected () {
		return connected;
	}
	
	public void waitAnyUpdateAll () {
		try {
			context.waitAnyUpdateAll();
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}
	
	public DepthGenerator getDepthGenerator () {
		return depthGen;
	}
	
	public DepthMetaData getDepthMetaData () {
		return depthGen.getMetaData();
	}
	
	public ImageMetaData getImageMetaData () {
		return imgGen.getMetaData();
	}
	
	public int getWidth () {
		return width;
	}
	
	public PointSet3D getPointSet3D() {
        PointSet3D ps3d = new PointSet3D(new ScreenOptions());
		try {
			ps3d.name = "kinect";
			int x, y;

	        context.waitAnyUpdateAll();
            
            ShortBuffer depth = depthMD.getData().createShortBuffer();
            ByteBuffer img = imgMD.getData().createByteBuffer();
            
            depth.rewind();
            int pos=0;
            while(depth.remaining() > 0)
            {
                pos = depth.position();
                y= pos/width;
            	x=pos%width;
                short d = depth.get();
                if (d!=0) {
	                org.OpenNI.Point3D p=depthGen.convertProjectiveToRealWorld(new org.OpenNI.Point3D(x, y, d));
	                Point3D point = new Point3D(p.getX()/1000, p.getY()/1000, -p.getZ()/1000, new Color3b(img.get(3*pos), img.get(3*pos+1), img.get(3*pos+2)), x, y);
	        		ps3d.addPoint(point);
                }
                else {
                	Point3D point = new Point3D(0, 0, 0, new Color3b((byte)0, (byte)0, (byte)0), x, y);
	        		ps3d.addPoint(point);
                }
            }
            if (ps3d.getNumElements() <2) 
            	return null;
	        ps3d.scr_opt.num_points = ps3d.getNumElements();
	        ps3d.scr_opt.is_visible = true;
        } catch (GeneralException exc) {
            exc.printStackTrace();
        }
        return ps3d;
	}
	
	public void getPoints(double[] points, byte[] colors) {
		try {
            DepthMetaData depthMD = depthGen.getMetaData();
            ImageMetaData imgMD = imgGen.getMetaData();
			int x, y;

	        context.waitAnyUpdateAll();
            
            ShortBuffer depth = depthMD.getData().createShortBuffer();
            ByteBuffer img = imgMD.getData().createByteBuffer();
            
            depth.rewind();
            int pos=0;
            while(depth.remaining() > 0)
            {
                pos = depth.position();
                y= pos/width;
            	x=pos%width;
                short d = depth.get();
                if (d!=0) {
	                org.OpenNI.Point3D p=depthGen.convertProjectiveToRealWorld(new org.OpenNI.Point3D(x, y, d));
	                points[3*pos] = p.getX()/1000;
	    			points[3*pos+1] = p.getY()/1000;
	    			points[3*pos+2] = p.getZ()/1000;
	    			colors[3*pos] = img.get(3*pos);
	    			colors[3*pos+1] = img.get(3*pos+1);
	    			colors[3*pos+2] = img.get(3*pos+2);
                }
                else {
                	points[3*pos] = 0;
	    			points[3*pos+1] = 0;
	    			points[3*pos+2] = 0;
	    			colors[3*pos] = 0;
	    			colors[3*pos+1] = 0;
	    			colors[3*pos+2] = 0;
                }
            }
            
        } catch (GeneralException exc) {
            exc.printStackTrace();
        }
	}
}
