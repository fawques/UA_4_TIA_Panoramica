package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;


/**
 * Integer Parameter
 * @author miguel
 */
public class ParamInt extends Parameter {
	public static final int MINVALUE = Integer.MIN_VALUE;
	public static final int MAXVALUE = Integer.MAX_VALUE;
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 */
	private int defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private int value;

	/** 
	 * Constructor
	 * @param n Name
	 * @param req Required
	 * @param input Input
	 */
	public ParamInt (String n) {
		super(n);
		defValue=0;
		value=0;
		assigned=false;
	}
	
	/** 
	 * Constructor
	 * @param n Name
	 * @param req Required
	 * @param input Input
	 */
	public ParamInt (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=0;
		value=0;
		assigned=false;
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (int v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return  Value
	 * @uml.property  name="value"
	 */
	public int getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (int v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public int getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.INT;
	}

}
