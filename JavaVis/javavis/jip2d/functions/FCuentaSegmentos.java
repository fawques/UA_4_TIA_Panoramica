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
public class FCuentaSegmentos extends Function2D {

	/**
	 * Tamaño de la ventana para la correlación
	 */
	int ventana;
	/**
	 * 
	 */
	private static final long serialVersionUID = 5457374739368893537L;

	/**
	 * Constructor de FPanorama. Establece tres parámetros: FNitzberg = umbral
	 * para la función FNitzberg. ventana = tamaño de ventana. Lambda = umbral
	 * de aceptación de correlación.
	 */
	public FCuentaSegmentos() {
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
		p2.setDefault(0.7f);
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
		percProgress = 0;
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

		int incrementoFrame = 100 / (frames - 1);
		int incrementoItem = incrementoFrame / 4;

		JIPImage panoramica = null;
		Point2D despl = new Point2D(0, 0);
		for (int i = 0; i < frames - 1; i++) {// recorremos desde el primero
												// hasta el penúltimo, y
												// accedemos siempre a uno y el
												// siguiente

			JIPGeomPoint nitzPrimera = (JIPGeomPoint) seq.getFrame(i);
			JIPGeomPoint nitzSegunda = (JIPGeomPoint) seq.getFrame(i + 1);

			JIPBmpByte primera = (JIPBmpByte) grises.getFrame(i);
			JIPBmpByte segunda = (JIPBmpByte) grises.getFrame(i + 1);
			ArrayList<Segment> desplazamientos = calcularDesplazamientos(
					nitzPrimera, nitzSegunda, primera, segunda);
			
		}

		return seq;
	}

	/**
	 * @param nitzPrimera
	 *            imagen con los puntos característicos de la primera imagen
	 * @param nitzSegunda
	 *            imagen con los puntos característicos de la segunda imagen
	 * @param primera
	 *            primera imagen en escala de grises
	 * @param segunda
	 *            segunda imagen en escala de grises
	 * @return ArrayList con los desplazamientos calculados en forma de
	 *         segmentos
	 * @throws JIPException
	 *             JIPException
	 */
	private ArrayList<Segment> calcularDesplazamientos(
			JIPGeomPoint nitzPrimera, JIPGeomPoint nitzSegunda,
			JIPBmpByte primera, JIPBmpByte segunda) throws JIPException {
		int aux = getParamValueInt("ventana");
		ArrayList<Segment> desplazamientos = new ArrayList<Segment>();
		for (int v = 0; v < aux; v++) {
			int totalCorrelaciones = 0;
			ventana = v;
			double divisor, mediaPrimera, mediaSegunda;
			

			ArrayList<Recorte> recortadosPrimera = getRecortes(nitzPrimera,
					primera);
			double divisorPrimera;
			ArrayList<Double> mediasPrimera = new ArrayList<Double>();
			ArrayList<Double> divisoresPrimera = new ArrayList<Double>();
			for (int i = 0; i < recortadosPrimera.size(); i++) {
				double media = calcularMedia((JIPBmpByte) recortadosPrimera
						.get(i).getRecorte());
				mediasPrimera.add(media);
				// System.out.println(media);
				divisorPrimera = calcularDivisor((JIPBmpByte) recortadosPrimera
						.get(i).getRecorte(), media);
				divisoresPrimera.add(divisorPrimera);
			}

			ArrayList<Recorte> recortadosSegunda = getRecortes(nitzSegunda,
					segunda);
			double divisorSegunda;
			ArrayList<Double> mediasSegunda = new ArrayList<Double>();
			ArrayList<Double> divisoresSegunda = new ArrayList<Double>();
			for (int i = 0; i < recortadosSegunda.size(); i++) {
				double media = calcularMedia((JIPBmpByte) recortadosSegunda
						.get(i).getRecorte());
				mediasSegunda.add(media);
				// System.out.println(media);
				divisorSegunda = calcularDivisor((JIPBmpByte) recortadosSegunda
						.get(i).getRecorte(), media);
				divisoresSegunda.add(divisorSegunda);
			}

			// ArrayList<Double> resultados = new ArrayList<Double>();
			// para cada recorte de la primera imagen
			for (int i = 0; i < recortadosPrimera.size(); i++) {
				PriorityQueue<Correlacion> correlaciones = new PriorityQueue<FCuentaSegmentos.Correlacion>();
				mediaPrimera = mediasPrimera.get(i);
				Recorte recortePrimera = recortadosPrimera.get(i);
				double imagen1[] = ((JIPBmpByte) recortePrimera.getRecorte())
						.getAllPixels();
				// para cada recorte de la segunda imagen
				for (int j = 0; j < recortadosSegunda.size(); j++) {
					divisor = divisoresPrimera.get(i) * divisoresSegunda.get(j);

					mediaSegunda = mediasSegunda.get(j);

					Recorte recorteSegunda = recortadosSegunda.get(j);
					Correlacion nueva = new Correlacion();
					nueva.setPrimerPunto(recortePrimera.getPunto());
					nueva.setSegundoPunto(recorteSegunda.getPunto());
					double acumulado = 0;

					double imagen2[] = ((JIPBmpByte) recorteSegunda
							.getRecorte()).getAllPixels();

					for (int k = 0; k < imagen1.length; k++) {
						acumulado += (imagen1[k] - mediaPrimera)
								* (imagen2[k] - mediaSegunda);
					}
					double res = acumulado / divisor;
					nueva.setCorrelacion(res);
					correlaciones.add(nueva);
					// resultados.add(res);
					// System.out.println("[" + i + ", " + j + "] " + res);
				}
				totalCorrelaciones += correlaciones.size();
				Correlacion CC1 = correlaciones.poll();
				Correlacion CC2 = correlaciones.poll();
				
				if (CC1 != null
						&& CC2 != null
						&& CC1.getCorrelacion() * getParamValueFloat("Lambda") > CC2
								.getCorrelacion()) {
					desplazamientos.add(new Segment(CC1.getPrimerPunto(), CC1
							.getSegundoPunto()));
				}

			}
			System.out.println(totalCorrelaciones + ", " + desplazamientos.size());
		}

		return desplazamientos;
	}

	/**
	 * @param nitz
	 *            imagen con los puntos calculados por Nitzberg.
	 * @param imagen
	 *            imagen en escala de grises
	 * @return ArrayList con un recorte para cada punto válido
	 * @throws JIPException
	 *             JIPException
	 */
	private ArrayList<Recorte> getRecortes(JIPGeomPoint nitz, JIPBmpByte imagen)
			throws JIPException {
		ArrayList<Recorte> recortados = new ArrayList<FCuentaSegmentos.Recorte>();
		for (int j = 0; j < nitz.getLength(); j++) {
			Recorte nuevo = new Recorte();
			Point2D punto = nitz.getPoint(j);

			int anchoPrimera = imagen.getWidth();
			int altoPrimera = imagen.getHeight();

			// calculamos la ventana...

			int filaInicioPrimera = punto.getY() - ventana;
			if (filaInicioPrimera < 0) {
				filaInicioPrimera = 0;
				continue;
			}
			/*
			 * int filaFinPrimera = puntoPrimera.getX()+ventana; if
			 * (filaFinPrimera >= primera.getHeight()) { filaFinPrimera =
			 * primera.getHeight()-1; }
			 */

			int columnaInicioPrimera = punto.getX() - ventana;
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
				if (columnaInicioPrimera + (tamanoVentana) > anchoPrimera)
					continue;
				recorte.setParamValue("w", tamanoVentana);
				if (filaInicioPrimera + (tamanoVentana) > altoPrimera)
					continue;
				recorte.setParamValue("h", tamanoVentana);
				JIPImage ventanaRecortada = recorte.processImg(imagen);
				nuevo.setPunto(punto);
				nuevo.setRecorte(ventanaRecortada);
				recortados.add(nuevo);
			} catch (JIPException e) {
				JIPException aux = new JIPException("ERROR RECORTE - j = " + j
						+ " - filaInicio = " + filaInicioPrimera
						+ " - columnaInicio = " + columnaInicioPrimera + "- "
						+ e.getMessage());
				throw aux;
			}
		}
		return recortados;
	}

	/**
	 * @param imagen
	 *            ventana de la que calcular la media
	 * @return media calculada
	 * @throws JIPException
	 *             JIPException
	 */
	private float calcularMedia(JIPBmpByte imagen) throws JIPException {
		float media;
		double bytes[] = imagen.getAllPixels();
		int tamanoTotalVentana = imagen.getHeight() * imagen.getWidth();
		long acumulado = 0;
		for (int i = 0; i < imagen.getHeight(); i++) {
			for (int l = 0; l < imagen.getWidth(); l++) {
				acumulado += bytes[i * imagen.getWidth() + l];
				// System.out.println("["+i+","+l+"]="+bytes[i*imagen.getWidth()
				// + l]);
			}
		}
		media = (float) acumulado / (float) tamanoTotalVentana;
		return media;
	}

	/**
	 * @param imagen
	 *            ventana de la que calcular el divisor
	 * @param media
	 *            media de la ventana
	 * @return divisor para utilizar en la correlación
	 * @throws JIPException
	 *             JIPException
	 */
	private double calcularDivisor(JIPBmpByte imagen, double media)
			throws JIPException {
		double bytes[] = imagen.getAllPixels();
		double acumulado = 0;
		for (int i = 0; i < bytes.length; i++) {
			double aux = bytes[i] - media;
			acumulado += aux * aux;
		}
		return Math.sqrt(acumulado);
	}

	/**
	 * @author Víctor Clase Correlacion. Guarda dos puntos (uno de cada imagen)
	 *         y la correlación entre ellos
	 */
	class Correlacion implements Comparable<Correlacion> {
		private Point2D primerPunto, segundoPunto;
		private double correlacion;

		public double getCorrelacion() {
			return correlacion;
		}

		public Point2D getPrimerPunto() {
			return primerPunto;
		}

		public Point2D getSegundoPunto() {
			return segundoPunto;
		}

		public void setCorrelacion(double corr) {
			correlacion = corr;
		}

		public void setPrimerPunto(Point2D punto) {
			primerPunto = punto;
		}

		public void setSegundoPunto(Point2D punto) {
			segundoPunto = punto;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object) Establece una
		 * ordenación de mayor a menor correlación
		 */
		@Override
		public int compareTo(Correlacion o) {
			if (o.getCorrelacion() > correlacion)
				return 1;
			else if (o.getCorrelacion() < correlacion)
				return -1;
			else
				return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "[" + primerPunto + "]->[" + segundoPunto + "]"
					+ correlacion;
		}

	}

	/**
	 * @author Víctor Clase Recorte. Guarda un punto central y la imagen
	 *         recortada a su alrededor.
	 */
	class Recorte {
		private Point2D punto;
		private JIPImage recorte;

		public Point2D getPunto() {
			return punto;
		}

		public JIPImage getRecorte() {
			return recorte;
		}

		public void setPunto(Point2D punto) {
			this.punto = punto;
		}

		public void setRecorte(JIPImage recorte) {
			this.recorte = recorte;
		}

	}

	/**
	 * @author Víctor Clase SegmentComparator. Establece un orden de comparación
	 *         de segmentos, ordenados por X y después por Y.
	 */
	public class segmentComparator implements Comparator<Segment> {

		@Override
		public int compare(Segment arg0, Segment arg1) {
			Point2D origen0 = arg0.getBegin();
			Point2D origen1 = arg1.getBegin();

			Point2D destino0 = arg0.getEnd();
			Point2D destino1 = arg1.getEnd();

			int desplX0 = destino0.getX() - origen0.getX();
			int desplY0 = destino0.getY() - origen0.getY();

			int desplX1 = destino1.getX() - origen1.getX();
			int desplY1 = destino1.getY() - origen1.getY();

			if (desplX0 > desplX1)
				return 1;
			else if (desplX0 == desplX1) {
				if (desplY0 > desplY1) {
					return 1;
				} else if (desplY0 < desplY1) {
					return -1;
				} else
					return 0;
			} else
				return -1;
		}

	}

}
