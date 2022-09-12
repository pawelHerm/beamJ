package chloroplastInterface;


public final class ActinicPhaseRemainder extends PhaseRemainder implements ActinicPhase
{
    private final double lightIntensityInPercents;

    public ActinicPhaseRemainder(double duration, StandardTimeUnit durationUnit, double lightIntensityInPercents, double originalPhaseDurationInMiliseconds)
    {
        super(duration, durationUnit, originalPhaseDurationInMiliseconds);
        this.lightIntensityInPercents = lightIntensityInPercents;
    }

    @Override
    public double getBeamIntensityInPercent()
    {
        return lightIntensityInPercents;
    }


    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31*result + Double.hashCode(this.lightIntensityInPercents);

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof ActinicPhaseRemainder)
        {
            ActinicPhaseRemainder that = (ActinicPhaseRemainder)o;
            boolean equal = super.equals(o);
            equal = equal && (Double.compare(this.lightIntensityInPercents, that.lightIntensityInPercents) == 0);

            return equal;
        }

        return false;
    }
}