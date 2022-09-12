package chloroplastInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fazecast.jSerialComm.SerialPort;

import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.RealSet;
import atomicJ.utilities.Validation;

public class MeasuringBeamControllerArduino implements MeasuringBeamController
{
    private static final double MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ = 10000;
    private final SerialPort measurementLightPort;

    public MeasuringBeamControllerArduino(SerialPort measurementLightPort)
    {
        Validation.requireNonNullParameterName(measurementLightPort, "measurementLightPort");
        this.measurementLightPort = measurementLightPort;
    }

    @Override
    public boolean isSoftwareControlOfMeasuringBeamIntensitySupported() 
    {
        return true;
    }  

    @Override
    public double getMaximalSupportedFrequencyInHertz()
    {
        return MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ;
    }

    @Override
    public void sendMeasuringLightIntensity(double lightIntensityInPercent) 
    {
        short voltage = (short) Math.round(RecordingModel.MAX_VOLTAGE_12_BITS*lightIntensityInPercent/100.);
        this.measurementLightPort.writeBytes(new byte[] {/*high byte*/(byte) ((voltage >> 8) & 0xFF),/*low byte*/(byte) (voltage & 0xFF), /*message byte, to distinguish between intensity and frequency*/(byte)0xF0}, 3);
    }  

    @Override
    public void sendMeasuringBeamFrequency(double lightFrequencyInHertz)
    {
        short frequencyInteger = (short) Math.round(RecordingModel.MAX_FREQUENCY_15_BITS*lightFrequencyInHertz/MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ);
        this.measurementLightPort.writeBytes(new byte[] {/*high byte*/(byte) ((frequencyInteger >> 8) & 0xFF),/*low byte*/(byte) (frequencyInteger & 0xFF), /*message byte, to distinguish between intensity and frequency*/(byte)0xFF}, 3);
    }

    @Override
    public String getUniqueDescription()
    {
        return this.measurementLightPort.getSystemPortName();
    }

    @Override
    public boolean requiresSerialPort(SerialPort port)
    {
        boolean portRequired = Objects.equals(this.measurementLightPort, port);
        return portRequired;
    }


    @Override
    public boolean requiresDevice(String deviceIdentifier)
    {
        return false;
    }

    @Override
    public boolean isFunctional() 
    {
        return this.measurementLightPort.isOpen();
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() 
    {
        return false;
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
                if(!Double.isNaN(val) && Double.isFinite(val) && val >= 0 && val <= MAX_FREQENCY_OF_MEASURING_LIGHT_IN_HERTZ)
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
    public boolean isSetOfSupportedFrequenciesDiscrete() {
        return false;
    }

    @Override
    public List<Double> getListOfSupportedDiscreteFrequenciesInHertzInAscendingOrder() {
        return Collections.emptyList();
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
}