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
		Coord right = new Coord(boundingBox.getTopRight().x(), yMid);
		Coord bottom = new Coord(xMid, boundingBox.getBottomLeft().y());
		Coord left = new Coord(boundingBox.getBottomLeft().x(), yMid);

		// clockwise
		Line topRightLine = new Line(top, right);
		Line rightBottomLine = new Line(right, bottom);
		Line bottomLeftLine = new Line(bottom, left);
		Line leftTopLine = new Line(left, top);

		pc.moveTo(top);
//		pc.movePen(true);
		topRightLine.plot(pc);
		rightBottomLine.plot(pc);
		bottomLeftLine.plot(pc);
		leftTopLine.plot(pc);
//		pc.movePen(false);
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}
}
