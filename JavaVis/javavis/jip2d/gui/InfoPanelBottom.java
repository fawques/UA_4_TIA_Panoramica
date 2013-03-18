package javavis.jip2d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.Properties;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;

import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * Class which has the information about the elements of the
 * program, the cursor position, its value, band and numFrame where
 * we are worked etc...
 */
public class InfoPanelBottom extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1428532347396392736L;
	
	private static Logger logger = Logger.getLogger(InfoPanelBottom.class);

	/**
	 * The canvas
	 * @uml.property  name="canvas"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="infoBottom:javavis.jip2d.gui.Canvas2D"
	 */
	Canvas2D canvas;

	/**
	 * Position label
	 * @uml.property  name="pos"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel pos;

	/**
	 * Current value label
	 * @uml.property  name="value"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel value;

	/**
	 * Frame size label
	 * @uml.property  name="tamFrame"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel tamFrame;

	/**
	 * Deployed of the selected numFrame
	 * @uml.property  name="frameSel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JComboBox frameSel;

	/**
	 * Deployed of the selected band
	 * @uml.property  name="bandSel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JComboBox bandSel;

	/**
	 * Progress Bar
	 * @uml.property  name="pBar"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JProgressBar pBar;
	
	/**
	 * Properties for language
	 * @uml.property  name="prop"
	 */
	Properties prop;

	/**
	 * Class constructor. It creates the information panel at the bottom of
	 * the main window
	 * @param c Geometric canvas
	 */
	public InfoPanelBottom(Canvas2D c, Properties propi) {
		canvas = c;
		prop = propi;
		
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());
		
		tamFrame = new JLabel(prop.getProperty("FrameSize")+":     ");
		pos = new JLabel(prop.getProperty("Coords")+":         ");
		value = new JLabel(prop.getProperty("Value")+":          ");
		
		JPanel auxPanel1 = new JPanel();
		JPanel auxPanel2 = new JPanel();
		JPanel auxPanel21 = new JPanel();
		JPanel auxPanel22 = new JPanel();
		JPanel auxPanel23 = new JPanel();
		
		auxPanel1.setLayout(new BoxLayout(auxPanel1, BoxLayout.Y_AXIS));
		auxPanel1.add(tamFrame);
		auxPanel1.add(pos);
		auxPanel1.add(value);
		
		auxPanel21.setLayout(new BorderLayout());
		auxPanel22.setLayout(new BorderLayout());
		auxPanel23.setLayout(new BorderLayout());
		auxPanel2.setLayout(new BoxLayout(auxPanel2, BoxLayout.Y_AXIS));
		
		JLabel labFrame = new JLabel (prop.getProperty("Frame")+":  ");
		auxPanel21.add(labFrame,BorderLayout.WEST);
				
		frameSel = new JComboBox();
		frameSel.addItem("[ "+prop.getProperty("Nosequence")+" ]");
		frameSel.setPreferredSize(new Dimension(250,20));
		auxPanel21.add(frameSel,BorderLayout.EAST);
					
		JLabel labBanda = new JLabel (prop.getProperty("Band")+":   ");
		auxPanel22.add(labBanda,BorderLayout.WEST);
		
		bandSel = new JComboBox();
		bandSel.addItem("[ "+prop.getProperty("Nosequence")+" ]");
		bandSel.setPreferredSize(new Dimension(250,20));
		auxPanel22.add(bandSel,BorderLayout.EAST);
		
		auxPanel2.add(auxPanel21);
		auxPanel2.add(auxPanel22);
		
		JLabel auxpbar = new JLabel (prop.getProperty("Progress")+":");
		auxPanel23.add(auxpbar,BorderLayout.WEST);
		//The progress bar has a minimum value 0 and a maximum of 100
		pBar = new JProgressBar(0, 100); 
		pBar.setPreferredSize(new Dimension(250,15));
		pBar.setStringPainted(true);
		auxPanel23.add(pBar,BorderLayout.EAST);
		auxPanel2.add(auxPanel23);
		
		add(auxPanel1,BorderLayout.WEST);
		add(auxPanel2,BorderLayout.EAST);
		
		bandSel.addActionListener(this);
		frameSel.addActionListener(this);
	}

	/**
	 * Method which put information of values of the new sequence in the panel	
	 * @param s New sequence
	 */
	public void assocSequence (Sequence s) {
		frameSel.removeAllItems();
		bandSel.removeAllItems();
		if (s == null) {
			frameSel.addItem("[ "+prop.getProperty("Nosequence")+" ]");
			bandSel.addItem("[ "+prop.getProperty("Nosequence")+" ]");
			return;
		}
		
		try {
			for (int i = 0; i < s.getNumFrames(); i++) {
				String type = s.getFrame(i).getType().toString();
				String nameF = s.getFrame(i).getName();
				if (nameF.length() > 10) 
					nameF = nameF.substring(0, 7) + "...";
				frameSel.addItem(i + ":" + type + ":" + nameF);
			}
			JIPImage aux = s.getFrame(0);
			updateBandsFrame(aux, 0);
		}catch (JIPException e) {logger.error(e);}
	}
	

	/**
	 * Method which updates the value of the bands
	 * @param img New image
	 * @param n Number of bands
	 */
	public void updateBandsFrame(JIPImage img, int n) {
		bandSel.removeAllItems();
		if (img instanceof JIPBmpColor) {
			bandSel.addItem("COLOR-Bands RGB");
		}
		else if (img instanceof JIPImgGeometric) {
			bandSel.addItem("Geometric image");
		}
		else {
			for (int i = 0; i < ((JIPImgBitmap)img).getNumBands(); i++)
				bandSel.addItem(prop.getProperty("Band")+" " + i);
			tamFrame.setText(prop.getProperty("FrameSize")+": " + img.getWidth() + "x" + img.getHeight());
		}
	}
	
	/**
	 * Method which updates the value of the progress bar
	 * @param value Integer between 0 and 100
	 */
	public void setBar (int value) {
		if (value >=0 && value <=100)
			pBar.setValue(value);
	}

	/**
	 * Method which updates the information of the panel, it can be the width,
	 * the height, number of segments, image, bands, etc...
	 * @param x current X 
	 * @param y current Y
	 * @param img Image
	 * @param b Number of bands
	 */
	public void updateInfo(int x, int y, JIPImage img, int b) {
		try {
			if (img==null) {
				tamFrame.setText(prop.getProperty("FrameSize")+":     ");
				value.setText(prop.getProperty("Value")+": <no image>");
			}
			else if (x < img.getWidth() && x >= 0 && y < img.getHeight() && y >= 0) {
				tamFrame.setText(prop.getProperty("FrameSize")+": ["+img.getWidth()+", "+img.getHeight()+"]");
				pos.setText(prop.getProperty("Coords")+": (" + x + "," + y + ")");
				if (img.getType() == ImageType.FLOAT) {
					value.setText(prop.getProperty("Value")+": <" + 
							Float.toString(((JIPBmpFloat)img).getPixelFloat(b, x, y))+ ">");
				} else if (img.getType() == ImageType.COLOR) {
					value.setText(prop.getProperty("Value")+": <" + (int)((JIPBmpColor)img).getPixelRed(x, y) + ","
							+ (int)((JIPBmpColor)img).getPixelGreen(x, y) + "," + (int)((JIPBmpColor)img).getPixelBlue(x, y) + ">");
				} else if (img instanceof JIPImgBitmap){
					value.setText(prop.getProperty("Value")+": <" + (int)((JIPImgBitmap)img).getPixel(b, x, y) + ">");
				} else { // Geometric type
					value.setText(prop.getProperty("Value")+":<Geom>");
				}
			} else {
				tamFrame.setText(prop.getProperty("FrameSize")+": ["+img.getWidth()+", "+img.getHeight()+"]");
				pos.setText(prop.getProperty("Coords")+": ( OUT )     ");
				value.setText(prop.getProperty("Value")+": ( OUT )");
			}
		}catch (JIPException e) {logger.error(e);}
	}


	/**
	 * Method which selects a numFrame which number is passed as parameter
	 * @param nframe Number of numFrame
	 */
	public void setFrame(int nFrame) {
		frameSel.setSelectedIndex(nFrame);
	}

	/**
	 * It captures the events  produced in the bottom panel
	 * @param e Event
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==frameSel) {
			int sel=frameSel.getSelectedIndex();
			if (sel<0) sel=0;
			try {
				canvas.changeToFrame(sel);
			} catch (JIPException ex) {logger.error("This never must show "+ex);}
		}
		if (e.getSource()==bandSel) {
			int sel=bandSel.getSelectedIndex();
			if (sel<0) sel=0;
			try {
				canvas.changeBand(sel);
			}catch(JIPException ex) {logger.error("This never must show "+ex);}
		}
	}
}
