package javavis.jip3d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author  Miguel Cazorla
 */
public class ChangeViewDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -8044403326538603901L;

	/**
	 * Tells us if OK has been pressed
	 * @uml.property  name="confirmed"
	 */
	boolean confirmed;

	/**
	 * Shows the number of parameters
	 * @uml.property  name="nparam"
	 */
	int nparam;

	/**
	 * Name of the parameters
	 * @uml.property  name="pnames" multiplicity="(0 -1)" dimension="1"
	 */
	String[] pnames = null;

	/**
	 * Saves the CheckBox to get its value
	 * @uml.property  name="checks"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	JCheckBox[] checks = null;

	/**
	 * Saves the Spinner to get its value
	 * @uml.property  name="spinners"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	JSpinner[] spinners = null;
	
	/**
	 * Buttons group
	 * @uml.property  name="array_ret"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Double"
	 */
	ArrayList<Double> array_ret;
	
	/**
	 * @uml.property  name="bg"
	 * @uml.associationEnd  
	 */
	ButtonGroup bg = null;
	
	/**
	 * Maximum value of integer
	 * @uml.property  name="maxInt"
	 */
	int maxInt = Integer.MAX_VALUE;
	
	/**
	 * Minimum value of integer
	 * @uml.property  name="minInt"
	 */
	int minInt = Integer.MIN_VALUE;
	
	/**
	 * Maximum value of double
	 * @uml.property  name="maxDouble"
	 */
	double maxDouble = Double.MAX_VALUE;
	
	/**
	 * Minimum value of double
	 * @uml.property  name="minDouble"
	 */
	double minDouble = -Double.MAX_VALUE;

	public ChangeViewDialog(JFrame frame, ArrayList<String> names, ArrayList<Double> values)
	{
		super(frame);
		nparam = values.size();
		array_ret = values;
		JPanel pparams = new JPanel();
		int total = nparam / 3;
		spinners = new JSpinner[nparam];
		int i, j;
		JPanel row; JPanel pair;

		pparams.setLayout(new GridLayout(total,1));
		for(i=0;i<nparam;i+=3)
		{
			row = new JPanel(new GridLayout(1,3));
			for(j=0;j<3;j++)
			{
				pair = new JPanel(new GridLayout(2,1));
				pair.add(new JLabel(names.get(i+j)));

				SpinnerNumberModel snmR =
					new SpinnerNumberModel(values.get(i+j).doubleValue(), minDouble, maxDouble, 0.1);
				spinners[i+j] = new JSpinner(snmR);
				pair.add(spinners[i+j]);
				row.add(pair);
			}
			pparams.add(row);
		}

		// Panel of buttons
		JButton bt0 = new JButton("Ok");
		bt0.setActionCommand("OK");
		bt0.addActionListener(this);
		JButton bt1 = new JButton("Cancel");
		bt1.setActionCommand("Cancel");
		bt1.addActionListener(this);
		JPanel pbot = new JPanel();
		pbot.add("West", bt0);
		pbot.add("East", bt1);

		getContentPane().add("North", new JLabel("It has to be changed"));
		getContentPane().add("Center", pparams);
		getContentPane().add("South", pbot);
		getContentPane().setBackground(new Color(232, 232, 232));
		setSize(320, 50+60*total);
		center(frame);

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
	 * It centers the window in the middle of window which make the call
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
		array_ret.clear();
		for(int i=0;i<nparam;i++)
			array_ret.add((Double)spinners[i].getValue());
	}

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
	}
}
