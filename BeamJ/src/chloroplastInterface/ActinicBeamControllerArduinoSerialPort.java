package chloroplastInterface;

import java.util.Objects;

import com.fazecast.jSerialComm.SerialPort;

import atomicJ.utilities.Validation;

public class ActinicBeamControllerArduinoSerialPort implements ActinicBeamController
{
    private final SerialPort actinicLightPort;

    public ActinicBeamControllerArduinoSerialPort(SerialPort actinicLightPort)
    {
        Validation.requireNonNullParameterName(actinicLightPort, "actinicLightPort");
        this.actinicLightPort = actinicLightPort;
    }

    @Override
    public void sendActinicLightIntensity(double lightIntensityInPercent)
    {
        short voltage = (short) Math.round(RecordingModel.MAX_VOLTAGE_12_BITS*lightIntensityInPercent/100.);
        this.actinicLightPort.writeBytes(new byte[] {/*high byte*/(byte) ((voltage >> 8) & 0xFF),/*low byte*/(byte) (voltage & 0xFF)}, 2);            
    }

    @Override
    public String getBeamControllerDescription()
    {
        return this.actinicLightPort.getSystemPortName();
    }

    @Override
    public boolean requiresSerialPort(SerialPort port)
    {
        boolean portRequired = Objects.equals(this.actinicLightPort, port);
        return portRequired;
    }

    @Override
    public boolean isOpen() 
    {
        return this.actinicLightPort.isOpen();
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() {
        return false;
    }       
}