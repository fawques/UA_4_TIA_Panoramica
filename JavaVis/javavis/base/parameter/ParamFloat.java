package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;


/**
 * Float Parameter
 * @author miguel
 */
public class ParamFloat extends Parameter {
	public static final float MINVALUE = -Float.MAX_VALUE;
	public static final float MAXVALUE = Float.MAX_VALUE;
	public static final float DSTEP = 0.5f;
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 */
	private float defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private float value;

	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamFloat (String n) {
		super(n);
		defValue=0.0f;
		value=0.0f;
	}
	
	/** 
	 * Constructor
	 * @param n Name
	 * @param req Required
	 * @param input Input
	 */
	public ParamFloat (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=0.0f;
		value=0.0f;
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (float v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return  Value
	 * @uml.property  name="value"
	 */
	public float getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (float v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public float getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.FLOAT;
	}

}
