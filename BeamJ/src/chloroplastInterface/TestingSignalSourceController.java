package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.Validation;

public class TestingSignalSourceController implements SignalSourceController 
{
    private final static String DESCRIPTION = "Test";
    private final static TestingSignalSourceController INSTANCE = new TestingSignalSourceController();

    private TestingSignalSourceController(){};

    public static TestingSignalSourceController getInstance()
    {
        return INSTANCE;
    }

    @Override
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double expectedCallCountPerSecond)
    {
        return DummySignalSource.getInstance();
    }

    @Override
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double minExpectedVoltage,double maxExpectedVoltage, double expectedCallCountPerSecond) {
        return DummySignalSource.getInstance();
    }

    @Override
    public double getMaximalSupportedFrequencyInHertz()
    {
        return Double.MAX_VALUE;
    }

    @Override
    public double getMaximalSignalSamplingRateInHertz() 
    {
        return DummySignalSource.getInstance().getMaximalSignalSamplingRateInHertz();
    }

    @Override
    public String getUniqueDescription() {
        return DESCRIPTION;
    }

    @Override
    public String toString()
    {
        return DESCRIPTION;
    }

    @Override
    public List<Double> selectSupportedDiscreteFrequencies(List<Double> proposedFrequencies)
    {
        List<Double> supportedFrequencies = new ArrayList<>();

        for(Double freq : proposedFrequencies)
        {
            if(freq !=null)
            {
                double val = freq.doubleValue();
                if(!Double.isNaN(val) && Double.isFinite(val) && val >= 0)
                {
                    supportedFrequencies.add(freq);
                }
            }
        }

        return supportedFrequencies;
    }


    @Override
    public boolean isFrequencySupported(double desiredFrequencyInHertz) 
    {
        boolean supported = desiredFrequencyInHertz >= 0 && desiredFrequencyInHertz <= Double.MAX_VALUE;//cannot be infinite
        return supported;
    }

    @Override
    public double getPreferredFrequencyIncrement(double currentFrequency)
    {
        Validation.requireNonNegativeParameterName(currentFrequency, "currentFrequency");
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, Double.MAX_VALUE, "currentFrequency");

        double incrementedFrequency = Math.min(Double.MAX_VALUE, currentFrequency + 1);
        double increment = incrementedFrequency - currentFrequency;
        return increment;
    }

    @Override
    public double getPreferredFrequencyDecrement(double currentFrequency)
    {
        Validation.requireNonNegativeParameterName(currentFrequency, "currentFrequency");
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, Double.MAX_VALUE, "currentFrequency");

        double frequencyAfterDecrement = Math.max(0., currentFrequency - 1);
        double decrement = currentFrequency - frequencyAfterDecrement;
        return decrement;
    }

    @Override
    public boolean isSetOfSupportedFrequenciesDiscrete()
    {
        return false;
    }

    @Override
    public List<Double> getListOfSupportedDiscreteFrequenciesInHertzInAscendingOrder()
    {
        return Collections.emptyList();
    }

    @Override
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz) 
    {
        //using Math.min makes sense, because the frequency must be smaller than Double.INFINITY
        double supportedFrequency = Math.min(Double.MAX_VALUE, Math.max(0, desiredFrequencyInHertz));
        return supportedFrequency;
    }

    @Override
    public RealSet getSupportedFrequencies() 
    {
        RealSet supportedFrequencies = new ClosedInterval(0, Double.MAX_VALUE);
        return supportedFrequencies;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() 
    {
        return false;
    }

    @Override
    public boolean isFunctional() {
        return true;
    }

    @Override
    public boolean requiresDevice(String deviceIdentifier) 
    {
        return false;
    }
}
