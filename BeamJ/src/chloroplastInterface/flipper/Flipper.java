package chloroplastInterface.flipper;

public interface Flipper
{
    public boolean flipIfPossible();
    public void moveToPositionIfPossible(FlipperPosition position);        
    public FlipperPosition getPosition();  
    public int getTransitTimeInMiliseconds();
    public void setTransitTimeInMiliseconds(int transitTimeNew);
    public boolean isActive();
}