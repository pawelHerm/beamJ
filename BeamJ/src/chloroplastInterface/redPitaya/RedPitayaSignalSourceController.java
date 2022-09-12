package chloroplastInterface.redPitaya;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import atomicJ.geometricSets.RealSet;
import chloroplastInterface.SignalSource;
import chloroplastInterface.SignalSourceController;

public class RedPitayaSignalSourceController implements SignalSourceController 
{    
    private final RedPitayaLockInDevice device;

    public RedPitayaSignalSourceController(RedPitayaLockInDevice redPitayaLockInDevice) 
    {
        this.device = redPitayaLockInDevice;
    }

    public RedPitayaLockInDevice getDevice()
    {
        return device;
    }

    @Override
    public double getMaximalSupportedFrequencyInHertz()
    {
        return this.device.getMaximalSupportedFrequencyInHertz();
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
                if(this.device.isFrequencySupported(val))
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
        boolean supported = this.device.isFrequencySupported(desiredFrequencyInHertz);
        return supported;
    }

    @Override
    public boolean isSetOfSupportedFrequenciesDiscrete()
    {
        return true;
    }

    @Override
    public List<Double> getListOfSupportedDiscreteFrequenciesInHertzInAscendingOrder()
    {
        return this.device.getListOfSupportedFrequenciesInHertzInAscendingOrder();
    }

    @Override
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz)
    {        
        double closestFrequency = this.device.getClosestSupportedFrequency(desiredFrequencyInHertz);
        return closestFrequency;
    }

    @Override
    public double getPreferredFrequencyIncrement(double currentFrequency)
    {
        return this.device.getPreferredFrequencyIncrement(currentFrequency);
    }

    @Override
    public double getPreferredFrequencyDecrement(double currentFrequency)
    {
        return this.device.getPreferredFrequencyDecrement(currentFrequency);
    }

    @Override
    public String toString()
    {
        String name = this.device.getIdentifier();
        return name;
    }

    @Override
    public String getUniqueDescription()
    {
        String id = this.device.getIdentifier();
        return id;
    }

    @Override
    public double getMaximalSignalSamplingRateInHertz()
    {
        return this.device.getMaximalSignalSamplingRateInHertz();
    }

    @Override
    public RealSet getSupportedFrequencies() 
    {
        return this.device.getSupportedFrequencies();
    }

    @Override
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double expectedCallCountPerSecond) 
    {
        return device;
    }

    @Override
    public SignalSource getSignalSource(int numberOfSamplesToAverage, double minExpectedVoltage, double maxExpectedVoltage, double expectedCallCountPerSecond) 
    {
        return device;
    }

    @Override
    public boolean requiresDevice(String deviceIdentifier)
    {
        boolean required = Objects.equal(this.device.getIdentifier(), deviceIdentifier);
        return required;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound()
    {
        boolean replace = !device.isFunctional();
        return replace;
    }

    @Override
    public boolean isFunctional() {
        return device.isFunctional();
    }  
}
