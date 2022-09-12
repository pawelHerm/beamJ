package chloroplastInterface.redPitaya;


import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;
import com.google.common.base.Objects;

import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.Validation;
import chloroplastInterface.MeasuringBeamController;

public class RedPitayaMeasuringBeamController implements MeasuringBeamController
{
    private final RedPitayaLockInDevice device;

    public RedPitayaMeasuringBeamController(RedPitayaLockInDevice redPitayaDevice)
    {
        Validation.requireNonNullParameterName(redPitayaDevice, "redPitayaDevice");

        this.device = redPitayaDevice;
    }

    @Override
    public double getMaximalSupportedFrequencyInHertz() 
    {
        return device.getMaximalSupportedFrequencyInHertz();
    }

    @Override
    public RealSet getSupportedFrequencies()
    {
        return device.getSupportedFrequencies();
    }

    @Override
    public boolean isSoftwareControlOfMeasuringBeamIntensitySupported()
    {
        return false;
    }

    @Override
    public void sendMeasuringLightIntensity(double beamIntensityInPercent) 
    {
        throw new UnsupportedOperationException();
    }  

    @Override
    public void sendMeasuringBeamFrequency(double beamFrequencyInHertz)
    {
        this.device.informAboutFrequency(beamFrequencyInHertz);
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
                if(RedPitayaLIAFrequency.isDesiredFrequencyMismatchWithinTolerance(val))
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
        boolean supported = RedPitayaLIAFrequency.isDesiredFrequencyMismatchWithinTolerance(desiredFrequencyInHertz);
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
        double closestFrequency = RedPitayaLIAFrequency.getClosestSupportedFrequency(desiredFrequencyInHertz);
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
    public String getUniqueDescription()
    {
        return this.device.getIdentifier();
    }

    @Override
    public boolean requiresSerialPort(SerialPort port)
    {
        return false;
    }

    @Override
    public boolean requiresDevice(String deviceIdentifier)
    {
        boolean required = Objects.equal(this.device.getIdentifier(), deviceIdentifier);
        return required;
    }

    @Override
    public boolean isFunctional() 
    {
        return this.device.isWorkingProperly();
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound()
    {
        boolean replace = !this.device.isWorkingProperly();
        return replace;
    }   
}