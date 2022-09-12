package chloroplastInterface;

public interface OpticsConfigurationListener
{
    public void numberOfActinicBeamFiltersChanged(int oldNumber, int newNumber);
    public void actinicBeamFilterTransmittanceInPercentChanged(int sliderPositionIndex, double transmittanceInPercentOld,double transmittanceInPercentNew);
    public void saveToFileEnabledChanged(boolean enabledOld, boolean enabledNew);
}