package javavis.jip2d.functions;

import javavis.base.ImageType; 
import javavis.base.JIPException;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.bitmaps.JIPBmpBit;

/**
 * It calculates the skeleton of the binary input image.<br />
 * It applies to BIT type.<br /><br />
 * <strong>Input parameters:</strong><br />
 * <ul>
 * <li><em>img</em>: The input image. It has to be a BIT type.<br />
 * </ul><br />
 * <strong>Image result</strong><br />
 * <ul>
 * <li>A binary image with the skeleton of the input image.</li>
 * </ul><br />
 */
public class Skeleton extends Function2D {
	private static final long serialVersionUID = 6263008513202276002L;

	public Skeleton() {
		super();
		name = "Skeleton";
		description = "Obtains the skeleton from a binary image.";
		groupFunc = FunctionGroup.Others;
	}
	
	public JIPImage processImg(JIPImage img) throws JIPException {
		if (img.getType() != ImageType.BIT)
			throw new JIPException("Image format must be BIT.");

		JIPBmpBit imgBit = (JIPBmpBit)img;
		int width = img.getWidth();
		int height = img.getHeight();
		int numBands = imgBit.getNumBands();
		JIPBmpBit res = (JIPBmpBit)JIPImage.newImage(numBands, width, height, ImageType.BIT);
		boolean[] o = new boolean[8];
		boolean[] bmp, destination;
		boolean changes, localChanges;

		for(int nb=0; nb < numBands; nb++){
			bmp = imgBit.getAllPixelsBool(nb);
			destination = (boolean[]) bmp.clone();
			
			do{
				changes = false;
				//B4
				localChanges = false;
				for (int row=1; row < height-1; row++)
					for (int col=1; col < width-1; col++) 
						if (bmp[row*width + col ]) {
							getMask(bmp,width,height,row,col,o);
							boolean B4 = o[0] && (o[1] || o[2] || o[6] || o[7]) && (o[2] || !(o[3])) && (!(o[5]) || o[6]) && !o[4];
							if (B4) {
								localChanges = true;
								changes = true;
								destination[row*width + col] = false;
							}
						}
				if(localChanges) bmp = (boolean[]) destination.clone();

				//B0
				localChanges = false;
				for (int row=1; row < height - 1; row++)
					for (int col=1; col < width - 1; col++) 
						if (bmp[row*width + col ]) {
							getMask(bmp,width,height,row,col,o);
							boolean B0 = o[4] && (o[2] || o[3] || o[5] || o[6]) && (o[6] || !(o[7])) && (!o[1] || o[2]) && !o[0];
							if (B0) {
								localChanges = true;
								changes = true;
								destination[row*width + col] = false;
							}
						}
				if (localChanges) bmp = (boolean[]) destination.clone();

				//B2
				localChanges = false;
				for (int row=1; row < height - 1; row++)
					for (int col=1; col < width - 1; col++) 
						if (bmp[row*width + col ]) {
							getMask(bmp,width,height,row,col,o);
							boolean B2 = o[6] && (o[0] || o[4] || o[5] || o[7]) && (o[0] || !o[1]) && (!o[3] || o[4]) && !o[2];
							if (B2) {
								localChanges = true;
								changes = true;
								destination[row*width + col] = false;
							}
						}
				if (localChanges) bmp = (boolean[]) destination.clone();
				
				//B6
				localChanges = false;
				for (int row=1; row < height-1; row++)
					for (int col=1; col < width-1; col++)
						if (bmp[row*width + col ]) {
							getMask(bmp,width,height,row,col,o);
							boolean B6 = o[2] && (o[0] || o[1] || o[3] || o[4]) && (o[4] || !o[5]) && (o[0] || !o[7]) && !o[6];
							if (B6) {
								localChanges = true;
								changes = true;
								destination[row*width + col] = false;
							}
						}
				if (localChanges) bmp = (boolean[]) destination.clone();
			} while (changes);
			res.setAllPixelsBool(nb, destination);
		}
		
		return res;
	}
	
	
	/**
	 * Method which gets the mask.
	 * @param bmp The image data.
	 * @param width The image width.
	 * @param height The image height.
	 * @param row The current row.
	 * @param col The current column.
	 * @param mask The new mask.
	 */
	public void getMask(boolean [] bmp, int width, int height, int row, int col, boolean []mask){
		mask[0] = bmp[(row)*width + (col + 1)];
		mask[1] = bmp[(row - 1)*width + (col + 1)];
		mask[2] = bmp[(row - 1)*width + (col)];
		mask[3] = bmp[(row - 1)*width + (col - 1)];
		mask[4] = bmp[(row)*width + (col - 1)];
		mask[5] = bmp[(row + 1)*width + (col - 1)];
		mask[6] = bmp[(row + 1)*width + (col)];
		mask[7] = bmp[(row + 1)*width + (col + 1)];
		return;
	}
}

