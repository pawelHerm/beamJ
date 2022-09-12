package chloroplastInterface;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitUtilities;
import atomicJ.utilities.Validation;
import jvisa.JVisaException;
import jvisa.JVisaInstrument;
import jvisa.JVisaResourceManager;

public class KeithleyPicoammeterSCPIController
{
    private final static String RESET_COMMAND = "*RST\n";
    private final static String ZERO_CHECK_ON_COMMAND = "SYST:ZCH ON\n";
    private final static String ZERO_CHECK_OFF_COMMAND = "SYST:ZCH OFF\n";
    private final static String ZERO_CORRECT_ON = "SYST:ZCOR ON\n";
    private final static String LOWEST_RANGE_COMMAND = "RANG 2e-9\n";
    private final static String AUTO_RANGE_COMMAND = "RANG:AUTO ON\n";
    private final static String TRIGGER_ONE_READING_COMMAND = "INIT\n";
    private final static String USE_LAST_READING_FOR_ZERO_CORRECTION = "SYST:ZCOR:ACQ\n";

    private final static String NPLC_COMMAND_PREFIX = "NPLC";

    private final static double MIN_READING_SPEED_IN_PLC = 0.1;
    private final static double MAX_READING_SPEED_IN_PLC = 50;

    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(.+?)(?:\\[|\\()(.*)(?:\\]|\\))");

    private final static double DEFAULT_READING_SPEED_IN_PLC = 5;

    private final String resourceName;
    private JVisaResourceManager resourceManager;
    private JVisaInstrument instrumentSerial;

    private double readingSpeedInPLC = DEFAULT_READING_SPEED_IN_PLC;

    public KeithleyPicoammeterSCPIController(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public void initialize()
    {
        try {
            resourceManager = new JVisaResourceManager();
            instrumentSerial = resourceManager.openInstrument(resourceName);

        } catch (JVisaException ex) {
            System.err.println("Couldn't open the default resource manager");
            ex.printStackTrace();
            return;
        } catch (UnsatisfiedLinkError err) {
            System.err.println("Couldn't load nivisa.dll");
            err.printStackTrace();
            return;
        }
    }

    public double getReadingSpeedInPLC()
    {
        return readingSpeedInPLC;
    }

    public void setReadingSpeedInPLC(double readingSpeedInPLCNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(readingSpeedInPLCNew, MIN_READING_SPEED_IN_PLC, MAX_READING_SPEED_IN_PLC, "readingSpeedInPLCNew");
        this.readingSpeedInPLC = readingSpeedInPLCNew;
        String integrationLengthCommand = getRateCommands(readingSpeedInPLCNew);
        try {
            instrumentSerial.write(integrationLengthCommand);
        } catch (JVisaException e) {
            e.printStackTrace();
        }
    }

    public UnitExpression readCurrent() throws JVisaException
    {
        ByteBuffer reading = instrumentSerial.sendAndReceiveBytes("READ?\n");           
        Charset charset = Charset.forName("UTF-8");
        String response = charset.decode(reading).toString();


        String currentValueAsString = response.trim().split(",")[0].trim();
        int n = currentValueAsString.length();

        double value = Double.parseDouble(currentValueAsString.substring(0, n-1));

        String unit = currentValueAsString.substring(n-1,n);

        UnitExpression currentReading = new UnitExpression(value, UnitUtilities.getSIUnit(unit));

        return currentReading;
    }

    public void prepareInstrumentAndPerformZeroCorrection()
    {
        try
        {            
            instrumentSerial.write(RESET_COMMAND);
            instrumentSerial.write(ZERO_CHECK_ON_COMMAND);
            instrumentSerial.write(LOWEST_RANGE_COMMAND);
            instrumentSerial.write(getRateCommands(DEFAULT_READING_SPEED_IN_PLC));//slow reading
            instrumentSerial.write(TRIGGER_ONE_READING_COMMAND);
            instrumentSerial.write(USE_LAST_READING_FOR_ZERO_CORRECTION);
            instrumentSerial.write(ZERO_CORRECT_ON);
            instrumentSerial.write(AUTO_RANGE_COMMAND);
            instrumentSerial.write(ZERO_CHECK_OFF_COMMAND);

        }
        catch (JVisaException e) {
            e.printStackTrace();
        }
    }

    private static String getRateCommands(double integrationTimeInPLC)
    {
        String command = NPLC_COMMAND_PREFIX + " " + Double.toString(integrationTimeInPLC) +"\n";
        return command;
    }
}
