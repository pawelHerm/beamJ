package chloroplastInterface;

import java.io.Serializable;

import atomicJ.utilities.Validation;
import chloroplastInterface.optics.NullFilter;
import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicPhaseAutomaticCalibration implements Serializable
{
    private static final double DEFAULT_MIN_INTENSITY_IN_PERCENT = 1;
    private static final double DEFAULT_MAX_INTENSITY_IN_PERCENT = 50;

    private static final StandardTimeUnit DEFAULT_TIME_UNIT = StandardTimeUnit.SECOND;
    private static final double DEFAULT_STEP_RECORDING_TIME_VALUE = 10;
    private static final double DEFAULT_PAUSE_BETWEEN_STEPS_VALUE = 10;

    private static final int DEFAULT_STEP_COUNT = 20;

    private static final long serialVersionUID = 1L;

    private double minimumIntensityInPercent = DEFAULT_MIN_INTENSITY_IN_PERCENT;
    private double maximumIntensityInPercent = DEFAULT_MAX_INTENSITY_IN_PERCENT;

    private double stepRecordingTimeValue = DEFAULT_STEP_RECORDING_TIME_VALUE;
    private StandardTimeUnit stepRecordingTimeUnit = DEFAULT_TIME_UNIT;
    private double pauseBetweenStepsValue = DEFAULT_PAUSE_BETWEEN_STEPS_VALUE;
    private StandardTimeUnit pauseBetweenStepsUnit = DEFAULT_TIME_UNIT;

    private ScaleType stepScaleType = ScaleType.LINEAR;
    private int stepCount = DEFAULT_STEP_COUNT;

    private SliderMountedFilter filter = new SliderMountedFilter(0, NullFilter.getInstance());

    public ActinicPhaseAutomaticCalibration()
    {}

    public ActinicPhaseAutomaticCalibration(ActinicPhaseAutomaticCalibration that)
    {
        this.minimumIntensityInPercent = that.minimumIntensityInPercent;
        this.maximumIntensityInPercent = that.maximumIntensityInPercent;

        this.stepRecordingTimeValue = that.stepRecordingTimeValue;
        this.stepRecordingTimeUnit = that.stepRecordingTimeUnit;
        this.pauseBetweenStepsValue = that.pauseBetweenStepsValue;
        this.pauseBetweenStepsUnit = that.pauseBetweenStepsUnit;

        this.stepScaleType = that.stepScaleType;
        this.stepCount = that.stepCount;

        this.filter = that.filter;
    }

    public double getMaximumIntensityInPercent()
    {
        return maximumIntensityInPercent;
    }

    public void setMaximumIntensityInPercent(double maximumIntensityInPercent)
    {
        this.maximumIntensityInPercent = Validation.requireValueEqualToOrBetweenBounds(maximumIntensityInPercent, 0., 100., "Maximum intensity");
    }

    public double getMinimumIntensityInPercent()
    {
        return minimumIntensityInPercent;
    }

    public void setMinimumIntensityInPercent(double minimumIntensityInPercent)
    {
        this.minimumIntensityInPercent = Validation.requireValueEqualToOrBetweenBounds(minimumIntensityInPercent, 0., 100., "Minimum intensity");
    }   

    public double getStepRecodingTime()
    {
        return stepRecordingTimeValue;
    }

    public void setStepRecordingTime(double stepRecordingTime)
    {
        this.stepRecordingTimeValue = Validation.requireValueGreaterThan(stepRecordingTime, 0);
    }

    public StandardTimeUnit getStepRecordingTimeUnit()
    {
        return stepRecordingTimeUnit;
    }

    public void setStepRecordingTimeUnit(StandardTimeUnit stepRecordingTimeUnit)
    {
        this.stepRecordingTimeUnit = Validation.requireNonNullParameterName(stepRecordingTimeUnit, "stepRecordingTimeUnit");
    }

    public double getPauseBetweenSteps()
    {
        return pauseBetweenStepsValue;
    }

    public void setPauseBetweenStops(double pauseBetweenSteps)
    {
        this.pauseBetweenStepsValue = Validation.requireValueGreaterOrEqualTo(pauseBetweenSteps, 0);
    }

    public StandardTimeUnit getPauseBetweenStepsUnit()
    {
        return pauseBetweenStepsUnit;
    }

    public void setPauseBetweenStopsUnit(StandardTimeUnit pauseBetweenStepsValueUnit)
    {
        this.pauseBetweenStepsUnit = Validation.requireNonNullParameterName(pauseBetweenStepsValueUnit, "pauseBetweenStepsValueUnit");
    }

    public ScaleType getStepScaleType()
    {
        return stepScaleType;
    }

    public void setStepScaleType(ScaleType scaleType)
    {
        this.stepScaleType = Validation.requireNonNullParameterName(scaleType, "scaleType");
    }

    public int getStepCount()
    {
        return stepCount;
    }

    public void setStepCount(int stepCount)
    {
        this.stepCount = Validation.requireValueGreaterThan(stepCount, 0);
    }

    public SliderMountedFilter getFilter()
    {
        return filter;
    }

    public void setFilter(SliderMountedFilter filter)
    {
        this.filter = Validation.requireNonNullParameterName(filter,"filter");
    }

    public boolean isWellSpecified()
    {
        boolean wellSpecified = !Double.isNaN(maximumIntensityInPercent) && !Double.isNaN(minimumIntensityInPercent) 
                && !Double.isNaN(stepRecordingTimeValue) && !Double.isNaN(pauseBetweenStepsValue);
        return wellSpecified;
    }

    public ActinicCalibrationPointImmutable getImmutableCopy()
    {
        ActinicCalibrationPointImmutable immutableCopy = new ActinicCalibrationPointImmutable(minimumIntensityInPercent, maximumIntensityInPercent, filter);
        return immutableCopy;
    }
}