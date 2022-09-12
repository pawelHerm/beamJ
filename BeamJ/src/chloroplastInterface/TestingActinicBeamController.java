package chloroplastInterface;

import com.fazecast.jSerialComm.SerialPort;

public class TestingActinicBeamController implements ActinicBeamController
{
    private static final String CONTROLLER_NAME = "Test";
    private static final TestingActinicBeamController INSTANCE = new TestingActinicBeamController();

    private TestingActinicBeamController(){           
    }

    public static TestingActinicBeamController getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void sendActinicLightIntensity(double lightIntensityInPercent) 
    {}

    @Override
    public String getBeamControllerDescription() 
    {
        return CONTROLLER_NAME;
    }

    @Override
    public boolean requiresSerialPort(SerialPort port) {
        return false;
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() {
        return true;
    }       
}