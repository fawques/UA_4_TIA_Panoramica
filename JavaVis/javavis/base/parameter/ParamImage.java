package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;
import javavis.jip2d.base.JIPImage;


/**
 * Integer Parameter
 * @author miguel
 */
public class ParamImage extends Parameter {
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 * @uml.associationEnd  
	 */
	private JIPImage defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 * @uml.associationEnd  
	 */
	private JIPImage value;

	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamImage (String n) {
		super(n);
		defValue=null;
		value=null;
	}
	
	/** 
	 * Constructor
	 * @param n Name
	 * @param req Required
	 * @param input Input
	 */
	public ParamImage (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=null;
		value=null;
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (JIPImage v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return  Value
	 * @uml.property  name="value"
	 */
	public JIPImage getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (JIPImage v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public JIPImage getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.IMAGE;
	}

}
