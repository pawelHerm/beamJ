package chloroplastInterface;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import com.fazecast.jSerialComm.SerialPort;

public enum SerialPortVisitor
{
    ACTINIC(new byte[] {(byte) 0xFF, 33, (byte) 0xFF, 51}) 
    {
        @Override
        public void registerThePortWithModel(byte[] magicBytesReturnedByPort, SerialPort port, RecordingModel model) 
        {
            boolean recognized = Arrays.equals(getMagicBytes(), magicBytesReturnedByPort);
            if(recognized)
            {
                model.registerActinicBeamPort(port);
            }
            else
            {
                port.closePort();
            }
        }
    }, 

    MEASUREMENT(new byte[] {(byte) 0xFF, 45, (byte) 0xFF, 20}) 
    {
        @Override
        public void registerThePortWithModel(byte[] magicBytesReturnedByPort, SerialPort port, RecordingModel model) 
        {
            boolean recognized = acceptsAsMagicBytes(magicBytesReturnedByPort);
            if(recognized)
            {
                model.registerMeasurementBeamPort(port);
            }
            else
            {
                port.closePort();
            }
        }
    };        

    static final byte[] PING_BYTES = new byte[] {(byte)0xFF,(byte)0xFF,(byte)0xFF};

    private final byte[] magicBytes;

    private SerialPortVisitor(byte[] magicBytes)
    {
        this.magicBytes = magicBytes;
    }

    public static Map<SerialPortVisitor, Boolean> getMapForRecordingVisits()
    {
        Map<SerialPortVisitor, Boolean> mapOfVisits = new EnumMap<>(SerialPortVisitor.class);
        for(SerialPortVisitor visitor : SerialPortVisitor.values())
        {
            mapOfVisits.put(visitor, Boolean.FALSE);
        }
        return mapOfVisits;
    }

    public boolean acceptsAsMagicBytes(byte[] bytes)
    {
        boolean accepts = Arrays.equals(this.magicBytes, bytes);

        return accepts;
    }

    protected byte[] getMagicBytes()
    {
        return Arrays.copyOf(magicBytes, magicBytes.length);
    }

    public abstract void registerThePortWithModel(byte[] magicBytesReturnedByPort, SerialPort port, RecordingModel model);

}