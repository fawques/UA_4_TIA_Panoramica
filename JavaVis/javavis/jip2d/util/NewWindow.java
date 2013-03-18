package javavis.jip2d.util;

import java.awt.Image;

import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.JIPImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * This class helps OpenWindow function, which opens a new window with the image in the canvas.
 */
public class NewWindow extends JFrame {
	private static final long serialVersionUID = 3352635944490144978L;

	/**
	 * @uml.property  name="img"
	 * @uml.associationEnd  
	 */
	JIPImage img;
	/**
	 * @uml.property  name="pane"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JPanel pane;
	/**
	 * @uml.property  name="pictureScrollPane"
	 * @uml.associationEnd  
	 */
	JScrollPane pictureScrollPane;
	/**
	 * @uml.property  name="menuBar"
	 * @uml.associationEnd  
	 */
	JMenuBar menuBar;	
	
	/**
	* It gets the name of the image and create the panel.
	*/
	public NewWindow(JIPImage image) throws JIPException {
		super("Image: " + image.getName());

		Image result;
		img = image;

		result = JIPToolkit.getAWTImage(img);
		ScrollablePicture pic = new ScrollablePicture(new ImageIcon(result), 5);

		pane = new JPanel();
		JScrollPane pictureScrollPane = new JScrollPane(pic);
		pane.add(pictureScrollPane);
		setContentPane(pane);
	}
	
	/**
	 * It constructs an empty new window
	 */
	public NewWindow() {
		img = null;
		pane = new JPanel();	
		pictureScrollPane = new JScrollPane();
		menuBar = null;
	}
	
	/**
	 *It changes the image in the panel
	 */
	public void setImage(JIPImage image) throws JIPException {	
		Image result;
		img = image;

		result = JIPToolkit.getAWTImage(img);
		ScrollablePicture pic = new ScrollablePicture(new ImageIcon(result), 0);
			
		pictureScrollPane = new JScrollPane(pic);		
		pane.add(pictureScrollPane,0);	
		pictureScrollPane.repaint();
		pane.repaint();
		this.repaint();
		setContentPane(pane);
	}
}
