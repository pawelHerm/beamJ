package chloroplastInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.filechooser.FileFilter;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.NumberUtilities;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.optics.SliderMountedFilter;

public class ActinicBeamAutomaticCalibrationModel 
{
    private final static String ACTINIC_BEAM_CALIBRATION = "ActinicBeamCalibration";
    private final static String ABSOLUTE_LIGHT_INTENSITY_UNIT = "AbsoluteLightIntensityUnit";
    private final static String INTENSITY_UNITS_PER_VOLT = "IntensityUnitsPerVolt";
    private final static String PROMPT_FOR_FILTER_CHANGE = "PromptForFilterChange";

    private final static Preferences PREF = Preferences.userNodeForPackage(ActinicBeamAutomaticCalibrationModel.class).node(ActinicBeamAutomaticCalibrationModel.class.getName());

    private List<ActinicPhaseAutomaticCalibration> calibrationPhases;
    private final List<ActinicBeamAutomaticCalibrationModelListener> modelListeners = new ArrayList<>();

    private IrradianceUnitType absoluteLightIntensityUnit;
    private double intensityPerVolt;
    private boolean promptForFilterChange;

    private boolean readActinicBeamPhasesFromFileEnabled;

    private boolean savingToFileEnabled;

    private final OpticsConfigurationModel opticsConfigurationModel;

    public ActinicBeamAutomaticCalibrationModel(OpticsConfigurationModel opticsConfigurationModel)
    {    
        this.calibrationPhases = (List<ActinicPhaseAutomaticCalibration>)SerializationUtilities.getSerializableObject(PREF, ACTINIC_BEAM_CALIBRATION, new ArrayList<>(Arrays.asList(new ActinicPhaseAutomaticCalibration())));
        this.absoluteLightIntensityUnit = IrradianceUnitType.getValue(PREF.get(ABSOLUTE_LIGHT_INTENSITY_UNIT, IrradianceUnitType.MICROMOLES_PER_SQUARE_METER_PER_SECOND.name()), IrradianceUnitType.MICROMOLES_PER_SQUARE_METER_PER_SECOND);
        this.intensityPerVolt = PREF.getDouble(INTENSITY_UNITS_PER_VOLT, Double.NaN);
        this.promptForFilterChange = PREF.getBoolean(PROMPT_FOR_FILTER_CHANGE, true);

        this.opticsConfigurationModel = opticsConfigurationModel;

        checkWhetherAllSettingsSpecified();
    }

    public void close()
    {
        saveSettingsToPreferences();
    }

    private void saveSettingsToPreferences()
    {
        PREF.put(ABSOLUTE_LIGHT_INTENSITY_UNIT, absoluteLightIntensityUnit.name());
        PREF.putDouble(INTENSITY_UNITS_PER_VOLT, intensityPerVolt);
        PREF.putBoolean(PROMPT_FOR_FILTER_CHANGE, promptForFilterChange);

        try {
            SerializationUtilities.putSerializableObject(PREF, ACTINIC_BEAM_CALIBRATION, this.calibrationPhases);
        } catch (ClassNotFoundException | IOException
                | BackingStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        flushPreferences(PREF);
    }

    public boolean isPromptForFilterChange()
    {
        return promptForFilterChange;
    }

    public void setPromptForFilterChange(boolean promptForFilterChangeNew)
    {
        if(this.promptForFilterChange != promptForFilterChangeNew)
        {
            boolean promptForFilterChangeOld = this.promptForFilterChange;
            this.promptForFilterChange = promptForFilterChangeNew;

            notifyAboutChangeInFilterPrompting(promptForFilterChangeOld, promptForFilterChangeNew);
        }
    }

    public double getIntensityUnitsPerVolt()
    {
        return intensityPerVolt;
    }

    public void setIntensityUnitsPerVolt(double intensityPerVoltNew)
    {
        if(Double.compare(this.intensityPerVolt, intensityPerVoltNew)!= 0)
        {
            double intensityPerVoltOld = this.intensityPerVolt;
            this.intensityPerVolt = intensityPerVoltNew;

            notifyAbountIntensityPerVoltChanged(intensityPerVoltOld, intensityPerVoltNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public IrradianceUnitType getAbsoluteLightIntensityUnitType()
    {
        return absoluteLightIntensityUnit;
    }

    public void setAbsoluteLightIntensityUnitType(IrradianceUnitType absoluteLightIntensityUnitNew)
    {
        Validation.requireNonNullParameterName(absoluteLightIntensityUnitNew, "absoluteLightIntensityUnitNew");

        if(!Objects.equals(this.absoluteLightIntensityUnit, absoluteLightIntensityUnitNew))
        {
            IrradianceUnitType absoluteLightIntensityUnitOld = this.absoluteLightIntensityUnit;
            this.absoluteLightIntensityUnit = absoluteLightIntensityUnitNew;

            notifyAboutAbsoluteLightIntensityUnitChanged(absoluteLightIntensityUnitOld, absoluteLightIntensityUnitNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public int getStepCount(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        int phaseIntensityInPercent = calibrationPhases.get(phaseIndex).getStepCount();

        return phaseIntensityInPercent;
    }

    public void setStepCount(int phaseIndex, int stepCountNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        Validation.requireNonNegativeParameterName(stepCountNew, "stepCountNew");
        Validation.requireValueSmallerOrEqualToParameterName(stepCountNew, 100, "stepCountNews");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        int stepCountOld = phase.getStepCount();

        if(stepCountOld != stepCountNew)
        {
            phase.setStepCount(stepCountNew);
            notifyAboutStepCountChanged(phaseIndex, stepCountOld, stepCountNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public double getStepRecordingTime(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        double time = calibrationPhases.get(phaseIndex).getStepRecodingTime();

        return time;
    }

    public void setStepRecordingTime(int phaseIndex, double stepRecordingTimeNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        Validation.requireValueGreaterThanParameterName(stepRecordingTimeNew, 0., "stepRecordingTimeNew");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        double stepRecordingTimeOld = phase.getStepRecodingTime();

        if(Double.compare(stepRecordingTimeOld, stepRecordingTimeNew) != 0)
        {
            phase.setStepRecordingTime(stepRecordingTimeNew);
            notifyAboutStepRecordingTimeChanged(phaseIndex, stepRecordingTimeOld, stepRecordingTimeNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public StandardTimeUnit getStepRecordingTimeUnit(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        StandardTimeUnit unit = calibrationPhases.get(phaseIndex).getStepRecordingTimeUnit();

        return unit;
    }


    public void setStepRecordingTimeUnit(int phaseIndex, StandardTimeUnit stepRecodingTimeUnitNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        StandardTimeUnit stepRecordingTimeUnitOld = phase.getStepRecordingTimeUnit();

        if(!Objects.equals(stepRecordingTimeUnitOld, stepRecodingTimeUnitNew))
        {
            phase.setStepRecordingTimeUnit(stepRecodingTimeUnitNew);
            notifyAboutUnitOfStepRecordingTimeChanged(phaseIndex, stepRecordingTimeUnitOld, stepRecodingTimeUnitNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public double getPauseBetweenSteps(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        double time = calibrationPhases.get(phaseIndex).getPauseBetweenSteps();

        return time;
    }

    public void setPauseBetweenSteps(int phaseIndex, double pauseBetweenStepsNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        Validation.requireValueGreaterOrEqualToParameterName(pauseBetweenStepsNew, 0., "pauseBetweenStepsNew");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        double pauseBetweenStepsOld = phase.getPauseBetweenSteps();

        if(Double.compare(pauseBetweenStepsOld, pauseBetweenStepsNew) != 0)
        {
            phase.setPauseBetweenStops(pauseBetweenStepsNew);
            notifyAboutPauseBetweenStepsChanged(phaseIndex, pauseBetweenStepsOld, pauseBetweenStepsNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public StandardTimeUnit getPauseBetweenStepsUnit(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        StandardTimeUnit unit = calibrationPhases.get(phaseIndex).getPauseBetweenStepsUnit();

        return unit;
    }


    public void setPauseBetweenStepsUnit(int phaseIndex, StandardTimeUnit pauseBetweenStepsUnitNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        StandardTimeUnit pauseBetweenStepsUnitOld = phase.getPauseBetweenStepsUnit();

        if(!Objects.equals(pauseBetweenStepsUnitOld, pauseBetweenStepsUnitNew))
        {
            phase.setPauseBetweenStopsUnit(pauseBetweenStepsUnitNew);
            notifyAboutUnitOfPauseBetweenStepsChanged(phaseIndex, pauseBetweenStepsUnitOld, pauseBetweenStepsUnitNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public ScaleType getStepScaleType(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        ScaleType scaleType = phase.getStepScaleType();

        return scaleType;
    }

    public void setStepScaleType(int phaseIndex, ScaleType scaleTypeNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");
        Validation.requireNonNullParameterName(scaleTypeNew, "scaleType");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        ScaleType scaleTypeOld = phase.getStepScaleType();

        if(!Objects.equals(scaleTypeOld, scaleTypeNew))
        {
            phase.setStepScaleType(scaleTypeNew);
            notifyAboutScaleTypeOfStepsChanged(phaseIndex, scaleTypeOld, scaleTypeNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public SliderMountedFilter getActinicBeamFilter(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        SliderMountedFilter phaseDurationUnit = calibrationPhases.get(phaseIndex).getFilter();       
        return phaseDurationUnit;
    }

    public void setActinicBeamFilter(int phaseIndex, SliderMountedFilter filterNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");
        Validation.requireNonNullParameterName(filterNew, "filterNew");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        SliderMountedFilter filterOld = phase.getFilter();

        if(!Objects.equals(filterOld, filterNew))
        {
            phase.setFilter(filterNew);
            notifyAboutFilterChanged(phaseIndex, filterOld, filterNew);
        }
    }

    public List<SliderMountedFilter> getAvailableActinicBeamSliderMountedFilters()
    {        
        List<SliderMountedFilter> filters = Collections.unmodifiableList(opticsConfigurationModel.getAvailableActinicBeamSliderMountedFilters());

        return filters;
    }

    public double getMinimumIntensityInPercent(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        double phaseIntensityInPercent = calibrationPhases.get(phaseIndex).getMinimumIntensityInPercent();

        return phaseIntensityInPercent;
    }

    public void setMinimumIntensityInPercent(int phaseIndex, double minimumIntensityInPercentNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        Validation.requireNonNegativeParameterName(minimumIntensityInPercentNew, "minimumIntensityInPercentNew");
        Validation.requireValueSmallerOrEqualToParameterName(minimumIntensityInPercentNew, 100, "minimumIntensityInPercentNew");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        double minimumIntensityInPercentOld = phase.getMinimumIntensityInPercent();

        if(Double.compare(minimumIntensityInPercentOld, minimumIntensityInPercentNew) != 0)
        {
            phase.setMinimumIntensityInPercent(minimumIntensityInPercentNew);
            notifyAboutMinimumIntensityInPercentChanged(phaseIndex, minimumIntensityInPercentOld, minimumIntensityInPercentNew);
            checkWhetherAllSettingsSpecified();
        }
    }


    public double getMaximumIntensityInPercent(int phaseIndex)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        double phaseIntensityInPercent = phaseIndex < calibrationPhases.size() ? calibrationPhases.get(phaseIndex).getMinimumIntensityInPercent() : 0;

        return phaseIntensityInPercent;
    }

    public void setMaximumIntensityInPercent(int phaseIndex, double maximumIntensityInPercentNew)
    {
        Validation.requireNonNegativeParameterName(phaseIndex, "phaseIndex");
        Validation.requireValueSmallerThanParameterName(phaseIndex, calibrationPhases.size(), "phaseIndex");

        Validation.requireNonNegativeParameterName(maximumIntensityInPercentNew, "maximumIntensityInPercentNew");
        Validation.requireValueSmallerOrEqualToParameterName(maximumIntensityInPercentNew, 100, "maximumIntensityInPercentNew");

        ActinicPhaseAutomaticCalibration phase = calibrationPhases.get(phaseIndex);
        double maximumIntensityInPercentOld = phase.getMaximumIntensityInPercent();

        if(Double.compare(maximumIntensityInPercentOld, maximumIntensityInPercentNew) != 0)
        {
            phase.setMaximumIntensityInPercent(maximumIntensityInPercentNew);
            notifyAboutMaximumIntensityInPercentChanged(phaseIndex, maximumIntensityInPercentOld, maximumIntensityInPercentNew);
            checkWhetherAllSettingsSpecified();
        }
    }

    public List<FileFilter> getFileFiltersForReadingActinicBeamPhases()
    {
        return Collections.singletonList(CLMFileReaderFactory.getInstance().getFileFilter());
    }


    public int getActinicBeamPhaseCount()
    {
        return calibrationPhases.size();
    }

    public void setPhaseCount(int phaseCountNew)
    {        
        Validation.requireNonNegativeParameterName(phaseCountNew, "phaseCountNew");

        int phaseCountOld = calibrationPhases.size();

        if(phaseCountNew == phaseCountOld)
        {
            return;
        }

        if(phaseCountNew < phaseCountOld)
        {
            this.calibrationPhases = new ArrayList<>(calibrationPhases.subList(0, phaseCountNew));//we need to make sure it is arrray list for serialization purposes, as the returned sublist is not serializable
        }
        else if(phaseCountNew > phaseCountOld)
        {
            ActinicPhaseAutomaticCalibration templatePhase = phaseCountOld > 0 ? calibrationPhases.get(phaseCountOld - 1) : new ActinicPhaseAutomaticCalibration();

            for(int i = 0; i<(phaseCountNew - phaseCountOld); i++)
            {
                ActinicPhaseAutomaticCalibration recordingPhaseNew = new ActinicPhaseAutomaticCalibration(templatePhase);
                this.calibrationPhases.add(recordingPhaseNew);
            }

            checkWhetherAllSettingsSpecified();
        }

        notifyAboutNumberOfPhasesChanged(phaseCountOld, phaseCountNew);
    }

    public List<ActinicCalibrationPointImmutable> getCalibrationPhases()
    {
        List<ActinicCalibrationPointImmutable> immutablePhases = new ArrayList<>();

        for(ActinicPhaseAutomaticCalibration phase : calibrationPhases)
        {
            immutablePhases.add(phase.getImmutableCopy());
        }

        return immutablePhases;
    }


    public boolean isReadActinicBeamPhasesFromFileEnabled()
    {
        return readActinicBeamPhasesFromFileEnabled;
    }

    public void addListener(ActinicBeamAutomaticCalibrationModelListener listener)
    {
        modelListeners.add(listener);
    }

    public void removeListener(ActinicBeamAutomaticCalibrationModelListener listener)
    {
        modelListeners.remove(listener);
    }

    private void notifyAboutChangeInFilterPrompting(boolean promptForFilterChangeOld, boolean promptForFilterChangeNew)
    {
        if(promptForFilterChangeOld != promptForFilterChangeNew)
        {
            for(ActinicBeamAutomaticCalibrationModelListener receiver: modelListeners)
            {
                receiver.filterPromptingChanged(promptForFilterChangeOld, promptForFilterChangeNew);
            }
        }
    }


    private void notifyAbountIntensityPerVoltChanged(double intensityPerVoltOld, double intensityPerVoltNew)
    {
        if(Double.compare(intensityPerVoltOld, intensityPerVoltNew) != 0)
        {
            for(ActinicBeamAutomaticCalibrationModelListener receiver: modelListeners)
            {
                receiver.intensityPerVoltChanged(intensityPerVoltOld, intensityPerVoltNew);
            }
        }
    }

    private void notifyAboutNumberOfPhasesChanged(int oldNumber, int newNumber)
    {
        if(oldNumber != newNumber)
        {
            for(ActinicBeamAutomaticCalibrationModelListener receiver: modelListeners)
            {
                receiver.numberOfPhasesChanged(oldNumber, newNumber);
            }
        }
    }

    private void notifyAboutFilterChanged(int phaseIndex, SliderMountedFilter filterOld, SliderMountedFilter filterNew)
    {
        if(!Objects.equals(filterOld, filterNew))
        {
            for(ActinicBeamAutomaticCalibrationModelListener listener: modelListeners)
            {
                listener.filterChanged(phaseIndex, filterOld, filterNew);
            }
        }
    }

    private void notifyAboutMinimumIntensityInPercentChanged(int phaseIndex, double intensityInPercentOld, double intensityInPercentNew)
    {
        //we use Double.compare as it behaves predictably for NaNs
        if(Double.compare(intensityInPercentOld, intensityInPercentNew) != 0)
        {
            for(ActinicBeamAutomaticCalibrationModelListener listener: modelListeners)
            {
                listener.minimumActinicIntensityInPercentChanged(phaseIndex, intensityInPercentOld, intensityInPercentNew);
            }
        }
    }

    private void notifyAboutMaximumIntensityInPercentChanged(int phaseIndex, double intensityInPercentOld, double intensityInPercentNew)
    {
        //we use Double.compare as it behaves predictably for NaNs
        if(Double.compare(intensityInPercentOld, intensityInPercentNew) != 0)
        {
            for(ActinicBeamAutomaticCalibrationModelListener listener: modelListeners)
            {
                listener.maximumActinicIntensityInPercentChanged(phaseIndex, intensityInPercentOld, intensityInPercentNew);
            }
        }
    }


    private void notifyAboutStepRecordingTimeChanged(int phaseIndex, double stepRecordingTimeOld, double stepRecordingTimeNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.stepRecordingTimeChanged(phaseIndex, stepRecordingTimeOld, stepRecordingTimeNew);
        }
    }

    private void notifyAboutUnitOfStepRecordingTimeChanged(int phaseIndex, StandardTimeUnit stepRecordingTimeUnitOld, StandardTimeUnit stepRecodingTimeUnitNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.unitOfStepRecordingTimeChanged(phaseIndex, stepRecordingTimeUnitOld, stepRecodingTimeUnitNew);
        }
    }

    private void notifyAboutPauseBetweenStepsChanged(int phaseIndex, double pauseBetweenStepsOld, double pauseBetweenStepsNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.pauseBetweenStepsChanged(phaseIndex, pauseBetweenStepsOld, pauseBetweenStepsNew);
        }
    }

    private void notifyAboutUnitOfPauseBetweenStepsChanged(int phaseIndex, StandardTimeUnit pauseBetweenStepsUnitOld,StandardTimeUnit pauseBetweenStepsUnitNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.unitOfPauseBetweenStepsChanged(phaseIndex, pauseBetweenStepsUnitOld, pauseBetweenStepsUnitNew);
        }
    }

    private void notifyAboutScaleTypeOfStepsChanged(int phaseIndex, ScaleType scaleTypeOld, ScaleType scaleTypeNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.scaleTypeOfStepsChanged(phaseIndex, scaleTypeOld, scaleTypeNew);
        }
    }


    private void notifyAboutStepCountChanged(int phaseIndex, int stepCountOld, int stepCountNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.stepCountChanged(phaseIndex, stepCountOld, stepCountNew);
        }
    }

    private void notifyAboutAbsoluteLightIntensityUnitChanged(IrradianceUnitType absoluteLightIntensityUnitOld, IrradianceUnitType absoluteLightIntensityUnitNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.absoluteLightIntensityUnitChanged(absoluteLightIntensityUnitOld, absoluteLightIntensityUnitNew);
        }
    }

    public void saveToFile(File outputFile) throws UserCommunicableException
    {
        if(!savingToFileEnabled)
        {
            throw new IllegalStateException("Saveing of actinic beam calibration to file is not possible");
        }

        //        try {
        //            ABCSaver.getInstance().save(getImmutableCopy(), outputFile);
        //        } catch (SavingException e) {
        //            e.printStackTrace();
        //            throw new UserCommunicableException("Error occured during parsing the settings", e);
        //        }
    }

    public boolean isSaveToFileEnabled()
    {
        return savingToFileEnabled;
    }

    public void setSaveToFileEnabled(boolean savingToFileEnabledNew)
    {
        if(this.savingToFileEnabled != savingToFileEnabledNew)
        {
            boolean savingToFileEnabledOld = this.savingToFileEnabled;
            this.savingToFileEnabled = savingToFileEnabledNew;

            notifyAboutSaveToFileEnabledChanged(savingToFileEnabledOld, savingToFileEnabledNew);
        }
    }

    private void checkWhetherAllSettingsSpecified()
    {
        boolean necessaryDataKnown = this.absoluteLightIntensityUnit != null;
        if(necessaryDataKnown)
        {
            for(ActinicPhaseAutomaticCalibration phase : calibrationPhases)
            {
                necessaryDataKnown = necessaryDataKnown && phase.isWellSpecified();
                if(!necessaryDataKnown)
                {
                    break;
                }
            }
        }

        necessaryDataKnown = necessaryDataKnown && NumberUtilities.isNumeric(intensityPerVolt);

        setSaveToFileEnabled(necessaryDataKnown);
    }

    public void notifyAboutSaveToFileEnabledChanged(boolean savingToFileEnabledOld, boolean savingToFileEnabledNew)
    {
        for(ActinicBeamAutomaticCalibrationModelListener listener : modelListeners)
        {
            listener.saveToFileEnabledChanged(savingToFileEnabledOld, savingToFileEnabledNew);
        }
    }

    private void flushPreferences(Preferences pref)
    {
        try {
            PREF.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public ActinicBeamCalibrationImmutable getImmutableCopy()
    {
        ActinicBeamCalibrationImmutable immutableCopy = new ActinicBeamCalibrationImmutable(getCalibrationPhases(), absoluteLightIntensityUnit);
        return immutableCopy;
    }
}
