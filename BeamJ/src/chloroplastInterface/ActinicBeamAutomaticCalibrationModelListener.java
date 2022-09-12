package chloroplastInterface;

import chloroplastInterface.optics.SliderMountedFilter;

public interface ActinicBeamAutomaticCalibrationModelListener
{
    public void numberOfPhasesChanged(int oldNumber, int newNumber);

    public void filterPromptingChanged(boolean promptForFilterChangeOld, boolean promptForFilterChangeNew);

    public void minimumActinicIntensityInPercentChanged(int phaseIndex, double minimumIntensityInPercentOld,double minimumIntensityInPercentNew);
    public void maximumActinicIntensityInPercentChanged(int phaseIndex, double maximumIntensityInPercentOld,double maximumIntensityInPercentNew);

    public void stepRecordingTimeChanged(int phaseIndex, double stepRecordingTimeOld, double stepRecordingTimeNew);
    public void unitOfStepRecordingTimeChanged(int phaseIndex, StandardTimeUnit stepRecordingTimeUnitOld, StandardTimeUnit stepRecordingTimeUnitNew);

    public void pauseBetweenStepsChanged(int phaseIndex, double pauseBetweenStepsOld, double pauseBetweenStepsNew);
    public void unitOfPauseBetweenStepsChanged(int phaseIndex, StandardTimeUnit pauseBetweenStepsUnitOld, StandardTimeUnit pauseBetweenStepsUnitNew);

    public void scaleTypeOfStepsChanged(int phaseIndex, ScaleType stepScaleTypeOld, ScaleType stepScaleTypeNew);
    public void stepCountChanged(int phaseIndex, int stepCountOld, int stepCountNew);

    public void filterChanged(int phaseIndex, SliderMountedFilter filterOld, SliderMountedFilter filterNew);

    public void absoluteLightIntensityUnitChanged(IrradianceUnitType absoluteLightIntensityUnitOld, IrradianceUnitType absoluteLightIntensityUnitNew);
    public void intensityPerVoltChanged(double intensityPerVoltOld, double intensityPerVoltNew);

    public void saveToFileEnabledChanged(boolean enabledOld, boolean enabledNew);
}