package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;

import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.Validation;

public class NIDeviceSignalSourceController implements SignalSourceController 
{
    private final NIDevice niDevice;

    public NIDeviceSignalSourceController(NIDevice niDevice)
    {
        this.niDevice = niDevice;
    }

    public static List<NIDeviceSignalSourceController> buildFactoriesWhenPossible(List<NIDevice> devices)
    {
        List<NIDeviceSignalSourceController> factories = new ArrayList<>();

        for(NIDevice device : devices)
        {
            if(!device.isSimulated() && device.isProperlyConstructed())
            {
                factories.add(new NIDeviceSignalSourceController(device));
            }
        }

        return factories;
    }

    @Override
    public double getMaximalSupportedFrequencyInHertz()
    {
        return Double.MAX_VALUE;
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
                if(Double.isFinite(val) && val >= 0)
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
        boolean supported = Double.isFinite(desiredFrequencyInHertz) && desiredFrequencyInHertz >= 0;
        return supported;
    }

    @Override
    public RealSet getSupportedFrequencies() 
    {
        RealSet supportedFrequencies = new ClosedInterval(0, Double.MAX_VALUE);
        return supportedFrequencies;
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
        double supportedFrequency = Math.min(Double.MAX_VALUE, Math.max(0, desiredFrequencyInHertz));//frequency must be finite
        return supportedFrequency;
    }

    @Override
    public String toString()
    {
        String name = "NI " + niDevice.toString();
        return name;
    }

    @Override
    public String getUniqueDescription()
    {
        String name = "NI " + niDevice.toString();
        return name;
    }

    @Override
    public double getMaximalSignalSamplingRateInHertz()
    {
        return niDevice.getMaximalAnalogRateForSingleChannel();
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
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double expectedCallCountPerSecond) 
    {
        SignalSource source = new NIDAQmxSignalSource(niDevice, numberOfSamplesToAverage,  expectedCallCountPerSecond);
        return source;
    }

    @Override
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double minExpectedVoltage, double maxExpectedVoltage, double expectedCallCountPerSecond) 
    {
        SignalSource source = new NIDAQmxSignalSource(niDevice, 0, minExpectedVoltage, maxExpectedVoltage, numberOfSamplesToAverage,  expectedCallCountPerSecond);
        return source;
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
        boolean required = Objects.equal(niDevice.getIdentifier(), deviceIdentifier);
        return required;
    }
}
