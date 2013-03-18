package javavis.desktop.gui;

import java.awt.event.ActionListener;
import java.util.Properties;

import javavis.Commons;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * 	Class which has the menu bar of the main program.
 */
public class MenuBarDesktop extends JMenuBar {
	private static final long serialVersionUID = 7975335917505405502L;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmnew"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmnew;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmopen"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmopen;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmsave"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmsave;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmsaveas"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmsaveas;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmexit"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmexit;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmabout"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmabout;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmhelp"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmhelp;
	/**
	 * JMenuItem objects
	 * @uml.property  name="jmgenerate"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem jmgenerate;
	
	/**
	 * @uml.property  name="prop"
	 */
	Properties prop;
	
	/**
	 *  Class constructor.
	 *  @param listener ActionListener
	 */
	public MenuBarDesktop(ActionListener listener, Properties propi) {
		prop=propi;
		
		JMenu menu = new JMenu(prop.getProperty("File"));
		add(menu);
		jmnew = new JMenuItem(prop.getProperty("New"), Commons.getIcon("new.jpg"));
		jmnew.addActionListener(listener);
		menu.add(jmnew);

		jmopen = new JMenuItem(prop.getProperty("Open"), Commons.getIcon("open.jpg"));
		jmopen.addActionListener(listener);
		menu.add(jmopen);

		jmsave = new JMenuItem(prop.getProperty("Save"), Commons.getIcon("guardar_ascii.jpg"));
		jmsave.addActionListener(listener);
		menu.add(jmsave);

		jmsaveas = new JMenuItem(prop.getProperty("SaveAs"), Commons.getIcon("guardar_ascii.jpg"));
		jmsaveas.addActionListener(listener);
		menu.add(jmsaveas);

		jmexit = new JMenuItem(prop.getProperty("Exit"), Commons.getIcon("salir.jpg"));
		jmexit.addActionListener(listener);
		menu.add(jmexit);
		
		JMenu generate = new JMenu(prop.getProperty("Generate"));
		add(generate);
		jmgenerate = new JMenuItem(prop.getProperty("GenFunc"));
		jmgenerate.addActionListener(listener);
		generate.add(jmgenerate);

		JMenu about = new JMenu(prop.getProperty("Help"));
		add(about);
		jmhelp = new JMenuItem(prop.getProperty("Help"));
		jmhelp.addActionListener(listener);
		about.add(jmhelp);
		
		jmabout = new JMenuItem(prop.getProperty("About"));
		jmabout.addActionListener(listener);
		about.add(jmabout);
	}
}
