package chloroplastInterface;

import com.fazecast.jSerialComm.SerialPort;

public interface MeasuringBeamController extends FrequencyDependentController
{
    public boolean isSoftwareControlOfMeasuringBeamIntensitySupported();
    public void sendMeasuringLightIntensity(double beamIntensityInPercent);
    public void sendMeasuringBeamFrequency(double beamFrequencyInHertz);
    public boolean requiresSerialPort(SerialPort port);
}