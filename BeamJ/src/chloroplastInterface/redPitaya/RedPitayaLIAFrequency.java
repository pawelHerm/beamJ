package chloroplastInterface.redPitaya;

import java.util.ArrayList;
import java.util.List;

import atomicJ.geometricSets.RealSet;
import atomicJ.geometricSets.StandardDiscreteRealSet;
import atomicJ.utilities.Validation;

public class RedPitayaLIAFrequency 
{
    public static final String FREQUENCY_REGISTER_NAME = "gen_mod_hp";

    public static final int MAX_LEVEL_INDEX = 16383;//2^14 - 1
    private static final double BASIC_PERIOD_IN_MICROSECONDS = 20.160;
    private static final double MIN_SUPPORTED_FREQUENCY = 1e6/((MAX_LEVEL_INDEX + 1.)*BASIC_PERIOD_IN_MICROSECONDS);
    private static final double MAX_SUPPORTED_FREQUENCY = 1e6/BASIC_PERIOD_IN_MICROSECONDS;

    public static int getMaxLevelIndex()
    {
        return MAX_LEVEL_INDEX;
    }  

    public static double getMaximalSupportedFrequencyInHertz()
    {
        return MAX_SUPPORTED_FREQUENCY;
    }

    static String getFrequencySettingCommand(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + FREQUENCY_REGISTER_NAME + " " +  Integer.toString(level);

        return command;
    }

    static double getPeriodInMiliseconds(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        double period = (level + 1)*BASIC_PERIOD_IN_MICROSECONDS;
        return period;
    }

    static double getFrequencyInHertz(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        double period = (level + 1)*BASIC_PERIOD_IN_MICROSECONDS;
        double freq = 1e6/period;
        return freq;
    }

    //we use List<Double> instead of double[] for convenient use in SpinnerModel
    //the returned list can be modified at will be the caller
    static List<Double> getListOfSupportedFrequenciesInHertzInAscendingOrder()
    {
        List<Double> values = new ArrayList<>(MAX_LEVEL_INDEX + 1);

        for(int level = MAX_LEVEL_INDEX; level >= 0; level--)
        {
            double period = (level + 1)*BASIC_PERIOD_IN_MICROSECONDS;
            Double freq = Double.valueOf(1e6/period);
            values.add(freq);
        }

        return values;
    }

    public static RealSet getSupportedFrequencies()
    {
        RealSet supportedFrequencies = StandardDiscreteRealSet.getInstance(getListOfSupportedFrequenciesInHertzInAscendingOrder());
        return supportedFrequencies;
    }

    static double[] getSupportedFrequenciesInHertzInAscendingOrder()
    {
        double[] values = new double[MAX_LEVEL_INDEX + 1];

        for(int level = MAX_LEVEL_INDEX; level >= 0; level--)
        {
            double period = (level + 1)*BASIC_PERIOD_IN_MICROSECONDS;
            values[level] = 1e6/period;
        }

        return values;
    }

    private static int forceLevelIntoValidRange(int level)
    {
        int levelInRange = Math.min(Math.max(level, 0), MAX_LEVEL_INDEX);
        return levelInRange;
    }

    public static boolean isDesiredFrequencyMismatchWithinTolerance(double desiredFrequencyInHertz)
    {
        return isDesiredFrequencyMismatchWithinTolerance(desiredFrequencyInHertz, 1e-3);
    }

    public static boolean isDesiredFrequencyMismatchWithinTolerance(double desiredFrequencyInHertz, double tolerance)
    {
        double periodInMicroseconds = 1e6/desiredFrequencyInHertz;
        int levelCeiling = forceLevelIntoValidRange((int)Math.ceil(periodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1;
        int levelFloor= forceLevelIntoValidRange((int)(Math.floor(periodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1);

        double frequencyCeiling = 1e6/((levelCeiling + 1.)*BASIC_PERIOD_IN_MICROSECONDS);
        double frequencyFloor = 1e6/((levelFloor + 1.)*BASIC_PERIOD_IN_MICROSECONDS);

        double minError = Math.min(Math.abs(desiredFrequencyInHertz - frequencyCeiling), Math.abs(desiredFrequencyInHertz - frequencyFloor));

        boolean errorWithinTolerance = minError <= tolerance;

        return errorWithinTolerance;
    }

    public static double getPreferredFrequencyIncrement(double currentFrequency)
    {
        Validation.requireNotNaNParameterName(currentFrequency, "currentFrequency");
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, MAX_SUPPORTED_FREQUENCY, "currentFrequency");

        double desiredFrequency = currentFrequency + 1;
        double desiredPeriodInMicroseconds = 1e6/desiredFrequency; //frequencyInHertz cannot be equal to 0, because MIN_SUPPORTED_FREQUENCY > 0
        int levelCeiling = forceLevelIntoValidRange((int)Math.ceil(desiredPeriodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1;
        int levelFloor= forceLevelIntoValidRange((int)(Math.floor(desiredPeriodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1);

        double desiredFrequencyFloor = Math.max(currentFrequency, Math.min(MAX_SUPPORTED_FREQUENCY, 1e6/((levelCeiling + 1.)*BASIC_PERIOD_IN_MICROSECONDS)));
        double desiredFrequencyCeiling = Math.max(currentFrequency, Math.min(MAX_SUPPORTED_FREQUENCY, 1e6/((levelFloor + 1.)*BASIC_PERIOD_IN_MICROSECONDS)));

        //if frequencyFloor is equal or very close (close) to current frequency, we will use frequencyCeilling
        if(Math.abs(desiredFrequencyFloor - currentFrequency) < 0.1)
        {
            double freqIncrement = desiredFrequencyCeiling - currentFrequency;

            return freqIncrement;
        }

        double freqIncrement = Math.abs(desiredFrequency - desiredFrequencyFloor) < Math.abs(desiredFrequency - desiredFrequencyCeiling) ? desiredFrequencyFloor - currentFrequency: desiredFrequencyCeiling- currentFrequency;

        return freqIncrement;
    }

    public static double getPreferredFrequencyDecrement(double currentFrequency)
    {
        Validation.requireNotNaNParameterName(currentFrequency, "currentFrequency");
        Validation.requireValueSmallerOrEqualToParameterName(currentFrequency, MAX_SUPPORTED_FREQUENCY, "currentFrequency");

        double desiredFrequency = currentFrequency - 1;
        double desiredPeriodInMicroseconds = 1e6/desiredFrequency; //frequencyInHertz cannot be equal to 0, because MIN_SUPPORTED_FREQUENCY > 0
        int levelCeiling = forceLevelIntoValidRange((int)Math.ceil(desiredPeriodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1;
        int levelFloor= forceLevelIntoValidRange((int)(Math.floor(desiredPeriodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1);

        double desiredFrequencyFloor = Math.min(currentFrequency, Math.max(MIN_SUPPORTED_FREQUENCY, 1e6/((levelCeiling + 1.)*BASIC_PERIOD_IN_MICROSECONDS)));
        double desiredFrequencyCeiling = Math.min(currentFrequency, Math.max(MIN_SUPPORTED_FREQUENCY, 1e6/((levelFloor + 1.)*BASIC_PERIOD_IN_MICROSECONDS)));

        //if frequencyCeiling is equal or very close to current frequency, we will use frequencyFloor
        if(Math.abs(desiredFrequencyCeiling - currentFrequency) < 0.1)
        {
            double decrement = currentFrequency - desiredFrequencyFloor;
            return decrement;
        }

        double freq = Math.abs(desiredFrequency - desiredFrequencyFloor) < Math.abs(desiredFrequency - desiredFrequencyCeiling) ? currentFrequency - desiredFrequencyFloor : currentFrequency - desiredFrequencyCeiling;
        return freq;
    }

    static int getClosestLevelForDesiredFrequency(double frequencyInHertz)
    {
        Validation.requireNotNaNParameterName(frequencyInHertz, "frequencyInHertz");

        if(frequencyInHertz <= MIN_SUPPORTED_FREQUENCY)
        {
            return MAX_LEVEL_INDEX;
        }

        if(frequencyInHertz >= MAX_SUPPORTED_FREQUENCY)
        {
            return 0;
        }

        double periodInMicroseconds = 1e6/frequencyInHertz; //frequencyInHertz cannot be equal to 0, because MIN_SUPPORTED_FREQUENCY > 0
        int levelCeiling = forceLevelIntoValidRange((int)Math.ceil(periodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1;
        int levelFloor= forceLevelIntoValidRange((int)(Math.floor(periodInMicroseconds/BASIC_PERIOD_IN_MICROSECONDS)) - 1);

        double frequencyCeiling = 1e6/((levelCeiling + 1.)*BASIC_PERIOD_IN_MICROSECONDS);
        double frequencyFloor = 1e6/((levelFloor + 1.)*BASIC_PERIOD_IN_MICROSECONDS);

        int level = Math.abs(frequencyInHertz - frequencyCeiling) < Math.abs(frequencyInHertz - frequencyFloor) ? levelCeiling : levelFloor;
        return level;
    }

    public static double getClosestSupportedFrequency(double desiredFrequencyInHertz)
    {
        Validation.requireNonNegativeParameterName(desiredFrequencyInHertz, "desiredFrequencyInHertz");
        Validation.requireNotNaNParameterName(desiredFrequencyInHertz, "desiredFrequencyInHertz");

        int level = getClosestLevelForDesiredFrequency(desiredFrequencyInHertz);

        double frequency = getFrequencyInHertz(level);

        return frequency;
    }
}