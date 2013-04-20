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
			ArrayList<Segment> desplazamientos = recorrerPuntos(nitzPrimera,
					nitzSegunda, primera, segunda);
			Collections.sort(desplazamientos, new segmentComparator());
			JIPBmpByte previsualizacion = new JIPBmpByte(Math.max(
					primera.getWidth(), segunda.getWidth()),
					primera.getHeight() + segunda.getHeight());
			double[] pixelesPrimera = primera.getAllPixels();
			double[] pixelesSegunda = segunda.getAllPixels();
			double[] pixelesPrev = new double[pixelesPrimera.length
					+ pixelesSegunda.length];
			System.arraycopy(pixelesPrimera, 0, pixelesPrev, 0,
					pixelesPrimera.length);
			System.arraycopy(pixelesSegunda, 0, pixelesPrev,
					pixelesPrimera.length, pixelesSegunda.length);
			previsualizacion.setAllPixels(pixelesPrev);
			previsualizacion.setName("Base_Segmentos_it_"+i);
			seq.addFrame(previsualizacion);
			JIPGeomSegment previsualizacionSegmentos = new JIPGeomSegment(
					previsualizacion.getWidth(), previsualizacion.getHeight());

			
			if (desplazamientos.size() >= 2) { // necesito al menos dos desplazamientos para poder calcular
				Point2D aux = calcularDesplazamiento(desplazamientos);
				despl.setX(despl.getX() + aux.getX());
				despl.setY(despl.getY() + aux.getY());

				pintarSegmentos(seq, primera, desplazamientos,
						previsualizacionSegmentos);

				if(panoramica == null)
					panoramica = original.getFrame(i);

				panoramica = crearPanoramica(
						(JIPBmpColor) panoramica,
						(JIPBmpColor) original.getFrame(i + 1), despl.getX(),
						despl.getY());

				panoramica = RecorteFinal(panoramica, despl.getX(),
						despl.getY(), 
						original.getFrame(i).getWidth(), 
						original.getFrame(i).getHeight(),
						original.getFrame(i + 1).getWidth(),
						original.getFrame(i + 1).getHeight());
				panoramica.setName("Panoramica_it_" + i);
				seq.addFrame(panoramica);
			} else {
				throw new JIPException(
						"No se han encontrado suficientes coincidencias para crear la panorámica. Modifica los parámetros o introduce otras imágenes");
			}
		}
		for(int i = 0; i < original.getNumFrames();i++) {
			original.getFrame(i).setName("Original_"+i);
		}
		seq.appendSequence(original);

		return seq;
	}

	private JIPImage RecorteFinal(JIPImage panoramica, int desplX, int desplY,
			int ancho1, int alto1, int ancho2, int alto2) {

		int anchofinal,altofinal;
		if (desplX >= 0 && desplY >= 0) {
			anchofinal = Math.max(desplX+ancho2, ancho1);
			altofinal = Math.max(desplY+alto2, alto1);
		} else if (desplX >= 0) { // desplY < 0
			anchofinal = Math.max(desplX+ancho2, ancho1);
			altofinal = Math.max(-desplY+alto1, alto2);
		} else if (desplY >= 0) { // desplX < 0
			anchofinal = Math.max(-desplX+ancho1, ancho2);
			altofinal = Math.max(desplY+alto2, alto1);
		} else {
			anchofinal = Math.max(-desplX+ancho1, ancho2);
			altofinal = Math.max(-desplY+alto1, alto2);
		}
		
		
		JIPImage panoramicaFinal = null;
		try {
			Crop recorte = new Crop();
			recorte.setParamValue("y", 0);
			recorte.setParamValue("x", 0);
			recorte.setParamValue("w", anchofinal);
			recorte.setParamValue("h", altofinal);
			panoramicaFinal = recorte.processImg(panoramica);
		} catch (JIPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return panoramicaFinal;
	}

	/**
	 * @param seq
	 * @param primera
	 * @param desplazamientos
	 * @param previsualizacionSegmentos
	 * @throws JIPException
	 */
	private void pintarSegmentos(Sequence seq, JIPBmpByte primera,
			ArrayList<Segment> desplazamientos,
			JIPGeomSegment previsualizacionSegmentos) throws JIPException {
		for (int j = 0; j < desplazamientos.size(); j++) {
			Segment segmento = desplazamientos.get(j);
			Point2D destino = segmento.getEnd();
			destino.setY(destino.getY() + primera.getHeight());
			segmento.setEnd(destino);
			previsualizacionSegmentos.addSegment(segmento);
		}
		previsualizacionSegmentos.setName("segmentos");
		seq.addFrame(previsualizacionSegmentos);
		
	}

	/**
	 * @param desplazamientos
	 * @param despl
	 */
	private Point2D calcularDesplazamiento(ArrayList<Segment> desplazamientos) {
		Point2D despl = new Point2D(0, 0);
		ArrayList<Point2D> semimedias = new ArrayList<Point2D>();

		Segment s1, s2;
		s1 = desplazamientos.get(0);
		for (int i = 1; i < desplazamientos.size(); i++) {
			s2 = desplazamientos.get(i);
			int x1 = s1.getBegin().getX() - s1.getEnd().getX();
			int y1 = s1.getBegin().getY() - s1.getEnd().getY();
			int x2 = s2.getBegin().getX() - s2.getEnd().getX();
			int y2 = s2.getBegin().getY() - s2.getEnd().getY();
			semimedias.add(new Point2D((x1 + x2) / 2, (y1 + y2) / 2));
			s1 = s2;
		}

		Point2D mediana = semimedias.get(semimedias.size() / 2);
		despl.setX(mediana.getX());
		despl.setY(mediana.getY());

		Point2D aux1;// TODO: DEBUG, quitar para la entrega
		if(semimedias.size() >= 2) {
		aux1 = semimedias.get(semimedias.size() / 2 - 1);
		System.out.println("Desplazamiento -1: [" + (aux1.getX()) + ", "
				+ (aux1.getY()) + "]");
		}

		System.out.println("Desplazamiento: [" + despl.getX() + ", "
				+ despl.getY() + "]");
		if(semimedias.size() >= 3) {
			aux1 = semimedias.get(semimedias.size() / 2 + 1);
		
		System.out.println("Desplazamiento +1: [" + (aux1.getX()) + ", "
				+ (aux1.getY()) + "]");
		}
		return despl;
	}

	/**
	 * @param primera
	 * @param segunda
	 * @param pixelesPrimera
	 * @param pixelesSegunda
	 * @param desplX
	 * @param desplY
	 * @return
	 * @throws JIPException
	 */
	private JIPBmpColor crearPanoramica(JIPBmpColor primera,
			JIPBmpColor segunda, int desplX, int desplY) throws JIPException {
		int anchoFinal = primera.getWidth() + segunda.getWidth();
		int altoFinal = primera.getHeight() + segunda.getHeight();
		double pixelesPrimera[] = primera.getAllPixels();
		double pixelesRojoPrimera[] = primera.getAllPixelsRed();
		double pixelesAzulPrimera[] = primera.getAllPixelsBlue();
		double pixelesVerdePrimera[] = primera.getAllPixelsGreen();
		double pixelesSegunda[] = segunda.getAllPixels();
		double pixelesRojoSegunda[] = segunda.getAllPixelsRed();
		double pixelesAzulSegunda[] = segunda.getAllPixelsBlue();
		double pixelesVerdeSegunda[] = segunda.getAllPixelsGreen();
		JIPBmpColor panoramica = new JIPBmpColor(anchoFinal, altoFinal);

		int desplXPrimera = 0;
		int desplYPrimera = 0;
		int desplXSegunda = 0;
		int desplYSegunda = 0;

		if (desplX >= 0 && desplY >= 0) {
			desplXSegunda = desplX;
			desplYSegunda = desplY;
		} else if (desplX >= 0) { // desplY < 0
			desplXSegunda = desplX;
			desplYPrimera = -desplY;
		} else if (desplY >= 0) { // desplX < 0
			desplXPrimera = -desplX;
			desplYSegunda = desplY;
		} else {
			desplXPrimera = -desplX;
			desplYPrimera = -desplY;
		}
		for (int j = 0; j < pixelesPrimera.length; j++) {
			panoramica.setPixelBlue((j % primera.getWidth()) + desplXPrimera,
					(j / primera.getWidth()) + desplYPrimera,
					pixelesAzulPrimera[j]);
			panoramica.setPixelRed((j % primera.getWidth()) + desplXPrimera,
					(j / primera.getWidth()) + desplYPrimera,
					pixelesRojoPrimera[j]);
			panoramica.setPixelGreen((j % primera.getWidth()) + desplXPrimera,
					(j / primera.getWidth()) + desplYPrimera,
					pixelesVerdePrimera[j]);
		}
		for (int j = 0; j < pixelesSegunda.length; j++) {
			panoramica.setPixelBlue((j % segunda.getWidth()) + desplXSegunda,
					(j / segunda.getWidth()) + desplYSegunda,
					pixelesAzulSegunda[j]);
			panoramica.setPixelRed((j % segunda.getWidth()) + desplXSegunda,
					(j / segunda.getWidth()) + desplYSegunda,
					pixelesRojoSegunda[j]);
			panoramica.setPixelGreen((j % segunda.getWidth()) + desplXSegunda,
					(j / segunda.getWidth()) + desplYSegunda,
					pixelesVerdeSegunda[j]);
		}
		return panoramica;
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
	private ArrayList<Segment> recorrerPuntos(JIPGeomPoint nitzPrimera,
			JIPGeomPoint nitzSegunda, JIPBmpByte primera, JIPBmpByte segunda)
			throws JIPException {
		ventana = getParamValueInt("ventana");
		double divisor, mediaPrimera, mediaSegunda;
		ArrayList<Segment> desplazamientos = new ArrayList<Segment>();

		ArrayList<Recorte> recortadosPrimera = getRecortes(nitzPrimera, primera);
		double divisorPrimera;
		ArrayList<Double> mediasPrimera = new ArrayList<Double>();
		ArrayList<Double> divisoresPrimera = new ArrayList<Double>();
		for (int i = 0; i < recortadosPrimera.size(); i++) {
			double media = calcularMedia((JIPBmpByte) recortadosPrimera.get(i)
					.getRecorte());
			mediasPrimera.add(media);
			// System.out.println(media);
			divisorPrimera = calcularDivisor((JIPBmpByte) recortadosPrimera
					.get(i).getRecorte(), media);
			divisoresPrimera.add(divisorPrimera);
		}

		ArrayList<Recorte> recortadosSegunda = getRecortes(nitzSegunda, segunda);
		double divisorSegunda;
		ArrayList<Double> mediasSegunda = new ArrayList<Double>();
		ArrayList<Double> divisoresSegunda = new ArrayList<Double>();
		for (int i = 0; i < recortadosSegunda.size(); i++) {
			double media = calcularMedia((JIPBmpByte) recortadosSegunda.get(i)
					.getRecorte());
			mediasSegunda.add(media);
			// System.out.println(media);
			divisorSegunda = calcularDivisor((JIPBmpByte) recortadosSegunda
					.get(i).getRecorte(), media);
			divisoresSegunda.add(divisorSegunda);
		}

		// ArrayList<Double> resultados = new ArrayList<Double>();
		// para cada recorte de la primera imagen
		for (int i = 0; i < recortadosPrimera.size(); i++) {
			PriorityQueue<Correlacion> correlaciones = new PriorityQueue<FPanorama.Correlacion>();
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

				double imagen2[] = ((JIPBmpByte) recorteSegunda.getRecorte())
						.getAllPixels();

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

		return desplazamientos;
	}

	/**
	 * @param nitz
	 *            imagen con los puntos calculados por Nitzberg.
	 * @param imagen
	 * @param ventana
	 * @param mediaPrimera
	 * @param recortados
	 * @return
	 * @throws JIPException
	 */
	private ArrayList<Recorte> getRecortes(JIPGeomPoint nitz, JIPBmpByte imagen)
			throws JIPException {
		ArrayList<Recorte> recortados = new ArrayList<FPanorama.Recorte>();
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
	 * @param ancho
	 * @param filaInicioPrimera
	 * @param columnaInicioPrimera
	 * @param tamanoVentana
	 * @throws JIPException
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
	 * @param ancho
	 * @param filaInicioPrimera
	 * @param columnaInicioPrimera
	 * @param tamanoVentana
	 * @throws JIPException
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

	class Correlacion implements Comparable<Correlacion> {
		private Point2D primerPunto, segundoPunto;
		private double correlacion;

		public Correlacion() {
		}

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

		@Override
		public int compareTo(Correlacion o) {
			if (o.getCorrelacion() > correlacion)
				return 1;
			else if (o.getCorrelacion() < correlacion)
				return -1;
			else
				return 0;
		}

		public String toString() {
			return "[" + primerPunto + "]->[" + segundoPunto + "]"
					+ correlacion;
		}

	}

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
