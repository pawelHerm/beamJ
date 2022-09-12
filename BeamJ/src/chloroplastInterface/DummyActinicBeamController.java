package chloroplastInterface;

import com.fazecast.jSerialComm.SerialPort;

public class DummyActinicBeamController implements ActinicBeamController
{
    private static final String DESCRIPTION = "Undetected!";
    private static final DummyActinicBeamController INSTANCE = new DummyActinicBeamController();

    private DummyActinicBeamController()
    {}

    public static DummyActinicBeamController getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void sendActinicLightIntensity(double lightIntensityInPercent) {

    }

    @Override
    public String getBeamControllerDescription() {
        return DESCRIPTION;
    }

    @Override
    public boolean requiresSerialPort(SerialPort port) {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() {
        return true;
    }       
}