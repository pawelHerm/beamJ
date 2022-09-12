package chloroplastInterface;

import java.util.Objects;

public class PhaseRemainder implements Phase
{
    private final double duration;
    private final StandardTimeUnit durationUnit;

    private final double originalPhaseDurationInMiliseconds;

    public PhaseRemainder(double duration, StandardTimeUnit durationUnit, double originalPhaseDurationInMiliseconds)
    {
        this.duration = duration;
        this.durationUnit = durationUnit;
        this.originalPhaseDurationInMiliseconds = originalPhaseDurationInMiliseconds;
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

    public double getOriginalPhaseDurationInMiliseconds()
    {
        return originalPhaseDurationInMiliseconds;
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
        result = 31*result + Double.hashCode(this.originalPhaseDurationInMiliseconds);

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof PhaseRemainder)
        {
            PhaseRemainder that = (PhaseRemainder)o;
            boolean equal = Objects.equals(this.durationUnit, that.durationUnit);
            equal = equal && (Double.compare(this.duration, that.duration) == 0);
            equal = equal && (Double.compare(this.originalPhaseDurationInMiliseconds, that.originalPhaseDurationInMiliseconds) == 0);

            return equal;
        }

        return false;
    }
}