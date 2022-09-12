package chloroplastInterface;

public class SignalSamplingSettingsImmutable 
{
    private final double signalSamplesPerMinute;
    private final String signalControllerIdentifier;

    public SignalSamplingSettingsImmutable(double signalSamplesPerMinute, String signalControllerIdentifier)
    {
        this.signalSamplesPerMinute = signalSamplesPerMinute;
        this.signalControllerIdentifier = signalControllerIdentifier;
    }

    public double getSignalSamplesPerMinute()
    {
        return signalSamplesPerMinute;
    }

    public String getSignalFactoryIdentifier()
    {
        return signalControllerIdentifier;
    }
}
