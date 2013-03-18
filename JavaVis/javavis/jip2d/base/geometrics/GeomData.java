package javavis.jip2d.base.geometrics;

import java.awt.Color;

public abstract class GeomData {

	/**
	 * @uml.property  name="color"
	 */
	Color color;

	/**
	 * @return
	 * @uml.property  name="color"
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color
	 * @uml.property  name="color"
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	
}
