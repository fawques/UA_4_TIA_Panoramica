package javavis.base;

import javax.vecmath.Color3b;

public class ColorTools {

	public static Color3b convertIntIntoRGB(int col)
	{
		byte r,g,b;

		b = (byte) col;
		col = col >> 8;
		g = (byte) col;
		col = col >> 8;
		r = (byte) col;
		return new Color3b(r, g, b);
	}

	public static int convertRGBToInt(Color3b col)
	{
		int ret = col.x & 0xFF;
		ret = ret << 8;
		ret = ret | col.y & 0xFF;
		ret = ret << 8;
		ret = ret | col.z & 0xFF;

		return ret;
	}
}
