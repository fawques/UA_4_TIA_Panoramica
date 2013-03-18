package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPGeomEdges;
import javavis.jip2d.base.geometrics.Point2D;

/**
 * It obtains an EDGE image from the output of function Canny.<br />
 * It applies to BYTE, SHORT or FLOAT type and only band 0 is processed.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BYTE, SHORT or FLOAT type.</li>
 * <li><em>low</em>: Integer value which indicates the percentage of lower threshold (default 30).
 * </li>
 * <li><em>high</em>: Integer value which indicates the percentage of upper threshold (default 45).
 * </li>
 * <li><em>minlength</em>: Integer value which indicates the minimum size of the included points 
 * (default 2).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>An EDGE image for the image points.</li>
 * </ul><br />
 */
public class Link extends Function2D {
	private static final long serialVersionUID = 4366090933966628065L;

	public Link() {
		super();
		name = "Link";
		description = "Creates a set of edges from an image. Applies to BYTE, SHORT or FLOAT type and only band 0 is processed.";
		groupFunc = FunctionGroup.Edges;
		
		ParamInt p1 = new ParamInt("low", false, true);
		p1.setDefault(30);
		p1.setDescription("Lower threshold (In percentage)");
		
		ParamInt p2 = new ParamInt("high", false, true);
		p2.setDefault(45);
		p2.setDescription("Upper threshold (In Percentage)");
		
		ParamInt p3 = new ParamInt("minlength", false, true);
		p3.setDefault(2);
		p3.setDescription("Minimum size of the included points");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.BYTE || img.getType() == ImageType.SHORT || img.getType() == ImageType.FLOAT) {
			ArrayList<Edge> edges = new ArrayList<Edge>();
			ImageType type = img.getType();
			int width = img.getWidth();
			int height = img.getHeight();
			int lowThreshold = getParamValueInt("low");
			int highThreshold = getParamValueInt("high");
			int minLeng = getParamValueInt("minlength");

			switch (type) {
				case SHORT :
					lowThreshold *= 65535 / 100;
					highThreshold *= 65535 / 100;
					break;
				case FLOAT :
					Function2D gray = new GrayToGray();
					gray.setParamValue("gray", "BYTE");
					img = gray.processImg(img);
			}
			double []bmp = ((JIPImgBitmap)img).getAllPixels(0);
			for (int row=0; row < height; row++)
				for (int col=0; col < width; col++)
					if (bmp[col + row * width] >= highThreshold) 
						trackEdge(row, col, width, height, lowThreshold, minLeng, edges, bmp);
			
			JIPGeomEdges res = new JIPGeomEdges(width,height);
			res.setData(edges);
			
			return res;
		}
		else 
			throw new JIPException("Function Link can not be applied to this image format.");
	}

	
	/**
	 * Method which makes a trace in an edge from the position which is passed as argument.
	 * For each point which is added into edge, the function set zero into original image. 
	 * After that, the array is turned round and it continues in the new direction.
	 * @param row The start row.
	 * @param col The start column.
	 * @param width The image width.
	 * @param height The image height.
	 * @param lowThreshold The percentage of lower threshold.
	 * @param minLeng The minimum size of the included points.
	 * @param edges Edge array which contains the list of edges.
	 * @param bmp The pixels of an image.
	 */
	private void trackEdge(int row, int col, int width, int height, int lowThreshold, 
			int minLeng, ArrayList<Edge> edges, double[]bmp) {
		int []p = new int[2];
		int []paux = new int[2];
		p[0] = row;
		p[1] = col;
		paux[0] = row;
		paux[1] = col;
		Edge pointData = new Edge(); 
		
		storePoint(p, width, pointData, bmp);

		while (nextPoint(width, height, lowThreshold, p, bmp))
			storePoint(p, width, pointData, bmp);
		pointData.reverse();
		
		while (nextPoint(width, height, lowThreshold, paux, bmp))
			storePoint(paux, width, pointData, bmp);
		
		if (pointData.length() >= minLeng)
			edges.add(pointData);
	}

	/**
	 * Method which sets the point of edge in the class variable. This variable has the
	 * temporal list of edges.
	 * @param p The current row and column.
	 * @param w The image width.
	 * @param pointData Edge indicating the point data.
	 * @param bmp The pixels of an image.
	 */
	private void storePoint(int []p, int w, Edge pointData, double[]bmp) {
		Point2D point = new Point2D(p[1], p[0]);
		pointData.addPoint(point);
		pointData.addValue((float)bmp[p[1] + p[0] * w]);
		bmp[p[1] + p[0] * w] = 0;
	}

	/**
	 * Method which calculates the next point for continuing with the trace.
	 * @param w The image width.
	 * @param h The image height.
	 * @param lowThreshold The percentage of lower threshold.
	 * @param p The current row and column.
	 * @param bmp The pixels of an image.
	 * @return A boolean indicating true if it finds the next point, false in otherwise.
	 */
	private boolean nextPoint(int w, int h, int lowThreshold, int []p, double[]bmp) {
		int i, r, c;
		int sizeOff = 8;
		
		int[] roff = new int[sizeOff];
		int[] coff = new int[sizeOff];
		roff[0] = 1;
		roff[1] = 0;
		roff[2] = -1;
		roff[3] = 0;
		roff[4] = 1;
		roff[5] = 1;
		roff[6] = -1;
		roff[7] = -1;
		coff[0] = 0;
		coff[1] = 1;
		coff[2] = 0;
		coff[3] = -1;
		coff[4] = 1;
		coff[5] = -1;
		coff[6] = -1;
		coff[7] = 1;

		for (i=0; i < sizeOff; i++) {
			r = p[0] + roff[i];
			c = p[1] + coff[i];
			if (r >= 0 && c >= 0 && r < h && c < w)
				if (bmp[c + r * w] >= lowThreshold) {
					p[0] += roff[i];
					p[1] += coff[i];
					return true;
				}
		}
		return false;
	} 
}

