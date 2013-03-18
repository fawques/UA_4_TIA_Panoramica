package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.bitmaps.JIPBmpFloat;

/**
 * It obtains saliency areas from an input image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image.</li>
 * <li><em>4pass</em>: Boolean value which indicates true if we want to use the normalizing
 * using 4-pass (default true).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>A processed image.</li>
 * </ul><br />
 */
public class VisualAttention2 extends Function2D {
	private static final long serialVersionUID = -267578389463647713L;

	private static final int NUM_SCALES = 9;
	private static final float M = 1;
	
	//filters
	/**
	 * @uml.property  name="filterIntensity"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterIntensity;
	/**
	 * @uml.property  name="filterRed"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterRed;
	/**
	 * @uml.property  name="filterGreen"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterGreen;
	/**
	 * @uml.property  name="filtroBlue"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterBlue;
	/**
	 * @uml.property  name="filterYellow"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterYellow;
	/**
	 * @uml.property  name="filterOr0"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterOr0;
	/**
	 * @uml.property  name="filterOr45"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterOr45;
	/**
	 * @uml.property  name="filterOr90"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterOr90;
	/**
	 * @uml.property  name="filterOr135"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> filterOr135;
	
	//maps
	/**
	 * @uml.property  name="mapIntensity"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapIntensity;
	/**
	 * @uml.property  name="mapColorRG"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapColorRG;
	/**
	 * @uml.property  name="mapColorBY"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapColorBY;
	/**
	 * @uml.property  name="mapOr0"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapOr0;
	/**
	 * @uml.property  name="mapOr45"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapOr45;
	/**
	 * @uml.property  name="mapOr90"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapOr90;
	/**
	 * @uml.property  name="mapOr135"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.bitmaps.JIPBmpFloat"
	 */
	ArrayList<JIPBmpFloat> mapOr135;
	
	//conscuipity
	/**
	 * @uml.property  name="ic"
	 * @uml.associationEnd  
	 */
	JIPBmpFloat ic;
	/**
	 * @uml.property  name="cc"
	 * @uml.associationEnd  
	 */
	JIPBmpFloat cc;
	/**
	 * @uml.property  name="oc"
	 * @uml.associationEnd  
	 */
	JIPBmpFloat oc;
	
	/**
	 * @uml.property  name="filter4"
	 */
	boolean filter4;
	
	public VisualAttention2() {
		name = "VisualAttention2";
		description = "Obtains saliency areas from the input image.";
		groupFunc = FunctionGroup.Applic;
		
		ParamBool p1 = new ParamBool("4pass", false, true);
		p1.setDescription("Normalizing using 4-pass");
		p1.setDefault(true);
		addParam(p1);
	}

	@Override
	public JIPImage processImg(JIPImage img) throws JIPException {
		JIPBmpColor inputImage = (JIPBmpColor) img;
		filter4 = getParamValueBool("4pass");
		int width = img.getWidth();
		int height = img.getHeight();
		ScaleNew FS = new ScaleNew();
		
		//create the multiresolution filter
		createFilters(inputImage);
		
		//create the feature maps
		createMaps();
		
		//build the conspicuity maps
		createConspicuity();
		
		//normalize the result
		normalizeRange0R(ic, 1);
		normalizeRange0R(cc, 1);
		normalizeRange0R(oc, 1);
		
		JIPBmpFloat ret = new JIPBmpFloat(10, width, height);
		FS.setParamValue("Width", width);
		FS.setParamValue("Height", height);
//		aux = (JIPBmpFloat)FS.processImg(Ic);
//		ret.setAllPixels(6, aux.getAllPixels());
//		aux = (JIPBmpFloat)FS.processImg(Cc);
//		ret.setAllPixels(7, aux.getAllPixels());
//		aux = (JIPBmpFloat)FS.processImg(Oc);
//		ret.setAllPixels(8, aux.getAllPixels());

		if (filter4)
		{
			normalizeData4pass(ic, M);
			normalizeData4pass(cc, M);
			normalizeData4pass(oc, M);
		}
		else
		{
			normalizeData(ic, M);
			normalizeData(cc, M);
			normalizeData(oc, M);
		}
//		aux = (JIPBmpFloat)FS.processImg(Ic);
//		ret.setAllPixels(9, aux.getAllPixels());
//		aux = (JIPBmpFloat)FS.processImg(Cc);
//		ret.setAllPixels(10, aux.getAllPixels());
//		aux = (JIPBmpFloat)FS.processImg(Oc);
//		ret.setAllPixels(11, aux.getAllPixels());
		
		JIPBmpFloat res = new JIPBmpFloat(width, height);
		res = addMaps(ic, cc);
		res = addMaps(oc, res);

		normalizeRange0R(res, 1);
		FS.setParamValue("Width", width);
		FS.setParamValue("Height", height);
		res = (JIPBmpFloat)FS.processImg(res);
		
		ret.setAllPixels(0, res.getAllPixels());
		ret.setAllPixels(1, filterIntensity.get(0).getAllPixels());
		ret.setAllPixels(2, filterRed.get(0).getAllPixels());
		ret.setAllPixels(3, filterGreen.get(0).getAllPixels());
		ret.setAllPixels(4, filterBlue.get(0).getAllPixels());
		ret.setAllPixels(5, filterYellow.get(0).getAllPixels());
		ret.setAllPixels(6, filterOr0.get(0).getAllPixels());
		ret.setAllPixels(7, filterOr45.get(0).getAllPixels());
		ret.setAllPixels(8, filterOr90.get(0).getAllPixels());
		ret.setAllPixels(9, filterOr135.get(0).getAllPixels());
		
//		ret.setAllPixels(16, ((JIPBmpFloat)FS.processImg(mapaOr0.get(0))).getAllPixels());
//		ret.setAllPixels(17, ((JIPBmpFloat)FS.processImg(mapaOr0.get(1))).getAllPixels());
//		ret.setAllPixels(18, ((JIPBmpFloat)FS.processImg(mapaOr0.get(2))).getAllPixels());
//		ret.setAllPixels(19, ((JIPBmpFloat)FS.processImg(mapaOr0.get(3))).getAllPixels());
//		ret.setAllPixels(20, ((JIPBmpFloat)FS.processImg(mapaOr0.get(4))).getAllPixels());
//		ret.setAllPixels(21, ((JIPBmpFloat)FS.processImg(mapaOr0.get(5))).getAllPixels());
		return ret;
	}
	
	public Sequence processSeq(Sequence seq) throws JIPException
	{
		int count;
		Sequence res = new Sequence();
		JIPBmpColor inputImage = (JIPBmpColor) seq.getFrame(0);
		filter4 = getParamValueBool("4pass");
		ScaleNew FS = new ScaleNew();
		
		//create the multiresolution filters
		createFilters(inputImage);
		
		for (count=0; count<NUM_SCALES; count++)
		{
			res.addFrame(filterOr0.get(count));
		}
		for (count=0; count<NUM_SCALES; count++)
		{
			res.addFrame(filterOr45.get(count));
		}
		for (count=0; count<NUM_SCALES; count++)
		{
			res.addFrame(filterOr90.get(count));
		}
		for (count=0; count<NUM_SCALES; count++)
		{
			res.addFrame(filterOr135.get(count));
		}
		
		//create the feature maps
		createMaps();
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapIntensity.get(count));
//		}
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapColorRG.get(count));
//		}
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapColorBY.get(count));
//		}
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapOr0.get(count));
//		}
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapOr45.get(count));
//		}
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapOr90.get(count));
//		}
//		for (count=0;count<6;count++)
//		{
//			ret.addFrame(mapOr135.get(count));
//		}
		
		//build the conspicuty maps
		createConspicuity();
		res.addFrame(ic);
		res.addFrame(cc);
		res.addFrame(oc);
		
		//form the result
		normalizeRange0R(ic, 1);
		normalizeRange0R(cc, 1);
		normalizeRange0R(oc, 1);
		
		if (filter4)
		{
			normalizeData4pass(ic, M);
			normalizeData4pass(cc, M);
			normalizeData4pass(oc, M);
		}
		else
		{
			normalizeData(ic, M);
			normalizeData(cc, M);
			normalizeData(oc, M);
		}

		double []vi = ic.getAllPixels();
		double []vc = cc.getAllPixels();
		double []vo = oc.getAllPixels();
		double []vr = new double[vi.length];
		
		for (count=0; count<vi.length; count++)
		{
			vr[count] = (0.4*vi[count] + 0.4*vc[count] + 0.2*vo[count]);
		}
		
		JIPBmpFloat retImg = new JIPBmpFloat(ic.getWidth(), ic.getHeight());
		retImg.setAllPixels(vr);
		
		normalizeRange0R(retImg, 1);
		
		FS.setParamValue("Width", inputImage.getWidth());
		FS.setParamValue("Height", inputImage.getHeight());
		
		res.insertFrame(FS.processImg(retImg), 0);
		
		return res;
	}
	
	
	/**
	 * Method which creates filters.
	 * @param inputImage The input image.
	 * @throws JIPException
	 */
	private void createFilters(JIPBmpColor inputImage) throws JIPException 
	{
		double []r = inputImage.getAllPixelsRed();
		double []g = inputImage.getAllPixelsGreen();
		double []b = inputImage.getAllPixelsBlue();
		int totalLength = r.length;
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		int count;
		double y, root;
		double []I = new double[totalLength];
		double []R = new double[totalLength];
		double []G = new double[totalLength];
		double []B = new double[totalLength];
		double []Y = new double[totalLength];
		double iMax = 0;
		
		//Obtain the information about the intensity and bands R, G, B, Y
		for (count = 0; count<totalLength; count++)
		{
			I[count] = Math.round(0.299 * (r[count]) + 0.587 * (g[count]) + 0.114 * 
					  (b[count]));
			
			if (I[count] > iMax) iMax = I[count];
		}
		
		for (count = 0; count<totalLength; count++)
		{
			if (I[count] > iMax * 0.1)
			{
				y = Math.max(Math.min(r[count], g[count])-b[count], 0);
				root = Math.max(Math.max(r[count], g[count]), b[count]);
				Y[count] = y / root;
				R[count] = Math.max(r[count] - y - (g[count]-y+b[count])/2, 0) / root;
				G[count] = Math.max(g[count] - y - (r[count]-y+b[count])/2, 0) / root;
				B[count] = Math.max(b[count] - (g[count] + r[count])/2, 0) / root;
			}
			else
			{
				I[count] = R[count] = G[count] = B[count] = Y[count] = 0;
			}
			I[count] /= 255;
		}
		
		//intensity pyramid filters
		JIPBmpFloat imgAux = new JIPBmpFloat(width, height);
		imgAux.setAllPixels(I);
		filterIntensity = createPyramid(imgAux);
		
		//color pyramid filters. 
		//Started by obtaining the pyramids for each band.
		JIPBmpFloat imgR = new JIPBmpFloat(width, height);
		imgR.setAllPixels(R);
		filterRed = createPyramid(imgR);
		JIPBmpFloat imgG = new JIPBmpFloat(width, height);
		imgG.setAllPixels(G);
		filterGreen = createPyramid(imgG);
		JIPBmpFloat imgB = new JIPBmpFloat(width, height);
		imgB.setAllPixels(B);
		filterBlue = createPyramid(imgB);
		JIPBmpFloat imgY = new JIPBmpFloat(width, height);
		imgY.setAllPixels(Y);
		filterYellow = createPyramid(imgY);
		
		//nomralize the color filters.
		for (count = 2; count<NUM_SCALES; count++)
		{
			if (filter4)
			{
				normalizeData4pass(filterRed.get(count), M);
				normalizeData4pass(filterGreen.get(count), M);
				normalizeData4pass(filterBlue.get(count), M);
				normalizeData4pass(filterYellow.get(count), M);
			}
			else
			{
				normalizeData(filterRed.get(count), M);
				normalizeData(filterGreen.get(count), M);
				normalizeData(filterBlue.get(count), M);
				normalizeData(filterYellow.get(count), M);
			}
		}
		
		//Create the images with filters. Size 7x7.
		Gabor fg = new Gabor();
		fg.setParamValue("columns", 7);
		fg.setParamValue("rows", 7);
		fg.setParamValue("scale", 2f);
		fg.setParamValue("type", true);
		
		ConvolveImage fcon = new ConvolveImage();
		fcon.setParamValue("method", "PAD");
		JIPImage gabor0 = fg.processImg(imgAux); 
		fg.setParamValue("orientation", 45f);
		JIPImage gabor45 = fg.processImg(imgAux);
		fg.setParamValue("orientation", 90f);
		JIPImage gabor90 = fg.processImg(imgAux);
		fg.setParamValue("orientation", 135f);
		JIPImage gabor135 = fg.processImg(imgAux);
		
		fcon.setParamValue("image", gabor0);
		JIPBmpFloat imgAuxG0 = (JIPBmpFloat)fcon.processImg(filterIntensity.get(0));
		filterOr0 = createPyramid(imgAuxG0);

		fcon.setParamValue("image", gabor45);
		JIPBmpFloat imgAuxG45 = (JIPBmpFloat)fcon.processImg(filterIntensity.get(0));
		filterOr45 = createPyramid(imgAuxG45);

		fcon.setParamValue("image", gabor90);
		JIPBmpFloat imgAux90 = (JIPBmpFloat)fcon.processImg(filterIntensity.get(0));
		filterOr90 = createPyramid(imgAux90);

		fcon.setParamValue("image", gabor135);
		JIPBmpFloat imgAux135 = (JIPBmpFloat)fcon.processImg(filterIntensity.get(0));
		filterOr135 = createPyramid(imgAux135);
	}
	
	/**
	 * Method which creates the maps.
	 * @throws JIPException
	 */
	private void createMaps() throws JIPException
	{
		//Intensity maps (6 maps)
		mapIntensity = new ArrayList<JIPBmpFloat>();
		mapIntensity.add(subtractFilterAbs(filterIntensity.get(2), filterIntensity.get(5)));
		mapIntensity.add(subtractFilterAbs(filterIntensity.get(2), filterIntensity.get(6)));
		mapIntensity.add(subtractFilterAbs(filterIntensity.get(3), filterIntensity.get(6)));
		mapIntensity.add(subtractFilterAbs(filterIntensity.get(3), filterIntensity.get(7)));
		mapIntensity.add(subtractFilterAbs(filterIntensity.get(4), filterIntensity.get(7)));
		mapIntensity.add(subtractFilterAbs(filterIntensity.get(4), filterIntensity.get(8)));
		
		//Red-green map
		mapColorRG = new ArrayList<JIPBmpFloat>();
		JIPBmpFloat RG2 = subtractFilter(filterRed.get(2), filterGreen.get(2));
		JIPBmpFloat RG3 = subtractFilter(filterRed.get(3), filterGreen.get(3));
		JIPBmpFloat RG4 = subtractFilter(filterRed.get(4), filterGreen.get(4));
		JIPBmpFloat GR5 = subtractFilter(filterGreen.get(5), filterRed.get(5));
		JIPBmpFloat GR6 = subtractFilter(filterGreen.get(6), filterRed.get(6));
		JIPBmpFloat GR7 = subtractFilter(filterGreen.get(7), filterRed.get(7));
		JIPBmpFloat GR8 = subtractFilter(filterGreen.get(8), filterRed.get(8));
		
		mapColorRG.add(subtractFilterAbs(RG2, GR5));
		mapColorRG.add(subtractFilterAbs(RG2, GR6));
		mapColorRG.add(subtractFilterAbs(RG3, GR6));
		mapColorRG.add(subtractFilterAbs(RG3, GR7));
		mapColorRG.add(subtractFilterAbs(RG4, GR7));
		mapColorRG.add(subtractFilterAbs(RG4, GR8));
		
		//Blue-yellow map
		mapColorBY = new ArrayList<JIPBmpFloat>();
		JIPBmpFloat BY2 = subtractFilter(filterBlue.get(2), filterYellow.get(2));
		JIPBmpFloat BY3 = subtractFilter(filterBlue.get(3), filterYellow.get(3));
		JIPBmpFloat BY4 = subtractFilter(filterBlue.get(4), filterYellow.get(4));
		JIPBmpFloat YB5 = subtractFilter(filterYellow.get(5), filterBlue.get(5));
		JIPBmpFloat YB6 = subtractFilter(filterYellow.get(6), filterBlue.get(6));
		JIPBmpFloat YB7 = subtractFilter(filterYellow.get(7), filterBlue.get(7));
		JIPBmpFloat YB8 = subtractFilter(filterYellow.get(8), filterBlue.get(8));
		
		mapColorBY.add(subtractFilterAbs(BY2, YB5));
		mapColorBY.add(subtractFilterAbs(BY2, YB6));
		mapColorBY.add(subtractFilterAbs(BY3, YB6));
		mapColorBY.add(subtractFilterAbs(BY3, YB7));
		mapColorBY.add(subtractFilterAbs(BY4, YB7));
		mapColorBY.add(subtractFilterAbs(BY4, YB8));
		
		//orientation 0 map
		mapOr0 = new ArrayList<JIPBmpFloat>();
		mapOr0.add(subtractFilterAbs(filterOr0.get(2), filterOr0.get(5)));
		mapOr0.add(subtractFilterAbs(filterOr0.get(2), filterOr0.get(6)));
		mapOr0.add(subtractFilterAbs(filterOr0.get(3), filterOr0.get(6)));
		mapOr0.add(subtractFilterAbs(filterOr0.get(3), filterOr0.get(7)));
		mapOr0.add(subtractFilterAbs(filterOr0.get(4), filterOr0.get(7)));
		mapOr0.add(subtractFilterAbs(filterOr0.get(4), filterOr0.get(8)));
		
		//orientation 45 map
		mapOr45 = new ArrayList<JIPBmpFloat>();
		mapOr45.add(subtractFilterAbs(filterOr45.get(2), filterOr45.get(5)));
		mapOr45.add(subtractFilterAbs(filterOr45.get(2), filterOr45.get(6)));
		mapOr45.add(subtractFilterAbs(filterOr45.get(3), filterOr45.get(6)));
		mapOr45.add(subtractFilterAbs(filterOr45.get(3), filterOr45.get(7)));
		mapOr45.add(subtractFilterAbs(filterOr45.get(4), filterOr45.get(7)));
		mapOr45.add(subtractFilterAbs(filterOr45.get(4), filterOr45.get(8)));
		
		//orientation 90 map
		mapOr90 = new ArrayList<JIPBmpFloat>();
		mapOr90.add(subtractFilterAbs(filterOr90.get(2), filterOr90.get(5)));
		mapOr90.add(subtractFilterAbs(filterOr90.get(2), filterOr90.get(6)));
		mapOr90.add(subtractFilterAbs(filterOr90.get(3), filterOr90.get(6)));
		mapOr90.add(subtractFilterAbs(filterOr90.get(3), filterOr90.get(7)));
		mapOr90.add(subtractFilterAbs(filterOr90.get(4), filterOr90.get(7)));
		mapOr90.add(subtractFilterAbs(filterOr90.get(4), filterOr90.get(8)));
		
		//orientation 135 map
		mapOr135 = new ArrayList<JIPBmpFloat>();
		mapOr135.add(subtractFilterAbs(filterOr135.get(2), filterOr135.get(5)));
		mapOr135.add(subtractFilterAbs(filterOr135.get(2), filterOr135.get(6)));
		mapOr135.add(subtractFilterAbs(filterOr135.get(3), filterOr135.get(6)));
		mapOr135.add(subtractFilterAbs(filterOr135.get(3), filterOr135.get(7)));
		mapOr135.add(subtractFilterAbs(filterOr135.get(4), filterOr135.get(7)));
		mapOr135.add(subtractFilterAbs(filterOr135.get(4), filterOr135.get(8)));
	}
	
	/**
	 * Method which create a conspicuity (used for ic, cc, oc)
	 * @throws JIPException
	 */
	private void createConspicuity() throws JIPException
	{
		int count;
		ScaleNew fs = new ScaleNew();

		//Normalize maps
		for (count=0; count<6; count++)
		{
			if (filter4)
			{
				normalizeData4pass(mapIntensity.get(count), M);
				normalizeData4pass(mapColorRG.get(count), M);
				normalizeData4pass(mapColorBY.get(count), M);
				normalizeData4pass(mapOr0.get(count), M);
				normalizeData4pass(mapOr45.get(count), M);
				normalizeData4pass(mapOr90.get(count), M);
				normalizeData4pass(mapOr135.get(count), M);
			}
			else
			{
				normalizeData(mapIntensity.get(count), M);
				normalizeData(mapColorRG.get(count), M);
				normalizeData(mapColorBY.get(count), M);
				normalizeData(mapOr0.get(count), M);
				normalizeData(mapOr45.get(count), M);
				normalizeData(mapOr90.get(count), M);
				normalizeData(mapOr135.get(count), M);
			}
		}
		
		//To create the striking image, we add the maps in scale 4.
		//Filters are in scale 2, 2, 3, 3, 4, 4
		//Intensity
		ic = addMaps(mapIntensity.get(0), mapIntensity.get(1));
		fs.setParamValue("Width", ic.getWidth());
		fs.setParamValue("Height", ic.getHeight());
		ic = addMaps((JIPBmpFloat)fs.processImg(mapIntensity.get(2)), ic);
		ic = addMaps((JIPBmpFloat)fs.processImg(mapIntensity.get(3)), ic);
		ic = addMaps((JIPBmpFloat)fs.processImg(mapIntensity.get(4)), ic);
		ic = addMaps((JIPBmpFloat)fs.processImg(mapIntensity.get(5)), ic);
		
		//Warning: In the original code, it only substracts some of them. We make the same.
		//Color
		cc = addMaps(mapColorRG.get(0), mapColorRG.get(1));
		cc = addMaps(mapColorBY.get(0), cc);
		cc = addMaps(mapColorBY.get(1), cc);
		fs.setParamValue("Width", cc.getWidth());
		fs.setParamValue("Height", cc.getHeight());
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorRG.get(2)), cc);
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorBY.get(2)), cc);
		// The next does not appear in original code, but appear in Itti
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorRG.get(3)), cc);
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorBY.get(3)), cc);
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorRG.get(4)), cc);
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorBY.get(4)), cc);
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorRG.get(5)), cc);
		cc = addMaps((JIPBmpFloat)fs.processImg(mapColorBY.get(5)), cc);

		//Orientations
		oc = addMaps(mapOr0.get(0), mapOr0.get(1));
		fs.setParamValue("Width", oc.getWidth());
		fs.setParamValue("Height", oc.getHeight());
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr0.get(2)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr0.get(3)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr0.get(4)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr0.get(5)), oc);

		oc = addMaps(mapOr45.get(0), oc);
		oc = addMaps(mapOr45.get(1), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr45.get(2)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr45.get(3)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr45.get(4)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr45.get(5)), oc);
		
		oc = addMaps(mapOr90.get(0), oc);
		oc = addMaps(mapOr90.get(1), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr90.get(2)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr90.get(3)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr90.get(4)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr90.get(5)), oc);
		
		oc = addMaps(mapOr135.get(0), oc);
		oc = addMaps(mapOr135.get(1), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr135.get(2)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr135.get(3)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr135.get(4)), oc);
		oc = addMaps((JIPBmpFloat)fs.processImg(mapOr135.get(5)), oc);
	}
	
	/**
	 * Method which creates a pyramid for intensity, color and orientation
	 * @param img The input filter.
	 * @return An array which contains the pyramid data.
	 * @throws JIPException
	 */
	private ArrayList<JIPBmpFloat> createPyramid(JIPBmpFloat img) throws JIPException
	{
		ArrayList<JIPBmpFloat> ret = new ArrayList<JIPBmpFloat>();
		int count;
		SmoothGaussian fsg = new SmoothGaussian();
		fsg.setParamValue("method", "PAD");
		ScaleNew fs = new ScaleNew();
		fs.setParamValue("Fast", true);
		JIPBmpFloat softened;
		JIPBmpFloat scaling;
		
 		ret.add(img);
		scaling = img;
		
		for (count=1; count<NUM_SCALES; count++)
		{
			softened = (JIPBmpFloat)fsg.processImg(scaling);
			fs.setParamValue("Width", Math.max(softened.getWidth()/2,1));
			fs.setParamValue("Height", Math.max(softened.getHeight()/2,1));
			scaling = (JIPBmpFloat)fs.processImg(softened);
			ret.add(scaling);
		}
		
		return ret;
	}

	/**
	 * Method which subtracts two filters.
	 * @param f1 The first filter for subtract.
	 * @param f2 The second filter for subtract.
	 * @return The subtract of two filter.
	 * @throws JIPException
	 */
	private JIPBmpFloat subtractFilter(JIPBmpFloat f1, JIPBmpFloat f2) throws JIPException
	{
		double []data1;
		double []data2;
		int count;
		ScaleNew FS = new ScaleNew();
		
		if (f1.getWidth() < f2.getWidth()) 
			throw new JIPException("Incompatible filter size.");
		
		FS.setParamValue("Width", f1.getWidth());
		FS.setParamValue("Height", f1.getHeight());
		f2 = (JIPBmpFloat)FS.processImg(f2);
		data1 = f1.getAllPixels();
		data2 = f2.getAllPixels();
		
		for (count=0; count<data1.length; count++)
			data1[count] -= data2[count];
		
		JIPBmpFloat ret = new JIPBmpFloat(f1.getWidth(), f1.getHeight());
		ret.setAllPixels(data1);
		
		return ret;
	}

	/**
	 * Method which subtracts two filters in absolute.
	 * @param f1 The first filter for subtract.
	 * @param f2 The second filter for subtract.
	 * @return The subtract of two filter.
	 * @throws JIPException
	 */
	private JIPBmpFloat subtractFilterAbs(JIPBmpFloat f1, JIPBmpFloat f2) throws JIPException
	{
		double []data1;
		double []data2;
		int count;
		ScaleNew FS = new ScaleNew();
		
		if (f1.getWidth() < f2.getWidth()) 
			throw new JIPException("Incompatible filter size.");
		
		FS.setParamValue("Width", f1.getWidth());
		FS.setParamValue("Height", f1.getHeight());
		f2 = (JIPBmpFloat)FS.processImg(f2);
		data1 = f1.getAllPixels();
		data2 = f2.getAllPixels();
		
		for (count=0; count<data1.length; count++)
			data1[count] = Math.abs(data1[count] - data2[count]);
		
		JIPBmpFloat ret = new JIPBmpFloat(f1.getWidth(), f1.getHeight());
		ret.setAllPixels(data1);
		
		return ret;
	}
	
	/**
	 * Method which adds two maps.
	 * @param f1 The first map for adding.
	 * @param f2 The second map for adding.
	 * @return The sum of two maps.
	 * @throws JIPException
	 */
	private JIPBmpFloat addMaps(JIPBmpFloat f1, JIPBmpFloat f2) throws JIPException
	{
		JIPBmpFloat ret = new JIPBmpFloat(f1.getWidth(), f1.getHeight());
		double []data1 = f1.getAllPixels();
		double []data2 = f2.getAllPixels();
		int count;
		
		if (data1.length != data2.length) 
			throw new JIPException("Error in addMaps: The size of the maps must be equals.");
		
		for (count=0; count<data1.length; count++)
			data1[count] += data2[count];
		
		ret.setAllPixels(data1);
		return ret;
	}
	
	/**
	 * Method which checks if a real value is a local maximum.
	 * @param img The input image data.
	 * @param max The real value.
	 * @param i The position.
	 * @param width The image width.
	 * @param height The image height.
	 * @return Boolean indicating true if the number is a local maximum, false in otherwise.
	 */
	private boolean isLocalMaximum(double[] img, double max, int i, int width, int height) {
		int x = i%width;
		int y = i/width;
		double maxmin = 0.1;
		
		// Does not take into account the first and last rows/columns
		if (x == 0 && x == width-1 && y == 0 && y == height-1) 
			return true;
		
		if (x == 0)
		{
			if (x == width-1)
			{
				if (y == 0)
				{
					return (img[i] >= max*maxmin &&
							img[i] > img[x+(y+1)*width]);
				}
				else if (y == height-1)
				{
					return (img[i] >= max*maxmin &&
							img[i] > img[x+(y-1)*width]);
				}
				else
				{
					return (img[i] >= max*maxmin &&
							img[i] > img[x+(y-1)*width] &&
							img[i] > img[x+(y+1)*width]);
				}
			}
			if (y == 0)
			{
				if (y == height-1)
				{
					return (img[i] >= max * maxmin &&
							img[i] > img[x+1+y*width]);
				}
				else
				{
					return (img[i] >= max * maxmin &&
						img[i] > img[x+1+y*width] &&
						img[i] > img[x+1+(y+1)*width] &&
						img[i] > img[x+(y+1)*width]);
				}
			}
			else if (y == height-1)
			{
				return (img[i] >= max*maxmin &&
						img[i] > img[x+1+(y-1)*width] &&
						img[i] > img[x+1+y*width] &&
						img[i] > img[x+(y-1)*width]);
			}
			else
			{
				return (img[i] >= max*maxmin &&
						img[i] > img[x+1+(y-1)*width] &&
						img[i] > img[x+1+y*width] &&
						img[i] > img[x+1+(y+1)*width] &&
						img[i] > img[x+(y-1)*width] &&
						img[i] > img[x+(y+1)*width]);
			}
		}
		else if (x == width-1)
		{
			if (y == 0)
			{
				if (y == height-1)
				{
					return (img[i] >= max*maxmin &&
							img[i] > img[x-1+y*width]);
				}
				else
				{
					return (img[i] >= max*maxmin &&
						img[i] > img[x-1+y*width] &&
						img[i] > img[x-1+(y+1)*width] &&
						img[i] > img[x+(y+1)*width]);
				}
			}
			else if (y == height-1)
			{
				return (img[i] >= max*maxmin &&
						img[i] > img[x-1+(y-1)*width] &&
						img[i] > img[x-1+y*width] &&
						img[i] > img[x+(y-1)*width]);
			}
			else
			{
				return (img[i] >= max*maxmin &&
						img[i] > img[x-1+(y-1)*width] &&
						img[i] > img[x-1+y*width] &&
						img[i] > img[x-1+(y+1)*width] &&
						img[i] > img[x+(y-1)*width] &&
						img[i] > img[x+(y+1)*width]);
			}
		}
		else
		{
			if (y == 0)
			{
				if (y == height-1)
				{
					return (img[i] >= max*maxmin &&
							img[i] > img[x-1+y*width] &&
							img[i] > img[x+1+y*width]);
				}
				else
				{
					return (img[i] >= max*maxmin &&
						img[i] > img[x-1+y*width] &&
						img[i] > img[x-1+(y+1)*width] &&
						img[i] > img[x+1+y*width] &&
						img[i] > img[x+1+(y+1)*width] &&
						img[i] > img[x+(y+1)*width]);
				}
			}
			else if (y == height-1)
			{
				return (img[i] >= max*maxmin &&
						img[i] > img[x-1+(y-1)*width] &&
						img[i] > img[x-1+y*width] &&
						img[i] > img[x+1+(y-1)*width] &&
						img[i] > img[x+1+y*width] &&
						img[i] > img[x+(y-1)*width]);
			}
			else
			{
				return (img[i] >= max*maxmin &&
						img[i] > img[x-1+(y-1)*width] &&
						img[i] > img[x-1+y*width] &&
						img[i] > img[x-1+(y+1)*width] &&
						img[i] > img[x+1+(y-1)*width] &&
						img[i] > img[x+1+y*width] &&
						img[i] > img[x+1+(y+1)*width] &&
						img[i] > img[x+(y-1)*width] &&
						img[i] > img[x+(y+1)*width]);
			}
		}
	}

	/**
	 * Method which makes a standar normalization. (used in AIT (TIA) practices)
	 * @param img The input image.
	 * @param M The M.
	 * @throws JIPException
	 */
	private void normalizeData(JIPBmpFloat img, double M) throws JIPException
	{
		double []data = img.getAllPixels();
		double min = 256;
		double max = -256;
		int width = img.getWidth();
		int height = img.getHeight();
		int count;
		for (count=0; count<data.length; count++) 
		{
			if (data[count] < min) min = data[count];
			if (data[count] > max) max = data[count];
		}
		if (max > min)
		{
			max -= min;
			double scale = M / max;
			for (count=0; count<data.length; count++)
			{
				data[count] = (data[count] - min) * scale;
			}
		}
		
		//search local maxima
		double localMax = 0;
		int totalMax = 0;

		for (count=0; count<data.length; count++)
		{
			if (isLocalMaximum(data, M, count, width, height))
			{
				localMax += data[count];
				totalMax++;
			}
		}
		
		double weighting;
		
		// Drop the global maxima from the local maxima list.
		if (totalMax > 1)
		{
			localMax -= M;
			totalMax --;
			weighting = (M - localMax/totalMax)*(M - localMax/totalMax);
		}
		else weighting = M * M;
		
		for (count=0; count<data.length; count++) data[count] *= weighting;

		img.setAllPixels(data);
	}

	/**
	 * Method which normalize the information in 4-pass. The result is saved directly in the
	 * input image.
	 * @param img The input image.
	 * @param localM
	 * @throws JIPException
	 */
	private void normalizeData4pass(JIPBmpFloat img, double localM) throws JIPException
	{
		double []data = img.getAllPixels();
		double max = -256;
		int count;
		
		//search local maxima (75% of global maxima). 4-pass
		max = 0;
		double localMax = 0;
		int totalMax = 0;
		
		//Pass I: left -> right and up -> down
		for (int i=0; i<img.getHeight(); i++)
		{
			int row = i * img.getWidth();
			for (int j=0; j<img.getWidth();j++)
			{
				if (max < data[row + j]) max = data[row + j];
				if (data[row + j] > max * 0.75)
				{
					localMax += data[row + j];
					totalMax ++;
				}
			}
		}

		//Pass II: right -> left and down -> up
		max = 0;
		for (int i=img.getHeight()-1; i>=0; i--)
		{
			int row = i * img.getWidth();
			for (int j=img.getWidth()-1; j>=0; j--)
			{
				if (max < data[row + j]) max = data[row + j];
				if (data[row + j] > max * 0.75)
				{
					localMax += data[row + j];
					totalMax ++;
				}
			}
		}

		//Pass III: left -> right and down -> up
		max = 0;
		for (int i=0; i<img.getHeight(); i++)
		{
			int row = i * img.getWidth();
			for (int j=img.getWidth()-1; j>=0; j--)
			{
				if (max < data[row + j]) max = data[row + j];
				if (data[row + j] > max * 0.75)
				{
					localMax += data[row + j];
					totalMax ++;
				}
			}
		}

		//Pass IV: right -> left and up -> down
		max = 0;
		for (int i=img.getHeight()-1; i>=0; i--)
		{
			int row = i * img.getWidth();
			for (int j=0; j<img.getWidth(); j++)
			{
				if (max<data[row + j]) max = data[row + j];
				if (data[row + j] > max * 0.75)
				{
					localMax += data[row + j];
					totalMax ++;
				}
			}
		}
		
		double weighting = (max - localMax/totalMax)*(max - localMax/totalMax);
		
		// Drop the global maxima from the local maxima list.
		if (totalMax > 3)
		{
			localMax -= 4*max;
			totalMax = totalMax - 4;
		}
		
		// Finally, we also multiply the map globally with (max - localMedium) squared
		if (totalMax != 0)
			weighting = (max - localMax/totalMax) * (max - localMax/totalMax);
		else
			weighting = max * max;

		for (count=0; count<data.length; count++) data[count] *= weighting;

		img.setAllPixels(data);
	}
	
	/**
	 * Method which normalizes the range 0R.
	 * @param img The input image
	 * @param R The R.
	 * @throws JIPException
	 */
	private void normalizeRange0R(JIPBmpFloat img, double R) throws JIPException
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		int count;
		double scale;
		double []data = img.getAllPixels();
		
		//Calculate the maximum and the minimum
		for (count=0; count < data.length; count++)
		{
			if (data[count] < min) min = data[count];
			if (data[count] > max) max = data[count];
		}
		
		max -= min;
		scale = R / max;
		
		for (count=0; count < data.length; count++)
		{
			data[count] = (data[count] - min) * scale;
		}
		img.setAllPixels(data);
	}
}

