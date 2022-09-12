package atomicJ.functions;

public enum MonotonicityType
{
    STRICTLY_INCREASING(true, true, true), WEAKLY_INCREASING(true, true,false), WEAKLY_DECREASING(true, false,false), STRICTLY_DECREASING(true, false, true), NON_MONOTONIC(false, false, false);

    private final boolean monotonic;
    private final boolean increasing;
    private final boolean strict;

    MonotonicityType(boolean monotonic, boolean increasing, boolean strict)
    {
        if(!monotonic && increasing)
        {
            throw new IllegalArgumentException("Non-monotonic function cannot be monotonicallly increasing");
        }

        if(!monotonic && strict)
        {
            throw new IllegalArgumentException("Non-monotonic function cannot be strictly monotonic");
        }

        this.monotonic = monotonic;
        this.increasing = increasing;
        this.strict = strict;
    }

    public boolean isMonotonic()
    {
        return monotonic;
    }

    public boolean isMonotonicallyIncreasing()
    {
        return increasing;
    }

    public boolean isStrictlyMonotonic()
    {
        return strict;
    }
}
