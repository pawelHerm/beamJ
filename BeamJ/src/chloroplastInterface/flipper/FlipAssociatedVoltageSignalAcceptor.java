package chloroplastInterface.flipper;

import chloroplastInterface.SignalReceiver;

public class FlipAssociatedVoltageSignalAcceptor implements FlipAssociatedSignalAcceptor
{
    private final SignalReceiver signalReceiver;
    private final VoltageFlipSignalSettings voltageSettings;

    public FlipAssociatedVoltageSignalAcceptor(SignalReceiver signalReceiver, VoltageFlipSignalSettings voltageSettings)
    {
        this.signalReceiver = signalReceiver;
        this.voltageSettings = voltageSettings;

        this.signalReceiver.initializeIfNecessary();
    }

    @Override
    public void triggerSignal() 
    {
        if(voltageSettings.isSendSignalAfterFlip())
        {
            double voltageValue = voltageSettings.getSignalVoltageValue();
            this.signalReceiver.sendSample(voltageValue);
        }
    }

    @Override
    public void endSignal()
    {
        if(voltageSettings.isSendSignalAfterFlip())
        {
            this.signalReceiver.sendSample(0);
        }
    }

    @Override
    public boolean isSendSignalAfterFlip() 
    {
        boolean sendSignal = voltageSettings.isSendSignalAfterFlip();
        return sendSignal;
    }

    @Override
    public double getLagTimeInMilliseconds() 
    {
        double lagTime = voltageSettings.getLagTimeInMilliseconds();
        return lagTime;
    }

    @Override
    public double getSignalDurationInMilliseconds()
    {
        double signalDuration = voltageSettings.getSignalDurationInMilliseconds();
        return signalDuration;
    }     
}