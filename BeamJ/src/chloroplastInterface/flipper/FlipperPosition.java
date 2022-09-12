package chloroplastInterface.flipper;

public enum FlipperPosition
{
    FIRST(1, true), SECOND(2, true), UNKNOWN(0, false);

    private final int positionCode;
    private final boolean known;

    FlipperPosition(int positionCode, boolean known)
    {
        this.positionCode = positionCode;
        this.known = known;
    }

    public FlipperPosition getNextPosition()
    {
        int nextCode = this.positionCode == 1 ? 2 : 1;
        FlipperPosition nextPosition = FlipperPosition.getThorlabsFlipperPosition(nextCode);
        return nextPosition;
    }

    public int getCode()
    {
        return positionCode;
    }

    public boolean isKnown()
    {
        return known;
    }

    public static FlipperPosition[] getKnownPositions()
    {
        return new FlipperPosition[] {FIRST,SECOND};
    }


    public static FlipperPosition getThorlabsFlipperPosition(int code)
    {
        for(FlipperPosition position : FlipperPosition.values())
        {
            if(code == position.positionCode)
            {
                return position;
            }
        }

        return UNKNOWN;
    }
}