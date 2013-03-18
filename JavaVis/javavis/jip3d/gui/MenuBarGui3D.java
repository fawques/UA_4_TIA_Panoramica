package javavis.jip3d.gui;

import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javavis.jip3d.base.FunctionList3D;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;


public class MenuBarGui3D extends JMenuBar {
	private static final long serialVersionUID = 6182348633458105353L;

    /**
	 * @uml.property  name="prop"
	 */
    Properties prop;
    /**
	 * @uml.property  name="paths"
	 */
    Properties paths;
    /**
	 * @uml.property  name="openJIP3D"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem openJIP3D;
    /**
	 * @uml.property  name="saveJIP3D"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem saveJIP3D;
    /**
	 * @uml.property  name="load3DModel"
	 * @uml.associationEnd  multiplicity="(1 1)"
     */
    public JMenuItem load3DModel; 
    /**
	 * @uml.property  name="open3DModel"
	 * @uml.associationEnd  multiplicity="(1 1)"
     */
    public JMenuItem open3DData;
    /**
	 * @uml.property  name="save3DData"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem save3DData;
    /**
	 * @uml.property  name="robot"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem robot;
    /**
	 * @uml.property  name="zenital"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem zenital;
    /**
	 * @uml.property  name="changeView3D"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem changeView3D;
    
    public JMenuItem showTimeLapse;

    public JMenuItem connectKinect;

    public JMenuItem captureStillKinect;
    
    public JMenuItem captureVideoKinect;
    
    public JMenuItem disconnectKinect;
	/**
	 * @uml.property  name="about"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem about;
	/**
	 * @uml.property  name="help"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JMenuItem help;


    public MenuBarGui3D(Gui3D gui3D, FunctionList3D funclist, Properties prop) {
    	super();
        this.prop = prop;

    	FileInputStream fis=null;
        paths = new Properties();
        try {
       		fis = new FileInputStream("path.properties");
        	paths.load(fis);
        	fis.close();
        } catch (IOException e) {System.out.println();}

        JMenu menu;
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        //Build the first menu.
        menu = new JMenu(prop.getProperty("File3D")); //File3D
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Common options to handle 3D files");
        add(menu);

        openJIP3D = new JMenuItem(gui3D.getOpenFileAction(prop.getProperty("OpenJIP3D"))); //OpenJIP3D
        menu.add(openJIP3D);

        saveJIP3D = new JMenuItem(gui3D.getSaveFileAction(prop.getProperty("SaveJIP3D"))); //SaveJIP3D
        menu.add(saveJIP3D);
        
        open3DData = new JMenuItem(gui3D.getOpenFileAction(prop.getProperty("Open3DData")));
        menu.add(open3DData);

        save3DData = new JMenuItem(gui3D.getSaveFileAction(prop.getProperty("Export3D"))); //Export3D
        menu.add(save3DData);

        //Load lightweigh model
        load3DModel = new JMenuItem(gui3D.getOpenFileAction(prop.getProperty("Load3DModel")));
        menu.add(load3DModel);

        //Build second menu in the menu bar.
        menu = new JMenu(prop.getProperty("view3D")); //view3D
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription(prop.getProperty("view3D")); //ViewDesc

        robot = new JMenuItem(prop.getProperty("Robot")); //Robot
        robot.addActionListener(gui3D.getChangeViewAction(prop.getProperty("Robot")));
        menu.add(robot);

        zenital = new JMenuItem(prop.getProperty("Zenital")); //Zenital
        zenital.addActionListener(gui3D.getChangeViewAction(prop.getProperty("Zenital")));
        menu.add(zenital);


        changeView3D = new JMenuItem(prop.getProperty("ChangeView3D")); //ChangeView3D
        changeView3D.addActionListener(gui3D.getChangeViewAction(prop.getProperty("ChangeView3D")));
        menu.add(changeView3D);


        showTimeLapse = new JMenuItem(prop.getProperty("ShowTimeLapse")); //Show TimeLapse
        showTimeLapse.addActionListener(gui3D.getChangeViewAction(prop.getProperty("ShowTimeLapse")));
        menu.add(showTimeLapse);

        add(menu);
        
        //Build kinect menu.
        menu = new JMenu(prop.getProperty("KinectMenu")); 
        
        connectKinect = new JMenuItem(prop.getProperty("connectKinect")); 
        connectKinect.addActionListener(gui3D.getConnectKinect(prop.getProperty("connectKinect")));
        menu.add(connectKinect);
        
        captureStillKinect = new JMenuItem(prop.getProperty("captureKinectStill")); 
        captureStillKinect.addActionListener(gui3D.getCaptureKinect(prop.getProperty("captureKinectStill")));
        captureStillKinect.setEnabled(false);
        menu.add(captureStillKinect);
        
        captureVideoKinect = new JMenuItem(prop.getProperty("captureVideoKinect")); 
        captureVideoKinect.addActionListener(gui3D.getVideoKinect(prop.getProperty("captureVideoKinect")));
        captureVideoKinect.setEnabled(false);
        menu.add(captureVideoKinect);
        
        disconnectKinect = new JMenuItem(prop.getProperty("disconnectKinect")); 
        disconnectKinect.addActionListener(gui3D.getDisconnectKinect(prop.getProperty("disconnectKinect")));
        disconnectKinect.setEnabled(false);
        menu.add(disconnectKinect);
        
        add(menu);

        add(funclist.getFunctionMenu(prop.getProperty("Func3D"), gui3D)); //Func3D
        

		menu = new JMenu(prop.getProperty("Help"));
		add(menu);

		help = new JMenuItem(prop.getProperty("Help"));
		help.setAccelerator(KeyStroke.getKeyStroke("F1"));
		help.setActionCommand("Help");
		help.addActionListener(gui3D);
		menu.add(help);
		about = new JMenuItem(prop.getProperty("About"));
		about.setActionCommand("About");
		about.addActionListener(gui3D);
		menu.add(about);
    }

}
