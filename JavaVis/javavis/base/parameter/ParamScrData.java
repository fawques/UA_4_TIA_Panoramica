package javavis.base.parameter;

import javavis.base.Parameter;
import javavis.base.ParamType;
import javavis.jip3d.base.ScreenData;


/**
 * Integer Parameter
 * @author miguel
 */
public class ParamScrData extends Parameter {
	/**
	 * Default value
	 * @uml.property  name="defValue"
	 */
	private ScreenData defValue;
	
	/**
	 * Value of the parameter
	 * @uml.property  name="value"
	 */
	private ScreenData value;
	
	/** 
	 * Constructor
	 * @param n Name
	 */
	public ParamScrData (String n) {
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
	public ParamScrData (String n, boolean req, boolean input) {
		super(n, req, input);
		defValue=null;
		value=null;
	}
	
	/**
	 * Sets the parameter value
	 * @param v  Value
	 * @uml.property  name="value"
	 */
	public void setValue (ScreenData v) {
		value=v;
		assigned=true;
	}
	
	/**
	 * Gets the parameter value
	 * @return  Value
	 * @uml.property  name="value"
	 */
	public ScreenData getValue () {
		if (assigned) return value;
		else return defValue;
	}
	
	/**
	 * Sets the default parameter value
	 * @param v Value
	 */
	public void setDefault (ScreenData v) {
		defValue = v;
	}
	
	/**
	 * Returns the default parameter value
	 * @return Value
	 */
	public ScreenData getDefault () {
		return defValue;
	}
	
	public ParamType getType () {
		return ParamType.SCRDATA;
	}

}
