package javavis.base;

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.*;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import javavis.Gui;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpBit;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.bitmaps.JIPBmpShort;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.gui.ALLImageFilter;

import com.lti.civil.CaptureException;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.swing.CaptureFrame;

/**
 * Class to define basic methods to get and load AWT and JIP images.
 * AWT images can be got with JIP images or with GIF o JPEG file directly.
 * JIP images can get with AWT images or it can be a directly sequence of JIP files.
 */
public final class JIPToolkit {
	static final Toolkit toolkit = Toolkit.getDefaultToolkit();

	private static Logger logger = Logger.getLogger(JIPToolkit.class);

	/**
	 * It gets an AWT image object from a band of a JIP image.
	 * @param img Source JIP image.
	 * @param b band number to extract (0 <= b <= numbands-1)
	 * @return AWT image result. AWT images are usually representing like color image
	 * , however, a band does not have color so the colors of a AWT image will be grey.
	 */
	public static Image getAWTImage(JIPImage img, int b) throws JIPException {
		if (img == null)
			return null;
		Image res = null;
		int npixels = img.getWidth() * img.getHeight();
		int[] pix = new int[npixels];

		if (img instanceof JIPImgGeometric) {
			Arrays.fill(pix, 0);
		}
		else {
			switch (img.getType()) {
				case BIT : boolean[] vbool = ((JIPBmpBit)img).getAllPixelsBool(b);
							for (int i = 0; i < npixels; i++)
								if (vbool[i])
									pix[i] = 0xFFFFFFFF;
								else
									pix[i] = 0xFF << 24;
							break;
				case BYTE : double[] vbyte = ((JIPBmpByte)img).getAllPixels(b);
							for (int i = 0; i < npixels; i++)
								pix[i] = (0xFF << 24) | ((int)vbyte[i] << 16) | ((int)vbyte[i] << 8) | (int)vbyte[i];
							break;
				case SHORT : // The short value is transformed into byte
							double[] vshort = ((JIPBmpShort)img).getAllPixels(b);
							int aux;
							for (int i = 0; i < npixels; i++)  {
								aux = (int)(255.0*vshort[i]/65535);
								pix[i] = (0xFF << 24) | (aux << 16) | (aux << 8) | aux;
							}
							break;
				case FLOAT : // The float value is transformed into byte
							float[] vfloat = ((JIPBmpFloat)img).getAllPixelsFloat(b);
							for (int i = 0; i < npixels; i++)  {
								aux = (int)Math.abs(255*vfloat[i]);
								pix[i] = (0xFF << 24) | (aux << 16) | (aux << 8) | aux;
							}
							break;
				default: return null;
			}
		}
		res = toolkit.createImage(new MemoryImageSource(img.getWidth(),
				img.getHeight(), pix, 0, img.getWidth()));
		return res;
	}

	/**
	* It gets an AWT image object from a JIP image.
	* If the JIP image is not COLOR type then it will be  the image of the 0 band.
	* @param img Source JIP image.
	* @return AWT image.
	*/
	public static Image getAWTImage(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.COLOR)
			return getAWTImage(img, 0);
		else {
			double[][] src = new double[3][];
			src[0]=((JIPBmpColor)img).getAllPixelsRed();
			src[1]=((JIPBmpColor)img).getAllPixelsGreen();
			src[2]=((JIPBmpColor)img).getAllPixelsBlue();
			int[] pix = new int[src[0].length];
			for (int i=0; i<pix.length; i++)
				pix[i]=(255 << 24) | ((int)src[0][i] << 16) | ((int)src[1][i] << 8) | (int)src[2][i];
			Image res = toolkit.createImage(new MemoryImageSource(img.getWidth(),
					img.getHeight(), pix, 0, img.getWidth()));
			return res;
		}
	}


	/**
	* It gets an AWT image object from a GIF or JPEG file.
	* This method blocks the execution and it do not return the control until the new
	* image is formed completely.
	* Beside, we do not use the MediaTracker object to wait for the load of the
	* image so this method uses MediaTracker internally.
	* @param file Source file (It should be GIF or JPEG)
	* @return AWT image result. (if can not load the image return null)
	*/
	public static Image getAWTImage(String file) {
		Image res = toolkit.getImage(file);
		if (res == null)
			return null;
		MediaTracker mt = new MediaTracker(new Canvas());
		mt.addImage(res, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException e) {
			logger.error("Some error in media tracker");
			return null;
		}
		if (mt.isErrorID(0))
			return null;
		else
			return res;
	}

	/**
	 * Converts an array of integer, containing an image to a 3 arrays of byte.
	 * This helps to separate a color coded in an integer, into three bytes:
	 * red, green, blue.
	 * @param src Array of integers
	 * @return Three array of bytes.
	 */
	public static double[][] convertFromIntToDouble (int[] src) {
		double[][] res = new double[3][];
		res[0] = new double[src.length];
		res[1] = new double[src.length];
		res[2] = new double[src.length];
		for (int i=0; i<src.length; i++) {
			res[0][i]=((src[i] >>> 16) & 0xFF);
			res[1][i]=((src[i] >>> 8) & 0xFF);
			res[2][i]=(src[i] & 0xFF);
		}
		return res;
	}

	/**
	 * It gets a COLOR image object from a AWT image.
	 * @param img AWT image source
	 * @return JIP image result (tCOLOR type).
	 */
	public static JIPImage getColorImage(Image img) throws JIPException {
		Canvas canv = new Canvas();
		int w = img.getWidth(canv);
		int h = img.getHeight(canv);
		if (w <= 0 || h <= 0)
			return null;
		int[] pix = new int[w * h];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pix, 0, w);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {return null;}
		JIPBmpColor resColor = (JIPBmpColor)JIPImage.newImage(w,h, ImageType.COLOR);
		double[][]bmp=convertFromIntToDouble(pix);
		resColor.setAllPixelsRed(bmp[0]);
		resColor.setAllPixelsGreen(bmp[1]);
		resColor.setAllPixelsBlue(bmp[2]);
		return resColor;
	}

	/**
	 * It saves a JIP image object in a JIP format file.
	 * The image is saved as a sequence
	 * @param img Image to save.
	 * @param file File where the image is saved (if it does not exist then it is created,
	 * else it is rewritten)
	 */
	public static void saveImageIntoFile(JIPImage img, String file) throws JIPException {
		saveSeqIntoFile(new Sequence(img), file);
	}

	/**
	 * It returns a (float) image which can be used by MatLab.
	 * @param seq JIP sequence which has the image to extract.
	 * @param f Frame to extract.
	 * @param b Band to extract.
	 * @return Image in array form.
	 */
	public static float [][] getImgMatlab (Sequence seq, int f, int b) throws JIPException {
		return getImgMatlab(seq.getFrame(f), b);
	}

	/**
	 * It returns a (float) image which can be used by MatLab.
	 * @param img JIP image which has the image to extract.
	 * @param b Band to extract.
	 * @return Image in array form.
	 */
	public static float [][] getImgMatlab (JIPImage img, int b) throws JIPException {
		int rows=img.getHeight();
		int cols=img.getWidth();
		float [][]out=new float [rows][];

		if (img instanceof JIPImgGeometric) {
			logger.warn("getImgMatlab: do not valid for geometric types");
			throw new JIPException("JIPToolkit.getImgMatlab: do not valid for geometric types");
		}

		double []bmp=((JIPImgBitmap)img).getAllPixels(b);
		for (int r=0; r<rows; r++) {
			out[r]=new float[cols];
			for (int c=0; c<cols; c++)
				out[r][c]=(float)bmp[r*cols+c];
		}

		return out;
	}

	/**
	 * It returns a JIP image from a matrix.
	 * @param img Matrix to assign.
	 * @param type Image type to create.
	 * @return JIP format image
	 */
	public static JIPImage setMatrix (float [][]img, ImageType type) throws JIPException {
		int rows=img.length, cols=img[0].length;
		double []aux=new double[rows*cols];
		JIPImgBitmap res=null;

		if (type==ImageType.EDGES || type==ImageType.POINT || type==ImageType.POLY || type==ImageType.SEGMENT)
			throw new JIPException("JIPToolkit.setMatrix: do not valid for geometric types");
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++)
				aux[r*cols+c]=img[r][c];
		switch (type) {
			case BIT: res=new JIPBmpBit(rows, cols); break;
			case BYTE: res=new JIPBmpByte(rows, cols); break;
			case SHORT: res=new JIPBmpShort(rows, cols); break;
			case FLOAT: res=new JIPBmpFloat(rows, cols); break;
			case COLOR: res=new JIPBmpColor(rows, cols); break;
		}
		res.setAllPixels(aux);

		return res;
	}

	/**
	 * It returns a color JIP image from 3 bands.
	 * @param RED Matrix which represents RED.
	 * @param GREEN Matrix which represents GREEN.
	 * @param BLUE Matrix which represents BLUE.
	 * @return JIP format image
	 */
	public static JIPImage setMatrix (float [][]red, float [][]green, float [][]blue) throws JIPException {
		int rows=red.length, cols=red[0].length;
		byte []auxr=new byte[rows*cols];
		byte []auxg=new byte[rows*cols];
		byte []auxb=new byte[rows*cols];
		JIPBmpColor res=new JIPBmpColor(cols, rows);
		for (int r=0; r<rows; r++)
			for (int c=0; c<cols; c++) {
				auxr[r*cols+c]=(byte)red[r][c];
				auxg[r*cols+c]=(byte)green[r][c];
				auxb[r*cols+c]=(byte)blue[r][c];
			}
		res.setAllPixelsByteRed(auxr);
		res.setAllPixelsByteGreen(auxg);
		res.setAllPixelsByteBlue(auxb);
		return res;
	}

	/**
	 * It gets a sequence JIP object from a JIP ZIP file.
	 * @param file Name of the file.
	 * @return JIP sequence. (if can not load the secuence return null)
	 */
	public static Sequence getSeqFromFile (String file) {
		String name = uncompressFile(file);
		Sequence seq = (Sequence)getSeqFromFileRaw(name);
		File f = new File(name);
		f.delete(); //delete the uncompressed file
		return seq;
	}

	/**
	 * Uncompress a file..
	 * @param file Name of the file.
	 * @return String indicating the file contained in the zip file
	 */
	public static String uncompressFile(String file) {
		String name=null;
		File auxFile=null;
		FileInputStream orig=null;
		BufferedOutputStream dest=null;
		ZipInputStream inZip=null;
		FileOutputStream fo=null;

		try {
			orig = new FileInputStream(file);
			inZip = new ZipInputStream(new BufferedInputStream(orig));

			byte[] data = new byte[8192];
			int count;
			ZipEntry entry=inZip.getNextEntry();
			if (entry != null) {
				//name = entry.getName(); //name without route
				name = file.substring(0,file.length()-1);
				auxFile = new File (name);
				auxFile.createNewFile();
				fo = new FileOutputStream(auxFile);
				dest = new BufferedOutputStream(fo, 8192);
				while ((count = inZip.read(data, 0, 8192)) != -1)
					dest.write(data, 0, count);
				dest.flush();
			}
		} catch (ZipException e) {logger.error(e);}
		  catch (IOException e) {logger.error(e);}
		finally {
			try {
				if (dest != null) dest.close();
				if (fo != null) fo.close();
				if (inZip != null) inZip.close();
			} catch (Exception e) {logger.error(e);}
		}
		return name;
	}

	/**
	 * It saves a JIP sequence object in a JIP format file.
	 * @param obj Sequence to save.
	 * @param file File where the sequence is loaded (if it does not exist then it is created,
	 * else it is rewritten)
	 */
	public static void saveSeqIntoFileRaw(Object obj, String file) {
		FileOutputStream fos=null;
		ObjectOutputStream oos=null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
		}
		catch (Exception e) {logger.error(e);}
		finally {
			try {
				if (oos != null && fos != null) {
					oos.close();
					fos.close();
				}
			}
			catch (Exception e) {logger.error(e);}
		}
	}

	/**
	 * It gets an object from a raw JIP file. It can be used in JIP and
	 * JIP3D sequences
	 * @param file File where the sequence is loaded
	 */
	public static Object getSeqFromFileRaw (String file) {
		if (file == null) return null;
		FileInputStream fos=null;
		ObjectInputStream oos=null;
		Object obj=null;
		try {
			fos = new FileInputStream(file);
			oos = new ObjectInputStream(fos);
			obj=oos.readObject();
		}
		catch (Exception e) {logger.error(e);}
		finally {
			try {
				if (oos != null && fos != null) {
					oos.close();
					fos.close();
				}
			}
			catch (Exception e) {logger.error(e);}
		}
		return obj;
	}

	/**
	 * It saves a JIP sequence object in a JIP format file.
	 * @param obj Sequence to save.
	 * @param dir Directory where save it.
	 * @param namefile File where the sequence is loaded (if it does not exist then it is created,
	 * else it is rewritten)
	 */
	public static void saveSeqIntoFile(Object obj, String dir, String namefile) {
		saveSeqIntoFile(obj, dir+namefile);
	}

	/**
	 * It saves a JIP sequence object in a JIP format file.
	 * @param obj Sequence to save.
	 * @param namefile File where the sequence is saved (if it does not exist then it is created,
	 * else it is rewritten)
	 */
	public static void saveSeqIntoFile(Object obj, String namefile) {
		// First, save the sequence in a temporary file
		saveSeqIntoFileRaw(obj, namefile+"~");
		// Then, compress it
		compressFile(namefile);
	}


	/*
	 * It saves a JPG image format.
	 * @param seq: Open a sequence in the screen which wants to save a copy in JPG format.
	 * @param active: Frame which is shown in the Gui window and it will be saved in a JPG format
	 * @param dir: Directory where the JPG image is saved.
	 * @param namefile: Name that the JPG format file will be saved.
	 *
	public static void saveImgIntoFileJpg(Sequence seq, int active, String dir, String namefile) {
		Image origin;
		BufferedImage buffImage;
		float quality = 1.0f; // Quality: 0.0 worst, 1.0 best
		// Writes in a file
		OutputStream os;
		// Encodes in format JPG
		JPEGImageEncoder encoder;
		try {
			origin = JIPToolkit.getAWTImage(seq.getFrame(active));
			buffImage = new BufferedImage(origen.getWidth(null),
					origen.getHeight(null), BufferedImage.TYPE_INT_RGB);
			AffineTransform tx = new AffineTransform();
			//paints the image in the image buffer
			Graphics2D g2d = buffImage.createGraphics();
			g2d.drawImage(origin, tx, null);
			g2d.dispose();
			//encodes the image in JPG format, and writes it in the file
			os = new FileOutputStream(dir + namefile);
			encoder = JPEGCodec.createJPEGEncoder(os);
			JPEGEncodeParam jep = encoder.getDefaultJPEGEncodeParam(buffImage);
			jep.setQuality(quality, true);
			encoder.setJPEGEncodeParam(jep);
			encoder.encode(buffImage);
			os.close();
		} catch (Exception e) {logger.error(e);}
	}*/
	
	/**
	 * It saves a JPG image format.
	 * @param seq: Open a sequence in the screen which wants to save a copy in JPG format.
	 * @param active: Frame which is shown in the Gui window and it will be saved in a JPG format
	 * @param dir: Directory where the JPG image is saved.
	 * @param namefile: Name that the JPG format file will be saved.
	 */
	public static void saveImgIntoFileJpg(Sequence seq, int active, String dir, String namefile) {
		Image origin;
		JIPImage source;
		BufferedImage buffImage;
		// file extension treatment
		String ext = namefile.substring(namefile.lastIndexOf('.')+1);
		if(ext.length()==3 || ext.length()==4&&ext.toLowerCase().compareTo("jpeg")==0)
			namefile = namefile.substring(0, namefile.lastIndexOf('.')+1);
		try {
			source = seq.getFrame(active);
			origin = JIPToolkit.getAWTImage(source);
			namefile += "png"; 
			if(source.getType()==ImageType.BYTE)
			{
				buffImage = new BufferedImage(origin.getWidth(null),
						origin.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
			}
			else
			{
				buffImage = new BufferedImage(origin.getWidth(null),
						origin.getHeight(null), BufferedImage.TYPE_INT_RGB);
			}

			AffineTransform tx = new AffineTransform();
			Graphics2D g2d = buffImage.createGraphics();
			g2d.drawImage(origin, tx, null);
			g2d.dispose();
			File outputfile = new File(dir + namefile);
			ImageIO.write(buffImage, "jpg", outputfile);

		} catch (Exception e) {logger.error(e);}
	}

	//added by dviejo on 23th/March/2011
	/**
	 * It saves a PNG image format.
	 * @param seq: Open a sequence in the screen which wants to save a copy in PNG format.
	 * @param active: Frame which is shown in the Gui window and it will be saved in a JPG format
	 * @param dir: Directory where the PNG image is saved.
	 * @param namefile: Name that the PNG format file will be saved.
	 */
	public static void saveImgIntoFilePng(Sequence seq, int active, String dir, String namefile) {
		Image origin;
		JIPImage source;
		BufferedImage buffImage;
		// file extension treatment
		String ext = namefile.substring(namefile.lastIndexOf('.')+1);
		if(ext.length()==3 || ext.length()==4&&ext.toLowerCase().compareTo("jpeg")==0)
			namefile = namefile.substring(0, namefile.lastIndexOf('.')+1);
		try {
			source = seq.getFrame(active);
			origin = JIPToolkit.getAWTImage(source);
			if(source.getType()==ImageType.BYTE)
			{
				namefile += ".pgm";
				buffImage = new BufferedImage(origin.getWidth(null),
						origin.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
			}
			else
			{
				namefile += "png"; 
				buffImage = new BufferedImage(origin.getWidth(null),
						origin.getHeight(null), BufferedImage.TYPE_INT_RGB);
			}

			AffineTransform tx = new AffineTransform();
			Graphics2D g2d = buffImage.createGraphics();
			g2d.drawImage(origin, tx, null);
			g2d.dispose();
			File outputfile = new File(dir + namefile);
			ImageIO.write(buffImage, "png", outputfile);

		} catch (Exception e) {logger.error(e);}
	}

	/**
	 * It compress a file
	 * @param namefile File to compress
	 */
	private static void compressFile(String namefile) {
		BufferedInputStream origin=null;
		FileOutputStream dest=null;
		ZipOutputStream out=null;
		FileInputStream fi=null;
		File file=null;

		try {
			dest = new FileOutputStream(namefile);
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte[] data = new byte[8192];
			file = new File(namefile+"~");
			fi = new FileInputStream(file);
			origin = new BufferedInputStream(fi, 8192);
			String [] splitRoute = namefile.split("[/\\\\]"); //In order to get the filename only.
			String extractedFileName = splitRoute[splitRoute.length-1];
			ZipEntry entry = new ZipEntry(extractedFileName.substring(0,extractedFileName.length()-1));
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, 8192)) != -1)
				out.write(data, 0, count);
		} catch (IOException e) {logger.error(e);}
		finally {
			try {
				if (origin != null) origin.close();
				if (fi != null) fi.close();
				if (out != null) out.close();
				if (dest != null) dest.close();
				if (file != null) file.delete();
			} catch (Exception e) {logger.error(e);}
		}
	}


	/**
	 * This method ask for a file. Depending of their type, it opens j2d, desktop or j3d applications
	 * @param where Directory where it starts to look for the file.
	 * @return A JFileChoser created object or null if we cancel it.
	 */
	public static void askForFile(String where, boolean open, Properties prop, Gui g) throws JIPException {
		JFileChooser chooseFile = new JFileChooser();
		if (open)
			chooseFile.setDialogType(JFileChooser.OPEN_DIALOG);
		else
			chooseFile.setDialogType(JFileChooser.SAVE_DIALOG);
		if (where != null) {
			String path = where.substring(0, where.lastIndexOf("\\") + 1);
			String nfile = where.substring(where.lastIndexOf("\\") + 1,
					where.length());
			chooseFile.setCurrentDirectory(new File(path));
			chooseFile.setSelectedFile(new File(nfile));
		} else {
			chooseFile.setCurrentDirectory(new File("."));
			chooseFile.setSelectedFile(new File(prop.getProperty("NewFile")));
		}

		chooseFile.addChoosableFileFilter(new DesktopImageFilter());
		chooseFile.addChoosableFileFilter(new ImageFilter());
		chooseFile.addChoosableFileFilter(new ALLImageFilter());
		if (open) {
			if (chooseFile.showOpenDialog(g) == JFileChooser.APPROVE_OPTION) {
				ALLImageFilter aif = new ALLImageFilter();
				ImageFilter jif = new ImageFilter();
				DesktopImageFilter jdf = new DesktopImageFilter();
				if (aif.accept(chooseFile.getSelectedFile())) {
					g.getTabPane().setSelectedIndex(Gui.INDEX2D);
					if (g.getG2d().isSaved() ||
						new Dialog(g).confirm(prop.getProperty("FileNotSaved")+
							prop.getProperty("Sure"), prop.getProperty("Exit")))
					    g.getG2d().openJPGImage(chooseFile.getSelectedFile());
				}
				else if (jif.accept(chooseFile.getSelectedFile())) {
					g.getTabPane().setSelectedIndex(Gui.INDEX2D);
					if (g.getG2d().isSaved() ||
							new Dialog(g).confirm(prop.getProperty("FileNotSaved")+
								prop.getProperty("Sure"), prop.getProperty("Exit")))
					    g.getG2d().openJIPImage(chooseFile.getSelectedFile());
				}
				else if (jdf.accept(chooseFile.getSelectedFile())) {
					g.getTabPane().setSelectedIndex(Gui.INDEXDESKTOP);
					if (g.getGdesk().isSaved() ||
							new Dialog(g).confirm(prop.getProperty("FileNotSaved")+
								prop.getProperty("Sure"), prop.getProperty("Exit")))
					    g.getGdesk().openDesktopFile(chooseFile.getSelectedFile());
				}
				/*else if (g.is3dPresent() &&
						chooseFile.getFileFilter() instanceof JIP3DImageFilter) {
					g.getTabPane().setSelectedIndex(Gui.INDEX3D);
					//Repair, create methods
					if (g.getG3d().isSaved() ||
						new Dialog(g).confirm(prop.getProperty("FileNotSaved")+
							prop.getProperty("Sure"), prop.getProperty("Exit")))
					    g.getG3d().openJIP3DFile(chooseFile.getSelectedFile());
				}*/
			}
		} else {
			if (chooseFile.showSaveDialog(g) == JFileChooser.APPROVE_OPTION)
				; // TODO: falta los metodos de salvar
		}
	}

	/**
	 * Open a dialog for capturing an image from a webcam. Once
	 * captured, opens the image. Uses FMJ.
	 * @param prop language properties
	 * @param g the GUI
	 * @throws JIPException
	 */
	public static void askForCapture(Properties prop, Gui g) throws JIPException {
		try {
			new CaptureFrame(DefaultCaptureSystemFactorySingleton.instance(),g,prop).run();
		} catch (CaptureException e) {
			new Dialog(g).information(prop.getProperty("CaptureInitializationError"),prop.getProperty("Error"));
		}
	}

	/**
	 * Check if the current files are saved before exit
	 * @param saved True if file saved
	 * @param prop Properties for language
	 * @param gui Main frame
	 */
	public static void exit (Properties prop, Gui gui) {
		
		if (!gui.getG2d().isSaved()) {
			gui.getTabPane().setSelectedIndex(Gui.INDEX2D);
			if (new Dialog(gui).confirm(prop.getProperty("FileNotSaved")+
						prop.getProperty("Sure"), prop.getProperty("Exit")))
				System.exit(0);
			else
				return;
		}
		else System.exit(0);

		if (!gui.getGdesk().isSaved()) {
			gui.getTabPane().setSelectedIndex(Gui.INDEXDESKTOP);
			if (new Dialog(gui).confirm(prop.getProperty("FileNotSaved")+
						prop.getProperty("Sure"), prop.getProperty("Exit")))
				System.exit(0);
			else
				return;
		}
		else System.exit(0);

	}
}

