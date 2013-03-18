package javavis.jip2d.util;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Class to be used by HistogramWindow, its objective is can show the histogram
 * image in the Swing struct. Its use is:
 * JIPImage -> ImageIcon -> ScrollablePicture -> JScrollPane -> JPanel. 
*/
public class ScrollablePicture extends JLabel implements Scrollable {
	private static final long serialVersionUID = 356993258316280993L;
	/**
	 * Maximum unit of increment
	 * @uml.property  name="maxUnitIncrement"
	 */
	private int maxUnitIncrement = 1;

	/**
	 * Constructor of the class:
	 * @param ImageIcon i, This will be the image to show.
	 * @param int m, Maximum unit of incremet.s
	 */
	public ScrollablePicture(ImageIcon i, int m) {
		super(i);
		maxUnitIncrement = m;
	}

	/**
	 * It gets the dimension 
	 * @return Dimension
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
 	 * It gets the unit of increment to scroll in the image.
	 * @param  visibleRect visible area of the image.
	 * @param  orientacion Who is the orientation. 
	 * @param  direction	  Who is the direction.
	 * @return The new position in the image.
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL)
			currentPosition = visibleRect.x;
		else
			currentPosition = visibleRect.y;

		if (direction < 0) {
			int newPosition = currentPosition - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
			return (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement - currentPosition;
		}
	}

	/**
	 * It gets the block of increment to scroll in the image.
	 * @param  visibleRect visible area of the image.
	 * @param  orientacion Who is the orientation. 
	 * @param  direction	  Who is the direction.
	 * @return The new position in the image.
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL)
			return visibleRect.width - maxUnitIncrement;
		else
			return visibleRect.height - maxUnitIncrement;
	}


	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * It sets the maximum unit of increment.
	 * @param pixels  the maximum unit of increment.
	 * @uml.property  name="maxUnitIncrement"
	 */
	public void setMaxUnitIncrement(int pixels) {
		maxUnitIncrement = pixels;
	}
}
