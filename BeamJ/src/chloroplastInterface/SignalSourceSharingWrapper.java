package chloroplastInterface;

import chloroplastInterface.redPitaya.RedPitayaLIAFrequency;

public class SignalSourceSharingWrapper implements SignalSource 
{
    //This class is a wrapper for signalSource that ignores finishReading call, so that it can be used to share the signal source

    private final SignalSource signalSource;

    public SignalSourceSharingWrapper(SignalSource signalSource)
    {
        this.signalSource = signalSource;
    }    

    @Override
    public void initializeIfNecessary() throws IllegalStateException
    {
        signalSource.initializeIfNecessary();
    }

    @Override
    public RawVoltageSample getSample()
    {
        return signalSource.getSample();
    }

    @Override
    public void finishReading()
    {}

    @Override
    public double getMaximalSignalSamplingRateInHertz()
    {
        return signalSource.getMaximalSignalSamplingRateInHertz();
    }

    @Override
    public boolean isFrequencySupported(double desiredFrequencyInHertz)
    {
        return signalSource.isFrequencySupported(desiredFrequencyInHertz);
    }

    @Override
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz)
    {
        double closestFrequency = RedPitayaLIAFrequency.getClosestSupportedFrequency(desiredFrequencyInHertz);
        return closestFrequency;
    }

    @Override
    public void informAboutFrequency(double desiredFrequencyInHertz)
            throws IllegalStateException {
        this.informAboutFrequency(desiredFrequencyInHertz);
    }
}
