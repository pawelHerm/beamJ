package chloroplastInterface;

public class MeasuringBeamSettingsImmutable
{
    private final double measuringBeamFrequencyInHertz;
    private final double measuringLightMaxIntensityInPercent;
    private final boolean keepMeasuringBeamOnWhenIdle;

    public MeasuringBeamSettingsImmutable(double measuringBeamFrequencyInHertz, double measuringLightMaxIntensityInPercent, boolean keepMeasuringBeamOnWhenIdle)
    {
        this.measuringBeamFrequencyInHertz = measuringBeamFrequencyInHertz;
        this.measuringLightMaxIntensityInPercent = measuringLightMaxIntensityInPercent;
        this.keepMeasuringBeamOnWhenIdle = keepMeasuringBeamOnWhenIdle;
    }

    public double getMeasuringBeamFrequencyInHertz()
    {
        return measuringBeamFrequencyInHertz;
    }

    public double getMeasuringBeamMaxIntensityInPercent()
    {
        return measuringLightMaxIntensityInPercent;
    }

    public boolean isKeepMeasuringBeamOnWhenIdle()
    {
        return keepMeasuringBeamOnWhenIdle;
    }
}
