package de.tuhh.diss.plotbot.geometry;

import de.tuhh.diss.plotbot.lowerLayer.PlotbotControl;

/**
 * Interface marking a shape which can be plotted by the Plotbot.
 * 
 */
public interface Plottable {
	public void plot(PlotbotControl pc);
}
