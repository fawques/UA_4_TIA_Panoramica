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

import javavis.jip2d.base.geometrics.JIPImgGeometric;

/**
 * It scales an image.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a bitmap image.</li>
 * <li><em>width</em>: Integer value which indicates the new width for the scaled image (default 320).</li>
 * <li><em>height</em>: Integer value which indicates the new height for the scaled image (default 200).</li>
 * <li><em>fast</em>: Boolean value which indicates the scale type. If it is checked, scale 
 * is applied using a poor quality and high speed subsampling method. If it is not checked, a 
 * good quality subsampling method is used (default unchecked).</li>
 * </ul><br />
 * <strong>Image result:</strong><br />
 * <ul>
 * <li>The scaled image.</li>
 * </ul><br />
 */
public class ScaleNew extends Function2D {
	private static final long serialVersionUID = -5899079536131549673L;

	public ScaleNew() {
		name = "ScaleNew";
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
	@Override
	public JIPImage processImg(JIPImage img) throws JIPException {
		boolean method = getParamValueBool("Fast");
		boolean action; 
		int width = getParamValueInt("Width");
		int height = getParamValueInt("Height");

		JIPImage res = null;
		int w = img.getWidth();
		int h = img.getHeight();
		ImageType t = img.getType();

		if (width > w)
		{
			if(height>=h) action = true;
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

		if (img instanceof JIPImgBitmap ) {
//			if(action) res = extend(method, (JIPImgBitmap)img, p6, p7, Xstep, Ystep);
			if(action) res = expand(method, (JIPImgBitmap)img, width, height);
			else res = reduce(method, (JIPImgBitmap)img, width, height, Xstep, Ystep);
			return res;
		} else { 
			double xmul = width / w;
			double ymul = height / h;
			if (t == ImageType.POINT || t == ImageType.SEGMENT) {
				ArrayList<Integer> points = new ArrayList<Integer>(((JIPImgGeometric)img).getData());
				for (int i = 0; i < points.size(); i++) {
					if (i % 2 != 0)
						points.set((int)(ymul * points.get(i)), i);
					else 
						points.set((int)(xmul * points.get(i)), i);
				}
				res = JIPImage.newImage((int)width, (int)height, t);
				((JIPImgGeometric)res).setData(points);
			} else if (t == ImageType.POLY || t == ImageType.EDGES) {
				ArrayList<ArrayList<Integer>> polygons = 
					new ArrayList<ArrayList<Integer>>(((JIPImgGeometric)img).getData());
				
				for (int j = 0; j < polygons.size(); j++) {
					int size = polygons.get(j).size();
					ArrayList<Integer> auxVec = polygons.get(j);
					for (int i = 0; i < size; i++) {
						if (i % 2 != 0)
							auxVec.set(i, (int) (ymul * auxVec.get(i)));
						else
							auxVec.set(i, (int) (xmul * auxVec.get(i)));
					}
				}
				res = JIPImage.newImage((int)width, (int)height, t);
				((JIPImgGeometric)res).setData(polygons);
			}
			return res;
		}
	}
	
	
	/**
	 * Method to reduce images.
	 * @param method The reduction method.
	 * @param img Frame to work.
	 * @param width Image width.
	 * @param height Image height.
	 * @param Xstep Size of the area in function of FE for Xs.
	 * @param Ystep Size of the area in function of FE for Ys.
	 * @return Image reduced.
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
	 * |ab| -> |a++++b|  where '+' positions         |aaabbb| if fast method 
	 * |cd| -> |++++++|  are interpolated to    OR   |aaabbb| is applied
	 * 		   |++++++|  a, b, c and d values		 |cccddd|
	 * 	 	   |c++++d|								 |cccddd|
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
		
		if (method)
		{
			strideX = (double)width / w;
			strideY = (double)height / h;
			for(b=0; b<numBands; b++)
			{
				origin = img.getAllPixels(b);
				for (i=0;i<height;i++)
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

			for (b=0; b<numBands; b++)
			{
				origin = img.getAllPixels(b);
				Oy = -1;
				for (i=0; i<height; i++)
				{
					distOriginY = (int)( ((moduleFactor*i)%(moduleFactor*strideY)) / moduleFactor);
					distNextY = strideY - distOriginY;
					row = i*width;
//					Oy = (int)(i / strideY);
					if (distOriginY==0) Oy++;
					rowOy = Oy * w;
					nextY = Oy+1;
					
					Ox = -1;
					for (j=0; j<width; j++)
					{
						distOriginX = (int)(((moduleFactor*j)%(moduleFactor*strideX)) / moduleFactor);
						distNextX = strideX - distOriginX;
												
						if(distOriginX == 0) 
							Ox++;
						
						nextX = Ox + 1;
						pa = origin[rowOy + Ox];
						D1 = (strideX - distOriginX)*(strideX - distOriginX) + (strideY - distOriginY)*(strideY - distOriginY);
						totalD = D1;
						
						if(nextX < w)
						{
							pb = origin[rowOy + nextX];
							D2 = (strideX - distNextX)*(strideX - distNextX) + (strideY - distOriginY)*(strideY - distOriginY);
							totalD += D2;
						}
						else {
							pb = 0;
							D2 = 0;
						}
						
						if(nextY < h) {
							pc = origin[rowOy + w + Ox];
							D3 = (strideX - distOriginX)*(strideX - distOriginX) + (strideY - distNextY)*(strideY - distNextY);
							totalD += D3;
						} 
						else {
							pc = 0;
							D3 = 0;
						}
						
						if(nextY < h && nextX < w) {
							pd = origin[rowOy + w + nextX];
							D4 = (strideX - distNextX)*(strideX - distNextX) + (strideY - distNextY)*(strideY - distNextY);
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

