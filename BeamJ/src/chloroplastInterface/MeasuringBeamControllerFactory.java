package chloroplastInterface;

public interface MeasuringBeamControllerFactory 
{
    public MeasuringBeamController getController();
    public String getIdentifier();
    @Override
    public String toString();
}
