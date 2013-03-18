package javavis.jip3d.functions;

import java.util.ArrayList;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamFloat;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PointSet3D;

public class ErrorReductionStereo extends Function3D {
	
	public ErrorReductionStereo()
	{
		super();
		this.allowed_input = ScreenOptions.tPOINTSET3D;
		group = Function3DGroup.Others;
		
		ParamFloat p1 = new ParamFloat("Error_Model");
		p1.setValue(0.065f);
		
		addParam(p1);
	}
	
	/**
	 * Esto es una prueba: Quita el 10 por ciento de la distancia de cada punto!!!
	 * solo para ver que pasa y si mejora el alineamiento entre escenas.
	 */
	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		PointSet3D result = new PointSet3D(new ScreenOptions());
		result.name = scrData.name.substring(0, scrData.name.length()-3);
		result.name.concat("reduced");
		
		double error_model = getParamValueFloat("Error_Model");
		
		Object []elements = scrData.elements();
		Point3D element;
		int tam = elements.length;
		int cont;
		Vector3D subvec;
		
		for(cont=0;cont<tam;cont++)
		{
			element = (Point3D)elements[cont];
			subvec = new Vector3D(element.getCoords());
			subvec.scaleVector(error_model);
			result.insert(element.subPoint(subvec));
		}
		
		result_list.add(result);
	}

}
