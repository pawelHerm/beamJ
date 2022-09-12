package chloroplastInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

public class NIDevice
{
    private final String name;
    private final int serialNumber;
    private final boolean simulated;
    private final double maximalAIRate;
    private final List<String> analogInputChannelNames;

    private NIDevice(String name)
    {
        this.name = name;
        this.serialNumber = getSerialNameProperty(name);
        this.simulated = getIsSimulatedProperty(name);
        this.maximalAIRate = getMaximalAnalogInputConversionRateForSingleChannel(name);

        this.analogInputChannelNames = Arrays.asList(getNamesOfAnalogInputChannels(name));
    }

    public static List<NIDevice> getAvailableNIDevices()
    {
        String[] names = getNamesOfAvailableNIDevices();
        List<NIDevice> devices = new ArrayList<>();
        
        for(int i = 0; i<names.length;i++)
        {
            String deviceName = names[i].trim();
            devices.add(new NIDevice(deviceName));
        }

        return devices;
    }

    public static String[] getNamesOfAvailableNIDevices()
    {     
        try
        {
            //based on
            //https://forums.ni.com/t5/Multifunction-DAQ/DAQmx-How-to-query-the-list-of-devices-in-ANSI-C/td-p/3604399?profile.language=en
            int lengthOfNameString = Nicaiu.INSTANCE.DAQmxGetSysDevNames(ByteBuffer.wrap(new byte[] {}), new NativeLong(0));
            if(lengthOfNameString < 0)
            {
                return new String[] {};
            }

            byte[] bytesOfNameString = new byte[lengthOfNameString];
            Nicaiu.INSTANCE.DAQmxGetSysDevNames(ByteBuffer.wrap(bytesOfNameString), new NativeLong(lengthOfNameString));

            String namesJoined = new String(bytesOfNameString, StandardCharsets.UTF_8);
            String[] names = namesJoined.split(",");

            return names;
        } 
        catch (java.lang.UnsatisfiedLinkError | java.lang.NoClassDefFoundError e)
        {
            e.printStackTrace();
            Logger.getLogger(NIDevice.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }

        return new String[] {};
    }

    private static int getSerialNameProperty(String deviceName)
    {
        NativeLongByReference referenceToSerialNumber = new NativeLongByReference();
        int errorCodeSerialNumber = Nicaiu.INSTANCE.DAQmxGetDevSerialNum(deviceName, referenceToSerialNumber);
        int serialNumber = referenceToSerialNumber.getValue().intValue();

        return serialNumber;
    }

    private static boolean getIsSimulatedProperty(String deviceName)
    {
        NativeLongByReference referenceToSimulationStatus = new NativeLongByReference();
        Nicaiu.INSTANCE.DAQmxGetDevIsSimulated(deviceName, referenceToSimulationStatus);
        boolean isSimulated = referenceToSimulationStatus.getValue().intValue() != 0;

        return isSimulated;
    }

    private static String[] getNamesOfAnalogInputChannels(String deviceName)
    {
        //based on
        //https://forums.ni.com/t5/Multifunction-DAQ/DAQmx-How-to-query-the-list-of-devices-in-ANSI-C/td-p/3604399?profile.language=en
        int lengthOfAINamesString = Nicaiu.INSTANCE.DAQmxGetDevAIPhysicalChans(deviceName, ByteBuffer.wrap(new byte[] {}), new NativeLong(0));
        if(lengthOfAINamesString < 0)
        {
            return new String[] {};
        }

        byte[] bytesOfAINamesString = new byte[lengthOfAINamesString];
        Nicaiu.INSTANCE.DAQmxGetDevAIPhysicalChans(deviceName, ByteBuffer.wrap(bytesOfAINamesString), new NativeLong(lengthOfAINamesString));

        String namesJoined = new String(bytesOfAINamesString, StandardCharsets.UTF_8);

        String[] names = namesJoined.split(",");

        return names;
    }


    private static double getMaximalAnalogInputConversionRateForSingleChannel(String deviceName)
    {
        DoubleBuffer buff = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asDoubleBuffer();
        Nicaiu.INSTANCE.DAQmxGetDevAIMaxSingleChanRate(deviceName, buff);

        return buff.get();
    }

    public String getChannelDescriptionForAnalogInputChannel(int channelIndex)
    {
        String channelDescription = name + "/ai" + Integer.toString(channelIndex);
        return channelDescription;
    }

    public String getChannelDescriptionForAnalogOutputChannel(int channelIndex)
    {
        String channelDescription = name + "/ao" + Integer.toString(channelIndex);
        return channelDescription;
    }

    public int getSerialNumber()
    {
        return serialNumber;
    }

    public String getName()
    {
        return name;
    }

    public boolean isSimulated()
    {
        return simulated;
    }

    public boolean isProperlyConstructed()
    {
        boolean properlyConstructed = maximalAIRate > 0;
        return properlyConstructed;
    }

    public double getMaximalAnalogRateForSingleChannel()
    {
        return maximalAIRate;
    }

    public List<String> getAnnalogInputChannelNames()
    {
        return new ArrayList<>(analogInputChannelNames);
    }

    @Override
    public int hashCode()
    {
        int result = (name == null ? 0 : name.hashCode());
        result = 31*result + Integer.hashCode(serialNumber);
        result = 31*result + Boolean.hashCode(simulated);
        result = 31*result + Double.hashCode(maximalAIRate);
        result = 31*result + analogInputChannelNames.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof NIDevice)
        {
            NIDevice that = (NIDevice)other;

            boolean equals = true;
            equals = equals && Objects.equals(this.name, that.name);
            equals = equals && (this.serialNumber == that.serialNumber);
            equals = equals && (this.simulated == that.simulated);
            equals = equals && (Double.compare(this.maximalAIRate, that.maximalAIRate) == 0);
            equals = equals && (this.analogInputChannelNames.equals(that.analogInputChannelNames));

        }

        return false;
    }

    private String buildIdentifier()
    {
        String nameAndSerial = new StringBuilder(name).append(" (").append(Integer.toHexString(serialNumber).toUpperCase(Locale.US)).append(")").toString();
        return nameAndSerial;
    }

    public String getIdentifier()
    {
        return buildIdentifier();
    }

    @Override
    public String toString()
    {
        return buildIdentifier();
    }
}