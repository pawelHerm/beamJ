package chloroplastInterface.redPitaya;

import java.io.IOException;
import java.util.List;

import com.jcraft.jsch.JSchException;

import atomicJ.geometricSets.RealSet;
import chloroplastInterface.RawVoltageSample;
import chloroplastInterface.SignalSource;

//ta klasa nie powinna implementowac SignalSource, tylko Signal Source Controller
public class RedPitayaLockInDevice implements SignalSource
{        
    private static final String LOCK_IN_APPLICATION_DIRECTORY = "/opt/redpitaya/www/apps/lock_in+pid";
    public static final String PYTHON_LOCK_SETTING_CODE_PATH = "/opt/redpitaya/www/apps/lock_in+pid/py/lock.py";
    private static final String PYTHON_LOCK_IN_RETRIEVAL_COMMAND = "/opt/redpitaya/www/apps/lock_in+pid/py/thinHugoSmall.py";
    private static final String PYTHON_LOCK_IN_INITIAL_SETTINGS_COMMAND = "cd /opt/redpitaya/www/apps/lock_in+pid/py/;./lock.py signal_sw 0;./lock.py sg_amp1 0;./lock.py gen_mod_phase 0;./lock.py gen_mod_hp 48;./lock.py out1_sw 5;./lock.py lpf_F1 46";

    private static final String MONITOR_COMMAND = "/opt/redpitaya/bin/monitor";//bash could not find monitor when used without the full path
    private static final int X28_FPG_REGISTER = 0x40600120; //first 28 bits of the returned 4-byte number
    private static final int Y28_FPG_REGISTER = 0x40600124; //first 28 bits of the returned 4-byte number

    private static final double INTEGER_TO_RAW_VOLTAGE_CONVERSION_FACTOR = 0.500057/0x007fefbd;

    private static final String LOCK_IN_INITIALIZATION_COMMAND = "cat /opt/redpitaya/www/apps/lock_in+pid/red_pitaya.bit > /dev/xdevcfg";
    public static final double MAX_SIGNAL_SAMPLING_FREQUENCY_IN_HERTZ = 20;

    private final RedPitayaCommunicationChannel communicationChannel;
    private boolean initialized;

    private final double maxExpectedVoltageInVolts;

    public RedPitayaLockInDevice(RedPitayaCommunicationChannel communicationChannel)
    {
        this(communicationChannel, 1);
    }   

    public RedPitayaLockInDevice(RedPitayaCommunicationChannel communicationChannel, double maxExpectedVoltage)
    {
        this.communicationChannel = communicationChannel;
        this.maxExpectedVoltageInVolts = maxExpectedVoltage;
    }

    public static boolean checkIfLockInApplicationInstalled(RedPitayaCommunicationChannel communicationChannel) throws JSchException, IOException
    {
        String command = "test -d " + LOCK_IN_APPLICATION_DIRECTORY +" && echo true || echo false";
        String response = communicationChannel.sendCommandToRedPitayaAndGetResponse(command).trim();//trimming is important, as white spaces at the beginning/end confuse Boolean.parseBoolean

        boolean exists = Boolean.parseBoolean(response);

        return exists;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    @Override
    public void initializeIfNecessary() throws IllegalStateException
    {           
        if(initialized)
        {
            return;
        }

        try {
            communicationChannel.sendCommandToRedPitayaAndGetResponse(LOCK_IN_INITIALIZATION_COMMAND);
            communicationChannel.sendCommandToRedPitayaAndGetResponse(PYTHON_LOCK_IN_INITIAL_SETTINGS_COMMAND);    
            this.initialized = true;
        } catch (JSchException | IOException e) 
        {
            e.printStackTrace();
            throw new IllegalStateException("Red Pitaya source does not work properly");
        }        
    }

    public void setSignalAmplificationLevel(int level) throws IllegalStateException
    {
        String command = RedPitayaLIAAmplification.getSettingCommand(level);
        try {
            communicationChannel.sendCommandToRedPitayaAndGetResponse(command);
        } catch (JSchException | IOException e)
        {
            e.printStackTrace();
            throw new IllegalStateException("Red Pitaya source does not work properly");
        }
    }

    public void setLowPassFilter(int cutoffIndex, int order) throws IllegalStateException
    {
        String command = RedPitayaLIALowPassFilter.getSettingCommand(cutoffIndex, order);
        try {
            communicationChannel.sendCommandToRedPitayaAndGetResponse(command);
        } catch (JSchException | IOException e)
        {
            e.printStackTrace();
            throw new IllegalStateException("Red Pitaya source does not work properly");
        }
    }

    public double getMaximalSupportedFrequencyInHertz()
    {
        return RedPitayaLIAFrequency.getMaximalSupportedFrequencyInHertz();
    }

    public List<Double> getListOfSupportedFrequenciesInHertzInAscendingOrder()
    {
        return RedPitayaLIAFrequency.getListOfSupportedFrequenciesInHertzInAscendingOrder();
    }

    public RealSet getSupportedFrequencies()
    {
        return RedPitayaLIAFrequency.getSupportedFrequencies();
    }

    public void setFrequencyLevel(int level) throws IllegalStateException
    {
        String command = RedPitayaLIAFrequency.getFrequencySettingCommand(level);
        try {
            communicationChannel.sendCommandToRedPitayaAndGetResponse(command);
        } catch (JSchException | IOException e)
        {
            throw new IllegalStateException("Red Pitaya source does not work properly");
        }
    }

    @Override
    public boolean isFrequencySupported(double desiredFrequencyInHertz)
    {
        boolean supported = RedPitayaLIAFrequency.isDesiredFrequencyMismatchWithinTolerance(desiredFrequencyInHertz);
        return supported;
    }

    @Override
    public double getClosestSupportedFrequency(double desiredFrequencyInHertz)
    {
        double closestFrequency = RedPitayaLIAFrequency.getClosestSupportedFrequency(desiredFrequencyInHertz);
        return closestFrequency;
    }

    @Override
    public void informAboutFrequency(double desiredFrequencyInHertz) throws IllegalStateException
    {
        boolean withinTolerance = RedPitayaLIAFrequency.isDesiredFrequencyMismatchWithinTolerance(desiredFrequencyInHertz);

        if(withinTolerance)
        {
            int closestLevel = RedPitayaLIAFrequency.getClosestLevelForDesiredFrequency(desiredFrequencyInHertz);
            String command = RedPitayaLIAFrequency.getFrequencySettingCommand(closestLevel);

            try {
                communicationChannel.sendCommandToRedPitayaAndGetResponse(command);
            } catch (JSchException | IOException e) 
            {
                e.printStackTrace();
                throw new IllegalStateException("Red Pitaya source does not work properly");
            }
        }
        else
        {
            throw new IllegalArgumentException("The desired frequenct "+ Double.toString(desiredFrequencyInHertz) + "cannot be achieved with the Red Pitaya - based Lock-In Amplifier");
        }
    }

    public double getPreferredFrequencyIncrement(double currentFrequency)
    {
        return RedPitayaLIAFrequency.getPreferredFrequencyIncrement(currentFrequency);
    }

    public double getPreferredFrequencyDecrement(double currentFrequency)
    {
        return RedPitayaLIAFrequency.getPreferredFrequencyDecrement(currentFrequency);
    }

    public RawVoltageSample getSampleUsingPhythonCodeOnServerSide() 
    {
        long timeInMilis = System.currentTimeMillis();
        double val = Double.NaN;

        try {
            String result = communicationChannel.sendCommandToRedPitayaAndGetResponse(PYTHON_LOCK_IN_RETRIEVAL_COMMAND);
            val = Double.valueOf(result);           
        } catch (JSchException | IOException e) 
        {
            e.printStackTrace();
        }

        RawVoltageSample sample = new RawVoltageSample(val, timeInMilis);
        return sample;
    }

    @Override
    public RawVoltageSample getSample() 
    {
        long timeInMilis = System.currentTimeMillis();
        double val = Double.NaN;

        try {

            long[] vals = getFPGRegisterValue(X28_FPG_REGISTER, Y28_FPG_REGISTER);
            long x28 = (((int)vals[0]) << 4) >> 4;//bitwise and will not work easily due to two's complement,we need to define x28 as a long number, otherwise x28*x28 is too large
            long y28 = (((int)vals[1]) << 4) >> 4;
            double r = Math.sqrt(x28*x28 + y28*y28);
            val = r*INTEGER_TO_RAW_VOLTAGE_CONVERSION_FACTOR;

            //            System.err.println("x28 " + x28);
            //            System.err.println("y28 " + y28);
            //
            //            System.err.println("raw " + r);
            //            System.out.println("val " + val);
        } catch (JSchException | IOException e) 
        {
            e.printStackTrace();
        }

        RawVoltageSample sample = new RawVoltageSample(val, timeInMilis);
        return sample;
    }

    private long getFPGRegisterValue(int address) throws JSchException, IOException
    {
        String command = new StringBuilder(MONITOR_COMMAND).append(" ").append(address).toString();        
        String result = communicationChannel.sendCommandToRedPitayaAndGetResponse(command).trim();

        long val = Long.decode(result);//Integer.parseInt(string, 16) will throw an error if there is the 0x prefix
        return val;
    }

    private long[] getFPGRegisterValue(int addressA, int addressB) throws JSchException, IOException
    {
        String command = new StringBuilder(MONITOR_COMMAND).append(" ").append(addressA).append(";").append(MONITOR_COMMAND).append(" ").append(addressB).toString();
        String result = communicationChannel.sendCommandToRedPitayaAndGetResponse(command).trim();

        String[] split = result.split("\\r?\\n");
        long[] vals = new long[] {Long.decode(split[0].trim()), Long.decode(split[1].trim())};

        return vals;
    }

    public boolean isFunctional() {
        return communicationChannel.canEstablishConnection();
    }  

    @Override
    public void finishReading() 
    {
        communicationChannel.close();
    }

    public boolean isWorkingProperly()
    {
        boolean working = communicationChannel.canEstablishConnection();
        return working;
    }

    @Override
    public double getMaximalSignalSamplingRateInHertz()
    {
        return MAX_SIGNAL_SAMPLING_FREQUENCY_IN_HERTZ;
    }

    public String getIdentifier()
    {
        return communicationChannel.getHost();
    }
}
