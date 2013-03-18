package javavis.jip2d.functions.adaBoost;

import java.io.Serializable;
import java.util.ArrayList;

import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;

public class strongLearner implements Serializable
{
	private static final long serialVersionUID = -4256682357166854638L;
	ArrayList<Double> Alfa;
	ArrayList<feature> weak;
	public int xSize, ySize; //size of training images
	double threshold;
	
	public strongLearner(int w, int h)
	{
		Alfa = new ArrayList<Double>();
		weak = new ArrayList<feature>();
		threshold = 0;
		xSize = w;
		ySize = h;
	}
	
	public void addWeak(feature w, double a)
	{
		int count, size;
		weak.add(w);
		Alfa.add(a);
		size = weak.size();
		threshold = 0;
		for(count=0;count<size;count++)
			threshold += Alfa.get(count);
		threshold *= 0.5;
	}
	
	public int classify(JIPBmpFloat img) throws JIPException
	{
		return classify(img, 0, 0);
	}

	public int classify(JIPBmpFloat img, int x, int y) throws JIPException
	{
		int ret = 0;
		double value = 0;
		int count, size;
		size = weak.size();
		for(count=0;count<size; count++)
			value += weak.get(count).classify(img, x, y) * Alfa.get(count);
		if(value >= threshold) ret = 1;
		return ret;
	}
	
	public int classify(trainExample img) throws JIPException 
	{
		return classify(img.integralImage);
	}

}
