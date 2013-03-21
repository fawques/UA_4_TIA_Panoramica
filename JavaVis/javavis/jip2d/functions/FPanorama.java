/**
 * 
 */
package javavis.jip2d.functions;

import java.util.ArrayList;

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
		p1.setDefault(1);
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
		ventana = getParamValueInt("ventana");
		double divisor, mediaPrimera, mediaSegunda;

		Sequence recortadosPrimera = getRecortes(nitzPrimera, primera);
		double divisorPrimera;
		ArrayList<Double> mediasPrimera = new ArrayList<Double>();
		ArrayList<Double> divisoresPrimera = new ArrayList<Double>();
		for (int i = 0; i < recortadosPrimera.getNumFrames(); i++) {
			double media = calcularMedia((JIPBmpByte)recortadosPrimera.getFrame(i));
			mediasPrimera.add(media);
			//System.out.println(media);
			divisorPrimera = calcularDivisor((JIPBmpByte)recortadosPrimera.getFrame(i),media);
			divisoresPrimera.add(divisorPrimera);
		}
		
		Sequence recortadosSegunda = getRecortes(nitzSegunda, segunda);
		double divisorSegunda;
		ArrayList<Double> mediasSegunda = new ArrayList<Double>();
		ArrayList<Double> divisoresSegunda = new ArrayList<Double>();
		for (int i = 0; i < recortadosSegunda.getNumFrames(); i++) {
			double media = calcularMedia((JIPBmpByte)recortadosSegunda.getFrame(i));
			mediasSegunda.add(media);
			//System.out.println(media);
			divisorSegunda = calcularDivisor((JIPBmpByte)recortadosSegunda.getFrame(i),media);
			divisoresSegunda.add(divisorSegunda);
		}
		
		ArrayList<Double> resultados = new ArrayList<Double>();
		// para cada recorte de la primera imagen
		for (int i = 0; i < 1/*divisoresPrimera.size()*/; i++) {
			// para cada recorte de la segunda imagen
			for (int j = 0; j < 1/*divisoresSegunda.size()*/; j++) {
				divisor = divisoresPrimera.get(i)* divisoresSegunda.get(j);
				mediaPrimera = mediasPrimera.get(i);
				mediaSegunda = mediasSegunda.get(j);
				System.out.println("media primera = " + mediaPrimera + " - media segunda = " + mediaSegunda);
				double acumulado = 0;
				double imagen1[] = ((JIPBmpByte) recortadosPrimera.getFrame(i)).getAllPixels(); 
				double imagen2[] = ((JIPBmpByte) recortadosSegunda.getFrame(j)).getAllPixels();
				
				for (int k = 0; k < imagen1.length; k++) {
					acumulado += imagen1[k] - mediaPrimera * imagen2[k] - mediaSegunda; 
				}
				
				for (int k = 0; k < imagen1.length; k++) {
					System.out.print("[" + imagen1[k] + "]"); 
				}
				System.out.println();
				
				for (int k = 0; k < imagen2.length; k++) {
					System.out.print("[" + imagen2[k] + "]"); 
				}
				System.out.println();
				
				System.out.println("acumulado numerador = " + acumulado);
				System.out.println("denominador primera parte = " + divisoresPrimera.get(i));
				System.out.println("denominador segunda parte = " + divisoresSegunda.get(j));
				System.out.println("acumulado denominador = " + divisor);
				double res = acumulado / divisor;
				System.out.println("resultado = " + res);
				resultados.add(res);
				System.out.println("[" + i + ", " + j + "] " + res);
				
			}
			
		}
		recortadosPrimera.appendSequence(recortadosSegunda);
		return recortadosPrimera;
	}

	/**
	 * @param nitz
	 * @param imagen
	 * @param ventana
	 * @param mediaPrimera
	 * @param recortados
	 * @return
	 * @throws JIPException
	 */
	private Sequence getRecortes(JIPGeomPoint nitz, JIPBmpByte imagen)
			throws JIPException {
		Sequence recortados = new Sequence();
		for (int j = 0; j < nitz.getLength(); j++) {
			Point2D puntoPrimera = nitz.getPoint(j);

			int anchoPrimera = imagen.getWidth();
			int altoPrimera = imagen.getHeight();

			// calculamos la ventana...

			int filaInicioPrimera = puntoPrimera.getY() - ventana;
			if (filaInicioPrimera < 0) {
				filaInicioPrimera = 0;
				continue;
			}
			/*
			 * int filaFinPrimera = puntoPrimera.getX()+ventana; if
			 * (filaFinPrimera >= primera.getHeight()) { filaFinPrimera =
			 * primera.getHeight()-1; }
			 */

			int columnaInicioPrimera = puntoPrimera.getX() - ventana;
			if (columnaInicioPrimera < 0) {
				columnaInicioPrimera = 0;
				continue;
			}
			/*
			 * int columnaFinPrimera = puntoPrimera.getY()+ventana; if
			 * (filaFinPrimera >= primera.getWidth()) { filaFinPrimera =
			 * primera.getWidth()-1; }
			 */

			int tamanoVentana = ventana * 2 + 1;
			try {
				Crop recorte = new Crop();
				recorte.setParamValue("y", filaInicioPrimera);
				recorte.setParamValue("x", columnaInicioPrimera);
				if(columnaInicioPrimera + (tamanoVentana)> anchoPrimera)
					continue;
				recorte.setParamValue("w",  tamanoVentana);
				if(filaInicioPrimera + (tamanoVentana)> altoPrimera)
					continue;
				recorte.setParamValue("h", tamanoVentana);
				JIPImage ventanaRecortada = recorte.processImg(imagen);
				recortados.addFrame(ventanaRecortada);
			} catch (JIPException e) {
				JIPException aux = new JIPException("ERROR RECORTE - j = " + j + " - filaInicio = " + filaInicioPrimera + " - columnaInicio = " + columnaInicioPrimera + "- "
						+ e.getMessage());
				throw aux;
			}
		}
		return recortados;
	}

	/**
	 * @param imagen
	 * @param ancho
	 * @param filaInicioPrimera
	 * @param columnaInicioPrimera
	 * @param tamanoVentana
	 * @throws JIPException 
	 */
	private float calcularMedia(JIPBmpByte imagen) throws JIPException {
		float media;
		double bytes[] = imagen.getAllPixels();
		int tamanoTotalVentana = imagen.getHeight()*imagen.getWidth();
		long acumulado = 0; 
		for (int i =0; i < imagen.getHeight(); i++) {
			for (int l = 0; l < imagen.getWidth(); l++) {
				acumulado += bytes[i*imagen.getWidth() + l]; 
				//System.out.println("["+i+","+l+"]="+bytes[i*imagen.getWidth() + l]);
			} 
		} 
		media = (float)acumulado/(float)tamanoTotalVentana;
		return media;
	}

	/**
	 * @param imagen
	 * @param ancho
	 * @param filaInicioPrimera
	 * @param columnaInicioPrimera
	 * @param tamanoVentana
	 * @throws JIPException 
	 */
	private double calcularDivisor(JIPBmpByte imagen, double media) throws JIPException {
		double bytes[] = imagen.getAllPixels();
		double acumulado = 0;
		for (int i = 0; i < bytes.length; i++) {
			double aux = bytes[i] - media;
			acumulado += aux*aux;
		}System.out.println();
		return Math.sqrt(acumulado);
	}
	
}
