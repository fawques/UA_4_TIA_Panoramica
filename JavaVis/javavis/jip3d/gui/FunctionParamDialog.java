package javavis.jip3d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javavis.base.JIPException;
import javavis.base.ParamType;
import javavis.base.Parameter;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamDir;
import javavis.base.parameter.ParamFile;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.base.parameter.ParamScrData;
import javavis.base.parameter.ParamString;
import javavis.jip3d.base.ScreenData;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * @author  Miguel Cazorla
 */
public class FunctionParamDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -1181844930391165575L;

	/**
	 * @uml.property  name="num_buttons"
	 */
	private int num_buttons;

	/**
	 * @uml.property  name="confirmed"
	 */
	public boolean confirmed;

	/**
	 * @uml.property  name="components"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javax.swing.JComponent"
	 */
	ArrayList<JComponent> components;

	/**
	 * @uml.property  name="local_param_list"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	ArrayList<Parameter>local_param_list;

	/**
	 * @uml.property  name="diag"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	MyDialog diag;

	/**
	 * @uml.property  name="scr_list"
	 */
	ArrayList<ScreenData> scr_list;
	
	/**
	 * @uml.property  name="actualData"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	ScreenData currentData;

	public FunctionParamDialog(MyDialog d, ArrayList<Parameter> param_list, ArrayList<ScreenData> data_list, ScreenData currentData) throws JIPException
	{
		super(d.owner, "Change Function Parameters");
		components = new ArrayList<JComponent>(); //Saves the components we create
		num_buttons = 0;
		diag = d;
		local_param_list = param_list;
		JPanel pmain;
		JPanel pcomp;
		JPanel paux = new JPanel();
		JLabel label;
		Parameter param;
		int count, size;
		size = param_list.size();
		JComponent comp = null;
		ParamType type;
		scr_list = data_list;
		JButton but;
		
		this.currentData = currentData;
		
		pmain = new JPanel(new GridLayout(size, 0));

		for(count=0;count<size;count++)
		{
			param = param_list.get(count);
			type = param.getType();

			switch(type)
			{
			case INT:
				comp = createNumberComponent(((ParamInt)param).getValue(), ParamInt.MINVALUE, ParamInt.MAXVALUE);
				paux = new JPanel();
				paux.add(comp);
				break;
			case FLOAT:
				comp = createNumberComponent(((ParamFloat)param).getValue(), ParamFloat.MINVALUE, ParamFloat.MAXVALUE, ParamFloat.DSTEP);
				paux = new JPanel();
				paux.add(comp);
				break;
			case BOOL:
				comp = createBooleanComponent(((ParamBool)param).getValue());
				paux = new JPanel();
				paux.add(comp);
				break;
			case STRING:
				comp = createStringComponent(((ParamString)param).getValue());
				paux = new JPanel();
				paux.add(comp);
				break;
			case FILE:
				comp = createPathComponent(((ParamFile)param).getValue());
				but = new JButton("...");
				but.setActionCommand("botonf"+num_buttons);
				but.addActionListener(this);
				paux = new JPanel();
				paux.add("West", comp);
				paux.add("East", but);
				break;
			case DIR:
				comp = createPathComponent(((ParamDir)param).getValue());
				but = new JButton("...");
				but.setActionCommand("botond"+num_buttons);
				but.addActionListener(this);
				paux = new JPanel();
				paux.add("West", comp);
				paux.add("East", but);
				break;
			case SCRDATA:
				comp = createScrDataComponent(((ParamScrData)param).getValue());
				paux = new JPanel();
				paux.add(comp);
				break;
			case LIST:
				comp = createListComponent(((ParamList)param).getDefault());
				paux = new JPanel();
				paux.add(comp);
				break;
			}
			
			num_buttons++;
			components.add(comp);
			label = new JLabel(param.getName()+":");
			pcomp = new JPanel();
			pcomp.add("West", label);
			pcomp.add("East", paux);
			pmain.add(pcomp);

		}

		// Panel of the buttons
		JButton bt0 = new JButton("Ok");
		bt0.setActionCommand("OK");
		bt0.addActionListener(this);
		JButton bt1 = new JButton("Cancel");
		bt1.setActionCommand("Cancel");
		bt1.addActionListener(this);
		JPanel pbot = new JPanel();
		pbot.add("West", bt0);
		pbot.add("East", bt1);

		getContentPane().add("North", new JLabel("Change Function Parameters"));
		getContentPane().add("Center", pmain);
		getContentPane().add("South", pbot);
		getContentPane().setBackground(new Color(232, 232, 232));

		pack();
		center(d.owner);
	}

	private JComboBox createListComponent(String []list)
	{
		JComboBox ret = new JComboBox(list);
		return ret;
	}
	
	private JSpinner createNumberComponent(int value, int min, int max)
	{
		JSpinner ret;
		SpinnerNumberModel snmR = new SpinnerNumberModel(value, min, max, 1);
		ret = new JSpinner(snmR);
		ret.setPreferredSize(new Dimension(100,20));
		return ret;
	}

	private JSpinner createNumberComponent(double value, double min, double max, double step)
	{
		JSpinner ret;
		SpinnerNumberModel snmR = new SpinnerNumberModel(value, min, max, step);
		ret = new JSpinner(snmR);
		ret.setPreferredSize(new Dimension(100,20));
		return ret;
	}

	private JCheckBox createBooleanComponent(boolean value)
	{
		JCheckBox ret = new JCheckBox();
		ret.setSelected(value);
		return ret;
	}

	private JTextField createStringComponent(String value)
	{
		JTextField ret = new JTextField(20);
		if (value!=null) ret.setText(value);
		return ret;
	}

	private JComboBox createScrDataComponent(ScreenData value) throws JIPException
	{
		JComboBox list = new JComboBox();
		int index = 0;
		String name;
		if(value!=null) name = value.name;
		else name = "";
		for(ScreenData dat: scr_list) {
			if(!dat.equals(currentData)) {
				list.addItem(dat.name);
				if(name.equals(dat.name)) index = list.getItemCount();
			}
		}
		if (list.getItemCount()==0) 
			throw new JIPException("Error: No ScrData Parameters available");
		list.setSelectedIndex(index);
		return list;
	}

	/**
	 * @param value
	 * @return
	 */
	private JTextField createPathComponent(String value)
	{
		JTextField ret = new JTextField(20);
		if (value!=null) ret.setText(value);
		return ret;
	}

	/**
	 * Shows if OK button has been pressed
	 * @return  TRUE if the button has been pressed. FALSE if the button has not been pressed
	 * @uml.property  name="confirmed"
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	/**
	 * Shows if Cancel button has been pressed
	 * @return TRUE if the button has been pressed. FALSE if the button has not been pressed
	 */
	public boolean isCancelled() {
		return !confirmed;
	}

	/**
	 * It centres the window in the midle of window which make the call
	 * @param numFrame JFrame that we want to center.
	 */
	void center(JFrame frame) {
		Point p = frame.getLocationOnScreen();
		Dimension dframe = frame.getSize();
		Dimension dthis = getSize();
		int x = (dframe.width - dthis.width) / 2;
		int y = (dframe.height - dthis.height) / 2;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		setLocation(p.x + x, p.y + y);
	}

	void assignValues()
	{
		int count, size, ivalue;
		double dvalue;
		boolean bvalue;
		size = components.size();
		JComponent comp;
		Parameter param;
		String name;
		ScreenData scr;
		ParamType type;
		for(count=0;count<size;count++)
		{
			comp = components.get(count);
			param = local_param_list.get(count);
			type = param.getType();

			switch(type)
			{
				case INT:
					ivalue = (Integer)((JSpinner)comp).getValue();
					((ParamInt)param).setValue(ivalue);
					break;
				case FLOAT:
					dvalue = (Double)((JSpinner)comp).getValue();
					((ParamFloat)param).setValue((float)dvalue);
					break;
				case BOOL:
					bvalue = ((JCheckBox)comp).isSelected();
					((ParamBool)param).setValue(bvalue);
					break;
				case STRING:
					((ParamString)param).setValue(((JTextField)comp).getText());
					break;
				case FILE:
					((ParamFile)param).setValue(((JTextField)comp).getText());
					break;
				case DIR:
					((ParamDir)param).setValue(((JTextField)comp).getText());
					break;
				case LIST:
					((ParamList)param).setValue((String)((JComboBox)comp).getSelectedItem());
					break;
				case SCRDATA:
					name = (String)((JComboBox)comp).getSelectedItem();
					scr = null;
					for(ScreenData dat: scr_list)
					{
						if(dat.name.equals(name))
							scr = dat;
					}
					((ParamScrData)param).setValue(scr);
					break;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cur = e.getActionCommand();
		String sub;
		char t;
		File data;

		if (cur.equals("OK")) {
			confirmed = true;
			assignValues();
			setVisible(false);
			return;
		}
		if (cur.equals("Cancel")) {
			confirmed = false;
			setVisible(false);
			return;
		}

		sub = cur.substring(6);
		t = cur.charAt(5);

		int pos = new Integer(sub).intValue();

		if(t=='f')
			data = diag.fileChooser(null, null, null, false);
		else
			data = diag.fileChooser(null, null, null, true, JFileChooser.DIRECTORIES_ONLY);

		String path;
		if(data!=null)
		{
			path = data.getAbsolutePath();
			((JTextField)components.get(pos)).setText(path);
		}

	}
}
