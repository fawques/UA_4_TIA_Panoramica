package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;


/**
 * Integer Parameter
 * @author miguel
 */
public class ParamList extends Parameter {
	/**
	 * Default value
	 * @uml.property  name="defValue" multiplicity="(0 -1)" dimension="1"
	 */
	private String[] defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private String value;

	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamList (String n) {
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
	public ParamList (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=null;
		value=null;
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
		else return defValue[0];
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (String[] v) {
		defValue = new String[v.length];
		for (int i=0; i<v.length; i++) 
			defValue[i]=v[i];
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public String[] getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.LIST;
	}

}
