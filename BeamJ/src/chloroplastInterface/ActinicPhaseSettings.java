package chloroplastInterface;

import java.io.Serializable;
import java.util.Objects;

import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicPhaseSettings implements ActinicPhase, Serializable
{
    private static final long serialVersionUID = 2L;

    private double duration;
    private StandardTimeUnit durationUnit;
    private double intensityInPercent;
    private SliderMountedFilter filter;

    public ActinicPhaseSettings(double duration, StandardTimeUnit durationUnit, double intensityInPercent, SliderMountedFilter filter)
    {
        this.duration = duration;
        this.durationUnit = durationUnit;
        this.intensityInPercent = intensityInPercent;
        this.filter = filter;
    }

    @Override
    public double getDuration()
    {
        return duration;
    }

    public void setDuration(double duration)
    {
        this.duration = duration;
    }

    @Override
    public StandardTimeUnit getDurationUnit()
    {
        return durationUnit;
    }

    @Override
    public double getDurationInMiliseconds()
    {
        double durationInMiliseconds = duration*durationUnit.getConversionFactorToMilliseconds();

        return durationInMiliseconds;
    }

    @Override
    public double getDuration(StandardTimeUnit unit)
    {
        if(Objects.equals(this.durationUnit, unit))
        {
            return duration;
        }
        double conversionFactor = this.durationUnit.getConversionFactorTo(unit);
        double durationInNewUnit = this.duration*conversionFactor;
        return durationInNewUnit;
    }

    public void setDurationUnit(StandardTimeUnit durationUnit)
    {
        this.durationUnit = durationUnit;
    }

    @Override
    public double getBeamIntensityInPercent()
    {
        return intensityInPercent;
    }

    public void setBeamIntensityInPercent(double intensityInPercent)
    {
        this.intensityInPercent = intensityInPercent;
    }

    public SliderMountedFilter getSliderFilter()
    {
        return filter;
    }

    public void setFilter(SliderMountedFilter filter)
    {
        this.filter = filter;
    }

    @Override
    public boolean isInstantenous()
    {
        boolean instant = (duration == 0);
        return instant;
    }

    public ActinicPhaseSettingsImmutable getImmutableCopy()
    {
        return new ActinicPhaseSettingsImmutable(duration, durationUnit, intensityInPercent, filter);
    }
}