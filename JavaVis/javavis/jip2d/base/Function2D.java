package javavis.jip2d.base;

import java.io.Serializable;
import java.util.ArrayList;
import javavis.base.JIPException;
import javavis.base.Parameter;
import javavis.base.ParamType;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamImage;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamObject;
import javavis.base.parameter.ParamString;

/**
 * Class to define function objects. A function is used to process an image 
 * (sequence) and to return an image (sequence). A function can have some additional 
 * parameters (input and/or output). Parameters can be get or set by name. Moreover, 
 * a function has a name and a description of what it does to the input image.
 * This class is abstract, so a function must implement this class.
 */
public abstract class Function2D implements Serializable {
	private static final long serialVersionUID = 6929836837647191255L;

	/**
	 * Name of the group at which this function is assigned.
	 * @uml.property  name="groupFunc"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	protected FunctionGroup groupFunc=FunctionGroup.Others;

	/**
	 * Name of the function.
	 * @uml.property  name="name"
	 */
	protected String name;

	/**
	 * Description of the function.
	 * @uml.property  name="description"
	 */
	protected String description;

	/**
	 * Array of Input and output parameters of the function.
	 * @uml.property  name="params"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.base.Parameter"
	 */
	private ArrayList<Parameter> params;

	/**
	 * Can contain some information at the end of the processing.
	 * @uml.property  name="info"
	 */
	protected String info;

	/**
	 * Contains the percentage of completeness in the current processing
	 * @uml.property  name="percProgress"
	 */
	protected int percProgress = 0;

	/**
	* Function constructor. 
	*/
	public Function2D() {
		params =  new ArrayList<Parameter>();
	}


	/**
	 * Gets the name of the function.
	 * @return  Name of the function.
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description of the function. 
	 * @return  Description of the function.
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	* Gets an output image from a input image. This method must be implemented and contains the
	* core of the function. 
	* @param img Input image.
	* @return Output image.
	*/
	public abstract JIPImage processImg(JIPImage img) throws JIPException;

	/**
	 * This is the default behavior when processing a sequence: every image in the sequence
	 * is processed when the processImg method. Thus, the result is a sequence with the same
	 * number of images than the input sequence. Every image in the new sequence is the result
	 * of applying the processImg method.
	 * This method can be overloaded if we wish to process the sequence in a different way.
	 * @param seq Input sequence.
	 * @return Output sequence.
	 */
	public Sequence processSeq(Sequence seq) throws JIPException {
		Sequence res = null;
		JIPImage imgRes;
		if (seq.getNumFrames() > 0) {
			res = new Sequence();
			for (JIPImage img : seq.getFrames()) {
				imgRes = processImg(img);
				if (imgRes != null) {
					imgRes.setName(img.getName());
					res.addFrame(imgRes);
				}
				else  res.addFrame(img.clone());	
			}
			res.setName(seq.getName());
		}
		return res;
	}

	/**
	 * Gets the number of input parameters of the function.	 
	 * @return Number of input parameters.
	 */
	public int getNumInputParams() {
		int cont=0;
		for (Parameter param : params) 
			if (param.isInput()) cont++;
		return cont;
	}

	/**
	 * Gets the number of output parameters of the function.	 
	 * @return Number of output parameters.
	 */
	public int getNumOutputParams() {
		int count=0;
		for (Parameter param : params) 
			if (!param.isInput()) count++;
		return count;
	}

	/**
	 * Gets all the input parameters name of the function.	 
	 * @return Array with input parameters name of the function.
	 */
	public String[] getInputParamNames() {
		String[] res = null;
		Parameter param;
		int nparams = getNumInputParams();
		if (nparams > 0)
			res = new String[nparams];
		for (int i = 0, j=0; i < params.size(); i++) {
			param = params.get(i);
			if (param.isInput()) 
				res[j++] = param.getName();
		}
		return res;
	}

	/**
	 * Gets all the output parameter name of the function.	 
	 * @return Array with output parameters name of the function.
	 */
	public String[] getOutputParamNames() {
		String[] res = null;
		Parameter param;
		int nparams = getNumInputParams();
		if (nparams > 0)
			res = new String[nparams];
		for (int i = 0; i < nparams; i++) {
			param = params.get(i);
			if (!param.isInput()) 
				res[i] = param.getName();
		}
		return res;
	}

	/**
	* Returns if there is or not a input parameter of the function with the given name.
	* @param nom Name to check.
	* @return true if the function has a parameter which is equal to nom, false elsewhere.  
	*/
	public boolean isInputParam(String nom) {
		for (Parameter p : params)
			if (p.isInput() && nom.equals(p.getName())) 
				return true;
		return false;
	}

	/**
	* Returns if there is or not a output parameter of the function with the given name.
	* @param nom Name to check.
	* @return true if the function has a output parameter which is equal to nom, false elsewhere.  
	*/
	public boolean isOutputParam(String nom) {
		for (Parameter p : params)
			if (!p.isInput() && nom.equals(p.getName())) 
				return true;
		return false;
	}

	/**
	* Returns if a input parameter of the function is required.	 
	* @param nom Name to check.
	* @return true if function has an input parameter which name is nom and this is required, false elsewhere. 
	*/
	public boolean isInputParamRequired(String nom) {
		for (Parameter p : params)
			if (p.isInput() && nom.equals(p.getName()) && p.isRequired())
				return true;
		return false;
	}

	/**
	* Returns if a output parameter of the function is required.	 
	* @param nom Name to check.
	* @return true if function has an output parameter which name is nom and this is required, false elsewhere. 
	*/
	public boolean isOutputParamRequired(String nom) {
		for (Parameter p : params)
			if (!p.isInput() && nom.equals(p.getName()) && p.isRequired())
				return true;
		return false;
	}

	/**
	 * Returns if a input parameter of the function has assigned a value.	 
	 * @param nom Name to check.
	 * @return true if function has a input parameter which name is nom and its value has been assigned 
	 * false elsewhere.
	 */
	public boolean isInputParamAssigned(String nom) {
		for (Parameter p : params)
			if (p.isInput() && nom.equals(p.getName()) && p.isAssigned())
				return true;
		return false;
	}

	/**
	 * Returns if a output parameter of the function has assigned a value.	 
	 * @param nom Name to check.
	 * @return true if function has a output parameter which name is nom and its value has been assigned 
	 * false elsewhere.
	 */
	public boolean isOutputParamAssigned(String nom) {
		for (Parameter p : params)
			if (!p.isInput() && nom.equals(p.getName()) && p.isAssigned())
				return true;
		return false;
	}

	/**
	* Gets the function parameter type.	
	* @param nom Parameter name.
	* @return Parameter type
	*/
	public ParamType getParamType(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName()))
				return p.getType();
		throw new JIPException("Function2D.getParamType: parameter not found");
	}

	/**
	 * Gets a description of the function parameter.
	 * @param nom Parameter name.
	 * @return Description of parameter. 	 
	 */
	public String getParamDescr(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName()))
				return p.getDescription();
		throw new JIPException("Function2D.getParamDesc: parameter not found");
	}

	/**
	 * Sets a boolean value in a parameter of the function.	 
	 * @param nom Name of parameter to assign (It must exist and be a BOOL type)
	 * @param v Value to assign.
	 */
	public void setParamValue(String nom, boolean v) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.BOOL) {
				((ParamBool)p).setValue(v);
				found=true;
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * Sets a integer value in a parameter of the function.
	 * @param nom Name of parameter to assign (It must exist and be a INT type)
	 * @param v Value to assign.
	 */
	public void setParamValue(String nom, int v) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.INT) {
				((ParamInt)p).setValue(v);
				found=true;
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * Sets a real value in a parameter of the function.	 
	 * @param nom Name of Parameter to assign (It should exist and be a FLOAT type)
	 * @param v Value to assign.
	 */
	public void setParamValue(String nom, float v) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.FLOAT) {
				((ParamFloat)p).setValue(v);
				found=true;
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * Sets a string value in a parameter of the function.	 
	 * @param nom Name of Parameter to assign (It must exist and be a STRING, FILE or DIR type)
	 * @param v Value to assign.
	 */
	public void setParamValue(String nom, String v) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (nom.equals(p.getName())) {
				switch (p.getType()) {
					case STRING: ((ParamString)p).setValue(v); 
								found=true; break;
					case FILE: ((ParamFile)p).setValue(v); 
								found=true; break;
					case DIR: ((ParamDir)p).setValue(v); 
								found=true; break;
					case LIST: ((ParamList)p).setValue(v); 
								found=true; break;
				}
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * Sets a string value in a parameter of the function.	 
	 * @param nom Name of Parameter to assign (It must exist and be a LIST type)
	 * @param v Value to assign.
	 */
	public void setParamValue(String nom, String []v) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (p.getType()==ParamType.LIST && nom.equals(p.getName())) {
				((ParamList)p).setDefault(v);
				found=true;
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * It assigns a value of the image in a parameter of the function.	 
	 * @param nom Name of parameter to assign (It must exist and be a IMAGE type)
	 * @param img Value to assign.
	 */
	public void setParamValue(String nom, JIPImage img) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.IMAGE) {
				((ParamImage)p).setValue(img);
				found=true;
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * It assigns a value of the image in a parameter of the function.	 
	 * @param nom Name of parameter to assign (It must exist and be a OBJECT type)
	 * @param img Value to assign.
	 */
	public void setParamValue(String nom, Object obj) throws JIPException {
		boolean found=false;
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.OBJECT) {
				((ParamObject)p).setValue(obj);
				found=true;
				break;
			}
		if (!found) 
			throw new JIPException("Function2D.setParamValue: parameter not found");
	}

	/**
	 * Gets a boolean value from a parameter of the function.	 
	 * @param nom Name of parameter to find (It must exist and be a BOOL type)
	 * @return Value found.
	 */
	public boolean getParamValueBool(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.BOOL) 
				return ((ParamBool)p).getValue();
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * Gets a integer value from a parameter of the function.
	 * @param nom Name of parameter to find (It must exist and be a INT type)
	 * @return Value found
	 */
	public int getParamValueInt(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.INT) 
				return ((ParamInt)p).getValue();
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * Gets a float value from a parameter of the function.	 
	 * @param nom Name of Parameter to find (It must exist and be a FLOAT type)
	 * @return v Value found.
	 */
	public float getParamValueFloat(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.FLOAT) 
				return ((ParamFloat)p).getValue();
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * Gets a string value from a parameter of the function.	 
	 * @param nom Name of Parameter to found (It must exist and be a STRING, FILE or DIR type)
	 * @return Value found.
	 */
	public String getParamValueString(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName())) {
				switch (p.getType()) {
					case STRING: return ((ParamString)p).getValue();
					case FILE: return ((ParamFile)p).getValue();
					case DIR: return ((ParamDir)p).getValue();
					case LIST: return ((ParamList)p).getValue();
					case IMAGE: return "";
				}
				break;
			}
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * Gets a string value from a parameter of the function.	 
	 * @param nom Name of Parameter to find (It must exist and be a LIST type)
	 * @return Value found.
	 */
	public String[] getParamValueList(String nom) throws JIPException {
		for (Parameter p : params)
			if (p.getType()==ParamType.LIST && nom.equals(p.getName())) 
				return ((ParamList)p).getDefault();
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * Gets a value of the image from a parameter of the function.	 
	 * @param nom Name of parameter to find (It must exist and be a IMAGE type)
	 * @return Value found.
	 */
	public JIPImage getParamValueImg(String nom) throws JIPException {
		for (Parameter p : params) 
			if (nom.equals(p.getName()) && p.getType() == ParamType.IMAGE) 
				return ((ParamImage)p).getValue();
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * Gets a value of the image from a parameter of the function.	 
	 * @param nom Name of parameter to find (It must exist and be a OBJECT type)
	 * @return Value found.
	 */
	public Object getParamValueObj(String nom) throws JIPException {
		for (Parameter p : params)
			if (nom.equals(p.getName()) && p.getType() == ParamType.OBJECT) 
				return ((ParamObject)p).getValue();
		throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	/**
	 * It checks if every function the required parameters have an assigned value.	 
	 * @return true if every function required parameter have some assigned value.
	 * or if the function does not have parameters. Else return false.
	 */
	public boolean paramsOK() {
		for (Parameter p : params)
			if (p.isRequired() && !p.isAssigned())
				return false;
		return true;
	}
	
	/**
	 * It returns if there was some information when function was used.	 
	 * @return true if there was some information false elsewhere.
	 */
	public boolean isInfo() {
		return info!=null;
	}
	
	/**
	 * It returns the information message of the function.	 
	 * @return  The generated information.
	 * @uml.property  name="info"
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * It returns the group at which this function is assigned.	 
	 * @return  A FunctionGroup indicating the group.
	 * @uml.property  name="groupFunc"
	 */
	public FunctionGroup getGroupFunc() {
		return groupFunc;
	}

	/**
	 * It returns the group at which this function is assigned.	 
	 * @param gf  A integer indicating the group.
	 * @uml.property  name="groupFunc"
	 */
	public void setGroupFunc(FunctionGroup gf) {
		groupFunc = gf;
	}

	/**
	 * Returns the percentage of completeness	 
	 * @return Integer.
	 */
	public int getProgress() {
		return percProgress;
	}

	/**
	 * Add a new parameter to the parameter array 	 
	 */
	public void addParam(Parameter p) {
		params.add(p);
	}
}
