package javavis.jip2d.functions.adaBoost;

import java.io.Serializable;

import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;


/**
 * Class region is used to ease integral image computations
 * @author dviejo
 *
 */
public class region implements Serializable
{
	private static final long serialVersionUID = -2481739683811622087L;
	int x1, y1, x2, y2;
	int value;
	
	public region(int xa, int ya, int xb, int yb, int v)
	{
		x1 = xa; y1 = ya; x2 = xb; y2 = yb;
		value = v;
	}
	
	public region(int dx, int dy, region r)
	{
		x1 = r.x1 + dx;
		y1 = r.y1 + dy;
		x2 = r.x2 + dx;
		y2 = r.y2 + dy;
		value = r.value;
	}

	public int getValue(JIPBmpFloat intImg) throws JIPException
	{
		return getValue(intImg, 0, 0);
	}

	public int getValue(JIPBmpFloat intImg, int x, int y) throws JIPException
	{
		int ax = x1 + x;
		int ay = y1 + y;
		int bx = x2 + x;
		int by = y2 + y;
		int ret = (int)(intImg.getPixel(bx, by));
		if(ax>-1 && ay>-1) 
			ret	+= (int)(intImg.getPixel(ax, ay));
		if(ax>-1)
			ret -= (int)(intImg.getPixel(ax, by));
		if(ay>-1)
			ret -= (int)(intImg.getPixel(bx, ay));
		
		return value*ret;
	}
	public String toString()
	{
		return "("+x1+","+y1+") ("+x2+","+y2+"): "+value;			
	}
}
