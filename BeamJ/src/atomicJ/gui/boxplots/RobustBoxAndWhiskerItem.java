package atomicJ.gui.boxplots;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jfree.util.ObjectUtilities;

public class RobustBoxAndWhiskerItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double mean;
    private final double median;
    private final double q1;
    private final double q3;
    private final double minRegularValue;
    private final double maxRegularValue;
    private final double minValue;
    private final double maxValue;

    private final List<Double> outliers;


    public RobustBoxAndWhiskerItem(double mean, double median,
            double q1, double q3,
            double minRegularValue,double maxRegularValue,
            double minValue, double maxValue,
            List<Double> outliers) {

        this.mean = mean;
        this.median = median;
        this.q1 = q1;
        this.q3 = q3;
        this.minRegularValue = minRegularValue;
        this.maxRegularValue = maxRegularValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.outliers = outliers;
    }

    public double getMean() {
        return this.mean;
    }

    public double getMedian() {
        return this.median;
    }


    public double getQ1() {
        return this.q1;
    }


    public double getQ3() {
        return this.q3;
    }

    public double getMinRegularValue() {
        return this.minRegularValue;
    }

    public double getMaxRegularValue() {
        return this.maxRegularValue;
    }


    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public List<Double> getOutliers() {
        if (this.outliers == null) {
            return null;
        }
        return Collections.unmodifiableList(this.outliers);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RobustBoxAndWhiskerItem)) {
            return false;
        }
        RobustBoxAndWhiskerItem that = (RobustBoxAndWhiskerItem) obj;
        if (!ObjectUtilities.equal(this.mean, that.mean)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.median, that.median)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.q1, that.q1)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.q3, that.q3)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.minRegularValue,
                that.minRegularValue)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.maxRegularValue,
                that.maxRegularValue)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.minValue, that.minValue)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.maxValue, that.maxValue)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.outliers, that.outliers)) {
            return false;
        }
        return true;
    }

}

