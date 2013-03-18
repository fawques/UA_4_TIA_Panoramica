/**
 * 
 */
package javavis.jip2d.functions;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.Point2D;

/**
 * @author EPS
 * 
 */
public class FPanorama extends Function2D {

	int ventana;
	/**
	 * 
	 */
	private static final long serialVersionUID = 5457374739368893537L;

	/**
	 * 
	 */
	public FPanorama() {
		name = "Panorámica";
		description = "Crea una panorámica a partir de una secuencia";
		groupFunc = FunctionGroup.Applic;

		ParamFloat p0 = new ParamFloat("FNitzberg", false, true);
		p0.setDescription("Threshold para Nitzberg");
		p0.setDefault(50.0f);
		addParam(p0);

		ParamInt p1 = new ParamInt("ventana", false, true);
		p1.setDescription("Ventana para la correlación cruzada");
		p1.setDefault(5);
		addParam(p1);

		ParamFloat p2 = new ParamFloat("Lambda", false, true);
		p2.setDescription("Margen de error para considerar un punto equivalente");
		p2.setDefault(0.5f);
		addParam(p2);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javavis.jip2d.base.Function2D#processImg(javavis.jip2d.base.JIPImage)
	 */
	@Override
	public JIPImage processImg(JIPImage img) throws JIPException {
		throw new JIPException(
				"La función debe aplicarse sobre una secuencia, no una imagen");
	}

	@Override
	public Sequence processSeq(Sequence seq) throws JIPException {
		int frames = seq.getNumFrames();
		Sequence original = new Sequence(seq);
		Sequence grises;
		if (frames < 2) {
			throw new JIPException(
					"La secuencia debe tener al menos dos imágenes");
		}

		ColorToGray ctg = new ColorToGray();
		ctg.setParamValue("gray", "BYTE");
		Nitzberg nitz = new Nitzberg();
		nitz.setParamValue("thres", getParamValueFloat("FNitzberg"));

		grises = ctg.processSeq(seq);
		seq = nitz.processSeq(grises);

		for (int i = 0; i < frames - 1; i++) {// recorremos desde el primero
												// hasta el penúltimo, y
												// accedemos siempre a uno y el
												// siguiente
			JIPGeomPoint nitzPrimera = (JIPGeomPoint) seq.getFrame(i);
			JIPGeomPoint nitzSegunda = (JIPGeomPoint) seq.getFrame(i + 1);

			JIPBmpByte primera = (JIPBmpByte) grises.getFrame(i);
			JIPBmpByte segunda = (JIPBmpByte) grises.getFrame(i + 1);

			// TODO: borrar esto, es para probar
			seq.appendSequence(recorrerPuntos(nitzPrimera, nitzSegunda,
					primera, segunda));
		}

		return seq;
	}

	/**
	 * @param nitzPrimera
	 * @param nitzSegunda
	 * @param primera
	 *            TODO
	 * @param segunda
	 *            TODO
	 * @throws JIPException
	 */
	private Sequence recorrerPuntos(JIPGeomPoint nitzPrimera,
			JIPGeomPoint nitzSegunda, JIPBmpByte primera, JIPBmpByte segunda)
			throws JIPException {
		int ventana = getParamValueInt("ventana");

		float mediaPrimera = 0, mediaSegunda = 0;

		Sequence recortados = new Sequence();
		for (int j = 0; j < 500/*nitzPrimera.getLength()*/; j++) {
			Point2D puntoPrimera = nitzPrimera.getPoint(j);

			double bytesPrimera[] = primera.getAllPixels();
			int anchoPrimera = primera.getWidth();
			int altoPrimera = primera.getHeight();

			// calculamos la ventana...

			int filaInicioPrimera = puntoPrimera.getX() - ventana;
			if (filaInicioPrimera < 0) {
				filaInicioPrimera = 0;
			}
			/*
			 * int filaFinPrimera = puntoPrimera.getX()+ventana; if
			 * (filaFinPrimera >= primera.getHeight()) { filaFinPrimera =
			 * primera.getHeight()-1; }
			 */

			int columnaInicioPrimera = puntoPrimera.getY() - ventana;
			if (filaInicioPrimera < 0) {
				filaInicioPrimera = 0;
			}
			/*
			 * int columnaFinPrimera = puntoPrimera.getY()+ventana; if
			 * (filaFinPrimera >= primera.getWidth()) { filaFinPrimera =
			 * primera.getWidth()-1; }
			 */

			try {
				Crop recorte = new Crop();
				recorte.setParamValue("y", filaInicioPrimera);
				recorte.setParamValue("x", columnaInicioPrimera);
				recorte.setParamValue("w",  ventana * 2 + 1);
				recorte.setParamValue("h", ventana * 2 + 1);
				JIPImage ventanaRecortada = recorte.processImg(primera);
				recortados.addFrame(ventanaRecortada);
			} catch (JIPException e) {
				JIPException aux = new JIPException("ERROR RECORTE - "
						+ e.getMessage());
				throw aux;
			}
			/*
			 * // calculamos la media de la ventana int tamano =
			 * (2*ventana+1)*(2*ventana+1); long acumulado = 0; for (int i =
			 * filaInicioPrimera; i < filaFinPrimera; i++) { for (int l =
			 * columnaInicioPrimera; l < columnaFinPrimera; l++) { acumulado +=
			 * bytesPrimera[i*anchoPrimera + j]; } } mediaPrimera =
			 * acumulado/tamano; System.out.println(mediaPrimera);
			 * 
			 * for (int k = 0; k < nitzSegunda.getLength(); k++) { Point2D
			 * puntoSegunda = nitzPrimera.getPoint(j);
			 * 
			 * 
			 * 
			 * }
			 */
		}
		return recortados;
	}

	/*
	 * int flongomongo = getParamValueInt("nombre de parámetro");
	 * System.out.println(flongomongo);
	 * 
	 * 
	 * int h,w;
	 * 
	 * 
	 * JIPBmpByte img_gris = (JIPBmpByte) ctg.processImg(img);
	 * 
	 * h = img_gris.getHeight(); w = img_gris.getWidth();
	 * 
	 * for (int i = 0; i < w/2; i++) { for (int j = 0; j < h/2; j++) {
	 * img_gris.setPixel(i, j, img_gris.getPixel(i, j)+flongomongo); } }
	 * 
	 * double[] vectorImagen = img_gris.getAllPixels(); for (int i = h/2*w; i <
	 * vectorImagen.length; i++) { vectorImagen[i] -= flongomongo; }
	 * 
	 * img_gris.setAllPixels(vectorImagen);
	 * 
	 * return img_gris;
	 */

}
