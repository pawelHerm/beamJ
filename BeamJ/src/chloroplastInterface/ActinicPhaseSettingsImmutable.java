package chloroplastInterface;

import java.util.Objects;

import chloroplastInterface.optics.SliderMountedFilter;

public final class ActinicPhaseSettingsImmutable implements ActinicPhase
{
    private final double duration;
    private final StandardTimeUnit durationUnit;
    private final double intensityInPercent;
    private final SliderMountedFilter filter;

    public ActinicPhaseSettingsImmutable(double duration, StandardTimeUnit durationUnit, double intensityInPercent, SliderMountedFilter filter)
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

    @Override
    public StandardTimeUnit getDurationUnit()
    {
        return durationUnit;
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

    @Override
    public double getDurationInMiliseconds()
    {
        double durationInMiliseconds = duration*durationUnit.getConversionFactorToMilliseconds();

        return durationInMiliseconds;
    }   

    @Override
    public double getBeamIntensityInPercent()
    {
        return intensityInPercent;
    }

    public SliderMountedFilter getFilter()
    {
        return filter;
    }

    @Override
    public boolean isInstantenous()
    {
        boolean instant = (duration == 0);
        return instant;
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hashCode(this.durationUnit);
        result = 31*result + Double.hashCode(this.duration);
        result = 31*result + Double.hashCode(this.intensityInPercent);
        result = 31*result + Objects.hashCode(this.filter);

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof ActinicPhaseSettingsImmutable)
        {
            ActinicPhaseSettingsImmutable that = (ActinicPhaseSettingsImmutable)o;
            boolean equal = Objects.equals(this.durationUnit, that.durationUnit);
            equal = equal && (Double.compare(this.duration, that.duration) == 0);
            equal = equal && (Double.compare(this.intensityInPercent, that.intensityInPercent) == 0);
            equal = equal && Objects.equals(this.filter, that.filter);

            return equal;
        }

        return false;
    }
}