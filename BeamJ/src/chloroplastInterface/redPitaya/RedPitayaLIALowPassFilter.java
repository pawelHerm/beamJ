package chloroplastInterface.redPitaya;

import atomicJ.utilities.Validation;

public class RedPitayaLIALowPassFilter 
{
    public static final String REGISTER_NAME = "lpf_F1";

    public static final int MAX_LEVEL_INDEX = 47;
    public static final int MAX_ORDER = 2;
    public static final int MAX_CUTOFF_INDEX = 15;

    private static final double BASIC_PERIOD_IN_MICROSECONDS = 131;

    public static int getMaxLevelIndex()
    {
        return MAX_LEVEL_INDEX;
    }

    public static String getSettingCommand(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + REGISTER_NAME + " " +  Integer.toString(level);

        return command;
    }

    public static String getSettingCommand(int cutOffIndex, int order)
    {
        int level = getLevel(cutOffIndex, order);
        String command = getSettingCommand(level);
        return command;
    }

    public static int getLevel(int cutOffIndex, int order)
    {
        Validation.requireNonNegativeParameterName(cutOffIndex, "cutOffIndex");
        Validation.requireValueSmallerOrEqualToParameterName(cutOffIndex, MAX_CUTOFF_INDEX, "cutOffIndex");

        Validation.requireNonNegativeParameterName(order, "order");
        Validation.requireValueSmallerOrEqualToParameterName(order, MAX_ORDER, "order");

        int level = cutOffIndex + order*16;

        return level;
    }

    public static int getFilterOrder(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        int order = level/16;
        return order;
    }

    public static double getCutOffFrequency(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        double frequencySetting = level % 16;
        double period = BASIC_PERIOD_IN_MICROSECONDS*(frequencySetting + 1);
        double phase = 1e6/period;
        return phase;
    }
}