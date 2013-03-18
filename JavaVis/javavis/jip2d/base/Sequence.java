package javavis.jip2d.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javavis.base.JIPException;

/**
 * This class defines the sequence object. A sequence is an ordered list of JIPImages. 
 * Image size and image type are not the same, necessarily, in all the images in the sequence. 
 * The number of frames in a sequence can change during its life even it can be 0 (empty sequence). 
 * The access to an image of a sequence is done with an integer  index which is between [0..numframes-1]. 
 * We can add, insert and delete images of a sequence and we can append another sequences to it.    
 * This object is used to store JavaVis images into files. 
 * Finally, a sequence has a name with default value of "[Unnamed]".
 */
public class Sequence implements Serializable {	
	private static final long serialVersionUID = -5825506384632041850L;

	/**
	 * Name of the sequence
	 * @uml.property  name="name"
	 */
	private String name;


	/**
	 * Vector which contains the frames of the sequence
	 * @uml.property  name="frames"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.jip2d.base.JIPImage"
	 */
	private ArrayList<JIPImage> frames;


	/**
	 * Sequence information
	 * @return String text with the description of the sequence. 
	 */
	public String toString() {
		String seq = "Sequence [" + name + "]/nNFrames: " + frames.size();
		for (JIPImage img : frames) {
			seq +=  "\nFrame [" + img.getName() + "]"
					+ "\n\tNPixels: " + img.getWidth()*img.getHeight() + "\n\tWidth: "
					+ img.getWidth() + "\n\tHeight: " + img.getHeight()
					+ "\n\tType: " + img.getType();
		}
		return (seq);
	}


	//******************//
	//  Constructors   //
	//******************//

	/**	 
	 * Empty sequence constructor.
	 */
	public Sequence() {
		name = "[Unnamed]";
		frames = new ArrayList<JIPImage>();
	}

	/**
	 * Sequence constructor with just an image, provided by parameter.	 
	 * @param img Image corresponding to sequence numFrame.
	 */
	public Sequence(JIPImage img) throws JIPException {
		this();
		if (img==null)
			throw new JIPException("Sequence: image parameter must not be null");
		frames.add(img);
	}

	/**	 
	 * Sequence copy constructor.
	 * @param seq Sequence to make a copy.
	 */
	public Sequence(Sequence seq) throws JIPException {
		this();
		if (seq != null) {
			name = seq.name;
			for (JIPImage img : seq.frames) 
				frames.add(img.clone());
		}
	}


	//*******************//
	//     METHODS       //
	//*******************//

	/**	 
	 * It gets the number of frames of sequence.
	 * @return Number of frames.
	 */
	public int getNumFrames() {
		return frames.size();
	}

	/**
	 * It gets the name of the sequence.
	 * @return  Name of the sequence.
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}

	/**
	 * It assigns the name of the sequence.
	 * @param nom  Name of the sequence.
	 * @uml.property  name="name"
	 */
	public void setName(String nom) {
		if (nom == null || nom.length() == 0)
			name = "[Unnamed]";
		else
			name = nom;
	}

	/**
	 * It returns the n image of the sequence.
	 * @param n Number of frames (0 <= n <= numframes-1)
	 * @return Image in the n poisition.
	 */
	public JIPImage getFrame(int n) throws JIPException {
		if (n < 0 || n >= frames.size())
			throw new JIPException("Sequence.getFrame: index out of bounds");
		return frames.get(n);
	}

	/**
	 * It sets an image into the sequence. It replaces the image in the indicated position
	 * @param img Image which will be set.
	 * @param n Position to set.
	 */
	public void setFrame(JIPImage img, int n) throws JIPException {
		if (img == null)
			throw new JIPException("Sequence.setFrame: null parameter");
		if (n < 0 || n >= frames.size()) 
			throw new JIPException("Sequence.setFrame: index out of bounds");
		frames.set(n, img);
	}

	/** 
	 * It adds an image at the end of the sequence.
	 * @param img Image to add.
	 */
	public void addFrame(JIPImage img) throws JIPException {
		if (img == null)
			throw new JIPException("Sequence.addFrame: null parameter");
		frames.add(img);
	}

	/**
	 * It inserts an image into the sequence. 
	 * @param img Image to insert.
	 * @param n Position to insert.
	 */
	public void insertFrame(JIPImage img, int n) throws JIPException {
		if (img == null)
			throw new JIPException("Sequence.insertFrame: null parameter");
		if (n < 0 || n >= frames.size()) 
			throw new JIPException("Sequence.insertFrame: index out of bounds");
		frames.add(n, img);
	}

	/**
	 * It deletes an image from the sequence.
	 * @param n Number of the image to remove (0 <= n <= numframes-1)
	 */
	public void removeFrame(int n) throws JIPException {
		if (n < 0 || n >= frames.size()) 
			throw new JIPException("Sequence.getFrame: index out of bounds");
		frames.remove(n);
	}

	/**	 
	 * It deletes all frames of the sequence 
	 */
	public void removeAllFrames() {
		frames.clear();
	}

	/**
	 * It appends a sequence at the end of the sequence.
	 * @param seq Sequence to append.
	 */
	public void appendSequence(Sequence seq) throws JIPException {
		if (seq == null)
			throw new JIPException("Sequence.appendSequence: null parameter");
		frames.addAll(seq.frames);
	}


	/**
	 * Returns the list of images
	 * @return  List of Images.
	 */
	public List<JIPImage> getFrames() {
		return frames;
	}


	/**
	 * @param frames  The frames to set.
	 */
	public void setFrames(ArrayList<JIPImage> frames) {
		this.frames = frames;
	}
}
