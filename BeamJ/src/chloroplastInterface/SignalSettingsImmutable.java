package chloroplastInterface;

public class SignalSettingsImmutable
{
    private final SignalSamplingSettingsImmutable samplingSettings;
    private final CalibrationSettingsImmutable calibrationSettings;
    private final LightSignalType signalType;

    public SignalSettingsImmutable(SignalSamplingSettingsImmutable samplingSettings, CalibrationSettingsImmutable calibrationSettings, LightSignalType signalType)
    {
        this.samplingSettings = samplingSettings;
        this.calibrationSettings = calibrationSettings;
        this.signalType = signalType;
    }
    
    public LightSignalType getSignalType()
    {
        return signalType;
    }

    public SignalSamplingSettingsImmutable getSamplingSettings()
    {
        return samplingSettings;
    }

    public CalibrationSettingsImmutable getCalibrationSettings()
    {
        return calibrationSettings;
    }
}