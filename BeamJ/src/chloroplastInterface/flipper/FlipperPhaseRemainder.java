package chloroplastInterface.flipper;

import java.util.Objects;

import chloroplastInterface.PhaseRemainder;
import chloroplastInterface.StandardTimeUnit;

public final class FlipperPhaseRemainder extends PhaseRemainder
{
    private final FlipperPosition flipperPosition;

    public FlipperPhaseRemainder(FlipperPosition flipperPosition, double duration, StandardTimeUnit durationUnit, double originalPhaseDurationInMiliseconds)
    {
        super(duration, durationUnit, originalPhaseDurationInMiliseconds);
        this.flipperPosition = flipperPosition;
    }

    public FlipperPhaseRemainder(FlipperPosition flipperPosition, PhaseRemainder remainder) 
    {
        this(flipperPosition, remainder.getDuration(), remainder.getDurationUnit(), remainder.getOriginalPhaseDurationInMiliseconds());
    }

    public FlipperPosition getFlipperPosition()
    {
        return flipperPosition;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31*result + Objects.hashCode(this.flipperPosition);

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof FlipperPhaseRemainder)
        {
            FlipperPhaseRemainder that = (FlipperPhaseRemainder)o;
            boolean equal = super.equals(that);
            equal = equal && (Objects.equals(this.flipperPosition, that.flipperPosition));

            return equal;
        }

        return false;
    }
}