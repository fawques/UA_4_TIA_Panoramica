package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;

/**
 * Boolean Parameter
 * @author miguel
 */
public class ParamBool extends Parameter {
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 */
	private boolean defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private boolean value;
	
	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamBool (String n) {
		super(n);
		defValue=false;
		value=false;
	}

	/** 
	 * Constructor
	 * @param n Name
	 * @param req Required
	 * @param input Input
	 */
	public ParamBool (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=false;
		value=false;
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (boolean v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return Value
	 */
	public boolean getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (boolean v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public boolean getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.BOOL;
	}

}
