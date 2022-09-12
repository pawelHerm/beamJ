package chloroplastInterface.flipper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThorlabsFlipperFactory
{
    private static final String THORLABS_FLIPPER_PREFIX = "37";
    private static final int THORLABS_FLIPPER_PREFIX_LENGTH = 8;

    private static ThorlabsFlipperFactory INSTANCE = new ThorlabsFlipperFactory();

    private ThorlabsFlipperFactory(){};

    public static ThorlabsFlipperFactory getInstance()
    {
        return INSTANCE;
    }

    private static Map<String, ThorlabsFlipper> FLIPPERS = new HashMap<>();


    public Flipper getFlipperIfPossible(String serialNumber)
    {
        if(!isCompleteValidThorlabsFlipperSerialNumberAtLeastWhenTrimmed(serialNumber))
        {
            Logger.getLogger(ThorlabsFlipper.class.getName()).log(Level.SEVERE, "Invalid Thorlabs flipper serial number " + serialNumber);
            return DummyFlipper.getInstance();
        }

        if(FLIPPERS.containsKey(serialNumber))
        {
            return FLIPPERS.get(serialNumber);
        }

        try
        {
            ThorlabsFlipper flipper = ThorlabsFlipper.buildFlipper(serialNumber);
            FLIPPERS.put(serialNumber, flipper);
            return flipper;
        }
        catch(UnsatisfiedLinkError error)
        {
            Logger.getLogger(ThorlabsFlipper.class.getName()).log(Level.SEVERE, error.getMessage(), error);
            return DummyFlipper.getInstance();
        }
    }

    public static void shutdownAllCreatedThorlabsFlipperObjects()
    {
        for(ThorlabsFlipper flipper: FLIPPERS.values())
        {
            flipper.shutDown();
        }

        FLIPPERS.clear();
    }

    public static boolean isCompleteValidThorlabsFlipperSerialNumberAtLeastWhenTrimmed(String serialNumber)
    {
        if(Objects.isNull(serialNumber))
        {
            return false;
        }

        String trimmedSerialNumber = serialNumber.trim();
        boolean isValid = trimmedSerialNumber.startsWith(THORLABS_FLIPPER_PREFIX) && trimmedSerialNumber.length() == THORLABS_FLIPPER_PREFIX_LENGTH;

        return isValid;
    }
}