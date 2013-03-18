package javavis.jip2d.base;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.reflections.Reflections;

/**
 * Class to manage the function list. In this class is where the functions and their groups are specified.
 */
public class FunctionList {
	
	private static Logger logger = Logger.getLogger(FunctionList.class);
	
	/**
	 * Number of functions in the list
	 * @uml.property  name="nfunc"
	 */
	int nfunc;

	/**
	 * Array which has the names of the functions
	 * @uml.property  name="funcnames" multiplicity="(0 -1)" dimension="1"
	 */
	String[] funcnames = null;
	
	/**
	 * Array which connect function with groups
	 * @uml.property  name="funcgroups"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	FunctionGroup[] funcgroups = null;
	
	/**
	 * Array keeping the number of functions in each group
	 * @uml.property  name="fgnum" multiplicity="(0 -1)" dimension="1"
	 */
	int[] fgnum = null;
	
	/**
	 * Integer indicating the number of groups
	 * @uml.property  name="ngrps"
	 */
	int ngrps;
	
	/**
	 *	Class constructor. Here the names of the function are inserted in the
	 * arrays and its groups.
	 */
	public FunctionList() {
		// Functions are loaded in a dinamic way
		Reflections reflections = new Reflections("javavis.jip2d.functions");
		Class<? extends Function2D> func;
		Object[] funcList = ((HashSet)reflections.getSubTypesOf(Function2D.class)).toArray();
		
		nfunc = funcList.length;
		funcnames = new String[nfunc];
		funcgroups = new FunctionGroup[nfunc];
		ngrps = FunctionGroup.values().length;
		fgnum = new int[ngrps];

		for (int count=0; count<nfunc; count++) {
			func = (Class<? extends Function2D>)funcList[count];
			funcnames[count]=func.getName().substring(func.getName().lastIndexOf(".")+1, func.getName().length());
		}
		Arrays.sort(funcnames);
		
		for (int count=0; count<nfunc; count++) {
			try {
				funcgroups[count]=((Function2D)Class.forName("javavis.jip2d.functions."+funcnames[count]).newInstance()).getGroupFunc();
				fgnum[funcgroups[count].ordinal()]++;
			} 
			catch (InstantiationException e) {logger.error(e);} 
			catch (IllegalAccessException e) {logger.error(e);} 
			catch (ClassNotFoundException e) {logger.error(e);}
		}
	}

	/**
	 *   Method to get the number of created function.
	 * @return Number of functions
	 */
	public int getNumFunctions() {
		return nfunc;
	}

	/**
	 *   Method to get the number of functions in each group.
	 * @return Array where each element is the number of functions of the
	 * corresponding group
	 */
	public int getFuncGroupNum(FunctionGroup fg) {
		return fgnum[fg.ordinal()];
	}

	/**
	 *   Method to get the name of the function name which is passed by parameter.	 
	 * @param f Number assigned to function
	 * @return Name of the asked function
	 */
	public String getName(int f) {
		if (f >= 0 && f < nfunc) return funcnames[f];
		else return ("");
	}

	/**
	 *   Method to create the menu that contain the function.
	 * @param tittle Menu tittle
	 * @param al ActionListener
	 * @return menu that contain the function.
	 */
	public JMenu getFunctionMenu(String tittle, ActionListener al) {
		JMenu mfunc = new JMenu(tittle);
		JMenuItem item;
		JMenu m;

		for (FunctionGroup f : FunctionGroup.values()) {
			m = new JMenu(f.toString());
			for (int j = 0; j < nfunc; j++) {
				if (funcgroups[j] == f) {
					item = new JMenuItem(funcnames[j]);
					item.setActionCommand("F_" + funcnames[j]);
					item.addActionListener(al);
					m.add(item);
				}
			}
			mfunc.add(m);
		}
		return mfunc;
	}
	
	public String[] getFuncArray () {
		return funcnames;
	}

	/**
	 * @return   Returns the ngrps.
	 * @uml.property  name="ngrps"
	 */
	public int getNgrps() {
		return ngrps;
	}

	/**
	 * @param ngrps   The ngrps to set.
	 * @uml.property  name="ngrps"
	 */
	public void setNgrps(int ngrps) {
		this.ngrps = ngrps;
	}

	/**
	 * @return   Returns the funcgroups.
	 * @uml.property  name="funcgroups"
	 */
	public FunctionGroup[] getFuncgroups() {
		return funcgroups;
	}

	/**
	 * @param funcgroups   The funcgroups to set.
	 * @uml.property  name="funcgroups"
	 */
	public void setFuncgroups(FunctionGroup[] funcgroups) {
		this.funcgroups = funcgroups;
	}
}