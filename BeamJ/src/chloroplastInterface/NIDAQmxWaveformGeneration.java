package chloroplastInterface;

import java.nio.DoubleBuffer;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class NIDAQmxWaveformGeneration 
{
    private static final String TASK_NAME = "WaveformGeneration";
    private static final int DAQmx_Val_Diff = 10106;
    private static final int DAQmx_Val_RSE = 10083;

    private final NIDevice niDevice;
    private final int numberOfPointsInWaveform;
    private final double writingRateInHertz = 500;
    private final double signalAmplitude =9.95;
    private Pointer transmittanceTask = null;


    public NIDAQmxWaveformGeneration(NIDevice niDevice, int numberOfPointsInWaveform)
    {
        this(niDevice, 0, 10.0, numberOfPointsInWaveform);
    }

    public NIDAQmxWaveformGeneration(NIDevice niDevice, double minOutputVoltage, double maxOutputVoltage, int numberOfPointsInWaveform)
    {     
        this.niDevice = niDevice;

        this.numberOfPointsInWaveform = numberOfPointsInWaveform;

        PointerByReference taskHandleRef = new PointerByReference();
        Nicaiu.INSTANCE.DAQmxCreateTask(TASK_NAME, taskHandleRef);
        this.transmittanceTask = taskHandleRef.getValue();

        String channelDescription = niDevice.getName() + "/ao1";
        int errorCodeChannels = Nicaiu.INSTANCE.DAQmxCreateAOVoltageChan(transmittanceTask, channelDescription, "",  minOutputVoltage, maxOutputVoltage, Nicaiu.DAQmx_Val_Volts, null);

        if(numberOfPointsInWaveform > 1)//if we execute this for numberOfSamplesToAverage = 1, subsequent calls to DAQmxReadAnalogScalarF64 will return an error
        {
            int errorTiming = Nicaiu.INSTANCE.DAQmxCfgSampClkTiming(transmittanceTask, "", writingRateInHertz, Nicaiu.DAQmx_Val_Rising, Nicaiu.DAQmx_Val_ContSamps, numberOfPointsInWaveform);
        }        
    }

    public void startGeneration() 
    {        
        double[] buffer = new double[numberOfPointsInWaveform];

        for(int i = 0;i<numberOfPointsInWaveform;i++)
        {
            buffer[i] = signalAmplitude*Math.sin(i*2.0*Math.PI/numberOfPointsInWaveform);
        }

        DoubleBuffer waveformBuffer = DoubleBuffer.wrap(buffer);

        int errorWrite = Nicaiu.INSTANCE.DAQmxWriteAnalogF64(transmittanceTask, numberOfPointsInWaveform, /*autostart vote*/ new NativeLong(0), /*timeout*/10.0, new NativeLong(Nicaiu.DAQmx_Val_GroupByChannel), waveformBuffer, null, null);        

        int errorStart = Nicaiu.INSTANCE.DAQmxStartTask(transmittanceTask);

    }

    public void finishGeneration()
    {
        Nicaiu.INSTANCE.DAQmxStopTask(transmittanceTask);
        Nicaiu.INSTANCE.DAQmxClearTask(transmittanceTask);
    }
}
