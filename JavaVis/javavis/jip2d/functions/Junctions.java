package javavis.jip2d.functions;

import java.util.ArrayList;
import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.base.parameter.ParamList;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;
import javavis.jip2d.base.geometrics.JIPGeomJunctions;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Junction;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.util.JunctionAux;

/**
 * It is the implementation of the methods proposed in "Two Bayesian Methods for JunctionAux 
 * Detection", M. Cazorla y F. Escolano. IEEE transaction on Image Processing (2003). 
 * volume 12. number 3 pp 317-327.<br />
 * It applies to COLOR, BYTE, SHORT or FLOAT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a COLOR, BYTE, SHORT or FLOAT type.</li>
 * <li><em>method</em>: List of methods to apply: origImg, ponpoff (default origImg).</li>
 * <li><em>thresPonPoff</em>: Real value which indicates the threshold to detect junctions 
 * with PonPoff method (default 0.5).</li>
 * <li><em>thresClassic</em>: Real value which indicates the threshold to detect junctions 
 * with original method (default 0.5).</li>
 * <li><em>difAng</em>: Integer value which indicates the minimum angular difference (default 20).</li>
 * <li><em>difReg</em>: Integer value which indicates the region difference (gray level) (default 15).</li>
 * <li><em>iter</em>: Integer value which indicates the number of iterations (original method)
 * (default 10).</li>
 * <li><em>r_i</em>: Integer value which indicates the internal radius (default 2).</li>
 * <li><em>r_e</em>: Integer value which indicates the external radius (default 5).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed JUNCTION image.</li>
 * </ul><br />
 */
public class Junctions extends Function2D {
	private static final long serialVersionUID = -6325253526988864760L;
	
	private static int ANG_TOL = 4;
	private static int CLOSEST_ANG = 30;
	
	public Junctions() {
		super();
		name = "JunctionAux";
		description = "Gets the junctions in the image. Applies to COLOR, BYTE, SHORT or FLOAT type.";
		groupFunc = FunctionGroup.FeatureExtract;

		ParamList p1 = new ParamList("method", false, true);
		String []values = {"origImg", "ponpoff"};
		p1.setDefault(values);
		p1.setDescription("Methods to apply");
		
		ParamFloat p2 = new ParamFloat("thresPonPoff", false, true);
		p2.setDefault(0.5f);
		p2.setDescription("Threshold to detect junctions with PonPoff method");
		
		ParamFloat p3 = new ParamFloat("thresClassic", false, true);
		p3.setDefault(0.5f);
		p3.setDescription("Threshold to detect junctions with original method");
		
		ParamInt p4 = new ParamInt("difAng", false, true);
		p4.setDefault(20);
		p4.setDescription("Minimum angular difference");
		
		ParamInt p5 = new ParamInt("difReg", false, true);
		p5.setDefault(15);
		p5.setDescription("Region difference (gray level)");
		
		ParamInt p6 = new ParamInt("iter", false, true);
		p6.setDefault(10);
		p6.setDescription("Number of iterations");
		
		ParamInt p7 = new ParamInt("r_i", false, true);
		p7.setDefault(2);
		p7.setDescription("Internal radius");
		
		ParamInt p8 = new ParamInt("r_e", false, true);
		p8.setDefault(5);
		p8.setDescription("External radius");

		addParam(p1);
		addParam(p2);
		addParam(p3);
		addParam(p4);
		addParam(p5);
		addParam(p6);
		addParam(p7);
		addParam(p8);
	}

	public JIPImage processImg(JIPImage img) throws JIPException {
		ImageType t;
		JIPBmpByte imgByte;
		float threshPonPoff, thresClassic;
		int r_i, r_e, difReg, difAng, iter;
		String method;
		
		t = img.getType();
		if (t == ImageType.BIT || img instanceof JIPImgGeometric) 
			throw new JIPException("Function JunctionAux can not be applied to this image format.");

		r_i = getParamValueInt("r_i");
		r_e = getParamValueInt("r_e");
		threshPonPoff = getParamValueFloat("thresPonPoff");
		thresClassic = getParamValueFloat("thresClassic");
		method = getParamValueString("method");
		difReg = getParamValueInt("difReg");
		difAng = getParamValueInt("difAng");
		iter = getParamValueInt("iter");
		method = getParamValueString("method");
		
		switch (t) {
			case COLOR: ColorToGray fcg = new ColorToGray();
						fcg.setParamValue("gray", "BYTE");
						imgByte = (JIPBmpByte)fcg.processImg(img);
						break;
			case BYTE: imgByte = (JIPBmpByte)img;
						break;
			default: 	GrayToGray fgg = new GrayToGray();
						fgg.setParamValue("gray", "BYTE");
						imgByte = (JIPBmpByte)fgg.processImg(img);
						break;
		}
		
		Susan fs = new Susan();
		JIPGeomPoint points = (JIPGeomPoint)fs.processImg(imgByte);
		ArrayList<Point2D> list_points = (ArrayList<Point2D>)points.getData();
		
		if (method.equals("ponpoff")) 
			return calcJunctionsPonPoff (imgByte, list_points, threshPonPoff, r_i, r_e);
		else if (method.equals("origImg"))
			return calcJunctionsOrig(imgByte, list_points, r_i, r_e, iter, difReg, difAng, thresClassic);
		else return img;
	}

	
	/**
	 * Method which implements the original method.
	 */
	private JIPImage calcJunctionsOrig (JIPBmpByte imgByte, ArrayList<Point2D> list_points, int r_i, int r_e, int iter, int difReg, int difAng, double geometricThresh) throws JIPException {
		int[] edges;
		JIPGeomJunctions ret;
		
		ret = (JIPGeomJunctions)JIPImage.newImage(imgByte.getWidth(), imgByte.getHeight(), ImageType.JUNCTION);
		
		Junction j;
		for (Point2D p : list_points) {
			int x = p.getX();
			int y = p.getY();
			if (x < r_e || x > imgByte.getWidth()-r_e-1 || y < r_e || y > imgByte.getHeight()-r_e-1) 
				continue;
			edges = findHisto (imgByte, x, y, r_i, r_e, iter, difReg, difAng, geometricThresh);
			if (edges != null) {
				j = new Junction(x, y, r_i, r_e, edges);
				ret.addJunction(j);
			}
		}
		
		return ret;
	}

	/**
	 * Method which calculate the gradient of an image.
	 * @param ang The angle.
	 * @param The data image.
	 * @param threshold The threshold value.
	 * @return The gradient of an image.
	 */
	private double calculate_gradient (int ang, double image[], double threshold) {  
	   double aux;
	   int count = 0;
	   
	   aux = image [(((ang-10)<0)?ang+350:ang-10)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-9)<0)?ang+351:ang-9)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-8)<0)?ang+352:ang-8)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-7)<0)?ang+353:ang-7)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-6)<0)?ang+354:ang-6)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-5)<0)?ang+355:ang-5)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-4)<0)?ang+356:ang-4)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-3)<0)?ang+357:ang-3)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-2)<0)?ang+358:ang-2)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   aux = image [(((ang-1)<0)?ang+359:ang-1)];
	   if (Math.abs(aux-image[ang]) < threshold) count++;
	   
	   if (Math.abs(image[(ang+1)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+2)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+3)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+4)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+5)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+6)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+7)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+8)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+9)%360]-image[ang]) < threshold) count++;
	   if (Math.abs(image[(ang+10)%360]-image[ang]) < threshold) count++;   
	   
	   return count;
	}
	
	/**
	 * Method which calculate the median.
	 * @param vector Vector with the data.
	 * @param num The final size for calculating the median.
	 * @return The median value.
	 */
	private double median (double []vector, int num) {
	   double aux;
	   
	   for (int i=1; i<num; i++)
	      for (int j=0; j < num-i; j++) 
	         if (vector[j] > vector[j+1]) {
	            aux = vector [j+1];
	            vector[j+1] = vector[j];
	            vector[j] = aux;
	         }
	   return vector[num/2];
	}

	/**
	 * Method which compares the actual image gradient with the others.
	 * @param i The position.
	 * @param image_grad The image data.
	 * @return A boolean indicating the comparison between the current image gradient and
	 * the others.
	 */
	private boolean comparation (int i, double image_grad[])  {
	   return (image_grad[i] >= image_grad[(((i-10)<0)?i+350:i-10)] &&
	           image_grad[i] >= image_grad[(((i-9)<0)?i+351:i-9)] &&
	           image_grad[i] >= image_grad[(((i-8)<0)?i+252:i-8)] &&
	           image_grad[i] >= image_grad[(((i-7)<0)?i+253:i-7)] &&
	           image_grad[i] >= image_grad[(((i-6)<0)?i+254:i-6)] &&
	      	   image_grad[i] >= image_grad[(((i-5)<0)?i+355:i-5)] &&
	           image_grad[i] >= image_grad[(((i-4)<0)?i+356:i-4)] &&
	           image_grad[i] >= image_grad[(((i-3)<0)?i+257:i-3)] &&
	           image_grad[i] >= image_grad[(((i-2)<0)?i+258:i-2)] &&
	           image_grad[i] >= image_grad[(((i-1)<0)?i+259:i-1)] &&
	           image_grad[i] > image_grad[(i+1)%360] &&
	           image_grad[i] > image_grad[(i+2)%360] &&
	           image_grad[i] > image_grad[(i+3)%360] &&
	           image_grad[i] > image_grad[(i+4)%360] &&
	           image_grad[i] > image_grad[(i+5)%360] &&
	           image_grad[i] > image_grad[(i+6)%360] &&
	           image_grad[i] > image_grad[(i+7)%360] &&
	           image_grad[i] > image_grad[(i+8)%360] &&
	           image_grad[i] > image_grad[(i+9)%360] &&
	           image_grad[i] > image_grad[(i+10)%360]);
	}
	
	/**
	 * Method which calculates mean and variance
	 * @param image The image data.
	 * @param inf The initial position.
	 * @param sup The final position.
	 * @param The statistics data.
	 */
	private void calc_rob (double image[], int inf, int sup, double []statistics) {
	   double vector[] = new double[360];
	   
	    for (int i=inf; i < sup; i++)
	    	vector [i-inf] = image [i%360];
	      
	    statistics[0] = median (vector, sup-inf);
	    statistics[1] = 0.00;
	    
	    for (int i=inf; i < sup; i++) 
	    	statistics[1] += Math.pow(image[i%360]-statistics[0], 2);
	    
	    statistics[1] /= sup-inf;
	}
	
	/**
	 * Method which find the histogram.
	 * @param img The input image.
	 * @param x X.
	 * @param y Y.
	 * @param r_i The internal radius.
	 * @param r_e The external radius.
	 * @param iter The number of iterations.
	 * @param difReg
	 * @param difAng
	 * @param geometricThreshold The geometric threshold
	 * @return A integer vector.
	 * @throws JIPException
	 */
	private int[] findHisto (JIPBmpByte img, int x, int y, int r_i, int r_e, int iter, int difReg, int difAng, double geometricThreshold) throws JIPException {
		double acum, aux_acum, displacement;
		int mark, ang, currentAng, currentAng1, counter, nextAng;
		boolean first;
		double[] vector_acum, image_grad, image;
		double[] average, variance;
		int[] situation;
		
		vector_acum = new double[20];
		average = new double[360];
		variance = new double[360];
		image_grad = new double[360];
		image = new double[360];
		situation = new int[100];
		
		for (ang=0; ang < 360; ang++) {
			acum = 0.0f;
			mark = 1;
		    first = true;
	        for (int i=0; i < JunctionAux.discr_cont_seg(ang); i++)  {
		    	aux_acum = acum;
		    	acum += JunctionAux.discr_value(i,ang);
		    	if (acum >= r_i) {
		    		/* First time */
		    		if (first) {
		    			vector_acum [0] = img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) * (acum-r_i);
		    			first=false;
		    			continue;
		    		}
		    		/* Final */
		    		if (acum > r_e) {
		    			if ((acum-(mark+r_i)) > 1) {
		    				vector_acum [mark-1] += img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang))
		    					* ((r_i+mark)-aux_acum);
		    				vector_acum [mark] = img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang));
		    			}
		    			else {
		    				vector_acum [mark-1] += img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang))
		    					* (r_e-aux_acum);
		    			}
		    			break;
		    		}
		    		/* One pixel is accumulated */
		    		if (acum > (r_i+mark)) {
		    			vector_acum [mark-1] += img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) *
		    				((r_i+mark)-aux_acum);
		    			vector_acum [mark] = img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) *
		    				(acum-(r_i+mark));	
		    			mark++;
		    			continue;
		    		}
		    		vector_acum [mark-1] += img.getPixel(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) *
		    				JunctionAux.discr_value(i,ang);	
		    	}
	        }
	        image [ang] = median (vector_acum, r_e-r_i);
		}

		/* Gradient */
        for (ang=0; ang < 360; ang++) {
	       image_grad[ang] = calculate_gradient(ang, image, 3.00);
	
	       /* Geometric threshold */
	       if (image_grad[ang] > geometricThreshold) image_grad [ang] = 0;
	       else image_grad [ang] = geometricThreshold - image_grad [ang];
        }
        
        /* Initialize segmentation */
        int egdeNumber = 0;
        for (int i=0; i < 360; i++) {
           if (image_grad[i]>=geometricThreshold/2 && comparation(i,image_grad)) {
              situation [egdeNumber] = i;
              egdeNumber++;
           }
        }
        for (int i=0; i < egdeNumber; i++) {
           currentAng = i;
           currentAng1 = (currentAng==0?egdeNumber-1:i-1);
           if (Math.abs(situation[currentAng] - situation[currentAng1])<15) {
              if (image[situation[currentAng]] > image[situation[currentAng1]])
                 ang = currentAng1;
              else ang = currentAng;
              egdeNumber--;
              for (; ang < egdeNumber; ang++)
                 situation [ang] = situation [ang+1];
           }
        }
        
        /* Check if two junctions can be merge */
        counter = 0;
        double statistics[] = new double[2];
        
        while (counter < egdeNumber && egdeNumber > 1) {
	           currentAng   = counter;
	           currentAng1 = (currentAng==0?egdeNumber-1:currentAng-1);
	           
	           /* Calculate mean and variance*/
	           if (situation[currentAng] > situation[(currentAng+1)%egdeNumber])
	              nextAng = 360 + situation[(currentAng+1)%egdeNumber];
	           else nextAng = situation[(currentAng+1)%egdeNumber];
	                              
	           calc_rob (image, situation[currentAng], nextAng, statistics);
	           average[currentAng] = statistics[0];
	           variance[currentAng] = statistics[1];
	            
	           if (situation[currentAng1] > situation[(currentAng1+1)%egdeNumber])
	              nextAng = 360 + situation[(currentAng1+1)%egdeNumber];
	           else nextAng = situation[(currentAng1+1)%egdeNumber];
	
	           calc_rob (image, situation[currentAng1], nextAng,statistics);
	           average[currentAng1] = statistics[0];
	           variance[currentAng1] = statistics[1];
	
	           /* Merge two regions */
	           if (Math.abs(average[currentAng] - average[currentAng1]) < difReg)  {
	        	   egdeNumber--;
	              for (; currentAng < egdeNumber; currentAng++)
	                     situation [currentAng] = situation [currentAng+1];
	              counter = 0;
	           }
	
	           counter++;
        }

        boolean join = false;
        for (counter=0; counter < iter && egdeNumber > 1; counter++) {
               currentAng   = counter%egdeNumber;
               currentAng1 = (currentAng==0?egdeNumber-1:currentAng-1);               

               /* Motion dynamic */
               displacement = Math.log(Math.sqrt(variance[currentAng]/variance[currentAng1])) +
                     (Math.pow (image[situation[currentAng]]-average[currentAng], 2)/(2*variance[currentAng]))-
                     (Math.pow (image[situation[currentAng]]-average[currentAng1], 2)/(2*variance[currentAng1]));
                     
            if (displacement > 0.01) situation [currentAng]++;
            else if (displacement < 0.01) situation [currentAng]--;
            
            if (situation [currentAng] < 0) situation [currentAng] += 360;
            else if (situation [currentAng] >= 360) situation [currentAng] -= 360;

           /* Check if 2 areas can be fused */
           if (Math.abs((average[currentAng] - average[currentAng1])) < difReg)  {
        	   egdeNumber--;
                 for (; currentAng<egdeNumber; currentAng++)
                    situation [currentAng] = situation [currentAng+1];
              join = true;
           }

           /* Delete an angular section if it is too small (less than 20)*/
           for (int i=0; i<egdeNumber; i++) {
              currentAng1 = (i==0?egdeNumber-1:i-1);
              if (Math.abs(((situation[i]+360) - situation[currentAng1])%360) < difAng) {
            	  egdeNumber--;
                 for (int j=i; j < egdeNumber; j++)
                    situation [j] = situation [j+1];
                 join = true;
              }
           }

           /* Calculate median and variances */
           if (join) 
              for (currentAng=0; currentAng < egdeNumber; currentAng++) {
                 currentAng1 = (currentAng==0?egdeNumber-1:currentAng-1);

                 if (situation[currentAng] > situation[(currentAng+1)%egdeNumber])
                    nextAng = 360 + situation[(currentAng+1)%egdeNumber];
                 else nextAng = situation[(currentAng+1)%egdeNumber];

                 calc_rob (image, situation[currentAng], nextAng, statistics);
                 average[currentAng]=statistics[0];
  	           	 variance[currentAng]=statistics[1];
              }
           join = false;
        }
	
	/* Delete junctions with only a angular section (edgeNumber==1) and junctions which are straight lines */
	if (egdeNumber < 2 || (egdeNumber == 2 && ((Math.abs(situation[0] - ((situation[1]+180)%360)) < 25) ||
             (Math.abs(((situation[0]+180)%360) - situation[1]) < 25))))
               return null;
	
		int[] ret = new int[egdeNumber];
		for (int i=0; i < egdeNumber; i++)
			ret[i] = situation[i];
		return ret;
	}

	/**
	 * Method which implements the ponpoff method.
	 * @param imgByte The input image.
	 * @param list_points The list of points.
	 * @param thres The threshold value.
	 * @param r_i The internal radius.
	 * @param r_e The external radius.
	 * @return An image.
	 * @throws JIPException
	 */
	private JIPImage calcJunctionsPonPoff (JIPBmpByte imgByte, ArrayList<Point2D> list_points, float thresh, int r_i, int r_e) throws JIPException {
		float []hon = JunctionAux.hon;
		float []hoff = JunctionAux.hoff;
		float sumon = 0.0f, sumoff = 0.0f;
		float logponpoff[];
		int[] edges;
		JIPBmpFloat grad;
		JIPGeomJunctions ret;

		//Normalize the histograms
		for (int i=0; i < hon.length; i++)
      		sumon += hon[i];
        for (int i=0; i < hoff.length; i++)
            sumoff += hoff[i];
		logponpoff = new float[hoff.length];
		for (int i=0; i < hon.length; i++) {
			hon[i] /= sumon;
			hoff[i] /= sumoff;
			logponpoff[i] = (float)Math.log(hon[i]/hoff[i]);
		}
		
		grad = JunctionAux.filterCalculation(imgByte);
		
		ret = (JIPGeomJunctions)JIPImage.newImage(imgByte.getWidth(), imgByte.getHeight(), ImageType.JUNCTION);
		
		Junction j;
		for (Point2D p : list_points) {
			int x = p.getX();
			int y = p.getY();
			if (x < r_e || x > imgByte.getWidth()-r_e-1 || y < r_e || y > imgByte.getHeight()-r_e-1) 
				continue;
			edges = findHisto (grad, x, y, r_i, r_e, thresh, logponpoff);
			if (edges != null) {
				j = new Junction(x, y, r_i, r_e, edges);
				ret.addJunction(j);
			}
		}
		
		return ret;
	}

	/** 
	 * Method which find the histogram.
	 * @param grad The input float image.
	 * @param x X.
	 * @param y Y.
	 * @param r_i The internal radius.
	 * @param r_e The external radius.
	 * @param iter The number of iterations.
	 * @param difReg
	 * @param difAng
	 * @param geometricThreshold The geometric threshold
	 * @return A integer vector.
	 * @throws JIPException
	 */
	private int[] findHisto (JIPBmpFloat grad, int x, int y, int r_i, int r_e, float threshold, float logponpoff[]) throws JIPException{
		float acum, aux_acum;
		int mark, bin;
		float[] vector_acum, vector_acum2;
		float[] histo, histo2, histo_aux;
		int[] situation;
		
	    float ang_tol = (float)(ANG_TOL*Math.PI/180.00);  // Pang
	    float inbox = (float)Math.log(2*Math.PI*0.9/(4*ang_tol));
	    float outbox = (float)Math.log(2*Math.PI*0.1/(2*Math.PI-4*ang_tol));
		
		vector_acum = new float[20];
		vector_acum2 = new float[20];
		histo = new float[360];
		histo2 = new float[360];
		histo_aux = new float[360];
		situation = new int[100];
		
		for (int ang=0; ang < 360; ang++) {
			acum = 0.0f;
			mark = 0;
	        for (int i=0; i < JunctionAux.discr_cont_seg(ang); i++)  {
		    	aux_acum = acum;
		    	acum += JunctionAux.discr_value(i,ang);
		    	bin = JunctionAux.which_bin (grad.getPixelFloat(1, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)), JunctionAux.binbnd);
		    	if (acum >= r_i) {
		    		/* First time */
		    		if (mark == 0) {
		    			vector_acum [0] = logponpoff[bin] * (acum-r_i);
		    			vector_acum2 [0] = grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) * (acum-r_i);
		    			mark = 1;
		    			continue;
		    		}
		    		/* Final */
		    		if (acum > r_e) {
		    			if ((acum-(mark+r_i)) > 1) {
		    				vector_acum [mark-1] += logponpoff[bin] * ((r_i+mark)-aux_acum);
		    				vector_acum2 [mark-1] += grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang))
		    					* ((r_i+mark)-aux_acum);
		    				vector_acum [mark] = logponpoff[bin];
		    				vector_acum2 [mark] = grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang));
		    			}
		    			else {
		    				vector_acum2 [mark-1] += grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang))
		    					* (r_e-aux_acum);
		    				vector_acum [mark-1] += logponpoff[bin] * (r_e-aux_acum);
		    			}
		    			break;
		    		}
		    		/* One pixel is accumulated */
		    		if (acum > (r_i+mark)) {
		    			vector_acum [mark-1] += logponpoff[bin] *  ((r_i+mark)-aux_acum);
		    			vector_acum2 [mark-1] += grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) *
		    				((r_i+mark)-aux_acum);
		    			vector_acum [mark] = logponpoff[bin] * (acum-(r_i+mark));
		    			vector_acum2 [mark] = grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) *
		    				(acum-(r_i+mark));	
		    			mark++;
		    			continue;
		    		}
		    		vector_acum [mark-1] += logponpoff[bin] * JunctionAux.discr_value(i,ang);
		    		vector_acum2 [mark-1] += grad.getPixelFloat(0, x + JunctionAux.discr_x(i,ang), y + JunctionAux.discr_y(i,ang)) *
		    				JunctionAux.discr_value(i,ang);	
		    	}
	        }
	        
	        // For each angle, the mean of the evidence is calculated
	        histo[ang] = 0.0f;
	        histo2[ang] = 0.0f;
	        for (int i=0; i < r_e-r_i; i++) {
	        	histo [ang] += vector_acum[i];
	        	histo2 [ang] += (JunctionAux.consistent((float)(Math.PI/2.00+Math.toRadians(ang)), vector_acum2[i], ang_tol) ? inbox : outbox);
	        }
	        histo[ang] /= r_e-r_i;
	        histo2[ang] /= r_e-r_i;
		}

        for (int i=0; i < 360; i++) {
        	System.out.print(histo[i]+", ");
        }
        System.out.println();

        for (int i=0; i<360; i++) {
        	System.out.print(histo2[i]+", ");
        }
        System.out.println();
        
		// Detect the edges
		// First, threshold
		for (int i=0; i < 360; i++) {
		    histo_aux[i] = histo[i]+histo2[i];
		    System.out.print(histo_aux[i] + ", ");
		    if (histo_aux[i] < threshold) histo_aux[i] = 0.0f;
		}
        System.out.println();
		// Then, find the maximum at a neighborhood
		int edge_number = 0;
		for (int i=0; i < 360; i++)
		    if (JunctionAux.comparacion(i,histo_aux))
		    	situation[edge_number++]=i;
		
		int j, k;
		// Delete two close edges
		for (int i=0; i < edge_number; i++)
		    if (Math.abs((situation[i]-(situation[(i+1)%edge_number]+360))%360) < CLOSEST_ANG) {
		    	if (histo_aux[situation[i]]>
		    	    histo_aux[situation[(i+1)%edge_number]]) k = i+1; 
		    	else k = i;
		    	edge_number--;
		    	i--;
		    	for (j=k; j < edge_number; j++) situation[j] = situation[j+1];
		    }
		
		if (edge_number > 1) {
			int[] ret = new int[edge_number];
			for (int i=0; i < edge_number; i++)
				ret[i] = situation[i];
			return ret;
		}
		else return null;
	}
}

