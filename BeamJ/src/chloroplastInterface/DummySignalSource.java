package chloroplastInterface;

class DummySignalSource implements SignalSource
{
    private final static DummySignalSource INSTANCE = new DummySignalSource();

    private DummySignalSource(){}

    public static DummySignalSource getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void initializeIfNecessary() throws IllegalStateException 
    {        
    } 

    @Override
    public RawVoltageSample getSample() 
    {
        double voltage = Math.random();
        RawVoltageSample sample = new RawVoltageSample(voltage, System.currentTimeMillis());

        return sample;
    }

    @Override
    public void finishReading() 
    {      
    }

    @Override
    public double getMaximalSignalSamplingRateInHertz()
    {
        return 1200;
    }

    @Override
    public boolean isFrequencySupported(double desiredFrequencyInHertz)
    {
        return true;
    }

    @Override
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz)
    {
        return desiredFrequencyInHertz;
    }

    @Override
    public void informAboutFrequency(double desiredFrequencyInHertz) throws IllegalStateException 
    {        
    }      
}