package javavis.jip3d.base;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;

import javavis.base.Function3DGroup;
import javavis.jip2d.base.FunctionGroup;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.reflections.Reflections;

/**
 * Function FunctionList3D
 * @author  Miguel Cazorla
 */
public class FunctionList3D {
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
	 * Array which connects function with groups
	 * @uml.property  name="funcgroups"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	Function3DGroup[] funcgroups = null;
	/**
	 * Array keeping the number of functions in each group
	 * @uml.property  name="fgnum" multiplicity="(0 -1)" dimension="1"
	 */
	int[] fgnum = null;
	/**
	 * Integer which indicates the number of groups
	 * @uml.property  name="ngrps"
	 */
	int ngrps;

	/** 
	 * Class constructor. Here the names of the function are inserted in the arrays and its groups.
	 */
	@SuppressWarnings("unchecked")
	public FunctionList3D()
	{
		Reflections reflections = new Reflections("javavis.jip3d.functions");
		Class<? extends Function3D> func;
		Object[] funcList = ((HashSet)reflections.getSubTypesOf(Function3D.class)).toArray();
		
		nfunc = funcList.length;
		funcnames = new String[nfunc];
		funcgroups = new Function3DGroup[nfunc];
		ngrps = FunctionGroup.values().length;
		fgnum = new int[ngrps];

		for (int cont=0; cont<nfunc; cont++) {
			func = (Class<? extends Function3D>)funcList[cont];
			funcnames[cont]=func.getName().substring(func.getName().lastIndexOf(".")+1, func.getName().length());
		}
		Arrays.sort(funcnames);
		
		for (int cont=0; cont<nfunc; cont++) {
			try {
				funcgroups[cont]=((Function3D)Class.forName("javavis.jip3d.functions."+funcnames[cont]).newInstance()).group;
				fgnum[funcgroups[cont].ordinal()]++;
			} 
			catch (Exception e) {
				System.err.println(e);} 
		}
	}


	/** 
	 * Method to get the number of created function.
	 * @return Number of functions
	 */
	public int getNumFunctions() {
		return nfunc;
	}

	/** 
	 * Method to get the number of functions in each group.
	 * @return Array where each element is the number of functions of the
	 * corresponding group
	 */
	public int[] getFuncGroupNum() {
		return (int[])fgnum.clone();
	}

	/** 
	 * Method to get the name of the function name which is passed by parameter.
	 * @param f Number assigned to function
	 * @return Name of the asked function
	 */
	public String getName(int f) {
		if (f >= 0 && f < nfunc) return funcnames[f];
		else return ("");
	}

	/** 
	 * Method to create the menu that contain the function.
	 * @param title Menu title
	 * @param al ActionListener
	 * @return menu that contain the function.
	 */
	public JMenu getFunctionMenu(String title, ActionListener al) {
		JMenu mfunc = new JMenu(title);
		JMenuItem item;
		JMenu m;

		for (Function3DGroup f : Function3DGroup.values()) {
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

	public int getNgrps() {
		return ngrps;
	}

	public void setNgrps(int ngrps) {
		this.ngrps = ngrps;
	}

	public Function3DGroup[] getFuncgroups() {
		return funcgroups;
	}

	public void setFuncgroups(Function3DGroup[] funcgroups) {
		this.funcgroups = funcgroups;
	}

}
