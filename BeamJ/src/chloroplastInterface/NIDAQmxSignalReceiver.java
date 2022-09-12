package chloroplastInterface;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class NIDAQmxSignalReceiver implements SignalReceiver 
{
    private static final double TIMEOUT_IN_SECONDS = 10.0;
    private static final String TASK_NAME = "VoltageSignal";

    private final int aoChannelIndex;
    private Pointer signalSendingTask = null;

    public NIDAQmxSignalReceiver(NIDevice niDevice)
    {
        this(niDevice, 0, -10.0, 10.0);
    }

    public NIDAQmxSignalReceiver(NIDevice niDevice, int aoChannelIndex, double minOutputVoltage, double maxOutputVoltage)
    {
        this.aoChannelIndex = aoChannelIndex;

        PointerByReference taskHandleRef = new PointerByReference();
        Nicaiu.INSTANCE.DAQmxCreateTask(TASK_NAME, taskHandleRef);
        this.signalSendingTask = taskHandleRef.getValue();

        String channelDescription = niDevice.getChannelDescriptionForAnalogOutputChannel(aoChannelIndex);
        int errorCreateChannel = Nicaiu.INSTANCE.DAQmxCreateAOVoltageChan(signalSendingTask, channelDescription, "", minOutputVoltage, maxOutputVoltage, Nicaiu.DAQmx_Val_Volts, null);
    }

    @Override
    public void initializeIfNecessary() throws IllegalStateException 
    {        
    } 

    @Override
    public void sendSample(double value) 
    {
        int errorStart = Nicaiu.INSTANCE.DAQmxStartTask(signalSendingTask);                      
        int errorRead = Nicaiu.INSTANCE.DAQmxWriteAnalogScalarF64(signalSendingTask, /*autostart*/new NativeLong(0), TIMEOUT_IN_SECONDS, value, null);       
    }

    public void finish()
    {
        Nicaiu.INSTANCE.DAQmxStopTask(signalSendingTask);
        Nicaiu.INSTANCE.DAQmxClearTask(signalSendingTask);
    }

    public int getAnalogOutputChannelIndex()
    {
        return aoChannelIndex;
    }
}
