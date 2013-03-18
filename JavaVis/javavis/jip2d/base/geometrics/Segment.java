package javavis.jip2d.base.geometrics;

public class Segment extends GeomData {

	/**
	 * @uml.property  name="begin"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Point2D begin;
	/**
	 * @uml.property  name="end"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Point2D end;
	
	public Segment(Point2D begini, Point2D endi) {
		begin=new Point2D(begini);
		end=new Point2D(endi);
	}
	
	public Segment (Segment s) {
		begin=new Point2D(s.begin);
		end=new Point2D(s.end);
	}
	
	public void setSegment (Point2D begini, Point2D endi) {
		begin=new Point2D(begini);
		end=new Point2D(endi);
	}

	public String toString() {
		return "x1="+begin.getX()+" y1="+begin.getY()+" x2="+end.getX()+" y2="+end.getY();
	}

	/**
	 * @return
	 * @uml.property  name="begin"
	 */
	public Point2D getBegin() {
		return begin;
	}

	/**
	 * @param begin
	 * @uml.property  name="begin"
	 */
	public void setBegin(Point2D begin) {
		this.begin = begin;
	}

	/**
	 * @return
	 * @uml.property  name="end"
	 */
	public Point2D getEnd() {
		return end;
	}

	/**
	 * @param end
	 * @uml.property  name="end"
	 */
	public void setEnd(Point2D end) {
		this.end = end;
	}
}
