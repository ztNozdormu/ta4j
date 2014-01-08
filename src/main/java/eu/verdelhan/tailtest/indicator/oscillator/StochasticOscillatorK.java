package eu.verdelhan.tailtest.indicator.oscillator;

import eu.verdelhan.tailtest.Indicator;
import eu.verdelhan.tailtest.TimeSeries;
import eu.verdelhan.tailtest.indicator.helper.HighestValue;
import eu.verdelhan.tailtest.indicator.helper.LowestValue;
import eu.verdelhan.tailtest.indicator.simple.ClosePrice;
import eu.verdelhan.tailtest.indicator.simple.MaxPrice;
import eu.verdelhan.tailtest.indicator.simple.MinPrice;
import java.math.BigDecimal;

/**
 * 
 * Receives timeSeries and timeFrame and calculates the StochasticOscillatorK
 * over ClosePrice, or receives an indicator, MaxPrice and
 * MinPrice and returns StochasticOsiclatorK over this indicator.
 * 
 */
public class StochasticOscillatorK implements Indicator<Double> {
	private final Indicator<? extends Number> indicator;

	private final int timeFrame;

	private MaxPrice maxPriceIndicator;

	private MinPrice minPriceIndicator;

	public StochasticOscillatorK(TimeSeries timeSeries, int timeFrame) {
		this(new ClosePrice(timeSeries), timeFrame, new MaxPrice(timeSeries), new MinPrice(
				timeSeries));
	}

	public StochasticOscillatorK(Indicator<? extends Number> indicator, int timeFrame,
			MaxPrice maxPriceIndicator, MinPrice minPriceIndicator) {
		this.indicator = indicator;
		this.timeFrame = timeFrame;
		this.maxPriceIndicator = maxPriceIndicator;
		this.minPriceIndicator = minPriceIndicator;
	}

	@Override
	public Double getValue(int index) {
		HighestValue<BigDecimal> highestHigh = new HighestValue<BigDecimal>(maxPriceIndicator, timeFrame);
		LowestValue<BigDecimal> lowestMin = new LowestValue<BigDecimal>(minPriceIndicator, timeFrame);

		BigDecimal highestHighPrice = highestHigh.getValue(index);
		BigDecimal lowestLowPrice = lowestMin.getValue(index);

		return ((indicator.getValue(index).doubleValue() - lowestLowPrice.doubleValue()) / (highestHighPrice.doubleValue() - lowestLowPrice.doubleValue())) * 100d;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName() + " timeFrame: " + timeFrame;
	}
}
