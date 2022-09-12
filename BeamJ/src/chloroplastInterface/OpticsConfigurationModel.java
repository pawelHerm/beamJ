package chloroplastInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;
import chloroplastInterface.optics.SliderMountedFilter;

public class OpticsConfigurationModel 
{
    private final static String FILTER_SETTINGS = "FilterSettings";

    private final static Preferences PREF = Preferences.userNodeForPackage(OpticsConfigurationModel.class).node(OpticsConfigurationModel.class.getName());

    private List<NeutralFilterSettings> filterSettings;
    private final List<OpticsConfigurationListener> listeners = new ArrayList<>();

    private IrradianceUnitType absoluteLightIntensityUnit;

    private final double initTransmittanceInPercents = Double.NaN;

    private boolean readFiltersFromFileEnabled;

    private boolean savingToFileEnabled;

    public OpticsConfigurationModel()
    {    
        this.filterSettings = (List<NeutralFilterSettings>)SerializationUtilities.getSerializableObject(PREF, FILTER_SETTINGS, new ArrayList<>(Arrays.asList(new NeutralFilterSettings(initTransmittanceInPercents))));
        updateSaveToFileEnabled();
    }

    public void close()
    {
        saveSettingsToPreferences();
    }

    private void saveSettingsToPreferences()
    {
        try {
            SerializationUtilities.putSerializableObject(PREF, FILTER_SETTINGS, this.filterSettings);
        } catch (ClassNotFoundException | IOException
                | BackingStoreException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        flushPreferences(PREF);
    }

    public double getFilterOpticalDensity(int filterIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(filterIndex, 0, filterSettings.size() - 1, "filterIndex");

        double opticalDensity = filterSettings.get(filterIndex).getOpticalDensity();
        return opticalDensity;
    }

    public double getTransmittanceInPercent(int filterIndex)
    {
        Validation.requireValueEqualToOrBetweenBounds(filterIndex, 0, filterSettings.size() - 1, "filterIndex");

        double transmittanceInPercent = filterSettings.get(filterIndex).getTransmittanceInPercents();
        return transmittanceInPercent;
    }

    public void setTransmittanceInPercent(int filterIndex, double transmittanceInPercentNew)
    {       
        Validation.requireValueEqualToOrBetweenBounds(filterIndex, 0, filterSettings.size() - 1, "filterIndex");
        Validation.requireValueEqualToOrBetweenBounds(filterIndex, 0, 100, "transmittanceInPercentNew");       

        NeutralFilterSettings settings = filterSettings.get(filterIndex);
        double transmittanceInPercentOld = settings.getTransmittanceInPercents();

        if(Double.compare(transmittanceInPercentOld, transmittanceInPercentNew) != 0)
        {
            settings.setTransmittanceInPercents(transmittanceInPercentNew);
            notifyTransmittanceInPercentChanged(filterIndex, transmittanceInPercentOld, transmittanceInPercentNew);
            updateSaveToFileEnabled();
        }
    }

    public void readInActinicCalibrationPhasesFromFile(File f) throws UserCommunicableException
    {               
        //        ActinicBeamCalibrationImmutable actnicBeamCalibration = ABCFileReader.getInstance().readActinicBeamSettings(f);
        //
        //        List<ActinicPhaseCalibrationImmutable> actnicBeamCalibrationPhases = actnicBeamCalibration.getActinicBeamPhaseCalibrations();
        //        int phaseCountNew = actnicBeamCalibrationPhases.size();
        //
        //        setPhaseCount(phaseCountNew);
        //
        //        for(int i = 0; i<phaseCountNew;i++)
        //        {
        //            ActinicPhaseCalibrationImmutable iThPhaseSettings = actnicBeamCalibrationPhases.get(i);
        //            setLightIntensityInAbsoluteUnits(i, iThPhaseSettings.getLightIntensityInAbsoluteUnits());
        //            setActinicBeamFilter(i, iThPhaseSettings.getFilter());
        //            setLightIntensityInPercent(i, iThPhaseSettings.getLightIntensityInPercent());
        //        }
    }

    public int getActinicBeamSliderMountedFilterCount()
    {
        return filterSettings.size();
    }

    public void setActinicBeamSliderMountedFilterCount(int filterCountNew)
    {        
        Validation.requireValueGreaterOrEqualToParameterName(filterCountNew, 0, "filterCountNew");

        int filterCountOld = filterSettings.size();

        if(filterCountNew == filterCountOld)
        {
            return;
        }

        if(filterCountNew < filterCountOld)
        {
            this.filterSettings = new ArrayList<>(filterSettings.subList(0, filterCountNew));//we need to make sure it is array list for serialization purposes, as the returned sublist is not serializable
        }
        else if(filterCountNew > filterCountOld)
        {
            double transmittanceInPercent = filterCountOld > 0 ? filterSettings.get(filterCountOld - 1).getTransmittanceInPercents(): initTransmittanceInPercents;

            for(int i = 0; i < (filterCountNew - filterCountOld); i++)
            {
                NeutralFilterSettings filterSettingsNew = new NeutralFilterSettings(transmittanceInPercent);
                this.filterSettings.add(filterSettingsNew);
            }

            updateSaveToFileEnabled();
        }

        notifyAboutNumberOfPhasesChanged(filterCountOld, filterCountNew);
    }
    //
    //    public List<ActinicPhaseCalibrationImmutable> getCalibrationPhases()
    //    {
    //        List<ActinicPhaseCalibrationImmutable> immutablePhases = new ArrayList<>();
    //
    //        for(ActinicPhaseCalibration phase : filterSettings)
    //        {
    //            immutablePhases.add(phase.getImmutableCopy());
    //        }
    //
    //        return immutablePhases;
    //    }


    public boolean isReadActinicBeamPhasesFromFileEnabled()
    {
        return readFiltersFromFileEnabled;
    }

    public void addListener(OpticsConfigurationListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(OpticsConfigurationListener listener)
    {
        listeners.remove(listener);
    }

    private void notifyAboutNumberOfPhasesChanged(int oldNumber, int newNumber)
    {
        if(oldNumber != newNumber)
        {
            for(OpticsConfigurationListener listener: listeners)
            {
                listener.numberOfActinicBeamFiltersChanged(oldNumber, newNumber);
            }
        }
    }

    private void notifyTransmittanceInPercentChanged(int filterIndex, double transmittanceInPercentOld, double transmittanceInPercentNew)
    {
        //we use Double.compare as it behaves predictably for NaNs
        if(Double.compare(transmittanceInPercentOld, transmittanceInPercentNew) != 0)
        {
            for(OpticsConfigurationListener listener: listeners)
            {
                listener.actinicBeamFilterTransmittanceInPercentChanged(filterIndex, transmittanceInPercentOld, transmittanceInPercentNew);
            }
        }
    }


    public void saveToFile(File outputFile) throws UserCommunicableException
    {
        if(!savingToFileEnabled)
        {
            throw new IllegalStateException("Saving of actinic beam calibration to file is not possible");
        }
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

    private void updateSaveToFileEnabled()
    {
        boolean necessaryDataKnown = this.absoluteLightIntensityUnit != null;
        if(necessaryDataKnown)
        {
            for(NeutralFilterSettings phase : filterSettings)
            {
                necessaryDataKnown = necessaryDataKnown && phase.isWellSpecified();
                if(!necessaryDataKnown)
                {
                    break;
                }
            }
        }

        setSaveToFileEnabled(necessaryDataKnown);
    }

    public void notifyAboutSaveToFileEnabledChanged(boolean savingToFileEnabledOld, boolean savingToFileEnabledNew)
    {
        for(OpticsConfigurationListener listener : listeners)
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


    public List<SliderMountedFilter> getAvailableActinicBeamSliderMountedFilters()
    {
        List<SliderMountedFilter> filters = new ArrayList<>();

        for(int i = 0;i < filterSettings.size();i++)
        { 
            NeutralFilterSettings fs = filterSettings.get(i);
            SliderMountedFilter filter = new SliderMountedFilter(i, fs.buildImmutableFilter());
            filters.add(filter);
        }

        return filters;
    }

    public SliderMountedFilter getActinicBeamSliderFilter(int filterIndex)
    {
        NeutralFilterSettings fs = filterSettings.get(filterIndex);
        SliderMountedFilter filter = new SliderMountedFilter(filterIndex, fs.buildImmutableFilter());

        return filter;
    }

    public OpticsConfigurationImmutable getOpticsConfiguration()
    {
        OpticsConfigurationImmutable configuration = new OpticsConfigurationImmutable(getAvailableActinicBeamSliderMountedFilters());
        return configuration;
    }
}
