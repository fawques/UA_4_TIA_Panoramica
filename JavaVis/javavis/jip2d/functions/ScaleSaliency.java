package javavis.jip2d.functions;

import java.util.ArrayList;
import java.lang.Math;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.*;
import javavis.jip2d.base.geometrics.*;
import javavis.jip2d.base.*;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * It estimates the saliency of each pixel of the image at an only scale using Kadir and 
 * Brady method.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type and it has to be a sequence.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>seq</em>: The input sequence. Each image has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>s0</em>: Integer value which indicates the initial scale. It has to be greater than 4 (default 5).</li>
 * <li><em>sf</em>: Integer value which indicates the final scale. It has to be greater than s0+1 (default 20).</li>
 * <li><em>%feat</em>: Integer value which indicates the percentage of most salient features
 * to show (default 5).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image representing the saliency map.</li>
 * </ul><br />
 */
public class ScaleSaliency extends Function2D {
	
	private static final long serialVersionUID = -5543080812213811142L;
	
	// clustering constants
	/**
	 * @uml.property  name="k"
	 */
	private int K = 3;
	/**
	 * @uml.property  name="vTh"
	 */
	private int vTh = 70;

	
	// Constructor
	public ScaleSaliency() {
		super();
		name = "ScaleSaliency";
		description = "Kadir and Brady feature extraction algorithm based on entropy. Applies to COLOR, BYTE, SHORT or FLOAT type and it must to be a sequence.";
		groupFunc = FunctionGroup.FeatureExtract;

		ParamInt p1 = new ParamInt("s0", true, true);
		p1.setDefault(5);
		
		p1.setDescription("Initial scale (>4)");
		ParamInt p2 = new ParamInt("sf", true, true);
		p2.setDefault(20);
		
		p2.setDescription("Final scale (>(s0+1))");
		ParamInt p3 = new ParamInt("%feat",true,true);
		p3.setDefault(5);
		p3.setDescription("% of most salient features to show");

		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	// This funcion is only valid for sequences containing an only frame, so this method
	// is empty
	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException("This algorithm must be used with a sequence having an only frame");
	}
	
	// Main method
	public Sequence processSeq(Sequence seq) throws JIPException 
	{
		if (seq.getNumFrames() > 1)
			throw new JIPException("This algorithm must be used with a sequence having an only frame");
		
		JIPImage img = (seq.getFrames()).get(0);
		
		// Parameter checking
		if (img instanceof JIPImgGeometric) 
			throw new JIPException("This algorithm can not be applied to this image format");
		
		int s0 = getParamValueInt("s0");
		if (s0 < 5)
			throw new JIPException("S0 value must be greater than 4.");
		
		int sf = getParamValueInt("sf");
		if (sf <= s0 + 1)
			throw new JIPException("Sf must be greater than s0 + 1.");
		
		int percFeat = getParamValueInt("%feat");
		if (percFeat <= 0 || percFeat > 100)
			throw new JIPException("The % of most salient features to show must be in the range ]0,100]");
		
		ImageType type = img.getType();
		int bins = 1000;
		int width = img.getWidth();
		int height = img.getHeight();

		JIPBmpFloat imgByte = null;
		
		if (type != ImageType.FLOAT) {
			switch(type) {
				case BIT: throw new JIPException("This function requires a BYTE type image"); 
				case BYTE:
				case SHORT: GrayToGray fgg = new GrayToGray();
							fgg.setParamValue("gray", "FLOAT");
							imgByte = (JIPBmpFloat)fgg.processImg(img);
							break;
				case COLOR: ColorToGray fcg = new ColorToGray();
							fcg.setParamValue("gray", "FLOAT");
							imgByte = (JIPBmpFloat)fcg.processImg(img);
							break;
			}
		}
		
		ArrayList<ArrayList<Coord>> masks = createCumulativeMasks(s0,sf);
		
		// Initialization
		int i;
		Sequence resSeq = new Sequence();
		JIPBmpFloat result = new JIPBmpFloat(sf-s0+2,width,height);
		JIPGeomPoly features = new JIPGeomPoly(width,height);
		resSeq.addFrame(features);
		float []lookup = createLookUp(bins);
		int totalSizes[] = new int[sf-s0+1];
		totalSizes[0] = masks.get(0).size();
		float []maxEntropies = new float[sf-s0+1];
		PriorityQueue<Feature> feat = new PriorityQueue<Feature>(1,new FeatureComparator());
		for (i=1; i < sf-s0+1; i++)
		{
			totalSizes[i] = totalSizes[i-1] + masks.get(i).size();
			maxEntropies[i] = 0;
		}
		
		// Scale saliency computation
		for (int y=sf-1; y < height-sf+1; y++)
			for (int x=sf-1; x < width-sf+1; x++)
			{
				double [][]hist = new double[sf-s0+1][128];
				double []entropies = new double[sf-s0+1];
				for (i=0; i<128; i++)
					hist[0][i] = 0;
				// Saliency computation
				for (int s=s0; s <= sf; s++)
				{
					int currentScale = s-s0;
					
					if (s > s0) 
						for (i=0; i < 128; i++)
							hist[currentScale][i] = hist[currentScale-1][i];
					for (i=0; i < masks.get(currentScale).size(); i++)
						hist[currentScale][(int)(imgByte.getPixel(x+((Coord)masks.get(currentScale).get(i)).x, y+((Coord)masks.get(currentScale).get(i)).y)*255)/2]++;
					float entropy = 0;
					for (i=0; i < 128; i++)
						if (hist[currentScale][i] != 0)
						{
							entropy -= lookup[(int)Math.floor(hist[currentScale][i]/(float)totalSizes[currentScale]*bins)];
						}
					result.setPixelFloat(currentScale+1, x, y,entropy);
					if (entropy > maxEntropies[currentScale])
						maxEntropies[currentScale] = entropy;
					entropies[currentScale] = entropy;
				}
				
				// Entropy peaks search and entropy weighting
				for (int s=s0+1; s < sf; s++)
				{
					int currentScale = s - s0;
					if (entropies[currentScale]>entropies[currentScale+1] && entropies[currentScale]>entropies[currentScale-1])
					{
						double weight = 0;
						for (i=0;i<128;i++)
							weight += Math.abs(hist[currentScale][i]/(float)totalSizes[currentScale] - hist[currentScale-1][i]/(float)totalSizes[currentScale-1]);
						weight *= s*s/(float)(2*s-1);
						feat.add(new Feature(x,y,s,weight*entropies[currentScale]));
					}
				}
			}
		
		// Clustering
		ArrayList<Feature> clustFeat = clustering(feat, percFeat);

		// Results ares showed
		ArrayList<Polygon2D> vecPun = new ArrayList<Polygon2D>();
		for (i=0; i < clustFeat.size(); i++)
		{
			Feature f = clustFeat.get(i);
			vecPun.add(genCircumference(f.x,f.y,f.scale));
		}
		features.setData(vecPun);
		
		result.setAllPixelsFloat(0, imgByte.getAllPixelsFloat());
		for (i=s0; i <= sf; i++)
			for (int y=sf-1; y < height-sf+1; y++)
				for (int x=sf-1; x < width-sf+1; x++)
					result.setPixelFloat(i-s0+1, x, y,result.getPixelFloat(i-s0+1, x, y)/maxEntropies[i-s0]);
		
		resSeq.addFrame(result);
		
		return resSeq;
	}
	
	/**
	 * Method which calculates the pixels to consider at maximum scale to calculate saliency.
	 * @param scale The scale.
	 * @return A list of coordinates.
	 */
	private ArrayList<Coord> createMask(int scale)
	{
		int maxDist = (scale-1)*(scale-1);
		ArrayList<Coord> mask = new ArrayList<Coord>();
		
		for (int y=-(scale-1); y <= scale-1; y++)
			for (double x=-(scale-1); x <= scale-1; x++)
			{
				double newX = 0;
				if (x != 0) newX = Math.abs(x) - 0.5;
				if ((y*y + newX*newX) <= maxDist)
				{
					Coord c = new Coord((int)x,y);
					mask.add(c);
				}
			}

		return mask;
	}
	
	/**
	 * Method used during mask creation.
	 * @param a A list of list of coordinates.
	 * @param s The maximum position where the function search.
	 * @param c The coordinate to search.
	 * @return A boolean indicating true if a pixel on a mask at scale s is also present 
	 * at scale s-1.
	 */
	private boolean contains(ArrayList<ArrayList<Coord>> a, int s, Coord c)
	{
		boolean count = false;
		int i = 0;
		int j = 0;
		
		while (!count &&  j < s)
		{
			if (a.get(j).get(i).equals(c)) count = true;
			else i++;
			
			if (i >= a.get(j).size()) {j++; i=0;}
		}
		
		return count;
	}
	
	/**
	 * Method which calculates the pixels to consider at each scale during saliency 
	 * calculation.
	 * @param s0 The initial position for calculating.
	 * @param sf The final position for calculating.
	 * @return A list of list of coordinates.
	 */
	private ArrayList<ArrayList<Coord>> createCumulativeMasks(int s0, int sf)
	{
		ArrayList<ArrayList<Coord>> masks = new ArrayList<ArrayList<Coord>>();
		
		masks.add(createMask(s0));
		
		for (int scale=s0+1;scale<=sf;scale++)
		{
			masks.add(new ArrayList<Coord>());
		
			int maxDist = (scale-1)*(scale-1);
			for (int y=-(scale-1);y<=scale-1;y++)
				for (double x=-(scale-1); x <= scale-1; x++)
				{
					double newX = 0;
					if (x != 0) newX = Math.abs(x) - 0.5;
					Coord c = new Coord((int)x,y);
					if ((y*y + newX*newX) <= maxDist && !contains(masks,scale-s0,c))
						masks.get(scale-s0).add(c);
				}
		}

		return masks;
	}
	
	/**
	 * Method which creates a look up table to avoid calculating logarithms during scale 
	 * saliency algorithm.
	 * @param bins The bins.
	 * @return A vector of real values.
	 */
	private float[] createLookUp(int bins)
	{
		float value;
		
		float []lookup = new float[bins+1];
		lookup[0] = 0;
		for (int i=1; i <= bins; i++)
		{
			value = i/(float)bins;
			lookup[i] = value*(float)Math.log(value);
		}
			
		return lookup;
	}

	/**
	 * Method which generates a circumference.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param radius The radius of the circumference.
	 * @return A circumference.
	 */
	private Polygon2D genCircumference(int x, int y, int radius) {
		Polygon2D circ;
		final float INC_ANG = 10f;
		
		circ = new Polygon2D();
		for(float ang=0f; ang < 360; ang+=INC_ANG) {
			Point2D aux = new Point2D((int)(x + Math.cos(Math.toRadians(ang))*radius), (int)(y + Math.sin(Math.toRadians(ang))*radius));
			circ.addPoint(aux);
		}
		
		return circ;
	}
	
	/**
	 * Method which clusters close regions corresponding to the most salient features on
	 * the image.
	 * @param feat A priority queue with the feature information.
	 * @param p3 A multiplier factor.
	 * @return A vector of real values.
	 */
	private ArrayList<Feature> clustering(PriorityQueue <Feature> feat, int p3)
	{
		int i, j;
		int totalFeatures = feat.size();
		ArrayList<Feature> selectedFeat = new ArrayList<Feature>();
		int featuresToShow = totalFeatures*p3/100;
		for (i=0; i < featuresToShow; i++)
			selectedFeat.add(feat.poll());
		ArrayList<Feature> clustFeat = new ArrayList<Feature>();
		// 	Clustering
		for (i=0; i < featuresToShow; i++)		
		{
			Feature f = selectedFeat.get(i);
			// 	Search of the k nearest neighbors
			PriorityQueue<Feature> knn = new PriorityQueue<Feature>(1,new FeatureDistanceComparator());
			for (j=0; j < featuresToShow; j++)
				if (j != i)
				{
					Feature f2 = selectedFeat.get(j);
					double distance = Math.sqrt(Math.pow(f2.x - f.x,2) + Math.pow(f2.y - f.y,2));
					f2.distance = distance;
					knn.add(f2);
				}
			// 	Find distance to regions already clustered
			double D = Double.MAX_VALUE;
			for (j=0; j < clustFeat.size(); j++)
			{
				Feature f2 = clustFeat.get(j);
				double distance = Math.sqrt(Math.pow(f2.x - f.x,2) + Math.pow(f2.y - f.y,2));
				if (distance < D)
					D = distance;
			}
			// 	Mean scale and center variance
			double meanScale = f.scale;
			double meanX = f.x;
			double meanY = f.y;
			ArrayList <Coord> centers = new ArrayList<Coord>();
			for (j=0; j < K; j++)
			{
				Feature f2 = knn.poll();
				centers.add(new Coord(f2.x,f2.y));
				meanX += f2.x;
				meanY += f2.y;
				meanScale += f2.scale;
			}
			meanX = meanX/(double)(K+1);
			meanY = meanY/(double)(K+1);
			meanScale = meanScale / (double)(K+1);
			
			double variance = Math.pow(meanX - f.x, 2) + Math.pow(meanY - f.y, 2);
			for (j=0; j < K; j++)
			{
				Coord c = centers.get(j);
				variance += Math.pow(meanX - c.x, 2) + Math.pow(meanY - c.y, 2);
			}
			// 	If the cluster is not redundant, it is added
			if (D > meanScale && variance < vTh)
			{
				Feature f2 = new Feature((int)meanX, (int)meanY, (int)meanScale, 0.0);
				clustFeat.add(f2);
			}
			
		}
		
		return clustFeat;
	}
	
	
	/**
	 * Class coordinate
	 */
	private class Coord {
		public int x;
		public int y;
		
		public Coord(int xnew, int ynew)
		{
			x = xnew; y = ynew;
		}
		
		boolean equals(Coord c)
		{
			return (c.x == this.x && c.y == this.y);
		}
	}
	
	/**
	 * Class Feature
	 */
	private class Feature {
		public double entropy;
		public int x;
		public int y;
		public int scale;
		public double distance;
		
		public Feature(int newX, int newY, int newScale, double newEntropy)
		{
			x = newX;
			y = newY;
			scale = newScale;
			entropy = newEntropy;
		}
	}
	
	/**
	 * Class feature comparator
	 */
	private class FeatureComparator  implements Comparator<Feature>
	{
		public FeatureComparator()
		{
			super();
		}
		
		public int compare(Feature o1, Feature o2)
		{
			if (o1.entropy > o2.entropy)
				return -1;
			else if (o1.entropy < o2.entropy)
				return 1;
			else return 0;
		}
	}
	
	/**
	 * Class feature distance comparator
	 */
	private class FeatureDistanceComparator implements Comparator<Feature>
	{
		public FeatureDistanceComparator()
		{
			super();
		}
		
		public int compare(Feature o1, Feature o2)
		{
			if (o1.distance < o2.distance)
				return -1;
			else if (o1.distance > o2.distance)
				return 1;
			else return 0;
		}
	}

}

