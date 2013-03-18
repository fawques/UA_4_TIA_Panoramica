package javavis.jip2d.util.sift;

import java.util.*;

public class HistoOrient {
	
	/**
	 * @uml.property  name="hist" multiplicity="(0 -1)" dimension="1"
	 */
	private float[] hist;
	
	/**
	 * @param bins number of intervals in the histogram
	 */
	public HistoOrient(int bins) {
		hist = new float[bins];
	}
	
	/**
	 * Increase the interval of the histogram. 
	 * @param orient Angle in radians 
	 * @param incr increment
	 */
	public void addBin(double orient, double incr) {
		int numBin;
		
		numBin = (int)(hist.length * orient / (2.0 * Math.PI));
		//Controls if the angle is 2PI
		if (numBin==hist.length)
			numBin--;
		hist[numBin]+=incr;
	}
	
	public float getBin(int i) {
		return hist[i];
	}
	
	/**
	 * Obtain the angle/angles where the histogram has its maximums.
	 * @param perc percentage which indicates the difference that a value could have with
	 * the maximum absolute to be considered like a maximum.
	 * @return
	 */
	public ArrayList<Float> getMax(float perc) {
		ArrayList<Float> max;
		float valMax;
		float angMax;
		int iBef, iNex;
		int lenHist;
		float value;
		
		lenHist = hist.length;
		max = new ArrayList<Float>();		
		valMax = Float.MIN_VALUE;
		angMax = 0.0f;
		//find maximum global
		for(int i=0; i<lenHist; i++) {
			if (hist[i]>valMax) {
				angMax = (float)(i * Math.PI * 2.0 / lenHist);
				valMax = hist[i];
			}
		}
		max.add(angMax);
		//find other maximum local, enough close to the maximum
		for(int i=0; i<lenHist; i++) {
			//find the neighbor index. After, see if it is a maximum local.
			if (i==0)
				iBef = lenHist-1;
			else
				iBef = i-1;
			if (i==lenHist-1)
				iNex = 0;
			else
				iNex = i+1;
			value = hist[i];
			
			if ((value>=hist[iNex])&&(value>hist[iBef])&&(value>valMax*perc/100.0f)) {
				max.add((float)(i * Math.PI * 2.0 / hist.length));
			}
		}
		return max;
	}

	public void smooth() {
		hist[0]=(hist[hist.length-1]+hist[0]+hist[1])/3;
		for (int i=1; i<hist.length; i++) 
			hist[i]=(hist[i-1]+hist[i]+hist[(i+1)%hist.length])/3;
	}

}