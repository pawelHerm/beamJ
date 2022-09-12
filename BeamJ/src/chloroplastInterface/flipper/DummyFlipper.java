package chloroplastInterface.flipper;

public class DummyFlipper implements Flipper
{    
    private static DummyFlipper INSTANCE = new DummyFlipper();
    private DummyFlipper(){};

    public static DummyFlipper getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean flipIfPossible(){return false;};
    @Override
    public void moveToPositionIfPossible(FlipperPosition position){};   
    @Override
    public FlipperPosition getPosition(){
        return FlipperPosition.UNKNOWN;
    }

    @Override
    public int getTransitTimeInMiliseconds()
    {
        return ThorlabsFlipper.DEFAULT_TRANSIT_TIME_IN_MILISECONDS;
    };

    @Override
    public void setTransitTimeInMiliseconds(int transitTimeNew) {            
    }

    @Override
    public boolean isActive() {
        return false;
    }
}