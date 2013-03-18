package javavis.base;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
*   Class to create Warning, Information, Error and Confirmation Dialog windows
* */

public class Dialog {
	/**
	 * Component
	 * @uml.property  name="owner"
	 */
	Component owner;

	/**
	*   Class Constructor.
	* @param c Component caller
	*/
	public Dialog(Component c) {
		owner = c;
	}

	/**
	*  Shows a message in a new window.
	* @param message Information message
	* @param title Information window title
	*/
	public void information(String message, String title) {
		JOptionPane.showMessageDialog(owner, message, title,
			JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	* Shows a YES/NO dialog and returns the button pressed by the user.
	* @param message Message
	* @param title Window title
	*/
	public boolean confirm(String message, String title) {
		Object[] options = { "Yes", "No" };
		int aux=JOptionPane.showOptionDialog(owner, message, title,
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
			null, options, "Yes");
		return 0==aux;
	}
	
	/**
	 * Input dialog
	 * @param message Message to show
	 * @param title Window Title
	 * @return String with the input
	 */
	public String input(String message, String title) {
		return JOptionPane.showInputDialog(owner, message, title, 
				JOptionPane.QUESTION_MESSAGE);
	}
}
