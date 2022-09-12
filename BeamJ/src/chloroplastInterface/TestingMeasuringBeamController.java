package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.Validation;

public class TestingMeasuringBeamController implements MeasuringBeamController
{
    private static final double MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ = 10000;
    private static final String CONTROLLER_NAME = "Test";  

    private static final TestingMeasuringBeamController INSTANCE = new TestingMeasuringBeamController();

    private TestingMeasuringBeamController()
    {}

    public static TestingMeasuringBeamController getInstance()
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
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, Double.MAX_VALUE, "currentFrequency");

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
    public String getUniqueDescription() 
    {
        return CONTROLLER_NAME;
    }

    @Override
    public boolean requiresSerialPort(SerialPort port) {
        return false;
    }

    @Override
    public boolean isFunctional()
    {
        return true;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() {
        return true;
    }

    @Override
    public boolean requiresDevice(String deviceIdentifier)
    {
        return false;
    }   
}