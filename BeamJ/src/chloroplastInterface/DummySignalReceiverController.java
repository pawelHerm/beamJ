package chloroplastInterface;

public class DummySignalReceiverController implements SignalReceiverController
{
    private static final String DESCRIPTION = "Undetected!";
    private static final DummySignalReceiverController INSTANCE = new DummySignalReceiverController();

    private DummySignalReceiverController()
    {}

    public static DummySignalReceiverController getInstance()
    {
        return INSTANCE;
    }

    @Override
    public SignalReceiver getSignalReceiver() 
    {
        return DummySignalReceiver.getInstance();
    }

    @Override
    public SignalReceiver getSignalReceiver(double minOutVoltage, double maxOutVoltage) {
        return DummySignalReceiver.getInstance();
    }

    @Override
    public boolean shouldBeReplacedWhenOtherControllerFound() 
    {
        return true;
    }

    @Override
    public boolean isFunctional() {
        return false;
    }

    @Override
    public String getUniqueDescription() 
    {
        return DESCRIPTION;
    }

}
