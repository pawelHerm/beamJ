package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.Validation;

public class DummyMeasuringBeamController implements MeasuringBeamController
{
    private static final double MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ = 10000;//we use the same value as when Arduino is used, to avoid unnecessary changes inGUI when Arduino is found

    private static final String DESCRIPTION = "Undetected!";
    private static final DummyMeasuringBeamController INSTANCE = new DummyMeasuringBeamController();

    private DummyMeasuringBeamController(){}

    public static DummyMeasuringBeamController getInstance()
    {
        return INSTANCE;
    }

    @Override
    public double getMaximalSupportedFrequencyInHertz() 
    {
        return MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ;
    }

    @Override
    public boolean isSoftwareControlOfMeasuringBeamIntensitySupported()
    {
        return true;
    }

    @Override
    public void sendMeasuringLightIntensity(double lightIntensityInPercent) 
    {}

    @Override
    public void sendMeasuringBeamFrequency(double lightFrequencyInHertz)
    {}

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
        boolean supported = desiredFrequencyInHertz >= 0 && desiredFrequencyInHertz <= MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ;
        return supported;
    }

    @Override
    public RealSet getSupportedFrequencies()
    {
        RealSet supportedFrequencies = new ClosedInterval(0, MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ);
        return supportedFrequencies;
    }

    @Override
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz) 
    {
        double supportedFrequency = Math.min(Math.max(0, desiredFrequencyInHertz), MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ);
        return supportedFrequency;
    }

    @Override
    public double getPreferredFrequencyIncrement(double currentFrequency)
    {
        Validation.requireNonNegativeParameterName(currentFrequency, "currentFrequency");
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ, "currentFrequency");

        double incrementedFrequency = Math.min(MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ, currentFrequency + 1);
        double increment = incrementedFrequency - currentFrequency;
        return increment;
    }

    @Override
    public double getPreferredFrequencyDecrement(double currentFrequency)
    {
        Validation.requireNonNegativeParameterName(currentFrequency, "currentFrequency");
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ, "currentFrequency");

        double frequencyAfterDecrement = Math.max(0., currentFrequency - 1);
        double decrement = currentFrequency - frequencyAfterDecrement;
        return decrement;
    }

    @Override
    public String getUniqueDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean requiresSerialPort(SerialPort port) {
        return false;
    }

    @Override
    public boolean isFunctional() {
        return false;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() {
        return true;
    }

    @Override
    public boolean requiresDevice(String deviceIdentifier) {
        return false;
    }
}