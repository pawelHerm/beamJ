package chloroplastInterface.flipper;

import java.util.logging.Level;
import java.util.logging.Logger;

import atomicJ.utilities.Validation;

public class ThorlabsFlipper implements Flipper
{
    private static final int DEFAULT_POLLING_FREQUENCY_IN_MILISECONDS = 200;
    public static final int SHORTEST_TRANSIT_TIME_IN_MILISECONDS = 300;
    public static final int DEFAULT_TRANSIT_TIME_IN_MILISECONDS = 500;
    public static final int LONGEST_TRANSIT_TIME_IN_MILISECONDS = 2800;

    private final String serialNumber;        

    private ThorlabsFlipper(String serialNumber)
    {           
        this.serialNumber = serialNumber.trim();
        ThorlabsFlipperMotionControl.INSTANCE.TLI_BuildDeviceList();
        initialize();
    }

    public static ThorlabsFlipper buildFlipper(String serialNumber)
    {
        ThorlabsFlipper flipper = new ThorlabsFlipper(serialNumber);
        return flipper;
    }

    private void initialize()
    {
        ThorlabsFlipperMotionControl.INSTANCE.FF_Open(this.serialNumber);
        ThorlabsFlipperMotionControl.INSTANCE.FF_StartPolling(this.serialNumber, DEFAULT_POLLING_FREQUENCY_IN_MILISECONDS);
    }

    void shutDown()
    {
        try
        {
            ThorlabsFlipperMotionControl.INSTANCE.FF_StopPolling(this.serialNumber);
            ThorlabsFlipperMotionControl.INSTANCE.FF_Close(this.serialNumber);
        }
        catch(UnsatisfiedLinkError error)
        {
            error.printStackTrace();
            Logger.getLogger(ThorlabsFlipper.class.getName()).log(Level.SEVERE, error.getMessage(), error);
        }
    }      

    @Override
    public boolean flipIfPossible()
    {         
        boolean flippedSuccessfully = false;

        int positionCode = ThorlabsFlipperMotionControl.INSTANCE.FF_GetPosition(serialNumber);
        FlipperPosition flipperPosition = FlipperPosition.getThorlabsFlipperPosition(positionCode);           

        if(flipperPosition.isKnown())
        {
            int response = ThorlabsFlipperMotionControl.INSTANCE.FF_MoveToPosition(serialNumber, flipperPosition.getNextPosition().getCode());
            flippedSuccessfully = (response == 0);
        }

        return flippedSuccessfully;
    }

    @Override
    public void moveToPositionIfPossible(FlipperPosition position)
    {            
        ThorlabsFlipperMotionControl.INSTANCE.FF_MoveToPosition(serialNumber, position.getCode());
    }

    @Override
    public FlipperPosition getPosition()
    {
        int positionCode = ThorlabsFlipperMotionControl.INSTANCE.FF_GetPosition(serialNumber);

        FlipperPosition flipperPosition = FlipperPosition.getThorlabsFlipperPosition(positionCode);

        return flipperPosition;
    }      

    @Override
    public int getTransitTimeInMiliseconds()
    {
        int transitTime = ThorlabsFlipperMotionControl.INSTANCE.FF_GetTransitTime(serialNumber);

        if(transitTime < SHORTEST_TRANSIT_TIME_IN_MILISECONDS || transitTime > LONGEST_TRANSIT_TIME_IN_MILISECONDS)
        {
            return DEFAULT_TRANSIT_TIME_IN_MILISECONDS;
        }

        return transitTime;
    }

    @Override
    public void setTransitTimeInMiliseconds(int transitTimeNew)
    {
        Validation.requireValueEqualToOrBetweenBounds(transitTimeNew, SHORTEST_TRANSIT_TIME_IN_MILISECONDS, LONGEST_TRANSIT_TIME_IN_MILISECONDS, "transitTimeNew");
        ThorlabsFlipperMotionControl.INSTANCE.FF_SetTransitTime(serialNumber, transitTimeNew);
    }

    public static boolean isTransitTimeInMilisecondsValid(int transitTime)
    {
        boolean valid = transitTime >= SHORTEST_TRANSIT_TIME_IN_MILISECONDS && transitTime <= LONGEST_TRANSIT_TIME_IN_MILISECONDS;
        return valid;
    }

    public static int ensureThatTransitTimeInMilisecondsIsValid(int transitTime)
    {
        if(isTransitTimeInMilisecondsValid(transitTime))
        {
            return transitTime;
        }

        return DEFAULT_TRANSIT_TIME_IN_MILISECONDS;
    }

    @Override
    public boolean isActive()
    {
        return true;
    }
}