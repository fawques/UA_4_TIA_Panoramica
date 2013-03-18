package javavis.base;

/**
 * Enum to identify image types
 */
public enum ImageType {
	/**
	 * Const that identify a binary image.
	 * Pixel values are integers between [0..1]
	 */
	BIT,

	/**
	 * Const that identify a 256 grayscale image.
	 * Pixel values are integers between [0..255]
	 */
	BYTE,

	/**
	 * Const that identify a 65536 grayscale image.
	 * Pixel values are integers between [0..65535]
	 */
	SHORT,

	/**
	 * Const that identify a grayscale image.
	 * Pixel values are floats between [0..1]
	 */
	FLOAT,

	/**
	 * Const that identify a color image (3 bands RGB).
	 * Pixel values are integers between [0..255] for each band.
	 */
	COLOR,
	
	/**
	 * Const that identify an image with Segment type.
	 */
	SEGMENT,
	
	/**
	 * Const that identify an image with Point type.
	 */
	POINT, 
	/**
	 * Const that identify an image with Polygon2D type.
	 */
	POLY,
	
	/**
	 * Const that identify an image with edges type
	 */
	EDGES,
	
	/**
	 * Const that identify an image with junctions type
	 */
	JUNCTION
}
