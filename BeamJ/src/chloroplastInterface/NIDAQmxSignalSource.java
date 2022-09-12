package chloroplastInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class NIDAQmxSignalSource implements SignalSource 
{
    private static final String TASK_NAME = "TransmittanceReading";
    private static final int DAQmx_Val_Diff = 10106;
    private static final int DAQmx_Val_RSE = 10083;
    private static final double READ_TIMEOUT = 100.0;

    private final NIDevice niDevice;
    private final int aiChannelIndex;
    private final double expectedCallCountPerSecond;
    private final int numberOfSamplesToAverage;
    private final double readingRateInHertz = 500;
    private Pointer transmittanceTask = null;


    public NIDAQmxSignalSource(NIDevice niDevice, int numberOfSamplesToAverage, double expectedCallCountPerSecond)
    {
        this(niDevice, 0, -10.0, 10.0, numberOfSamplesToAverage, expectedCallCountPerSecond);
    }

    public NIDAQmxSignalSource(NIDevice niDevice, int aiChannelIndex, double minOutputVoltage, double maxOutputVoltage, int numberOfSamplesToAverage, double expectedCallCountPerSecond)
    {
        this.expectedCallCountPerSecond = expectedCallCountPerSecond;
        this.niDevice = niDevice;
        this.aiChannelIndex = aiChannelIndex;

        this.numberOfSamplesToAverage = numberOfSamplesToAverage;

        PointerByReference taskHandleRef = new PointerByReference();
        Nicaiu.INSTANCE.DAQmxCreateTask(TASK_NAME, taskHandleRef);
        this.transmittanceTask = taskHandleRef.getValue();

        String channelDescription = niDevice.getChannelDescriptionForAnalogInputChannel(aiChannelIndex);
        int errorCodeChannels = Nicaiu.INSTANCE.DAQmxCreateAIVoltageChan(transmittanceTask, channelDescription, "", DAQmx_Val_Diff, minOutputVoltage, maxOutputVoltage, Nicaiu.DAQmx_Val_Volts, null);


        System.err.println("ERROR CODE " + errorCodeChannels);

        if(numberOfSamplesToAverage > 1)//if we execute this for numberOfSamplesToAverage = 1, subsequent calls to DAQmxReadAnalogScalarF64 will return an error
        {
            int errorTiming = Nicaiu.INSTANCE.DAQmxCfgSampClkTiming(transmittanceTask, "", readingRateInHertz, Nicaiu.DAQmx_Val_Rising, Nicaiu.DAQmx_Val_FiniteSamps, numberOfSamplesToAverage);
        }        
    }

    @Override
    public void initializeIfNecessary() throws IllegalStateException 
    {        
    } 

    @Override
    public RawVoltageSample getSample() 
    {
        long timeInMilis = System.currentTimeMillis();

        double val = readAnalogueIn();
        //        for(int i = 0; i<numberOfSamplesToAverage; i++)
        //        {
        //            val += readAnalogueIn();// (numberOfSamplesToAverage > 1) ? DescriptiveStatistics.arithmeticMean(readAnalogueIn(this.numberOfSamplesToAverage)) : readAnalogueIn();
        //            try {
        //                Thread.sleep(4);
        //            } catch (InterruptedException e) {
        //                // TODO Auto-generated catch block
        //                e.printStackTrace();
        //            }
        //        }

        Nicaiu.INSTANCE.DAQmxStopTask(transmittanceTask);

        RawVoltageSample sample = new RawVoltageSample(val, timeInMilis);
        return sample;
    }

    @Override
    public void finishReading()
    {
        Nicaiu.INSTANCE.DAQmxStopTask(transmittanceTask);
        Nicaiu.INSTANCE.DAQmxClearTask(transmittanceTask);
    }

    private double readAnalogueIn() 
    {
        int errorStart = Nicaiu.INSTANCE.DAQmxStartTask(transmittanceTask);
        DoubleBuffer buff = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asDoubleBuffer();
        int errorRead = Nicaiu.INSTANCE.DAQmxReadAnalogScalarF64(transmittanceTask,  READ_TIMEOUT, buff, null);

        //  System.out.println("error start A " + errorStart);
        //  System.out.println("error read A " + errorRead);

        return buff.get();
    }

    private double[] readAnalogueIn(int inputBufferSize) 
    {
        int errorStart = Nicaiu.INSTANCE.DAQmxStartTask(transmittanceTask);
        double[] buffer = new double[inputBufferSize];

        DoubleBuffer inputBuffer = DoubleBuffer.wrap(buffer);
        IntBuffer samplesPerChannelRead = IntBuffer.allocate(1);
        int errorRead = Nicaiu.INSTANCE.DAQmxReadAnalogF64(transmittanceTask, -1, 100.0, new NativeLong(Nicaiu.DAQmx_Val_GroupByChannel), inputBuffer, new NativeLong(inputBufferSize), samplesPerChannelRead, null);

        return buffer;
    }

    public int getAnalogInputChannelIndex()
    {
        return aiChannelIndex;
    }

    @Override
    public double getMaximalSignalSamplingRateInHertz() 
    {
        return niDevice.getMaximalAnalogRateForSingleChannel();
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


    //we can ignore this information
    @Override
    public void informAboutFrequency(double desiredFrequencyInHertz)
            throws IllegalStateException {        
    }
}
