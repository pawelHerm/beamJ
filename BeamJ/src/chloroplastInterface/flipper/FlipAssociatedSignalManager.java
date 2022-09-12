package chloroplastInterface.flipper;

public interface FlipAssociatedSignalManager
{
    public boolean isSendSignalAfterFlip(FlipperPosition position);
    public double getSignalDurationInMilliseconds(FlipperPosition currentPosition);
    public double getLagTimeInMilliseconds(FlipperPosition position);
    public void triggerSignal(FlipperPosition currentPosition);
    public void endSignal(FlipperPosition currentPosition);
}