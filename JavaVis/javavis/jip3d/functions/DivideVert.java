package javavis.jip3d.functions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;

/**
 * It divides a set of points in two subsets. To do that, it uses a vertical plane parallel to
 * YZ plane. It also allows an interactive execution based on a manual or automatic parameter.
 * In manual case, you use a text dialog to select the division plane position in the X axis.
 * In automatic case, you must specify a parameter which indicates the position of the division
 * plane in the X axis.<br />
 * The screen data must be a set of 3D points.<br />
 */
public class DivideVert extends Function3D {

	double width;

	public DivideVert()
	{
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		this.group = Function3DGroup.Others;

		ParamBool p1 = new ParamBool("Manual");
		p1.setValue(true);
		ParamFloat p2 = new ParamFloat("Width");
		p2.setValue(0.0f);

		addParam(p1);
		addParam(p2);
	}

	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		ScreenData scr_new;
		ArrayList<Object []> sets;
		Point3D point;
		int count;
		Object []elements;
		DivideDialog div_diag = null;

		boolean manual = getParamValueBool("Manual");
		width = getParamValueFloat("Width");

		if(manual)
		{
			width = scr_data.getMinRange()[0];
			div_diag = new DivideDialog(scr_data);
			div_diag.setModal(true);
			div_diag.setVisible(true);
		}
		if(!manual || div_diag.isConfirmed())
		{
			sets = divideTree(scr_data);

			scr_new = new PointSet3D(new ScreenOptions());
			scr_new.name = "right_" + scr_data.name;
			elements = sets.get(1);
			for(count=0;count<elements.length;count++)
			{
				point = (Point3D)elements[count];
				scr_new.insert(point);
			}
			result_list.add(scr_new);

			scr_new = new PointSet3D(new ScreenOptions());
			scr_new.name = "left_" + scr_data.name;
			elements = sets.get(0);
			for(count=0;count<elements.length;count++)
			{
				point = (Point3D)elements[count];
				scr_new.insert(point);
			}
			result_list.add(scr_new);
		}
		else
			result_list = null;

		return;
	}

	/**
	 * It divides the screen data in two subsets and saves them in an array list.
	 * @param scr_data ScreenData.
	 */
	private ArrayList<Object []> divideTree(ScreenData scr_data)
	{
		ArrayList<Object []>ret = new ArrayList<Object[]>(2);
		double []center = new double [3];

		try {
			center[0] = width;
			center[1] = scr_data.getMaxRange()[1];
			center[2] = scr_data.getMaxRange()[2];
			ret.add(scr_data.range(scr_data.getMinRange(), center));
			center[1] = scr_data.getMinRange()[1];
			center[2] = scr_data.getMinRange()[2];
			ret.add(scr_data.range(center, scr_data.getMaxRange()));

		} catch(Exception e) {
			ret = null;
		}

		return ret;
	}

	
	/**
	 * Private class DivideDialog
	 * @author Miguel Cazorla
	 */
	class DivideDialog extends JDialog implements ActionListener, ChangeListener
	{
		private static final long serialVersionUID = -8084428193661519578L;

		JLabel utag;
		JLabel btag;
		JLabel thres;
		JSlider slider;

		boolean confirmed;
		ScreenData myscrdata;

		public DivideDialog(ScreenData scr_data)
		{
			super(dialog.owner);
			myscrdata = scr_data;
			JPanel pmain = new JPanel(new GridLayout(4,0));
			JPanel paux;

			ArrayList<Object []> sets = divideTree(scr_data);

			int s1 = sets.get(0).length;
			int s2 = sets.get(1).length;

			paux = new JPanel();
			thres = new JLabel(" "+width);
			paux.add("West", new JLabel("Threshold: "));
			paux.add("East", thres);
			pmain.add(paux);

			slider = new JSlider(JSlider.HORIZONTAL, (int)(scr_data.getMinRange()[0]*100), (int)(scr_data.getMaxRange()[0]*100), (int)(width*100));
			slider.addChangeListener(this);
			pmain.add(slider);

			paux = new JPanel();
			utag = new JLabel(s2 + " points  ");
			paux.add("West", new JLabel("  Right set contains: "));
			paux.add("East", utag);
			pmain.add(paux);

			paux = new JPanel();
			btag = new JLabel(s1 + " points");
			paux.add("West", new JLabel("Left set contains: "));
			paux.add("East", btag);
			pmain.add(paux);

			// Button panels
			JButton bt0 = new JButton("Accept");
			bt0.setActionCommand("Accept");
			bt0.addActionListener(this);
			JButton bt1 = new JButton("Decline");
			bt1.setActionCommand("Decline");
			bt1.addActionListener(this);
			JPanel pbot = new JPanel();
			pbot.add("West", bt0);
			pbot.add("East", bt1);

			getContentPane().add("North", new JLabel("Original Set contains: "+scr_data.scr_opt.num_points+" points"));
			getContentPane().add("Center", pmain);
			getContentPane().add("South", pbot);
			getContentPane().setBackground(new Color(232, 232, 232));
			pack();
			center(dialog.owner);
		}

		/**
		 * It returns true if OK button has been pressed.
		 * @return TRUE if OK button has been pressed. FALSE in otherwise.
		 */
		public boolean isConfirmed() {
			return confirmed;
		}

		/**
		 * It returns true if CANCEL button has been pressed.
		 * @return TRUE if CANCEL button has been pressed. FALSE in otherwise.
		 */
		public boolean isCancelled() {
			return !confirmed;
		}

		/**
		 * It centers the window in the middle of the window which makes the call.
		 * @param numFrame JFrame that we want to center.
		 */
		private void center(JFrame frame) {
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
		 * It indicates if the parameter <em>confirmed</em> is accepted.
		 * @param evt ActionEvent.
		 */
		public void actionPerformed(ActionEvent evt) {
			String act = evt.getActionCommand();
			if (act.equals("Accept")) {
				confirmed = true;
				setVisible(false);
				return;
			}
			if (act.equals("Decline")) {
				confirmed = false;
				setVisible(false);
				return;
			}
		}

		/**
		 * It indicates if the parameter <em>confirmed</em> is accepted.
		 * @param evt ActionEvent.
		 */
		public void stateChanged(ChangeEvent arg0) {
			int val = slider.getValue();
			width = (double)val/100.0;
			ArrayList<Object []> sets = divideTree(myscrdata);

			int s1 = sets.get(0).length;
			int s2 = sets.get(1).length;

			utag.setText(s2 + " points");
			btag.setText(s1 + " points");
			thres.setText(" "+width);
		}
	}

}
