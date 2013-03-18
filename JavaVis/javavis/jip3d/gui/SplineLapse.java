package javavis.jip3d.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.media.j3d.Transform3D;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javavis.jip3d.geom.MyTransform;

import com.sun.j3d.utils.behaviors.interpolators.RotPosScaleTCBSplinePathInterpolator;

/**
 * This class manages a spline
 */
public class SplineLapse extends JDialog implements ChangeListener, ItemListener, Runnable {
	private static final long serialVersionUID = -6187056254400868620L;
	JSlider timeBar;
	JSlider vel;
	JCheckBox auto;
	JButton record;
	RotPosScaleTCBSplinePathInterpolator spline;
	JPanel up = new JPanel();
	JPanel down = new JPanel();
	JPanel infoPanel = new JPanel();
	JLabel info, info2;
	MyCanvas3D canvas;
	MyWindowAdapter mwa;
	Thread th;
	boolean threadLive;
	float max;
	float count;
	ArrayList<MyTransform> tList;
	int tListSize;
	
	public static float SLIDER_RANGE =100000000;
	static int MAX_TIME = 1000;

	public SplineLapse(Properties prop, Gui3D gui, RotPosScaleTCBSplinePathInterpolator sp, ArrayList<MyTransform> transformList) {		
		super(gui.getFather());
		this.setName("View ");
		this.spline = sp;
		canvas = gui.getCanvas();
		tList=transformList;
		tListSize=tList.size();
		
		this.setLayout(new BorderLayout());
		up.setLayout(new BorderLayout());
		down.setLayout(new BorderLayout());
		infoPanel.setLayout(new BorderLayout());
		
		timeBar = new JSlider(JSlider.HORIZONTAL, 0, MAX_TIME, 0);
		timeBar.addChangeListener(this);
		timeBar.setEnabled(false);
		up.add("Center", timeBar);

		auto = new JCheckBox(prop.getProperty("SplineAuto")); 
		auto.setSelected(true);
		auto.addItemListener(this);
		up.add("West", auto);

		record = new JButton(prop.getProperty("GrabSequence")); 
		record.addItemListener(this);
		up.add("East", record);
		
		
		info = new JLabel(getTranslation(0));
		info2 = new JLabel(getRotation(0));
		infoPanel.add("North", info);
		infoPanel.add("South", info2);
		up.add("South",infoPanel);
		this.add("North",up);
		
		vel = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
		vel.addChangeListener(this);
		down.add("Center",vel);
		
		JLabel label = new JLabel(prop.getProperty("Velocidad"));
		down.add("West", label);
		this.add("South", down);
		
		this.setBackground(new Color(232, 232, 232));
		threadLive=true;
		max=SLIDER_RANGE;
		count=0;
		
		th = new Thread(this);
		th.start();
	}
	
	private String getTranslation (int val) {
		String ret;
		
		ret = "Traslation "+tList.get(val).trX+" "+tList.get(val).trY+" "+tList.get(val).trZ+"\n";
		return ret;
	}
	
	private String getRotation (int val) {
		String ret;
		
		ret = "Rotation "+tList.get(val).angX+" "+tList.get(val).angY+" "+tList.get(val).angZ+"\n";
		return ret;
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();

		if(source == auto) {
			if (!threadLive && auto.isSelected()) {
				threadLive=true;
				th = new Thread(this);
				th.start();
				timeBar.setEnabled(false);
			}
			else if (threadLive && !auto.isSelected()) {
				threadLive=false;
				timeBar.setEnabled(true);
				timeBar.setValue((int)(MAX_TIME*count/max));
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		Transform3D transform = new Transform3D();
		int aux;
		
		if(e.getSource().equals(timeBar)) {
			count = max*timeBar.getValue()/MAX_TIME;
			spline.computeTransform(count/max, transform);
			canvas.tgView.setTransform(transform);
			aux=(int)(tListSize*count/max);
			if (aux==tListSize) aux--;
			info.setText(getTranslation(aux));
			info2.setText(getRotation(aux));
		}
		else if(e.getSource().equals(vel)) {
			float aux2 =(float) ((SLIDER_RANGE/10)*Math.pow(10, (100-vel.getValue())/50.0f));
			count = aux2 * count / max;
			max = aux2;
		}
		
	}
	
	public MyWindowAdapter getWindowListener () {
		mwa = new MyWindowAdapter(this);
		return mwa;
	}
	
	public void run() {
		Transform3D transform = new Transform3D();
		int aux;
		
		while (threadLive) {
			count = (count+1)%max;
			spline.computeTransform(count/max, transform);
			canvas.tgView.setTransform(transform);
			aux=(int)(tListSize*count/max);
			if (aux==tListSize) aux--;
			info.setText(getTranslation(aux));
			info2.setText(getRotation(aux));
		}
	}
	
	public void close() {
		this.setVisible(false);
	}
	

	/**
	 * Auxiliary class to capture the closing window event.
	 * @author  Miguel Cazorla
	 */
	class MyWindowAdapter extends WindowAdapter {
		SplineLapse sp;
		MyWindowAdapter (SplineLapse sp) {
			this.sp=sp;
		}
		public void windowClosing(WindowEvent e) {
			threadLive=false;
			close();
		}
	};
}
