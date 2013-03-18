package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.util.Blob;

/**
 * It implements the Blob algorithm to get connected regions in the image. This function 
 * receives a binary image and returns a POINT image which represents the centroids of 
 * the obtained blobs.<br />
 * It applies to the first band of a BIT image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BIT type.</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>blobs</em>: A list with the blobs found.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A POINT image where the points indicate the centroids of the Blobs found.</li>
 * </ul><br />
 */
public class Blobs extends Function2D {
	private static final long serialVersionUID = -803595271042342803L;

	public Blobs() {
		super();
		name = "Blobs";
		description = "Obtains the blobs of an image. Applies to BIT type.";
		groupFunc = FunctionGroup.Others;
		
		//Output parameters
		ParamObject o1 = new ParamObject ("blobs", false, false);
		addParam(o1);
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BIT) 
			throw new JIPException("Function Blobs can only be applied to BIT images.");
		
		int code;
		JIPBmpBit srcBit = (JIPBmpBit)img;
		int width = img.getWidth();
		int height = img.getHeight();
		int[][] regions = new int[width][height];

		for (int x=0; x < width; x++) {
			regions[x][0] = 0;
			srcBit.setPixel(x, 0, 0);
		}
		for (int y=0; y < height; y++) {
			regions[0][y] = 0;
			srcBit.setPixel(0, y, 0);
		}

		ArrayList<Blob> list_blobs = new ArrayList<Blob>();
		Blob blob;

		for (int y=1; y < height; y++) {
			for (int x = 1; x < width; x++) {
				// Calculates the code 
				code = (int)(srcBit.getPixel(x-1, y-1) + 2*srcBit.getPixel(x, y-1) + 
						4*srcBit.getPixel(x-1, y) + 8*srcBit.getPixel(x, y));
				if (code < 8) 
					regions[x][y] = 0;
				// New blob
				else if (code == 8 || code == 9) {
					blob = new Blob();
					blob.list_x.add(x);
					blob.list_y.add(y);
					list_blobs.add(blob);
					regions[x][y] = list_blobs.size();
					// Assigns the point to a previous created blob
				} else if (code == 10 || code == 11 || code == 15) {
					blob = list_blobs.get(regions[x][y - 1] - 1);
					blob.list_x.add(x);
					blob.list_y.add(y);
					regions[x][y] = regions[x][y - 1];
					// Assigns the point to a previous created blob
				} else if (code == 12 || code == 13) {
					blob = list_blobs.get(regions[x - 1][y] - 1);
					blob.list_x.add(x);
					blob.list_y.add(y);
					regions[x][y] = regions[x - 1][y];
					// Merge the two correlative blobs
				} else if (code == 14) {
					if (regions[x][y - 1] != regions[x - 1][y])
						merge(regions[x][y - 1], regions[x - 1][y], regions, list_blobs);
					blob = list_blobs.get(regions[x][y - 1] - 1);
					blob.list_x.add(x);
					blob.list_y.add(y);
					regions[x][y] = regions[x][y - 1];
				}
			}
		}

		list_blobs.trimToSize();

		JIPGeomPoint result = new JIPGeomPoint(width, height);
		ArrayList<Blob> aux_list = new ArrayList<Blob>();
		
		for (Blob b : list_blobs) {
			b.calcCentroid();
			if (b.length() > 0 && b.valid) {
				result.addPoint(new Point2D(b.center_x, b.center_y));
				aux_list.add(b);
			}
		}
		
		setParamValue("blobs", aux_list);
		
		return result;
	}

	
	/**
	 * Method which merges two blobs.
	 * @param blob1 Index of the first blob.
	 * @param blob2 Index of the second blob.
	 * @param regions Regions which contains the blobs.
	 * @param list_blobs List which contains the blobs.
	 */
	private void merge(int blob1, int blob2, int[][]regions, ArrayList<Blob> list_blobs) {
		Blob b1 = list_blobs.get(blob1 - 1);
		Blob b2 = list_blobs.get(blob2 - 1);
		
		// Assigns the blob number of the first blob to the points in the second one
		for (int i=0; i<b2.length(); i++)
			regions[b2.list_x.get(i)][b2.list_y.get(i)] = blob1;

		// Add the coordinates from the second blob to the first one
		b1.list_x.addAll(b2.list_x);
		b1.list_y.addAll(b2.list_y);
		
		// Delete the second blob
		b2.list_x.clear();
		b2.list_y.clear();
	}
}

