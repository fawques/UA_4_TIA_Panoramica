package javavis.jip3d.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Properties;

import javavis.Gui;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * This class manages the ability of showing or not a sequence of data
 */
public class TimeLapse extends JDialog {
	private static final long serialVersionUID = -3092141602244977110L;
	
	Properties prop;
	public JSlider timeBar;
	public JCheckBox acumulated;
	public JLabel currentT;
	
	int minValue, maxValue;
	int init, last;

	public TimeLapse(Properties prop, Gui gui, int initTime, int lastTime) {		
		super(gui);
		this.prop = prop;
		
		this.setLayout(new BorderLayout());
		
		init=initTime;
		last=lastTime;
		
		timeBar = new JSlider(JSlider.VERTICAL, init, last, init);
		timeBar.addChangeListener(gui.getG3d());
		// Calculate the separation between ticks
		int diff=last-init;
		int tickSpacingMajor=diff/10;
		int tickSpacingMinor=diff/100;
		timeBar.setMajorTickSpacing(tickSpacingMajor);
		timeBar.setMinorTickSpacing(tickSpacingMinor);
		timeBar.setPaintTicks(true);
		timeBar.setPaintLabels(true);
		this.add("Center", timeBar);

		acumulated = new JCheckBox(prop.getProperty("TimeLapseAccum")); 
		acumulated.addItemListener(gui.getG3d());
		acumulated.setEnabled(true);
		this.add("North", acumulated);
		

		JPanel currentTime= new JPanel(new BorderLayout());
		JLabel labelCurrentT=new JLabel(prop.getProperty("CurrentTime"));
		currentTime.add("North",labelCurrentT);
		currentT = new JLabel(" ");
		currentTime.add(currentT);
		
		this.add("South", currentTime);
		this.setBackground(new Color(232, 232, 232));
	}
}
