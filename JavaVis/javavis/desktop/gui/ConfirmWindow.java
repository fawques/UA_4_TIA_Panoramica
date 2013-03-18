
package javavis.desktop.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author      Miguel Cazorla
 * @version      0.1
 * @date      5-2006
 */
public class ConfirmWindow extends JDialog implements ActionListener {
	private static final long serialVersionUID = -6944110598872733847L;

	//dimensions
	/**
	 * @uml.property  name="width"
	 */
	private int width=250;
	/**
	 * @uml.property  name="height"
	 */
	private int height=140;
	private static final int btWidth=70;
	private static final int btHeight=20;
	
	/**
	 * @uml.property  name="butYes"
	 */
	public boolean butYes = false ;
	/**
	 * @uml.property  name="butNo"
	 */
	public boolean butNo = false ;
	/**
	 * @uml.property  name="butCancel"
	 */
	public boolean butCancel = false ;
	
	//design
	/**
	 * @uml.property  name="contentPane"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JPanel contentPane=new JPanel();
	/**
	 * @uml.property  name="img"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JLabel img=new JLabel();
	/**
	 * @uml.property  name="label1"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	public JLabel label1=new JLabel();
	/**
	 * @uml.property  name="btYes"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton btYes=new JButton();
	/**
	 * @uml.property  name="btNo"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton btNo=new JButton();
	/**
	 * @uml.property  name="btCancel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton btCancel=new JButton();
	
	/**
	 * GuiDesktop
	 * @uml.property  name="mw"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private GuiDesktop mw;
	
	/**
	 * Object constructor
	 * 
	 * @param w - root numFrame
	 */
	public ConfirmWindow(JFrame mf, GuiDesktop mw) {
		super(mf);
		this.setTitle("Save project");
		this.setResizable(false);
		this.mw=mw;
		int newX=(int)((Toolkit.getDefaultToolkit().getScreenSize().getWidth()-this.width)/2);
		int newY=(int)((Toolkit.getDefaultToolkit().getScreenSize().getHeight()-this.height)/2)-100;

		this.setBounds(new Rectangle(newX,newY,this.width,this.height));
		
		this.contentPane.setLayout(null);
		this.contentPane.setPreferredSize(new Dimension(this.width,this.height));
		
		this.img.setBounds(new Rectangle(20,20,32,32));
		
		this.label1.setText("Save changes?");
		this.label1.setBounds(new Rectangle(75,-15,200,100));
		this.btYes.setText("Yes");
		this.btYes.setBounds(new Rectangle(7,70,ConfirmWindow.btWidth,ConfirmWindow.btHeight));
		this.btNo.setText("No");
		this.btNo.setBounds(new Rectangle(87,70,ConfirmWindow.btWidth,ConfirmWindow.btHeight));
		this.btCancel.setText("Cancel");
		this.btCancel.setBounds(new Rectangle(167,70,ConfirmWindow.btWidth,ConfirmWindow.btHeight));
		
		this.btYes.addActionListener(this);
		this.btNo.addActionListener(this);
		this.btCancel.addActionListener(this);
		
		//this.contentPane.add(this.img);
		this.contentPane.add(this.label1);
		this.contentPane.add(this.btYes);
		this.contentPane.add(this.btNo);
		this.contentPane.add(this.btCancel);
		this.getContentPane().add(contentPane);
	}

	/**
	 * @return   the butCancel
	 * @uml.property  name="butCancel"
	 */
	public boolean getButCancel()
	{
		return butCancel;
	}
	
	/**
	 * @return   the butYes
	 * @uml.property  name="butYes"
	 */
	public boolean getButYes()
	{
		return butYes;
	}
	
	/**
	 * @return   the butNo
	 * @uml.property  name="butNo"
	 */
	public boolean getButNo()
	{
		return butNo;
	}
	
	/**
	 * action performed
	 */
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource()==this.btCancel) {
			butCancel = true;
			this.setVisible(false);
		} else if(arg0.getSource()==this.btYes) {
			this.mw.btSave_action();
			butYes = true;
			this.setVisible(false);
		} else if(arg0.getSource()==this.btNo) {
			butNo = true;
			this.setVisible(false);
		} 	
	}
	

}
