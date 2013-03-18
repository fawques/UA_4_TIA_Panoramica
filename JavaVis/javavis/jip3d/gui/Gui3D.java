package javavis.jip3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javavis.Commons;
import javavis.Gui;
import javavis.base.Kinect;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.FunctionList3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.gui.dataobjects.PointSet3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import com.centerkey.utils.BareBonesBrowserLaunch;

/**
 * Class Gui3D
 */
public class Gui3D extends JPanel implements ActionListener, ItemListener, ChangeListener{
	private static final long serialVersionUID = 624955885721583016L;

	/**
	 * @uml.property  name="lpx"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel lpx;

	/**
	 * @uml.property  name="lpy"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel lpy;

	/**
	 * @uml.property  name="lpz"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel lpz;

	/**
	 * @uml.property  name="lox"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel lox;

	/**
	 * @uml.property  name="loy"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel loy;

	/**
	 * @uml.property  name="loz"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel loz;
    /**
	 * @uml.property  name="dialogs"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public MyDialog dialogs;
    /**
	 * @uml.property  name="canvas"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="menu:javavis.jip3d.gui.MyCanvas3D"
	 */
    MyCanvas3D canvas;
    /**
	 * @uml.property  name="father"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="g3d:javavis.Gui"
	 */
    Gui father;

    // screen data panel
    /**
	 * @uml.property  name="scr_list"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip3d.gui.ScreenData"
	 */
    JComboBox scr_list;


    /**
	 * @uml.property  name="dataPanel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    DataPanel dataPanel;
	/**
	 * @uml.property  name="object_selected"
	 * @uml.associationEnd  
	 */
	private ScreenData object_selected; //screen data object name that is already in use

	/**
	 * @uml.property  name="maxSpinner"
	 */
	public double maxSpinner = 99;
	/**
	 * @uml.property  name="minSpinner"
	 */
	public double minSpinner = 0.1;
    /**
	 * @uml.property  name="scrDataPanel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JPanel scrDataPanel;
    /**
	 * @uml.property  name="current_func"
	 * @uml.associationEnd  
	 */
    private Function3D current_func;
	/**
	 * @uml.property  name="bcancelfunc"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JButton bcancelfunc;
	/**
	 * @uml.property  name="pizda"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JPanel pizda;
	/**
	 * @uml.property  name="func_name"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel func_name;
	/**
	 * @uml.property  name="func_name_max_width"
	 */
	int func_name_max_width = 12;
	/**
	 * @uml.property  name="progress_bar"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JProgressBar progress_bar;
	/**
	 * @uml.property  name="total_time"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel total_time;
	
	/**
	 * @uml.property  name="infoData"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JDialog infoData;

	/**
	 * @uml.property  name="noobject"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	ScreenData noobject;
	/**
	 * @uml.property  name="dEBUG"
	 */
	boolean DEBUG;

    /**
	 * @uml.property  name="prop"
	 */
    Properties prop;
    /**
	 * @uml.property  name="paths"
	 */
    Properties paths;
    
    /**
     * This member allows to manage Time Lapse window
     */
    TimeLapse tl=null;
    
    /**
     * boolean value to show accumulated time lapse
     */
    boolean tlAccum;
    
    int currentTL;

	/**
	 * @uml.property  name="function_list"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	FunctionList3D function_list;
	
	private Kinect kinect;
	
	GraphicsConfigTemplate3D gct;
	GraphicsConfiguration[] gc;

    public Gui3D(Gui f, Properties pr, Properties iPaths, boolean debug)
    {
    	super(new BorderLayout());
        prop = pr;
		function_list = new FunctionList3D();

    	dialogs = new MyDialog(f,prop);

    	father = f;
        lpx = new JLabel();
        lpy = new JLabel();
        lpz = new JLabel();
        lox = new JLabel();
        loy = new JLabel();
        loz = new JLabel();

        // Create Canvas3D
    	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        gc =  gs[0].getConfigurations();
    	gct = new GraphicsConfigTemplate3D();
        DEBUG = debug;
    	canvas = new MyCanvas3D(gct.getBestConfiguration(gc), this, prop, DEBUG);
    	addCanvas(canvas);

    	noobject = new PointSet3D(new ScreenOptions());
    	object_selected = noobject;

        paths=iPaths;

        JLabel laux;
        //Create the content-pane-to-be.
        setOpaque(true);
        setBackground(new Color(182, 182, 182));

        pizda = new JPanel(new BorderLayout(3,3));
        pizda.setBackground(new Color(182, 182, 182));

        JPanel pfrustrum = new JPanel(new GridLayout(4, 0, 2, 5));
        pfrustrum.setBackground(new Color(222, 222, 222));
        JPanel pRow = new JPanel(new GridLayout(0,3, 2, 5));


        laux = new JLabel("pos x", JLabel.CENTER);
        pRow.add(laux);
        laux = new JLabel("pos y", JLabel.CENTER);
        pRow.add(laux);
        laux = new JLabel("pos z", JLabel.CENTER);
        pRow.add(laux);
        pfrustrum.add(pRow);
        pRow = new JPanel(new GridLayout(0,3));
        lpx.setText((" "+canvas.globalx+"00000").substring(0, 6));
        lpy.setText((" "+canvas.globaly+"00000").substring(0, 6));
        lpz.setText((" "+canvas.globalz+"00000").substring(0, 6));
        pRow.add(lpx);
        pRow.add(lpy);
        pRow.add(lpz);
        pfrustrum.add(pRow);
        pRow = new JPanel(new GridLayout(0,3));
        laux = new JLabel("head x", JLabel.CENTER);
        pRow.add(laux);
        laux = new JLabel("head y", JLabel.CENTER);
        pRow.add(laux);
        laux = new JLabel("head z", JLabel.CENTER);
        pRow.add(laux);
        pfrustrum.add(pRow);
        pRow = new JPanel(new GridLayout(0,3));
        lox.setText((" "+canvas.orx+"00000").substring(0, 6));
        loy.setText((" "+canvas.ory+"00000").substring(0, 6));
        loz.setText((" "+canvas.orz+"00000").substring(0, 6));
        pRow.add(lox);
        pRow.add(loy);
        pRow.add(loz);
        pfrustrum.add(pRow);

        //Screen Object Selection
        scrDataPanel = new JPanel(new BorderLayout(1,1));
        dataPanel = new DataPanel(prop, this);
        String []data = {prop.getProperty("NoObject")};
        scr_list = new JComboBox(data);
        scr_list.setActionCommand("Lista");
        scr_list.addActionListener(this);
        createSelect(new ArrayList<ScreenData>());

        scrDataPanel.add("North", scr_list);
    	scrDataPanel.add("Center", dataPanel);

        JPanel aux = new JPanel(new BorderLayout(1, 1));
        aux.add("Center", functionPanel(null));

        pizda.add("North",pfrustrum);
        pizda.add("Center",scrDataPanel);
        pizda.add("South", aux);

        infoData = new JDialog(father);
        infoData.add(pizda);
        infoData.setSize(200, 550);
        infoData.setResizable(false);
        infoData.setJMenuBar(father.getMenuBarGui3D());
        infoData.setLocation(600, 0);
        infoData.setDefaultCloseOperation(WindowConstants. DO_NOTHING_ON_CLOSE);
        setVisAdditionalWindows(true);

        add(canvas, BorderLayout.CENTER);
    }

    public Gui getFather() {
    	return father;
    }
	public void setVisAdditionalWindows(boolean show) {
        infoData.setVisible(show);
	}

    public void addCanvas(MyCanvas3D can)
    {
    	canvas = can;
    }

    public void createSelect(ArrayList<ScreenData> names)
    {
    	noobject.name = prop.getProperty("NoObject"); //NoObject

    	//Clean objects
    	scr_list.removeAllItems();

    	if(names.size()==0)
    	{
            scr_list.addItem(noobject);
            dataPanel.updateContent(null);
            object_selected = noobject;
    	}
    	else
    	{
    		for(ScreenData scrd: names)
    		{
    			scr_list.addItem(scrd);
    		}
    		dataPanel.updateContent(object_selected.scr_opt);
    	}
		scr_list.setSelectedItem(object_selected);
    }
    

    protected JPanel functionPanel(Function3D func)
    {
    	JPanel ret = new JPanel(new BorderLayout());
		JPanel pmain = new JPanel(new GridLayout(4,0));
		JPanel pbot = new JPanel();
		bcancelfunc = new JButton(prop.getProperty("StopFunc3D")); //StopFunc3D
		bcancelfunc.setActionCommand("CancelFunc");
		bcancelfunc.addActionListener(this);
		pbot.add("Center", bcancelfunc);
    	int max_chars;

		progress_bar = new JProgressBar();
		progress_bar.setValue(0);
		total_time = new JLabel("");

		if(func!=null)
		{
			max_chars = func.name.length()<func_name_max_width?func.name.length():func_name_max_width;
			func_name = new JLabel(prop.getProperty("Running3D")+" "+func.name.substring(0, max_chars)); //Running3D
			pmain.add(func_name);
			bcancelfunc.setEnabled(true);
		}
		else
		{
			func_name = new JLabel(prop.getProperty("NoFunction3D")); //NoFunction3D
			pmain.add(func_name);
			bcancelfunc.setEnabled(false);
		}
		pmain.add(progress_bar);
		pmain.add(total_time);

		ret.add("Center", pmain);

    	return ret;
    }

    public void startFunction(String name)
    {
    	int max_chars = name.length()<func_name_max_width?name.length():func_name_max_width;
		func_name.setText(prop.getProperty("Running3D")+" "+name.substring(0, max_chars)); //Running3D
		progress_bar.setValue(0);
		total_time.setText("");
    }

    public void endFunctionAction(String name, long time)
    {
    	float d_time;
    	int max_chars;
    	if(name!=null)
    	{
    		max_chars = name.length()<func_name_max_width?name.length():func_name_max_width;
    		d_time = time/1000.0f;
    		func_name.setText(prop.getProperty("Finished3D")+" "+name.substring(0, max_chars)); //Finished3D
    		progress_bar.setValue(0);
    		total_time.setText(prop.getProperty("Time3D")+": "+d_time+" "+prop.getProperty("TUnits3D")); //Time3D //TUnits3D
    	}
    	else
    		func_name.setText(prop.getProperty("NoFunction3D"));
    }


    public void poseUpdate(double px, double py, double pz, double ox, double oy, double oz)
    {
    	double EPS = 0.001;
    	String text;

    	if(px>-EPS && px<EPS)
    		text = " 0.000000";
    	else
    		text = " "+px+"00000";
    	lpx.setText(text.substring(0, 6));

    	if(py>-EPS && py<EPS)
    		text = " 0.000000";
    	else
    		text = " "+py+"00000";
    	lpy.setText(text.substring(0, 6));

    	if(pz>-EPS && pz<EPS)
    		text = " 0.000000";
    	else
    		text = " "+pz+"00000";
    	lpz.setText(text.substring(0, 6));

    	if(ox>-EPS && ox<EPS)
    		text = " 0.000000";
    	else
    		text = " "+ox+"00000";
    	lox.setText(text.substring(0, 6));

    	if(oy>-EPS && oy<EPS)
    		text = " 0.000000";
    	else
    		text = " "+oy+"00000";
    	loy.setText(text.substring(0, 6));

    	if(oz>-EPS && oz<EPS)
    		text = " 0.000000";
    	else
    		text = " "+oz+"00000";
    	loz.setText(text.substring(0, 6));
    }

    public JProgressBar getProgressBar()
    {
    	return this.progress_bar;
    }

    public ScreenData getObjectSelected()
    {
    	if(object_selected != noobject)
    		return object_selected;
    	return null;
    }
    
    public OpenFileAction getOpenFileAction (String name) {
    	return new OpenFileAction(name);
    }

    public SaveFileAction getSaveFileAction (String name) {
    	return new SaveFileAction(name);
    }

    public ChangeViewAction getChangeViewAction (String name) {
    	return new ChangeViewAction(name);
    }

    public ConnectKinect getConnectKinect (String name) {
    	return new ConnectKinect(name);
    }

    public CaptureKinect getCaptureKinect (String name) {
    	return new CaptureKinect(name);
    }

    public VideoKinect getVideoKinect (String name) {
    	return new VideoKinect(name);
    }

    public DisconnectKinect getDisconnectKinect (String name) {
    	return new DisconnectKinect(name);
    }

    public void help () {
    	BareBonesBrowserLaunch.openURL("http://sourceforge.net/apps/mediawiki/javavis/?source=navbar");
    }
    

    class OpenFileAction extends AbstractAction
    {
    	String nombre;
		private static final long serialVersionUID = 4223870651668267434L;

		public OpenFileAction(String name)
    	{
			super(name);
    		nombre = name;
    	}

		public void actionPerformed(ActionEvent e) {
			File data;
			String path;
			String title = prop.getProperty("Import3D");
			ArrayList<String> extensions;

			extensions = new ArrayList<String>();
			extensions.add("xml");
			extensions.add("jip3d");
			path = paths.getProperty("OpenJIP3D"); //default option
			
			data = dialogs.fileChooser(path, title+" "+nombre.substring(6), extensions, true); //Import3D
			if (data!=null)
			{
				if (e.getSource() == father.getMenuBarGui3D().openJIP3D) {
					canvas.openJIP3D(data.getName(), data.getParent()+"/");
				}
				
				if (e.getSource() == father.getMenuBarGui3D().load3DModel) {
					canvas.addScreen3DModel(data.getName(), data.getParent()+"/");
				}
				else if (e.getSource() == father.getMenuBarGui3D().open3DData) {
					canvas.addScreenData(data.getName(), data.getParent()+"/");
				}
				paths.setProperty("OpenJIP3D", data.getParent()+"/");
			}
		}
    }

    class SaveFileAction extends AbstractAction
    {
		private static final long serialVersionUID = -6526129533901736429L;
		String name;

		public SaveFileAction(String n)
    	{
    		super(n);
    		name = n;
    	}

		public void actionPerformed(ActionEvent e) {
			File data;
			String path;
			String fname;
			String action = e.getActionCommand();
			String title = prop.getProperty("ExportObject3D"); //ExportObject3D
			ArrayList<String> types = new ArrayList<String>();
			if(DEBUG)
				System.out.println("Objeto: "+object_selected.name);

			if(object_selected!=null)
			{
				if(!name.equals(prop.getProperty("Export3D")))
				{
					if(DEBUG)
						System.out.println("Guardo jip "+action);
					types.add("jip3d");
					title = prop.getProperty("SaveJIP3D"); //SaveJIP3D
				}
				path = paths.getProperty(name);
				do
				{
					data = dialogs.fileChooser(path, title, types, false);
				} while (data!=null && data.exists() && !dialogs.confirm(prop.getProperty("Thefile3D")+" "+data.getName()+" "+prop.getProperty("Exist3D"), title+" "+name)); //Thefile3D //Exist3D
				if (data!=null)
				{
					paths.setProperty(name, data.getParent()+"/");
					if(name.equals(prop.getProperty("Export3D")))
						canvas.saveScreenData(object_selected, data.getName(), data.getParent()+"/");
					else
					{
						fname = data.getName();
						if(fname.lastIndexOf('.')==-1 || !fname.substring(fname.lastIndexOf('.')+1).toLowerCase().equals("jip3d"))
							fname += ".jip3d";
						canvas.saveJIP3D(object_selected, fname, data.getParent()+"/");
					}
				}
			}
			else
				dialogs.error(prop.getProperty("SaveError3D"), title); //SaveError3D
		}
    }

    class ChangeViewAction extends AbstractAction
    {
		private static final long serialVersionUID = -7174213429633165458L;

		public ChangeViewAction(String nombre)
		{
			super(nombre);
		}

		public void actionPerformed(ActionEvent e) {
			double px, py, pz, ox, oy, oz;
			double []result;
			Vector3f pos;
			double module;
			double []orientation = new double[3];
			canvas.updateView();

			if(e.getActionCommand().equals(prop.getProperty("Zenital")))
			{
				canvas.changeToCenitalView();
			}
			else if(e.getActionCommand().equals(prop.getProperty("Robot")))
			{
				canvas.changeToRobotView();
			}
			else if(e.getActionCommand().equals(prop.getProperty("ShowTimeLapse")))
			{
				if (canvas.getNumberTimeStamp()>=2) {
					tl = new TimeLapse(prop, father, canvas.getMinTimeStamp(), canvas.getMaxTimeStamp());
	
					tl.setSize(200, 500);
					tl.setResizable(false);
					tl.setLocation(800, 0);
					tl.setVisible(true);
				}
			}
			else
			{
				px = canvas.globalx;
				py = canvas.globaly;
				pz = canvas.globalz;
				ox = canvas.orx;
				oy = canvas.ory;
				oz = canvas.orz;
				result = dialogs.changePose(px, py, pz, ox, oy, oz);
				pos = new Vector3f((float)result[0], (float)result[1], (float)result[2]);
				module = Math.sqrt(result[3]*result[3] + result[4]*result[4] + result[5]*result[5]);
				orientation[0] = -result[3] / module;
				orientation[1] = -result[4] / module;
				orientation[2] = -result[5] / module;
				canvas.changeView(pos, orientation);
			}
		}
    }

    class ConnectKinect extends AbstractAction
    {
		private static final long serialVersionUID = 4225037780608174641L;

		public ConnectKinect(String name) {
		}
		
		public void actionPerformed(ActionEvent e) {
			kinect = new Kinect();
			if (kinect.isConnected()) {
				father.getMenuBarGui3D().connectKinect.setEnabled(false);
				father.getMenuBarGui3D().captureStillKinect.setEnabled(true);
				father.getMenuBarGui3D().captureVideoKinect.setEnabled(true);
				father.getMenuBarGui3D().disconnectKinect.setEnabled(true);
			}
		}
    }

    class DisconnectKinect extends AbstractAction
    {
		private static final long serialVersionUID = 4225037780608174641L;

		public DisconnectKinect(String nombre) {
		}
		
		public void actionPerformed(ActionEvent e) {
			kinect.disconnect();
			if (!kinect.isConnected()) {
				father.getMenuBarGui3D().connectKinect.setEnabled(true);
				father.getMenuBarGui3D().captureStillKinect.setEnabled(false);
				father.getMenuBarGui3D().captureVideoKinect.setEnabled(false);
				father.getMenuBarGui3D().disconnectKinect.setEnabled(false);
			}
		}
    }

    class CaptureKinect extends AbstractAction
    {
		private static final long serialVersionUID = -7174213429633165458L;

		public CaptureKinect(String name) {
		}
		
		public void actionPerformed(ActionEvent e) {
			PointSet3D ps3d = kinect.getPointSet3D();
			if (ps3d!=null) {
		        canvas.scr_data.add(ps3d);
		        canvas.createSelection();
		        canvas.reDraw();
			}
			else {
				System.out.println("No Kinect data");
			}
		}
    }

    class VideoKinect extends AbstractAction
    {
		private static final long serialVersionUID = -6125691323793828507L;
	
		public VideoKinect(String nombre) {
		}
		
		public void actionPerformed(ActionEvent e) {
			KinectLife vk = new KinectLife(gct.getBestConfiguration(gc), kinect);
			
			vk.setVisible(true);
			vk.toFront();
			vk.updateView();
		}
    }

    class SelectObjectAction extends AbstractAction {
		private static final long serialVersionUID = 510859158363323382L;
		int position;
		public SelectObjectAction(String name, int pos) {
			super(name);
			position = pos;
		}

		public void actionPerformed(ActionEvent arg0) {
			SelectDialog sdiag = new SelectDialog(dialogs.owner, canvas.scr_data.get(position).scr_opt);
			sdiag.setModal(true);
			sdiag.setVisible(true);

			if(sdiag.delete)
				canvas.removeScreenData(canvas.scr_data.get(position));
			else 
				canvas.reDraw(canvas.scr_data.get(position));
		}

    }

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals("Color")) {
			Color newColor = JColorChooser.showDialog(father, "Choose Object Color",
                    dataPanel.jbt_color.getBackground());
			if(newColor != null)
			{
				dataPanel.jbt_color.setBackground(newColor);
				object_selected.scr_opt.color = new Color3f(newColor);
				object_selected.scr_opt.hasChanged = true;
				canvas.reDraw(object_selected);
			}
		}

		if(action.equals("Remove Object"))
		{
			if(dialogs.confirm("Object will be removed, are you sure?", "Remove Object"))
				canvas.removeScreenData(object_selected);
		}

		if(action.equals("Lista"))
		{
			ArrayList<ScreenData> data_list; 
			int count;
			ScreenData object_name = (ScreenData)scr_list.getSelectedItem();
			if (object_name != null)
			{
				if((object_name).equals(noobject))
				{
					dataPanel.updateContent(null);
				}
				else
				{
					data_list = new ArrayList<ScreenData>();
					for(count=0;count<scr_list.getItemCount();count++)
						data_list.add((ScreenData)scr_list.getItemAt(count));
					object_selected = object_name;
					createSelect(data_list);
				}
			}
			else
			{
				object_selected = null;
			}
		}
		if(action.equals("CancelFunc"))
		{
			if(getCurrent_func()!=null)
			{
				System.out.println("Cancelo funcion: "+getCurrent_func().name);
				getCurrent_func().stop();
			}
			setCurrent_func(null);
		}
		if(action.startsWith("F_")) //launch function
		{
			if(getCurrent_func() == null)
			{
				String aux = action.substring(2);
				Function3D f=null;
				try {
					f = (Function3D)Class.forName("javavis.jip3d.functions." + aux).newInstance();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				setCurrent_func(f);
				getCurrent_func().setCanvas(canvas);
				getCurrent_func().name = aux;
				getCurrent_func().dialog = this.dialogs;
				this.startFunction(getCurrent_func().name);
				canvas.func_panel.setFunction(getCurrent_func(), getProgressBar());
				if(canvas.launchFunction(getCurrent_func(), this.getObjectSelected()))
					canvas.func_panel.start();
				else
				{
					setCurrent_func(null);
					endFunctionAction(null, 0);
				}

			}
		}
		if(action.equals("Remove Object")) //cancel function
		{
			if(getCurrent_func()!=null)
			{
				getCurrent_func().stop();
			}
			return;
		}
		if (action.equals("About")) {
			Commons.showAbout();
		}
		if (action.equals("Help")) {
			BareBonesBrowserLaunch.openURL("http://sourceforge.net/apps/mediawiki/javavis/?source=navbar");
		}
			
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		ScreenData datum;

		if(source == dataPanel.jcb_color)
		{
			datum = object_selected;
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				dataPanel.jbt_color.setEnabled(true);
				datum.scr_opt.global_color = true;
				datum.scr_opt.hasChanged = true;
			}
			else
			{
				dataPanel.jbt_color.setEnabled(false);
				datum.scr_opt.global_color = false;
				datum.scr_opt.hasChanged = true;
			}
			canvas.reDraw(datum);
		}
		else if(source == dataPanel.jcb_visible)
		{
			datum = object_selected;
			datum.scr_opt.is_visible = dataPanel.jcb_visible.isSelected();
			canvas.reDraw(datum);
		}
		else if(source == dataPanel.jcb_improve)
		{
			datum = object_selected;
			datum.scr_opt.improved = dataPanel.jcb_improve.isSelected();
			datum.scr_opt.hasChanged = true;
			canvas.reDraw(datum);
		}
		else if (tl!=null && source == tl.acumulated)
		{
			tlAccum=tl.acumulated.isSelected();
			canvas.reDraw(currentTL, tlAccum);
		}

	}

	public void stateChanged(ChangeEvent arg0) {
		double value;

		if(arg0.getSource().equals(dataPanel.spinner)) 
		{
			value = (Double)dataPanel.spinner.getValue();
			object_selected.scr_opt.width = value;
			object_selected.scr_opt.hasChanged = true;
			canvas.reDraw(object_selected);
		}
		if(arg0.getSource().equals(dataPanel.jsp_alpha)) 
		{
			value = (Double)dataPanel.jsp_alpha.getValue();
			object_selected.scr_opt.alpha = value;
			canvas.reDraw(object_selected);
		}
		if(arg0.getSource().equals(dataPanel.jsp_length)) 
		{
			value = (Double)dataPanel.jsp_length.getValue();
			object_selected.scr_opt.length = value;
			canvas.reDraw(object_selected);
		}
		if(tl!=null && arg0.getSource().equals(tl.timeBar)) 
		{
			currentTL=tl.timeBar.getValue();
			String val = " "+currentTL;
			tl.currentT.setText(val);
			canvas.reDraw(currentTL, tlAccum);
		}
	}

	public MyCanvas3D getCanvas() {
		return canvas;
	}

	public void setCanvas(MyCanvas3D canvas) {
		this.canvas = canvas;
	}

	public void setCurrent_func(Function3D current_func) {
		this.current_func = current_func;
	}

	public Function3D getCurrent_func() {
		return current_func;
	}
}
