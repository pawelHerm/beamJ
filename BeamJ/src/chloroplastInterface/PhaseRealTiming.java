package chloroplastInterface;

public class PhaseRealTiming
{
    private final int phaseIndex;
    private final long intendedDurationInMiliseconds;
    private final long beginningTime;
    private long endTime = -1;

    public PhaseRealTiming(int phaseIndex, long intendedDurationInMiliseconds, long timeOfBeginning)
    {
        this.phaseIndex = phaseIndex;
        this.intendedDurationInMiliseconds = intendedDurationInMiliseconds;
        this.beginningTime = timeOfBeginning;
    }

    public int getPhaseIndex()
    {
        return phaseIndex;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public long getIntendedDurationInMiliseconds()
    {
        return intendedDurationInMiliseconds;
    }

    public long getRealDurationInMiliseconds()
    {
        if(endTime == -1)
        {
            return -1;
        }

        long realDuration = endTime - beginningTime;

        return realDuration;
    }

    public long getDurationMismatchInMiliseconds()
    {
        if(endTime == -1)
        {
            return -1;
        }

        long mismatch = (endTime - beginningTime) - intendedDurationInMiliseconds;

        return mismatch;
    }

    protected long getMismatchNonNeqative()
    {
        if(endTime == -1)
        {
            return -1;
        }

        long mismatchNonNegative = Math.max(intendedDurationInMiliseconds - (endTime - beginningTime), 0);
        return mismatchNonNegative;
    }

    public PhaseRemainder getMismatchPhase()
    {
        long mismatchNonNegative = getMismatchNonNeqative();

        if(mismatchNonNegative < 0)
        {
            return null;
        }

        PhaseRemainder mismatchPhase = new PhaseRemainder(mismatchNonNegative, StandardTimeUnit.MILISECOND,intendedDurationInMiliseconds);

        return mismatchPhase;
    }

    //this method should be used if the ActinicBeam
    public PhaseRemainder getMismatchPhase(double newPhaseDurationInMiliseconds, long furtherIncreaseInPhaseLength)
    {
        if(endTime == -1)
        {
            return null;
        }

        long mismatchNonNegative = Math.max(intendedDurationInMiliseconds - (endTime - beginningTime) + furtherIncreaseInPhaseLength, 0);
        PhaseRemainder mismatchPhase = new PhaseRemainder(mismatchNonNegative, StandardTimeUnit.MILISECOND, newPhaseDurationInMiliseconds);

        return mismatchPhase;
    }
}