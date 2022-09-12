package chloroplastInterface;

public class CalibratedSignalSample 
{
    private final double valueInPercents;
    private final double timeInSecondsSinceRecordingOnset;
    private final long absoluteTimeInMillis;

    public CalibratedSignalSample(double valueInPercents, long absoluteTimeInMillis, double timeInSecondsSinceOnset)
    {
        this.valueInPercents = valueInPercents;
        this.timeInSecondsSinceRecordingOnset = timeInSecondsSinceOnset;
        this.absoluteTimeInMillis = absoluteTimeInMillis;
    }

    public static CalibratedSignalSample getCalibrated(RawVoltageSample voltageSample, double slope, double offset, long absoluteTimeInMillis, long experimentOnsetInMillis)
    {        
        double transmittanceInPercents = slope*(voltageSample.getValueInVolts() - offset);
        double relativeTimeInSeconds = (voltageSample.getAbsoluteTimeInMilis() - experimentOnsetInMillis)/1000.;

        CalibratedSignalSample calibratedSample = new CalibratedSignalSample(transmittanceInPercents, absoluteTimeInMillis, relativeTimeInSeconds);

        return calibratedSample;
    }

    public long getAbsoluteTimeInMillis()
    {
        return absoluteTimeInMillis;
    }

    public double getTimeInSecondsSinceExperimentOnset()
    {
        return timeInSecondsSinceRecordingOnset;
    }

    public double getSignalValueInPercents()
    {
        return valueInPercents;
    }
}
