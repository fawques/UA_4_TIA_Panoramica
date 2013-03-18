package javavis.jip2d.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.Sequence;

import javax.swing.*;

import org.apache.log4j.Logger;

/**
 * Class used to create the dialog box for functions.
 * Here is where all buttons and input text are created to receive the user input.
 */
public class FunctionDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 2340270412441319644L;
	
	private static Logger logger = Logger.getLogger(FunctionDialog.class);

	/**
	 * True when OK button has been pressed
	 */
	boolean confirmed;

	/**
	 * True when values has been assigned
	 */
	boolean ok = false;
	
	/**
	 * Shows the number of parameters
	 */
	int nparam;
	
	/**
	 * Name of the parameters
	 */
	String[] pnames = null;
	
	/**
	 * Properties for language
	 */
	Properties prop;

	/**
	 * Text values
	 */
	JTextField[] values = null;

	/**
	 * Buttons
	 */
	JButton[] buttons = null;

	/**
	 * Keeps the ComboBox to get its value
	 */
	JComboBox[] combos = null;

	/**
	 * Keeps the CheckBox to get its value
	 */
	JCheckBox[] checks = null;

	/**
	 * Keeps the Spinner to get its value
	 */
	JSpinner[] spinners = null;

	/**
	 * Buttons group
	 */
	ButtonGroup bg = null;

	/**
	 * Function where the call is made
	 */
	Function2D func = null;

	/**
	 * Maximum value of integer
	 */
	int maxInt = Integer.MAX_VALUE;
	/**
	 * Minimum value of integer
	 */
	int minInt = Integer.MIN_VALUE;
	/**
	 * Maximum value of double
	 */
	double maxDouble = (double)Integer.MAX_VALUE;
	/**
	 * Minimum value of double
	 */
	double minDouble = (double)Integer.MIN_VALUE;

	/**
	 * Class constructor.
	 * @param frame Frame calling this class. It is used to center the window.
	 * @param function Function to apply
	 */
	public FunctionDialog(JFrame frame, Function2D function, Properties prop) {
		super(frame, "Function: " + function.getName(), true);
		confirmed = false;
		func = function;
		
		nparam = func.getNumInputParams();
		pnames = func.getInputParamNames();
		if (nparam > 0) {
			values = new JTextField[nparam];
			buttons = new JButton[nparam];
			checks = new JCheckBox[nparam];
			combos = new JComboBox[nparam];
			spinners = new JSpinner[nparam];
		}

		// Parameters panel
		JPanel pparams = new JPanel();

		try {
			if (func.getName().equals("Convol3x3")) {
				pparams.setLayout(new GridLayout(6, 1));
				for (int i = 0; i < 9; i += 3) {
					JPanel fila = new JPanel();
					for (int j=0; j<3; j++) {
						spinners[i+j] = new JSpinner(new SpinnerNumberModel(func.getParamValueFloat(pnames[i+j]), -100.0f, 100.0f, 0.01f));
						fila.add("East", spinners[i+j]);
					}				
					pparams.add(fila);
				}
				JPanel mul = new JPanel();
				mul.add("West", new JLabel("Multiplicator:"));
				SpinnerNumberModel snmM = 
					new SpinnerNumberModel(func.getParamValueFloat(pnames[9]), -10000.0, 10000.0, 0.01);
				spinners[9] = new JSpinner(snmM);
				spinners[9].setValue((double)func.getParamValueFloat(pnames[9]));
				mul.add(spinners[9]);
				JPanel div = new JPanel();
				div.add("West", new JLabel("Divider:"));
				SpinnerNumberModel snmD = 
					new SpinnerNumberModel(func.getParamValueFloat(pnames[10]), -10000.0, 10000.0, 0.01);
				spinners[10] = new JSpinner(snmD);
				spinners[10].setValue((double)func.getParamValueFloat(pnames[10]));
				div.add(spinners[10]);
				JPanel metodo = new JPanel();
				metodo.add("West", new JLabel("Method:"));
				combos[11] = new JComboBox();
				String []lv=func.getParamValueList(pnames[11]);
				for (int v=0; v<lv.length; v++)
					combos[11].addItem(lv[v]);
				metodo.add(combos[11]);
				pparams.add(mul);
				pparams.add(div);
				pparams.add(metodo);
			} else {
				if (func.getName().equals("Convol5x5")) {
					pparams.setLayout(new GridLayout(8, 1));
					for (int i = 0; i < 25; i += 5) {
						JPanel row = new JPanel();
						for (int j=0; j<5; j++) {
							SpinnerNumberModel snmR = 
								new SpinnerNumberModel(func.getParamValueFloat(pnames[i+j]), -100.0, 100.0, 0.01);
							spinners[i+j] = new JSpinner(snmR);
							row.add("East", spinners[i+j]);
						}
						pparams.add(row);
					}
					JPanel mul = new JPanel();
					mul.add("West", new JLabel("Multiplicator:"));
					SpinnerNumberModel snmM = 
						new SpinnerNumberModel(func.getParamValueFloat(pnames[25]), minDouble, maxDouble, 0.01);
					spinners[25] = new JSpinner(snmM);
					spinners[25].setValue((double)func.getParamValueFloat(pnames[25]));
					mul.add(spinners[25]);
					JPanel div = new JPanel();
					div.add("West", new JLabel("Divider:"));
					SpinnerNumberModel snmD = 
						new SpinnerNumberModel(func.getParamValueFloat(pnames[26]), minDouble, maxDouble, 0.01);
					spinners[26] = new JSpinner(snmD);
					spinners[26].setValue((double)func.getParamValueFloat(pnames[26]));
					div.add(spinners[26]);
					JPanel method = new JPanel();
					method.add("West", new JLabel("Method:"));
					combos[27] = new JComboBox();
					String []lv=func.getParamValueList(pnames[27]);
					for (int v=0; v<lv.length; v++)
						combos[27].addItem(lv[v]);
					method.add(combos[27]);
					pparams.add(mul);
					pparams.add(div);
					pparams.add(method);
				} else {
					if (nparam > 0)
						pparams.setLayout(new GridLayout(nparam * 2, 1));
					for (int i = 0; i < nparam; i++) {
						JPanel aux = new JPanel();
						switch (func.getParamType(pnames[i])) {
							case LIST:
								aux.add("West", new JLabel(pnames[i] + ":"));
								combos[i] = new JComboBox();
								combos[i].putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
								String []lv=func.getParamValueList(pnames[i]);
								if (lv!=null) { 
									for (int v=0; v<lv.length; v++)
										combos[i].addItem(lv[v]);
								}
								else combos[i].addItem("Not set");
								aux.add(combos[i]);
								break;
							case BOOL:
								aux.add("West", new JLabel(pnames[i] + ":"));
								checks[i]= new JCheckBox();
								checks[i].setSelected(func.getParamValueBool(pnames[i]));
								aux.add(checks[i]);
								break;
							case INT:
								SpinnerNumberModel snm = 
									new SpinnerNumberModel(func.getParamValueInt(pnames[i]), 
											minInt, maxInt, 1);
								spinners[i] = new JSpinner(snm);
								aux.add("West", new JLabel(pnames[i] + ":"));
								aux.add(spinners[i]);
								break;
							case FLOAT:
								SpinnerNumberModel snmR = 
									new SpinnerNumberModel(func.getParamValueFloat(pnames[i]), minDouble, maxDouble, 0.01);							
								spinners[i] = new JSpinner(snmR);
								aux.add("West", new JLabel(pnames[i] + ":"));
								aux.add(spinners[i]);
								break;
							case STRING:
								aux.add("West", new JLabel(pnames[i] + ":"));
								values[i] = new JTextField("", 15);
								if (!func.isInputParamRequired(pnames[i]))
									values[i].setText(func.getParamValueString(pnames[i]));
								aux.add("East", values[i]);
								break;
							case IMAGE:
								aux.add("West", new JLabel(pnames[i] + ":"));
								values[i] = new JTextField("", 15);
								if (!func.isInputParamRequired(pnames[i]))
									values[i].setText(func.getParamValueString(pnames[i]));
								aux.add("East", values[i]);
								buttons[i] = new JButton("...");
								buttons[i].addActionListener(this);
								aux.add(buttons[i]);
								break;
							case FILE:
								aux.add("West", new JLabel(pnames[i] + ":"));
								values[i] = new JTextField("", 15);
								values[i].setText(func.getParamValueString(pnames[i]));
								aux.add("East", values[i]);
								buttons[i] = new JButton("...");
								buttons[i].addActionListener(this);
								aux.add(buttons[i]);
								break;
							case DIR:
								aux.add("West", new JLabel(pnames[i] + ":"));
								values[i] = new JTextField("", 15);
								values[i].setText("");
								aux.add("East", values[i]);
								buttons[i] = new JButton("....");
								buttons[i].addActionListener(this);
								aux.add(buttons[i]);
								break;
						}					
						pparams.add(new JLabel(func.getParamDescr(pnames[i])));
						pparams.add(aux);
					}
				}
			}
		}
		catch (JIPException e) {logger.error(e);}
		// Button panel
		JButton bt0 = new JButton("Ok");	
		bt0.setActionCommand("OK");
		bt0.addActionListener(this);
		JButton bt1 = new JButton(prop.getProperty("Cancel"));
		bt1.setActionCommand("Cancel");
		bt1.addActionListener(this);
		JPanel pbot = new JPanel();
		pbot.add("West", bt0);
		pbot.add("East", bt1);

		// Panel to select to apply to the sequence or the current image
		bg = new ButtonGroup();
		JRadioButton jrb1 = new JRadioButton(prop.getProperty("CompSeq"));
		jrb1.setActionCommand("Complete");
		JRadioButton jrb2 = new JRadioButton(prop.getProperty("FramSel"));
		jrb2.setActionCommand("Selected");
		jrb2.setSelected(true);
		bg.add(jrb1);
		bg.add(jrb2);
		JPanel pcheck = new JPanel(new GridLayout(3, 1));

		pcheck.add(new JLabel(prop.getProperty("ApplyTo")));
		pcheck.add(jrb1);
		pcheck.add(jrb2);

		// Panel Buttons+Check
		JPanel pbotcheck = new JPanel();
		pbotcheck.add("West", pcheck);
		pbotcheck.add("East", pbot);

		getContentPane().add("North", new JLabel(func.getDescription()));
		getContentPane().add("Center", pparams);
		getContentPane().add("South", pbotcheck);
		getContentPane().setBackground(new Color(232, 232, 232));
		pack();
		center(frame);
	}

	/**
	 * Returns if OK button has been pressed
	 * @return  true if button has been pressed. False in otherwise
	 * @uml.property  name="confirmed"
	 */
	public boolean isConfirmed() {
		return confirmed;
	}

	/**
	 * Returns if Cancel button has been pressed
	 * @return true if button has been pressed. False in otherwise
	 */
	public boolean isCancelled() {
		return !confirmed;
	}

	/**
	 * Returns if parameters has been assigned.
	 * @return true if it has been assigned. Fase in otherwise
	 */
	public boolean isAssignedOK() {
		return ok;
	}

	/**
	 * Returns if apply to all the sequence has been checked 
	 * @return true if apply to all the sequence is set 
	 */
	public boolean applyToSeq() {
		return bg.getSelection().getActionCommand().equals("Complete");
	}

	/**
	 * It centers this window with respect to the window making the call
	 * @param frame Calling frame.
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

	/**
	 * Sets the values of parameters to the function
	 */
	void assignValues() {
		ok = true;
		for (int i = 0; i < nparam && ok; i++) {
			String param = pnames[i];
			try {
				switch (func.getParamType(param)) {
					case BOOL :
						func.setParamValue(param, checks[i].isSelected());
						break;
					case INT :
						func.setParamValue(param, (int)((Integer)spinners[i].getValue()));
						break;
					case FLOAT :
						func.setParamValue(param, (float)((Double)spinners[i].getValue()).doubleValue());
						break;
					case IMAGE :
						if (!values[i].getText().trim().equals("")) {
							Sequence auxseq = JIPToolkit.getSeqFromFile(values[i].getText().trim());
							if (auxseq != null)
								func.setParamValue(param, auxseq.getFrame(0));
							else if (func.isInputParamRequired(param))
								ok = false;
						}
						break;
					case FILE :
					case DIR :
					case STRING :
						func.setParamValue(param, values[i].getText().trim());
						break;
					case LIST :
						func.setParamValue(param, (String) combos[i].getSelectedItem());
						break;
				}
			}
			catch (JIPException e){logger.error(e);}
		}
		if (!func.paramsOK())
			ok = false;
	}

	/**
	 * Implements the mouse ActionListener on the buttons
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("OK")) {
			confirmed = true;
			assignValues();
			setVisible(false);
			return;
		}
		if (e.getActionCommand().equals("Cancel")) {
			confirmed = false;
			setVisible(false);
			return;
		}
		if (e.getActionCommand().equals("...")) {
			JFileChooser eligeFich = new JFileChooser();
			eligeFich.setDialogType(JFileChooser.OPEN_DIALOG);
			eligeFich.setCurrentDirectory(new File("."));
			if (eligeFich.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File fich = eligeFich.getSelectedFile();
				for (int i = 0; i < nparam; i++) 
					if ((JButton) e.getSource() == buttons[i])
						values[i].setText(fich.getPath());
			}
		}
		// Directory is different from a file, because we have to
		// select a directory name. It is differentiated using four ....
		if (e.getActionCommand().equals("....")) {
			JFileChooser chooseFile = new JFileChooser();
			chooseFile.setDialogType(JFileChooser.OPEN_DIALOG);
			chooseFile.setCurrentDirectory(new File("."));
			chooseFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooseFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooseFile.getSelectedFile();
				for (int i = 0; i < nparam; i++) 
					if ((JButton) e.getSource() == buttons[i])
						values[i].setText(file.getPath());
			}
		}
	}
}
