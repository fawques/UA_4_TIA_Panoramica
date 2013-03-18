package javavis.jip3d.functions;

import java.util.ArrayList;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.base.parameter.ParamScrData;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Feature2D;
import javavis.jip3d.geom.MyTransform3D;
import javavis.jip3d.geom.Point3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.FeatureSet2D;

public class ErrorReductionFeatures2D extends Function3D {

	public ErrorReductionFeatures2D() {
		super();
		this.allowed_input = ScreenOptions.tFEATURESET2D;
		group = Function3DGroup.Others;

		ParamScrData p1 = new ParamScrData("Next Set");
		
		addParam(p1);
	}

	@Override
	public void proccessData(ScreenData scrData) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		ScreenData secondSet = getParamValueScrData("Next Set");
		int cont;
		Feature2D key;
		int best1, best2, best3;
		double dist, dist1, dist2, dist3;
		Feature2D a, b, c;
		trilateration tri;
		double [][]radius;
		int num_matches, match;
		ArrayList<Feature2D> matchedFeatures = new ArrayList<Feature2D>();
		
		Object []elements1 = scrData.elements();
		
		Object []elements2 = secondSet.elements();
		int tam2 = elements2.length;

		//match both sets
		int []matches = new int[tam2];
		num_matches=0;
		for(cont=0;cont<tam2;cont++)
		{
			key = (Feature2D)elements2[cont];
			match = checkForMatch(key, elements1);
			matches[cont] = match;
			if(match>-1) num_matches++;
		}
		
		//look for the three closest features from the matched
		dist1 = dist2 = dist3 = 9999999;
		best1 = best2 = best3 = -1;
		for(cont=0;cont<tam2;cont++)
		{
			if(matches[cont]>-1)
			{
				key = (Feature2D) elements2[cont];
				dist = key.getOriginDistance();
				
				if(dist<dist1)
				{
					dist3 = dist2;
					dist2 = dist1;
					dist1 = dist;
					
					best3 = best2;
					best2 = best1;
					best1 = cont;
				}
				else if(dist<dist2)
				{
					dist3 = dist2;
					dist2 = dist;
					
					best3 = best2;
					best2 = cont;
				}
				else if(dist<dist3)
				{
					dist3 = dist;
					best3 = cont;
				}
			}
		}
		
		//create the trilateration with the corresponding points in the first set
		a = (Feature2D)elements1[matches[best1]];
		b = (Feature2D)elements1[matches[best2]];
		c = (Feature2D)elements1[matches[best3]];
//		a = (Feature2DNew)elements2[best1];
//		b = (Feature2DNew)elements2[best2];
//		c = (Feature2DNew)elements2[best3];
		if(checkForLinearity(a, b, c))
			System.out.println("Son lineales!");
		tri = new trilateration(a, b, c);
		
		// get the distances from each point to the three closest ones in the second set
		radius = new double[num_matches][3];
		a = (Feature2D)elements2[best1];
		b = (Feature2D)elements2[best2];
		c = (Feature2D)elements2[best3];
		
		match = 0;
		for(cont=0;cont<tam2;cont++)
		{
			if(matches[cont]>-1 && cont!=best1 && cont!=best2 && cont!=best3)
			{
//				key = (Feature2DNew)elements1[matches[cont]];
				key = (Feature2D)elements2[cont];
				radius[match][0] = a.getDistance(key);
				radius[match][1] = b.getDistance(key);
				radius[match][2] = c.getDistance(key);
				matchedFeatures.add((Feature2D)elements1[matches[cont]]);
				match++;
			}
		}
		
		//compute the corrected set
		result_list.add(tri.process(radius, matchedFeatures));
	}
	
	public int checkForMatch(Feature2D key, Object []list)
	{
		Feature2D element;
		int i, best;
		int tam = list.length;
		double dist, dist1, dist2;
		best = -1;
		dist1 = dist2 = 999999999;
		
		for(i=0;i<tam;i++)
		{
			element = (Feature2D)list[i];
			dist = key.getDistanceDescriptor(element);
			if(dist < dist1)
			{
				dist2 = dist1;
				dist1 = dist;
				best = i;
			}
			else if(dist<dist2) dist2 = dist;
		}
		
		if(dist1 >= 0.36 * dist2) best = -1;
		
		return best;
	}
	
	public boolean checkForLinearity(Feature2D a, Feature2D b, Feature2D c)
	{
		boolean ret = false;
		Vector3D ab = new Vector3D(b.subPoint(a));
		Vector3D ac = new Vector3D(c.subPoint(a));
		double eps = 0.0001;
		
		double angle = ab.getAngle(ac); //0 <= angle <= pi
		System.out.println("Angle: "+angle);
		if(angle < eps || (Math.PI-angle)<eps) ret = true;
		
		return ret;
	}
	
	/**
	 * @author  miguel
	 */
	public class trilateration
	{
		/**
		 * @uml.property  name="transformation"
		 * @uml.associationEnd  
		 */
		MyTransform3D transformation; //Transforms a point from the trilaterated system to the global one
		/**
		 * @uml.property  name="inversetransformation"
		 * @uml.associationEnd  
		 */
		MyTransform3D inversetransformation; //Transforms a point from the global system to the trilaterated
		double d;
		double i;
		double j;
		/**
		 * @uml.property  name="pA"
		 * @uml.associationEnd  
		 */
		Feature2D pA;
		/**
		 * @uml.property  name="pB"
		 * @uml.associationEnd  
		 */
		Feature2D pB;
		/**
		 * @uml.property  name="pC"
		 * @uml.associationEnd  
		 */
		Feature2D pC;
		
		public trilateration(Feature2D A, Feature2D B, Feature2D C)
		{
			DenseDoubleMatrix2D rotation;
			double [] traslation;
			double [] inverseTraslation;
			traslation = A.getCoords();
			inverseTraslation = new double[3];
			inverseTraslation[0] = -traslation[0]; inverseTraslation[1] = -traslation[1]; inverseTraslation[2] = -traslation[2];
			//The new origin is A
			Feature2D localA = A.subPoint(A);
			Feature2D localB = B.subPoint(A);
			Feature2D localC = C.subPoint(A);
			
			//Build a new reference frame from plane ABC
			Vector3D X, Y, Z;
			Vector3D AB, AC, normal;
			
			AB = new Vector3D(localB);
			AC = new Vector3D(localC);
			normal = AB.crossProduct(AC);
			X = new Vector3D(AB);
			X.normalize();
			Z = new Vector3D(normal);
			Z.normalize();
			Y = normal.crossProduct(AB);
			Y.normalize();
			
			rotation = new DenseDoubleMatrix2D(3, 3);
			
			rotation.setQuick(0, 0, X.getX()); rotation.setQuick(0, 1, Y.getX()); rotation.setQuick(0, 2, Z.getX());
			rotation.setQuick(1, 0, X.getY()); rotation.setQuick(1, 1, Y.getY()); rotation.setQuick(1, 2, Z.getY());
			rotation.setQuick(2, 0, X.getZ()); rotation.setQuick(2, 1, Y.getZ()); rotation.setQuick(2, 2, Z.getZ());
			transformation = new MyTransform3D(rotation);
			transformation.setTranslation(traslation);

			inversetransformation = transformation.getInverse();

			System.out.println("Original Points");
			System.out.println(A.toString());
			System.out.println(B.toString());
			System.out.println(C.toString());
			pA = new Feature2D(localA);
			pB = new Feature2D(B);
			pC = new Feature2D(C);
			pB.applyTransform(inversetransformation);
			pC.applyTransform(inversetransformation);
			System.out.println("transformation: "+transformation.toStringExtended());
			System.out.println("invTrans: "+inversetransformation.toStringExtended());
			System.out.println("TRansformated points:");
			System.out.println(pA.toString());
			System.out.println(pB.toString());
			System.out.println(pC.toString());
			d = pB.getX();
			i = pC.getX();
			j = pC.getY();			
		}
		
		/**
		 * Compute the trilateration for the input set.
		 * @param radius radius from the three circles
		 * @param features
		 * @return
		 */
		public FeatureSet2D process(double [][]radius, ArrayList<Feature2D> features)
		{
			FeatureSet2D ret = new FeatureSet2D(new ScreenOptions());
			ret.name = "trilaterated";
			Feature2D element;
			Point3D target;
			int cont, tam;
			double x, y, z;
			double r1, r2, r3;
			double root;
			Point3D candidate1, candidate2;
			
			tam = features.size();
			y = 0;
			
			for(cont=0;cont<tam;cont++)
			{
				r1 = radius[cont][0];
				r2 = radius[cont][1];
				r3 = radius[cont][2];
				
				x = (r1*r1 -r2*r2 + d*d) / (2*d);
				y = (r1*r1 - r3*r3 + i*i + j*j)/(2*j) - (i*x/j);
				
				root = r1*r1 - x*x -y*y; //¿la y que aparece en la ecuación, sería la z?
//				System.out.println("raiz: "+root);
				if(root<0 && root > -1) root = -root;
				if(root>0) // && root < 2)
				{
					z = Math.sqrt(root);
					//here we have two options: the positive and the negative result of the squared root...
					//we have to choose the z value that makes the new point rely closest to the original one
					candidate1 = new Point3D(x, y, z);
					candidate2 = new Point3D(x, y, -z);
					element = new Feature2D(features.get(cont));
					element.applyTransform(inversetransformation);
					System.out.println(candidate1.getDistance2(element)+" - "+candidate2.getDistance2(element)+" = "+(candidate1.getDistance2(element) - candidate2.getDistance2(element)));
					if(candidate1.getDistance2(element) < candidate2.getDistance2(element))
						target = candidate1;
					else
						target = candidate2;
					target.applyTransform(transformation);
					element.setX(target.getX());
					element.setY(target.getY());
					element.setZ(target.getZ());
					ret.insert(element);
				}
				
			}
			
			//the center of the circles are also inserted in the result
			pA.applyTransform(transformation);
			pB.applyTransform(transformation);
			pC.applyTransform(transformation);
			System.out.println("Original points (supossed)");
			System.out.println(pA.toString());
			System.out.println(pB.toString());
			System.out.println(pC.toString());
			ret.insert(pA);
			ret.insert(pB);
			ret.insert(pC);
			return ret;
		}
	}

}
