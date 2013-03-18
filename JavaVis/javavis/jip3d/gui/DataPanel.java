package javavis.jip3d.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Properties;

import javavis.jip3d.base.ScreenOptions;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * This class is used to save in JavaVis3D the visualization options  of a 3D object.
 * It also allows the user interaction in the panel, with the different visualization options.
 * @author Diego Viejo
 */
public class DataPanel extends JPanel {
	private static final long serialVersionUID = -3974559425157803401L;

	/**
	 * @uml.property  name="jcb_visible"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JCheckBox jcb_visible;
	/**
	 * @uml.property  name="jcb_color"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JCheckBox jcb_color;	//for PointSet3D
	/**
	 * @uml.property  name="jcb_improve"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JCheckBox jcb_improve;	//for PointSet3D

    //for scr_data panel
	/**
	 * @uml.property  name="spinner"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Double"
	 */
	public JSpinner spinner = null; //width spinner
	/**
	 * @uml.property  name="jsp_length"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Double"
	 */
	public JSpinner jsp_length;
	/**
	 * @uml.property  name="jsp_alpha"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Double"
	 */
	public JSpinner jsp_alpha;
	
	/**
	 * @uml.property  name="jbt_color"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JButton jbt_color;
	/**
	 * @uml.property  name="jbt_remove"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JButton jbt_remove;
	
	/**
	 * @uml.property  name="numElementsLabel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel numElementsLabel;
	
	/**
	 * @uml.property  name="prop"
	 */
	Properties prop;

	public DataPanel(Properties prop, Gui3D gui3D) {
		this.prop = prop;
		
    	JPanel ContentPane = new JPanel();
		JPanel pcontent = new JPanel(new BorderLayout());
		JPanel pcheckboxes = new JPanel(new GridLayout(7,0,1,0));
		JPanel pcolor = new JPanel();
		JPanel pspinner = new JPanel();
		JPanel plenght = new JPanel();
		JPanel pnumber = new JPanel();

		SpinnerNumberModel snmR;
		JPanel palpha = new JPanel();

		numElementsLabel = new JLabel(prop.getProperty("Elements3D")+" 0   "); 
		pnumber.add(numElementsLabel);
		pcheckboxes.add(pnumber);

		jcb_visible = new JCheckBox(prop.getProperty("Visible3D")+"          "); //Visible3D
		jcb_visible.addItemListener(gui3D);
		pcheckboxes.add(jcb_visible);

		jcb_color = new JCheckBox(prop.getProperty("Color3D")); //Color3D
		jcb_color.addItemListener(gui3D);
		jcb_color.setEnabled(false);
		pcheckboxes.add(jcb_color);
		jcb_improve = new JCheckBox(prop.getProperty("Improved3D")); //Improved3D
		jcb_improve.addItemListener(gui3D);
		jcb_improve.setEnabled(false);
		pcheckboxes.add(jcb_improve);

		snmR = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsp_alpha = new JSpinner(snmR);
		jsp_alpha.addChangeListener(gui3D);
		jsp_alpha.setPreferredSize(new Dimension(50,20));
		jsp_alpha.setEnabled(false);
		palpha.add("West", new JLabel(prop.getProperty("Transparency3D"))); //Transparency3D
		palpha.add("East", jsp_alpha);
		pcheckboxes.add(palpha);

		snmR = new SpinnerNumberModel(0.1, 0.01, 1.0, 0.01);
		jsp_length = new JSpinner(snmR);
		jsp_length.addChangeListener(gui3D);
		jsp_length.setPreferredSize(new Dimension(50, 20));
		jsp_length.setEnabled(false);
		plenght.add("West", new JLabel(prop.getProperty("VLength3D"))); //VLength3D
		plenght.add("East", jsp_length);
		pcheckboxes.add(plenght);

		snmR = new SpinnerNumberModel(1.0, 1.0, 20.0, 1.0);
		spinner = new JSpinner(snmR);
		spinner.addChangeListener(gui3D);
		spinner.setEnabled(false);
		spinner.setPreferredSize(new Dimension(50,20));
		pspinner.add("West", new JLabel(prop.getProperty("Width3D")+": ")); //Width3D
		pspinner.add("East", spinner);


		pcolor.add("West", new JLabel(prop.getProperty("Color3D"))); //Color3D
		jbt_color = new JButton("");
		jbt_color.setActionCommand("Color");
		jbt_color.addActionListener(gui3D);
		jbt_color.setPreferredSize(new Dimension(50,20));
		jbt_color.setEnabled(false);
		pcolor.add("East", jbt_color);

		JPanel p_remove = new JPanel(new GridLayout(2,0));
		jbt_remove = new JButton(prop.getProperty("Delete3D")); //Delete3D
		jbt_remove.setActionCommand("Remove Object");
		jbt_remove.addActionListener(gui3D);
		jbt_remove.setEnabled(false);
		p_remove.add(pcolor);
		p_remove.add(jbt_remove);
		
		pcontent.add("North", pcheckboxes);
		pcontent.add("Center", pspinner);
		pcontent.add("South", p_remove);

		ContentPane.add("Center", pcontent);
		ContentPane.setBackground(new Color(232, 232, 232));
		this.add(ContentPane);

	}
	
	public void updateContent(ScreenOptions opt)
	{
		//set everything to 'disabled'
		jcb_visible.setEnabled(false);
		jcb_color.setEnabled(false);
		jcb_improve.setEnabled(false);
		jsp_alpha.setEnabled(false);
		jsp_length.setEnabled(false);
		spinner.setEnabled(false);
		jbt_color.setEnabled(false);
		jbt_remove.setEnabled(false);
		if(opt==null) return;
		
		numElementsLabel.setText(prop.getProperty("Elements3D")+" "+opt.num_points);
		jcb_visible.setSelected(opt.is_visible);
		jcb_visible.setEnabled(true);


		switch(opt.type)
		{
			case ScreenOptions.tPOINTSET3D:
			case ScreenOptions.tIMAGESET3D:
				jcb_color.setEnabled(true);
				jcb_improve.setEnabled(true);
				break;
			case ScreenOptions.tNORMALSET3D:
				jsp_length.setValue(opt.length);
				jsp_length.setEnabled(true);
				break;
			case ScreenOptions.tTRAJ2D:
			case ScreenOptions.tTRAJ3D:
				jsp_alpha.setValue(opt.alpha);
				jsp_alpha.setEnabled(true);
				break;
		}
		
		spinner.setValue(opt.width);
		spinner.setEnabled(true);
		jbt_color.setBackground(opt.color.get());
		jbt_color.setEnabled(true);
		if(opt.type == ScreenOptions.tPOINTSET3D && !jcb_color.isSelected())
			jbt_color.setEnabled(false);

		jbt_remove.setEnabled(true);
	}

}
