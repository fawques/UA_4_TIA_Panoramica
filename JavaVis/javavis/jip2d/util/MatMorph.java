package javavis.jip2d.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import org.apache.log4j.Logger;

import javavis.base.JIPException;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;

/**
 *This class implements the basic mathematical morphology operators.
 */
public class MatMorph {
	private static Logger logger = Logger.getLogger(MatMorph.class);
	/**
	 * @uml.property  name="mat" multiplicity="(0 -1)" dimension="2"
	 */
	int [][]mat;
	/**
	 * @uml.property  name="mx"
	 */
	int mx=0;
	/**
	 * @uml.property  name="my"
	 */
	int my=0;
	/**
	 * @uml.property  name="correct"
	 */
	boolean correct=false;;
	
	public MatMorph (String ee) {
		try {
			FileReader r = new FileReader(ee);
			StreamTokenizer st = new StreamTokenizer(r);

			st.nextToken();
			mx = (int) st.nval;
			st.nextToken();
			my= (int) st.nval;

			mat = new int[mx][my];
			for (int j = 0; j < my; j++) {
				for (int i = 0; i < mx; i++) {
					st.nextToken();
					if (st.ttype == StreamTokenizer.TT_EOF)
						correct = false;
					mat[i][j] = (int) st.nval;
				}
			}
		} 
		catch (FileNotFoundException e) {correct = false;}
	    catch (IOException e) {correct = false;}
		correct = true;
	}

	/**
	 * @return
	 * @uml.property  name="correct"
	 */
	public boolean isCorrect() {
		return correct;
	}
	
	public JIPImgBitmap dilate(JIPImgBitmap img){
		int width = img.getWidth();
		int height = img.getHeight();
		int c_x = (mx - 1) / 2;
		int c_y = (my - 1) / 2;
		double aux;

		try {
			JIPImgBitmap res = (JIPImgBitmap)img.clone();
	
			for (int x = c_x; x < width-(c_x+1); x++)
				for (int y = c_y; y < height-(c_y+1); y++) {
					aux=0.0;
					for (int x1 = 0; x1 <mx; x1++)
						for (int y1 = 0; y1 < my; y1++) 
							if (mat[x1][y1]==1) 
								aux=Math.max(img.getPixel(x+x1-c_x, y+y1-c_y), aux);
					res.setPixel(x, y, aux);
				}
			return res;
		}catch (JIPException e){logger.error(e);return null;}
	}
	
	public JIPImgBitmap erode(JIPImgBitmap img){
		int width = img.getWidth();
		int height = img.getHeight();
		int c_x = (mx - 1) / 2;
		int c_y = (my - 1) / 2;
		double aux;

		try {
			JIPImgBitmap res = (JIPImgBitmap)img.clone();
	
			for (int x = c_x; x < width-(c_x+1); x++)
				for (int y = c_y; y < height-(c_y+1); y++) {
					aux=100000;
					for (int x1 = 0; x1 <mx; x1++)
						for (int y1 = 0; y1 < my; y1++) 
							if (mat[x1][y1]==1) 
								aux=Math.min(img.getPixel(x+x1-c_x, y+y1-c_y), aux);
					res.setPixel(x, y, aux);
				}
			return res;
		}catch (JIPException e){logger.error(e);return null;}
	}
}
