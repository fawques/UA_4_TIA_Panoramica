package javavis.jip3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javavis.jip3d.base.Function3D;

import javax.swing.JProgressBar;

public class FunctionPanel implements ActionListener,Runnable {

	/**
	 * @uml.property  name="confirmed"
	 */
	public boolean confirmed = true;

	/**
	 * @uml.property  name="func"
	 * @uml.associationEnd  
	 */
	Function3D func;
	/**
	 * @uml.property  name="progress_bar"
	 * @uml.associationEnd  
	 */
	JProgressBar progress_bar;
	/**
	 * @uml.property  name="canvas"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="func_panel:javavis.jip3d.gui.MyCanvas3D"
	 */
	MyCanvas3D canvas;

	/**
	 * @uml.property  name="blinker"
	 */
	private Thread blinker;

	public FunctionPanel (MyCanvas3D can)
	{
		func = null;
		canvas = can;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand().equals("Cancel")) {
			confirmed = false;
			func.stop();
			return;
		}
	}

	public void setFunction(Function3D func, JProgressBar jpb)
	{
		this.func = func;
		progress_bar = jpb;
	}

	public void start()
	{
		blinker = new Thread(this);
		blinker.start();
	}


	public void run() {
		try {
			while(func.progress<100)
			{
				func.getThread().join(150);
				progress_bar.setValue((int)func.progress);
			}
			//wait for the current end of the function
			func.getThread().join();
		}catch (InterruptedException e)
		{}
		canvas.functionEnded(func);
		func = null;
	}
}
