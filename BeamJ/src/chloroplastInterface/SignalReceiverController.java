package chloroplastInterface;

public interface SignalReceiverController 
{
    public SignalReceiver getSignalReceiver();
    public SignalReceiver getSignalReceiver(double minOutVoltage, double maxOutVoltage);

    public boolean shouldBeReplacedWhenOtherControllerFound();
    public boolean isFunctional();

    public String getUniqueDescription();
}
