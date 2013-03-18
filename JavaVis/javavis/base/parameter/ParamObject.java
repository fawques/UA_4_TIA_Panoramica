package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;


/**
 * Integer Parameter
 * @author miguel
 */
public class ParamObject extends Parameter {
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 */
	private Object defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private Object value;

	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamObject (String n) {
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
	public ParamObject (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=null;
		value=null;
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (Object v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return  Value
	 * @uml.property  name="value"
	 */
	public Object getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (Object v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public Object getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.OBJECT;
	}

}
