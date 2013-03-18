package javavis.jip2d.functions;

import java.util.ArrayList;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.base.parameter.ParamBool;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPImgBitmap;
import javavis.jip2d.base.geometrics.Edge;
import javavis.jip2d.base.geometrics.JIPImgGeometric;
import javavis.jip2d.base.geometrics.Junction;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Polygon2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * It scales an image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>width</em>: Integer value which indicates the new width for the scaled image (default 320).</li>
 * <li><em>height</em>: Integer value which indicates the new height for the scaled image (default 200).</li>
 * <li><em>fast</em>: Boolean value which indicates the scale type. If it is checked, scale is applied using a 
 * poor quality and high speed subsampling method. If it is not checked, a good quality 
 * subsampling method is used (default unchecked).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The scaled image.</li>
 * </ul><br />
 */
public class Scale extends Function2D {
	private static final long serialVersionUID = -323208875505182117L;

	public Scale() {
		super();
		name = "Scale";
		description = "Scales an image.";
		groupFunc = FunctionGroup.Manipulation;

		ParamInt p1 = new ParamInt("Width", false, true);
		p1.setDescription("New image width");
		p1.setDefault(320);

		ParamInt p2 = new ParamInt("Height", false, true);
		p2.setDescription("New image height");
		p2.setDefault(200);
		
		ParamBool p3 = new ParamBool("Fast", false, true);
		p3.setDescription("Fast method (poor quality)");
		p3.setDefault(false);
		
		addParam(p1);
		addParam(p2);
		addParam(p3);
	}

	@SuppressWarnings("unchecked")
	public JIPImage processImg(JIPImage img) throws JIPException {
		boolean method = getParamValueBool("Fast");
		boolean action; 
		int width = getParamValueInt("Width");
		int height = getParamValueInt("Height");

		int w = img.getWidth();
		int h = img.getHeight();
		ImageType t = img.getType();

		if (width > w)
		{
			if (height >= h) action = true;
			else throw new JIPException("It can not combine expand/reduce in X Y axis");
		}
		else if (width < w)
		{
			if (height <= h) action = false;
			else throw new JIPException("It can not combine expand/reduce in X Y axis");
		}
		else
		{
			if (height > h) action = true;
			else if (height < h) action = false;
			else return img;
		}

		double Xstep = w / (double)width;
		double Ystep = h / (double)height;

		if (img instanceof JIPImgBitmap) {
			JIPImage res = null;
			if (action) res = expand(method, (JIPImgBitmap)img, width, height);
			else res = reduce(method, (JIPImgBitmap)img, width, height, Xstep, Ystep);
			return res;
		} else { 
			double xmul = width / w;
			double ymul = height / h;
			JIPImgGeometric res = (JIPImgGeometric)JIPImage.newImage(width, height, t);
			switch (t) {
				case POINT: ArrayList<Point2D> listpoints = (ArrayList<Point2D>)(((JIPImgGeometric)img).getData());
							ArrayList<Point2D> pointres = new ArrayList<Point2D>();
							for (Point2D p : listpoints) {
								Point2D paux = new Point2D((int)(p.getX()*xmul), (int)(p.getY()*ymul));
								pointres.add(paux);
							}
							res.setData(pointres);
							break;
				case SEGMENT: ArrayList<Segment> listsegment = (ArrayList<Segment>)(((JIPImgGeometric)img).getData());
							ArrayList<Segment> segmentres = new ArrayList<Segment>();
							for (Segment s : listsegment) {
								Point2D p = s.getBegin();
								Point2D begin = new Point2D((int)(p.getX()*xmul), (int)(p.getY()*ymul));
								p = s.getEnd();
								Point2D end = new Point2D((int)(p.getX()*xmul), (int)(p.getY()*ymul));
								Segment s1 = new Segment(begin, end);
								segmentres.add(s1);
							}
							res.setData(segmentres);
							break;
				case JUNCTION: ArrayList<Junction> listjunc = (ArrayList<Junction>)(((JIPImgGeometric)img).getData());
							ArrayList<Junction> juncres = new ArrayList<Junction>();
							for (Junction j : listjunc) {
								Junction j1 = new Junction((int)(j.getX()*xmul), (int)(j.getY()*ymul), j.getR_i(), j.getR_e(), j.getSituation());
								juncres.add(j1);
							}
							res.setData(juncres);
							break;
				case POLY: ArrayList<Polygon2D> listpolys = (ArrayList<Polygon2D>)(((JIPImgGeometric)img).getData());
							ArrayList<Polygon2D> polyres = new ArrayList<Polygon2D>();
							for (Polygon2D pol : listpolys) {
								ArrayList<Point2D> points = pol.getData();
								ArrayList<Point2D> listp = new ArrayList<Point2D>();
								for (Point2D p : points) {
									Point2D paux = new Point2D((int)(p.getX()*xmul), (int)(p.getY()*ymul));
									listp.add(paux);
								}
								Polygon2D polr = new Polygon2D(listp);
								polyres.add(polr);
							}
							res.setData(polyres);
							break;
				case EDGES: ArrayList<Edge> listEdges = (ArrayList<Edge>)(((JIPImgGeometric)img).getData());
							ArrayList<Edge> edgeres = new ArrayList<Edge>();
							for (Edge edg : listEdges) {
								ArrayList<Point2D> points = edg.getData();
								ArrayList<Point2D> listp = new ArrayList<Point2D>();
								ArrayList<Float> values = edg.getValues();
								for (Point2D p : points) {
									Point2D paux = new Point2D((int)(p.getX()*xmul), (int)(p.getY()*ymul));
									listp.add(paux);
								}
								Edge polr = new Edge(listp, values);
								edgeres.add(polr);
							}
							res.setData(edgeres);
							break;
			}
			return res;
		}
	}
	
	
	/**
	 * Method to reduce images.
	 * @param method: The reduction method
	 * @param img: Frame to work.
	 * @param width: Image width.
	 * @param height: Image height.
	 * @param Xstep: Size of the area in function of FE for Xs.
	 * @param Ystep: Size of the area in function of FE for Ys.
	 * @return image reduced
	 * @throws JIPException
	 */
	private JIPImage reduce(boolean method, JIPImgBitmap img, int width, int height, 
			double Xstep, double Ystep) throws JIPException {
		int numBands = img.getNumBands();
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, img.getType());
		int denom, xs, ys;
		float average;

		for (int nb=0; nb < numBands; nb++)
			for (int h=0; h < height; h++)
				for (int w=0; w < width; w++) {
					xs = (int) (w * Xstep);
					ys = (int) (h * Ystep);
					if (!method) //good quality
					{ 
						average = 0.0f;
						denom = 0;
						for (int x=xs; x < xs + Xstep - 1; x++)
							for (int y=ys; y < ys + Ystep - 1; y++) {
								average += img.getPixel(nb, x, y);
								denom++;
							}
						if (denom == 0)
							res.setPixel(nb, w, h, img.getPixel(nb, xs, ys));
						else 
							res.setPixel(nb, w, h, average / denom);
					} else //faster
						res.setPixel(nb, w, h, img.getPixel(nb, xs, ys));
				}
		return res;
	}

	/**
	 * Method used for expanding an image. Two algorithms are used:
	 * Accurate method							OR			Fast Method
	 * |ab| -> |a++++b|  where '+' positions         		|aaabbb| if fast method 
	 * |cd| -> |++++++|  are interpolated to    OR   		|aaabbb| is applied
	 * 		   |++++++|  a, b, c and d values		 		|cccddd|
	 * 	 	   |c++++d|								 		|cccddd|
	 * @param method If true, fast method is applied (not recommended)
	 * @param img Input image
	 * @param width Width for the final image
	 * @param height Height for the final image
	 * @return image expanded
	 * @throws JIPException
	 */
	private JIPImage expand(boolean method, JIPImgBitmap img, int width, int height) throws JIPException
	{
		//TODO hacer esta propiedad una constante de la clase
		int moduleFactor = 1000;
		int numBands = img.getNumBands();
		int w = img.getWidth();
		int h = img.getHeight();
		double strideX, strideY;
		int b, i, j;
		double []pixels = new double[width * height];
		int row, rowOy;
		double []origin;
		JIPImgBitmap res = (JIPImgBitmap)JIPImage.newImage(numBands, width, height, img.getType());
		double distOriginX, distOriginY; //distance along X,Y axis to the 'previous' pixel
		double distNextX, distNextY; //distance along X,Y axis to the 'next' pixel
		int Ox, Oy; // corresponding coordinates in the source image for the 'previous' pixel
		int nextX, nextY; // corresponding coordinates in the source image for the 'next' pixel
		double D1, D2, D3, D4, totalD; //distances
		double pa, pb, pc, pd;
		double diffX, diffY, diffNextX, diffNextY;
		
		if (method)
		{
			strideX = (double)width / w;
			strideY = (double)height / h;
			for (b=0; b<numBands; b++)
			{
				origin = img.getAllPixels(b);
				for(i=0;i<height;i++)
				{
					row = i * width;
					Oy = (int)(i / strideY);
					rowOy = Oy * w;
					for(j=0;j<width;j++)
					{
						pixels[row + j] = origin[rowOy + (int)(j/strideX)];
					}
				}
				res.setAllPixels(b, pixels);
			}
			return res;
		}
		else
		{
			if (w == 1)
				strideX = width;
			else
				strideX = (double)(width-1) / (w-1);
			if (h == 1)
				strideY = height;
			else
				strideY = (double)(height-1) / (h-1);

			for(b=0; b<numBands; b++)
			{
				origin = img.getAllPixels(b);
				Oy = -1;
				for (i=0; i<height; i++)
				{
					distOriginY = (int)( ((moduleFactor*i)%(moduleFactor*strideY)) / moduleFactor);
					distNextY = strideY - distOriginY;
					diffY = strideY - distOriginY;
					diffNextY = strideY - distNextY;
					row = i*width;
					if (distOriginY==0) Oy++;
					rowOy = Oy * w;
					nextY = Oy+1;
					
					Ox = -1;
					for (j=0; j<width; j++)
					{
						distOriginX = (int)(((moduleFactor*j)%(moduleFactor*strideX)) / moduleFactor);
						distNextX = strideX - distOriginX;
						diffX = strideX - distOriginX;
						diffNextX = strideX - distNextX;
						if(distOriginX == 0) 
							Ox++;
						
						nextX = Ox + 1;
						pa = origin[rowOy + Ox];
						D1 = diffX*diffX + diffY*diffY;
						totalD = D1;
						if (nextX < w)
						{
							pb = origin[rowOy + nextX];
							D2 = diffNextX*diffNextX + diffY*diffY;
							totalD += D2;
						} 
						else {
							pb = 0;
							D2 = 0;
						}
						
						if(nextY < h) {
							pc = origin[rowOy + w + Ox];
							D3 = diffX*diffX + diffNextY*diffNextY;
							totalD += D3;
						} 
						else {
							pc = 0;
							D3 = 0;
						}
						
						if(nextY < h && nextX < w) {
							pd = origin[rowOy + w + nextX];
							D4 = diffNextX*diffNextX + diffNextY*diffNextY;
							totalD += D4;
						}
						else {
							pd = 0;
							D4 = 0;
						}
						pixels[row + j] = (D1*pa + D2*pb + D3*pc + D4*pd) / totalD;
					} // for cols
				} // for rows
				res.setAllPixels(b, pixels);
			} //for numBands
			return res;
		}
	}
}

