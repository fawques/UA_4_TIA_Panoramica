/**
 * 
 */
package javavis.jip2d.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.base.parameter.ParamInt;
import javavis.jip2d.base.Function2D;
import javavis.jip2d.base.FunctionGroup;
import javavis.jip2d.base.JIPImage;
import javavis.jip2d.base.Sequence;
import javavis.jip2d.base.bitmaps.JIPBmpByte;
import javavis.jip2d.base.bitmaps.JIPBmpColor;
import javavis.jip2d.base.geometrics.JIPGeomPoint;
import javavis.jip2d.base.geometrics.JIPGeomSegment;
import javavis.jip2d.base.geometrics.Point2D;
import javavis.jip2d.base.geometrics.Segment;

/**
 * @author EPS
 * 
 */
public class FCuentaNitz extends Function2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1079160732527893776L;

	/**
	 * Constructor de FPanorama. Establece tres parámetros: FNitzberg = umbral
	 * para la función FNitzberg. ventana = tamaño de ventana. Lambda = umbral
	 * de aceptación de correlación.
	 */
	public FCuentaNitz() {
		name = "CuentaNitzberg";
		description = "Muestra por consola la cantidad de puntos generados con distintos umbrales";
		groupFunc = FunctionGroup.Applic;

		ParamFloat p0 = new ParamFloat("FNitzberg", false, true);
		p0.setDescription("Threshold para Nitzberg");
		p0.setDefault(500.0f);
		addParam(p0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javavis.jip2d.base.Function2D#processImg(javavis.jip2d.base.JIPImage)
	 */
	@Override
	public JIPImage processImg(JIPImage img) throws JIPException {
		Nitzberg nitz = new Nitzberg();
		float param = getParamValueFloat("FNitzberg");
		for (int i = 0; i < param; i++) {
			nitz.setParamValue("thres", (float)i);

			JIPGeomPoint aux;
			aux = (JIPGeomPoint) nitz.processImg(img);
			
			System.out.println(aux.getLength());
		}
		return img;
	}

	@Override
	public Sequence processSeq(Sequence seq) throws JIPException {

		return seq;
	}
}