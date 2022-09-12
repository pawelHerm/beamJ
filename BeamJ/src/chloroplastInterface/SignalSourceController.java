package chloroplastInterface;

public interface SignalSourceController extends FrequencyDependentController
{
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double expectedCallCountPerSecond);
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double minExpectedVoltage, double maxExpectedVoltage, double expectedCallCountPerSecond);
    public double getMaximalSignalSamplingRateInHertz();
}
