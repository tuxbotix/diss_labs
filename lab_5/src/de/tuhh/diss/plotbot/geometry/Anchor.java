package de.tuhh.diss.plotbot.geometry;

import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;

public class Anchor implements Plottable{
	private final int ASPECT_RATIO =2; //width * 2 = height 

	private int height;
	private int width;
	private Coord topCenter;
	
	public Anchor(int width, Coord topCenter){
		this.width = width;
		height = width* ASPECT_RATIO;
		this.topCenter = topCenter;
	}
	
	/**
	 * Plot method
	 * 
	 */
	@Override
	public void plot(PlotbotControl pc){

		double leftX = topCenter.x() - width/2;// leftmost point
		double rightX = topCenter.x() + width/2; //rightmost point
		double diamondWidth = width/2;
		double diamoneLeftX=topCenter.x() - (diamondWidth/2);
		
		Line topLine = new Line(new Coord(leftX, topCenter.y()), new Coord(rightX, topCenter.y()));
		Line sideLineLeft = new Line(new Coord(leftX,topCenter.y()), new Coord(leftX,topCenter.y() - (height/ 5)) );
		Line sideLineRight = sideLineLeft;// copy left side line to right
		sideLineRight.translate(width, 0);// translate by width amount
		Line centerLine = new Line(topCenter, new Coord(topCenter.x(), topCenter.y() - (height - diamondWidth)));
		Line midHorizontalLine = new Line(new Coord(diamoneLeftX, topCenter.y() - height/2 ), new Coord(diamoneLeftX + diamondWidth, topCenter.y() - height/2));
		
		Diamond diamond = new Diamond(new Coord(diamoneLeftX, topCenter.y()-height), new Coord(diamoneLeftX + diamondWidth , topCenter.y() - height/2));
		
		sideLineLeft.plot(pc);
		topLine.plot(pc);
		sideLineRight.plot(pc);
		centerLine.plot(pc);
		midHorizontalLine.plot(pc);
		diamond.plot(pc);
		
		/**
		 * TODO TEST
		 */
	}
}
