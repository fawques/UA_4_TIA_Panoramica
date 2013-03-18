package javavis.jip2d.util;

/**
 * Class used to calculate the histogram of the REAL images. This type of images
 * have a pixel intensity between [0..1], In a infinite intensity set is required to
 * calculate this class, that give us an intensity with its instances number per value.
 */
public class RealDataHistogram {

	/**
	 * Intensity in pixel
	 * @uml.property  name="intensity"
	 */
	private float intensity;
	/**
	 * Number of instances
	 * @uml.property  name="nOcurrences"
	 */
	private int nOcurrences;

	/**
	 * Constructor of the class. It starts the intensity and the number of instances to -1.
	 */
	public RealDataHistogram() {
		this.nOcurrences = -1;
		this.intensity = -1;
	}

	/**
	 * Method to get the intensity value.
	 * @return Intensity value.
	 */
	public float getIValue() {
		return intensity;
	}

	/**
	 * Method to get the instances value.
	 * @return Number of instances.
	 */
	public int getOValue() {
		return nOcurrences;
	}

	/**
	 * Method to assign same value to the intensity.
	 * @param Intensity value.
	 */
	public void setIValue(float in) {
		this.intensity = in;
	}

	/**
	 * Method to assign value to the instances.
	 * @param Number of instances.
	 */
	public void setOValue(int nOcu) {
		this.nOcurrences = nOcu;
	}

}
