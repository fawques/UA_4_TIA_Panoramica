package javavis.jip2d.util;

/**
 * Class to keep a circular mask
 */
public class Mask {
	/**
	 * @uml.property  name="radius"
	 */
	public int radius;
	/**
	 * @uml.property  name="maxArea"
	 */
	public int maxArea;
	/**
	 * @uml.property  name="mask" multiplicity="(0 -1)" dimension="2"
	 */
	public boolean mask[][];

	public Mask(int r) {
		radius = r;
		mask = new boolean[2 * r][2 * r];
		maxArea = 0;

		for (int i = 0; i < 2 * r; i++) {
			for (int j = 0; j < 2 * r; j++) {
				if ((i - r) * (i - r) + (j - r) * (j - r) < r * r) {
					mask[i][j] = true;
					maxArea++;
				}
				else mask[i][j] = false;
			}
		}
	}
}
