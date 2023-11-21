package org.ta4j.core;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.Objects;

/**
 * 波浪指标
 * MA Wave  (MAWave) indicator.
 *  @see <a href=
 *        "https://gitee.com/dromara/northstar/blob/master/northstar-api/src/main/java/org/dromara/northstar/indicator/wave/MABasedWaveIndicator.java">
 */
public class MAWaveIndicator extends CachedIndicator<Num> {

    private int numOfBarToConfirmTheSegment;

    private EndpointType type;

    private Boolean isGoingUp;

    private Double sectionMax;
    private Double sectionMin;

    private HighPriceIndicator high;
    private LowPriceIndicator low;
    private Indicator maLine;
    private ClosePriceIndicator close;

    public MAWaveIndicator(BarSeries series, Indicator<Num> maLine, int numOfBarToConfirmTheSegment,
                           EndpointType type) {
            super(series);
            this.numOfBarToConfirmTheSegment = numOfBarToConfirmTheSegment;
            this.type = type;
            this.maLine = maLine;
            this.close = new ClosePriceIndicator(series);
            this.high = new HighPriceIndicator(series);
            this.low = new LowPriceIndicator(series);
            this.maLine = maLine;
        //Assert.isTrue(maLine.getUnstableBars() > numOfBarToConfirmTheSegment, "可回溯长度不足以确定波浪");
      //  Assert.isTrue(cfg.cacheLength() > numOfBarToConfirmTheSegment, "可回溯长度不足以确定波浪");
    }

    @Override
    public int getUnstableBars() {
        return this.numOfBarToConfirmTheSegment;
    }

    @Override
    protected Num calculate(int index) {
        if (maLine.getUnstableBars()==0) {
            return NaN.NaN;
        }
        // 各关键值的初始化
        if (Objects.isNull(isGoingUp))
            isGoingUp = close.getValue(index).isGreaterThan((Num) maLine.getValue(index));
        if (Objects.isNull(sectionMax))
            sectionMax = type.equals(EndpointType.CLOSE) ? close.getValue(index).doubleValue() : high.getValue(index).doubleValue();
        if (Objects.isNull(sectionMin))
            sectionMin = type.equals(EndpointType.CLOSE) ? close.getValue(index).doubleValue() : low.getValue(index).doubleValue();

        // 记录波段最大值
        if (isGoingUp) {
            sectionMax = type == EndpointType.CLOSE
                    ? Math.max(sectionMax, close.getValue(index).doubleValue())
                    : Math.max(sectionMax, high.getValue(index).doubleValue());
        } else {
            sectionMin = type == EndpointType.CLOSE
                    ? Math.min(sectionMin, close.getValue(index).doubleValue())
                    : Math.min(sectionMin, low.getValue(index).doubleValue());
        }

        if (isGoingUp && close.getValue(-numOfBarToConfirmTheSegment).isGreaterThan((Num) maLine.getValue(-numOfBarToConfirmTheSegment))) {
            boolean isValidTurnAround = true;
            for (int i = 0; i < numOfBarToConfirmTheSegment; i++) {
                if (close.getValue(-i).isGreaterThan((Num)maLine.getValue(-i))) {
                    isValidTurnAround = false;
                    break;
                }
            }
            if(isValidTurnAround) {
                isGoingUp = false;
                sectionMin = sectionMax;	// 重置最小值
                return DoubleNum.valueOf(sectionMax);
            }
        }


        if (!isGoingUp && close.getValue(-numOfBarToConfirmTheSegment).isLessThan((Num) maLine.getValue(-numOfBarToConfirmTheSegment))) {
            boolean isValidTurnAround = true;
            for (int i = 0; i < numOfBarToConfirmTheSegment; i++) {
                if (close.getValue(-i).isLessThan((Num) maLine.getValue(-i))) {
                    isValidTurnAround = false;
                    break;
                }
            }
            if(isValidTurnAround) {
                isGoingUp = true;
                sectionMax = sectionMin; 	// 重置最大值
                return DoubleNum.valueOf(sectionMin);
            }
        }
        return NaN.NaN;
    }

    public enum EndpointType {
        CLOSE, HIGH_LOW;
    }
}
