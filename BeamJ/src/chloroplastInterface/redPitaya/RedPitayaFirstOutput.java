package chloroplastInterface.redPitaya;

public class RedPitayaFirstOutput 
{
    public static final String REGISTER_NAME = "out1_sw";

    public static final int MAX_LEVEL_INDEX = 15;

    public static int getMaxLevelIndex()
    {
        return MAX_LEVEL_INDEX;
    }

    public static String getSettingCommandForCosineReferenceInFirstOutput()
    {
        String command = RedPitayaLockInDevice.PYTHON_LOCK_SETTING_CODE_PATH + " " + REGISTER_NAME + " " +  Integer.toString(5);

        return command;
    }

}
