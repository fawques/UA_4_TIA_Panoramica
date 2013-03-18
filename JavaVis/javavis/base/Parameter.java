package javavis.base;

import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamImage;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamObject;
import javavis.base.parameter.ParamScrData;
import javavis.base.parameter.ParamString;


/**
 * Class to define the parameter object. 
 * A parameter can be: BOOL, INT, FLOAT, STRING, IMAGE, LIST, DIR and OBJECT, all of them 
 * defined in the ParameterType enum. 
 * A parameter can be required or no. In case of not required, the parameter can have a default 
 * assigned value. The basic constructor of parameter should specify its name, type and if it 
 * is required or not. That characteristics do not change during the object life. We can assign 
 * a description parameter which shows its operation. The parameter value can be get or set.
 */
public abstract class Parameter {
	/**
	 * Name of parameter
	 * @uml.property  name="name"
	 */
	String name;

	/**
	 * Indicates if the parameter is required
	 * @uml.property  name="required"
	 */
	boolean required;

	/**
	 * Indicates if the parameter has a value assigned
	 * @uml.property  name="assigned"
	 */
	protected boolean assigned;

	/**
	 * Description of the parameter
	 * @uml.property  name="description"
	 */
	String description;
	
	/**
	 * Indicates if it is a input (true) or output (false) parameter
	 * @uml.property  name="input"
	 */
	private boolean input;

	/**
	* Constructor.
	* @param n Name
	*/
	public Parameter(String n) {
		name = n;
		assigned = false;
		input = true;
		required = false; // Output parameters can not be required
	}
	
	/**
	* Constructor.
	* @param n Name
	* @param req Flag which indicates if it is required (true) or not (false).
	* @param input Flag which indicates if it is an input (true) or output (false) parameter.
	*/
	public Parameter(String n, boolean req, boolean input) {
		name = n;
		assigned = false;
		this.input = input;
		if (!input)
			required = false; // Output parameters can not be required
		else
			required = req;
	}
	
	public static Parameter newInstance (String n, boolean req, boolean input, ParamType pt) {
		switch (pt) {
			case BOOL: return new ParamBool(n, req, input);
			case DIR: return new ParamDir(n, req, input);
			case FILE: return new ParamFile(n, req, input);
			case FLOAT: return new ParamFloat(n, req, input);
			case IMAGE: return new ParamImage(n, req, input);
			case INT: return new ParamInt(n, req, input);
			case LIST: return new ParamList(n, req, input);
			case OBJECT: return new ParamObject(n, req, input);
			case SCRDATA: return new ParamScrData(n, req, input);
			case STRING: return new ParamString(n, req, input);
			default: return null;
		}
	}

	/**
	 * Gets the name of the parameter.
	 * @return  Name of parameter.
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * It gets the type of parameter.	
	 * @return  Type of parameter.
	 * @uml.property  name="type"
	 * @uml.associationEnd  readOnly="true"
	 */
	public abstract ParamType getType();

	/**
	 * Returns if the parameter is required or not.
	 * @return  true if the parameter is required else return false.
	 * @uml.property  name="required"
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Returns if the parameter value is assigned o no.	 
	 * @return  true if the parameter value is assigned else return false.
	 * @uml.property  name="assigned"
	 */
	public boolean isAssigned() {
		return assigned;
	}

	/**
	 * Sets a description.	 
	 * @param d  Description assigned to the parameter.
	 * @uml.property  name="description"
	 */
	public void setDescription(String d) {
		description = d;
	}

	/**
	 * It gets the description that assigns on the parameter.
	 * @return  Description assigns to the parameter.
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns if the parameter is a input or output parameter
	 * @return  true if input parameter, false else where
	 * @uml.property  name="input"
	 */
	public boolean isInput() {
		return input;
	}
}
