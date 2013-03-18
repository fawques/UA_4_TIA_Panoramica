package javavis.jip2d.functions.adaBoost;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;

/**
 * This class represents a weak learner for adaBoost
 * It is implemented like image features described in Viola&Jones
 * To obtain the result of applying this feature to an image, this has to be converted to integral image
 * @author dviejo
 *
 */
public class feature implements Serializable
{
	private static final long serialVersionUID = 7016517645693864979L;
	int type;
//	int x, y;
	ArrayList<region> regions;
	int threshold;
	int parity;
	
	
	public feature(int w, int h, int type) throws Exception
	{
//		this.x = x; this.y = y;
		int x = -1;
		int y = -1; // ojo con esto

		this.type = type;
		regions = new ArrayList<region>();
		threshold = 0;
		parity = 1;
		
		int x1, y1, x2, y2;
		
		switch(type)
		{
			case 1:
				if(h%2!=0) throw new Exception("feature: is not posible to create this kind of feature");
				y1 = y + (h / 2);
				regions.add(new region(x, y, x+w, y1, 1));
				regions.add(new region(x, y1, x+w, y+h, -1));
				break;
			case 2:
				if(w%2!=0) throw new Exception("feature: is not posible to create this kind of feature");
				x1 = x + (w / 2);
				regions.add(new region(x, y, x1, y+h, 1));
				regions.add(new region(x1, y, x+w, y+h, -1));
				break;
			case 3:
				if(h%3!=0) throw new Exception("feature: is not posible to create this kind of feature");
				y1 = y + (h/3);
				y2 = y1 + (h/3);
				regions.add(new region(x, y, x+w, y1, 1));
				regions.add(new region(x, y1, x+w, y2, -1));
				regions.add(new region(x, y2, x+w, y+h, 1));
				break;
			case 4:
				if(w%3!=0) throw new Exception("feature: is not posible to create this kind of feature");
				x1 = x + (w / 3);
				x2 = x1 + (w / 3);
				regions.add(new region(x, y, x1, y+h, 1));
				regions.add(new region(x1, y, x2, y+h, -1));
				regions.add(new region(x2, y, x+w, y+h, 1));
				break;
			case 5:
				if(w%2!=0 || h%2!=0) throw new Exception("feature: is not posible to create this kind of feature");
				x1 = x + (w / 2);
				y1 = y + (h / 2);
				regions.add(new region(x, y, x1, y1, 1));
				regions.add(new region(x1, y, x+w, y1, -1));
				regions.add(new region(x, y1, x1, y+h, -1));
				regions.add(new region(x1, y1, x+w, y+h, 1));
				break;
		} //end switch
	} //end constructor
	
	public feature(int x, int y, feature f)
	{

		regions = new ArrayList<region>();
		for(region r: f.regions)
		{
			regions.add(new region(x, y, r));
		}

	} //end constructor

	/**
	 * Trains a region based weak learner to find threshold and parity. Training result depends on train example's weights
	 * @param trainSet
	 * @param W
	 * @return Error associated to this weak learner for the train set.
	 * @throws JIPException
	 */
	public double train(ArrayList<trainExample> trainSet, double []W) throws JIPException
	{
		int count, size;
		int value;
		int bestThreshold, bestParity;
		double errorsP1, errorsP2, bestErrors; //errors for parity 1 and -1
//		boolean exit = false;
		trainExample example;
		size = trainSet.size();
		int []classResult;
		int []types;
		int max, min;
		ArrayList<Integer> thresholds;
		
		thresholds = new ArrayList<Integer>();
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		classResult = new int[size];
		types = new int[size];
		for(count = 0; count < size; count++)
		{
			example = trainSet.get(count);
			value = 0;
			for(region r: regions)
				value += r.getValue(example.integralImage);
			classResult[count] = value;
			if(thresholds.indexOf(value)==-1) thresholds.add(value);
			if(value>max) max = value;
			if(value<min) min = value;
			types[count] = example.type;
		}
		
		bestThreshold = min-1;
		bestParity = 1;
		bestErrors = size;
		
int numFail1, numFail2;
int bestFails = 0;
//		for(int aux=min; aux<max&&!exit; aux++)
		for(int aux: thresholds)
		{
numFail1 = numFail2 = 0;
			errorsP1 = 0;	//parity 1
			errorsP2 = 0;	//parity -1
			for(count=0;count<size;count++)
			{
				value = classResult[count]<aux?1:0;
				if(value != types[count])
{
numFail1++;
					errorsP1+=W[count];
}
				else 
{
numFail2++;
					errorsP2+=W[count];
}
			}
			if(errorsP1<bestErrors)
			{
bestFails = numFail1;
				bestThreshold = aux;
				bestParity = 1;
				bestErrors = errorsP1;
			}
			if(errorsP2<bestErrors)
			{
bestFails = numFail2;
				bestThreshold = aux;
				bestParity = -1;
				bestErrors = errorsP2;
			}
//			if(errorsP1 == 0 || errorsP2 == 0)
//			{
//				bestErrors = 0;
//				exit = true;
//			}
		}
		threshold = bestThreshold;
		parity = bestParity;
		return bestErrors + bestFails;
	}

	public int classify(JIPBmpFloat img) throws JIPException
	{
		return classify(img, 0, 0);
	}

	public int classify(JIPBmpFloat img, int x, int y) throws JIPException
	{
		int ret = 0;
		for(region r: regions)
			ret += r.getValue(img, x, y);
		return parity*ret<parity*threshold?1:0;
	}
	
	public String toString()
	{
		String ret = "Feature thres: "+threshold+" parity: "+parity+"\n";
		for(region r: regions)
			ret += r.toString()+"\n";
		return ret;
	}
}
