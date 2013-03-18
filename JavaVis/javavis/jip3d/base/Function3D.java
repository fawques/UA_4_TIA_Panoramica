package javavis.jip3d.base;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.ParamType;
import javavis.base.Parameter;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamEnum;
import javavis.base.parameter.ParamObject;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamImage;
import javavis.base.parameter.ParamScrData;
import javavis.base.parameter.ParamString;
import javavis.jip2d.base.JIPImage;
import javavis.jip3d.gui.MyCanvas3D;
import javavis.jip3d.gui.MyDialog;

/**
 * Abstract class Function3D, which implements from Thread. 
 * @author Diego Viejo
 */
public abstract class Function3D implements Runnable {

	/**
	 * @uml.property  name="blinker"
	 */
	protected volatile Thread blinker;

	/**
	 * @uml.property  name="scr_data"
	 * @uml.associationEnd  
	 */
	private ScreenData scr_data;

	/**
	 * @uml.property  name="dialog"
	 * @uml.associationEnd  
	 */
	public MyDialog dialog;

	/**
	 * @uml.property  name="result_list"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip3d.gui.ScreenData"
	 */
	public ArrayList<ScreenData> result_list;
	/**
	 * @uml.property  name="allowed_input"
	 */
	public int allowed_input;

	/**
	 * Parameter list
	 */
	public ArrayList<Parameter> param_list;
	/**
	 * @uml.property  name="param_names"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	public ArrayList<String> param_names;

	/**
	 * @uml.property  name="data_loaded"
	 */
	public boolean data_loaded;

	/**
	 * @uml.property  name="elapsed_time"
	 */
	public long elapsed_time;

	/**
	 * progress is the percentage of function already performed
	 * @uml.property  name="progress"
	 */
	public double progress;

	/**
	 * @uml.property  name="name"
	 */
	public String name;

	/**
	 * @uml.property  name="cancelled"
	 */
	public boolean cancelled;

	/**
	 * @uml.property  name="group"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public Function3DGroup group;

	/**
	 * @uml.property  name="canvas"
	 * @uml.associationEnd  
	 */
	private MyCanvas3D canvas;

	/**
	 * Abstract Constructor.
	 * @param d Dialog object for showing information about warnings, errors, etc.
	 */
	public Function3D()
	{
		name = "";
		param_list = new ArrayList<Parameter>();
		param_names = new ArrayList<String>();
		data_loaded = false;
		allowed_input = ScreenOptions.tALLTYPES;
		progress = 0;
		group = Function3DGroup.Others;
		canvas = null;
	}

    public void start() {
    	cancelled = false;
        blinker = new Thread(this, name);
        blinker.start();
    }

	public void stop() {
		cancelled = true;
        Thread tmpBlinker = blinker;
        blinker = null;
        if (tmpBlinker != null) {
           tmpBlinker.interrupt();
        }
    }

	public Thread getThread() {
		return blinker;
	}

	/**
	 * loadData must be called before start this thread.
	 * @param sd Input data for executing this function
	 */
	public boolean loadData(ScreenData sd)
	{
		if(sd!=null && (allowed_input & sd.getType()) != 0)
		{
			scr_data = sd;
			data_loaded = true;
		}
		else
			data_loaded = false;
		return data_loaded;
	}

	public void run()
	{
		elapsed_time = 0;
		try {
			long t1 = System.currentTimeMillis();
			proccessData(scr_data);
			long t2 = System.currentTimeMillis();
			elapsed_time = t2 - t1;
			progress = 100;
		} catch(JIPException e) {
			dialog.error(e.getMessage(), "Error: "+name+"::run");
		}

		return;
	}

	public abstract void proccessData(ScreenData scr_data) throws JIPException;

	public void addParam(Parameter p)
	{ 
		int pos = param_names.indexOf(p.getName());
		if(pos==-1)
		{
			param_list.add(p);
			param_names.add(p.getName());
		}
		else
			dialog.error("Parameter name already exists", "Function3D::addParam Error");
	}

	public int getParamValueInt(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.INT)
				return ((ParamInt)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type integer");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public double getParamValueFloat(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.FLOAT)
				return ((ParamFloat)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type float");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public boolean getParamValueBool(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.BOOL)
				return ((ParamBool)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type boolean");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public String getParamValueString(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.STRING)
				return ((ParamString)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type string");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public ScreenData getParamValueScrData(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.SCRDATA)
				return ((ParamScrData)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type screendata");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public Object getParamValueObject(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.OBJECT)
				return ((ParamObject)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type object");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public String getParamValueList(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.LIST)
				return ((ParamList)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type list");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public String getParamValueDir(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.DIR)
				return ((ParamDir)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type dir");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public int getParamValueEnum(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.ENUM)
				return ((ParamEnum)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type enum,");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public String getParamValueFile(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.FILE)
				return ((ParamFile)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type file,");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public JIPImage getParamValueImage(String name) throws JIPException
	{
		int pos = param_names.indexOf(name);

		if(pos != -1) {
			if (param_list.get(pos).getType() == ParamType.IMAGE)
				return ((ParamImage)param_list.get(pos)).getValue();	
			else
				throw new JIPException("Function2D.getParamValue: parameter "+name+" is not of type image,");
		}
		else
			throw new JIPException("Function2D.getParamValue: parameter not found");
	}

	public void updateProgress(double value)
	{
		progress = value;
		if (progress <0) progress = 0;
		if (progress>100) progress = 100;

	}

	public void setParamValue(String name, int value) throws JIPException
	{
		int pos = param_names.indexOf(name);
		
		if(pos!=-1 && param_list.get(pos).getType() == ParamType.INT)
			((ParamInt)param_list.get(pos)).setValue(value);
		else
			throw new JIPException("Function2D.setParamValue: parameter "+name+" not found or not an int");
	}

	public void setParamValue(String name, double value)  throws JIPException
	{
		int pos = param_names.indexOf(name);
		
		if(pos!=-1 && param_list.get(pos).getType() == ParamType.FLOAT)
			((ParamFloat)param_list.get(pos)).setValue((float)value);
		else
			throw new JIPException("Function2D.setParamValue: parameter "+name+" not found or not an float");
	}

	public void setParamValue(String name, boolean value) throws JIPException
	{
		int pos = param_names.indexOf(name);
		
		if(pos!=-1 && param_list.get(pos).getType() == ParamType.BOOL)
			((ParamBool)param_list.get(pos)).setValue(value);
		else
			throw new JIPException("Function2D.setParamValue: parameter "+name+" not found or not a boolean");
	}

	public void setParamValue(String name, String value) throws JIPException
	{
		int pos = param_names.indexOf(name);
		
		if(pos!=-1 && param_list.get(pos).getType() == ParamType.STRING)
			((ParamString)param_list.get(pos)).setValue(value);
		else
			throw new JIPException("Function2D.setParamValue: parameter "+name+" not found or not a string");
	}

	public void setParamValue(String name, ScreenData value) throws JIPException
	{
		int pos = param_names.indexOf(name);
		
		if(pos!=-1 && param_list.get(pos).getType() == ParamType.SCRDATA)
			((ParamScrData)param_list.get(pos)).setValue(value);
		else
			throw new JIPException("Function2D.setParamValue: parameter "+name+" not found or not a screendata");
	}

	public void setParamValue(String name, Object value) throws JIPException
	{
		int pos = param_names.indexOf(name);
		
		if(pos!=-1 && param_list.get(pos).getType() == ParamType.OBJECT)
			((ParamObject)param_list.get(pos)).setValue(value);
		else
			throw new JIPException("Function2D.setParamValue: parameter "+name+" not found or not an object");
	}

	public void setCanvas(MyCanvas3D can)
	{
		canvas = can;
	}

	public MyCanvas3D getCanvas()
	{
		return canvas;
	}
}
