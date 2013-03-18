package javavis.jip2d.functions;

import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.JIPImage;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.*;
import javavis.base.parameter.*;

import java.util.ArrayList;

/**
 * It looks for the Maximally Stable Extremal Regions in an image.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>mins</em>: Integer value which indicates the minimum size of the MSERS (default 50).</li>
 * <li><em>maxs</em>: Integer value which indicates the maximum size of the MSERS (default 500).</li>
 * <li><em>delta</em>: Integer value which indicates the width of the intensity range (default 1).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed BYTE type image.</li>
 * </ul><br />
 */
public class MSER extends Function2D {
	private static final long serialVersionUID = -5543080812213828342L;
	
	private static final int MAX_VALUE_PIXEL = 256;
	
	/**
	 * @uml.property  name="mSERS"
	 * @uml.associationEnd  multiplicity="(0 -1)" inverse="this$0:javavis.jip2d.functions.MSER$MSER"
	 */
	private ArrayList<MyMSER> MSERS = new ArrayList<MyMSER>();
	
	/**
	 * @uml.property  name="iNNER"
	 */
	private double INNER = 1000;
	
	/**
	 * @uml.property  name="minSize"
	 */
	private int minSize;
	/**
	 * @uml.property  name="maxSize"
	 */
	private int maxSize;
	/**
	 * @uml.property  name="delta"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	private int delta;
	
	public MSER()
	{
		super();
		name = "MSER";
		description = "Looks for the Maximally Stable Extremal Regions in an image. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.FeatureExtract;
		
		ParamInt p1 = new ParamInt("mins", true, true);
		p1.setDefault(50);
		p1.setDescription("Minimum size of the MSERs");
		
		ParamInt p2 = new ParamInt("maxs", true, true);
		p2.setDefault(500);
		p2.setDescription("Maximum size of the MSERS");
		
		ParamInt p3 = new ParamInt("delta", true, true);
		p3.setDescription("Width of the intensity range (delta)");
		p3.setDefault(1);
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
	}
	
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("Function MSER can not be applied to this image format.");
		
		JIPBmpByte imgByte = null;	
		ImageType type = img.getType();
		
		if (type != ImageType.BYTE) {
			switch(type) {
				case BIT: throw new JIPException("This function requires a BYTE type image."); 
				case FLOAT:
				case SHORT: GrayToGray fgg = new GrayToGray();
							fgg.setParamValue("gray", ImageType.BYTE);
							imgByte = (JIPBmpByte)fgg.processImg(img);
							break;
				case COLOR: ColorToGray fcg = new ColorToGray();
							imgByte = (JIPBmpByte)fcg.processImg(img);
							break;
			}
		}
		else //If it's a BYTE type image, only copy the current img to imgByte.
		{
			imgByte = (JIPBmpByte)img.clone();
		}
		
		minSize = getParamValueInt("mins");
		maxSize = getParamValueInt("maxs");
		delta = getParamValueInt("delta");
		
		if (minSize < 1 || maxSize < 1 || maxSize <= minSize) {
			throw new JIPException("The value of the parameter mins must be greater than 0 and lesser than maxs.");
		}
		
		int height = img.getHeight();
		int width = img.getWidth();
		
		// Output generation
		JIPBmpColor result = new JIPBmpColor(width, height);
		result.setAllPixelsByte(0, imgByte.getAllPixelsByte());
		result.setAllPixelsByte(1, imgByte.getAllPixelsByte());
		result.setAllPixelsByte(2, imgByte.getAllPixelsByte());
		// From lowest to highest intensity
		processMSER(imgByte, result, width, height);
		
		// From highest to lowest intensity
		Negate fn = new Negate();
		JIPImage negated = fn.processImg(imgByte);
		JIPBmpByte negatedByte = (JIPBmpByte)negated;
		processMSER(negatedByte, result, width, height);
		
		// All MSERS are stored in the MSERS ArrayList, that can be accessed
		// from this method
		
		return result;
	}
	
	
	/**
	 * Method which process the MSER function.
	 * @param imgByte The input image.
	 * @param result The output image.
	 * @param width The image width.
	 * @param height The image height.
	 * @throws JIPException
	 */
	private void processMSER(JIPBmpByte imgByte, JIPBmpColor result, int width, int height) throws JIPException {
		DisjointSetNode [][]allPixels = new DisjointSetNode[height][width];
		
		// The data structure for the pixels is initialized
		for (int y=0; y < height; y++)
		{
			for (int x=0; x < width; x++)
			{
				allPixels[y][x] = new DisjointSetNode(x,y,(int)imgByte.getPixel(x,y));
			}
		}
		// The pixels are ordered by intensity
		ArrayList<DisjointSetNode>[] orderedPixels = binsort(allPixels, height, width);
		
		// Main loop - looking for MSERs
		for (int i=0; i < MAX_VALUE_PIXEL; i++)
		{
			for (int j=0; j < orderedPixels[i].size(); j++)
			{
				DisjointSetNode currentPixel = orderedPixels[i].get(j);
				currentPixel.sizes = new ArrayList<Integer>();
				currentPixel.sizes.add(i);
				currentPixel.sizes.add(1); // A region of size 1
				
				int x = currentPixel.x;
				int y = currentPixel.y;
				
				// We check if any connected component must be joined with the new region
				if (x > 0 && allPixels[y][x-1].find().sizes != null && allPixels[y][x-1].find() != currentPixel.find())
				{
					mergeComponents(currentPixel, allPixels[y][x-1], i);
				}
				
				if (x < width-1 && allPixels[y][x+1].find().sizes != null && allPixels[y][x+1].find() != currentPixel.find())
				{
					mergeComponents(currentPixel, allPixels[y][x+1], i);
				}
				
				if (y > 0 && allPixels[y-1][x].find().sizes != null && allPixels[y-1][x].find() != currentPixel.find())
				{
					mergeComponents(currentPixel, allPixels[y-1][x], i);
				}
				
				if (y < height-1 && allPixels[y+1][x].find().sizes != null && allPixels[y+1][x].find() != currentPixel.find())
				{
					mergeComponents(currentPixel, allPixels[y+1][x], i);
				}
				
			}
		}	
	
		for (int i=0; i < MSERS.size(); i++)
		{
			ArrayList<Pixel> boundary = new ArrayList<Pixel>();
			// We get all the pixels inside each MSER and its boundary to draw it. These two
			// lines are the most computationally expensive, and are not part of the original
			// algorithm
			getMSER((JIPBmpByte)imgByte.clone(), new JIPBmpFloat(imgByte.getWidth(), imgByte.getHeight()), MSERS.get(i).x, MSERS.get(i).y, MSERS.get(i).threshold, boundary);
			drawMSER(boundary, result);
		}
	}
	
	/** 
	 * Method which draws the boundary of a MSER.
	 * @param mp List of pixels to draw.
	 * @param img Image where the pixels will be drawn.
	 */
	private void drawMSER(ArrayList <Pixel> mp, JIPBmpColor img)
	{
		try {
			while (mp.size() != 0)
			{
				Pixel p = mp.get(0);
				mp.remove(0);
				img.setPixel(0,p.x,p.y,0);
				img.setPixel(1,p.x,p.y,255);
				img.setPixel(2,p.x,p.y,0);
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Function which gets the boundary and all the pixels inside a MSER. 
	 * This method is a modification of the flood fill algorithm.
	 * @param imgByte The input image.
	 * @param inner The output image.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param t A pixel value.
	 * @param boundary An array with the boundary values.
	 * @return An array with pixels.
	 */
	private ArrayList<Pixel> getMSER(JIPBmpByte imgByte, JIPBmpFloat inner, int x, int y, int t, ArrayList<Pixel> boundary)
	{
		ArrayList<Pixel> result = new ArrayList<Pixel>();
		try {
			ArrayList<Pixel> queue = new ArrayList<Pixel>();
			if (imgByte.getPixel(x, y) >= t)
				return result;
			queue.add(new Pixel(x,y));
			
			while (queue.size() != 0)
			{
				Pixel p = queue.get(0);
				int xn = p.x;
				int yn = p.y;
				queue.remove(0);
				
				if (imgByte.getPixel(xn,yn) < t && inner.getPixel(xn, yn) != INNER)
				{
					inner.setPixel(xn,yn,INNER);
					result.add(new Pixel(xn,yn));
					if (xn > 0) {
						if (imgByte.getPixel(xn-1, yn) < t && inner.getPixel(xn-1,yn)!= INNER) {
							queue.add(new Pixel(xn-1,yn));
						} else if (inner.getPixel(xn-1,yn) != INNER){
							boundary.add(new Pixel(xn-1,yn));
						}
					}
					if (xn < imgByte.getWidth() - 1)
					{
						if (imgByte.getPixel(xn+1, yn) < t && inner.getPixel(xn+1,yn) != INNER) {
							queue.add(new Pixel(xn+1,yn));
						} else if (inner.getPixel(xn+1,yn) != INNER){
							boundary.add(new Pixel(xn+1,yn));
						}
					}
					if (yn > 0) {
						if (imgByte.getPixel(xn,yn-1)<t && inner.getPixel(xn,yn-1) != INNER) {
							queue.add(new Pixel(xn,yn-1));
						} else if (inner.getPixel(xn,yn-1) != INNER){
							boundary.add(new Pixel(xn,yn-1));
						}
					}
					if (yn < imgByte.getHeight() - 1) {
						if (imgByte.getPixel(xn,yn+1)<t && inner.getPixel(xn,yn+1) != INNER) {
							queue.add(new Pixel(xn,yn+1));
						} else if (inner.getPixel(xn,yn+1) != INNER){
							boundary.add(new Pixel(xn,yn+1));
						}
					}
					
				}
				else
				{
					result.add(new Pixel(xn,yn));
				}
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Checks if two connected components must be joined; in this case, the smallest one 
	 * is removed, and we look for MSERS in its size array.
	 * @param cn The first component.
	 * @param n The second component.
	 * @param intensity The intensity.
	 */
	private void mergeComponents (DisjointSetNode cn, DisjointSetNode n, int intensity) {
		// We join the two connected components and we see which of the two components roots
		// is the root of the new connected component
		DisjointSetNode previouscn = cn.find(), previousn = n.find();
		
		DisjointSetNode newRoot = cn.union(n);
		DisjointSetNode parent, son;
		if (newRoot == previouscn)
		{
			parent = previouscn;
			son = previousn;
		}
		else
		{
			parent = previousn;
			son = previouscn;
		}
		
		// We look for new MSERS in son sizes array (local minimal in sizes change rate)
		// First a vector of change rates is built
		int changeRates = son.sizes.get(son.sizes.size()-2) - son.sizes.get(0) -2*delta;
		if (changeRates > 2)
		{
			int []rates = new int[changeRates];
			for (int i=0; i < changeRates; i++)
			{
				rates[i] = son.sizes.get(2*i + 1 + 2*delta*2) - son.sizes.get(2*i+1);
			}
		    // And next we look for local minimal in change rate
			int prev = rates[0];
			int i = 1;
			while (rates[i] == prev && i <changeRates-1)
					i++;
			while (i < changeRates-1)
			{
				if (rates[i] != rates[i+1])
				{
					if (rates[i] < prev && rates[i] < rates[i+1])
					{
						// Only MSERS in the range of sizes [minSize, maxSize] are stored]
						int mserSize = son.sizes.get(2*(i + delta) + 1);
						if (mserSize >= minSize && mserSize <= maxSize)
						{
							// 	NEW MSER
							MyMSER newMSER = new MyMSER(son.x, son.y, son.sizes.get(0) + i + delta+1,mserSize);
							// The intensity threshold is an upper threshold; a MSER is composed by
							// all pixels surrounding the centroid which intensity is less than it
							MSERS.add(newMSER);
						}
					}
					prev = rates[i];
				}
				i++;
			}
		}
		
		// The parent's sizes array is updated, while removing the son's one
		int parentLastIntensity = parent.sizes.get(parent.sizes.size()-2);
		int parentLastSize = parent.sizes.get(parent.sizes.size()-1);
		int sonLastSize = son.sizes.get(son.sizes.size()-1);
		if (parentLastIntensity == intensity)
			parent.sizes.set(parent.sizes.size()-1, parentLastSize + sonLastSize);
		else
		{
			int curInt = parentLastIntensity+1;
			while (curInt != intensity)
			{
				parent.sizes.add(curInt);
				parent.sizes.add(parentLastSize);
				curInt++;
			}
			parent.sizes.add(intensity);
			parent.sizes.add(parentLastSize + sonLastSize);
		}
		son.sizes = null;
	}
	
	/**
	 * Method which implements BINSORT or bucket algorithm. Complexity is O(n) since all 
	 * the possible values are in a limited range [0..255].
	 * @param allPixels A matrix of pixels.
	 * @param h The image height.
	 * @param w The image width.
	 * @return A vector of list of components.
	 */
	private ArrayList<DisjointSetNode>[] binsort(DisjointSetNode allPixels[][], int h, int w)
	{
		ArrayList<DisjointSetNode>[] ordered = new ArrayList[MAX_VALUE_PIXEL];
		
		for (int i=0; i < MAX_VALUE_PIXEL; i++)
			ordered[i] = new ArrayList<DisjointSetNode>();
		
		for (int y=0; y < h; y++)
			for (int x=0; x < w; x++)
				ordered[allPixels[y][x].index].add(allPixels[y][x]);
		
		return ordered;
	}
	
	
	/**
	 * Class disjoint set node.
	 */
	private class DisjointSetNode 
	{
		/**
		 * @uml.property  name="parent"
		 * @uml.associationEnd  
		 */
		DisjointSetNode parent;
		int index;
		int rank;
		int x;
		int y;
		ArrayList<Integer> sizes = null;
		
		
		public DisjointSetNode(int x, int y, int i) 
		{
			parent = this;
			index = i;
			rank = 0;
			this.x = x;
			this.y = y;
		}
		
		public DisjointSetNode find()
		{
			if (this!=this.parent)
				this.parent = this.parent.find();
			return this.parent;
		}
		
		public DisjointSetNode union(DisjointSetNode other)
		{
			DisjointSetNode A = this.find();
			DisjointSetNode B = other.find();
			
			if (A != B)
			{
				DisjointSetNode root;
				
				if (A.rank > B.rank)
				{
					B.parent = A;
					root = A;
				}
				else
				{
					A.parent = B;
					root = B;
				}
				if (A.rank == B.rank)
					B.rank++;
				
				return root;
			}
			else
				return A;
		}
	
	}
	
	/**
	 * Class Pixel 
	 */
	private class Pixel
	{
		public int x;
		public int y;
		
		public Pixel(int newx, int newy)
		{
			x = newx;
			y = newy;
		}
	}
	
	/**
	 * Class MSER
	 */
	private class MyMSER 
	{
		public int threshold;
		public int x;
		public int y;
		public int size;
		
		public MyMSER(int newX, int newY, int newT, int newSize)
		{
			x = newX;
			y = newY;
			threshold = newT;
			size = newSize;
		}
	}
	
}

