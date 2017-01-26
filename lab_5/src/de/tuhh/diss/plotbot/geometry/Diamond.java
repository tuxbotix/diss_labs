package de.tuhh.diss.plotbot.geometry;

import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;

public class Diamond implements Plottable {

	Rectangle boundingBox;

	public Diamond(Coord bl, Coord tr) {
		boundingBox = new Rectangle(bl, tr);
	}

	@Override
	public void plot(PlotbotControl pc) {
		double xMid = (boundingBox.getTopRight().x() - boundingBox
				.getBottomLeft().x()) / 2;
		double yMid = (boundingBox.getBottomLeft().y() - boundingBox
				.getTopRight().y()) / 2;

		Coord top = new Coord(xMid, boundingBox.getTopRight().y());
		Coord right = new Coord(yMid, boundingBox.getTopRight().x());
		Coord bottom = new Coord(xMid, boundingBox.getBottomLeft().y());
		Coord left = new Coord(yMid, boundingBox.getBottomLeft().x());

		// clockwise
		Line topLine = new Line(top, right);
		Line rightLine = new Line(right, bottom);
		Line bottomLine = new Line(bottom, left);
		Line leftLine = new Line(left, top);

		pc.movePen(true);
		topLine.plot(pc);
		rightLine.plot(pc);
		bottomLine.plot(pc);
		leftLine.plot(pc);
		pc.movePen(false);
	}

}
