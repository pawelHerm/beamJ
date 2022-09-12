package chloroplastInterface.redPitaya;

public class RedPitayaInput 
{
    public static final String INPUT_REGISTER_NAME = "signal_sw";

    public static final int MAX_LEVEL_INDEX = 11;

    public static int getMaxLevelIndex()
    {
        return MAX_LEVEL_INDEX;
    }

    public static String getSettingCommandForFirstInput()
    {
        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + INPUT_REGISTER_NAME + " " +  Integer.toString(0);

        return command;
    }

    public static String getSettingCommandForSecondInput()
    {
        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + INPUT_REGISTER_NAME + " " +  Integer.toString(1);

        return command;
    }

    public static String getSettingCommandForCosineReferenceInput()
    {
        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + INPUT_REGISTER_NAME + " " +  Integer.toString(1);

        return command;
    }
}