package chloroplastInterface;

import com.fazecast.jSerialComm.SerialPort;

public interface ActinicBeamController
{
    public void sendActinicLightIntensity(double lightIntensityInPercent);
    public String getBeamControllerDescription();
    public boolean requiresSerialPort(SerialPort port);
    public boolean isOpen();
    public boolean shouldBeReplacedWhenOtherControllerFound();
}