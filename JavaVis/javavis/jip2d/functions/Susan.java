package javavis.jip2d.functions;

import java.util.Arrays;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPGeomEdges;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Point2D;

/**
 * It implements the Susan method for corner and edge detection.<br />
 * <em>S.M. Smith and J.M. Brady. SUSAN - a new approach to low level image processing. 
 * International Journal of Computer Vision, 23(1):45-78, May 1997</em>. This 
 * implementation is a Java version of the one from the authors.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>geomThresh</em>: Integer value which indicates the geometric threshold (1850 
 * for corners, 2650 for edges) (default 1850).</li>
 * <li><em>brightThresh</em>: Integer value which indicates the brightness threshold (default 
 * 13).</li>
 * <li><em>corners</em>: Boolean value which indicates if we want to detect corners (true) or
 * we want to detect edges (false) (default true).</li>
 * <li><em>quick</em>: Boolean value which indicates true if we want to apply a quick version 
 * of Susan (default false).</li>
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A POINT image if corners is activated and a BYTE image in otherwise.</li>
 * </ul><br />
 */
public class Susan extends Function2D {
	private static final long serialVersionUID = -6325253526988864760L;
	
	private static final int LENGTHTHRESH = 2;
	
	public Susan() {
		super();
		name = "Susan";
		description = "Applies Susan method to detect corners or edges. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.Edges;

		ParamInt geomThresh = new ParamInt("geomThresh", false, true);
		geomThresh.setDefault(1850);
		geomThresh.setDescription("Geometric threshold: 1850 for corners, 2650 for edges");
		
		ParamInt brightThresh = new ParamInt("brightThresh", false, true);
		brightThresh.setDefault(13);
		brightThresh.setDescription("Brightness threshold");
		
		ParamBool corners = new ParamBool("corners", false, true);
		corners.setDefault(true);
		corners.setDescription("Detection of corners or edges");
		
		ParamBool quick = new ParamBool("quick", false, true);
		quick.setDefault(false);
		quick.setDescription("Apply a quick version of Susan");
		
		addParam(geomThresh);
		addParam(brightThresh);
		addParam(corners);
		addParam(quick);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() == ImageType.BIT || img instanceof JIPImgGeometric) 
			throw new JIPException("Function Susan can not be applied to this image format.");
		
		boolean corners = getParamValueBool("corners");
		boolean quick = getParamValueBool("quick");
		int brightThresh = getParamValueInt("brightThresh");
		int geomThresh = getParamValueInt("geomThresh");
		
		JIPImgBitmap srcbn;
		if (img.getType() == ImageType.COLOR) {
			Function2D fctg = new ColorToGray();
			fctg.setParamValue("gray", "BYTE");
			srcbn = (JIPImgBitmap)fctg.processImg(img);
		}
		else srcbn = (JIPImgBitmap)img;
		
		int []lut = setupBrightnessLUT(brightThresh, true);
		
		if (quick) {
			if (corners)
				return susanCornerQuick(srcbn, geomThresh, lut);
			else {
				JIPImgBitmap midImg = (JIPImgBitmap)JIPImage.newImage(img.getWidth(), img.getHeight(), img.getType());
				JIPImgBitmap edges = susanEdgesSmall(srcbn, midImg, geomThresh, lut);
				edgeThin(edges, midImg);
				return extractEdges(midImg, LENGTHTHRESH);
			}
		}
		else
			if (corners)
				return susanCorner(srcbn, geomThresh, lut);
			else {
				JIPImgBitmap midImg = (JIPImgBitmap)JIPImage.newImage(img.getWidth(), img.getHeight(), img.getType());
				JIPImgBitmap edges = susanEdges(srcbn, midImg, geomThresh, lut);
				edgeThin(edges, midImg);
				return extractEdges(midImg, LENGTHTHRESH);
			}
	}

	
	/**
	 * Method which indicate if a pixel is a local maximum.
	 * @param image Variable which contains the image data.
	 * @param col The current column.
	 * @param row The current row.
	 * @return A boolean indicating true if the pixel is a local maximum, false in otherwise.
	 * @throws JIPException
	 */
	private boolean is_maximum(double[] pixels, int width, int col, int row) throws JIPException {
		double value = pixels[col + width*row];
		return (value > pixels[(row-3)*width + col-3]
		        && value > pixels[(row-3)*width + col-2]
		        && value > pixels[(row-3)*width + col-1]
  		        && value > pixels[(row-3)*width + col]
		        && value > pixels[(row-3)*width + col+1]
		        && value > pixels[(row-3)*width + col+2]
		        && value > pixels[(row-3)*width + col+3]
                && value > pixels[(row-2)*width + col-3]
		        && value > pixels[(row-2)*width + col-2]
  		        && value > pixels[(row-2)*width + col-1]
		        && value > pixels[(row-2)*width + col]
  		        && value > pixels[(row-2)*width + col+1]
  		        && value > pixels[(row-2)*width + col+2]
  		        && value > pixels[(row-2)*width + col+3]
                && value > pixels[(row-1)*width + col-3]
		        && value > pixels[(row-1)*width + col-2]
  		        && value > pixels[(row-1)*width + col-1]
		        && value > pixels[(row-1)*width + col]
  		        && value > pixels[(row-1)*width + col+1]
  		        && value > pixels[(row-1)*width + col+2]
  		        && value > pixels[(row-1)*width + col+3]
                && value > pixels[(row)*width + col-3]
		        && value > pixels[(row)*width + col-2]
  		        && value > pixels[(row)*width + col-1]
  		        && value >= pixels[(row)*width + col+1]
  		        && value >= pixels[(row)*width + col+2]
  		        && value >= pixels[(row)*width + col+3]
                && value >= pixels[(row+1)*width + col-3]
		        && value >= pixels[(row+1)*width + col-2]
  		        && value >= pixels[(row+1)*width + col-1]
		        && value >= pixels[(row+1)*width + col]
  		        && value >= pixels[(row+1)*width + col+1]
  		        && value >= pixels[(row+1)*width + col+2]
  		        && value >= pixels[(row+1)*width + col+3]
                && value >= pixels[(row+2)*width + col-3]
		        && value >= pixels[(row+2)*width + col-2]
  		        && value >= pixels[(row+2)*width + col-1]
		        && value >= pixels[(row+2)*width + col]
  		        && value >= pixels[(row+2)*width + col+1]
  		        && value >= pixels[(row+2)*width + col+2]
  		        && value >= pixels[(row+2)*width + col+3]
                && value >= pixels[(row+3)*width + col-3]
		        && value >= pixels[(row+3)*width + col-2]
  		        && value >= pixels[(row+3)*width + col-1]
		        && value >= pixels[(row+3)*width + col]
  		        && value >= pixels[(row+3)*width + col+1]
  		        && value >= pixels[(row+3)*width + col+2]
  		        && value >= pixels[(row+3)*width + col+3]);
	}
	
	/**
	 * Method which implements the brightness LUT.
	 * @param thresh The brightness threshold.
	 * @param form Boolean indicating the form to apply.
	 * @return The brightness LUT.
	 */
	private int[] setupBrightnessLUT (int thresh, boolean form) {
		int []lut = new int[513];
		double temp;
		
		for (int k=-256; k < 257; k++) {
			temp = (double)k/(double)thresh;
			temp *= temp;
			if (form) 
				temp = temp*temp*temp;
			lut[k+256] = (int)(100.0*Math.exp(-temp));
		}
		return lut;
	}
	
	/**
	 * Method which converts a real into a integer, applying rounding.
	 * @param a The number we want to convert into a integer.
	 * @return The integer value of a real value, with rounding.
	 */
	private int ftoi (double a) {
		if (a < 0) 
			return (int)(a-0.5);
		else
			return (int)(a+0.5);
	}
	
	/**
	 * Method which implements the Susan algorithm for detecting corners.
	 * @param image The input image.
	 * @param geomThresh The geometric threshold.
	 * @param lut The brightness LUT.
	 * @return The output image.
	 * @throws JIPException
	 */
	private JIPImgGeometric susanCorner (JIPImgBitmap image, int geomThresh, int[] lut) throws JIPException {
		int width = image.getWidth();
		int height = image.getHeight();
		double []pixels = image.getAllPixels();
		double []r = new double[pixels.length];
		double []cgx = new double[pixels.length];
		double []cgy = new double[pixels.length];
		int n, p, cp, x, y, sq, xx, yy;
		double c;
		
		for (int i=5; i < height-5; i++)
		    for (int j=5; j < width-5; j++) {
		    	n = 100;
		        p = (i-3)*width + j - 1;
		        cp = (int)pixels[i*width+j];

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-3; 

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-5;

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-6;

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        if (n < geomThresh) {    /* do this test early and often ONLY to save wasted computation */
		        	p += 2;
		        	n += lut[256+cp-(int)pixels[p++]];
		        	if (n < geomThresh){
		        		n += lut[256+cp-(int)pixels[p++]];
		        		if (n < geomThresh){
		        			n += lut[256+cp-(int)pixels[p]];
		        			if (n < geomThresh){
		        				p += width-6;

		        				n += lut[256+cp-(int)pixels[p++]];
		        				if (n < geomThresh){
		        					n += lut[256+cp-(int)pixels[p++]];
		        					if (n < geomThresh){
		        						n += lut[256+cp-(int)pixels[p++]];
		        						if (n < geomThresh){
		        							n += lut[256+cp-(int)pixels[p++]];
		        							if (n < geomThresh){
		        								n += lut[256+cp-(int)pixels[p++]];
		        								if (n < geomThresh){
		        									n += lut[256+cp-(int)pixels[p++]];
		        									if (n < geomThresh){
		        										n += lut[256+cp-(int)pixels[p]];
		        										if (n < geomThresh){
		        											p += width-5;

		        											n += lut[256+cp-(int)pixels[p++]];
		        											if (n < geomThresh) {
		        												n += lut[256+cp-(int)pixels[p++]];
		        												if (n < geomThresh){
		        													n += lut[256+cp-(int)pixels[p++]];
		        													if (n < geomThresh){
		        														n += lut[256+cp-(int)pixels[p++]];
		        														if (n < geomThresh){
		        															n += lut[256+cp-(int)pixels[p]];
		        															if (n < geomThresh){
		        																p += width-3;

		        																n += lut[256+cp-(int)pixels[p++]];
		        																if (n < geomThresh){
		        																	n += lut[256+cp-(int)pixels[p++]];
		        																	if (n < geomThresh){
		        																		n += lut[256+cp-(int)pixels[p]];

		        																		if (n < geomThresh) {
		        																			x=0;y=0;
		        																			p=(i-3)*width + j - 1;

		        																			c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=3*c;
																				            c=lut[256+cp-(int)pixels[p++]]; y-=3*c;
																				            c=lut[256+cp-(int)pixels[p]];   x += c; y-=3*c;
																				            p += width-3; 
																				    
																				            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y-=2*c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=2*c;
																				            c=lut[256+cp-(int)pixels[p++]]; y-=2*c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += c; y-=2*c;
																				            c=lut[256+cp-(int)pixels[p]];   x += 2*c; y-=2*c;
																				            p += width-5;
																				    
																				            c=lut[256+cp-(int)pixels[p++]]; x-=3*c; y-=c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y-=c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=c;
																				            c=lut[256+cp-(int)pixels[p++]]; y-=c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += c; y-=c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += 2*c; y-=c;
																				            c=lut[256+cp-(int)pixels[p]];   x += 3*c; y-=c;
																				            p += width-6;
																		
																				            c=lut[256+cp-(int)pixels[p++]]; x-=3*c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=2*c;
																				            c=lut[256+cp-(int)pixels[p]];   x-=c;
																				            p += 2;
																				            c=lut[256+cp-(int)pixels[p++]]; x += c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += 2*c;
																				            c=lut[256+cp-(int)pixels[p]];   x += 3*c;
																				            p += width-6;
																				    
																				            c=lut[256+cp-(int)pixels[p++]]; x-=3*c; y += c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y += c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += c;
																				            c=lut[256+cp-(int)pixels[p++]]; y += c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += c; y += c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += 2*c; y += c;
																				            c=lut[256+cp-(int)pixels[p]]; x += 3*c; y += c;
																				            p += width-5;
																		
																				            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y += 2*c;
																				            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += 2*c;
																				            c=lut[256+cp-(int)pixels[p++]]; y += 2*c;
																				            c=lut[256+cp-(int)pixels[p++]]; x += c; y += 2*c;
																				            c=lut[256+cp-(int)pixels[p]]; x += 2*c; y += 2*c;
																				            p += width-3;
																		
																				            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += 3*c;
																				            c=lut[256+cp-(int)pixels[p++]]; y += 3*c;
																				            c=lut[256+cp-(int)pixels[p]]; x += c; y += 3*c;
																		
																				            xx = x*x;
																				            yy = y*y;
																				            sq = xx+yy;
																				            if (sq > ((n*n)/2))  {
																				            	sq = sq/2;
																				            	if (yy < sq) {
																				            		double divide = y/Math.abs(x);
																				            		sq = Math.abs(x)/x;
																				            		sq = lut[256+cp-(int)pixels[(i+ftoi(divide))*width+j+sq]] +
																				            		   lut[256+cp-(int)pixels[(i+ftoi(2*divide))*width+j+2*sq]] +
																				            		   lut[256+cp-(int)pixels[(i+ftoi(3*divide))*width+j+3*sq]];
																				            	}
																				            	else if (xx < sq) {
																				            		double divide=x/Math.abs(y);
																				            		sq = Math.abs(y)/y;
																				            		sq = lut[256+cp-(int)pixels[(i+sq)*width+j+ftoi(divide)]] +
																				            		   lut[256+cp-(int)pixels[(i+2*sq)*width+j+ftoi(2*divide)]] +
																				            		   lut[256+cp-(int)pixels[(i+3*sq)*width+j+ftoi(3*divide)]];
																				            	}
																				            	if (sq > 290){
																				            		r[i*width+j] = geomThresh-n;
																				            		cgx[i*width+j] = (51*x)/n;
																				            		cgy[i*width+j] = (51*y)/n;
																				            	}
																				            }
		        																		}
		        																	}
		        																}
		        															}
		        														}
		        													}
		        												}
		        											}
		        										}
		        									}
		        								}
		        							}
		        						}
		        					}
		        				}
		        			}
		        		}
		        	}
		        }
		    }

	  	JIPGeomPoint resultPoints = (JIPGeomPoint)JIPImage.newImage(width, height, ImageType.POINT);
	  	/* to locate the local maxima */
	  	for (int i=5; i < height-5; i++)
	  		for (int j=5; j < width-5; j++) {
	  			if (r[i*width+j] > 0)  {
	  				if (is_maximum(r, width, j, i)) {
	  					resultPoints.addPoint(new Point2D(j, i));
	  				}
	  			}
	  		}
	  	return resultPoints;
	}
	
	/**
	 * Method which implements the quick Susan algorithm for detecting corners.
	 * @param image The input image.
	 * @param geomThresh The geometric threshold.
	 * @param lut The brightness LUT.
	 * @return The output image.
	 * @throws JIPException
	 */
	private JIPImgGeometric susanCornerQuick (JIPImgBitmap image, int geomThresh, int[] lut) throws JIPException {
		int width = image.getWidth();
		int height = image.getHeight();
		double []pixels = image.getAllPixels();
		double []r = new double[pixels.length];
		int n, p, cp;
		
		for (int i=7; i < height-7; i++)
		    for (int j=7; j < width-7; j++) {
		    	n = 100;
		        p = (i-3)*width + j - 1;
		        cp = (int)pixels[i*width+j];

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-3; 

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-5;

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-6;

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        if (n < geomThresh){    /* do this test early and often ONLY to save wasted computation */
		        	p += 2;
		        	n += lut[256+cp-(int)pixels[p++]];
		        	if (n < geomThresh){
		        		n += lut[256+cp-(int)pixels[p++]];
		        		if (n < geomThresh){
		        			n += lut[256+cp-(int)pixels[p]];
		        			if (n < geomThresh){
		        				p += width-6;

		        				n += lut[256+cp-(int)pixels[p++]];
		        				if (n < geomThresh){
		        					n += lut[256+cp-(int)pixels[p++]];
		        					if (n < geomThresh){
		        						n += lut[256+cp-(int)pixels[p++]];
		        						if (n < geomThresh){
		        							n += lut[256+cp-(int)pixels[p++]];
		        							if (n < geomThresh){
		        								n += lut[256+cp-(int)pixels[p++]];
		        								if (n<geomThresh){
		        									n += lut[256+cp-(int)pixels[p++]];
		        									if (n < geomThresh){
		        										n += lut[256+cp-(int)pixels[p]];
		        										if (n < geomThresh){
		        											p += width-5;

		        											n += lut[256+cp-(int)pixels[p++]];
		        											if (n < geomThresh){
		        												n += lut[256+cp-(int)pixels[p++]];
		        												if (n < geomThresh){
		        													n += lut[256+cp-(int)pixels[p++]];
		        													if (n < geomThresh){
		        														n += lut[256+cp-(int)pixels[p++]];
		        														if (n < geomThresh){
		        															n += lut[256+cp-(int)pixels[p]];
		        															if (n < geomThresh){
		        																p += width-3;

		        																n += lut[256+cp-(int)pixels[p++]];
		        																if (n < geomThresh){
		        																	n += lut[256+cp-(int)pixels[p++]];
		        																	if (n < geomThresh){
		        																		n += lut[256+cp-(int)pixels[p]];

		        																		if (n < geomThresh) 
																				            r[i*width+j] = geomThresh-n;
		        																	}
		        																}
		        															}
		        														}
		        													}
		        												}
		        											}
		        										}
		        									}
		        								}
		        							}
		        						}
		        					}
		        				}
		        			}
		        		}
		        	}
		        }
		    }

	  	JIPGeomPoint resultPoints = (JIPGeomPoint)JIPImage.newImage(width, height, ImageType.POINT);
	  	/* to locate the local maxima */
	  	for (int i=5; i < height-5; i++)
	  		for (int j=5; j < width-5; j++) {
	  			if (r[i*width+j] > 0)  {
	  				if (is_maximum(r, width, j, i)) {
	  					resultPoints.addPoint(new Point2D(j, i));
	  				}
	  			}
	  		}
	  	return resultPoints;
	}
	
	/**
	 * Method which detects thin edges.
	 * @param image The input image.
	 * @param midImg The output image.
	 * @throws JIPException
	 */
	private void edgeThin (JIPImgBitmap image, JIPImgBitmap midImg) throws JIPException {
		double[] mid = midImg.getAllPixels();
		double[] r = image.getAllPixels();
		int width = image.getWidth();
		int height = image.getHeight();
		
		int   centre, b01, b12, b21, b10,
		      p1, p2, p3, p4,
		      b00, b02, b20, b22,
		      m, n, a=0, b=0, mp, x, y;
		
		int []l = new int[9];

		for (int i=4; i < height-4; i++)
		    for (int j=4; j < width-4; j++)
		    	if (mid[i*width+j] < 8) {
			        centre = (int)r[i*width+j];
			        /* count number of neighbours */
			        mp = (i-1)*width + j-1;
	
			        n = (mid[mp]<8?1:0) +
		        		(mid[mp+1]<8?1:0) +
		        		(mid[mp+2]<8?1:0) +
			        	(mid[mp+width]<8?1:0) +
			        	(mid[mp+width+2]<8?1:0) +
			        	(mid[mp+width+width]<8?1:0) +
			        	(mid[mp+width+width+1]<8?1:0) +
			        	(mid[mp+width+width+2]<8?1:0);
	
			        /* n==0 no neighbours - remove point */
			        if (n == 0)
			        	mid[i*width+j] = 100;
	
			        /* n==1 - extend line if I can */
			        /* extension is only allowed a few times - the value of mid is used to control this */
			        if (n == 1 && mid[i*width+j] < 6) {
			          /* find maximum neighbour weighted in direction opposite the
			             neighbour already present. e.g.
			             have: O O O  weight r by 0 2 3
			                   X X O              0 0 4
			                   O O O              0 2 3     */
	
			            l[0]=(int)r[(i-1)*width+j-1]; l[1]=(int)r[(i-1)*width+j]; l[2]=(int)r[(i-1)*width+j+1];
			            l[3]=(int)r[(i  )*width+j-1]; l[4]=0;                 l[5]=(int)r[(i  )*width+j+1];
			            l[6]=(int)r[(i+1)*width+j-1]; l[7]=(int)r[(i+1)*width+j]; l[8]=(int)r[(i+1)*width+j+1];
	
			            if (mid[(i-1)*width+j-1] < 8) { 
			            	l[0]=0; l[1]=0; l[3]=0; l[2]*=2; 
			            	l[6]*=2; l[5]*=3; l[7]*=3; l[8]*=4; 
			            }
			            else { 
			            	if (mid[(i-1)*width+j] < 8) { 
			            		l[1]=0; l[0]=0; l[2]=0; l[3]*=2; 
			            		l[5]*=2; l[6]*=3; l[8]*=3; l[7]*=4; 
			            	}
			            	else { 
			            		if (mid[(i-1)*width+j+1] < 8) { 
			            			l[2]=0; l[1]=0; l[5]=0; l[0]*=2; 
			            			l[8]*=2; l[3]*=3; l[7]*=3; l[6]*=4; 
			            		}
			            		else { 
			            			if (mid[(i)*width+j-1] < 8) { 
			            				l[3]=0; l[0]=0; l[6]=0; l[1]*=2; 
			            				l[7]*=2; l[2]*=3; l[8]*=3; l[5]*=4; 
			            			}
			            			else { 
			            				if (mid[(i)*width+j+1] < 8) { 
			            					l[5]=0; l[2]=0; l[8]=0; l[1]*=2; 
			            					l[7]*=2; l[0]*=3; l[6]*=3; l[3]*=4; 
			            				}
			            				else { 
			            					if (mid[(i+1)*width+j-1] < 8) { 
			            						l[6]=0; l[3]=0; l[7]=0; l[0]*=2; 
			            						l[8]*=2; l[1]*=3; l[5]*=3; l[2]*=4; 
			            					}
			            					else { 
			            						if (mid[(i+1)*width+j] < 8) { 
			            							l[7]=0; l[6]=0; l[8]=0; l[3]*=2; 
			            							l[5]*=2; l[0]*=3; l[2]*=3; l[1]*=4; 
			            						}
			            						else { 
			            							if (mid[(i+1)*width+j+1] < 8) { 
			            								l[8]=0; l[5]=0; l[7]=0; l[6]*=2; 
			                                            l[2]*=2; l[1]*=3; l[3]*=3; l[0]*=4; 
			                                        } 
			            						}
			            					}
			            				}
			            			}
			            		}
			            	}
			            }
	
				        m = 0;     /* find the highest point */
				        for (y=0; y < 3; y++)
				            for (x=0; x < 3; x++)
				            	if (l[y+y+y+x] > m) { 
				            		m = l[y+y+y+x]; a = y; b = x; 
				            	}
		
				        if (m > 0) {
				            if (mid[i*width+j] < 4)
				            	mid[(i+a-1)*width+j+b-1] = 4;
				            else
				            	mid[(i+a-1)*width+j+b-1] = mid[i*width+j]+1;
				            
				            if ((a+a+b) < 3) /* need to jump back in image */ {
				            	i += a-1;
				            	j += b-2;
				            	if (i < 4) i = 4;
				            	if (j < 4) j = 4;
				            }
				        }
			        }
			        if (n == 2) {
			        	/* put in a bit here to straighten edges */
			        	b00 = (mid[(i-1)*width+j-1]<8?1:0); /* corners of 3x3 */
			        	b02 = (mid[(i-1)*width+j+1]<8?1:0);
			        	b20 = (mid[(i+1)*width+j-1]<8?1:0);
			        	b22 = (mid[(i+1)*width+j+1]<8?1:0);
			        	if (((b00+b02+b20+b22) == 2) && ((b00 == 1 || b22 == 1) && (b02 == 1 || b20 == 1))) {  
			        		/* case: move a point back into line.
			                	e.g. X O X  CAN  become X X X
			                     	 O X O              O O O
			                     	 O O O              O O O    */
			        		if (b00 == 1)  {
			        			if (b02 == 1) { x = 0; y = -1; }
			        			else     { x = -1; y = 0; }
			        		}
			        		else {
			        			if (b02 == 1) { x = 1; y = 0; }
			        			else     { x = 0; y = 1; }
			        		}
			        		if (r[(i+y)*width+j+x]/centre > 0.7) {
			        			if ( ( (x==0) && (mid[(i+(2*y))*width+j]>7) && (mid[(i+(2*y))*width+j-1]>7) && (mid[(i+(2*y))*width+j+1]>7) ) ||
			        				  ( (y==0) && (mid[(i)*width+j+(2*x)]>7) && (mid[(i+1)*width+j+(2*x)]>7) && (mid[(i-1)*width+j+(2*x)]>7) ) ) {
			        			  mid[(i)*width+j] = 100;
			        			  mid[(i+y)*width+j+x] = 3;  /* no jumping needed */
			        			}
			        		}
			        	}
			        	else {
			        		b01 = (mid[(i-1)*width+j  ]<8?1:0);
			        		b12 = (mid[(i  )*width+j+1]<8?1:0);
			        		b21 = (mid[(i+1)*width+j  ]<8?1:0);
			        		b10 = (mid[(i  )*width+j-1]<8?1:0);
			        		/* right angle ends - not currently used */
			        		if ( ((b01+b12+b21+b10)==2) && ((b10==1 || b12==1) && (b01==1 || b21==1)) &&
			        				((b01==1 && ((mid[(i-2)*width+j-1]<8) || (mid[(i-2)*width+j+1]<8)))|(b10 ==1 &&((mid[(i-1)*width+j-2]<8) || (mid[(i+1)*width+j-2]<8))) ||
			        				(b12==1 && ((mid[(i-1)*width+j+2]<8) || (mid[(i+1)*width+j+2]<8))) || (b21==1 && ((mid[(i+2)*width+j-1]<8) || (mid[(i+2)*width+j+1]<8)))) ) { 
				        	 /* case; clears odd right angles.
			                 e.g.; O O O  becomes O O O
			                       X X O          X O O
			                       O X O          O X O     */
			        			mid[(i)*width+j] = 100;
			        			i--;               /* jump back */
			        			j -= 2;
			        			if (i < 4) i = 4;
			        			if (j < 4) j = 4;
			        		}
			        	}
			        }
	
			        /* n>2 the thinning is done here without breaking connectivity */
			        if (n > 2) {
			            b01 = (mid[(i-1)*width+j  ]<8?1:0);
			            b12 = (mid[(i  )*width+j+1]<8?1:0);
			            b21 = (mid[(i+1)*width+j  ]<8?1:0);
			            b10 = (mid[(i  )*width+j-1]<8?1:0);
			            if ((b01+b12+b21+b10)>1) {
			            	b00 = (mid[(i-1)*width+j-1]<8?1:0);
			            	b02 = (mid[(i-1)*width+j+1]<8?1:0);
						    b20 = (mid[(i+1)*width+j-1]<8?1:0);
						    b22 = (mid[(i+1)*width+j+1]<8?1:0);
				            p1 = (b00==1 || b01==1?1:0);
				            p2 = (b02==1 || b12==1?1:0);
				            p3 = (b22==1 || b21==1?1:0);
				            p4 = (b20==1 || b10==1?1:0);
		
				            int aux = 0;
				            if (b01 == 1 && p2 == 1) aux++; 
				            if (b12 == 1 && p3 == 1) aux++;
				            if (b21 == 1 && p4 == 1) aux++;
				            if (b10 == 1 && p1 == 1) aux++;
				            if ( ((p1 + p2 + p3 + p4) - aux) < 2) {
				            	mid[(i)*width+j]=100;
					            i--;
					            j -= 2;
					            if (i < 4) i = 4;
					            if (j < 4) j = 4;
				            }
			            }
			        }
			    }

	}
	
	/**
	 * Method which implements the Susan algorithm for detecting edges.
	 * @param image The input image.
	 * @param midImg The other input image.
	 * @param geomThresh The geometric threshold.
	 * @param lut The brightness LUT.
	 * @return The output image.
	 */
	private JIPImgBitmap susanEdges (JIPImgBitmap image, JIPImgBitmap midImg, int geomThresh, int[] lut) throws JIPException {
		int width = image.getWidth();
		int height = image.getHeight();
		double []pixels = image.getAllPixels();
		double []r = new double[pixels.length];
		double []mid = new double[pixels.length];
		Arrays.fill(mid, 100);
		int n, p, cp;
		double z, c;
		int m, a, b, x, y, s;
		boolean do_symmetry;
		
		for (int i=3; i < height-3; i++)
			for (int j=3; j < width-3; j++) {
				n = 100;
		        p = (i-3)*width + j - 1;
		        cp = (int)pixels[i*width+j];

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-3; 

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-5;

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-6;

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        if (n<geomThresh){    /* do this test early and often ONLY to save wasted computation */
		        	p += 2;
		        	n += lut[256+cp-(int)pixels[p++]];
		        	if (n<geomThresh){
		        		n += lut[256+cp-(int)pixels[p++]];
		        		if (n<geomThresh){
		        			n += lut[256+cp-(int)pixels[p]];
		        			if (n<geomThresh){
		        				p += width-6;

		        				n += lut[256+cp-(int)pixels[p++]];
		        				if (n<geomThresh){
		        					n += lut[256+cp-(int)pixels[p++]];
		        					if (n<geomThresh){
		        						n += lut[256+cp-(int)pixels[p++]];
		        						if (n<geomThresh){
		        							n += lut[256+cp-(int)pixels[p++]];
		        							if (n<geomThresh){
		        								n += lut[256+cp-(int)pixels[p++]];
		        								if (n<geomThresh){
		        									n += lut[256+cp-(int)pixels[p++]];
		        									if (n<geomThresh){
		        										n += lut[256+cp-(int)pixels[p]];
		        										if (n<geomThresh){
		        											p += width-5;

		        											n += lut[256+cp-(int)pixels[p++]];
		        											if (n<geomThresh){
		        												n += lut[256+cp-(int)pixels[p++]];
		        												if (n<geomThresh){
		        													n += lut[256+cp-(int)pixels[p++]];
		        													if (n<geomThresh){
		        														n += lut[256+cp-(int)pixels[p++]];
		        														if (n<geomThresh){
		        															n += lut[256+cp-(int)pixels[p]];
		        															if (n<geomThresh){
		        																p += width-3;

		        																n += lut[256+cp-(int)pixels[p++]];
		        																if (n<geomThresh){
		        																	n += lut[256+cp-(int)pixels[p++]];
		        																	if (n<geomThresh){
		        																		n += lut[256+cp-(int)pixels[p]];

		        																		if (n<geomThresh) 
																				            r[i*width+j] = geomThresh-n;
		        																	}
		        																}
		        															}
		        														}
		        													}
		        												}
		        											}
		        										}
		        									}
		        								}
		        							}
		        						}
		        					}
		        				}
		        			}
		        		}
		        	}
		        }
		    }

	  	for (int i=4; i < height-4; i++)
	  		for (int j=4; j < width-4; j++) {
	  			if (r[i*width+j] > 0)  {
	  		        m = (int)r[i*width+j];
	  		        n = geomThresh - m;
			        cp = (int)pixels[i*width+j];

	  		        if (n > 600) {
	  			        p = (i-3)*width + j - 1;
		  		        x = 0; y = 0;
	
		  		        c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=3*c;
			            c=lut[256+cp-(int)pixels[p++]]; y-=3*c;
			            c=lut[256+cp-(int)pixels[p]];   x += c; y-=3*c;
			            p += width-3; 
			    
			            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y-=2*c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=2*c;
			            c=lut[256+cp-(int)pixels[p++]]; y-=2*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c; y-=2*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 2*c; y-=2*c;
			            p += width-5;
			    
			            c=lut[256+cp-(int)pixels[p++]]; x-=3*c; y-=c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y-=c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=c;
			            c=lut[256+cp-(int)pixels[p++]]; y-=c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c; y-=c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 2*c; y-=c;
			            c=lut[256+cp-(int)pixels[p]];   x += 3*c; y-=c;
			            p += width-6;
	
			            c=lut[256+cp-(int)pixels[p++]]; x-=3*c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=2*c;
			            c=lut[256+cp-(int)pixels[p]];   x-=c;
			            p += 2;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 2*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 3*c;
			            p += width-6;
			    
			            c=lut[256+cp-(int)pixels[p++]]; x-=3*c; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 2*c; y += c;
			            c=lut[256+cp-(int)pixels[p]]; x += 3*c; y += c;
			            p += width-5;
	
			            c=lut[256+cp-(int)pixels[p++]]; x-=2*c; y += 2*c;
			            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += 2*c;
			            c=lut[256+cp-(int)pixels[p++]]; y += 2*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c; y += 2*c;
			            c=lut[256+cp-(int)pixels[p]]; x += 2*c; y += 2*c;
			            p += width-3;
	
			            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += 3*c;
			            c=lut[256+cp-(int)pixels[p++]]; y += 3*c;
			            c=lut[256+cp-(int)pixels[p]]; x += c; y += 3*c;
	
		  		        z = Math.sqrt(x*x + y*y);
		  		        if (z > (0.9*n)) /* 0.5 */ {
		  		            do_symmetry=false;
		  		            if (x == 0)
		  		            	z = 1000000.0;
		  		            else
		  		            	z = (double)y / x;
		  		            if (z < 0) { z = -z; s = -1; }
		  		            else s = 1;
		  		            if (z < 0.5) { /* vert_edge */ a = 0; b = 1; }
		  		            else { if (z > 2.0) { /* hor_edge */ a = 1; b = 0; }
		  		            else { /* diag_edge */ if (s>0) { a = 1; b = 1; }
		  		                                   else { a = -1; b = 1; }}}
		  		            if ( (m > r[(i+a)*width+j+b]) && (m >= r[(i-a)*width+j-b]) &&
		  		                 (m > r[(i+(2*a))*width+j+(2*b)]) && (m >= r[(i-(2*a))*width+j-(2*b)]) )
		  		            	mid[i*width+j] = 1;
		  		        }
		  		        else
		  		            do_symmetry = true;
		  		    }
		  		    else 
		  		    	do_symmetry = true;
	
	  		        if (do_symmetry) { 
	  			        p = (i-3)*width + j - 1;
		  		        x = 0; y=0; s=0;
	
		  		        /*   |      \
		  		             y  -x-  w
		  		             |        \   */

			            c=lut[256+cp-(int)pixels[p++]]; x += c; y += 9*c; s += 3*c;
			            c=lut[256+cp-(int)pixels[p++]]; y += 9*c;
			            c=lut[256+cp-(int)pixels[p]];   x += c; y += 9*c; s-=3*c;
			            p += width-3; 
			    
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c; y += 4*c; s += 4*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += 4*c; s += 2*c;
			            c=lut[256+cp-(int)pixels[p++]]; y += 4*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += 4*c; s-=2*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 4*c; y += 4*c; s-=4*c;
			            p += width-5;
			    
			            c=lut[256+cp-(int)pixels[p++]]; x += 9*c; y += c; s += 3*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c; y += c; s += 2*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += c; s += c;
			            c=lut[256+cp-(int)pixels[p++]]; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += c; s-=c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c; y += c; s-=2*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 9*c; y += c; s-=3*c;
			            p += width-6;
	
			            c=lut[256+cp-(int)pixels[p++]]; x += 9*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c;
			            c=lut[256+cp-(int)pixels[p]];   x += c;
			            p += 2;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 9*c;
			            p += width-6;
			    
			            c=lut[256+cp-(int)pixels[p++]]; x += 9*c; y += c; s-=3*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c; y += c; s-=2*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += c; s-=c;
			            c=lut[256+cp-(int)pixels[p++]]; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += c; s += c;
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c; y += c; s += 2*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 9*c; y += c; s += 3*c;
			            p += width-5;
	
			            c=lut[256+cp-(int)pixels[p++]]; x += 4*c; y += 4*c; s-=4*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += 4*c; s-=2*c;
			            c=lut[256+cp-(int)pixels[p++]]; y += 4*c;
			            c=lut[256+cp-(int)pixels[p++]]; x += c;   y += 4*c; s += 2*c;
			            c=lut[256+cp-(int)pixels[p]];   x += 4*c; y += 4*c; s += 4*c;
			            p += width-3;
	
			            c=lut[256+cp-(int)pixels[p++]]; x += c; y += 9*c; s-=3*c;
			            c=lut[256+cp-(int)pixels[p++]]; y += 9*c;
			            c=lut[256+cp-(int)pixels[p]];   x += c; y += 9*c; s += 3*c;
		  		        
		  		        if (y == 0)
		  		        	z = 1000000.0;
		  		        else
		  		        	z = (double)x / y;
		  		        if (z < 0.5) { /* vertical */ a = 0; b = 1; }
		  		        else { 
		  		        	if (z > 2.0) { /* horizontal */ a = 1; b = 0; }
		  		        	else { /* diagonal */ 
		  		        		if (s > 0) { a = -1; b = 1; }
		  		        		else { a = 1; b = 1; }
		  		        	}
		  		        }
		  		        if ( (m > r[(i+a)*width+j+b]) && (m >= r[(i-a)*width+j-b]) &&
		  		             (m > r[(i+(2*a))*width+j+(2*b)]) && (m >= r[(i-(2*a))*width+j-(2*b)]) )
		  		        	mid[i*width+j] = 2;	
		  		    }
	  			}
	  		}
	  	midImg.setAllPixels(mid);
	  	JIPImgBitmap resultImg = (JIPImgBitmap)JIPImage.newImage(width, height, image.getType());
	  	resultImg.setAllPixels(r);
	  	return resultImg;
	}
	
	/**
	 * Method which implements the quick Susan algorithm for detecting edges.
	 * @param image The input image.
	 * @param midImg The other input image.
	 * @param geomThresh The geometric threshold.
	 * @param lut The brightness LUT.
	 * @return The output image.
	 */
	private JIPImgBitmap susanEdgesSmall (JIPImgBitmap image, JIPImgBitmap midImg, int geomThresh, int[] lut) throws JIPException {
		int width = image.getWidth();
		int height = image.getHeight();
		double []pixels = image.getAllPixels();
		double []r = new double[pixels.length];
		double []mid = new double[pixels.length];
		Arrays.fill(mid, 100);
		int n, p, cp;
		double z, c;
		int m, a, b, x, y, s;
		boolean do_symmetry;
		geomThresh = 730; 
		
		for (int i=1; i < height-1; i++)
			for (int j=1; j < width-1; j++) {
				n = 100;
		        p = (i-1)*width + j - 1;
		        cp = (int)pixels[i*width+j];

		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-2; 
		        
		        n += lut[256+cp-(int)pixels[p]];
		        p += 2;
		        n += lut[256+cp-(int)pixels[p]];
		        p += width-2; 
		        
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p++]];
		        n += lut[256+cp-(int)pixels[p]];

				if (n<geomThresh) 
		            r[i*width+j] = geomThresh-n;
		    }

	  	for (int i=2; i < height-2; i++)
	  		for (int j=2; j < width-2; j++) {
	  			if (r[i*width+j] > 0)  {
	  		        m = (int)r[i*width+j];
	  		        n = geomThresh - m;
			        cp = (int)pixels[i*width+j];

	  		        if (n > 250) {
	  			        p = (i-3)*width + j - 1;
		  		        x = 0; y = 0;
	
		  		        c=lut[256+cp-(int)pixels[p++]]; x-=c; y-=c;
			            c=lut[256+cp-(int)pixels[p++]]; y-=c;
			            c=lut[256+cp-(int)pixels[p]];   x += c; y-=c;
			            p += width-2; 
	
			            c=lut[256+cp-(int)pixels[p]]; x-=c;
			            p += 2;
			            c=lut[256+cp-(int)pixels[p]]; x += c;
			            p += width-2;
	
			            c=lut[256+cp-(int)pixels[p++]]; x-=c; y += c;
			            c=lut[256+cp-(int)pixels[p++]]; y += c;
			            c=lut[256+cp-(int)pixels[p]]; x += c; y += c;
	
		  		        z = Math.sqrt(x*x + y*y);
		  		        if (z > (0.4*n)) /* 0.5 */ {
		  		            do_symmetry=false;
		  		            if (x == 0)
		  		            	z = 1000000.0;
		  		            else
		  		            	z = (double)y / x;
		  		            if (z < 0) { z = -z; s = -1; }
		  		            else s = 1;
		  		            if (z < 0.5) { /* vert_edge */ a = 0; b = 1; }
		  		            else { if (z > 2.0) { /* hor_edge */ a = 1; b = 0; }
		  		            else { /* diag_edge */ if (s > 0) { a = 1; b = 1; }
		  		                                   else { a=-1; b=1; }}}
		  		            if ( (m > r[(i+a)*width+j+b]) && (m >= r[(i-a)*width+j-b])) 
		  		            	mid[i*width+j] = 1;
		  		        }
		  		        else
		  		            do_symmetry = true;
		  		    }
		  		    else 
		  		    	do_symmetry = true;
	
	  		        if (do_symmetry) { 
	  			        p = (i-1)*width + j - 1;
		  		        x = 0; y = 0; s = 0;
	
		  		        /*   |      \
		  		             y  -x-  w
		  		             |        \   */

			            c=lut[256+cp-(int)pixels[p++]]; x += c; y += c; s += c;
			            c=lut[256+cp-(int)pixels[p++]]; y += c;
			            c=lut[256+cp-(int)pixels[p]];   x += c; y += c; s-=c;
			            p += width-3; 
	
			            c=lut[256+cp-(int)pixels[p]];   x += c;
			            p += 2;
			            c=lut[256+cp-(int)pixels[p]];   x += c;
			            p += width-2;
	
			            c=lut[256+cp-(int)pixels[p++]]; x += c; y += c; s-=c;
			            c=lut[256+cp-(int)pixels[p++]]; y += c;
			            c=lut[256+cp-(int)pixels[p]];   x += c; y += c; s += c;
		  		        
		  		        if (y == 0)
		  		        	z = 1000000.0;
		  		        else
		  		        	z = (double)x / y;
		  		        if (z < 0.5) { /* vertical */ a = 0; b = 1; }
		  		        else { 
		  		        	if (z > 2.0) { /* horizontal */ a = 1; b = 0; }
		  		        	else { /* diagonal */ 
		  		        		if (s > 0) { a = -1; b = 1; }
		  		        		else { a = 1; b = 1; }
		  		        	}
		  		        }
		  		        if ( (m > r[(i+a)*width+j+b]) && (m >= r[(i-a)*width+j-b]) )
		  		        	mid[i*width+j] = 2;	
		  		    }
	  			}
	  		}
	  	midImg.setAllPixels(mid);
	  	JIPImgBitmap resultImg = (JIPImgBitmap)JIPImage.newImage(width, height, image.getType());
	  	resultImg.setAllPixels(r);
	  	return resultImg;
	}

	/**
	 * Method which detects thin edges.
	 * @param midImg The input image.
	 * @param lengthThresh The threshold length.
	 * @return The output image.
	 * @throws JIPException
	 */
	private JIPGeomEdges extractEdges (JIPImgBitmap midImg, int lengthThresh) throws JIPException {
		double[] mid = midImg.getAllPixels();
		int w = midImg.getWidth();
		int h = midImg.getHeight();
		int []p = new int[2];
		
		JIPGeomEdges result = (JIPGeomEdges)JIPImage.newImage(w, h, ImageType.EDGES);
		for (int i=5; i<h-5; i++)
	  		for (int j=5; j<w-5; j++) {
	  			if (mid[i*w+j] < 8)  {
	  				mid[i*w+j] = 100;
	  				Edge e = new Edge();
  		  			e.addPoint(new Point2D(j,i));
	  				p[0] = i;
	  				p[1] = j;
	  				while (nextPoint(w, h, p, mid)) {
	  					mid[p[0]*w+p[1]] = 100;
	  		  			e.addPoint(new Point2D(p[1],p[0]));
	  				}
	  				e.reverse();
	  				while (nextPoint(w, h, p, mid)) {
	  					mid[p[0]*w+p[1]] = 100;
	  		  			e.addPoint(new Point2D(p[1],p[0]));
	  				}
	  				if (e.length() > lengthThresh)
	  					result.addEdge(e);  
	  			}
	  		}
		
		return result;
	}
	
	/**
	 * Method which calculates the next point for continuing with the trace.
	 * @param width The image width.
	 * @param height The image height.
	 * @param p The current row and column.
	 * @param bmp The pixels of an image.
	 * @return A boolean indicating ture if it finds the next point, false in otherwise.
	 */
	public boolean nextPoint(int width, int height, int []p, double[]bmp) {
		int i, row, col;
		int[] roff = new int[8];
		int[] coff = new int[8];
		roff[0] = 1;
		roff[1] = 0;
		roff[2] = -1;
		roff[3] = 0;
		roff[4] = 1;
		roff[5] = 1;
		roff[6] = -1;
		roff[7] = -1;
		coff[0] = 0;
		coff[1] = 1;
		coff[2] = 0;
		coff[3] = -1;
		coff[4] = 1;
		coff[5] = -1;
		coff[6] = -1;
		coff[7] = 1;

		for (i=0; i < 8; i++) {
			row = p[0] + roff[i];
			col = p[1] + coff[i];
			if (row >= 0 && col >= 0 && row < height && col < width)
				if (bmp[col + row * width] < 8) {
					p[0] += roff[i];
					p[1] += coff[i];
					return true;
				}
		}
		return false;
	} 
}

