package de.tuhh.diss.plotbot.geometry;

/**
 * Strictly contains definitions and methods of a 2D point.
 * 
 * Some of the functionality this class supposed to have is ported to Robot
 * class. Reason : Separation of geometric primitives, operations from robot's
 * kinematics
 * 
 */
public class Coord {
	private double x;
	private double y;

	/**
	 * Constructor accepting double
	 * 
	 * @param x
	 * @param y
	 */
	public Coord(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * constructor accepting int
	 * 
	 * @param x
	 * @param y
	 */
	public Coord(int x, int y) {
		this.x = (double) x;
		this.y = (double) y;
	}

	/**
	 * Translate a given coord by the given amounts
	 * 
	 * @param x
	 * @param y
	 */
	public void translate(double x, double y) {
		this.x += x;
		this.y += y;
	}

	/**
	 * Update a point's coordinates
	 * 
	 * @param x
	 * @param y
	 */
	public void setValue(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Get distance between this point and another point
	 * @param coord2
	 * @return
	 */
	public double getDistance(Coord coord2) {
		return Coord.getDistance(this, coord2);
	}

	/**
	 * Get distance between two coordinates coord 2 - coord 1
	 * 
	 * @return
	 */
	public static double getDistance(Coord coord1, Coord coord2) {

		return Math.sqrt(Math.pow((coord2.x() - coord1.x()), 2)
				+ Math.pow((coord2.y() - coord1.y()), 2));
	}

	/**
	 * Get angle from coord 1 to coord 2 around coord 3
	 * 
	 * @param coord1
	 * @param coord2
	 * @param coord3
	 * @return
	 */
	public static double getThreePointAngle(Coord p1, Coord p2, Coord p3) {
		return Math.atan2(p2.y() - p1.y(), p2.x() - p1.x())
				- Math.atan2(p3.y() - p1.y(), p3.x() - p1.x());
	}

	/**
	 * Return X value. Use short name for keeping code length minimal. Most
	 * geometry/ image processing libraries (ie: OpenCV, Boost Geometry) use
	 * this format
	 * 
	 * @return
	 */
	public double x() {
		return x;
	}

	/**
	 * Return Y value. Use short name for keeping code length minimal. Most
	 * geometry/ image processing libraries (ie: OpenCV, Boost Geometry) use
	 * this format
	 * 
	 * @return
	 */
	public double y() {
		return y;
	}

	/**
	 * set X value. Use short name for keeping code length minimal. Most
	 * geometry/ image processing libraries (ie: OpenCV, Boost Geometry) use
	 * this format
	 * 
	 * @return
	 */
	public void x(double x) {
		this.x = x;
	}

	/**
	 * set Y value. Use short name for keeping code length minimal. Most
	 * geometry/ image processing libraries (ie: OpenCV, Boost Geometry) use
	 * this format
	 * 
	 * @return
	 */
	public void y(double y) {
		this.y = y;
	}
}
