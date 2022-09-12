package chloroplastInterface;

import java.io.File;
import java.util.Date;
import chloroplastInterface.CalibrationSwingWorker.CalibrationPhase;
import chloroplastInterface.optics.SliderMountedFilter;

public interface RecordingModelListener
{
    public void numberOfActinicPhasesChanged(int oldNumber, int newNumber);
    public void phaseDurationChanged(int phaseIndex, double durationOld, double durationNew);
    public void phaseUnitDurationChanged(int phaseIndex, StandardTimeUnit durationUnitOld, StandardTimeUnit durationUnitNew);
    public void phaseEndTimeChanged(int phaseIndex, Date newDate);
    public void actinicLightIntensityInPercentChanged(int phaseIndex, double intensityInPercentOld, double intensityInPercentNew);
    public void actinicBeamFilterChanged(int phaseIndex, SliderMountedFilter filterOld, SliderMountedFilter filterNew);
    public void availableActinicBeamFiltersChanged(int oldNumber, int newNumber);
    public void propertiesOfActinicBeamFilterChanged(SliderMountedFilter filterNew);
    public void actinicBeamCalibrationFileChanged(File calibrationFileOld, File calibrationFileNew);
    public void absoluteLightIntensityChanged(int phaseIndex, double absoluteIntensityNew);

    public void supportOfSoftwareControlOfMeasuringBeamIntensityChanged(boolean supportedNew);
    public void updateMeasuringBeamFrequencyInHertz(double measuringLightFrequencyInHertzOld, double measuringLightFrequencyInHertzNew);
    public void maximalMeasuringBeamFrequencyInHertzChanged(double maxFrequencyInHertzOld, double maxFrequencyInHertzNew);
    public void preferredFrequencyIncrementAndDecrementChanged(double preferredIncrementOld, double preferredIncrementNew, double preferredDecrementOld, double preferredDecrementNew);
    public void measuringBeamIntensityInPercentChanged(double indensityInPercentOld,double indensityInPercentNew);
    public void setKeepMeasuringBeamOnWhenIdleChanged(boolean keepMeasuringBeamOnWhenIdleOld, boolean keepMeasuringBeamOnWhenIdleNew);
    public void measuringBeamControllerChanged(String descriptionOld, String descriptioNew);
    public void availabilityOfFunctionalMeasuringBeamControllersChange(boolean workingMeasuringBeamControllerAvilableOld, boolean workingMeasuringBeamControllerAvilableNew);

    public void latestCalibrationDateChanged(int signalIndex, Date atestCalibrationDateOld, Date atestCalibrationDateNew);
    public void calibrationOffsetInVoltsChanged(int signalIndex, double calibrationOffsetInVoltsOld, double calibrationOffsetInVoltsNew);
    public void calibrationSlopeInPercentsPerVoltChanged(int signalIndex, double calibrationSlopeInPercentsPerVoltOld, double calibrationSlopeInPercentsPerVoltNew);
    public void calibrationPhaseChanged(int signalIndex, CalibrationPhase calibrationPhaseOld, CalibrationPhase calibrationPhaseNew);
    public void progressInPercentOfCurrentCalibrationPhaseChanged(int signalIndex, int currentProgressInPercent);

    public void signalSourcesCountChanged(int usedSignaSourcesCountOld, int usedSignalSourcesCount);
    public void signalSourceControllerChanged(int signalIndex, SignalSourceController signalSourceControllerOld, SignalSourceController signalSourceControllerNew);
    public void signalTypeChanged(int signalIndex, LightSignalType signalTypeOld, LightSignalType signalTypeNew);
    public void signalSourceSelectionEnabledChange(boolean enabledNew);
    public void signalSamplesPerMinuteChanged(int signalIndex, double samplesPerMinuteOld, double samplesPerMinuteNew);
    public void maximalSignalSamplesPerMinuteChanged(int signalIndex, double maximalSamplesPerMinuteForSelectedSourceOld, double maximalSamplesPerMinuteForSelectedSourceNew);
    public void availabilityOfFunctionalSignalSourceControllersChange(boolean functionalSignalSourceControllerAvailableOld, boolean functionalSignalSourceControllerAvilableNew);
    public void functionalSignalControllerAdded(SignalSourceController controllerNew);
    public void functionalSignalControllerRemoved(SignalSourceController controller);

    public void measuringBeamFrequencyModificationEnabledChange(boolean enabledNew);
    public void measuringBeamIntensityModificationEnabledChange(boolean enabledNew);
    public void measuringBeamIdleStateBehaviourModificationEnabledChange(boolean enabledNew);
    public void functionalMeasuringBeamControllerAdded(String newMeasuringBeamDescription);
    public void statusOfConnectionWithMeasuringBeamControllerChanged(boolean connectedOld, boolean connectedNew);
    public void functionalMeasuringBeamControllerRemoved(String measuringBeamControllerDescription);

    public void statusOfConnectionWithActinicBeamControllerChanged(boolean connectedOld, boolean connectedNew);

    public void actinicBeamPhaseChange(long recordingStartAbsoluteTimeInMiliseconds, PhaseStamp phaseObjectOld, PhaseStamp phaseObjectNew);

    public void overlayPlotOnPreviousChanged(boolean overlayPlotOnPreviousOld, boolean overlayPlotOnPreviousNew);

    public void readActinicBeamPhasesFromFileEnabled(boolean enabledNew);

    public void runEnabledChange(boolean enabledNew);
    public void stopEnabledChange(boolean eabledNew);
    public void resumeEnabledChange(boolean eabledNew);
    public void cancelEnabledChange(boolean eabledNew);
    public void calibrationEnabledChange(int signalIndex, boolean enabledNew);
}