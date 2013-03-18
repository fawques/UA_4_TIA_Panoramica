package javavis.jip2d.util;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 * Auxiliary class to manage blobs.
 */
public class Blob {
	private static Logger logger = Logger.getLogger(Blob.class);
	
	/**
	 * @uml.property  name="list_x"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	public ArrayList<Integer> list_x;
	
	/**
	 * @uml.property  name="list_y"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
	public ArrayList<Integer> list_y;

	/**
	 * @uml.property  name="center_x"
	 */
	public int center_x;
	/**
	 * @uml.property  name="center_y"
	 */
	public int center_y;
	/**
	 * @uml.property  name="minx"
	 */
	public int minx;
	/**
	 * @uml.property  name="miny"
	 */
	public int miny;
	/**
	 * @uml.property  name="maxx"
	 */
	public int maxx;
	/**
	 * @uml.property  name="maxy"
	 */
	public int maxy;
	/**
	 * @uml.property  name="maxDistX"
	 */
	public int maxDistX;
	/**
	 * @uml.property  name="maxDistY"
	 */
	public int maxDistY;
	/**
	 * @uml.property  name="xsize"
	 */
	public int xsize;
	/**
	 * @uml.property  name="ysize"
	 */
	public int ysize;
	

	/**
	 * @uml.property  name="valido"
	 */
	public boolean valid=false;

	public Blob() {
		list_x = new ArrayList<Integer>();
		list_y = new ArrayList<Integer>();
	}
	
	public void calcEverything () {
		calcCentroid();
		xSize();
		ySize();
		calcDistMax();
	}
	
	public JIPImgBitmap getImage () {
		calcEverything();
		try {
			JIPBmpBit img = new JIPBmpBit(xsize, ysize);
			for (int i=0; i<list_x.size(); i++) 
				img.setPixelBool(list_x.get(i)-minx, list_y.get(i)-miny, true);
			return img;
		}catch (JIPException e){logger.error("Blob: "+e); return null;}
	}

	public void calcCentroid() {
		int l = list_x.size();

		if (l == 0) 
			valid = false;
		else {
			for (int i=0; i<l; i++) {
				center_x += list_x.get(i);
				center_y += list_y.get(i);
			}
			valid = true;
			center_x /= l;
			center_y /= l;
		}
	}
	
	public int length() {
		return list_x.size();
	}
	
	/**
	 * Calculates the maximum distance from the centroid to
	 * one pixel in the blob  
	 */
	private void calcDistMax () {
		maxDistX=0;
		maxDistY=0;
		calcCentroid();
		for (int i=0; i<list_x.size(); i++) {
			maxDistX = Math.max(maxDistX, Math.abs(center_x - list_x.get(i)));
			maxDistY = Math.max(maxDistY, Math.abs(center_y - list_y.get(i)));
		}
	}
	
	/**
	 * It returns the x size of the blob
	 */
	private void xSize () {
		minx=100000;
		maxx=0;
		int aux;

		for (int i=0; i<list_x.size(); i++) {
			aux=list_x.get(i);
			if (minx > aux) minx=aux;
			if (maxx < aux) maxx=aux;
		}
		
		xsize = maxx+1-minx;
	}
	
	/**
	 * It returns the y size of the blob
	 */
	private void ySize () {
		miny=100000;
		maxy=0;
		int aux;

		for (int i=0; i<list_y.size(); i++) {
			aux=list_y.get(i);
			if (miny > aux) miny=aux;
			if (maxy < aux) maxy=aux;
		}
		
		ysize = maxy+1-miny;
	}
}
