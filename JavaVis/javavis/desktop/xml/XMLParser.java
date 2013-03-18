package javavis.desktop.xml;

import java.awt.Point;

import javavis.base.JIPException;
import javavis.desktop.gui.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

/**
 * @author     Miguel
 */
public class XMLParser implements ContentHandler {
	/**
	 * @uml.property  name="window"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	GuiDesktop window;
	
	private static Logger logger = Logger.getLogger(XMLParser.class);
	
	/**
	 * @uml.property  name="functionIndex"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javavis.desktop.gui.DrawFunction"
	 */
	private int functionIndex;
	
	/**
	 * Constructor del miContentHandler que recibe el nodo raiz del arbol que hay que rellenar.
	 * y crea los ArrayList.
	 * 
	 * @param meta
	 */	
	public XMLParser(GuiDesktop w) {
		window = w;
		functionIndex = 0;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String arg0, String arg1)
		throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String arg0) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localname,
		String qName, Attributes atributos)
	
		throws SAXException {
		if(localname.equals("function")) {
			functionIndex++;
			Function f = new Function(atributos.getValue("name"),window.functionsList);
			Point p = new Point(Integer.parseInt(atributos.getValue("positionx")),Integer.parseInt(atributos.getValue("positiony")));
			
			DrawFunction df = new DrawFunction(window,f,p);
			
			window.functionsList.add(df);
			window.mainContentPane.add(df);
		   	df.show();
		   	df.pack();
		}
		else if (localname.equals("param")) {	
			DrawFunction df = (DrawFunction)window.functionsList.get(functionIndex-1);
			Function f = df.func;
			String paramName = atributos.getValue("name");
			String paramType = atributos.getValue("type");
			
			try {
				if (paramType.equals("real"))
					f.jipfunction.setParamValue(paramName,Float.parseFloat(atributos.getValue("value")));
				else if (paramType.equals("int"))
					f.jipfunction.setParamValue(paramName,Integer.parseInt(atributos.getValue("value")));
				else if (paramType.equals("bool"))
					f.jipfunction.setParamValue(paramName,Boolean.getBoolean(atributos.getValue("value")));
				else if (paramType.equals("string") || paramType.equals("list") 
						|| paramType.equals("dir") || paramType.equals("file"))
					f.jipfunction.setParamValue(paramName,atributos.getValue("value"));
			} catch (JIPException e) {logger.error(e);}
		}
	}


	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceUri, String localname, String qName)
		throws SAXException{
		
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] cadena, int inicio, int longitud)
		throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
		throws SAXException {
		//System.out.println("Ignorable WHITE space.");
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String arg0, String arg1)
		throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String arg0) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator arg0) {
		
	}

}
