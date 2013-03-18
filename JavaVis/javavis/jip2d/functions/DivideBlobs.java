package javavis.jip2d.functions;

import java.util.ArrayList;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.util.Blob;

/**
 * It finds the blobs in a binary image.<br />
 * It applies to BIT images.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BIT image.</li>
 * <li><em>radius</em>: Integer value which indicates the radius to scale (default 50).</li>
 * <li><em>minp</em>: Integer value which indicates the minimum number of points required to 
 * be a blob (default 200).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>Returns the blobs separated in bands.</li>
 * </ul><br />
 */
public class DivideBlobs extends Function2D{
	private static final long serialVersionUID = 534245315835245598L;

	public DivideBlobs(){
		super();
		name = "DivideBlobs";
		description = "Find the blobs in a binary image and returns the blobs separated in bands.";
		groupFunc = FunctionGroup.Others;
		
		ParamInt p1 = new ParamInt("radius", false, true);
		p1.setDefault(50);
		p1.setDescription("Radius to scale");
		
		ParamInt p2 = new ParamInt("minp", false, true);
		p2.setDefault(200);
		p2.setDescription("Minimum number of points necessary to be a blob");
		
		addParam(p1);
		addParam(p2);
	}
				
	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BIT) 
			throw new JIPException ("DivideBlobs only applied to BIT images");
		
		int radius = getParamValueInt("radius");
		int minp = getParamValueInt("minp");

		// First, detects blobs in the image
		Function2D func = new Blobs();
		func.processImg(img);
		
		ArrayList<Blob> blobsList = (ArrayList<Blob>)func.getParamValueObj("blobs");
		ArrayList<Integer> xcoordList = new ArrayList<Integer>();
		ArrayList<Blob> blobListAux = new ArrayList<Blob>(); 
		for (Blob b : blobsList) {
			// Check if the number of points is enough
			if (b.length() > minp) {
				b.calcEverything();
				boolean found = false;
				// Sort the blobs from right to left
				for (int i=0; i < blobListAux.size(); i++) {
					if (xcoordList.get(i) > b.center_x) {
						blobListAux.add(i, b);
						xcoordList.add(i, b.center_x);
						found = true;
						break;
					}
				}
				if (!found) {
					blobListAux.add(b);
					xcoordList.add(b.center_x);
				}
			}
		}

		// It creates a new image with a number of bands equal to the number of blobs 
		JIPBmpBit res = new JIPBmpBit(blobListAux.size(), 2*radius, 2*radius);
		JIPImgBitmap imgAux;
		Blob b;
		double sfX, sfY;
		int r, c, cx, cy;
		
		// Apply a scale to get all the images with the same size
		for (int i=0; i < blobListAux.size(); i++) {
			b = blobListAux.get(i);
			imgAux = b.getImage();
			cx = imgAux.getWidth()/2;
			cy = imgAux.getHeight()/2;
			sfX = b.xsize/(2.0*radius);
			sfY = b.ysize/(2.0*radius);
			for (int x=0; x < 2*radius; x++)
				for (int y=0; y<2*radius; y++) {
					c = (int)(sfX * (x-radius)) + cx;
					r = (int)(sfY * (y-radius)) + cy;
					res.setPixel(i, x, y, imgAux.getPixel(c, r));
				}
		}
		
		return res;
	}
}

