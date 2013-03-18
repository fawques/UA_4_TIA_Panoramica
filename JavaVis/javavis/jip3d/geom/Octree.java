package javavis.jip3d.geom;

import java.util.ArrayList;


public class Octree {


	/**
	 * @uml.property  name="resolution"
	 */
	private float resolution = 0.05f;
	final static private int num_children = 8;

	/**
	 * @uml.property  name="node"
	 * @uml.associationEnd  
	 */
	private Point3D node;
	/**
	 * @uml.property  name="children"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	private Octree []children;
	/**
	 * @uml.property  name="anchor"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Point3D anchor; //lower limit
	/**
	 * @uml.property  name="limite"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	Point3D limit; //upper limit
	/**
	 * @uml.property  name="is_leaf"
	 */
	boolean is_leaf;
	/**
	 * @uml.property  name="num_points"
	 */
	int num_points;


	/**
	 * Constructor 
	 * @param l_l lower limit
	 * @param u_l upper limit
	 */
	public Octree(Point3D l_l, Point3D u_l, float res)
	{
		double difX, difY, difZ;
		children = new Octree[8];
		for(int i=0;i<num_children;i++) children[i] = null;
		anchor = l_l;
		limit = u_l;
		num_points = 0;
		node = new Point3D();
		resolution = res;

		difX = Math.abs(anchor.getX()- limit.getX());
		difY = Math.abs(anchor.getY()- limit.getY());
		difZ = Math.abs(anchor.getZ()- limit.getZ());
		if(resolution>difX || resolution>difY || resolution>difZ)
			is_leaf = true;
		else
		{
			is_leaf = false;
			node.setX( (limit.getX() + anchor.getX()) / 2.0f );
			node.setY( (limit.getY() + anchor.getY()) / 2.0f );
			node.setZ( (limit.getZ() + anchor.getZ()) / 2.0f );
		}
	}

	public Octree(Point3D p, Point3D l_l, Point3D u_l, float res)
	{
		double difX, difY, difZ;
		Point3D next_l, next_u;
		children = new Octree[8];
		for(int i=0;i<num_children;i++) children[i] = null;
		anchor = l_l;
		limit = u_l;
		num_points = 0;
		node = new Point3D();
		resolution = res;
		int position;

		next_l = new Point3D();
		next_u = new Point3D();

		difX = Math.abs(anchor.getX() - limit.getX());
		difY = Math.abs(anchor.getY() - limit.getY());
		difZ = Math.abs(anchor.getZ() - limit.getZ());
		if(resolution>difX || resolution>difY || resolution>difZ)
		{
			is_leaf = true;
			node.addData(p,	num_points);
			num_points++;
		}
		else
		{
			is_leaf = false;
			node.setX( (limit.getX() + anchor.getX()) / 2.0f );
			node.setY( (limit.getY() + anchor.getY()) / 2.0f );
			node.setZ( (limit.getZ() + anchor.getZ()) / 2.0f );
			position = this.search(p, next_l, next_u);
			if(children[position]!=null)
				children[position].insert(p);
			else children[position] = new Octree(p, next_l, next_u, resolution);
		}
	}

	/**
	 * Function insert. Insert a point in the patch
	 * @param p Point to insert
	 */
	public void insert(Point3D p)
	{
		int position;
		Point3D next_l, next_u;

		next_l = new Point3D();
		next_u = new Point3D();

		if(is_leaf)
		{
			node.addData(p, num_points);
			num_points ++;
		}
		else
		{
			position = this.search(p, next_l, next_u);
			if(children[position]!=null)
				children[position].insert(p);
			else children[position] = new Octree(p, next_l, next_u, resolution);
		}
	}

	public ArrayList<Point3D>getAll()
	{
		ArrayList<Point3D> ret = new ArrayList<Point3D>();
		int count;

		if(this.is_leaf)
		{
			if(num_points>0)
				ret.add(node);
		}
		else
		{
			for(count=0;count<8;count++)
			{
				if(children[count]!=null)
				{
					ret.addAll(children[count].getAll());
				}
			}
		}
		return ret;
	}

	private int search(Point3D p, Point3D next_l_l, Point3D next_u_l)
	{
		int ret;

		if(p.getX() < node.getX())
		{
			if(p.getY() < node.getY())
			{
				if(p.getZ() < node.getZ())
				{
					ret = 0;
					next_l_l.setX(anchor.getX());
					next_l_l.setY(anchor.getY());
					next_l_l.setZ(anchor.getZ());
					next_u_l.setX(node.getX());
					next_u_l.setY(node.getY());
					next_u_l.setZ(node.getZ());
				}
				else
				{
					ret = 1;
					next_l_l.setX(anchor.getX());
					next_l_l.setY(anchor.getY());
					next_l_l.setZ(node.getZ());
					next_u_l.setX(node.getX());
					next_u_l.setY(node.getY());
					next_u_l.setZ(limit.getZ());
				}
			}
			else
			{
				if(p.getZ() < node.getZ())
				{
					ret = 2;
					next_l_l.setX(anchor.getX());
					next_l_l.setY(node.getY());
					next_l_l.setZ(anchor.getZ());
					next_u_l.setX(node.getX());
					next_u_l.setY(limit.getY());
					next_u_l.setZ(node.getZ());
				}
				else
				{
					ret = 3;
					next_l_l.setX(anchor.getX());
					next_l_l.setY(node.getY());
					next_l_l.setZ(node.getZ());
					next_u_l.setX(node.getX());
					next_u_l.setY(limit.getY());
					next_u_l.setZ(limit.getZ());
				}
			}
		}
		else
		{
			if(p.getY() < node.getY())
			{
				if(p.getZ() < node.getZ())
				{
					ret = 4;
					next_l_l.setX(node.getX());
					next_l_l.setY(anchor.getY());
					next_l_l.setZ(anchor.getZ());
					next_u_l.setX(limit.getX());
					next_u_l.setY(node.getY());
					next_u_l.setZ(node.getZ());
				}
				else
				{
					ret = 5;
					next_l_l.setX(node.getX());
					next_l_l.setY(anchor.getY());
					next_l_l.setZ(node.getZ());
					next_u_l.setX(limit.getX());
					next_u_l.setY(node.getY());
					next_u_l.setZ(limit.getZ());
				}
			}
			else
			{
				if(p.getZ() < node.getZ())
				{
					ret = 6;
					next_l_l.setX(node.getX());
					next_l_l.setY(node.getY());
					next_l_l.setZ(anchor.getZ());
					next_u_l.setX(limit.getX());
					next_u_l.setY(limit.getY());
					next_u_l.setZ(node.getZ());
				}
				else
				{
					ret = 7;
					next_l_l.setX(node.getX());
					next_l_l.setY(node.getY());
					next_l_l.setZ(node.getZ());
					next_u_l.setX(limit.getX());
					next_u_l.setY(limit.getY());
					next_u_l.setZ(limit.getZ());
				}
			}
		}

		return ret;
	}
	
	/**
	 * Remove points from the tree's leaves when the amount of points in that leave is less than min
	 * @param min
	 */
	public void cleanTree(int min)
	{
		int count;
		if(this.is_leaf)
		{
			if(num_points<min)
			{
				num_points = 0;
				node = null;
			}
		}
		else
		{
			for(count=0;count<8;count++)
			{
				if(children[count]!=null)
				{
					children[count].cleanTree(min);
				}
			}
		}
	}
}
