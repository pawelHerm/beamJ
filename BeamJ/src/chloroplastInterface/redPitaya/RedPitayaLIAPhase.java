package chloroplastInterface.redPitaya;

import atomicJ.utilities.Validation;

public class RedPitayaLIAPhase 
{
    public static final String PHASE_REGISTER_NAME = "gen_mod_phase";

    public static final int MAX_LEVEL_INDEX = 2519;
    private static final double BASIC_PERIOD_IN_DEGREES = 360./2520.;

    public static int getMaxLevelIndex()
    {
        return MAX_LEVEL_INDEX;
    }

    public static String getPhaseSettingCommand(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + PHASE_REGISTER_NAME + " " +  Integer.toString(level);

        return command;
    }

    public static double getPhaseInDegrees(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        double phase = level*BASIC_PERIOD_IN_DEGREES;
        return phase;
    }

    public static double getPhaseInRadians(int level)
    {
        Validation.requireNonNegativeParameterName(level, "level");
        Validation.requireValueSmallerOrEqualToParameterName(level, MAX_LEVEL_INDEX, "level");

        double phase = Math.PI*level*BASIC_PERIOD_IN_DEGREES/180.;
        return phase;
    }
}