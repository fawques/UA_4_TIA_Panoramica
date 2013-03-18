package javavis.jip2d.base.geometrics;

import java.awt.Color;

public class Junction extends GeomData {
	
	// Center of the junction
	/**
	 * @uml.property  name="x"
	 */
	private int x;
	/**
	 * @uml.property  name="y"
	 */
	private int y;
	// Internal and external radius
	/**
	 * @uml.property  name="r_i"
	 */
	private int r_i;
	/**
	 * @uml.property  name="r_e"
	 */
	private int r_e;
	// Limits
	/**
	 * @uml.property  name="situation" multiplicity="(0 -1)" dimension="1"
	 */
	private int []situation;
	
	public Junction (int xi, int yi, int r_ii, int r_ei, int []situationi) {
		x=xi;
		y=yi;
		r_i=r_ii;
		r_e=r_ei;
		situation=situationi;
		color=null;
	}
	
	public Junction (int xi, int yi, int r_ii, int r_ei, int []situationi, Color colori) {
		x=xi;
		y=yi;
		r_i=r_ii;
		r_e=r_ei;
		this.situation = new int[situationi.length];
		for (int i=0; i<situationi.length; i++) 
			this.situation[i]=situationi[i];
		color=colori;
	}
	
	public Junction (Junction j) {
		x=j.x;
		y=j.y;
		r_i=j.r_i;
		r_e=j.r_e;
		situation=new int[j.situation.length];
		for (int i=0; i<j.situation.length; i++) 
			situation[i]=j.situation[i];
		color=j.color;
	}

	public String toString() {
		String s="x="+x+" y="+y;
		
		for (int i=0; i<situation.length; i++) {
			s+=" "+situation[i];
		}
		return s;
	}

	/**
	 * @return
	 * @uml.property  name="x"
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x
	 * @uml.property  name="x"
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return
	 * @uml.property  name="y"
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y
	 * @uml.property  name="y"
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return
	 * @uml.property  name="r_i"
	 */
	public int getR_i() {
		return r_i;
	}

	/**
	 * @param r_i
	 * @uml.property  name="r_i"
	 */
	public void setR_i(int r_i) {
		this.r_i = r_i;
	}

	/**
	 * @return
	 * @uml.property  name="r_e"
	 */
	public int getR_e() {
		return r_e;
	}

	/**
	 * @param r_e
	 * @uml.property  name="r_e"
	 */
	public void setR_e(int r_e) {
		this.r_e = r_e;
	}

	/**
	 * @return
	 * @uml.property  name="situation"
	 */
	public int[] getSituation() {
		return situation;
	}

	/**
	 * @param situation
	 * @uml.property  name="situation"
	 */
	public void setSituation(int[] situation) {
		this.situation = new int[situation.length];
		for (int i=0; i<situation.length; i++) 
			this.situation[i]=situation[i];
	}
}
