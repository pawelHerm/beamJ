package chloroplastInterface;

public interface SignalReceiver 
{
    public void initializeIfNecessary() throws IllegalStateException;
    public void sendSample(double value);
}
