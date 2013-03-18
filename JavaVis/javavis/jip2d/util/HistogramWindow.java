package javavis.jip2d.util;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import javavis.base.Dialog;
import javavis.base.ImageType;
import javavis.base.JIPException;
import javavis.base.JIPToolkit;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

import javax.swing.*;

import org.apache.log4j.Logger;

/**
 * Class to calculate and show histogram results for the selected numFrame or the sequence. 
 * Also it is possible to obtain the result as an image or as an ASCII text file.
 */
public class HistogramWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 3734337464283449111L;
	
	private static Logger logger = Logger.getLogger(HistogramWindow.class);

	/**
	 * Menu Bar
	 * @uml.property  name="menuBar"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JMenuBar menuBar;

	/**
	 * Input image
	 * @uml.property  name="img"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JIPImgBitmap img;

	/**
	 * Output image
	 * @uml.property  name="res"
	 * @uml.associationEnd  
	 */
	JIPImgBitmap res;

	/**
	 * Intensity tag
	 * @uml.property  name="intensity"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel intensity;

	/**
	 * Number of occurrences
	 * @uml.property  name="occurrences"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel occurrences;

	/**
	 * Upper bound
	 * @uml.property  name="upper_bound"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JLabel upper_bound;

	/**
	 * Panel
	 * @uml.property  name="pane"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	JPanel pane;

	/**
	 * Mouse Listener
	 * @uml.property  name="mml"
	 * @uml.associationEnd  multiplicity="(1 1)" inverse="this$0:javavis.jip2d.util.HistogramWindow$MyMouseListener"
	 */
	MyMouseListener mml;

	/**
	 * Aux var
	 * @uml.property  name="max"
	 */
	int max;
	/**
	 * Number of items
	 * @uml.property  name="nItems"
	 */
	int nItems;
	/**
	 * Aux var. Indicates if exists calculated histogram
	 * @uml.property  name="his"
	 */
	boolean his;
	/**
	 * Aux var. Indicates if exists Brightness histogram
	 * @uml.property  name="hLum"
	 */
	boolean hLum;
	/**
	 * Aux var. Indicates if exists RED histogram
	 * @uml.property  name="hRed"
	 */
	boolean hRed;
	/**
	 * Aux var. Indicates if exists GREEN histogram
	 * @uml.property  name="hGreen"
	 */
	boolean hGreen;
	/**
	 * Aux var. Indicates if exists BLUE histogram
	 * @uml.property  name="hBlue"
	 */
	boolean hBlue;
	/**
	 * Aux var. Indicates if exists Byte histogram
	 * @uml.property  name="hByte"
	 */
	boolean hByte;
	/**
	 * Aux var. Indicates if exists Word histogram
	 * @uml.property  name="hWord"
	 */
	boolean hWord;
	/**
	 * Aux var. Indicates if exists Real histogram
	 * @uml.property  name="hReal"
	 */
	boolean hReal;
	/**
	 * Aux var. Indicates the number of intensities of real type
	 * @uml.property  name="nIntR"
	 */
	int nIntR;
	/**
	 * Aux var
	 * @uml.property  name="maximum"
	 */
	int maximum;
	/**
	 * Aux var
	 * @uml.property  name="div"
	 */
	int div;
	/**
	 * Array of points
	 * @uml.property  name="points" multiplicity="(0 -1)" dimension="1"
	 */
	int[] points;
	/**
	 * Array of points (integer)
	 * @uml.property  name="pointsW" multiplicity="(0 -1)" dimension="1"
	 */
	int[] pointsW;
	/**
	 * Array of points (floats)
	 * @uml.property  name="pointsF" multiplicity="(0 -1)" dimension="1"
	 */
	float[] pointsF;

	/**
	 * Array of occurrences and intensities
	 * @uml.property  name="pointsR"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	RealDataHistogram[] pointsR;

	/**
	* Class Constructor. All needed data structures are initialized. 
	* Some structures are dependent of the image type. Others are common for any 
	* type of image. The treated types are : [COLOR, BYTE, SHORT y FLOAT]
	* According to the type:
	* [COLOR]: This type creates a menu with four different histograms: 
	* Brightness histogram, result of the average of the histograms RGB.
	* RED Histogram, GREEN Histogram, BLUE Histogram, For all three corresponding bands.
	* [BYTE, BIT, SHORT, FLOAT]: For these types only a complete histogram is generated.
	* Inside every menu two options exist, To Save ASCII and To Save JIP , These options 
	* are accessible once we have calculated any histogram And they allow to save the 
	* information as ASCII file or as image respectively.
	* Common structures:
	* The common structures are those that indicate us the numerical information on the histogram
	* These are common to all types and their Description is the following one:
	* Intensity:  Shows the selected intensity.
	* Occurrences: Shows total number of occurrences in % for the selected intensity.
	* Maximum:    Shows the maximum number of occurrences in the image.
	* @param image JIPImage object to be treated belonging to the sequence.
	*/
	public HistogramWindow(JIPImgBitmap image) {
		super("Histograms of: " + image.getName());
		JMenuItem menuItem;
		JMenu menu = null;
		img = image;
		nIntR = 0;
		max = 1;
		nItems = 0;
		div = 256;
		his = hLum = hRed = hGreen = false;
		hBlue = hByte = hWord = hReal = false;
		maximum = 0;

		ImageType t = img.getType();
		res = null;
		points = new int[256];
		pointsW = new int[65536];
		pane = new JPanel();
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		ImageIcon icon1 = new ImageIcon("icons/colors.jpg");
		ImageIcon icon2 = new ImageIcon("icons/ocurrencias.jpg");
		ImageIcon icon3 = new ImageIcon("icons/guardar_ascii.jpg");
		ImageIcon icon4 = new ImageIcon("icons/guardar_frame.jpg");

		if (t == ImageType.COLOR) {
			menu = new JMenu("Histograms (RGB)");
			menuBar.add(menu);

			menuItem = new JMenuItem("Brightness");
			menuItem.addActionListener(this);
			menu.add(menuItem);
			menuItem = new JMenuItem("Histogram RED");
			menuItem.addActionListener(this);
			menu.add(menuItem);
			menuItem = new JMenuItem("Histogram GREEN");
			menuItem.addActionListener(this);
			menu.add(menuItem);
			menuItem = new JMenuItem("Histogram BLUE");
			menuItem.addActionListener(this);
			menu.add(menuItem);
		} else {
			switch (t) {
				case BYTE:
					menu = new JMenu("Histogram (0..256)");
					menuBar.add(menu);
					break;
				case SHORT:
					menu = new JMenu("Histogram (0..65536)");
					menuBar.add(menu);
					break;
				default:
					menu = new JMenu("Histogram (0..1)");
					menuBar.add(menu);
					break;
			}
			menuItem = new JMenuItem("Complete Histogram");
			menuItem.addActionListener(this);
			menu.add(menuItem);
		}

		menuItem = new JMenuItem("Save as ASCII", icon3);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("Save as JIP", icon4);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		ScrollablePicture pic = new ScrollablePicture(new ImageIcon(), 5);
		JScrollPane pictureScrollPane = new JScrollPane(pic);

		pane.add(pictureScrollPane);
		setContentPane(pane);

		upper_bound = new JLabel("Maximum : ?");
		intensity = new JLabel("Intensity : ?", icon1, JLabel.LEFT);
		occurrences = new JLabel("Value : ?", icon2, JLabel.LEFT);

		pane.add(intensity);
		pane.add(occurrences);
		pane.add(upper_bound);

		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		nItems = 4;
		setContentPane(pane);
		mml = new MyMouseListener();
	}

	public void showHisto(int b, ImageType t, int color) throws JIPException {
		Image img2 = null;
		int x, y;
		max = 1;
		for (y = 0; y < 256; y++)
			if (points[y] > max)
				max = points[y];
		res = (JIPImgBitmap)JIPImage.newImage(b, 256, 256, t);
		double []data = new double[256*256];
		Arrays.fill(data, 128);
		res.setAllPixels(data);
		for (x = 0; x < 256; x++) {
			int n = 0;
			y = points[x];
			n = (255 * y) / max; // occurrences in function of percentage
			for (y = 255; n >= 0; n--, y--) {
				switch (t) {
					case COLOR:
						if (color == 0 || color == 1)
							((JIPBmpColor)res).setPixelRed(x, y, x); // Luminosity or RED
						else ((JIPBmpColor)res).setPixelRed(x, y, 0);
						if (color == 0 || color == 2)
							((JIPBmpColor)res).setPixelGreen(x, y, x); // Luminosity or GREEN
						else ((JIPBmpColor)res).setPixelGreen(x, y, 0);
						if (color == 0 || color == 3)
							((JIPBmpColor)res).setPixelBlue(x, y, x); // Luminosity or BLUE
						else ((JIPBmpColor)res).setPixelBlue(x, y, 0);
						break;
					case BYTE:
						res.setPixel(x, y, x);
						break;
					case SHORT:
						res.setPixel(0, x, y, ((x + 1) * 256) - 128);
						break;
				}
			}
		}
		if (t == ImageType.BYTE) {
			for (x = 0; x < 256; x++)
				for (y = points[x] - 1; y >= 0; y--)
					res.setPixel(0, x, 255 - (y * 256) / max, x);
		}
		img2 = JIPToolkit.getAWTImage(res);
		ScrollablePicture pic = new ScrollablePicture(new ImageIcon(img2), 5);
		JScrollPane pictureScrollPane = new JScrollPane(pic);
		pictureScrollPane.addMouseMotionListener(mml);
		if (pane.getComponentCount() == nItems) {
			pane.remove(0);
			setContentPane(pane);
		}
		pane.add(pictureScrollPane, 0);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		setContentPane(pane);
	}

	/**
	* Method to perform the action according to the event. 
	* @param e , The event treated
	* According to the event the following actions will be realized: 
	* Save ASCII: the histogram showed by screen will be save in text mode. 
	* The file name will depend of the image type and his calculated histogram.
	* You need to have a previous histogram to perform successfully this option.
	* Save JIP: This option saves the histogram as a JIP sequence with only one numFrame
	* with the same type that the input image. The file name will depend of 
	* the image type and his histogram.
	*/
	public void actionPerformed(ActionEvent e) {
		JFileChooser openFile = new JFileChooser();
		if (img != null) {
			int w = img.getWidth();
			int h = img.getHeight();
			int b = img.getNumBands();
			ImageType t = img.getType();
			int x = 0, y = 0;
			String path, nfile = null;

			if (e.getActionCommand().equals("Save as ASCII") && his) {
				String ascii;
				FileWriter fOut;
				ascii = null;
				switch (t) {
					case COLOR: 
						if (hLum) ascii = (img.getName()) + "_lum.jhi";
						else if (hRed) ascii = (img.getName()) + "_red.jhi";
						else if (hGreen) ascii = (img.getName()) + "_green.jhi";
						else if (hBlue) ascii = (img.getName()) + "_blue.jhi";
						break;
					case BYTE:
						if (hByte) ascii = (img.getName()) + "_byte.jhi";
						break;
					case SHORT:
						if (hWord) ascii = (img.getName()) + "_word.jhi";
						break;
					case FLOAT: 
						if (hReal) ascii = (img.getName()) + "_real.jhi";
						break;
				}
				if (ascii != null) {
					path = ".\\";
					openFile.setCurrentDirectory(new File(path));
					openFile.setSelectedFile(new File(ascii));
					if (openFile.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
						File fich = openFile.getSelectedFile();
						nfile = fich.getAbsolutePath();
						if (nfile != null) {
							try {
								fOut = new FileWriter(nfile);
								for (x = 0; x < 256; x++) {
									if (t == ImageType.COLOR || t == ImageType.BYTE)
										fOut.write(x + "       " + points[x] + "\n");
									else if (t == ImageType.SHORT)
										fOut.write(x + "       " + pointsW[x] + "\n");
									else if (t == ImageType.FLOAT)
										fOut.write(pointsR[x].getIValue() + "       "
												+ pointsR[x].getOValue() + "\n");
								}
								fOut.close();
								new Dialog(this).information("File " + nfile + " saved.",
									"Attention");
							} catch (Exception err) {logger.error(err);}
						}
					}
				}
				repaint();
			}

			if (e.getActionCommand().equals("Save as JIP") && his) {
				switch (t) {
					case COLOR: 
						if (hLum) nfile = (img.getName()) + "_H_lum.jip";
						else if (hRed) nfile = (img.getName()) + "_H_red.jip";
						else if (hGreen) nfile = (img.getName()) + "_H_green.jip";
						else if (hBlue) nfile = (img.getName()) + "_H_blue.jip";
						break;
					case BYTE: 
						if (hByte) nfile = (img.getName()) + "_H_byte.jip";
						break;
					case SHORT:
						if (hWord) nfile = (img.getName()) + "_H_word.jip";
						break;
					case FLOAT:
						if (hReal) nfile = (img.getName()) + "_H_real.jip";
				}
				path = ".\\";
				openFile.setCurrentDirectory(new File(path));
				openFile.setSelectedFile(new File(nfile));
				if (openFile.showSaveDialog(this)
					== JFileChooser.APPROVE_OPTION) {
					File fich = openFile.getSelectedFile();
					nfile = fich.getAbsolutePath();
					if (!nfile.toLowerCase().endsWith(".jip"))
						nfile = nfile + ".jip";
					try {
						JIPToolkit.saveImageIntoFile(res, nfile);
					}catch (JIPException ex) {logger.error(ex);}
					new Dialog(this).information("File " + nfile + " saved.","Attention");
				}
				repaint();
			}

			if (t == ImageType.COLOR) {
				try {
					JIPBmpColor bmpColor = (JIPBmpColor)img;
					if (e.getActionCommand().equals("Brightness")) {
						for (y = 0; y < 256; y++)
							points[y] = 0;
						for (y = 0; y < h; y++)
							for (x = 0; x < w; x++)
								points[(int)((bmpColor.getPixelRed(x, y) + bmpColor.getPixelGreen(x, y)
										+ bmpColor.getPixelBlue(x, y))) / 3]++;
						showHisto(b, t, 0);
						his = hLum = true;
						hRed = hGreen = hBlue = hByte = hWord = hReal = false;
					}
					if (e.getActionCommand().equals("Histogram RED")) {
						for (y = 0; y < 256; y++)
							points[y] = 0;
						for (y = 0; y < h; y++)
							for (x = 0; x < w; x++)
								points[(int)bmpColor.getPixelRed(x, y)]++;
						showHisto(b, t, 1);
						his = hRed = true;
						hLum = hGreen = hBlue = hByte = hWord = hReal = false;
					}
					if (e.getActionCommand().equals("Histogram GREEN")) {
						for (y = 0; y < 256; y++)
							points[y] = 0;
						for (y = 0; y < h; y++)
							for (x = 0; x < w; x++)
								points[(int)bmpColor.getPixelGreen(x, y)]++;
						showHisto(b, t, 2);
						his = hGreen = true;
						hLum = hRed = hBlue = hByte = hWord = hReal = false;
					}
					if (e.getActionCommand().equals("Histogram BLUE")) {
						for (y = 0; y < 256; y++)
							points[y] = 0;
						for (y = 0; y < h; y++)
							for (x = 0; x < w; x++)
								points[(int)bmpColor.getPixelBlue(x, y)]++;
						showHisto(b, t, 3);
						his = hBlue = true;
						hLum = hRed = hGreen = hByte = hWord = hReal = false;
					}
				}catch (JIPException ex) {logger.error(ex);}
			}
			if (e.getActionCommand().equals("Complete Histogram")) {
				try {
					if (t == ImageType.BYTE) {
						for (y = 0; y < 256; y++)
							points[y] = 0;
						for (y = 0; y < h; y++)
							for (x = 0; x < w; x++)
								points[(int)((JIPImgBitmap)img).getPixel(0, x, y)]++;
						showHisto(b, t, 4);
						his = hByte = true;
						hLum = hRed = hGreen = hBlue = hWord = hReal = false;
					}
					if (t == ImageType.SHORT) {
						for (y = 0; y < 65536; y++)
							pointsW[y] = 0;
						for (y = 0; y < h; y++)
							for (x = 0; x < w; x++)
								pointsW[(int)((JIPImgBitmap)img).getPixel(0, x, y)]++;
						int acum = 0;
						for (x = 0, y = 0; y < 65536; y++) {
							acum += pointsW[y];
							if (y % 256 == 0) {
								points[x] = acum;
								x++;
								acum = 0;
							}
						}
						showHisto(b, t, 4);
						his = hWord = true;
						hLum = hRed = hGreen = hBlue = hByte = hReal = false;
					}
					if (t == ImageType.FLOAT) {
						float iAct = -1;
						int iter = 0, i = 0;
						float iAux = -1;
						RealDataHistogram[] nData = new RealDataHistogram[w * h];
						for (x = 0; x < w * h; x++)
							nData[x] = new RealDataHistogram();
						for (x = 0; x < w; x++) {
							for (y = 0; y < h; y++) {
								int fin = 0;
								iAct = ((JIPBmpFloat)img).getPixelFloat(0, x, y);
								for (i = 0; i < iter && fin == 0; i++) {
									iAux = nData[i].getIValue();
									if (Math.abs(iAux-iAct)<0.000001) { 
										nData[i].setOValue(
											nData[i].getOValue() + 1);
										fin = 1;
									} 		      					
								} 
								if (fin == 0) { 
									nData[iter].setIValue(iAct);
									nData[iter].setOValue(1);
									iter++;
								} 
							} 
						} 
						nIntR = iter;
						RealDataHistogram[] auxR = new RealDataHistogram[nIntR];
						for (x = 0; x < nIntR; x++) {
							auxR[x] = new RealDataHistogram();
							auxR[x].setIValue(nData[x].getIValue());
							auxR[x].setOValue(nData[x].getOValue());
						}
						// now we're going to sort all data			
						float inf = 10000; 
						pointsR = new RealDataHistogram[nIntR];
						for (x = 0; x < nIntR; x++) {
							pointsR[x] = new RealDataHistogram();
							pointsR[x].setIValue(inf);
							pointsR[x].setOValue(-1);
						}
						for (x = 0; x < nIntR; x++) {
							float iPtos = pointsR[x].getIValue();
							float inAux;
							int uPos = 0;
							for (y = 0; y < nIntR; y++) {
								inAux = auxR[y].getIValue();
								if (inAux < iPtos) {
									iPtos = inAux;
									uPos = y;
								}
							}
							pointsR[x].setIValue(auxR[uPos].getIValue());
							pointsR[x].setOValue(auxR[uPos].getOValue());
							auxR[uPos].setIValue(inf);
							auxR[uPos].setOValue(-1);
						}
						// we will obtain the maximum
						max = 1;
						for (y = 0; y < nIntR; y++)
							if ((pointsR[y].getOValue()) > max)
								max = pointsR[y].getOValue();
						// Declare the new result image			
						res = new JIPBmpFloat(1, nIntR, 256);
						for (x = 0; x < nIntR; x++)
							for (y = 0; y < 256; y++)
								res.setPixel(0, x, y, (float) (0.5));
	
						for (x = 0; x < nIntR; x++) {
							int n = 0;
							y = pointsR[x].getOValue();
							n = (255 * y) / max;
							// occurrences in function of percentage
							for (y = 255; n >= 0; n--, y--)
								res.setPixel(0, x, y, (pointsR[x].getIValue()));
						}
						Image img2 = JIPToolkit.getAWTImage(res);
						ScrollablePicture pic =
							new ScrollablePicture(new ImageIcon(img2), 5);
						JScrollPane pictureScrollPane = new JScrollPane(pic);
						pictureScrollPane.addMouseMotionListener(mml);
						if (pane.getComponentCount() == nItems) {
							pane.remove(0);
						}
						pane.add(pictureScrollPane, 0);
						pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
						setContentPane(pane);
						his = hReal = true;
						hLum = hRed = hGreen = hBlue = hByte = hWord = false;
					}
				}catch (JIPException ex) {logger.error(ex);}
			}
		}
	}

	/**
	* Class to listen and capture mouse events.
	* This class has two methods:
	* public void mouseMoved(MouseEvent e) and public void mouseDragged(MouseEvent e).
	* Both methods have the same input, an event. This event is managed according to
	* the type of the image to be treated. The objective of this class is to translate 
	* and to show the information collected by user and mouse in information about the histogram.
	*/
	public class MyMouseListener extends MouseAdapter implements MouseMotionListener {
		public void mouseMoved(MouseEvent e) {
			int x = e.getX()-1;
			int y = e.getY()-1;
			ImageType t = img.getType();

			if (x >= 0 && x < 256 && y >= 0 && y < 256) {
				switch (t) {
					case BYTE:
					case COLOR: 
						intensity.setText("Intensity : " + x);
						occurrences.setText("Value : " + points[x]);
						break;
					case SHORT:
						intensity.setText("Intensity : [" + x * 256 + ", "
							+ ((x * 256) + 256) + "]");
						occurrences.setText("Value : " + points[x]);
						break;
					case FLOAT:
						intensity.setText("Intensity : " + pointsR[x].getIValue());
						occurrences.setText("Value : " + pointsR[x].getOValue());
						break;
				}
				upper_bound.setText("Maximum : " + max);
			}
			else {
				intensity.setText("Intensity : ?");
				occurrences.setText("Value : ?");
				upper_bound.setText("Maximum : ?");
			}
		}

		public void mouseDragged(MouseEvent e) {}
	}
}
