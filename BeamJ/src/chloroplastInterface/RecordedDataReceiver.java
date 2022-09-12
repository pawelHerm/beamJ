package chloroplastInterface;

import java.io.File;

import atomicJ.data.Channel1D;

public interface RecordedDataReceiver 
{
    public void signalSampleReceived(int signalIndex, double transmittanceValue);
    public void elapsedRecordingTime(long elapsedTimeInMiliseconds);
    public void signalSourcesCountChanged(int signalSourcesCountOld, int signalSourcesCountNew);
    public void signalTypeChanged(int signalIndex, LightSignalType signalTypeOld, LightSignalType signalTypeNew);

    public void recordedChannelChanged(String changedChannelKey);
    public void recordedChannelAdded(Channel1D channel);
    public void recordedChannelRemoved(String channelKeyOld);

    public void recordingDurationSecondsChanged(double durationInSecondsOld, double durationInSecondsNew);
    public void numberOfActinicPhasesChanged(int oldNumber, int newNumber);
    public void phaseDurationChanged(int phaseIndex, double durationOld, double durationNew);
    public void phaseUnitDurationChanged(int phaseIndex, StandardTimeUnit durationUnitOld, StandardTimeUnit durationUnitNew);
    public void sampleOutputFileChanged(File outputFileOld, File outputFileNew);
    public void outputFileSelectionEnabledChange(boolean enabledNew);
    public void sampleDataFormatTypeChanged(SaveFormatType<PhotometricResource> formatTypeOld, SaveFormatType<PhotometricResource> formatTypeNew);
    public void experimentDescriptionChanged(String descriptionOld, String descriptionNew);
}
