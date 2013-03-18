package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;


/**
 * Integer Parameter
 * @author miguel
 */
public class ParamString extends Parameter {
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 */
	private String defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private String value;

	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamString (String n) {
		super(n);
		defValue="";
		value="";
	}
	
	/** 
	 * Constructor
	 * @param n Name
	 * @param req Required
	 * @param input Input
	 */
	public ParamString (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue="";
		value="";
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (String v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return  Value
	 * @uml.property  name="value"
	 */
	public String getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (String v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public String getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.STRING;
	}

}
