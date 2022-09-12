package chloroplastInterface;

public interface SignalSource
{
    public void initializeIfNecessary() throws IllegalStateException;
    public RawVoltageSample getSample();
    public void finishReading();
    public double getMaximalSignalSamplingRateInHertz();
    public void informAboutFrequency(double desiredFrequencyInHertz) throws IllegalStateException;
    public boolean isFrequencySupported(double desiredFrequencyInHertz);
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz);
}