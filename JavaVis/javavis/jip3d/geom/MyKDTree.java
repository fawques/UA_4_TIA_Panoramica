package javavis.jip3d.geom;

import java.io.Serializable;
import java.util.ArrayList;

import edu.wlu.cs.levy.CG.*;

/**
 * Class MyKDTree, extends from KDTree class
 * @author Diego Viejo Hernando
 * 
 * Last update: 2004-July-19
 */
public class MyKDTree extends KDTree implements Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * @uml.property  name="num_elements"
	 */
	public int num_elements;
	/**
	 * Tree dimension
	 * @uml.property  name="k"
	 */
	public int K;
	/**
	 * Max. range that a tree element can achieve
	 * @uml.property  name="maxRange" multiplicity="(0 -1)" dimension="1"
	 */
	public double []maxRange;
	/**
	 * Min. range that a tree element can achieve
	 * @uml.property  name="minRange" multiplicity="(0 -1)" dimension="1"
	 */
	public double []minRange;
	/**
	 * @uml.property  name="radius"
	 */
	public double radius;

	public MyKDTree()
	{
		super(3);
		num_elements = 0;
		radius = 0.0;
		K = 3;
	}

	/**
	 * Constructor
	 * @param int k: dimension
	 */
	public MyKDTree(int k)
	{
		super(k);
		K=k;
		num_elements = 0;
		maxRange = new double[k];
		minRange = new double[k];
		radius = 0.001;
		for(int i=0;i<k;i++)
		{
			maxRange[i] = -Double.MAX_VALUE;
			minRange[i] = Double.MAX_VALUE;
		}
	}

	/**
	 * Function insert. Insert an element in the tree
	 * @param double []key: key to order the tree when you insert
	 * @param Object value: element to insert
	 */
	public void insert(double []key, Object value)
	{
		try {
			super.insert(key, value);
			num_elements++;
			for(int i=0;i<K;i++)
			{
				if(key[i]>maxRange[i]) maxRange[i]=key[i];
				if(key[i]<minRange[i]) minRange[i]=key[i];
			}
		} catch(Exception e){}
	}

	/**
	 * Function elements. Return all elements from the tree
	 * @return Array Objects with all elements from the tree
	 */
	public Object[] elements()
	{
		Object []ret = null;
		try {
			if(num_elements>0)
				ret = super.range(minRange, maxRange);
		} catch(Exception e)
		{
			System.err.println("ArbolKD (elements): "+e);
		}
		return ret;
	}

	/**
	 * Function range. Returns all elements that are within rad distance from point key
	 * @param key Range center
	 * @param rad Range radius
	 * @return Array with the selected elements
	 * @throws Exception
	 */
	public Object[] range(Point3D key, double rad) throws Exception
	{
		double []min;
		double []max;
		Object []elements;
		ArrayList <Object>result = new ArrayList<Object>();
		int count;
		Point3D point;
		Plane3D plane;

		min = key.getCoords();
		max = key.getCoords();
		min[0] -= rad;
		min[1] -= rad;
		min[2] -= rad;
		max[0] += rad;
		max[1] += rad;
		max[2] += rad;

		elements = this.range(min, max);
		for(count=0;count<elements.length;count++)
		{
			if(elements[count] instanceof Point3D)
			{
				point = (Point3D) elements[count];
				if(point.getDistance(key)<rad) result.add(elements[count]);
			}
			else if(elements[count] instanceof Plane3D)
			{
				plane = (Plane3D) elements[count];
				if(plane.origin.getDistance(key)<rad) result.add(elements[count]);
			}
			else result.add(elements[count]);
		}
		return result.toArray();
	}

	/**
	 * Function size. Return the number of elements from a tree.
	 * @return The number of elements from a tree.
	 */
	public int size() {
		return num_elements;
	}

	/**
	 * Function search. Search an element in a tree
	 * @param double []key: Key to search the element
	 * @return The object with that key. If it is not find the element, it returns null
	 */
	public Object search(double []key)
	{
		Object ret = null;
		try {
			ret = super.search(key);
		}catch(Exception e)
		{
			System.err.println(e);
		}
		return ret;

	}

	/**
	 * Function neighbor. Return the nearest neighbor
	 * @param double[] key: Key to search
	 * @param MyKDTree visited: Elements from the tree
	 * @return The nearest neighbor
	 */
	public Object neighbor(double []key, MyKDTree visited)
	{
		double increase = 0.001;
		boolean exit = false;
		Object []elements;
		Point3D element;
		Object betterNeighbor = null;
		double betterDistance;
		double difX, difY, difZ, distancia;
		double []rangeMax = new double[K];
		double []rangeMin = new double[K];
		rangeMax[0] = key[0]+radius;
		rangeMax[1] = key[1]+radius;
		rangeMax[2] = key[2]+radius;
		rangeMin[0] = key[0]-radius;
		rangeMin[1] = key[1]-radius;
		rangeMin[2] = key[2]-radius;

		try {
			if (visited.size()==0)
			{
				return super.nearest(key);
			}
			else if(visited.size()>=this.num_elements)
				return null;
			else while(!exit&&radius<0.3)
			{
				betterDistance = radius;
				elements = super.range(rangeMin, rangeMax);
				for(int i=0;i<elements.length;i++)
				{
					element = (Point3D)elements[i];
					if(visited.search(element.getCoords())==null)
					{
						difX = Math.abs(key[0] - element.getX());
						if(difX<betterDistance)
						{
							difY = Math.abs(key[1]-element.getY());
							if(difY<betterDistance)
							{
								difZ = Math.abs(key[2]-element.getZ());
								if(difZ<betterDistance)
								{
									distancia = Math.sqrt(difX*difX + difY*difY + difZ*difZ);
									if(distancia<betterDistance)
									{

										exit=true;
										betterDistance = distancia;
										betterNeighbor = elements[i];
										radius = distancia+increase;
									} 
								} 
							}
						}
					}
				}
				//For the next search, increase the radius
				if(!exit)
				{
					radius+=increase;
					rangeMax[0]+=increase;
					rangeMax[1]+=increase;
					rangeMax[2]+=increase;
					rangeMin[0]-=increase;
					rangeMin[1]-=increase;
					rangeMin[2]-=increase;
				}
			}
		} catch(Exception e)
		{
			System.err.println(e);
		}
		if(betterNeighbor!=null)
		{
			element = (Point3D)betterNeighbor;
			visited.insert(element.getCoords(), element);
		}
		return betterNeighbor;
	}

	/**
	 * Function resetRadius. Initialize the search radius for neighbor function.
	 */
	public void resetRadius()
	{
		radius = 0.001;
	}

	/**
	 * Function setRadius. Method set of radius
	 * @param  double v: the new radius value
	 * @uml.property  name="radius"
	 */
	public void setRadius(double v)
	{
		radius = v;
	}

	/**
	 * Function getRadius. Method get of radius
	 * @return  Return the current radius
	 * @uml.property  name="radius"
	 */
	public double getRadius()
	{
		return radius;
	}
}
