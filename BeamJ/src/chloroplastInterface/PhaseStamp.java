package chloroplastInterface;

import atomicJ.utilities.Validation;

public final class PhaseStamp
{
    public static final PhaseStamp IDLE_INSTANCE = new PhaseStamp(0,-1, -1);

    private final long currentPhaseOnsetAbsoluteTimeInMiliseconds;

    private final int finishedPhaseCount;
    private final int currentPhaseIndex;

    public PhaseStamp(int finishedPhaseCount, int currentPhaseIndex, long currentPhaseOnsetAbsoluteTimeInMiliseconds)
    {
        Validation.requireNonNegativeParameterName(finishedPhaseCount, "finishedPhaseCount");

        this.currentPhaseOnsetAbsoluteTimeInMiliseconds = currentPhaseOnsetAbsoluteTimeInMiliseconds;
        this.finishedPhaseCount = finishedPhaseCount;
        this.currentPhaseIndex = currentPhaseIndex;
    }

    public boolean isFirstPhaseOfRecording()    
    {
        boolean first = (this.currentPhaseIndex == 0);
        return first;
    }

    public int getFinishedPhaseCount()
    {
        return finishedPhaseCount;
    }

    public int getCurrentPhaseIndex()
    {
        return currentPhaseIndex;
    }

    public long getOnsetAbsoluteTimeInMiliseconds()
    {
        return currentPhaseOnsetAbsoluteTimeInMiliseconds;
    }

    @Override
    public int hashCode()
    {
        int result = Integer.hashCode(finishedPhaseCount);

        result = 31*result + Integer.hashCode(currentPhaseIndex);
        result = 31*result + Long.hashCode(currentPhaseOnsetAbsoluteTimeInMiliseconds);

        return result;
    }

    @Override
    public boolean equals(Object that)
    {
        if(that != null && that instanceof PhaseStamp)
        {
            PhaseStamp thatPhaseObject = (PhaseStamp)that;

            boolean equal = (this.finishedPhaseCount == thatPhaseObject.finishedPhaseCount) 
                    && (this.currentPhaseIndex == thatPhaseObject.currentPhaseIndex) && 
                    (this.currentPhaseOnsetAbsoluteTimeInMiliseconds == thatPhaseObject.currentPhaseOnsetAbsoluteTimeInMiliseconds);

            return equal;
        }
        else
        {
            return false;
        }
    }
}