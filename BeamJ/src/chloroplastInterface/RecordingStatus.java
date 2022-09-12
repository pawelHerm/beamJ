package chloroplastInterface;

enum RecordingStatus
{
    IDLE(true,false,false,false, true, true, true, true, true),
    UNDER_CALIBRATION(false,false,false,false, false, false, false, false, true), 
    RUNNING(false,true,false,true, false, false, true, false, false), 
    ACTINIC_BEAM_SETTINGS_UNDER_MODIFICATION_WHEN_RUNNING(true,false,false,false, false, false, true, false, false), 
    STOPPED(false,false,true,true, true, true, true, true, false), 
    ACTINIC_BEAM_SETTINGS_UNDER_MODIFICATION_WHEN_STOPPED(false,false,true, true, true, true, true, true, false), 
    CANCELLING_IN_PROGRESS(false,false,false,false, false, false, false, false, false);

    private final boolean runEnabled;
    private final boolean stopEnabled;
    private final boolean resumeEnabled;
    private final boolean cancelEnabled;
    private final boolean calibrateEnabled;
    private final boolean measuringBeamParametersEnabled;
    private final boolean measuringBeamIdleStateModificationEnabled;
    private final boolean transmittanceSourceSelectionEnabled;
    private final boolean outputFileSelectionEnabled;

    RecordingStatus(boolean runEnabled, boolean stopEnabled, boolean resumeEnabled,
            boolean cancelEnabled, boolean calibrateEnabled, 
            boolean measuringBeamParametersEnabled, boolean measuringBeamIdelStateModificationEnabled, boolean transmittanceSourceSelectionEnabled, boolean outputFileSelectionEnabled)
    {
        this.runEnabled = runEnabled;
        this.stopEnabled = stopEnabled;
        this.resumeEnabled = resumeEnabled;
        this.cancelEnabled = cancelEnabled;
        this.calibrateEnabled = calibrateEnabled;
        this.measuringBeamParametersEnabled = measuringBeamParametersEnabled;
        this.measuringBeamIdleStateModificationEnabled = measuringBeamIdelStateModificationEnabled;
        this.transmittanceSourceSelectionEnabled = transmittanceSourceSelectionEnabled;
        this.outputFileSelectionEnabled = outputFileSelectionEnabled;
    }

    public boolean isRunEnabled(boolean connectionWithActinicBeamControllerEstablished, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.runEnabled && connectionWithActinicBeamControllerEstablished && connectionWithMeasuringBeamControllerEstablished && connectionWithTransmittanceSourceEstablished;
    }

    public boolean isStopEnabled(boolean connectionWithActinicBeamControllerEstablished, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.stopEnabled;
    }

    public boolean isResumeEnabled(boolean connectionWithActinicBeamControllerEstablished, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.resumeEnabled && connectionWithActinicBeamControllerEstablished && connectionWithMeasuringBeamControllerEstablished && connectionWithTransmittanceSourceEstablished;
    }

    public boolean isCancelEnabled(boolean connectionWithActinicBeamControllerEstablied, boolean connectionWithMeasuringBeamControllerEstablied, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.cancelEnabled;
    }

    public boolean isCalibrateEnabled(boolean connectionWithActinicBeamControllerEstablied, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.calibrateEnabled && connectionWithMeasuringBeamControllerEstablished && connectionWithTransmittanceSourceEstablished;
    }

    public boolean isMeasuringBeamParameterModificationsEnabled(boolean connectionWithActinicBeamControllerEstablied, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.measuringBeamParametersEnabled;
    }

    public boolean isMeasuringBeamIdleStateBehaviourModificationsEnabled(boolean connectionWithActinicBeamControllerEstablied, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.measuringBeamIdleStateModificationEnabled;
    }

    public boolean isSignalSourceSelectionEnabled(boolean connectionWithActinicBeamControllerEstablied, boolean connectionWithMeasuringBeamControllerEstablished, boolean connectionWithTransmittanceSourceEstablished)
    {
        return this.transmittanceSourceSelectionEnabled;
    }

    public boolean isOutputFileSelectionEnabled()
    {
        return outputFileSelectionEnabled;
    }

    public boolean iReadActinicBeamPhasesFromFileEnabled() 
    {
        return runEnabled;
    }
}