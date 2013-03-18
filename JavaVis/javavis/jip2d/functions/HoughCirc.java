package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamObject;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.geometrics.JIPGeomPoly;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.util.Circumference;

/**
 * It calculates all the possible circumferences presents in the image. Every pixel votes for
 * a circumference to which it can belong, then we use the circumference which its number of 
 * votes are maximum.<br />
 * It applies to BIT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BIT type image.</li>
 * <li><em>thres</em>: Integer value which indicates the minimal number of votes to accept 
 * the circumference (default 30).</li>
 * <li><em>Rmin</em>: Integer value which indicates the minimum radius of the circumference 
 * (default 10).</li>
 * <li><em>Rmax</em>: Integer value which indicates the maximum radius of the circumference 
 * (default 80).</li>
 * </ul><br />
 * <strong>Output parameters:</strong><br />
 * <ul>
 * <li><em>ncirc</em>: Integer value which indicates the number of circumferences found.</li>
 * <li><em>circum</em>: Object with the circumferences found.</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A POLY image which has found circumferences.</li>
 * </ul><br />
 */
public class HoughCirc extends Function2D {
	private static final long serialVersionUID = -3844283432270245143L;

	public HoughCirc() {
		super();
		name = "HoughCirc";
		description = "Obtains the number of circumferences in a input image. Applies to BIT type.";
		groupFunc = FunctionGroup.FeatureExtract;
		
		ParamInt p1 = new ParamInt("thres", false, true);
		p1.setDefault(30);
		p1.setDescription("Minimum percentage of votes");
		
		ParamInt p2 = new ParamInt("Rmin", false, true);
		p2.setDefault(10);
		p2.setDescription("Minimum radius");
		
		ParamInt p3 = new ParamInt("Rmax", false, true);
		p3.setDefault(80);
		p3.setDescription("Maximum radius");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		
		// Return parameters
		ParamInt r1 = new ParamInt("ncirc", false, false);
		r1.setDescription("Number of circumferences found");
		
		ParamObject r2 = new ParamObject("circum", false, false);
		r2.setDescription("Circumferences found");
		
		addParam(r1);
		addParam(r2);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BIT) 
			throw new JIPException("Function HoughCirc can not be applied to this image format.");

		JIPBmpBit imgBit = (JIPBmpBit)img;
		int width = img.getWidth();
		int height = img.getHeight();
		int threshold = getParamValueInt("thres");
		int rmin = getParamValueInt("Rmin");
		int rmax = getParamValueInt("Rmax");
		
		//Creation of a cube which we will use to save the number of votes.
		int xLengthCube = width-1;
		int yLengthCube = height-1;
		int zLengthCube = rmax - rmin;
		int table[][][] = new int[xLengthCube][yLengthCube][zLengthCube];

		//Initialize the table (cube) of votes to 0 value.
		for (int i=0; i < xLengthCube; i++) 
			for (int j=0; j < yLengthCube; j++) 
				for (int k=0; k < zLengthCube; k++) 
					table[i][j][k] = 0;

		//For each pixel of the image.
		for (int i=0; i < width; i++) 
			for (int j=0; j < height; j++) 
				if (imgBit.getPixelBool(i, j))
					for (int xc=0; xc < width-1; xc++)
						for (int yc=0; yc < height-1; yc++) {
							int r = Circumference.calculatesRadius(xc, yc, i, j);
							//Checks that value of radius does not exceed the cube dimensions.
							if (r < rmax && r > rmin)
								table[xc][yc][r - rmin]++;
						}
		
		
		/* Prepare the vector which contains the point vectors for drawing the circumference,
		and other vector for storing the obtained circumferences before removing similar.*/
		ArrayList<Circumference> resultTemp = new ArrayList<Circumference>();

		// The table dimensions are a1, a2 and a3
		boolean flag;
		int ref = 0;
		int circ_f = 0; 
		for (int i=0; i < xLengthCube; i++) {
			for (int j=0; j < yLengthCube; j++) {
				for (int k=0; k < zLengthCube; k++) {
					flag = false;
					/*Checks if that circumference satisfies at least the threshold of pixels that
					 pertains to its perimeter.*/
					if (checkCirc(table[i][j][k], threshold, k + rmin)) {
						ref = table[i][j][k];
						/*If the circumference which it is analyzing satisfies with the
						 minimum threshold, we must to check if it has neighbords that they are equals or
						 better than it. (For each 26 neighbords)*/
						if (((i - 1) >= 0) && ((j - 1) >= 0)
							&& (table[i - 1][j - 1][k] >= ref))
							flag = true;
						if (((i - 1) >= 0) && (table[i - 1][j][k] >= ref))
							flag = true;
						if (((i - 1) >= 0) && ((j + 1) < yLengthCube) && (table[i - 1][j + 1][k] >= ref))
							flag = true;
						if (((j - 1) >= 0) && (table[i][j - 1][k] >= ref))
							flag = true;
						if (((j + 1) < yLengthCube) && (table[i][j + 1][k] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((j - 1) >= 0) && (table[i + 1][j - 1][k] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && (table[i + 1][j][k] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((j + 1) < yLengthCube) && (table[i + 1][j + 1][k] >= ref))
							flag = true;

						//parallel plane
						if (((i - 1) >= 0) && ((j - 1) >= 0) && ((k - 1) >= 0)
								&& (table[i - 1][j - 1][k - 1] >= ref))
							flag = true;
						if (((i - 1) >= 0) && ((k - 1) >= 0)
								&& table[i - 1][j][k - 1] >= ref)
							flag = true;
						if (((i - 1) >= 0) && ((j + 1) < yLengthCube) && ((k - 1) >= 0)
								&& (table[i - 1][j + 1][k - 1] >= ref))
							flag = true;
						if (((j - 1) >= 0) && ((k - 1) >= 0)
								&& (table[i][j - 1][k - 1] >= ref))
							flag = true;
						if (((j + 1) < yLengthCube) && ((k - 1) >= 0)
								&& (table[i][j + 1][k - 1] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((j - 1) >= 0) && ((k - 1) >= 0)
								&& (table[i + 1][j - 1][k - 1] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((k - 1) >= 0)
								&& (table[i + 1][j][k - 1] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((j + 1) < yLengthCube) && ((k - 1) >= 0)
								&& (table[i + 1][j + 1][k - 1] >= ref))
							flag = true;
						if (((k - 1) >= 0) && (table[i][j][k - 1] >= ref))
							flag = true;

						//Parallel plane
						if (((i - 1) >= 0) && ((j - 1) >= 0) && ((k + 1) < zLengthCube)
								&& (table[i - 1][j - 1][k + 1] >= ref))
							flag = true;
						if (((i - 1) >= 0) && ((k + 1) < zLengthCube)
								&& table[i - 1][j][k + 1] >= ref)
							flag = true;
						if (((i - 1) >= 0) && ((j + 1) < yLengthCube) && ((k + 1) < zLengthCube)
								&& (table[i - 1][j + 1][k + 1] >= ref))
							flag = true;
						if (((j - 1) >= 0) && ((k + 1) < zLengthCube)
								&& (table[i][j - 1][k + 1] >= ref))
							flag = true;
						if (((j + 1) < yLengthCube) && ((k + 1) < zLengthCube)
								&& (table[i][j + 1][k + 1] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((j - 1) >= 0) && ((k + 1) < zLengthCube)
								&& (table[i + 1][j - 1][k + 1] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((k + 1) < zLengthCube)
								&& (table[i + 1][j][k + 1] >= ref))
							flag = true;
						if (((i + 1) < xLengthCube) && ((j + 1) < yLengthCube) && ((k + 1) < zLengthCube)
								&& (table[i + 1][j + 1][k + 1] >= ref))
							flag = true;
						if (((k + 1) < zLengthCube) && (table[i][j][k + 1] >= ref))
							flag = true;

						/*If it has not a better neightbord, we save that circumference in our temporal
						 result circumferences vector.*/
						if (!flag) {
							circ_f++;
							resultTemp.add(new Circumference(i, j, k + rmin));
						}
					} 
				}
			}
		}

		/* For each circumference, it compares to the others and, if it has not similar circumferences,
		 * it saves in result vector. If it has similar circumference, it checks if the vector has 
		 * some similar circumference. If the vector has not a similar circumference, we save the
		 * circumference and mark it like saved.
		 * Each time we save a circumference, the result vector votes its box.
		 * Finally, we must to keep the greatest circumference.
		 */
		int higher = 0;
		ArrayList<Circumference> auxVec = new ArrayList<Circumference>();
		int circFfinal = 0;
		for (int i=0; i < circ_f; i++) {
			higher = i;
			flag = false;
			for (int j=0; j < circ_f; j++) {
				if (nearby(resultTemp.get(higher), resultTemp.get(j), rmin)
					&& (resultTemp.get(j)).radius > (resultTemp.get(higher)).radius) {
					higher = j;
					flag = true;
				}
			}
			if (!flag) {
				auxVec.add(resultTemp.get(higher));
				circFfinal++;
			}
		}
		ArrayList<Polygon2D> points_of_circ = new ArrayList<Polygon2D>();
		for (int i=0; i < circFfinal; i++)
			points_of_circ.add(Circumference.getPoints(auxVec.get(i)));
		
		setParamValue("ncirc", circFfinal);
		setParamValue("circum", auxVec);

		JIPGeomPoly res = new JIPGeomPoly(width,height);
		res.setData(points_of_circ);
		
		return res;
	}

	
	/**
	 * Method which checks if two circumferences are quite similar. It is checked from  
	 * their center distance and their radius. If distance is less than double of minimum 
	 * radius then radius are checked.
	 * @param circ1 First circumference to check.
	 * @param circ2 Second circumference to check.
	 * @param rMin The minimal radius.
	 * @return A boolean indicating true if we have similar circumferences, false in otherwise.
	 */
	private boolean nearby(Circumference circ1, Circumference circ2, int rMin) {
		int x = circ1.centerX - circ2.centerX;
		int y = circ1.centerY - circ2.centerY;

		if (Math.sqrt(x*x + y*y) < 2 * rMin) {
			if (Math.abs(circ1.radius-circ2.radius) < rMin) return true;
			else return false;
		} 
		else return false;
	}

	/**
	 * Method which checks if the number of received votes (pixels with value '1') 
	 * are at least the same the specified percentage in the circumference perimeter.
	 * @param num_votes Number of votes of the studied circumference.
	 * @param percentage Minimal percentage of pixels which value equal to '1'.
	 * @param radius Radius of the circumference.
	 * @return A boolean value indicating true if the upper number is more than required, false
	 * in otherwise.
	 */
	private boolean checkCirc(int num_votes, int percentage, int radius) {
		int perimeter = (int) (2 * Math.PI * radius);
		if (num_votes >= perimeter * (percentage / 100.0))
			return true;
		else
			return false;
	}
}

