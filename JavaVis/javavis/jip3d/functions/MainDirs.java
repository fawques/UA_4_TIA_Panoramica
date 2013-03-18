package javavis.jip3d.functions;

import java.util.ArrayList;

import javax.vecmath.Color3f;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

import javavis.base.Function3DGroup;
import javavis.base.JIPException;
import javavis.jip3d.base.Function3D;
import javavis.jip3d.base.ScreenData;
import javavis.jip3d.base.ScreenOptions;
import javavis.jip3d.geom.Plane3D;
import javavis.jip3d.geom.Vector3D;
import javavis.jip3d.gui.dataobjects.PlaneSet3D;

/**
 * It calculates the main direction from a set of 3D planes.<br />
 * The screen data must be a set of 3D planes.<br />
 */
public class MainDirs extends Function3D {

	static final double pi2 = Math.PI/2.0;

	public MainDirs()
	{
		super();
		this.allowed_input = ScreenOptions.tPLANARSET3D;
		this.group = Function3DGroup.Others;
	}
	@Override
	public void proccessData(ScreenData scr_data) throws JIPException {
		result_list = new ArrayList<ScreenData>();
		int count;
		ArrayList<Vector3D> maindirs = findMainDirections(scr_data.elements());
		ArrayList<ArrayList<Plane3D>> scene_selected = segmentDirection(scr_data.elements(), maindirs);
		PlaneSet3D dir;
		dir = new PlaneSet3D(new ScreenOptions());
		ArrayList<Plane3D> planes_list = scene_selected.get(0);
		dir.name = "dir1";
		dir.scr_opt.width = 1;
		dir.scr_opt.color = new Color3f(1,0,0);
		dir.scr_opt.global_color = true;
		for(count=0;count<planes_list.size();count++)
			dir.insert(planes_list.get(count));
		result_list.add(dir);

		dir = new PlaneSet3D(new ScreenOptions());
		planes_list = scene_selected.get(1);
		dir.name = "dir2";
		dir.scr_opt.width = 1;
		dir.scr_opt.color = new Color3f(0,1,0);
		dir.scr_opt.global_color = true;
		for(count=0;count<planes_list.size();count++)
			dir.insert(planes_list.get(count));
		result_list.add(dir);

		dir = new PlaneSet3D(new ScreenOptions());
		planes_list = scene_selected.get(2);
		dir.name = "dir3";
		dir.scr_opt.width = 1;
		dir.scr_opt.color = new Color3f(0,0,1);
		dir.scr_opt.global_color = true;
		for(count=0;count<planes_list.size();count++)
			dir.insert(planes_list.get(count));
		result_list.add(dir);

	}
    public ArrayList<Vector3D> findMainDirections(Object []scene)
    {
    	int count;
    	Plane3D plane;
    	ArrayList<Vector3D>ret = new ArrayList<Vector3D>();
    	Vector3D v1, v2, v3;
		double [][]matrix = new double [3][3];
		DenseDoubleMatrix2D ddmatrix;
		DoubleMatrix2D U;
		SingularValueDecomposition SVD;
		double []data;

		matrix[0][0] = matrix[0][1] = matrix[0][2] =
			matrix[1][0] = matrix[1][1] = matrix[1][2] =
			matrix[2][0] = matrix[2][1] = matrix[2][2] = 0;

		for(count=0;count<scene.length;count++)
		{
			plane = (Plane3D)scene[count];
			data = plane.vector.getCoords();
			matrix[0][0] += data[0] * data[0];
			matrix[0][1] += data[0] * data[1];
			matrix[0][2] += data[0] * data[2];
			matrix[1][1] += data[1] * data[1];
			matrix[1][2] += data[1] * data[2];
			matrix[2][2] += data[2] * data[2];

		}
		matrix[1][0] = matrix[0][1];
		matrix[2][0] = matrix[0][2];
		matrix[2][1] = matrix[1][2];

		ddmatrix = new DenseDoubleMatrix2D(matrix);
		SVD = new SingularValueDecomposition(ddmatrix);
		U = SVD.getU();
		v1 = new Vector3D(U.getQuick(0, 0), U.getQuick(1, 0), U.getQuick(2, 0));
		v2 = new Vector3D(U.getQuick(0, 1), U.getQuick(1, 1), U.getQuick(2, 1));
		v3 = new Vector3D(U.getQuick(0, 2), U.getQuick(1, 2), U.getQuick(2, 2));
		ret.add(v1); ret.add(v2); ret.add(v3);
		return ret;
    }

    public ArrayList<ArrayList<Plane3D>> segmentDirection(Object []elements, ArrayList<Vector3D> dirs)
    {
    	ArrayList<ArrayList<Plane3D>> ret = new ArrayList<ArrayList<Plane3D>>();
    	ArrayList<Plane3D> set1 = new ArrayList<Plane3D>();
    	ArrayList<Plane3D> set2 = new ArrayList<Plane3D>();
    	ArrayList<Plane3D> set3 = new ArrayList<Plane3D>();
    	int count;
    	double d1, d2, d3;
    	Plane3D plane;

    	for(count=0;count<elements.length;count++)
    	{
    		plane = (Plane3D)elements[count];
    		d1 = plane.vector.getAngle(dirs.get(0));
    		if(d1>pi2) d1 = Math.PI - d1;
    		d2 = plane.vector.getAngle(dirs.get(1));
    		if(d2>pi2) d2 = Math.PI - d2;
    		d3 = plane.vector.getAngle(dirs.get(2));
    		if(d3>pi2) d3 = Math.PI - d3;

    		if(d1<d2 && d1<d3) set1.add(plane);
    		else if(d2<d1 && d2<d3) set2.add(plane);
    		else set3.add(plane);
    	}
    	ret.add(set1);
    	ret.add(set2);
    	ret.add(set3);
    	return ret;
    }

}
