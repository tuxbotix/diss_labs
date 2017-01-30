package de.tuhh.diss.plotbot.geometry;

import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;
import de.tuhh.diss.plotbot.lowerLayer.Robot;
import lejos.nxt.LCD;
import lejos.util.Delay;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Line implements Plottable {

	private Coord startPoint;
	private Coord endPoint;
	private double length;
	private double slopeAngle;// in radians. No real use of handling this in degrees.
	private PlotbotControl control;
	private boolean plotStart = false;

	/**
	 * Constructor for line
	 * 
	 * @param starting coord 
	 * @param end coord 
	 */
	
	public Line(Coord start, Coord end) {
		startPoint = start;
		endPoint = end;
		length = Coord.getDistance(start, end);
		slopeAngle = Math.atan2((end.y() - start.y()), (end.x() - start.x()));
	}

	/**
	 * A timer is used to plot. relatively better than a while loop. Reason :
	 * while loop cause unnecessarily load the system.
	 * 
	 */
	
	class PlotTimerHandler implements TimerListener {
		private Timer mainTimer;
		public boolean timerStatus;

		public PlotTimerHandler() {
			mainTimer = new Timer((1000 / Robot.TIMER_FREQ), this);
			timerStatus = false;
		}

		public void timerStart() {
			mainTimer.start();
			timerStatus = true;
		}

		public void timerStop() {
			mainTimer.stop();
			timerStatus = false;
		}

		@Override
		public void timedOut() {
			if (!trajectoryPlanner()) {
				timerStop();
				control.stopMotion();
			}
		}
	}

	/**
	 * Plot function
	 * 
	 * Pen movements shall be controlled elsewhere (save time when drawing
	 * shapes like rectangle, etc)
	 */
	@Override
	public void plot(PlotbotControl pc) {
		control = pc;
		// move to start position
		// If moveTo fails, its unsolvable case, this line will not be drawn
		// further
		if (!PlotbotControl.getInstance().moveTo(startPoint)) {
			LCD.drawString("fail*", 0, 2);
			return;
		}
		LCD.drawString("line Init", 0, 2);
		PlotbotControl.getInstance().movePen(true); // move pen down

		// start the timer
//		PlotTimerHandler plotTimer = new PlotTimerHandler();
//		plotTimer.timerStart();

		LCD.drawString("timer started", 0, 3);

//		while (plotTimer.timerStatus) {

//		}
		while(trajectoryPlanner()){
			Delay.msDelay(100);
		}
		PlotbotControl.getInstance().stopMotion(); // stop motion
//		PlotbotControl.getInstance().resetActuatorSpeeds(); // reset motor
															// speeds
		PlotbotControl.getInstance().movePen(false); // pull up the pen

		LCD.drawString("timer stopped", 0, 3);
	}

	/**
	 * Called periodically to calculate target point & move.
	 * 
	 * @return false if movement should be stopped.
	 */
	private boolean trajectoryPlanner() {

		Coord currentPosition = PlotbotControl.getInstance().getRobotPosition();

		double distanceToEnd = currentPosition.getDistance(endPoint);

		// if the robot is close enough to end point, stop drawing.
		if (currentPosition.getDistance(endPoint) < Robot.DEAD_BAND) {
			LCD.drawString("DEADBAND", 0, 4);
			return false;
		}

		Coord targetCoord = endPoint; // by default target is end of line

		/**
		 * If the distance between pen and end point is more than increment;
		 * 
		 * A simplified implementation of "pure pursuit algorithm" is used.
		 * Based on : https://github.com/tuxbotix/capstone_bumblebee_due/blob
		 * /master/trajectory.ino
		 * 
		 * 1. Perpendicular to the line is drawn from robot's current position.
		 * 
		 * 2. The intersecting point of perpendicular and line = intercept 3.
		 * Using trigonometry, get point ahead of this intercept = target 4.
		 * Instruct robot to reach this point.
		 * 
		 * Note: moveTo will fail (return false) if arm has to be extended
		 * beyond limits, will immidiately end execution.
		 */

		if (distanceToEnd > Robot.DISTANCE_INCREMENT) {

			Coord intercept = getNormalInteceptingPoint(currentPosition);

			double targetX = intercept.x() + Robot.DISTANCE_INCREMENT
					* Math.cos(slopeAngle);
			double targetY = intercept.y() + Robot.DISTANCE_INCREMENT
					* Math.sin(slopeAngle);

			targetCoord = new Coord(targetX, targetY);
			// set speeds so that the pen's motion will be close to the line's
			// path
//			PlotbotControl.getInstance().setActuatorSpeeds(currentPosition,
//					targetCoord);
		}
		//try to move to the new target. If fail return false
		if (!PlotbotControl.getInstance().moveTo(targetCoord, true)) {
			LCD.drawString("traj f" + (int) targetCoord.x() + " "
					+ (int) targetCoord.y(), 0, 4);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Distance from a point to a line
	 * 
	 * @param curPosition
	 * @return
	 */
	public double normalDistanceToLine(Coord curPosition) {
		Coord intercept = getNormalInteceptingPoint(curPosition);
		return intercept.getDistance(curPosition);
	}

	/**
	 * A normal is drawn to the line from a point. This method find the point
	 * where this normal and line intersect
	 * 
	 * Uses vector dot product.
	 * 
	 * @param curPosition
	 *            the point which the normal will be drawn to the line
	 * @return intersecting point on the line.
	 */
	private Coord getNormalInteceptingPoint(Coord curPosition) {
		double x1 = startPoint.x();
		double x2 = endPoint.x();
		double y1 = startPoint.y();
		double y2 = endPoint.y();

		double dx = x2 - x1;
		double dy = y2 - y1;
		dx /= length; // cos theta
		dy /= length;// sin theta
		// translate the point and get the dot product
		double lambda = (dx * (curPosition.x() - x1))
				+ (dy * (curPosition.y() - y1));
		return new Coord(((dx * lambda) + x1), ((dy * lambda) + y1));
	}
	
	/**
	 * Translate a line by given X and Y values
	 * 
	 * @param x
	 * @param y
	 */
	public void translate(double x, double y){
		this.startPoint.translate(x, y);
		this.endPoint.translate(x, y);
		length = Coord.getDistance(startPoint, endPoint);
		slopeAngle = Math.atan2((endPoint.y() - startPoint.y()), (endPoint.x() - startPoint.x()));		
	}
}