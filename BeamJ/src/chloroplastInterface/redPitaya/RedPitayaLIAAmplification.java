package chloroplastInterface.redPitaya;

import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.Validation;

public class RedPitayaLIAAmplification 
{
    public static final String AMPLIFICATION_REGISTER_NAME = "sg_amp1";

    public static final int MAX_LEVEL_INDEX = 9;

    public static int getMaxLevelIndex()
    {
        return MAX_LEVEL_INDEX;
    }

    public static String getSettingCommand(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + AMPLIFICATION_REGISTER_NAME + " " +  Integer.toString(level);

        return command;
    }

    public static double getAmplificationFactor(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        double phase = MathUtilities.intPow(2, level);
        return phase;
    }
}