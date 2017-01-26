package de.tuhh.diss.plotbot.geometry;

import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;

public class Rectangle implements Plottable {

	Coord bottomLeft;
	Coord topRight;

	public Rectangle(Coord bl, Coord tr) {
		bottomLeft = bl;
		topRight = tr;
	}

	@Override
	public void plot(PlotbotControl pc) {
		Coord topLeft = new Coord(bottomLeft.x(), topRight.y());
		Coord bottomRight = new Coord(topRight.x(), bottomLeft.y());

		// clockwise
		Line topLine = new Line(topLeft, topRight);
		Line rightLine = new Line(topRight, bottomRight);
		Line bottomLine = new Line(bottomRight, bottomLeft);
		Line leftLine = new Line(bottomLeft, topLeft);

		pc.movePen(true);
		topLine.plot(pc);
		rightLine.plot(pc);
		bottomLine.plot(pc);
		leftLine.plot(pc);
		pc.movePen(false);
	}

	public double getWidth() {
		return Math.abs(getTopRight().x() - getBottomLeft().x());
	}

	public double getHeight() {
		return Math.abs(getBottomLeft().y() - getTopRight().y());
	}

	public Coord getBottomLeft() {
		return bottomLeft;
	}

	public Coord getTopRight() {
		return topRight;
	}
}
