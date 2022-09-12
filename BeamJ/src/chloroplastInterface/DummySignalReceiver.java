package chloroplastInterface;

class DummySignalReceiver implements SignalReceiver
{
    private final static DummySignalReceiver INSTANCE = new DummySignalReceiver();

    private DummySignalReceiver(){}

    public static DummySignalReceiver getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void initializeIfNecessary() throws IllegalStateException 
    {        
    } 

    @Override
    public void sendSample(double value) 
    {
    }
}