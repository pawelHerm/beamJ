package chloroplastInterface;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Nicaiu extends StdCallLibrary 
{
    Nicaiu INSTANCE = (Nicaiu) Native.loadLibrary("nicaiu", Nicaiu.class);

    public static final int DAQmx_Val_GroupByChannel = 0;
    public static final int DAQmx_Val_GroupByScanNumber = 1;
    public static final int DAQmx_Val_ChanPerLine = 0;
    public static final int DAQmx_Val_ChanForAllLines = 1;
    public static final int DAQmx_Val_Cfg_Default = -1;
    public static final int DAQmx_Val_Volts = 10348;
    public static final int DAQmx_Val_Amps = 10342;
    public static final int DAQmx_Val_Default = -1;
    public static final int DAQmx_Val_Rising = 10280;
    public static final int DAQmx_Val_FiniteSamps = 10178;
    public static final int DAQmx_Val_ContSamps = 10123;
    public static final int DAQmx_Val_OnDemand = 10390;

    /**
     * Original signature : <code>int32 DAQmxCreateDOChan(TaskHandle, const char[], const char[], int32)</code><br>
     * <i>native declaration : line 2511</i>
     */
    int DAQmxCreateDOChan(Pointer taskHandle, byte lines[], byte nameToAssignToLines[], int lineGrouping);

    /**
     * Original signature : <code>int32 DAQmxCreateTask(const char[], TaskHandle*)</code><br>
     * <i>native declaration : line 2410</i>
     */

    int DAQmxCreateDIChan(Pointer taskHandle, byte lines[], byte nameToAssignToLines[], int lineGrouping);
    /**
     * Original signature : <code>int32 DAQmxCreateDOChan(TaskHandle, const char[], const char[], int32)</code><br>
     * <i>native declaration : line 2511</i><br>
     */

    //PH changed the parameter type from byte[] to String
    int DAQmxCreateTask(String taskName, PointerByReference taskHandle);

    /**
     * Original signature : <code>int32 DAQmxStartTask(TaskHandle)</code><br>
     * <i>native declaration : line 2414</i>
     */
    int DAQmxStartTask(Pointer taskHandle);


    /**
     * Original signature : <code>int32 DAQmxCreateAIVoltageChan(TaskHandle, const char[], const char[], int32, float64, float64, int32, const char[])</code><br>
     * <i>native declaration : line 2443</i>
     */
    //(PH) changed type of physicalChannel, nameToAssignToChannel and customScaleName from byte[] to String, due to random problems encountered when the type byte[] was used
    int DAQmxCreateAIVoltageChan(Pointer taskHandle, String physicalChannel, String nameToAssignToChannel, int terminalConfig, double minVal, double maxVal, int units, String customScaleName);

    /**
     * (Analog/Counter Timing)<br>
     * Original signature : <code>int32 DAQmxCfgSampClkTiming(TaskHandle, const char[], float64, int32, int32, uInt64)</code><br>
     * <i>native declaration : line 2547</i>
     */
    //PH changed the type of source from byte[] to String
    int DAQmxCfgSampClkTiming(Pointer taskHandle, String source, double rate, int activeEdge, int sampleMode, long sampsPerChan);

    /**
     * Original signature : <code>int32 DAQmxCreateAICurrentChan(TaskHandle, const char[], const char[], int32, float64, float64, int32, int32, float64, const char[])</code><br>
     * <i>native declaration : line 2444</i>
     */
    int DAQmxCreateAICurrentChan(Pointer taskHandle, byte physicalChannel[], byte nameToAssignToChannel[], int terminalConfig, double minVal, double maxVal, int units, int shuntResistorLoc, double extShuntResistorVal, byte customScaleName[]);


    /**
     * Original signature : <code>int32 DAQmxReadAnalogF64(TaskHandle, int32, float64, bool32, float64[], uInt32, int32*, bool32*)</code><br>
     * <i>native declaration : line 2601</i>
     */
    int DAQmxReadAnalogF64(Pointer taskHandle, int numSampsPerChan, double timeout, NativeLong fillMode, DoubleBuffer readArray, NativeLong arraySizeInSamps, IntBuffer sampsPerChanRead, NativeLongByReference reserved);


    /**
     * Original signature : <code>int32 DAQmxReadAnalogScalarF64(TaskHandle, float64, float64*, bool32*)</code><br>
     * <i>native declaration : line 2602</i>
     */
    int DAQmxReadAnalogScalarF64(Pointer taskHandle, double timeout, DoubleBuffer value, NativeLongByReference reserved);

    /**
     * Original signature : <code>int32 DAQmxStopTask(TaskHandle)</code><br>
     * <i>native declaration : line 2415</i>
     */

    int DAQmxReadDigitalU32(Pointer taskHandle, int numSampsPerChan, double timeout, NativeLong fillMode, DoubleBuffer readArray, NativeLong arraySizeInSamps, IntBuffer sampsPerChanRead, NativeLongByReference reserved);
    /**
     * Original signature : <code>int32 DAQmxReadDigitalScalarU32(TaskHandle, float64, uInt32*, bool32*)</code><br>
     * <i>native declaration : line 2615</i>
     */

    int DAQmxStopTask(Pointer taskHandle);

    /**
     * Original signature : <code>int32 DAQmxClearTask(TaskHandle)</code><br>
     * <i>native declaration : line 2417</i>
     */
    int DAQmxClearTask(Pointer taskHandle);

    /**
     * Original signature : <code>int32 DAQmxResetDevice(const char[])</code><br>
     * <i>native declaration : line 2967</i>
     */
    int DAQmxResetDevice(byte deviceName[]);

    int DAQmxGetNthTaskChannel(Pointer taskHandle, NativeLong index, String buffer, int bufferSize);
    /**
     * Original signature : <code>int32 DAQmxGetNthTaskChannel(TaskHandle, uInt32, char[], int32)</code><br>
     * <i>native declaration : line 2424</i>
     */

    int DAQmxGetPhysicalChanName(Pointer taskHandle, String channel, String data, NativeLong bufferSize);
    /**
     * *** Set/Get functions for DAQmx_PhysicalChanName ***<br>
     * Original signature : <code>int32 DAQmxGetPhysicalChanName(TaskHandle, const char[], char*, uInt32)</code><br>
     * <i>native declaration : line 5375</i>
     */

    /**
     * Original signature : <code>int32 DAQmxReadDigitalLines(TaskHandle, int32, float64, bool32, uInt8[], uInt32, int32*, int32*, bool32*)</code><br>
     * <i>native declaration : line 2616</i>
     */
    int DAQmxReadDigitalLines(Pointer taskHandle, int numSampsPerChan, double timeout, NativeLong fillMode, ByteBuffer readArray, NativeLong arraySizeInBytes, IntBuffer sampsPerChanRead, IntBuffer numBytesPerSamp, NativeLongByReference reserved);

    /**
     * *** Set/Get functions for DAQmx_Sys_DevNames ***<br>
     * Original signature : <code>int32 DAQmxGetSysDevNames(char*, uInt32)</code><br>
     * <i>native declaration : line 6052</i>
     */
    int DAQmxGetSysDevNames(ByteBuffer data, NativeLong bufferSize);

    /**
     * *** Set/Get functions for DAQmx_Dev_SerialNum ***<br>
     * Original signature : <code>int32 DAQmxGetDevSerialNum(const char[], uInt32*)</code><br>
     * <i>native declaration : line 5614</i>
     */
    //(PH) I had problems when the type of device parameter was byte[]
    int DAQmxGetDevSerialNum(String device, NativeLongByReference data);

    /**
     * *** Set/Get functions for DAQmx_Dev_IsSimulated ***<br>
     * Original signature : <code>int32 DAQmxGetDevIsSimulated(const char[], bool32*)</code><br>
     * <i>native declaration : line 5605</i>
     */
    //(PH) I changed the type of the device parameter to String (from byte[[)
    int DAQmxGetDevIsSimulated(String device, NativeLongByReference data);


    /**
     * *** Set/Get functions for DAQmx_Dev_AI_PhysicalChans ***<br>
     * Original signature : <code>int32 DAQmxGetDevAIPhysicalChans(const char[], char*, uInt32)</code><br>
     * <i>native declaration : line 5630</i>
     */
    //PH changed the type of the device parameter to String
    int DAQmxGetDevAIPhysicalChans(String device, ByteBuffer data, NativeLong bufferSize);

    /**
     * *** Set/Get functions for DAQmx_AIConv_MaxRate ***<br>
     * Original signature : <code>int32 DAQmxGetAIConvMaxRate(TaskHandle, float64*)</code><br>
     * <i>native declaration : line 6202</i>
     */
    int DAQmxGetAIConvMaxRate(Pointer taskHandle, DoubleBuffer data);
    /**
     * *** Set/Get functions for DAQmx_Dev_AI_MaxSingleChanRate ***<br>
     * Original signature : <code>int32 DAQmxGetDevAIMaxSingleChanRate(const char[], float64*)</code><br>
     * <i>native declaration : line 5635</i>
     */
    int DAQmxGetDevAIMaxSingleChanRate(String deviceName, DoubleBuffer data);

    /**
     * Original signature : <code>int32 DAQmxCreateAOVoltageChan(TaskHandle, const char[], const char[], float64, float64, int32, const char[])</code><br>
     * <i>native declaration : line 2505</i><br>
     */
    int DAQmxCreateAOVoltageChan(Pointer transmittanceTask, String channelDescription, String nameToAssignToChannel, double minOutputVoltage, double maxOutputVoltage, int daqmxValVolts, String customScaleName);

    /**
     * Original signature : <code>int32 DAQmxWriteAnalogF64(TaskHandle, int32, bool32, float64, bool32, const float64[], int32*, bool32*)</code><br>
     * <i>native declaration : line 2654</i>
     */
    int DAQmxWriteAnalogF64(Pointer taskHandle, int numSampsPerChan, NativeLong autoStart, double timeout, NativeLong dataLayout, DoubleBuffer writeArray, IntBuffer sampsPerChanWritten, NativeLongByReference reserved);

    //int32 DAQmxWriteAnalogScalarF64 (TaskHandle taskHandle, bool32 autoStart, float64 timeout, float64 value, bool32 *reserved);
    int DAQmxWriteAnalogScalarF64(Pointer taskHandle, NativeLong autoStart, double timeout, double value, NativeLongByReference reserved);
}
