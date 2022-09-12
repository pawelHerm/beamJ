package chloroplastInterface;

public class CalibrationSettingsImmutable 
{
    private final double calibrationSlopeInPercentsPerVolt;
    private final double calibrationOffsetInVolts;

    CalibrationSettingsImmutable(double calibrationSlopeInPercentsPerVolt, double calibrationOffsetInVolts)
    {
        this.calibrationSlopeInPercentsPerVolt = calibrationSlopeInPercentsPerVolt;
        this.calibrationOffsetInVolts = calibrationOffsetInVolts;

    }

    public double getCalibrationSlopeInPercentsPerVolt()
    {
        return calibrationSlopeInPercentsPerVolt;
    }

    public double getCalibrationOffsetInVolts()
    {
        return calibrationOffsetInVolts;
    }

    public boolean isCalibrationKnown() 
    {
        boolean calibrationKnown = !Double.isNaN(this.calibrationOffsetInVolts) && !Double.isNaN(this.calibrationSlopeInPercentsPerVolt);
        return calibrationKnown;
    }

    @Override
    public int hashCode()
    {
        int result = Double.hashCode(this.calibrationSlopeInPercentsPerVolt);
        result = 31*result + Double.hashCode(this.calibrationOffsetInVolts);

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof CalibrationSettingsImmutable)
        {
            CalibrationSettingsImmutable that = (CalibrationSettingsImmutable)o;
            boolean equal = (Double.compare(this.calibrationSlopeInPercentsPerVolt, that.calibrationSlopeInPercentsPerVolt) == 0);
            equal = equal && (Double.compare(this.calibrationOffsetInVolts, that.calibrationOffsetInVolts) == 0);

            return equal;
        }

        return false;
    }
}
