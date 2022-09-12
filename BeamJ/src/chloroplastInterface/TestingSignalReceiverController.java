package chloroplastInterface;

public class TestingSignalReceiverController implements SignalReceiverController 
{
    private final static String DESCRIPTION = "Test";
    private final static TestingSignalReceiverController INSTANCE = new TestingSignalReceiverController();

    private TestingSignalReceiverController(){};

    public static TestingSignalReceiverController getInstance()
    {
        return INSTANCE;
    }

    @Override
    public SignalReceiver getSignalReceiver()
    {
        return DummySignalReceiver.getInstance();
    }

    @Override
    public SignalReceiver getSignalReceiver(double minExpectedVoltage,double maxExpectedVoltage) 
    {
        return DummySignalReceiver.getInstance();
    }

    @Override
    public String getUniqueDescription() {
        return DESCRIPTION;
    }

    @Override
    public String toString()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() 
    {
        return false;
    }

    @Override
    public boolean isFunctional() {
        return true;
    }
}
